import akka.actor.Actor
import akka.event.Logging

class Store extends Actor {

  val log = Logging(context.system, this)
  var cache: Array[String] = Array.empty

  def receive: Receive = {
    case FetchedData(data) =>
      cache = data
      log.debug("Cached data")
  }

}
