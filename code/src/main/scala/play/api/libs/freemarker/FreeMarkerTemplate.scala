package play.api.libs.freemarker
import java.io.File
import freemarker.template.{ Version, TemplateExceptionHandler, Configuration }
import play.api.libs.iteratee.Concurrent
import scala.concurrent.ExecutionContext
import java.util.Locale

/**
 * Created by evan on 14-8-28.
 */
trait FreeMarkerConfiguration {
  val cfg = {
    println("........................................")
    val c = new Configuration()
    val encoding:String = "UTF-8"
    val debug:Boolean = true
    c.setObjectWrapper(ScalaObjectWrapper);
    c.setIncompatibleImprovements(new Version(2, 3, 20));
    c.setDefaultEncoding(encoding);
    if (debug) c.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    c
  }

}

/**
 * trait FreeMarkerApplication  
 */
  trait FreeMarkerTemplate {
    self:FreeMarkerConfiguration =>
    def render(tpl: String)(data: Any)(implicit ec: ExecutionContext, loc: Locale) =
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

