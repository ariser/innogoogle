package innogoogle

class Scoring(index: Index) {

	def idf(term: String) =
		math.log(index.size / index.docFrequency(term))

	def wf(tf: Int) =
		if (tf > 0) 1 + math.log(tf) else 0D

	def docNorm(docId: Int) = {
		val docTerms = index.tokenizer(index.doc(docId))
		math.sqrt( docTerms.map( term => math.pow(idf(term), 2)).sum )
	}

	def fastCosineScore(q: String, topK: Int = 100) = {

		val scores = new collection.mutable.HashMap[Int, Double].withDefaultValue(0D)

		for {
			term <- index.tokenizer(q)
			posting <- index.postings(term)
		} scores.put(posting.docId, scores(posting.docId) + wf(posting.tf) * idf(term))

		scores.map(scoreResult).toSeq.sortWith(_.score > _.score).take(topK)
	}

	case class Result(docId: Int, score: Double, title: String, url: String)

	private def scoreResult(docIdAndScore: (Int, Double)): Result = {
		val (docId, score) = docIdAndScore
		Result(docId, score / docNorm(docId), index.title(docId), index.url(docId))
	}
}

