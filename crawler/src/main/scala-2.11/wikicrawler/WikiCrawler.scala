package wikicrawler

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class WikiCrawler() {
  private val browser = JsoupBrowser()
  private var collected: Set[String] = Set()

  def start(entryPoint: String): Unit = {
    if (entryPoint != null && entryPoint.nonEmpty) {
      start(List(entryPoint))
    }
  }

  def start(entryPoints: List[String]): Unit = {
    if (entryPoints.nonEmpty) {
      processLinks(entryPoints.map(url => (null, url)))
    }
  }

  private def processLinks(links: List[(String, String)]): Unit = {
    if (links.isEmpty) return

    val aggregated: Future[List[ArticleCrawlerResult]] = Future.sequence(links.map(link => Future {
      val label = link._1
      val url = link._2
      if (collected.contains(url)) {
        if (label != null) {
          new UpdateRefsAsync(url, label)
        }
        null
      } else {
        collected += url
        val result = new ArticleCrawler(browser, url, label).call()
        if (result != null) {
          new AddArticleAsync(result).run()
        }
        result
      }
    }))

    val result: List[ArticleCrawlerResult] = Await.result(aggregated, Duration.Inf)
    val newLinks = result.flatMap(result => if (result != null) result.links else List())

    println("New links acquired: " + newLinks.size)

    processLinks(newLinks)
  }
}
