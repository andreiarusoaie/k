package org.kframework.kore

import org.kframework.attributes._

trait K {
  def att: Att
}

trait KItem extends K

trait KLabel {
  def name: String
  override def equals(other: Any) = other match {
    case l: KLabel => name == l.name
    case _ => false
  }
}

trait KToken extends KItem {
  def sort: Sort
  def s: String
  override def equals(other: Any) = other match {
    case other: KToken => sort == other.sort && s == other.s
    case _ => false
  }
}

trait Sort {
  def name: String
  override def equals(other: Any) = other match {
    case other: Sort => name == other.name
    case _ => false
  }
}

trait KCollection {
  def items: java.util.List[K]
  def stream: java.util.stream.Stream[K] = items.stream()
}

trait KList extends KCollection {
  def size: Int = items.size
}

trait KApply extends KItem {
  def klabel: KLabel
  def klist: KList
}

trait KSequence extends KCollection with K

trait KVariable extends KItem with KLabel {
  def name: String
}

trait KRewrite extends K {
  def left: K
  def right: K
}

trait InjectedKLabel extends KItem {
  def klabel: KLabel
}
