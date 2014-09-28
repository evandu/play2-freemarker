package play.api.freemarker
import java.util
import freemarker.core.Environment
import freemarker.template.{TemplateDirectiveBody, TemplateModelException}
import org.apache.commons.lang3.StringUtils

/**
 * Created by spring on 14-9-28.
 */
package object directives {

   val ATTR_NAME = "name"
   val OVERRIDE_CURRENT_NODE = "__ftl_override_curr_node__"
   val BLOCK = "__ftl_override__";

   val overrideVariableName = (name:String) => BLOCK + name

   def requiredKey(map:util.Map[_, _], key:String) = {
     val v =  map.get(key)
     if(v == null || StringUtils.isBlank(v.toString)){
       throw new TemplateModelException("not found required key:"+key+" for directive");
     }
     v.toString
   }

  def getOverrideBody(env:Environment, name:String) = env.getVariable(overrideVariableName(name))
}
