package controllers

import java.io.File

import play.Play
import play.api.mvc._
import utilities.Utils
import utilities.actions.AuthenticatedAction
import views.html.defaultpages.notFound

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
  /** serve the index page app/views/index.scala.html */
  def index(any: String) = Action {
    Ok(views.html.index("SPATE", Utils.getPythiaBaseName.get))
  }

  /** resolve "any" into the corresponding HTML page URI */
  def getURI(any: String): String = any match {

    case "home" => "/public/app/home/home.view.html"
    case "login" => "/public/app/login/login.view.html"
    case "moving" => "/public/app/maps/moving.view.html"
    case "location" => "/public/app/maps/location.view.html"
    case "nms" => "/public/app/maps/nms.view.html"
    case "million" => "/public/app/maps/million.view.html"
    case "admin" => "/public/app/admin/admin.view.html"
    case _ => "error"
  }
  /** load an HTML page from public/html */
  def loadPublicHTML(any: String) = Action {
    val file = Play.application().resourceAsStream(getURI(any))

    if (file != null)
      Ok(scala.io.Source.fromInputStream(file, "UTF-8").mkString).as("text/html");
    else
      NotFound
  }

}