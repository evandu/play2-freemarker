package play.api.libs.freemarker

import java.lang.reflect.{ Method, Modifier }

import freemarker.ext.beans.{ NumberModel, BooleanModel, StringModel, BeansWrapper }
import freemarker.template._
import play.api.libs.json._

/**
 * Created by evan on 14-8-15.
 */

sealed trait TemplateScalaModel extends TemplateHashModel with TemplateScalarModel


case class ScalaMapModel(map: Map[String, Any], wrapper: ObjectWrapper) extends TemplateScalaModel {

  def isEmpty = map.isEmpty

  def get(key: String): TemplateModel = wrapper.wrap(map.getOrElse(key, null))

  def getAsString = if (isEmpty) null else map.toString
}

case class ScalaListModel(data: Seq[Any], wrapper: ObjectWrapper) extends TemplateSequenceModel {
  def size = data.size
  def get(idx: Int) = wrapper.wrap(data(idx))
}



case class CaseClassModel(obj: Any, wrapper: ObjectWrapper) extends TemplateScalaModel {

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
      case _ => ???
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

object ScalaObjectWrapper extends DefaultObjectWrapper {
  override def wrap(target: scala.Any) =
    target match {
      case map: Map[String, Any] => ScalaMapModel(map, this)
      case list: Seq[Any] => ScalaListModel(list, this)
      case js: JsValue => JSONObjectModel(js, this)
      case None => wrap(null)
      case Some(data) => wrap(data)
      case data: Any => CaseClassModel(data, this)
      case obj => super.wrap(obj)
    }
}
