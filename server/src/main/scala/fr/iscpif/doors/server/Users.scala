package fr.iscpif.doors.server

import fr.iscpif.doors.ext.Data.User
import slick.driver.H2Driver.api._

/*
 * Copyright (C) 16/03/16 // mathieu.leclaire@openmole.org
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

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def login = column[String]("LOGIN")
  def password = column[String]("PASSWORD")
  def hashAlgorithm = column[String]("HASH_ALGORITHM")
  def name = column[String]("NAME")
  def email = column[String]("EMAIL")

  def * = (id, login, password, hashAlgorithm, name, email) <> (User.tupled, User.unapply)
}