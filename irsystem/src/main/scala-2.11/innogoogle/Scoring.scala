package innogoogle

class Scoring(index: Index) {

	def idf(term: String) =
		math.log(index.size / index.docFrequency(term))

	def wf(tf: Int) =
		if (tf > 0) 1 + math.log(tf) else 0D

	def fastCosineScore(q: String, topK: Int = 100) = {

		val scores = new collection.mutable.HashMap[Int, Double].withDefaultValue(0D)

		for {
			term <- index.tokenizer(q)
			posting <- index.postings(term)
		} scores.put(posting.docId, scores(posting.docId) + wf(posting.tf) * idf(term))

		//TODO: a better approach is to use a heap to retrieve top K
		scores.map(scoreResult).toSeq.sortWith(_.score > _.score).take(topK)
	}

	case class Result(docId: Int, doc: String, score: Double)

	private def scoreResult(docIdAndScore: (Int, Double)): Result = {
		val (docId, score) = docIdAndScore
		Result(docId, index.doc(docId), score / index.doc(docId).length)// TODO: to choose a better normalization
	}
}

