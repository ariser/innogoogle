package wikicrawler

import java.util.concurrent.Callable

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element

class ArticleCrawler(browser: JsoupBrowser, url: String, reference: String = null) extends Callable[ArticleCrawlerResult] {
  private val ignoredTags: Set[String] = Set("script", "table")
  private val ignoredClassNames: Set[String] = Set("hatnote", "navbox")
  private val boundaryHeadlines: Set[String] = Set("see also", "references", "external links")
  private var boundaryHeadlineHit: Boolean = false

  def call(): ArticleCrawlerResult = {
    val document = browser.get(url)
    val title = document >> text("#firstHeading")
    val article: Option[Element] = document >?> element("#mw-content-text")

    if (article.isEmpty) return null

    val articleNodes = article.get.children.filter(filterArticleContent)
    val articleContent = articleNodes.map(_.text).mkString(" ").replaceAllLiterally("[edit]", "")

    val articleLinks: List[Element] = article.get >> elementList("a")
    val valuableLinks = articleLinks.filter(filterLinks)

    val h2Nodes = articleNodes.filter(filterH2)
    val h2Text = h2Nodes.map(_ >> text(".mw-headline")).toList

    val h3Nodes = articleNodes.filter(filterH3)
    val h3Text = h3Nodes.map(_ >> text(".mw-headline")).toList

    print("Thread: " + Thread.currentThread.getName + "; ")
    println("Article title: " + title)

    val result = new ArticleCrawlerResult()
    result.title = title
    result.content = articleContent
    result.url = url
    result.refs = if (reference != null) Some(List(reference)) else None
    result.links = getLinksResult(valuableLinks)
    result.h2 = if (h2Text.nonEmpty) Some(h2Text) else None
    result.h3 = if (h3Text.nonEmpty) Some(h3Text) else None

    result
  }

  private def filterArticleContent(node: Element): Boolean = {
    if (node == null) {
      return false
    }
    if (boundaryHeadlineHit) {
      return false
    }
    if (ignoredTags.contains(node.tagName.toLowerCase)) {
      return false
    }
    if (node.hasAttr("class") && ignoredClassNames.contains(node.attr("class"))) {
      return false
    }
    val headlineText: Option[String] = node >?> text(".mw-headline")
    if (headlineText.isDefined && boundaryHeadlines.contains(headlineText.get.toLowerCase)) {
      boundaryHeadlineHit = true
      return false
    }
    true
  }

  private def filterH2(node: Element): Boolean = {
    if (node == null) {
      return false
    }
    node.tagName.equalsIgnoreCase("h2")
  }

  private def filterH3(node: Element): Boolean = {
    if (node == null) {
      return false
    }
    node.tagName.equalsIgnoreCase("h3")
  }

  private def filterLinks(node: Element): Boolean = {
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

  private def getLinksResult(links: List[Element]): List[(String, String)] = {
    links.map(node => (node.text, wiki_url_prefix + node.attr("href")))
  }
}

class ArticleCrawlerResult() {
  var title: String = _
  var content: String = _
  var url: String = _
  var refs: Option[List[String]] = _
  var h2: Option[List[String]] = _
  var h3: Option[List[String]] = _
  var links: List[(String, String)] = _
}