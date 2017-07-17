package fr.iscpif.doors.client

import fr.iscpif.scaladget.api.{BootstrapTags => bs}
import bs._

import scalatags.JsDom.all._
import fr.iscpif.doors.ext.Data._
import fr.iscpif.scaladget.stylesheet.{all => sheet}
import fr.iscpif.scaladget.tools.JsRxTags._
import autowire._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.scalajs.dom.raw.HTMLElement
import sheet._
import rx._
import shared.Api

import scala.concurrent.Future


object PassEdition {
  // default should be 8
  val PASS_MIN_CHAR = 8

  def newUser = new PassEdition(new NewUserPasswordForm)

  def oldUser = new PassEdition(new OldUserPasswordForm)

  def validatePassString(passString: String): Boolean = (passString.length > PASS_MIN_CHAR)

  trait PasswordForm {

    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()
    val passStatus: Var[PassStatus] = Var(PassUndefined())

    val errorToShow = Rx {
      passStatus() match {
        case _@(_: PassMatchOk | _: PassUndefined) => false
        case _: PassStatus => true
      }
    }

    val stringError = Rx {
      passStatus() match {
        case ps@(_: PassMatchOk | _: PassUndefined) => None
        case danger: PassStatus => Some(danger.message)
      }
    }

    def isStatusOK = status.map { s =>
      passStatus() = s
      s match {
        case ok: PassMatchOk => true
        case _ => false
      }
    }

    val passStyle: ModifierSeq = Seq(
      `type` := "password"
    )

    val oldPassInput = bs.input("")(placeholder := "Previous password", passStyle).render
    val newPassInput1 = bs.input("")(placeholder := "New password", passStyle).render
    val newPassInput2 = bs.input("")(placeholder := "New password again", passStyle).render

    def reset = {
      oldPassInput.value = ""
      newPassInput1.value = ""
      newPassInput2.value = ""
    }

    protected def passChecking: PassStatus = {
      (newPassInput1.value, newPassInput2.value) match {
        case ("", "") => PassEmpty()
        case ("", _) => PassError("You did not fill the first password")
        case (_, "") => PassError("You did not fill the second password")
        case (p1, p2) if p1 != p2 => PassError("The passwords don't match !")
        case (p1, _) if !validatePassString(p1) => PassError("Passwords match but this new password is too simple")
        case _ => PassMatchOk()
      }
    }

    protected def status: Future[PassStatus]

    def render: HTMLElement
  }

  class NewUserPasswordForm extends PasswordForm {
    def render = div(
      bs.vForm(width := "100%")(
        newPassInput1.withLabel("Password"),
        newPassInput2.withLabel("Repeat password")
      )
    ).render

    def status = Future(passChecking)

  }

  class OldUserPasswordForm extends PasswordForm {
    def render = div(
      bs.vForm(width := "100%")(
        oldPassInput.withLabel("Previous password"),
        newPassInput1.withLabel("Password"),
        newPassInput2.withLabel("Repeat password")
      )
    ).render

    def status = Post[Api].isPasswordValid(oldPassInput.value).call().map { passOK =>
      if (passOK) passChecking
      else PassError("The old password is not correct.")
    }
  }


}

import PassEdition._

class PassEdition(passForm: PasswordForm) {

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  val errorToShow = passForm.errorToShow

  val stringError = passForm.stringError

  def isStatusOK = passForm.isStatusOK

  def reset = passForm.reset


  def newPassword = passForm.newPassInput2.value

  lazy val panel = passForm

  lazy val errorPanel = Rx {
    bs.dangerAlert("", stringError().getOrElse(""), errorToShow)()
  }

  lazy val panelWithError = div(
    panel.render,
    errorPanel
  )

}