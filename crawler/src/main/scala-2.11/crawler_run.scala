import wikicrawler.WikiCrawler

object crawler_run extends App {
  val entry_point = "https://en.wikipedia.org/wiki/Information_retrieval"
  WikiCrawler.start("https://en.wikipedia.org/wiki/463")
}
