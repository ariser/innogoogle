package innogoogle

import PorterStemmer.stem

case class Tokenizer(separator: String = "[^a-z]+") {
	val source = io.Source.fromFile("stopwords.txt")
	val stopwords = source.getLines.toSet
	def apply(s: String) = s.toLowerCase.split(separator).filter( !stopwords.contains(_)).map(stem)
	source.close()
}
