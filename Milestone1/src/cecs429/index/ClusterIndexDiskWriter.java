package cecs429.index;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cecs429.cluster.ClusterDoc;
import cecs429.documents.Document;

public class ClusterIndexDiskWriter {

	private String path;
	private Index index;

	public ClusterIndexDiskWriter(String path) {
		this.path = path;
	}
	
	public void setIndex(Index index) {
		this.index = index;
	}

	private HashMap<Integer, Long> docIDLengthMap = new HashMap<Integer, Long>();
	private HashMap<Integer, Double> docIDWeightsMap = new HashMap<Integer, Double>();
	private HashMap<Integer, Long> docIDByteSizeMap = new HashMap<Integer, Long>();
	private HashMap<Integer, Double> docIDAvgTfdMap = new HashMap<Integer, Double>();
	private double docLengthA = 0.0;

	
	/**
	 * This method is used to create the document weights file for the leaders
	 * @param doc - The document object
	 * @param cd - The ClusterDoc object which contains all the values relating to a leader document to be written.
	 */
	public void createLeaderDocWeightsFile(Document doc, ClusterDoc cd) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//docWeightsLeaders.bin", "rw");

			raf.seek(raf.length());
			raf.writeInt(cd.getDocId());
			raf.writeDouble(cd.getLd());
			docIDWeightsMap.put(doc.getId(), cd.getLd());
			raf.writeLong(cd.getDocLength());
			docIDLengthMap.put(doc.getId(), cd.getDocLength());
			raf.writeLong(doc.getDocSize());
			docIDByteSizeMap.put(doc.getId(), doc.getDocSize());
			raf.writeDouble(cd.getAvgTfd());
			docIDAvgTfdMap.put(doc.getId(),cd.getAvgTfd());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * This method is used to write leader index to the disk.
	 */
	public void writeLeaderIndex() {
		List<String> vocabulary = index.getVocabulary();
		try {
			List<Long> vocabPositions = createVocabFile(path, vocabulary);
			List<Long> postingsPositions = createPostingsFile(index, path, vocabulary);
			createVocabTable(path, vocabulary, vocabPositions, postingsPositions);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param path       - Path where file is to be created.
	 * @param vocabulary - List of vocabulary for the corpus.
	 * @return - Returns a list of positions where start of each vocabulary term is
	 *         written to the vocab.bin file.
	 * @throws IOException
	 */
	public List<Long> createVocabFile(String path, List<String> vocabulary) throws IOException {

		List<Long> bytePositions = new ArrayList<Long>();
		FileOutputStream outputStream = new FileOutputStream(path + "//vocabLeader.bin");
		long currentPosition = 0;
		for (String term : vocabulary) {

			outputStream.write(term.getBytes());
			bytePositions.add(currentPosition);
			currentPosition += term.getBytes().length;
		}
		outputStream.close();

		return bytePositions;
	}

	/**
	 * 
	 * @param path              - Path where file is to be created.
	 * @param vocabulary        - List of vocabulary for the corpus.
	 * @param vocabPositions    - List of positions where each position represents
	 *                          start position of a vocabulary term in the vocab.bin
	 *                          file
	 * @param postingsPositions - List of positions where each position represents
	 *                          start position of a posting in the postings.bin file
	 * @throws IOException
	 */
	public void createVocabTable(String path, List<String> vocabulary, List<Long> vocabPositions,
			List<Long> postingsPositions) throws IOException {
		// Need to be opened and written in binary mode
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;
		try {
			outputStream = new FileOutputStream(path + "//vocabTableLeader.bin");
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
	 * 
	 * @param index - The index to be written to the postingsLeader.bin file
	 * @param path - path where the index is to be stored on disk.
	 * @param vocabulary - List of Vocabulary
	 * @return - Returns a list of positions where start of each posting is written to the postings.bin file.
	 * @throws IOException
	 */
	public List<Long> createPostingsFile(Index index, String path, List<String> vocabulary) throws IOException {
		// Need to be opened and written in binary mode
		PositionalInvertedIndex pIndex = (PositionalInvertedIndex) index;
		List<Long> bytePositions = new ArrayList<Long>();
		FileOutputStream outputStream = null;
		DataOutputStream outStream = null;

		try {
			outputStream = new FileOutputStream(path + "//postingsLeader.bin");
			outStream = new DataOutputStream(new BufferedOutputStream(outputStream));

			// first term starts at zero
			int previousPosition = 0;
			bytePositions.add((long) outStream.size()); // start position of first term
			List<Posting> postings1 = pIndex.vocabulary.get(vocabulary.get(0));
			outStream.writeInt(postings1.size()); // size of postings list
			for (Posting p : postings1) {
				outStream.writeInt(p.getDocumentId()); // write document id
				int termFrequency = p.getPositionsInDoc().size();
				outStream = writeDefaultWdt(outStream, path, termFrequency, p.getDocumentId());
				outStream = writeTfIdfWdt(outStream, path, termFrequency, p.getDocumentId());
				outStream = writeOKAPIWdt(outStream, path, termFrequency, p.getDocumentId());
				outStream = writeWackyWdt(outStream, path, termFrequency, p.getDocumentId());

				outStream.writeInt(termFrequency); // write frequency of term in doc
				for (int pos : p.getPositionsInDoc()) {
					outStream.writeInt(pos); // positions in doc
				}
			}
			int gap = outStream.size() - previousPosition;
			previousPosition = outStream.size();

			for (int i = 1; i < vocabulary.size(); i++) {
				bytePositions.add((long) gap);
				List<Posting> postings = pIndex.vocabulary.get(vocabulary.get(i));
				outStream.writeInt(postings.size()); // doc frequency
				for (Posting p : postings) {
					outStream.writeInt(p.getDocumentId()); // write document id
					int termFrequency = p.getPositionsInDoc().size();
					outStream = writeDefaultWdt(outStream, path, termFrequency, p.getDocumentId());
					outStream = writeTfIdfWdt(outStream, path, termFrequency, p.getDocumentId());
					outStream = writeOKAPIWdt(outStream, path, termFrequency, p.getDocumentId());
					outStream = writeWackyWdt(outStream, path, termFrequency, p.getDocumentId());

					outStream.writeInt(termFrequency); // write frequency of term in doc
					for (int pos : p.getPositionsInDoc())
						outStream.writeInt(pos);
				}
				gap = outStream.size() - previousPosition;
				previousPosition = outStream.size();
			}
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

	
	/**
	 * @param outStream     - The stream used to write the wdt values for Default
	 *                      Weighting scheme
	 * @param path          - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being
	 *                      calculated.
	 * @param documentID    - Document Id.
	 * @return - The DataOutputStream object to be used further by the calling method.
	 */
	public DataOutputStream writeDefaultWdt(DataOutputStream outStream, String path, int termFrequency,
			int documentID) {
		IWeightingScheme defaultWeightingScheme = new DefaultWeightingScheme();
		try {
			outStream.writeDouble(defaultWeightingScheme.getWdt(termFrequency, documentID));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outStream;
	}

	/**
	 * @param outStream     - The stream used to write the wdt values for TfIdf
	 *                      Weighting scheme
	 * @param path          - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being
	 *                      calculated.
	 * @param documentID    - Document Id.
	 * @return - The DataOutputStream object to be used further by the calling method.
	 */
	public DataOutputStream writeTfIdfWdt(DataOutputStream outStream, String path, int termFrequency, int documentID) {
		IWeightingScheme tdIDFWeightingScheme = new TfIdfWeightingScheme();
		try {
			outStream.writeDouble(tdIDFWeightingScheme.getWdt(termFrequency, documentID));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outStream;
	}

	/**
	 * @param outStream     - The stream used to write the wdt values for OKAPI BM
	 *                      Weighting scheme
	 * @param path          - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being
	 *                      calculated.
	 * @param documentID    - Document Id.
	 * @return - The DataOutputStream object to be used further by the calling method.
	 */
	public DataOutputStream writeOKAPIWdt(DataOutputStream outStream, String path, int termFrequency, int documentID) {
		IWeightingScheme okapiBMWeightingScheme = new OkapiBM25WeightingScheme(path, this);
		try {
			outStream.writeDouble(okapiBMWeightingScheme.getWdt(termFrequency, documentID));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outStream;
	}

	/**
	 * @param outStream     - The stream used to write the wdt values for Wacky
	 *                      Weighting scheme
	 * @param path          - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being
	 *                      calculated.
	 * @param documentID    - Document Id.
	 * @return - The DataOutputStream object to be used further by the calling method.
	 */
	public DataOutputStream writeWackyWdt(DataOutputStream outStream, String path, int termFrequency, int documentID) {
		IWeightingScheme wackyWeightingScheme = new WackyWeightingScheme(path, this);
		try {
			outStream.writeDouble(wackyWeightingScheme.getWdt(termFrequency, documentID));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outStream;
	}

	
	/**
	 * This method writes out the average document length for the whole corpus
	 * @param avgDocLength - Contains a list of document lengths for each of the documents present in the corpus.
	 */
	public void writeAvgDocLength(List<Long> avgDocLength) {
		RandomAccessFile raf = null;
		long sumDocLength = avgDocLength.stream().mapToLong(Long::longValue).sum();
		double avgTfd = ((double) sumDocLength) / ((double) avgDocLength.size());
		try {
			raf = new RandomAccessFile(path + "//docWeightsLeaders.bin", "rw");
			System.out.println("Average doc Length for leader corpus: " + avgTfd);
			docLengthA = avgTfd;
			raf.seek(raf.length());
			raf.writeDouble(avgTfd);
			raf.writeInt(avgDocLength.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Long getDocLengthD(Integer DocID) {
		return docIDLengthMap.get(DocID).longValue();
	}

	public Double getDocWeightD(Integer DocID) {
		return docIDWeightsMap.get(DocID);
	}

	public Long getDocSizeD(Integer DocID) {
		return docIDByteSizeMap.get(DocID);
	}

	public Double getAvgTdfD(Integer DocID) {
		return docIDAvgTfdMap.get(DocID);
	}
	
	public double getDocLengthA() {
		return docLengthA;
	}
}
