package pythia.master

import akka.actor.{Actor, Address}
import pythia.deploy.messages.ActorMessages.{MasterStateResponse, RequestMasterState}
import pythia.deploy.messages.DeployMessages._
import pythia.logging.PythiaLogging
import pythia.master.MasterMessages.{BoundPortRequest, BoundPortResponse, CheckForJobTimeOut}
import pythia.models.PythiaApiKeyValidation.{ApiKeyValidation, InvalidApiKey, ValidApiKey}
import pythia.models._
import pythia.security.SecurityManager
import pythia.util.Utils
import util.AppVariables

import scala.collection.mutable
import scala.concurrent.duration._

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
private[pythia] class MasterActor(host: String,
                                  port: Int) extends Actor with PythiaLogging {

  import java.text.SimpleDateFormat
  import java.util.Date

  import akka.actor.ActorRef
  import context.dispatcher   // to use Akka's scheduler.schedule()

  Utils.checkHost(host, "Expected hostname")

  //////////////////////////////////////////////////////////////////////

  // constants and helpers
  def createDateFormat      = new SimpleDateFormat("yyyyMMddHHmmss")
  val JOB_TIMEOUT           = AppVariables.getConfig.getInt("pythia.deploy.job.timeout").getOrElse(10000)

  //////////////////////////////////////////////////////////////////////

  // master start time - uptime
  var startTime = new Date()

  var nextJobNumber = 0
  val jobs = new mutable.HashSet[JobInfo]()
  val idToJob = new mutable.HashMap[String, JobInfo]()
  val actorToJob = new mutable.HashMap[ActorRef, JobInfo]()
  val addressToJob = new mutable.HashMap[Address, JobInfo]()

  // security manager - user authentication
  val securityManager = new SecurityManager

  // the public address for web ui and rest api
  val masterPublicAddress = {
    val envVar = System.getenv("PYTHIA_PUBLIC_DNS")
    if (envVar != null) envVar else host
  }
  val masterUrl = "pythia://" + host + ":" + port
  //////////////////////////////////////////////////////////////////////

  override def preStart() {
    super.preStart()
    log.debug("Starting Pythia master at " + masterUrl)
    log.info(s"Running Pythia version ${pythia.PYTHIA_VERSION}")

    context.system.scheduler.schedule(0.seconds, JOB_TIMEOUT.millis, self, CheckForJobTimeOut)

    log.info("Started Pythia master at " + masterUrl)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message) // calls postStop()!
    log.error(s"Master actor restarting due to exception: ${reason.getClass.getSimpleName}", reason)
  }

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    this.startTime = new Date()
    log.error(s"Master actor restarted due to exception: ${reason.getClass.getSimpleName}", reason)
  }

  override def postStop() {
    log.info("Master actor stopped")
  }

  // master message passing
  def receive = {

    case RegisterJob(description) =>
      log.debug(s"Registering job '${description.name}'")
      val apiKeyValidation = validatePythiaKey(description.apiKey)
      apiKeyValidation match {
        case ValidApiKey(user) =>
          val job = createJob(description, user, sender())
          registerJob(job)
          log.info("Registered job '" + description.name + "' with ID '" + job.id + "'")
          sender ! RegisteredJob(job.id, user)

        case InvalidApiKey =>
          log.warn(s"Rejected job '${description.name}' due to invalid ApiKey")
          sender ! InvalidKeyProvided
      }

    case RegisterJobWithId(description, jobId) =>
      log.info("Registering job " + description.name + " with specified id " + jobId)
      val apiKeyValidation = validatePythiaKey(description.apiKey)
      apiKeyValidation match {
        case ValidApiKey(user) =>
          val job = createJob(description, jobId, user, sender())
          registerJob(job)
          job.state = JobState.RUNNING
          job.lastHeartbeat = System.currentTimeMillis()
          log.info("Registered job " + description.name + " with ID " + job.id)
          sender ! RegisteredJob(job.id, user)

        case InvalidApiKey =>
          log.warn(s"Rejected job '${description.name}' due to invalid ApiKey")
          sender ! InvalidKeyProvided
      }

    case UnregisterJob(jobId) =>
      log.info("Unregistering job " + jobId)
      if(idToJob.contains(jobId))
        finishJob(idToJob(jobId))
      sender ! UnregisteredJob(jobId)

    case StartedJob(jobId, jobStartTime) =>
      idToJob(jobId).markStarted(jobStartTime)
      sender ! UpdatedJob(jobId)

    case BoundPortRequest =>
      sender ! BoundPortResponse(port)

    //        case StartRecorder(hdfsPath) =>
    //            if(Recorder.isStopped)
    //                Recorder.start(hdfsPath)
    //            sender ! RecorderStarted
    //
    //        case StopRecorder =>
    //            if(Recorder.isStarted)
    //                Recorder.stop()
    //            sender ! RecorderStopped

    /** Requesting modules state **/

    case RequestMasterState =>
      sender ! MasterStateResponse(host, port, "ALIVE", jobs.size, 0, uptime)

    //        case RequestJobsState =>
    //            val j = jobs.toSeq
    //            val cj = completedJobs.toSeq
    //            sender ! JobsStateResponse(j, cj)
    //
    //        case RequestGeneratorState =>
    //            sender ! GeneratorStateResponse(GeneratorApi.getGeneratorInfo)
    //
    //        case RequestRecorderState =>
    //            sender ! RecorderStateResponse(Recorder.info())

    /********/

    case Heartbeat(jobId) =>
      if (idToJob.contains(jobId)) {
        val job = idToJob(jobId)
        if (job.state == JobState.RUNNING)
          idToJob(jobId).lastHeartbeat = System.currentTimeMillis()
        else
          log.warn(s"Received heartbeat from a non running job with id '$jobId'")
      }
      else {
        log.warn(s"Received heartbeat from unregistered job with id '$jobId'")
        //Do nothing
      }

    case CheckForJobTimeOut =>
      timeoutDeadJobs()

    case x: Any =>
      log.info(s"Nothing to do: $x")
  }

  //////////////////////////////////////////////////////////////////////

  def newApplicationId(submitDate: Date): String = {
    val appId = "job-%s-%04d".format(createDateFormat.format(submitDate), nextJobNumber)
    nextJobNumber += 1
    appId
  }

  def validatePythiaKey(key: String): ApiKeyValidation = {
    securityManager.checkApiKey(key)
  }

  def createJob(desc: JobDescription, user: String, driver: ActorRef): JobInfo = {
    val now = System.currentTimeMillis()
    val date = new Date(now)
    new JobInfo(newApplicationId(date), desc, user, date, driver)
  }

  def createJob(desc: JobDescription, jobId: String, user: String, driver: ActorRef): JobInfo = {
    val now = System.currentTimeMillis()
    val date = new Date(now)
    new JobInfo(jobId, desc, user, date, driver)
  }

  def registerJob(job: JobInfo): Unit = {
    val appAddress = job.jobActor.path.address
    if (addressToJob.contains(appAddress)) {
      log.warn("Attempted to re-register job at same address: " + appAddress)
      return
    }

    jobs += job
    idToJob(job.id) = job
    actorToJob(job.jobActor) = job
    addressToJob(appAddress) = job
  }

  def finishJob(app: JobInfo) {
    removeJob(app, JobState.FINISHED)
  }

  def killJob(app: JobInfo) {
    //TODO: send kill signal to job
    removeJob(app, JobState.FAILED)
  }

  def removeJob(job: JobInfo, state: JobState.Value) {
    if (jobs.contains(job)) {
      log.info("Removing job " + job.id)
      jobs -= job
      idToJob -= job.id
      actorToJob -= job.jobActor
      addressToJob -= job.jobActor.path.address

      job.markFinished(state)
      if (state != JobState.FINISHED) {
        job.jobActor ! JobRemoved(state.toString)
      }
    }
  }

  def timeoutJob(job: JobInfo) {
    if (jobs.contains(job)) {
      log.info("Removing job " + job.id)
      jobs -= job
      idToJob -= job.id
      actorToJob -= job.jobActor
      addressToJob -= job.jobActor.path.address
    }
    job.markFinished(JobState.FAILED)
  }

  def timeoutDeadJobs(): Unit = {
    val currentTime = System.currentTimeMillis()
    val toRemove = jobs.filter(_.lastHeartbeat < currentTime - JOB_TIMEOUT).toArray
    toRemove.foreach(job => {
      if(job.state == JobState.RUNNING) {
        log.info(s"Removing job '${job.id}' because of no heartbeat for the last $JOB_TIMEOUT seconds")
        timeoutJob(job)
      }
    })
  }

  def uptime = {
    new Date().getTime - startTime.getTime
  }

}
