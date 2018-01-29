import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.HttpApp
import akka.stream.ActorMaterializer

object HttpInterface extends App {

  implicit val system = ActorSystem("httpInterface")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  object WebServer extends HttpApp {
    val data: Array[String] = Array("A", "B", "C", "D")
    override def routes = path(IntNumber) { (index: Int) =>
      get {
        val itemO: Option[String] = data.lift(index)
        itemO match {
          case Some(item) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, item))
          case None => complete((StatusCodes.NotFound, "Index out of bounds"))
        }
      }
    }
  }

  WebServer.startServer("localhost", 8080)

}
