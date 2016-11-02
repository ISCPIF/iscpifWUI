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

import monocle.macros._
import org.apache.directory.api.ldap.model.exception.{LdapAuthenticationException, LdapException, LdapInvalidDnException, LdapUnwillingToPerformException}
import org.apache.directory.ldap.client.api.exception.InvalidConnectionException

import scala.util.{Failure, Success, Try}

object Data {

  type ImagePath = String

  sealed trait LdapAuthentication

  case class LoginPassword(login: String, password: String) extends LdapAuthentication

  case class DnPassword(dn: String, password: String) extends LdapAuthentication

  object Anonymous extends LdapAuthentication

  type LDAPUserQuery = Either[LDAPUser, ErrorData]

  type UserQuery = Either[User, ErrorData]

  case class LDAPUser(dn: String,
                      givenName: String,
                      email: String,
                      description: String)

  object User {
    type Id = String

    implicit def ordering = new Ordering[User] {
      def compare(u1: User, u2: User) = u1.name compare u2.name
    }

    def emptyUser = User(java.util.UUID.randomUUID.toString, "", "", "", "")
  }

  object Lock {
    type Id = String
  }

  object Chronicle {
    type Id = String
  }

  object State{
    type Id = String
  }

  case class Chronicle(chronicle: Chronicle.Id, lock: Lock.Id, state: State.Id, time: Long, increment: Option[Long])

  case class UserID(id: User.Id)

  case class PartialUser(id: User.Id, name: String)

  case class Password(password: Option[String])

  sealed trait PassStatus {
    def message: String
  }

  case class PassUndefined(message: String = "Never changed") extends PassStatus

  case class PassError(message: String) extends PassStatus

  case class PassMatchOk(message: String = "Your new password is valid") extends PassStatus

  case class PassEmpty(message: String = "Empty password !") extends PassStatus

  case class PairOfPasses(oldpass: Password, newpass: Password, status: PassStatus)

  case class User(id: String, password: String, name: String, hashAlgorithm: String, hashParameters: String)


  case class Email(id: Chronicle.Id, email: String)

  case class UserChronicle(userID: User.Id, chronicle: Chronicle.Id)

  case class Version(id: Int)

  case class EmailConfirmation(chronicleID: Chronicle.Id, secret: String, deadline: Long)

  case class AdminUser(id: String, pass: String)

  @Lenses case class ErrorData(className: String, code: Int, message: String)


  // old UserQuery renamed to LDAPUserQuery
  object LDAPUserQuery {
    implicit def stackTrace(st: Array[StackTraceElement]): String = st.map {
      _.toString
    }.mkString("\n")

    implicit def tryUserToUserQuery(t: Try[LDAPUser]): LDAPUserQuery = apply(t)


    def apply(o: Try[LDAPUser]): LDAPUserQuery =
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

  case class EmailDeliveringError(message: String) extends Error

  case object UnauthorizedError extends Error {
    def message = "Not authorized to perform this action"
  }

  case class Capacity(authorized: Boolean) {
    def ||(otherCapacity: Capacity) = Capacity(authorized || otherCapacity.authorized)
  }

}