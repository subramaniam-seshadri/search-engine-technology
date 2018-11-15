package cecs429.query;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.IWeightingScheme;
import cecs429.index.Posting;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class RankedRetrieval {

	private IWeightingScheme weightingScheme;
	private String query;
	private Integer numberDocs;
	private String path;

	public RankedRetrieval(IWeightingScheme weightingScheme, String query, String path) {
		this.weightingScheme = weightingScheme;
		this.query = query;
		this.path = path;
	}

	// Each "row" contains following 4 values
	/**
	 * Ld - Double - 8 bytes - Document weight docLength - Long - 8 bytes - number
	 * of tokens in document byteSize - Long - 8 bytes - number of bytes in the file
	 * for the document avgTfd - Double - 8 bytes - avg tfd count for a document
	 * 
	 * 
	 * @param path - path where the index and other files are located.
	 */
	public void getNumberOfDocs() {
		FileInputStream inputStream = null;
		DataInputStream inStream = null;
		Integer numberOfDocs = 0;
		try {
			// read file
			inputStream = new FileInputStream(path + "//docAvgWeight.bin");
			inStream = new DataInputStream(inputStream);
			while (inStream.available() != 0) {

				try {
					inStream.skip(8);
					numberOfDocs = inStream.readInt();
					
				} catch (EOFException e) {

				}
			}
			System.out.println("Number of Docs:" + numberOfDocs);
			numberDocs = numberOfDocs;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<RankedResults> getRankedDocuments(String query, TokenProcessor processor) {
		EnglishTokenStream queryStream = new EnglishTokenStream(new StringReader(query));
		Iterable<String> queryTokens = queryStream.getTokens();
		HashMap<Integer, Double> accumulatorDocMap = new HashMap<Integer, Double>();
		try {
			DiskPositionalIndex di = new DiskPositionalIndex(path);
			
			for (String term : queryTokens) {
				System.out.println("RR for term:" + term);
				List<Posting> termPostings = di.getPositionalPostings(term);
				Integer dft = termPostings.size();
				Double wqt = weightingScheme.getWqt(numberDocs, dft);
				for(Posting p : termPostings) {
					// if map contains the document ID increment it 
					if(accumulatorDocMap.containsKey(p.getDocumentId())) {
						double accumulatorValue = accumulatorDocMap.get(p.getDocumentId());
						Double wdt = weightingScheme.getWdt(p.getPositionsInDoc().size(), p.getDocumentId());
						accumulatorValue += (wqt * wdt);
						accumulatorDocMap.put(p.getDocumentId(), accumulatorValue);
					}
					else {
						accumulatorDocMap.put(p.getDocumentId(), 1.0);
					}
				}
			}
			
			for (Integer key : accumulatorDocMap.keySet()) {
			    if(accumulatorDocMap.get(key)!= 0.0) {
			    	Double Ld = weightingScheme.getLd(key);
			    	Double accumulatorValue = accumulatorDocMap.get(key);
			    	accumulatorValue /= Ld;
			    	accumulatorDocMap.put(key, accumulatorValue);
			    }
			}
			
			PriorityQueue<Map.Entry<Integer, Double>> heap = new PriorityQueue<Map.Entry<Integer, Double>>(new Comparator<Map.Entry<Integer, Double>>(){
				@Override
				public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
					if(entry2.getValue()>entry1.getValue())
						return 1;
					else return -1;
				}
			});
			
			for(Map.Entry<Integer, Double> entry : accumulatorDocMap.entrySet()) {
				heap.offer(entry);
			}
			
			List<RankedResults> resultList = new ArrayList<RankedResults>();
			for(int i = 0; i < 3; i++) {
				Map.Entry<Integer, Double> entry = heap.poll();
				RankedResults rr = new RankedResults();
				rr.setDocumentID(entry.getKey());
				rr.setRetrievalScore(entry.getValue());
				resultList.add(rr);
			}
			
			return resultList;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
