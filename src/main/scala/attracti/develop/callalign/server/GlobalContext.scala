package attracti.develop.callalign.server

import akka.util.Timeout
import scala.concurrent.duration._

/**
 * Created by Administrator on 24.12.2014.
 */


object GlobalContext {
  val timeToPing= Timeout(1.seconds)
//  val timeOfPkgLife= Timeout(1.seconds)
  val timeToCallRequest=3000
  val timeIntIntentsCalculator= Timeout(5.seconds).duration
  val timeIntentRQToTCP= Timeout(4.seconds).duration
  val timeToWeightCallIntentAnswer= Timeout(7.seconds).duration

  val timeToWeightCallIntentAnswerTCP= Timeout(6.seconds).duration
  val timeToWeightCallIntent= Timeout(12.minutes).duration
  val defoultPkgTimeToLive= Timeout(2.seconds).duration

}
