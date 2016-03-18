package fr.iscpif.doors.ext

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
import org.apache.directory.api.ldap.model.exception.{LdapException, LdapAuthenticationException, LdapInvalidDnException, LdapUnwillingToPerformException}
import org.apache.directory.ldap.client.api.exception.InvalidConnectionException
import scala.util.{Failure, Success, Try}

object Data {

  type ImagePath = String

  sealed trait LdapAuthentication

  case class LoginPassword(login: String, password: String) extends LdapAuthentication

  case class DnPassword(dn: String, password: String) extends LdapAuthentication

  object Anonymous extends LdapAuthentication

  type UserQuery = Either[LDAPUser, ErrorData]

  case class LDAPUser(dn: String,
                      givenName: String,
                      email: String,
                      description: String)

  case class User(id: Long = 0, login: String, password: String, name: String, email: String, hashAlgorithm: String)

  case class ErrorData(className: String, code: Int, message: String)

  object UserQuery {
    implicit def stackTrace(st: Array[StackTraceElement]): String = st.map {
      _.toString
    }.mkString("\n")

    implicit def tryUserToUserQuery(t: Try[LDAPUser]): UserQuery = apply(t)


    def apply(o: Try[LDAPUser]): UserQuery =
      o match {
        case Success(t) => Left(t)
        case Failure(ex: Throwable) =>
          Right(ex match {
            case lde: LdapException => lde match {
              case e: InvalidConnectionException => HttpError(404, LDAPInvalidConnectionError("Cannot connect to the server"))
              case e: LdapUnwillingToPerformException => HttpError(401, LDAPUnwillingToPerformError("Please, give a password"))
              case e: LdapInvalidDnException => HttpError(401, LDAPInvalidDNError("User not found"))
              case e: LdapAuthenticationException => HttpError(401, LDAPAuthenticationError("Invalid login or password"))
              case _ => HttpError(400, OtherLDAPError(lde.getClass.toString, lde.getMessage, lde.getStackTrace))
            }
            case e: HttpError => e
            case x: Any => HttpError(400, UnexceptedError(ex.getMessage, ex.getStackTrace))
          }
          )
      }
  }

  // REST API

  object HttpError {

    implicit def httpErrorToErrorData(e: HttpError): ErrorData = ErrorData(e.error.map {
      _.getClass.toString.split('$').last
    }.getOrElse(""), e.code, e.error.map {
      _.message
    }.getOrElse(""))

    def apply(c: Int, e: Error): HttpError = HttpError(c, Some(e))

    def apply(c: Int, e: Option[Error]): HttpError = new HttpError {
      def code: Int = c

      def error: Option[Error] = e
    }
  }

  sealed trait Error {
    def message: String
  }


  sealed trait HttpError {
    def code: Int

    def error: Option[Error]
  }

  case class LDAPInvalidConnectionError(message: String) extends Error

  case class LDAPUnwillingToPerformError(message: String) extends Error

  case class LDAPAuthenticationError(message: String) extends Error

  case class LDAPInvalidDNError(message: String) extends Error

  case class OtherLDAPError(exceptionName: String, message: String, stack: String) extends Error

  case class UnexceptedError(message: String, stackTrace: String, level: Option[String] = None) extends Error

}
