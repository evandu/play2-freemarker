package play.api.libs.freemarker
import java.io.File
import freemarker.template.{ Version, TemplateExceptionHandler, Configuration }
import play.api.libs.iteratee.Concurrent
import scala.concurrent.ExecutionContext
import java.util.Locale

/**
 * Created by evan on 14-8-28.
 */



object DefaultFreeMarkerConfiguration {
  
  def apply(templatesPath:String,
      sharedVariable:Map[String, String] =  Map.empty,
      encoding:String = "UTF-8",
      debug:Boolean = false) =  new  DefaultFreeMarkerConfiguration(templatesPath,sharedVariable,encoding,debug)
  
}

class DefaultFreeMarkerConfiguration(templatesPath:String,sharedVariable:Map[String, String], encoding:String,debug:Boolean) extends FreeMarkerConfiguration



/**
 * trait FreeMarkerConfiguration
 */
trait FreeMarkerConfiguration {
  
    private val cfg: Configuration = new Configuration()
    
    val templatesPath:String
    
    val encoding:String = "UTF-8"
    val debug:Boolean = true
    val sharedVariable: Map[String, String] = Map.empty
    
    def withConfig(cfg: Configuration ) : Configuration = cfg
    
    def getConfig = {
        cfg.setObjectWrapper(ScalaObjectWrapper);
	    cfg.setIncompatibleImprovements(new Version(2, 3, 20));
	    cfg.setDirectoryForTemplateLoading(new File(templatesPath));
	    cfg.setDefaultEncoding(encoding);
	    if (debug) cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
	    sharedVariable.map(f => cfg.setSharedVariable(f._1, f._2))
	    withConfig(cfg)
    }

}


/**
 * trait FreeMarkerApplication  
 */
trait FreeMarkerApplication extends FreeMarkerConfiguration {
  
  self:FreeMarkerConfiguration =>

  def render(tpl: String)(data: Any)(implicit ec: ExecutionContext, loc: Locale) =
    Concurrent.unicast[Array[Byte]] { channel =>
      getConfig.getTemplate(tpl, loc).process(data,
	        new java.io.Writer() {
		       override def write(cbuf: Array[Char], off: Int, len: Int): Unit =
		            channel.push(new String(cbuf, off, len).getBytes("UTF-8"))
		
		       override def flush(): Unit = channel.end
		
		       override def close(): Unit = channel.eofAndEnd
	          
	        })
    }
}

