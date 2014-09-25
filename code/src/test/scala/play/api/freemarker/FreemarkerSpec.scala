package play.api.freemarker

import java.util.Locale
import java.util.concurrent.TimeUnit

import freemarker.cache.StringTemplateLoader
import freemarker.template.{Configuration, Version}
import org.specs2.mutable._
import play.api.libs.iteratee.{Enumeratee, Iteratee}
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration._
/**
 * Created by evan on 14-8-28.
 */

case class Parent(name:String,age:Int,title:String, children :List[Child])

case class Child(name:String,age:Int)

class FreemarkerSpec extends  Specification  {

  val sharedVariable: Map[String, String] = Map("staticUrl"->"http://static.jje.com")
  val templateLoader = new StringTemplateLoader
  templateLoader.putTemplate("test.ftl",
    """
      |hello name=${name} age=${age}, title=${title}
      |sharedVariable = ${staticUrl}
      |<#list children  as c>
      |<li>name = ${c.name} - age=${c.age}</li>
      |</#list>
    """.stripMargin
  )

   implicit  val loc = Locale.getDefault
   object DefFreeMarkerTemplate {
     def apply() ={
       val cfg = new Configuration()
        cfg.setTemplateLoader(templateLoader)
       cfg.setIncompatibleImprovements(new Version(2, 3, 20))
       sharedVariable.map(f=>cfg.setSharedVariable(f._1, f._2))
       cfg.setObjectWrapper(ScalaObjectWrapper)
       cfg.setDefaultEncoding("UTF-8")
       new FreeMarkerTemplate(cfg)
     }
   }

  "Freemarker Application put scala object" should {
        "be render success " in {
          running(FakeApplication()){
            val strings = Iteratee.fold[String, String]("") { (s, e) => s + e}
            val toStr: Enumeratee[Array[Byte], String] = Enumeratee.map[Array[Byte]] { s => new String(s, "UTF-8")}
            Await.result[String](
              Iteratee.flatten(
                DefFreeMarkerTemplate().render("test.ftl",Parent("Parent", 35, "teacher", List(Child("child1", 5), Child("child2", 6)))) |>> toStr &>> strings
              ).run ,  Duration(2,TimeUnit.SECONDS)

            ) must containing(
              """
                |hello name=Parent age=35, title=teacher
                |sharedVariable = http://static.jje.com
                |<li>name = child1 - age=5</li>
                |<li>name = child2 - age=6</li>
              """.trim.stripMargin
            )
          }

        }
    }


  "Freemarker Application put Json Object" should {
    "be render success " in {
      running(FakeApplication()){
        val strings = Iteratee.fold[String, String]("") { (s, e) => s + e}
        val toStr: Enumeratee[Array[Byte], String] = Enumeratee.map[Array[Byte]] { s => new String(s, "UTF-8")}
        Await.result[String](
          Iteratee.flatten(
            DefFreeMarkerTemplate().render("test.ftl",
              Json.parse("""
                   |{
                   |"name":"Parent",
                   |"age":35,
                   |"title":"teacher",
                   |"children":[
                   |{
                   |"name":"child1",
                   |"age":5
                   |},
                   |{
                   |"name":"child2",
                   |"age":6
                   |}
                   |]
                   |}
                   |""".stripMargin)
            ) |>> toStr &>> strings
          ).run ,  Duration(2,TimeUnit.SECONDS)

        ) must containing(
          """
            |hello name=Parent age=35, title=teacher
            |sharedVariable = http://static.jje.com
            |<li>name = child1 - age=5</li>
            |<li>name = child2 - age=6</li>
          """.trim.stripMargin
        )
      }

    }
  }


}