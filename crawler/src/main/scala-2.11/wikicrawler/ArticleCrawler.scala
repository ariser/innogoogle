package wikicrawler

import java.util.concurrent.Callable

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element

class ArticleCrawler(browser: JsoupBrowser, url: String) extends Callable[Set[String]] {
  private val ignored_tags: Set[String] = Set("script", "table")
  private val ignored_class_names: Set[String] = Set("hatnote", "navbox")
  private val boundary_headlines: Set[String] = Set("see also", "references", "external links")
  private var boundary_headline_hit: Boolean = false

  def call(): Set[String] = {
    val document = browser.get(url)
    val title = document >> text("#firstHeading")
    val article: Option[Element] = document >?> element("#mw-content-text")

    if (article.isEmpty) return Set()

    val articleNodes = article.get.children.filter(filter_article_content)

    val articleLinks: List[Element] = article.get >> elementList("a")
    val valuableLinks = articleLinks.filter(filter_links)

    print("Thread: " + Thread.currentThread.getName + "; ")
    println("Article title: " + title)

    get_absolute_urls(valuableLinks) toSet
  }

  private def filter_article_content(node: Element): Boolean = {
    if (node == null) {
      return false
    }
    if (boundary_headline_hit) {
      return false
    }
    if (ignored_tags.contains(node.tagName.toLowerCase)) {
      return false
    }
    if (node.hasAttr("class") && ignored_class_names.contains(node.attr("class"))) {
      return false
    }
    val headline_text: Option[String] = node >?> text(".mw-headline")
    if (headline_text.isDefined && boundary_headlines.contains(headline_text.get.toLowerCase)) {
      boundary_headline_hit = true
      return false
    }
    true
  }

  private def filter_links(node: Element): Boolean = {
    if (node == null) {
      return false
    }
    if (!node.tagName.equalsIgnoreCase("a")) {
      return false
    }
    if (!node.hasAttr("href")) {
      return false
    }
    val href = node.attr("href")
    if (href.contains(":")) {
      // omit service and non-informative pages, e.g. /wiki/Category:Some_category
      return false
    }
    if (!href.startsWith("/wiki/")) {
      // process only internal links
      return false
    }
    true
  }

  private val wiki_url_prefix: String = "https://en.wikipedia.org"

  private def get_absolute_urls(links: List[Element]): List[String] = {
    links.map(wiki_url_prefix + _.attr("href"))
  }
}
