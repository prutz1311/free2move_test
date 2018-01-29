import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.ByteString

case object FetchItems

class Fetcher extends Actor {

  val log = Logging(context.system, this)

  var cache: Array[String] = Array.empty
  val uri: String = "http://challenge.carjump.net/A"

  val http = Http(context.system)

  import context.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()(context.system)

  def receive: Receive = {
    case FetchItems => 
      log.debug("Fetching items")
      http.singleRequest(HttpRequest(uri = uri)).pipeTo(self)
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        cache = body.utf8String.split("\n")
        log.debug("Received OK response, first items: {}", cache.take(5).toList.toString)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

}
