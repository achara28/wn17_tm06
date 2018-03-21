package controllers

import _root_.util.JsonReply
import _root_.util.actions.{AuthenticatedAction, AdminJsonAction}
import com.couchbase.client.java.document.json.JsonArray
import com.couchbase.client.java.error.DocumentDoesNotExistException
import com.couchbase.client.java.view.{Stale, ViewQuery}
import play.api.libs.json._
import play.api.mvc.Controller
import pythia.db.CouchbaseHelper
import pythia.db.CouchbaseViews._

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Copyright (c) 2015 Chrysovalantis Anastasiou (canast02) - http://dmsl.cs.ucy.ac.cy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions
 *  of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 */
object JobsController extends Controller {

    def listJobs = AdminJsonAction {
        val (running, finished) = fetchAllJobs

        val jobsJson = Json.toJson(Map(
            "running" -> running,
            "finished" -> finished
        ))

        Ok(jobsJson)
    }

    def jobInfo(jobId: String) = AuthenticatedAction { request =>
        val connection = CouchbaseHelper.mainBucket

        val vr = connection.query(
            ViewQuery.from(JOBS_DOCUMENT, JOBS_BY_ID_VIEW).key(jobId).stale(Stale.FALSE)
        )
        val rows = vr.allRows()

        if(rows.size()==0) {
            Ok(JsonReply.error("job not found"))
        }
        else {
            val doc = rows.get(0).value().asInstanceOf[String]
            val jsValue = Json.parse(doc)

            if (jsValue != null && (jsValue \ "userId").as[String] == request.user.userId) {
                Ok(jsValue)
            }
            else if (jsValue != null && request.user.role == "admin") {
                Ok(jsValue)
            }
            else {
                Ok(JsonReply.error("job not found"))
            }
        }
    }

    def deleteJob(jobId: String) = AdminJsonAction {
        val connection = CouchbaseHelper.mainBucket
        try {
            connection.remove(jobId)
            Ok(JsonReply.success("successfully deleted!"))
        }
        catch {
            case d: DocumentDoesNotExistException =>
                Ok(JsonReply.error("could not delete job history"))
        }
    }

    def userJobs() = AuthenticatedAction { request =>
        val (running, finished) = fetchUserJobs(request.user.userId)

        val jobsJson = Json.toJson(Map(
            "running" -> running,
            "finished" -> finished
        ))

        Ok(jobsJson)
    }

    // Jobs fetching from Couchbase
    case class JobMeta(jobId: String,appId: String, jobName: String, userId: String, isFinished: Boolean, timestamp: Option[Long] = None)
    implicit val formats = Json.format[JobMeta]
    def fetchAllJobs: (Seq[JobMeta], Seq[JobMeta]) = {

        val connection = CouchbaseHelper.mainBucket
        val resp = connection.query(
            ViewQuery.from(JOBS_DOCUMENT, JOBS_META_VIEW).stale(Stale.FALSE)
        )

        val running = mutable.MutableList[JobMeta]()
        val finished = mutable.MutableList[JobMeta]()

        resp.foreach(row => {
            val jsv = Json.parse(row.value().toString)
            val jm =
                jsv.validate[JobMeta].map {
                        case j: JobMeta => j
                } recoverTotal {
                    e => JobMeta("","", "", "", isFinished = true)
                }

            if(jm.isFinished)
                finished += jm
            else
                running += jm
        })

        (running.toSeq, finished.toSeq)
    }

    def fetchUserJobs(userId: String): (Seq[JobMeta], Seq[JobMeta]) = {
        val connection = CouchbaseHelper.mainBucket
        val resp = connection.query(
            ViewQuery.from(JOBS_DOCUMENT, JOBS_BY_USER_ID_VIEW).keys(JsonArray.from(userId, "user_shared")).stale(Stale.FALSE)
        )

        val running = mutable.MutableList[JobMeta]()
        val finished = mutable.MutableList[JobMeta]()

        resp.foreach(row => {
            val jsv = Json.parse(row.value().toString)
            val jm =
                jsv.validate[JobMeta].map {
                    case j: JobMeta => j
                } recoverTotal {
                    e => JobMeta("","", "", "", isFinished = true)
                }

            if(jm.isFinished)
                finished += jm
            else
                running += jm
        })

        (running.toSeq, finished.toSeq)
    }



}