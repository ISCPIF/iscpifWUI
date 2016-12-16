//package fr.iscpif.doors.server
//
//import org.scalatra.auth.ScentryStrategy
//import fr.iscpif.doors.ext.Data
//import org.scalatra.ScalatraBase
//
///*
// * Copyright (C) 24/06/16 // mathieu.leclaire@openmole.org
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//class DoorsAuthStrategy(protected override val app: ScalatraBase, authenticated: (String, String) => Option[Data.UserID]) extends ScentryStrategy[Data.UserID] {
//
//  def authenticate()(implicit r: javax.servlet.http.HttpServletRequest,
//                     response: javax.servlet.http.HttpServletResponse): Option[Data.UserID] = {
//    val email = app.params.getOrElse("email", "")
//    val password = app.params.getOrElse("password", "")
//
//    authenticated(email, password)
//
//    //val result = Utils.connect(db)(email, password, salt)
//
////    authenticated(email, password) match {
////      case true => Some(UserID(result.head.id))
////      case false => None
////
////    }
////    if(authenticated) Some(erID(result.head.id))
////
////    if (result.isEmpty) None
////    else Some(UserID(result.head.id))
//
//  }
//
//  protected def getUserId(user: Data.UserID)(
//    implicit request: javax.servlet.http.HttpServletRequest,
//    response: javax.servlet.http.HttpServletResponse): String = user.id
//
//}
