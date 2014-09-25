package play.api.libs.freemarker

import com.typesafe.config.ConfigRenderOptions
import freemarker.template.{Configuration, Version, TemplateExceptionHandler}
import play.api.{Application, Plugin}
import play.api.libs.iteratee.{Enumerator, Concurrent}
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext
import java.util.Locale

/**
 * Created by evan on 14-8-28.
 */
object FreeMarker {

  private def freeMarkerPluginAPI(implicit app: Application): FreeMarkerTemplateAPI = {
    app.plugin[FreeMarkerPluginAPI] match {
      case Some(plugin) => plugin.fm
      case None => throw new Exception("There is no  FreeMarker plugin registered. Make sure at least one FreeMarkerPlugin implementation is enabled.")
    }
  }

  def render(tpl: String, data: Any)(implicit app: Application,ec: ExecutionContext, loc: Locale):Enumerator[Array[Byte]] ={
    freeMarkerPluginAPI.render(tpl: String, data: Any)
  }
}


trait FreeMarkerTemplateAPI {
   def render(tpl: String, data: Any)(implicit ec: ExecutionContext, loc: Locale):Enumerator[Array[Byte]]
}

class FreeMarkerTemplate(cfg:Configuration) extends FreeMarkerTemplateAPI{
  override def render(tpl: String, data: Any)(implicit ec: ExecutionContext, loc: Locale) =
    Concurrent.unicast[Array[Byte]] { channel =>
      cfg.getTemplate(tpl, loc).process(data,
        new java.io.Writer() {
          override def write(cbuf: Array[Char], off: Int, len: Int): Unit =
            channel.push(new String(cbuf, off, len).getBytes("UTF-8"))
          override def flush(): Unit = channel.end

          override def close(): Unit = channel.eofAndEnd

        })
    }
}
trait FreeMarkerPluginAPI extends Plugin {
  def fm:FreeMarkerTemplateAPI
}

class FreeMarkerPlugin(app:play.api.Application) extends  FreeMarkerPluginAPI {

  lazy val cfg = new Configuration()

  val config = app.configuration.getObject("freemarker.config").map(c=>Json.parse(c.render(ConfigRenderOptions.concise())))

  override def onStart(): Unit = {
    config match {
      case Some(c) =>
        cfg.setObjectWrapper(ScalaObjectWrapper)
        ( c \  "encoding").asOpt[String].map(e=>cfg.setDefaultEncoding(e))
        ( c \  "version").asOpt[String].map(e=>cfg.setIncompatibleImprovements(new Version(e)))
        ( c \  "debug").asOpt[Boolean].map(e=>if(e) cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER))
        ( c \  "templatePaths").asOpt[String].map(e=>cfg.setDirectoryForTemplateLoading(new java.io.File(e)))
      case None  => ???
    }
  }

  override lazy val enabled = {
    !app.configuration.getString("freemarkerplugin").filter(_ == "disabled").isDefined
  }

  override def fm: FreeMarkerTemplateAPI = new FreeMarkerTemplate(cfg)
}

