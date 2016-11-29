package innogoogle
import java.io._
import doobie.imports._, scalaz.effect.IO
import doobie.imports._
import scalaz._, Scalaz._
import scalaz.concurrent.Task


object SearchEngine extends App {

	val xa = DriverManagerTransactor[Task](
		"org.postgresql.Driver", "jdbc:postgresql:irdata", "postgres", "admin"
	)

	case class Article (id: Int, url: String, title: String, content: String)

	def select: List[Article] =
	{
		sql"select id, url, title, article_content from article"
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

/*	serialization
	val index: Index = indexFromPostgres
	val oos = new ObjectOutputStream(new FileOutputStream("index.tmp"))
	oos.writeObject(index)
	oos.close

	val ois = new ObjectInputStream(new FileInputStream("index.tmp"))
	val stock = ois.readObject.asInstanceOf[Index]
	ois.close*/

	println("Innogoogle:")

	val scoring = new Scoring(indexFromPostgres).fastCosineScore(scala.io.StdIn.readLine()).foreach(println)

}


