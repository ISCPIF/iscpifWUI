package fr.iscpif.doors.api

/*
 * Copyright (C) 25/10/16 // mathieu.leclaire@openmole.org
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
import slick.driver.H2Driver.api._


class UserChronicles(tag: Tag) extends Table[UserChronicle](tag, "USER_CHRONICLES") {
  def userID = column[User.Id]("USER_ID")
  def chronicleID = column[Chronicle.Id]("CHRONICLE")

  def * = (userID, chronicleID) <> ((UserChronicle.apply _).tupled, UserChronicle.unapply)

  def user = foreignKey("USER_FK", userID, users)(_.id)

}