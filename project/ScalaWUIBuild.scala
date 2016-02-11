import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import com.earldouglas.xwp._

object ScalaWUIBuild extends Build {
  val Organization = "fr.iscpif"
  val Name = "iscpifWUI"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.7"
  val scalatraVersion = "2.4.0"
  val jettyVersion = "9.3.7.v20160115"
  val json4sVersion = "3.3.0"
  val httpComponentsVersion = "4.5.1"
  val scalatagsVersion = "0.5.4"
  val apacheDirectoryVersion = "1.0.0-M33"
  val Resolvers = Seq(Resolver.sonatypeRepo("snapshots"),
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  lazy val ext = Project(
    "ext",
    file("ext"),
    settings = Seq(
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers ++= Resolvers,
      libraryDependencies ++= Seq(
        "org.apache.directory.shared" % "shared-ldap-client-api" % "1.0.0-M13"
      )
    )
  ) enablePlugins (ScalaJSPlugin)

  lazy val rest = Project(
    "rest",
    file("rest"),
    settings = Seq(
      version := Version,
      organization := "fr.iscpif",
      scalaVersion := ScalaVersion,
      resolvers ++= Resolvers,
      libraryDependencies ++= Seq(
        "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion,
        "org.apache.httpcomponents" % "httpmime" % httpComponentsVersion,
        "org.json4s" %% "json4s-jackson" % json4sVersion
      )
    )
  ).dependsOn(ext)


  lazy val shared = project.in(file("./shared")).settings(
    scalaVersion := ScalaVersion
  ) dependsOn (ext)

  lazy val client = Project(
    "client",
    file("client"),
    settings = Seq(
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers ++= Resolvers,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "autowire" % "0.2.5",
        "com.lihaoyi" %%% "upickle" % "0.3.8",
        "com.lihaoyi" %%% "scalatags" % scalatagsVersion,
        "com.lihaoyi" %%% "scalarx" % "0.2.9",
        "fr.iscpif" %%% "scaladget" % "0.8.0-SNAPSHOT",
        "org.scala-js" %%% "scalajs-dom" % "0.8.2",
        "org.json4s" %% "json4s-jackson" % json4sVersion
      )
    )
  ).dependsOn(shared, ext) enablePlugins (ScalaJSPlugin)

  lazy val server = Project(
    "server",
    file("server"),
    settings = ScalatraPlugin.scalatraWithJRebel ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers ++= Resolvers,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "autowire" % "0.2.5",
        "com.lihaoyi" %% "upickle" % "0.3.8",
        "com.lihaoyi" %% "scalatags" % scalatagsVersion,
        "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion,
        "org.apache.httpcomponents" % "httpmime" % httpComponentsVersion,
        "org.scalatra" %% "scalatra" % scalatraVersion,
        "ch.qos.logback" % "logback-classic" % "1.1.3" % "runtime",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container",
        "org.json4s" %% "json4s-jackson" % json4sVersion,
        "org.apache.directory.shared" % "shared-ldap-client-api" % "1.0.0-M13",
        "org.apache.directory.shared" % "shared-ldap-codec-standalone" % "1.0.0-M13"
      )
    )
  ).dependsOn(shared, ext) enablePlugins (JettyPlugin)

  lazy val go = taskKey[Unit]("go")

  lazy val bootstrap = Project(
    "bootstrap",
    file("target/bootstrap"),
    settings = Seq(
      version := Version,
      scalaVersion := ScalaVersion,
      (go <<= (fullOptJS in client in Compile, resourceDirectory in client in Compile, target in server in Compile) map { (ct, r, st) =>
        copy(ct, r, new File(st, "webapp"))
      }
        )
    )
  ) dependsOn(client, server)


  private def copy(clientTarget: Attributed[File], resources: File, webappServerTarget: File) = {
    clientTarget.map { ct =>
      recursiveCopy(new File(resources, "webapp"), webappServerTarget)
      recursiveCopy(ct, new File(webappServerTarget, "js/" + ct.getName))
    }
  }

  private def recursiveCopy(from: File, to: File): Unit = {
    if (from.isDirectory) {
      to.mkdirs()
      for {
        f ← from.listFiles()
      } recursiveCopy(f, new File(to, f.getName))
    }
    else if (!to.exists() || from.lastModified() > to.lastModified) {
      println(s"Copy file $from to $to ")
      from.getParentFile.mkdirs
      IO.copyFile(from, to, preserveLastModified = true)
    }
  }

}
