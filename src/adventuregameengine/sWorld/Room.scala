package adventuregameengine.sWorld

import scala.concurrent.duration.Duration
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import scala.actors.threadpool.TimeUnit
import akka.util.Timeout
import scala.concurrent.Await

class Room(val id: String) extends Actor {
  var objects: Map[String, ActorRef] = Map()
  val dimensions = (214, 120, 10)
  val system = ActorSystem("World System")

  def walkable(dim: (Int, Int, Int)): Boolean = {
    if (dim._1 < 0 || dim._2 < 0 || dim._3 < 0 || dim._1 > dimensions._1 || dim._2 > dimensions._2 || dim._3 > dimensions._3)
      false
    else
      clash(dim, objects.values.toList)
  }

  def clash(point: (Int, Int, Int), obs: List[ActorRef]): Boolean = {
    obs match {
      case x :: xs => {
        val f1 = ask(x, WhereAreYou)(Timeout(1))
        val f2 = ask(x, GetSize)(Timeout(1))

        val loc = Await.result(f1, Duration("1")).asInstanceOf[(Int, Int, Int)]
        val size = Await.result(f2, Duration("1")).asInstanceOf[(Int, Int, Int)]

        if ((point._1 < loc._1 || point._1 > loc._1 + size._1) && (point._2 < loc._2 || point._2 > loc._2 + size._2) &&
          (point._3 < loc._3 || point._3 > loc._3 + size._3)) clash(point, xs)
        else
          false
      }
      case List() => true
    }
  }

  def receive = {
    case AddObject(item, ref) => objects = objects + (item -> ref)
    case RemoveItem(item) => objects = objects - item
    case SeeInventory => sender ! Inventory(objects.keys.toSet)
    case MoveObject(lab, d) => objects(lab) ! GoHere(d._1, d._2, d._3)
  }
}