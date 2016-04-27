package fr.iscpif.doors.server

import fr.iscpif.doors.ext.Data.User
import slick.dbio.DBIOAction
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration
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

package object database {
  lazy val users = TableQuery[Users]
  lazy val states = TableQuery[States]

  lazy val db = Database.forDriver(
    driver = new org.h2.Driver,
    url = s"jdbc:h2:/${Settings.dbLocation}"
  )

  def query[T](f: DBIOAction[T, slick.dbio.NoStream, scala.Nothing]) = Await.result(db.run(f), Duration.Inf)

  def newUser(login: String, password: String, name: String, email: String, hashAlgo: String = Hashing.currentJson) =
    User(id = 0L, login, Hashing(password), name, email, hashAlgo)
}