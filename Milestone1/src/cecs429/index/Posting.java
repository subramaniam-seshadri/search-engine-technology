package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositionIds = new ArrayList<Integer>();

	public Posting(int documentId, List<Integer> positions) {
		mDocumentId = documentId;
		mPositionIds = positions;
	}

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getPositionsInDoc() {
		return mPositionIds;
	}
}
