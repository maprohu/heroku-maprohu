package maprohu.heroku.backend

import monix.reactive.{Consumer, MulticastStrategy}
import monix.reactive.subjects.ConcurrentSubject
import monix.execution.Scheduler.Implicits.global

import scala.io.StdIn

/**
  * Created by pappmar on 24/02/2017.
  */
object RunMonix01 {

  def main(args: Array[String]): Unit = {

    val subject = ConcurrentSubject[String](MulticastStrategy.publish)

    subject
      .consumeWith(
        Consumer.foreach(println)
      )
      .runAsync

    (1 to 10).foreach({ i =>
      new Thread() {
        override def run(): Unit = {
          subject.onNext(i.toString)
        }
      }.start()
    })

    subject.onNext("boo")

    StdIn.readLine()


  }

}
