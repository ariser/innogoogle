package wikicrawler

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.{Document, Element}

object WikiCrawler {
  private val browser = JsoupBrowser()

  private val ignored_tags: Set[String] = Set("script", "table")
  private val ignored_class_names: Set[String] = Set("hatnote", "navbox")
  private val boundary_headlines: Set[String] = Set("see also", "references", "external links")
  private var boundary_headline_hit: Boolean = false

  def start(entry_point: String): Unit = {
    process_wiki_page(entry_point)
  }

  private def get(url: String): Document = browser.get(url)

  private def process_wiki_page(url: String): Unit = {
    val document = get(url)
    val title = document >> text("#firstHeading")
    val article: Option[Element] = document >?> element("#mw-content-text")

    if (article.isEmpty) return

    boundary_headline_hit = false
    val articleNodes = article.get.children.filter(filter_article_content)

    val articleLinks: List[Element] = article.get >> elementList("a")
    val valuableLinks = articleLinks.filter(filter_links)

    println(articleNodes.map(_.text).mkString(" ").replaceAllLiterally("[edit]", ""))
    println(valuableLinks.mkString("\n"))
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
      return false
    }
    if (!href.startsWith("/wiki/")) {
      return false
    }
    true
  }
}
