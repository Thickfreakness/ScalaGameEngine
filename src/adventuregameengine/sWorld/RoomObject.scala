package adventuregameengine.sWorld

import scala.collection.immutable.HashMap
import adventuregameengine.triggers.Trigger
import akka.actor.Actor
import akka.actor.actorRef2Scala
import scala.concurrent.Awaitable

class RoomObject(val label: String,
    val sizeX: Int, val sizeY: Int, val sizeZ: Int, trig: Trigger, onCombine: HashMap[String, Trigger]) extends Actor with Awaitable[Unit]{
	var x = 0
	var y = 0
	var z = 0
	var state = 0
   
  def receive = {
    case GoHere(xs,ys,zs) => {
      x = xs
      y = ys
      z = zs
    }
    case WhereAreYou => sender ! IAmHere(x,y,z,label)
    case Action => trig.triggerEvent()
    case Combine(label) => for(s <- onCombine.keys ; if(s == label) ) onCombine(s).triggerEvent()
    case State(s) => state = s
    case GetState => sender ! State(state)
  }
}