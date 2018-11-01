package cecs429.index;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiskIndexWriter {

	public void writeIndex(Index index, String path) {
		List<String> vocabulary = index.getVocabulary();
		try {
			List<Long> vocabPositions = createVocabFile(path, vocabulary);
			List<Long> postingsPositions = createPostingsFile(index, path, vocabulary);
			createVocabTable(path, vocabulary, vocabPositions, postingsPositions);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createDocWeightsFile(String path, HashMap<String, Integer> termFrequencyMap) throws IOException {
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;
		try {
			outputStream = new FileOutputStream(path + "//docWeights.bin");
			outStream = new DataOutputStream(new BufferedOutputStream(outputStream));
			//for each term in map calculate wd,t for that term.
			List<Double> wdtList = new ArrayList<Double>();
			for (String key : termFrequencyMap.keySet()) {
				double wdt = (1 + Math.log(termFrequencyMap.get(key))); 
				wdtList.add(wdt*wdt);
			}
			double Ld = Math.sqrt(wdtList.stream().mapToDouble(Double::doubleValue).sum());
			//System.out.println(Ld);
			outStream.writeDouble(Ld);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			outStream.flush();
			outStream.close();
			outputStream.flush();
			outputStream.close();
		}
	}

	public List<Long> createPostingsFile(Index index, String path, List<String> vocabulary) throws IOException {
		// Need to be opened and written in binary mode
		PositionalInvertedIndex pIndex = (PositionalInvertedIndex) index;
		List<Long> bytePositions = new ArrayList<Long>();
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;
		try {
			outputStream = new FileOutputStream(path + "//postings.bin");
			outStream = new DataOutputStream(new BufferedOutputStream(outputStream));

			// first term starts at zero
			int previousPosition = 0;
			bytePositions.add((long) outStream.size()); // start position of first term
			List<Posting> postings1 = pIndex.vocabulary.get(vocabulary.get(0));
			outStream.writeInt(postings1.size()); // size of postings list
			for (Posting p : postings1) {
				outStream.writeInt(p.getDocumentId()); // write document id
				outStream.writeInt(p.getPositionsInDoc().size()); // write frequency of term in doc
				for (int pos : p.getPositionsInDoc()) {
					outStream.writeInt(pos); // positions in doc
				}
			}
			int gap = outStream.size() - previousPosition;
			previousPosition = outStream.size();

			for (int i = 1; i < vocabulary.size(); i++) {
				//System.out.println(vocabulary.get(i) + "-" + gap);
				bytePositions.add((long) gap);
				List<Posting> postings = pIndex.vocabulary.get(vocabulary.get(i));
				outStream.writeInt(postings.size()); // doc frequency
				for (Posting p : postings) {
					outStream.writeInt(p.getDocumentId());
					outStream.writeInt(p.getPositionsInDoc().size());
					for (int pos : p.getPositionsInDoc())
						outStream.writeInt(pos);
				}
				gap = outStream.size() - previousPosition;
				previousPosition = outStream.size();
			}

			/*
			 * for (String term : vocabulary) { System.out.println(term + "-" +
			 * currentPosition); bytePositions.add(currentPosition);
			 * 
			 * List<Posting> postings = pIndex.vocabulary.get(term);
			 * outStream.writeInt(postings.size()); // doc frequency currentPosition +=
			 * Integer.BYTES; for (Posting p : postings) {
			 * outStream.writeInt(p.getDocumentId()); // docID currentPosition +=
			 * Integer.BYTES; outStream.writeInt(p.getPositionsInDoc().size()); // term
			 * frequency currentPosition += Integer.BYTES; for (int pos :
			 * p.getPositionsInDoc()) { outStream.writeLong(pos); // positions in doc
			 * currentPosition += Long.BYTES; } } }
			 */

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			outStream.flush();
			outStream.close();
			outputStream.flush();
			outputStream.close();
		}

		return bytePositions;
	}

	public List<Long> createVocabFile(String path, List<String> vocabulary) throws IOException {

		List<Long> bytePositions = new ArrayList<Long>();
		FileOutputStream outputStream = new FileOutputStream(path + "//vocab.bin");
		long currentPosition = 0;
		for (String term : vocabulary) {

			outputStream.write(term.getBytes());
			bytePositions.add(currentPosition);
			currentPosition += term.getBytes().length;
		}
		outputStream.close();

		return bytePositions;
	}

	public void createVocabTable(String path, List<String> vocabulary, List<Long> vocabPositions,
			List<Long> postingsPositions) throws IOException {
		// Need to be opened and written in binary mode
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;
		try {
			outputStream = new FileOutputStream(path + "//vocabTable.bin");
			outStream = new DataOutputStream(new BufferedOutputStream(outputStream));
			for (int i = 0; i < vocabulary.size(); i++) {
				outStream.writeLong(vocabPositions.get(i));
				outStream.writeLong(postingsPositions.get(i));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally {
			outStream.flush();
			outStream.close();
			outputStream.flush();
			outputStream.close();
		}
	}

}
