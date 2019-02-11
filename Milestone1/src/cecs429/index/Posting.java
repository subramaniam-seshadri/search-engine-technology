package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositionIds = new ArrayList<Integer>();
	private int mTermFrequency;
	private double wdtDefaultValue;
	private double wdtTfIdfValue;
	private double wdtOkapiValue;
	private double wdtWackyValue;	

	public double getWdtDefaultValue() {
		return wdtDefaultValue;
	}

	public double getWdtTfIdfValue() {
		return wdtTfIdfValue;
	}

	public double getWdtOkapiValue() {
		return wdtOkapiValue;
	}

	public double getWdtWackyValue() {
		return wdtWackyValue;
	}

	public void setWdtDefaultValue(double wdtDefaultValue) {
		this.wdtDefaultValue = wdtDefaultValue;
	}

	public void setWdtTfIdfValue(double wdtTfIdfValue) {
		this.wdtTfIdfValue = wdtTfIdfValue;
	}

	public void setWdtOkapiValue(double wdtOkapiValue) {
		this.wdtOkapiValue = wdtOkapiValue;
	}

	public void setWdtWackyValue(double wdtWackyValue) {
		this.wdtWackyValue = wdtWackyValue;
	}

	public int getTermFrequency() {
		return mTermFrequency;
	}

	public Posting(int documentId, List<Integer> positions) {
		mDocumentId = documentId;
		mPositionIds = positions;
	}

	public Posting(int documentId) {
		mDocumentId = documentId;
	}
	
	public Posting(int documentId, int termFrequency) {
		this.mDocumentId = documentId;
		this.mTermFrequency = termFrequency;
	}

	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getPositionsInDoc() {
		return mPositionIds;
	}
}
