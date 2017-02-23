//package maprohu.heroku.frontend
//
//import monix.execution.Cancelable
//
//trait Sink[T] {
//  def put(item: T) : Unit
//}
//
//object Sink {
//  def apply[T](fn: T => Unit): Sink[T] = {
//    new Sink[T] {
//      override def put(item: T): Unit = fn(item)
//    }
//  }
//}
//
//trait Source[T] {
//  def foreach(fn: T => Unit) : Cancelable
//
//  def map[Out](fn: T => Out) : Source[Out]
//}
//
//object Topic {
//  def apply[T]() = new Topic[T]
//}
//
//class Topic[T] extends Sink[T] with Source[T] {
//  var subscriptions = Vector[Sink[T]]()
//
//  override def put(item: T): Unit = {
//    subscriptions.foreach(_.put(item))
//  }
//
//  override def foreach(fn: (T) => Unit): Cancelable = {
//    val sink = Sink(fn)
//    subscriptions :+= sink
//    Cancelable(() => subscriptions = subscriptions.diff(Seq(sink)))
//  }
//
//  override def map[Out](fn: (T) => Out): Source[Out] = {
//    new MappedTopic[T, Out](this, fn)
//  }
//}
//
//class MappedTopic[In, Out](topic: Topic[In], mapper: In => Out) extends Sink[In] with Source[Out] {
//  override def put(item: In): Unit = topic.put(item)
//
//  override def foreach(fn: (Out) => Unit): Cancelable = {
//    topic.foreach(mapper andThen fn)
//  }
//
//  override def map[O](fn: (Out) => O): Source[O] = {
//    new MappedTopic[In, O](topic, mapper andThen fn)
//  }
//}
//
//

