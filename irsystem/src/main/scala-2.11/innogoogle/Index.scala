package innogoogle

case class Posting(docId: Int, tf: Int)

class Index(val tokenizer: Tokenizer = Tokenizer(),
			private val invertedIndex: Map[String, List[Posting]] = Map.empty,
			private val data: IndexedSeq[String] = Vector.empty,
			val urlMap: Map[Int, String] = Map.empty) extends Serializable {



	def index(id: Int, url: String, doc: String): Index = {
		val dictionary = tokenizer(doc).groupBy(identity).mapValues(_.length)
		var newInverted = invertedIndex
		for((term, tf) <- dictionary) {
			val newPostingList = Posting(id, tf) :: invertedIndex.getOrElse(term, Nil)

			newInverted += (term -> newPostingList)

		}
		var newUrlMap = urlMap
		newUrlMap += (id -> url)

		new Index(tokenizer, newInverted, data :+ doc, newUrlMap)

	}

	def size = invertedIndex.size

	def doc(id: Int) = data(id-1)//starts from 0 in vector

	def url(id: Int) = urlMap(id).toString

	def postings(term: String): List[Posting] =
		invertedIndex.getOrElse(term, Nil)

	def docFrequency(term: String) = postings(term).size
}
