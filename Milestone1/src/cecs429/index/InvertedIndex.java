/*package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InvertedIndex implements Index {
	HashMap<String, List<Integer>> vocabulary = new HashMap<String, List<Integer>>();
	
	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> results = new ArrayList<>();
		List<Integer> docIDs = vocabulary.get(term);
		for(int i=0;i<docIDs.size();i++) {
			Posting p = new Posting(docIDs.get(i));
			results.add(p);
		}
		return results;
	}

	@Override
	public List<String> getVocabulary() {
		List<String> mVocabulary = new ArrayList<>(vocabulary.keySet());
		//TO-DO sort the vocabulary and return the list of strings
		return Collections.unmodifiableList(mVocabulary);
	}

	public void addTerm(String term, int documentId) {
		if(vocabulary.containsKey(term)) {
			List<Integer> docIDs = vocabulary.get(term);
			if (documentId > docIDs.get(docIDs.size()-1)) {
				docIDs.add(documentId);
			}
		}
		else {
			List<Integer> docIDs = new ArrayList<Integer>();
			docIDs.add(documentId);
			vocabulary.put(term, docIDs);
		}
	}
}
*/