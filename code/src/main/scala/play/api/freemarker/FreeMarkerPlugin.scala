package play.api.freemarker
import java.io.File
import java.util.{ Properties, Locale}
import freemarker.template.{Configuration,  Version}
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.{Application, Plugin}
import scala.concurrent.ExecutionContext
/**
 * Created by evan on 14-8-28.
 */
object FreeMarker {

  private def freeMarkerPluginAPI(implicit app: Application): FreeMarkerTemplateAPI = {
    app.plugin[FreeMarkerPluginAPI] match {
      case Some(plugin) => plugin.fm
      case None => throw new Exception("There is no FreeMarker plugin registered. Make sure at least one FreeMarkerPlugin implementation is enabled.")
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
            channel.push(new String(cbuf, off, len).getBytes(cfg.getEncoding(loc)))
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

  val resourceName = app.configuration.getString("freemarker.config") getOrElse "freemarker.properties"

  val TEMPLATE_PATHS_KEY = "template_paths"

  val VERSION_KEY = "version_key"

  override def onStart(): Unit = {
    val p = new Properties();
    p.load(app.classloader.getResource(resourceName).openStream());
    val it = p.keySet.iterator
    while (it.hasNext) {
      val key: String = it.next.asInstanceOf[String]
      if(key.equalsIgnoreCase(TEMPLATE_PATHS_KEY)){
        cfg.setDirectoryForTemplateLoading(new File(p.getProperty(key).trim))
      } else if(key.equalsIgnoreCase(VERSION_KEY)){
        cfg.setIncompatibleImprovements(new Version(p.getProperty(key).trim))
      }else{
         cfg.setSetting(key, p.getProperty(key).trim)
      }
    }
    cfg.setObjectWrapper(ScalaObjectWrapper)
  }

  override lazy val enabled = {
    !app.configuration.getString("freemarkerplugin").filter(_ == "disabled").isDefined
  }

  override def fm: FreeMarkerTemplateAPI = new FreeMarkerTemplate(cfg)
}

