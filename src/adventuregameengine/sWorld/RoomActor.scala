package adventuregameengine.sWorld

import scala.collection.immutable.HashMap

import adventuregameengine.triggers.Trigger
import akka.actor.ActorContext

class RoomActor(label: String,
  sizeX: Int, sizeY: Int, sizeZ: Int, trig: Trigger, onCombine: HashMap[String, Trigger],
  actions: Set[String]) extends RoomObject(
  label, sizeX, sizeY, sizeZ, trig, onCombine) {
  var route:List[(Int,Int,Int)] = List()
  var currentAction = ""

  def receive = {
    case SetAction(a) => currentAction = a
    case Step => route match{
      case x :: xs => self ! GoHere(x._1,x._2,x._3) ; route = xs
      case List() => Unit
    }
    case AssignRoute(r) => route = r
    		
  }
}