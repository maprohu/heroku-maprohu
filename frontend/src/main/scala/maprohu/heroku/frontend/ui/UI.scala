package maprohu.heroku.frontend.ui

import org.scalajs.dom
import org.scalajs.dom.raw.Node
import rx.Var

import scalatags.JsDom

/**
  * Created by maprohu on 24-02-2017.
  */
object UI {

  implicit class NodeOps(element: Node) {
    def replaceContent(c: Node) = {
      while (element.childNodes.length > 0) {
        element.removeChild(element.childNodes(0))
      }
      element.appendChild(
        c
      )
    }
  }

  def setup(root: Root) = {
    import rx.Ctx.Owner.Unsafe._

    import JsDom.all._
    val statusLabel = div.render
    val mainPanel = div.render

    dom.document.body.appendChild(
      div(
        statusLabel,
        mainPanel
      ).render
    )

    root
      .connected
      .foreach({ c =>
        statusLabel.replaceContent(
          span(if (c) "connected" else "disconnected").render
        )
      })

    root
      .main
      .foreach({ n =>
        mainPanel.replaceContent(n)
      })

  }

}

class Root {
  val connected = Var(false)
  val main = Var[Node](JsDom.all.div.render)
}

class Connected {


}

