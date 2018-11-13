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

	private String path;
	private HashMap<String, Integer> termFrequencyMap;
	private Index index;

	/**
	 * Constructor to initialize things that will be needed to write index to disk.
	 * 
	 * @param path  - where the bin files will be generated
	 * @param index - index to write to disk
	 */
	public DiskIndexWriter(String path, Index index) {
		this.path = path;
		this.index = index;
	}

	public DiskIndexWriter(String path) {
		this.path = path;
	}

	/**
	 * Constructor to initialize things that will be needed to write document
	 * weights to disk.
	 * 
	 * @param path             - where the bin files will be generated
	 * @param termFrequencyMap - using which document weights will be calculated
	 */
	public DiskIndexWriter(String path, HashMap<String, Integer> termFrequencyMap) {
		this.path = path;
		this.termFrequencyMap = termFrequencyMap;
	}

	public void writeIndex() {
		List<String> vocabulary = index.getVocabulary();
		try {
			List<Long> vocabPositions = createVocabFile(path, vocabulary);
			List<Long> postingsPositions = createPostingsFile(index, path, vocabulary);
			createVocabTable(path, vocabulary, vocabPositions, postingsPositions);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Long createDocWeightsFile(Long fileByteSize) throws IOException {
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;
		Long docLength = 0L;
		try {
			outputStream = new FileOutputStream(path + "//docWeights.bin",true);
			outStream = new DataOutputStream(new BufferedOutputStream(outputStream));
			// for each term in map add frequency of that term to docLength to get total
			// number of tokens in document

			List<Integer> frequencyList = new ArrayList<Integer>();

			// for each term in map calculate wd,t for that term.
			List<Double> wdtList = new ArrayList<Double>();
			for (String key : termFrequencyMap.keySet()) {
				frequencyList.add(termFrequencyMap.get(key));
				docLength += termFrequencyMap.get(key);
				double wdt = (1 + Math.log(termFrequencyMap.get(key)));
				wdtList.add(wdt * wdt);
			}
			int sumFrequency = frequencyList.stream().mapToInt(Integer::intValue).sum();
			double avgTfd = ((double) sumFrequency) / ((double) frequencyList.size());
			double Ld = Math.sqrt(wdtList.stream().mapToDouble(Double::doubleValue).sum());
			System.out.println("Doc weight:" + Ld);
			System.out.println("Doc Length:" + docLength);
			System.out.println("Doc Size in bytes:" + fileByteSize);
			System.out.println("Avg term frequency in doc:" + avgTfd);
			outStream.writeDouble(Ld);
			outStream.writeLong(docLength);
			outStream.writeLong(fileByteSize);
			outStream.writeDouble(avgTfd);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			outStream.flush();
			outStream.close();
			outputStream.flush();
			outputStream.close();
		}
		return docLength;
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
				// System.out.println(vocabulary.get(i) + "-" + gap);
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
		} finally {
			outStream.flush();
			outStream.close();
			outputStream.flush();
			outputStream.close();
		}
	}

	/**
	 * This function writes out the average number of tokens in all the documents in
	 * the corpus to the docWeights.bin file. The value is appended to the end of
	 * the file.
	 * 
	 * @param avgDocLength - List of Long containing the docLength for each of the
	 *                     documents in the corpus
	 */
	public void writeAvgDocLength(List<Long> avgDocLength) throws IOException {
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;
		long sumDocLength = avgDocLength.stream().mapToLong(Long::longValue).sum();
		double avgTfd = ((double) sumDocLength) / ((double) avgDocLength.size());
		try {
			outputStream = new FileOutputStream(path + "//docWeights.bin", true);
			outStream = new DataOutputStream(new BufferedOutputStream(outputStream));
			System.out.println("Average doc Length for corpus: " + avgTfd);
			outStream.writeDouble(avgTfd);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			outStream.flush();
			outStream.close();
			outputStream.flush();
			outputStream.close();
		}
	}
}
