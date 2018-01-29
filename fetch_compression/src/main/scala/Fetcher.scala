import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.annotation.tailrec

trait Compressor {
  def compress[A]: Seq[A] => Seq[Compressed[A]]
  def decompress[A]: Seq[Compressed[A]] => Seq[A]
}

sealed trait Compressed[+A]
case class Single[A](element: A) extends Compressed[A]
case class Repeat[A](count: Int, element: A) extends Compressed[A]

case object FetchItems

class Fetcher extends Actor {

  val log = Logging(context.system, this)

  var cache: Seq[Compressed[String]] = Seq.empty
  val uri: String = "http://challenge.carjump.net/A"

  val http = Http(context.system)

  val compressor = new Compressor {
    def compress[A] = { (raw: Seq[A]) =>
      @tailrec
      def recurse(
        acc: List[(A, Int)],
        remaining: List[A],
        lastElement: Option[A]
      ): List[(A, Int)] = remaining match {
        case Nil => acc
        case x :: xs if Some(x) == lastElement && acc.nonEmpty =>
          recurse(acc.head.copy(_2 = acc.head._2 + 1) :: acc.tail, remaining.tail, Some(x))
        case x :: xs =>
          recurse((x, 1) :: acc, remaining.tail, Some(x))
      }
      recurse(Nil, raw.toList, None).reverse.map {
        case (el, 1) => Single(el)
        case (el, count) => Repeat(count, el)
      }
    }
    def decompress[A] = { (compressed: Seq[Compressed[A]]) =>
      compressed.flatMap {
        case Single(element) => Seq(element)
        case Repeat(count, element) => Seq.fill(count)(element)
      }
    }
  }

  import context.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()(context.system)

  def receive: Receive = {
    case FetchItems => 
      log.debug("Fetching items")
      http.singleRequest(HttpRequest(uri = uri)).pipeTo(self)
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        cache = compressor.compress(body.utf8String.split("\n"))
        log.debug("Received OK response, first items: {}", cache.take(5).toList.toString)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

}
