import net.ruippeixotog.scalascraper.browser.JsoupBrowser

object Crawler extends App {
  val browser = JsoupBrowser()
  var doc = browser.get("http://ariser.ru").
  println("Hello world")
}
