package fr.iscpif.doors.client

import fr.iscpif.doors.ext.Data._
import fr.iscpif.scaladget.api.{BootstrapTags ⇒ bs}
import bs._
import fr.iscpif.scaladget.tools.JsRxTags._
import org.scalajs.dom.raw.HTMLDivElement
import shared.Api
import scalatags.JsDom.tags
import scalatags.JsDom.all._
import autowire._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import rx._

/*
 * Copyright (C) 23/09/15 // mathieu.leclaire@openmole.org
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

object ServiceWall {
  def apply(user: LDAPUser, authentication: LoginPassword) = new ServiceWall(user, authentication)
}

class ServiceWall(_user: LDAPUser, authentication: LoginPassword) {
  val user = Var(_user)
  val userEdition = UserEdition(user(), authentication, this)
  val services = Seq(
    ServiceLink("OwnCloud", Resources.owncloud, "http://owncloud.iscpif.fr", "File sharing"),
    ServiceLink("Gogs", Resources.gogs, "http://gogs.iscpif.fr", "Code sharing"),
    ServiceLink("Jenkins", Resources.jenkins, "http://jenkins.iscpif.fr", "Continous integration"),
    ServiceLink("Seminar", Resources.seminar, "http://webcast.iscpif.fr/stream.webm", "Seminar streaming"),
    ServiceLink("EGI Certificate", Resources.egi, "https://igc.services.cnrs.fr/usercert/?CA=GRID2-FR&lang=fr", "Procedure on how to get a digital Grid certificate"),
    ServiceLink("Complex-systems VO", Resources.vo, "https://voms.grid.auth.gr:8443/voms/vo.complex-systems.eu/", "Subscribe to the VO complex-systems.eu")
  )

  val userPanel = new UserEditionPanel("testpanel", userEdition)

  val userInfoButton = bs.glyphButton(
      "Edit your info",
      btn_default + btn_small + btn_right,
      glyph_settings,
      { () =>  bs.showModal(userPanel.modalID) }
  )
  // private def setLDAPEdition = userEdition() = UserEdition(user(), authentication, this)

  val render: HTMLDivElement = tags.div(`class` := "fullpanel")(
    tags.div(`class` := Rx {
      s"centerpanel"
    })(
      bs.div("user")(Rx {
        s"${user().givenName}"
      },
        userInfoButton
      ),
        BootstrapTags.thumbs(services).render,
        tags.img(src := Resources.isc, `class` := "logoISC")
          ,
      userPanel.dialog
      )
  ).render

}