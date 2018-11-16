package cecs429.index;

import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {
	/**
	 * Retrieves a list of Postings of documents that contain the given term along with the respective positions of the term in each of the documents.
	 */
	List<Posting> getPositionalPostings(String term);
	
	/**
	 * A (sorted) list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();
	
	/**
	 * Retrieves a list of postings of documents that contain the term.
	 * 
	 */
	List<Posting> getPostings(String term);
}
