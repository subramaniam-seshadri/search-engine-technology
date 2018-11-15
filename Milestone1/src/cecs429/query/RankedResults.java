package cecs429.query;

public class RankedResults {

	private int documentID;
	private double retrievalScore;
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
