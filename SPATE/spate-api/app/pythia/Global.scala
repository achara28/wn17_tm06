package pythia

import java.io.File

import _root_.util.{UIUtils, AppVariables}
import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import play.api._
import pythia.db.{CouchbaseHelper, Couchbase}
import pythia.db.Couchbase.CouchbaseBucket
import pythia.logging.PythiaLogging
import pythia.master.MasterActor
import pythia.util.ActorSystemUtils

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
object Global extends GlobalSettings with PythiaLogging {

    private var _actorSystemPort: Int = _
    private var _actorSystem: ActorSystem = _
    private var _masterActor: ActorRef = _

    def actorSystem = _actorSystem
    def masterActor = _masterActor

    override def onStart(app: Application) {
        val couchbaseNodes = AppVariables.getConfig.getStringSeq("pythia.couchbase.nodes").getOrElse(Seq())

        val main_bucket_n = AppVariables.getConfig.getString("pythia.db.bucket.main.name").get
        val main_bucket_p = AppVariables.getConfig.getString("pythia.db.bucket.main.password").get
        val main_bucket = CouchbaseBucket(main_bucket_n, main_bucket_p)
        val hist_bucket_n = AppVariables.getConfig.getString("pythia.db.bucket.history.name").get
        val hist_bucket_p = AppVariables.getConfig.getString("pythia.db.bucket.history.password").get
        val hist_bucket = CouchbaseBucket(hist_bucket_n, hist_bucket_p)
        log.info("Couchbase initialization")
        Couchbase.initialize(couchbaseNodes, Seq(main_bucket, hist_bucket))
        CouchbaseHelper.setMainBucketName(main_bucket_n)
        CouchbaseHelper.setHistoryBucketName(hist_bucket_n)

        val desiredPort = AppVariables.getConfig.getInt("pythia.deploy.master.port").getOrElse(4444)

        val (s, p) = ActorSystemUtils.createActorSystem(ActorSystemUtils.masterActorSystemName, ActorSystemUtils.actorSystemHost, desiredPort)
        _actorSystem = s
        _actorSystemPort = p
        // Initializing Master actor
        _masterActor = _actorSystem.actorOf(Props(classOf[MasterActor], ActorSystemUtils.actorSystemHost, _actorSystemPort), ActorSystemUtils.masterActorName)

        log.info("Application has started")
    }

    override def onStop(app: Application) {
        actorSystem.shutdown()
        Couchbase.shutdown()
        log.info("Application shutdown...")
    }

    override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
        log.debug(s"Loading ${mode.toString} configuration")
        val modeSpecificConfig = config ++ Configuration(ConfigFactory.load(s"application.${mode.toString.toLowerCase}.conf"))
        super.onLoadConfig(modeSpecificConfig, path, classloader, mode)
    }



    //TODO: error pages
//    override def onError(request: RequestHeader, ex: Throwable) = {
//        Future.successful(InternalServerError(
////            views.html.errorPage(ex)
//        ))
//    }
//
//    override def onHandlerNotFound(request: RequestHeader) = {
//        Future.successful(NotFound(
//            views.html.notFoundPage(request.path)
//        ))
//    }
}
