package adventuregameengine.sWorld

import scala.collection.immutable.HashMap

import adventuregameengine.triggers.Trigger
import akka.actor.actorRef2Scala

class Player(label: String,
  sizeX: Int, sizeY: Int, sizeZ: Int, trig: Trigger, onCombine: HashMap[String, Trigger],
  actions: Set[String]) extends RoomActor(label, sizeX, sizeY, sizeZ, trig, onCombine,actions) {

  var inventory:Set[RoomObject] = Set()
  
  def receive = {
    case AddItem(item) => inventory = inventory + item
    case RemoveItem(item) => inventory = for(i <- inventory ; if(i.label != item)) yield i
    case SeeInventory => sender ! Inventory(for(i <- inventory) yield i.label)
  }

}