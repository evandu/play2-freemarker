package play.api.freemarker

import java.util.{Calendar, Locale}
import java.util.concurrent.TimeUnit

import freemarker.cache.StringTemplateLoader
import freemarker.template.{Configuration, Version}
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.libs.iteratee.{Enumeratee, Iteratee}
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import specs2.run

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
  templateLoader.putTemplate("date.ftl",
    """
      |hello date now=${date?string("yyyy-MM-dd")}
      |datetime=${datetime?string("yyyy-MM-dd HH:mm:ss")}
      |boolean=${boolean?string("yes","no")}
      |num=${num}
      |double=${double}
      |f=${f}
      |b=${b}
    """.stripMargin)

  val strings = Iteratee.fold[String, String]("") { (s, e) => s + e}
  val toStr: Enumeratee[Array[Byte], String] = Enumeratee.map[Array[Byte]] { s => new String(s, "UTF-8")}

   implicit  val loc =  Locale.CHINESE
   object DefFreeMarkerTemplate {
     def apply() ={
       val cfg = new Configuration()
        cfg.setIncompatibleImprovements(new Version(2, 3, 20))
        sharedVariable.map(f=>cfg.setSharedVariable(f._1, f._2))
        cfg.setObjectWrapper(ScalaObjectWrapper)
        cfg.setDefaultEncoding("UTF-8")
        cfg.setDateFormat("yyyy-MM-dd")
        cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss")
        cfg.setTagSyntax(0)
        cfg.setLocale(loc)
         cfg.setTemplateLoader(templateLoader)
        new FreeMarkerTemplate(cfg)
     }
   }



  "Freemarker Application put Date and DateTime" should{
    "be render success " in {
      running(FakeApplication()){
       val c = Calendar.getInstance()
        c.set(2000, 7, 1, 0, 0, 0)
        Await.result[String](
          Iteratee.flatten(
            DefFreeMarkerTemplate().render(
            "date.ftl",
              Map(
                  "date"->c.getTime(),
                  "datetime" -> DateTime.parse("2014-08-11"),
                  "boolean" -> true,
                  "num" -> 3,
                  "double" -> 12321.0023d,
                  "f" -> 123.44,
                  "b" -> BigDecimal(21321)
               )
             ) |>> toStr &>> strings
          ).run ,  Duration(2,TimeUnit.SECONDS)
        ) must containing(
          """
           hello date now=2000-08-01
            |datetime=2014-08-11 00:00:00
            |boolean=yes
            |num=3
            |double=12,321.002
            |f=123.44
            |b=21,321
          """.trim.stripMargin
        )
      }
    }
  }

  "Freemarker Application put scala object" should {
        "be render success " in {
          running(FakeApplication()){
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