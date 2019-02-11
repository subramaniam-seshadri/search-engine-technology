package cecs429.rankedretrieval;

public class RankedResults {

	/**
	 * This class is used to collect the ranked results.
	 */
	private int documentID;
	private double retrievalScore;
	private int accumulatorCount;
	public int getAccumulatorCount() {
		return accumulatorCount;
	}
	public void setAccumulatorCount(int accumulatorCount) {
		this.accumulatorCount = accumulatorCount;
	}
	public int getDocumentID() {
		return documentID;
	}
	public void setDocumentID(int documentID) {
		this.documentID = documentID;
	}
	public double getRetrievalScore() {
		return retrievalScore;
	}
	public void setRetrievalScore(double retrievalScore) {
		this.retrievalScore = retrievalScore;
	}
}
