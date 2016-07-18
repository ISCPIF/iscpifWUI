package fr.iscpif.doors.server


/*
 * Copyright (C) 08/06/15 // mathieu.leclaire@openmole.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import fr.iscpif.doors.ext.Data._
import org.scalatra._

import scala.concurrent.ExecutionContext.Implicits.global
import upickle._
import autowire._
import shared._

import collection.JavaConversions._
import rx._
import scala.concurrent.duration._
import scala.concurrent.Await
import scalatags.Text.all._
import scalatags.Text.{all => tags}
import Utils._
import fr.iscpif.doors.api.AccessQuest

object AutowireServer extends autowire.Server[String, upickle.default.Reader, upickle.default.Writer] {
  def read[Result: upickle.default.Reader](p: String) = upickle.default.read[Result](p)

  def write[Result: upickle.default.Writer](r: Result) = upickle.default.write(r)
}

class Servlet(quests: Map[String, AccessQuest]) extends ScalatraServlet with AuthenticationSupport {

  val basePath = "shared"

  val connection = html("Client().connection();")

  def application = html(s"Client().application('${userIDFromSession.map{_.id}.getOrElse("")}');")

  val connectedUsers: Var[Seq[UserID]] = Var(Seq())
  val USER_ID = "UserID"

  def html(javascritMethod: String) = tags.html(
    tags.head(
      tags.meta(tags.httpEquiv := "Content-Type", tags.content := "text/html; charset=UTF-8"),
      tags.link(tags.rel := "stylesheet", tags.`type` := "text/css", href := "css/bootstrap.min.css"),
      tags.link(tags.rel := "stylesheet", tags.`type` := "text/css", href := "css/styleISC.css"),
      tags.script(tags.`type` := "text/javascript", tags.src := "js/client-opt.js"),
      tags.script(tags.`type` := "text/javascript", tags.src := "js/jquery.min.js"),
      tags.script(tags.`type` := "text/javascript", tags.src := "js/bootstrap.min.js")
    ),
    tags.body(tags.onload := javascritMethod)
  )

  protected def basicAuth() = {
    val baReq = new DoorsAuthStrategy(this)
    val rep = baReq.authenticate()
    rep match {
      case Some(u: UserID) =>
        response.setHeader("WWW-Authenticate", "Doors realm=\"%s\"" format realm)
        recordUser(u)
        Ok()
      case _ =>
        redirect("/connection")
    }
  }

  get("/") {
    redirect("/app")
  }

  get("/connection") {
    if (isLoggedIn) redirect("/app")
    else {
      response.setHeader("Access-Control-Allow-Origin", "*")
      response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
      response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
      contentType = "text/html"
      connection
    }
  }

  post("/connection") {
    response.setHeader("Access-Control-Allow-Origin", "*")
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
    basicAuth.status.code match {
      case 200 => redirect("/app")
      case _ => redirect("/connection")
    }
  }

  get("/app") {
    contentType = "text/html"
    if (isLoggedIn) application
    else redirect("/connection")
  }


  post("/logout") {
    userIDFromSession.foreach { u =>
      connectedUsers() = connectedUsers.now.filterNot {
        _ == u
      }
    }
    redirect("/connection")
  }

  def isLoggedIn: Boolean = userIDFromSession.map {
    connectedUsers.now.contains
  }.getOrElse(false)

  def recordUser(u: UserID) = {
    session.put(USER_ID, u)
    connectedUsers() = connectedUsers.now :+ u
  }

  def userIDFromSession =
    session.getAttribute(USER_ID) match {
      case u: UserID => Some(u)
      case _ => None
    }


  post("/api/user") {
    val login = params get "login" getOrElse ("")
    val pass = params get "password" getOrElse ("")
    val connectRequest = LdapConnection.connect(LoginPassword(login, pass))
    connectRequest match {
      case Left(u: LDAPUser) => Ok(u.toJson)
      case Right(e: ErrorData) => halt(e.code, e.toJson)
    }
  }

  post(s"/$basePath/*") {
    Await.result(AutowireServer.route[shared.Api](new ApiImpl(quests))(
      autowire.Core.Request(Seq(basePath) ++ multiParams("splat").head.split("/"),
        upickle.default.read[Map[String, String]](request.body))
    ), Duration.Inf)
  }

}
