import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.DurationInt


object Fetch extends App {

  val conf = ConfigFactory.load()
  val x: Int = conf.getInt("app.x")
  val system = ActorSystem("fetchSystem", conf)
  import system.dispatcher

  val fetcher = system.actorOf(Props[Fetcher], "fetcher")

  system.scheduler.schedule(0 seconds, x seconds, fetcher, FetchItems)

}
