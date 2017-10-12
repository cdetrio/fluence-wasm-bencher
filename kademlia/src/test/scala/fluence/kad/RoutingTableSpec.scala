package fluence.kad

import java.nio.ByteBuffer

import cats.Id
import cats.kernel.Monoid
import cats.instances.try_._
import org.scalatest.{ Matchers, WordSpec }

import scala.language.implicitConversions
import scala.util.{ Failure, Success, Try }

class RoutingTableSpec extends WordSpec with Matchers {
  implicit def key(i: Long): Key = Key(Array.concat(Array.ofDim[Byte](Key.Length - java.lang.Long.BYTES), {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.putLong(i)
    buffer.array()
  }))

  implicit def toLong(k: Key): Long = {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.put(k.id.takeRight(java.lang.Long.BYTES))
    buffer.flip()
    buffer.getLong()
  }

  "kademlia routing table (non-iterative)" should {
    val failLocalRPC = (_: Long) ⇒ new KademliaRPC[Try, Long] {
      override def ping() = Failure(new NoSuchElementException)

      override def lookup(key: Key) = ???

      override def lookupIterative(key: Key) = ???
    }

    val successLocalRPC = (c: Long) ⇒ new KademliaRPC[Try, Long] {
      override def ping() = Success(Node(c, c))

      override def lookup(key: Key) = ???

      override def lookupIterative(key: Key) = ???
    }

    "not fail when requesting its own key" in {
      val rt0 = RoutingTable[Long](Monoid[Key].empty, 2, 2)

      RoutingTable.find[Id, Long](0l).run(rt0)._2 should be('empty)
      RoutingTable.lookup[Id, Long](0l).run(rt0)._2 should be('empty)
    }

    "find nodes correctly" in {

      val rt0 = RoutingTable[Long](Monoid[Key].empty, 2, 2)

      val rt6 = (1l to 5l).foldLeft(rt0) {
        case (rt, i) ⇒
          val Success((rtU, _)) = RoutingTable.update[Try, Long](Node(i, i), failLocalRPC).run(rt)

          (1l to i).foreach { n ⇒
            RoutingTable.find[Id, Long](n).run(rtU)._2 should be('defined)
          }

          rtU
      }

      val Success((rt7, _)) = RoutingTable.update[Try, Long](Node(6l, 6l), failLocalRPC).run(rt6)

      RoutingTable.find[Id, Long](4l).run(rt7)._2 should be('empty)

      val Success((rt8, _)) = RoutingTable.update[Try, Long](Node(6l, 6l), successLocalRPC).run(rt6)

      RoutingTable.find[Id, Long](4l).run(rt8)._2 should be('defined)

    }

    "lookup nodes correctly" in {
      val rt10 = (1l to 10l).foldLeft(RoutingTable[Long](Monoid[Key].empty, 2, 2)) {
        case (rtb, i) ⇒
          val Success((rtU, _)) = RoutingTable.update[Try, Long](Node(i, i), successLocalRPC).run(rtb)

          rtU
      }

      val (_, nbs10) = RoutingTable.lookup[Id, Long](100l).run(rt10)
      nbs10.size should be.>=(7)

      // Our implicit Int-to-Key conversion doesn't allow larger numbers
      val rt127 = (1l to 127l).foldLeft(RoutingTable[Long](Monoid[Key].empty, 10, 10)) {
        case (rtb, i) ⇒
          val Success((rtU, _)) = RoutingTable.update[Try, Long](Node(i, i), successLocalRPC).run(rtb)

          rtU
      }

      (1l to 127l).foreach { i ⇒
        val (_, nbs127) = RoutingTable.lookup[Id, Long](i).run(rt127)
        nbs127.size should be.>=(10)
      }
    }
  }
}
