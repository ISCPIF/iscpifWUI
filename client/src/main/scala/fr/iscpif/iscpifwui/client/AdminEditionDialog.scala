package fr.iscpif.iscpifwui.client

import fr.iscpif.doors.client.{UserEditionPanel, Post}
import fr.iscpif.doors.ext.Data.{PartialUser, User}
import fr.iscpif.scaladget.api.{BootstrapTags => bs}
import fr.iscpif.scaladget.stylesheet.{all ⇒ sheet}
import autowire._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import shared.Api
import fr.iscpif.scaladget.tools.JsRxTags._
import scalatags.JsDom.{all => tags}
import tags._
import sheet._
import rx._

/*
 * Copyright (C) 27/04/16 // mathieu.leclaire@openmole.org
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

object AdminEditionDialog {
  def apply(id: bs.ModalID) = new AdminEditionDialog(id)
}

class AdminEditionDialog(_modalID: bs.ModalID) extends ModalPanel {

  lazy val modalID = _modalID

  val users: Var[Seq[User]] = Var(Seq())
  val userEdition: Var[Option[User]] = Var(None)


  val addUserButton = bs.button("Add", () => {
    Post[Api].addUser(PartialUser(
      s"id + ${java.util.UUID.randomUUID().toString}",
      s"login + ${java.util.UUID.randomUUID().toString}",
      s"password + ${java.util.UUID.randomUUID().toString}",
      s"name + ${java.util.UUID.randomUUID().toString}",
      s"email + ${java.util.UUID.randomUUID().toString}"
    )
    ).call().foreach { u =>
      getUsers
    }
  })(sheet.btn_primary +++ btn_right)


  def getUsers =
    Post[Api].allUsers.call().foreach { u =>
      users() = u
    }


  val userTable = tags.table(sheet.table +++ sheet.striped)(
    tbody(
      Rx {
        for {u <- users()} yield {
          ReactiveLine(u).render
        }
      }
    )
  )

  val lineHovered: Var[Option[User]] = Var(None)

  case class ReactiveLine(user: User) {

    val render = tr(row)(
      onmouseover := { () ⇒ lineHovered() = Some(user) },
      onmouseout := { () ⇒ lineHovered() = None },
      td(colMD(4), a(user.name, pointer, onclick := { () => userEdition() = Some(user) })),
      td(colMD(7), "States ..."),
      td(colMD(1),
        tags.span(Rx {
          glyph_trash +++ pointer +++(lineHovered() == Some(user), opaque, transparent)
        },
          onclick := { () ⇒
            Post[Api].removeUser(user).call().foreach { u =>
              getUsers
            }
          }
        )
      )
    )

  }

  val dialog = bs.modalDialog(
    modalID,
    bs.headerDialog(
      h3("Admin panel"),
      addUserButton
    ),
    bs.bodyDialog(
      Rx {
        userEdition() match {
          case Some(u: User) =>
            val userpanel = UserEditionPanel.userPanel(u, () => save)
            div(userpanel.panel, userpanel.saveButton)
          case _ => span()
        }
      },
      userTable),
    bs.footerDialog(
      closeButton
    )
  )

  getUsers

  def save = {
    userEdition() = None

  }

  val render = this.dialog
}
