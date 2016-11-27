package wikicrawler

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class WikiCrawler() {
  private val browser = JsoupBrowser()
  private var collected: Set[String] = Set()

  def start(entry_point: String): Unit = {
    start(Set(entry_point))
  }

  def start(entry_points: Set[String]): Unit = {
    if (entry_points.isEmpty) return

    val aggregated: Future[Set[Set[String]]] = Future.sequence(entry_points.map(url => Future {
      if (collected.contains(url)) return
      collected += url
      new ArticleCrawler(browser, url).call()
    }))
    val new_urls: Set[String] = Await.result(aggregated, Duration.Inf).flatten
    println(new_urls)
    println(new_urls.size)
    start(new_urls)
  }
}
