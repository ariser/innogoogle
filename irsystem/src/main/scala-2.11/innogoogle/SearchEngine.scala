package innogoogle
import java.io._
import doobie.imports._, scalaz.effect.IO
import scalaz._, Scalaz._
import scalaz.concurrent.Task


object SearchEngine extends App {

	val xa = DriverManagerTransactor[Task](
		"org.postgresql.Driver", "jdbc:postgresql:postgres", "postgres", "admin"
	)

	case class Article (id: Int, url: String, title: String, content: String)

	def select: List[Article] =
	{
		sql"select id, url, title, content from wikipedia"
				.query[Article] // Query0[Article]
				.list // ConnectionIO[List[Article]]
				.transact(xa) // Task[List[Article]]
				.unsafePerformSync // List[Article]
				.take(1000)
	}

	def indexFromPostgres: Index = {
		val emptyIndex = new Index()
		val index = select.foldLeft(emptyIndex) { (nextIndex, doc) => nextIndex.index(doc.id, doc.url, doc.title, doc.content) }
		index
	}

	while(true) {

		println("Innogoogle:")

		val scoring = new Scoring(indexFromPostgres).fastCosineScore(scala.io.StdIn.readLine()).foreach(println)
	}
}


