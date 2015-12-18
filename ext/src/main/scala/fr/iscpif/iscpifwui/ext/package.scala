package fr.iscpif.iscpifwui.ext

/*
 * Copyright (C) 05/09/15 // mathieu.leclaire@openmole.org
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

package object ldap {


  type LdapAttribute = String

  val email: LdapAttribute = "mail"
  val cn: LdapAttribute = "cn"
  val givenName: LdapAttribute = "giveName"
  val name: LdapAttribute = "name"
  val surname: LdapAttribute = "sn"
  val uid: LdapAttribute = "uid"
  val dn: LdapAttribute = "dn"

}