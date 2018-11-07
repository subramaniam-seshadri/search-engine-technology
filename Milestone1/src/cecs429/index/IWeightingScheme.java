package cecs429.index;

public interface IWeightingScheme {

	/**
	 * 
	 * @param N - number of documents in the corpus
	 * @param dft - number of documents that contain the term t
	 * @return returns wqt
	 */
	double getWqt(Integer N, Integer dft);

	double getWd();

	double getLd();
}
