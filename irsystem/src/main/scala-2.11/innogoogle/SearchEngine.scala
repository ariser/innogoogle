package innogoogle
import java.io._

object SearchEngine extends App {
	def indexFromFile(filePath: String): Index = {
		val emptyIndex = new Index()
		val source = io.Source.fromFile(filePath)
		val data: Vector[String] = source.mkString.split("#").toVector
		val index = data.foldLeft(emptyIndex) { (nextIndex, doc) => nextIndex.index(doc) }
		source.close()
		index
	}

	val index  = indexFromFile("docs.txt")

/*	serialization

	val oos = new ObjectOutputStream(new FileOutputStream("index.tmp"))
	oos.writeObject(index)
	oos.close

	val ois = new ObjectInputStream(new FileInputStream("index.tmp"))
	val stock = ois.readObject.asInstanceOf[Index]
	ois.close*/

	println("Innogoogle:")

	val scoring = new Scoring(index).fastCosineScore(scala.io.StdIn.readLine()).foreach(println)


}


