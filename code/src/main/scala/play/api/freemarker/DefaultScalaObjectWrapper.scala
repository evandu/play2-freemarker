package play.api.freemarker

import java.lang.reflect.Method
import java.util

import freemarker.ext.beans.BeansWrapper
import freemarker.template._
import play.api.libs.json._

/**
 * Created by evan on 14-8-15.
 */

sealed trait TemplateScalaModel extends TemplateHashModel with TemplateScalarModel


case class ScalaMapModel[T](map: Map[String,T], wrapper: ObjectWrapper) extends TemplateScalaModel {

  def isEmpty = map.isEmpty

  def get(key: String): TemplateModel = wrapper.wrap(map.getOrElse(key, null))

  def getAsString = if (isEmpty) null else map.toString
}

case class ScalaListModel(data: Seq[Any], wrapper: ObjectWrapper) extends TemplateSequenceModel {
  def size = data.size
  def get(idx: Int) = wrapper.wrap(data(idx))
}



case class CaseClassModel(obj: Any, wrapper: BeansWrapper) extends TemplateScalaModel {

  def getMethods(clazz: Class[_], methodName: String): List[Method] = clazz.getMethods filter (_.getName equals methodName) toList match {
    case Nil if clazz != classOf[Object] => getMethods(clazz.getSuperclass, methodName)
    case d => d
  }
  def isEmpty = obj == null
  @throws(classOf[TemplateModelException])
  def get(methodName: String): TemplateModel = {

    getMethods(obj.getClass, methodName) match {
      case Nil => wrapper.wrap(null)
      case List(head) if head.getParameterTypes.length == 0 => getValue(head)
      case List(head)=>
        println(s"...................................${methodName}")
        ScalaSimpleMethodModel(obj, head.getName, head.getParameterTypes, wrapper)
      case methods @head::tail => ScalaOverloadedMethodModel(obj, methods, wrapper)
    }
  }
  
  

  def getAsString = if (obj == null) null else obj.toString

  @throws(classOf[TemplateModelException])
  private[this] def getValue(method: Method): TemplateModel =
    try {
      wrapper.wrap(method.invoke(obj))
    } catch {
      case e: Exception => throw new TemplateModelException(s"invoking method ${method.getName} occurred Exception", e)
    }

}

case class JSONObjectModel(json: JsValue, wrapper: ObjectWrapper) extends TemplateScalaModel {
  def isEmpty = json == null
  @throws(classOf[TemplateModelException])
  def get(key: String): TemplateModel = json \ key match {
    case undefined: JsUndefined => wrapper.wrap(null)
    case JsNull => wrapper.wrap(null)
    case array: JsArray => wrapper.wrap(array.value)
    case str: JsString => new SimpleScalar(str.value)
    case num: JsNumber => wrapper.wrap(num.value)
    case b: JsBoolean => wrapper.wrap(b.value)
    case jsObj => wrapper.wrap(jsObj)
  }
  def getAsString = if (json == null) null else json match {
    case str: JsString => str.value
    case v => v.toString()
  }
}

case class ScalaSimpleMethodModel(obj: Any, methodName: String, parameterTypes: Array[Class[_]], wrapper: BeansWrapper) extends TemplateMethodModelEx {

  override def exec(args: util.List[_]): Object = {
    val arguments = parameterTypes.zip(args.asInstanceOf[List[TemplateModel]]) map {c=>
      c match {
        case (clazz, model) => wrapper.unwrap(model, clazz)
      }
    }

    val method = obj.getClass.getMethod(methodName, parameterTypes: _*)
    method.invoke(obj, arguments: _*)
  }
}

case class ScalaOverloadedMethodModel(obj: Any, methods: List[Method], wrapper: BeansWrapper) extends TemplateMethodModelEx {

  override  def exec(arguments: util.List[_]): Object = {

    val potentialMethods = methods.view.filter(_.getParameterTypes.length == arguments.size).iterator
    while (potentialMethods.hasNext) {
      val method = potentialMethods.next
      try {
        val typedArguments = getTypedArguments(arguments.asInstanceOf[List[TemplateModel]], method.getParameterTypes)
        return method.invoke(obj, typedArguments: _*)
      }
      catch {
        case e:Throwable =>
      }
    }
    null
  }

  def getTypedArguments(arguments: List[TemplateModel], types: Array[Class[_]]): Array[Object] =
    types.zip(arguments).map { c=>
      c match {
        case (clazz, model) => wrapper.unwrap(model, clazz)
      }
    }

}

object ScalaObjectWrapper extends DefaultObjectWrapper {
  override def wrap(target: scala.Any) =
    target match {
      case map: Map[String,_]=> ScalaMapModel(map, this)
      case list: Seq[Any] => ScalaListModel(list, this)
      case js: JsValue => JSONObjectModel(js, this)
      case None => wrap(null)
      case Some(data) => wrap(data)
      case data: Any => CaseClassModel(data, this)
      case obj => super.wrap(obj)
    }
}
