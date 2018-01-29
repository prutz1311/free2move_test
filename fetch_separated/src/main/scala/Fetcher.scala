import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.ByteString

case object FetchItems
case class FetchedData(data: Array[String])

class Fetcher extends Actor {

  val log = Logging(context.system, this)

  val uri: String = "http://challenge.carjump.net/A"
  val store = context.actorOf(Props[Store], "store")
  val http = Http(context.system)

  import context.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()(context.system)

  def receive: Receive = {
    case FetchItems => 
      log.debug("Fetching items")
      http.singleRequest(HttpRequest(uri = uri)).pipeTo(self)
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        val data = body.utf8String.split("\n")
        store ! FetchedData(data)
        log.debug("Received OK response, first items: {}", data.take(5).toList.toString)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

}
