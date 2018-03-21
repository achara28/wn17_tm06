package controllers

import java.io.FileNotFoundException

import play.Play
import play.api.mvc._
import pythia.actors.MasterRef
import util.actions.AuthenticatedAction

/**
 * Copyright (c) 2015 Chrysovalantis Anastasiou (canast02) - http://dmsl.cs.ucy.ac.cy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 */
object Application extends Controller {

  case class Tab(name: String, icon: String, target: Call)

  val tabs = Seq(
    Tab("Dashboard", "fa-dashboard", routes.Application.dashboard()),
    Tab("Generator", "fa-gears", routes.Application.generator()),
    Tab("Recorder", "fa-hdd-o", routes.Application.recorder()),
    Tab("Jobs", "fa-tasks", routes.Application.jobs()),
    Tab("Editor", "fa-edit", routes.Application.editor()),
    Tab("Developers", "fa-university", routes.Application.developersPage()),
    Tab("Manuals", "fa-book", routes.Application.manuals())
  )

  def loginPage() = Action {
    Ok(views.html.login())
  }

  def index() = Action {
    Redirect(routes.Application.dashboard())
  }

  def developersPage() = Action {
    Ok(views.html.developers())
  }

  def dashboard() = AuthenticatedAction {
    val masterState = MasterRef.getMasterState
    val (running, finished) = JobsController.fetchAllJobs
    val generatorInfoResp = MasterRef.getGeneratorState
    val recorderInfoResp = MasterRef.getRecorderState
    Ok(views.html.dashboard(masterState, running, finished, generatorInfoResp.generatorInfo, recorderInfoResp.recorderInfo))
  }

  def recorder() = AuthenticatedAction {
    val recorderInfoResp = MasterRef.getRecorderState
    Ok(views.html.recorder(recorderInfoResp.recorderInfo))
  }

  def generator() = AuthenticatedAction {
    val generatorInfoResp = MasterRef.getGeneratorState
    Ok(views.html.generator(generatorInfoResp.generatorInfo))
  }

  def manuals() = AuthenticatedAction {
    val generatorInfoResp = MasterRef.getGeneratorState
    Ok(views.html.manuals(generatorInfoResp.generatorInfo))
  }

  def editor() = AuthenticatedAction {
    var lines = ""
    try {
      val source = Play.application().resourceAsStream("/public/examples/CountUsersPerCellTower.scala")
      lines = try scala.io.Source.fromInputStream(source).mkString finally source.close()
    } catch {
      case fnfe: FileNotFoundException => lines = "//Example file not found " // more specific cases first !
      case e: Exception => lines = "//Something went wrong" // more specific cases first !

    }
    Ok(views.html.editor(lines))
  }

  def jobs() = AuthenticatedAction {
    val (historyRunning, historyFinished) = JobsController.fetchAllJobs
    Ok(views.html.jobs(historyRunning, historyFinished))
  }

  def InternalServerErrorPage() = Action {
    Ok(views.html.errorPage())
  }

  def NotFoundPage() = play.mvc.Results.TODO
}