package adventuregameengine.sWorld

import akka.actor.ActorRef

case class GoHere(x: Int, y: Int, z: Int)
case class WhereAreYou
case class GetSize
case class Size(size: (Int,Int,Int))
case class IAmHere(x: Int, y: Int, z: Int, label: String)
case class Action
case class Combine(label: String)
case class GetState
case class State(state: Int)
case class SetAction(act: String)
case class Step
case class AssignRoute(route: List[(Int,Int,Int)])
case class AddItem(item: RoomObject)
case class RemoveItem(item: String)
case class SeeInventory
case class Inventory(items: Set[String])
case class MoveObject(label: String, dest: (Int,Int,Int))
case class AddObject(label: String, ref: ActorRef)