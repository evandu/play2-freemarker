package play.api.freemarker.directives

import java.io.Writer
import java.util

import freemarker.cache.TemplateCache
import freemarker.core.Environment
import freemarker.template.{ TemplateDirectiveBody, TemplateModel, TemplateDirectiveModel}

/**
 * Created by spring on 14-9-29.
 */

class TemplateDirectiveBodyOverrideWrapper(body:TemplateDirectiveBody,
                                           env:Environment) extends TemplateDirectiveBody with TemplateModel{
  override def render(out: Writer): Unit = {
      if(body != null){
        try {
          env.setVariable(OVERRIDE_CURRENT_NODE, this)
          body.render(out)
        }finally {
          env.setVariable(OVERRIDE_CURRENT_NODE, env.getVariable(OVERRIDE_CURRENT_NODE))
        }
      }
  }
}

/**
 *  override
 */
object OverrideDirective extends TemplateDirectiveModel{
  override def execute(env: Environment, params: util.Map[_, _], loopVars: Array[TemplateModel], body: TemplateDirectiveBody): Unit = {
    val name = requiredKey(params,"name")
    val overrideVar = overrideVariableName(name)
    val overrideNode = getOverrideBody(env, name)
    if(overrideNode == null) {
      env.setVariable(overrideVar, new TemplateDirectiveBodyOverrideWrapper(body,env))
    }else {
      env.setVariable(overrideVar, overrideNode)
    }
  }
}

/**
 * block
 */
object BlockDirective extends TemplateDirectiveModel{
  override def execute(env: Environment, params: util.Map[_, _], loopVars: Array[TemplateModel], body: TemplateDirectiveBody): Unit = {
    val overrideBody = getOverrideBody(env, requiredKey(params, "name"))
    if(overrideBody == null) {
       if(body != null) body.render(env.getOut())
    }else {
       overrideBody.asInstanceOf[TemplateDirectiveBodyOverrideWrapper].render(env.getOut())
    }
  }
}

/**
 * extends
 */
object ExtendsDirective extends TemplateDirectiveModel  {
  override def execute(env: Environment, params: util.Map[_, _], loopVars: Array[TemplateModel], body: TemplateDirectiveBody): Unit = {
    env.include(TemplateCache.getFullTemplatePath(env,currentTemplateDir, requiredKey(params,"name")),null, true)
    def currentTemplateDir = {
      val templateName = env.getTemplate().getName()
      if(templateName.indexOf('/') == -1) "" else templateName.substring(0, templateName.lastIndexOf('/') + 1)
    }
  }
}


