import wikicrawler.{Storage, WikiCrawler}

object crawler_run extends App {
  val entry_point = "https://en.wikipedia.org/wiki/Information_retrieval"
  new WikiCrawler().start(entry_point)
}
