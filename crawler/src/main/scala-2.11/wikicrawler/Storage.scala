package wikicrawler

import doobie.imports._
import doobie.contrib.postgresql.pgtypes._
import scalaz._, Scalaz._
import scalaz.concurrent.Task

case class Article(
                    id: Int,
                    title: String,
                    content: String,
                    url: String,
                    refs: Option[List[String]],
                    h2: Option[List[String]],
                    h3: Option[List[String]]
                  )

object Storage {
  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:postgres", "postgres", "admin"
  )

  import xa.yolo._

  def selectAllWikipedia(): List[Article] = {
    sql"select * from wikipedia"
      .query[Article]
      .list
      .transact(xa)
      .run
  }

  def addArticle(title: String, content: String, url: String, refs: Option[List[String]], h2: Option[List[String]], h3: Option[List[String]]): Unit = {
    sql"insert into wikipedia (title, content, url, refs, h2, h3) values ($title, $content, $url, $refs, $h2, $h3)"
      .update
      .quick
      .run
  }

  def addReference(url: String, reference: String): Unit = {
    val refsResult: Option[List[String]] = sql"select refs from wikipedia where url=$url"
      .query[Option[List[String]]]
      .unique
      .transact(xa)
      .run

    val refs = reference :: refsResult.getOrElse(List())

    sql"update wikipedia set refs=$refs where url=$url"
      .update
      .quick
      .run
  }

  def contains(url: String): Boolean = {
    val count = sql"select count(id) from wikipedia where url=$url"
      .query[Int]
      .unique
      .quick
      .run
    count > 0
  }
}

class AddArticleAsync(article: ArticleCrawlerResult) extends Runnable {
  def run() {
    if (article != null) {
      Storage.addArticle(article.title, article.content, article.url, article.refs, article.h2, article.h3)
    }
  }
}

class UpdateRefsAsync(url: String, reference: String) extends Runnable {
  def run() {
    Storage.addReference(url, reference)
  }
}