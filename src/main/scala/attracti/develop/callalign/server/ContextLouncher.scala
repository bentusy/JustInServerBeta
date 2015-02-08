package attracti.develop.callalign.server

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import akka.event.Logging.{InitializeLogger, LoggerInitialized}
import akka.util.Timeout
import com.typesafe.config._
//import _root_.org.apache.logging.log4j._
import akka.actor._
import akka.io.{IO, Tcp}
import attracti.develop.callalign.server.bd.{BDManager, BDHandler}
import attracti.develop.callalign.server.conections.TcpConnectionHandlerActor
import attracti.develop.callalign.server.intents.{IntentCalculator, IntentManager}
import attracti.develop.callalign.server.users.UserManager
import attracti.develop.callalign.server.utill._
//import org.apache.logging.log4j.core.config.Configurator
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable.{ArrayBuilder => ARB, Map => MMap, Set => MSet}
import scala.util.Properties.{lineSeparator => newLine}

object CallAlign extends App {
//  Configurator.initialize(null, "./resources/log4j2.xml")
//
//  var log:Logger = LogManager.getLogger("InfoLoger")
  val myConf = ConfigFactory.load("loggers.conf")
  val config = ConfigFactory.load()
  val conf = myConf.withFallback(config)


  val system = ActorSystem("hostsystem", ConfigFactory.load(conf))

GlobalContext.configIt(system.settings.config)

  val bdHandler= new BDHandler()

  val DAO = system.actorOf(Props(new BDManager(bdHandler)), name = "DAO")

  val intentManager = system.actorOf(Props(new IntentManager(DAO)), name = "intentManager")


  val usermeneger = system.actorOf(UserManager.props(intentManager, DAO), name = "userManager")

  val endpoint = new InetSocketAddress("10.0.1.8", 9900) // conf it

  val server = system.actorOf(TCPServer.props(endpoint, usermeneger), name = "tcpServer")
  usermeneger ! SystemTouserManagerPutServer(server)
  DAO ! SystemToBdManagerLoad(usermeneger)

  Console.readLine()
  system.shutdown()
//  System.exit(0)



}


object TCPServer {
  def props(endpoint: InetSocketAddress, usermg: ActorRef): Props =
  Props(new TCPServer(endpoint, usermg))
}



class TCPServer(endpoint: InetSocketAddress, usermg: ActorRef) extends Actor with ActorLogging{

//  var log:Logger =LogManager.getLogger("InfoLoger")

  import context.system


  var usermeneger = usermg

  override def preStart() {
    log.debug("TCPServer load success")
  }

  override def postStop(): Unit ={
    log.debug("TCPServer stopped")
  }

  override def postRestart(reason: Throwable): Unit = {
  log.error(reason, "TCPServer was restart")
    IO(Tcp) ! Tcp.Bind(self, endpoint)
  }


  override def  preRestart(reason: Throwable, msg: Option[Any]): Unit = {
    log.error(reason, "TCPServer will restart bczof - "+msg)
  }

  

  override def receive: Receive = {
    case Tcp.Connected(remote, _) => {
      log.debug("New TCP connection:" + remote)
      sender ! Tcp.Register(context.actorOf(TcpConnectionHandlerActor.props(remote, sender, usermeneger))) // sender - tcp connection
    }
    case InitializeLogger => sender() ! LoggerInitialized

    case UserMangerToServerStart() => {
      IO(Tcp) ! Tcp.Bind(self, endpoint)
      log.debug("All load successfully ♥")
    }
  }

}
