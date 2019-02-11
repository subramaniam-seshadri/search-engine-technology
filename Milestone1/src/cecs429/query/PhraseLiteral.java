package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();

	// The kth position between two terms, k = 1 for phrase literals, k = n for NEAR
	// /k literals where n is entered by user
	private int k;

	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms, int k2) {
		mTerms.addAll(terms);
		k = k2;
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms, int k2) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
		k = k2;
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}

	@Override
	public List<Posting> getPostings(Index index) {
		// Retrieve the postings for the individual terms in
		// the phrase,
		// and positional merge them together.
		List<Posting> result = null;

		if (mTerms != null) {
			try {
				result = intersectPostings(mTerms, index);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<Posting> intersectPostings(List<String> Terms, Index index) {
		List<Posting> common = new ArrayList<>();
		int i = mTerms.size() - 1;
		while (i > 0) {

			String term1 = Terms.get(i);
			String term2 = Terms.get(--i);
			List<Posting> postingResult1 = index.getPositionalPostings(term1);
			List<Posting> postingResult2 = index.getPositionalPostings(term2);

			if (CollectionUtils.isEmpty(postingResult1) || CollectionUtils.isEmpty(postingResult2)) {
				return Collections.EMPTY_LIST;
			} else {
				if (CollectionUtils.isEmpty(common))
					common = positionalIntersect(postingResult1, postingResult2, k);
				else
					common = positionalIntersect(common, postingResult2, k);
				if (common.isEmpty()) {
					break;
				}
			}
		}
		return common;
	}

	public List<Posting> positionalIntersect(List<Posting> term1, List<Posting> term2, int k) {
		List<Posting> postingResult = new ArrayList<Posting>();
		int i = 0, j = 0;
		for (; i < term1.size() && j < term2.size();) {
			if (term1.get(i).getDocumentId() == term2.get(j).getDocumentId()) {
				List<Integer> pp1 = term1.get(i).getPositionsInDoc();
				List<Integer> pp2 = term2.get(j).getPositionsInDoc();
				List<Integer> phrasePosition = new ArrayList<Integer>();
				Posting p = null;
				
				
				/*for (Integer positions1 : pp1) {
					for (Integer positions2 : pp2) {
						int relative_position = positions1 - positions2;
						if ((relative_position <= k) && (relative_position >= 0)) {
							phrasePosition.add(positions2);
						} else if ((positions1 - positions2) > k) {
							continue;
						}
					}
				}*/
				int n =0, q = 0;
				for(; n < pp1.size() && q <pp2.size();) {
					int _position1 = pp1.get(n);
					int _position2 = pp2.get(q);
					if(_position1 > _position2) {
						int _relative = _position1 - _position2;
						if((_relative)<=k && (_relative >=0)){
							phrasePosition.add(_position2);
							n++; 
							q++;
							continue;
						}else {
							q++;
							continue;
						}
					}else {
						n++;
						continue;
					}
				}
				
				
				if (phrasePosition.isEmpty()) {
					i += 1;
					j += 1;
					continue;
				} else {
					Posting ans = new Posting(term1.get(i).getDocumentId(), phrasePosition);
					postingResult.add(ans);
					i += 1;
					j += 1;
					continue;
				}
			} else if (term1.get(i).getDocumentId() < term2.get(j).getDocumentId()) {
				i += 1;
			} else {
				j += 1;
			}
		}
		return postingResult;
	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}