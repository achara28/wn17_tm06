package pythia.actors

import java.util.concurrent.TimeoutException

import akka.pattern.ask
import pythia.Global
import pythia.deploy.messages.ActorMessages
import pythia.deploy.messages.ActorMessages._
import pythia.exceptions.{MasterUnavailableException, PythiaException}
import pythia.models.generator.GeneratorInfo
import pythia.models.recorder.RecorderInfo

import scala.concurrent.Await
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
object MasterRef {

    val timeout = 5.seconds

    // TODO: add exception handling to present service unavailable view
    def askWithReply[T](message: ActorMessages, timeout: FiniteDuration): T = {
        try {
            val future = Global.masterActor.ask(message)(timeout)
            val result = Await.result(future, timeout)
            if (result == null) {
                throw new PythiaException("Actor returned null")
            }
            result.asInstanceOf[T]
        }
        catch {
            case t: TimeoutException =>
                throw new MasterUnavailableException("master did not respond", t)
        }
    }

    def getMasterState: MasterStateResponse = {
        try {
            askWithReply[MasterStateResponse](RequestMasterState, timeout)
        }
        catch {
            case ma: MasterUnavailableException =>
                MasterStateResponse("N/A", 0, "Offline", 0, 0, 0)
        }
    }

    def getGeneratorState: GeneratorStateResponse = {
        return GeneratorStateResponse(GeneratorInfo("Unknown", 0, 0, Seq(), "0"))
        try {
            askWithReply[GeneratorStateResponse](RequestGeneratorState, timeout)
        }
        catch {
            case ma: MasterUnavailableException =>
                GeneratorStateResponse(GeneratorInfo("Unknown", 0, 0, Seq(), "0"))
        }
    }

    def getRecorderState: RecorderStateResponse = {
        return  RecorderStateResponse(RecorderInfo("Unknown", 0, "--"))
        try {
            askWithReply[RecorderStateResponse](RequestRecorderState, timeout)
        }
        catch {
            case ma: MasterUnavailableException =>
                RecorderStateResponse(RecorderInfo("Unknown", 0, "--"))
        }
    }

    def getJobsState: JobsStateResponse = {
        return JobsStateResponse(Seq(), Seq())
        try {
            askWithReply[JobsStateResponse](RequestJobsState, timeout)
        }
        catch {
            case ma: MasterUnavailableException =>
                JobsStateResponse(Seq(), Seq())
        }
    }

//    def startRecorder(hdfsPath: String): Boolean = {
//        try {
//            val future = masterActor.ask(StartRecorder(hdfsPath))(timeout)
//            val result = Await.result(future, timeout)
//            if (result == null) {
//                throw new PythiaException("Actor returned null")
//            }
//            true
////            result.asInstanceOf[RecorderStarted]
//        }
//        catch {
//            case t: TimeoutException =>
//                throw new MasterUnavailableException("master did not respond", t)
//        }
//    }
//
//    def stopRecorder(): Boolean = {
//        try {
//            val future = masterActor.ask(StopRecorder)(timeout)
//            val result = Await.result(future, timeout)
//            if (result == null) {
//                throw new PythiaException("Actor returned null")
//            }
//            true
////            result.asInstanceOf[RecorderStopped]
//        }
//        catch {
//            case t: TimeoutException =>
//                throw new MasterUnavailableException("master did not respond", t)
//        }
//    }

}
