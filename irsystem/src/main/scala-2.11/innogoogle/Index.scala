package innogoogle

case class Posting(docId: Int, tf: Int)

class Index(val tokenizer: Tokenizer = Tokenizer(),
			private val invertedIndex: Map[String, List[Posting]] = Map.empty,
			private val data: Map[Int, String] = Map.empty,
			private val urlMap: Map[Int, (String, String)] = Map.empty) extends Serializable {



	def index(id: Int, url: String, title: String, doc: String): Index = {
		val dictionary = tokenizer(doc).groupBy(identity).mapValues(_.length)
		var newInverted = invertedIndex
		for((term, tf) <- dictionary) {
			val newPostingList = Posting(id, tf) :: invertedIndex.getOrElse(term, Nil)

			newInverted += (term -> newPostingList)

		}
		var newUrlMap = urlMap
		newUrlMap += (id -> (title, url))


		var newData = data
		newData += (id -> doc)

		new Index(tokenizer, newInverted, newData, newUrlMap)

	}

	def size = invertedIndex.size

	def doc(id: Int) = data(id)

	def title(id: Int) = urlMap(id)._1.toString

	def url(id: Int) = urlMap(id)._2.toString

	def postings(term: String): List[Posting] =
		invertedIndex.getOrElse(term, Nil)

	def docFrequency(term: String) = postings(term).size
}
