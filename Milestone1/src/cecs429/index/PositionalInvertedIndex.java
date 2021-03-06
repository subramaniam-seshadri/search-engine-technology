package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Implements an Index using a positional inverted index. Unlike
 * TermDocumentIndex, doesn't require knowing the full corpus vocabulary and
 * number of documents prior to construction.
 */
public class PositionalInvertedIndex implements Index {
	HashMap<String, List<Posting>> vocabulary = new HashMap<String, List<Posting>>();

	@Override
	public List<Posting> getPositionalPostings(String term) {
		List<Posting> results = new ArrayList<>();
		results = vocabulary.get(term);
		return results;
	}

	/**
	 * Sorts the vocabulary alphabetically and returns the sorted vocabulary.
	 */
	@Override
	public List<String> getVocabulary() {
		List<String> mVocabulary = new ArrayList<>(vocabulary.keySet());
		Collections.sort(mVocabulary);
		return Collections.unmodifiableList(mVocabulary);
	}

	/**
	 * Adds the term to the index by recording the terms' position and document id.
	 * 
	 * @param term       Term to be indexed
	 * @param documentId DocumentId from where the term has been fetched
	 * @param position   Position of the term in the document
	 */
	public void addTerm(String term, int documentId, int position) {
		if (vocabulary.containsKey(term)) { // if the vocabulary contains the term then
			List<Posting> postingList = vocabulary.get(term); // get the related posting
			if (documentId > postingList.get(postingList.size() - 1).getDocumentId()) { // new document
				List<Integer> positions = new ArrayList<Integer>();
				positions.add(position);
				Posting p = new Posting(documentId, positions);
				postingList.add(p);
				vocabulary.put(term, postingList);
			} else if (documentId == postingList.get(postingList.size() - 1).getDocumentId()) {
				// To-Do add to list if otherwise
				List<Integer> positions = postingList.get(postingList.size() - 1).getPositionsInDoc();
				positions.add(position);
				vocabulary.put(term, postingList);
			}

		} else { // vocabulary doesn't contain the term, add the new term to vocabulary
			List<Posting> postingList = new ArrayList<Posting>();
			List<Integer> positions = new ArrayList<Integer>();
			positions.add(position);
			Posting p = new Posting(documentId, positions);
			postingList.add(p);
			vocabulary.put(term, postingList);
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		return null;
	}
}