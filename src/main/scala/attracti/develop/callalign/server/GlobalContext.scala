package attracti.develop.callalign.server

import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config._
import java.util.concurrent.TimeUnit
/**
 * Created by Administrator on 24.12.2014.
 */


object GlobalContext {

  def configIt(settings: Config): Unit ={
    val tu= TimeUnit.SECONDS
    timeToPing=Duration(settings.getDuration("justin.timers.ping",tu), tu)
    pkgTimeToLive= Duration(settings.getDuration("justin.timers.pkgTimeToLive",tu), tu)
    delayIStatusReturn= Duration(settings.getDuration("justin.timers.delayIStatusReturn",tu), tu)
    delayIPsStop= Duration(settings.getDuration("justin.timers.delayIPsStop",tu), tu)
    delayUserStatusTo0= Duration(settings.getDuration("justin.timers.delayUserStatusTo0",tu), tu)
    delayUserToUserCallRQ= Duration(settings.getDuration("justin.timers.delayUserToUserCallRQ",tu), tu)
    delayUserRollBackFree= Duration(settings.getDuration("justin.timers.delayUserRollBackFree",tu), tu)
    delayIntentCalculatorStart= Duration(settings.getDuration("justin.timers.delayIntentCalculatorStart",tu), tu)
    periodicityIntentCalculatorStart= Duration(settings.getDuration("justin.timers.periodicityIntentCalculatorStart",tu), tu)




    bdUri=settings.getString("justin.bd.uri")


  }

  var bdUri:String=_

  var timeToPing:FiniteDuration= _
  var pkgTimeToLive:FiniteDuration=_// Timeout(2.seconds).duration
  var delayIStatusReturn:FiniteDuration= _//Timeout(40.seconds).duration
  var delayIPsStop:FiniteDuration=_// Timeout(1.minutes).duration
  var delayUserStatusTo0:FiniteDuration= _//Timeout(1.minutes).duration  //когда после дропа онекта менять статус
  var delayUserToUserCallRQ:FiniteDuration=_//Timeout(8.seconds).duration
  var delayUserRollBackFree:FiniteDuration=_//Timeout(15.seconds).duration//Если бзер ответил юзеру 5 что можно звонить когда он вернёт свой статус в предыдущий если, этот юзер всётаки не позвонит
  var delayIntentCalculatorStart:FiniteDuration=_//Timeout(20.seconds).duration//Сколько ждать после старта сервера чтобы запустить интенткалькулятор
  var periodicityIntentCalculatorStart:FiniteDuration=_//Timeout(20.seconds).duration//Сколько ждать после старта сервера чтобы запустить интенткалькулятор
}
