package util

import org.json.JSONArray
import play.api.libs.json._

import scala.collection.mutable

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
object JsonReply {

  def error(message: String): JsObject = {
    replyWithStatus("error", message)
  }

  def success(message: String): JsObject = {
    replyWithStatus("success", message)
  }

  def replyWithStatus(status: String, message: String): JsObject = {
    Json.obj(
      "status" -> status,
      "message" -> message
    )
  }

  def replyWith[T <: Any](status: String, obj: T)(implicit writes: Writes[T]): JsObject = {
    Json.obj(("status", status)) ++ Json.toJson(obj).as[JsObject]
  }


  def replyWithJSON(status: String, arr: JSONArray): JsValue = {
    val s = arr.toString
    Json.parse(s)
  }

}
