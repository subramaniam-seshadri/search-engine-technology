package cecs429.cluster;

import java.util.HashMap;

public class ClusterDoc {

	/**
	 * This class is contains all the values needed to calculate similarity between
	 * documents to determine clusters
	 */

	/**
	 * termWdtMap - represents the vector formed from a document
	 */
	private HashMap<String, Double> termWdtMap;

	/**
	 * docId - docId of the document used for comparison
	 */

	private int docId;

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	/**
	 * Ld - Weight of the document used for normalization
	 * docLength - Number of tokens in the document
	 * docSize - Size of the document in number of bytes
	 * avgTfd - Average term frequency of the terms in the document
	 */
	private double Ld;
	private long docLength;
	private double avgTfd;
	private long docSize;

	public long getDocSize() {
		return docSize;
	}

	public void setDocSize(long docSize) {
		this.docSize = docSize;
	}

	public double getAvgTfd() {
		return avgTfd;
	}

	public void setAvgTfd(double avgTfd) {
		this.avgTfd = avgTfd;
	}

	public long getDocLength() {
		return docLength;
	}

	public void setDocLength(long docLength) {
		this.docLength = docLength;
	}

	public HashMap<String, Double> getTermWdtMap() {
		return termWdtMap;
	}

	public void setTermWdtMap(HashMap<String, Double> termWdtMap) {
		this.termWdtMap = termWdtMap;
	}

	public double getLd() {
		return Ld;
	}
	
	public void setLd(double ld) {
		Ld = ld;
	}
}
