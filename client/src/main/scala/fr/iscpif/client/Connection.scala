package fr.iscpif.client

import client.Post
import rx.core.Var
import shared.Api
import fr.iscpif.scaladget.api.{BootstrapTags ⇒ bs}
import bs._
import fr.iscpif.scaladget.tools.JsRxTags._
import scalatags.JsDom.tags
import scalatags.JsDom.all._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import autowire._
import rx._

/*
 * Copyright (C) 27/05/15 // mathieu.leclaire@openmole.org
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

class Connection {

  val connected: Var[Option[String]] = Var(None)

  val loginInput = bs.input("")(
    placeholder := "Login",
    width := "130px",
    autofocus
  ).render

  val passwordInput = bs.input("")(
    `type` := "password",
    width := "130px",
    placeholder := "Password",
    autofocus
  ).render

  val connectButton = bs.button("Connect", btn_primary)(`type` := "submit", onclick := { () =>
    connect(loginInput.value, passwordInput.value)
    false
  }).render

  def render =
    tags.div(
      Rx {
        connected() match {
          case Some(login: String) => tags.div("Hello " + login)
          case _ => tags.div(
            tags.form(`class` := "centerPage",
              tags.p(`class` := "grouptop", loginInput),
              tags.p(`class` := "groupbottom", passwordInput),
              connectButton
            )
          )
        }
      }
    ).render

  def connect(login: String, pass: String) = {
    println("in connect method")
    Post[Api].connect(login, pass).call().foreach { c =>
      connected() = {
        if (c) Some(login)
        else None
      }
    }
  }
}
