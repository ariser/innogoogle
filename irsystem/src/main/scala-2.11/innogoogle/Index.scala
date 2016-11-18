package innogoogle

case class Posting(docId: Int, tf: Int)

class Index(val tokenizer: Tokenizer = Tokenizer(),
			private val invertedIndex: Map[String, List[Posting]] = Map.empty,
			private val data: IndexedSeq[String] = Vector.empty) extends Serializable {

	def index(doc: String): Index = {
		val dictionary = tokenizer(doc).groupBy(identity).mapValues(_.length)
		var newInverted = invertedIndex
		for((term, tf) <- dictionary) {
			val newPostingList = Posting(data.size, tf) :: invertedIndex.getOrElse(term, Nil)

			newInverted += (term -> newPostingList)

		}
		new Index(tokenizer, newInverted, data :+ doc)
	}

	def size = invertedIndex.size

	def doc(id: Int) = data(id)

	def postings(term: String): List[Posting] =
		invertedIndex.getOrElse(term, Nil)

	def docFrequency(term: String) = postings(term).size
}
