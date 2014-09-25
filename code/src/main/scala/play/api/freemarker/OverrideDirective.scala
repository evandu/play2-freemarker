package play.api.freemarker

import java.io.Writer
import java.util

import freemarker.core.Environment
import freemarker.template.{TemplateDirectiveBody, TemplateDirectiveModel, TemplateModel}
import org.apache.commons.lang3.StringUtils

/**
 * Created by evan on 14-8-28.
 */
class OverrideDirective extends TemplateDirectiveModel{
  val ATTR = "name"
  override def execute(env: Environment,
                       params: util.Map[_, _],
                       loopVars: Array[TemplateModel],
                       body: TemplateDirectiveBody): Unit = {
    val name = params.get(ATTR)
    if(name != null && StringUtils.isNotBlank(name.toString)){


    }
//    val name = DirectiveUtils.getRequiredParam(params, "name");
//    String overrideVariableName = DirectiveUtils.getOverrideVariableName(name);
//
//    TemplateDirectiveBodyOverrideWraper override = DirectiveUtils.getOverrideBody(env, name);
//    TemplateDirectiveBodyOverrideWraper current = new TemplateDirectiveBodyOverrideWraper(body,env);
//    if(override == null) {
//      env.setVariable(overrideVariableName, current);
//    }else {
//      DirectiveUtils.setTopBodyForParentBody(env, current, override);
//    }
  }
}

class TemplateDirectiveBodyOverrideWrapper(body:TemplateDirectiveBody, env:Environment) extends TemplateDirectiveBody with TemplateModel{
  override def render(out: Writer): Unit = ???
}



