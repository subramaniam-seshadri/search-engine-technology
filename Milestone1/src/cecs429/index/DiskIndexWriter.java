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

public class DiskIndexWriter {

	private String path;
	private Index index;

	// For fast retrieval of document related values, used when writing wdt values
	// to postings.bin file
	private HashMap<Integer, Long> docIDLengthMap = new HashMap<Integer, Long>();
	private HashMap<Integer, Double> docIDWeightsMap = new HashMap<Integer, Double>();
	private HashMap<Integer, Long> docIDByteSizeMap = new HashMap<Integer, Long>();
	private HashMap<Integer, Double> docIDAvgTfdMap = new HashMap<Integer, Double>();
	private double docLengthA = 0.0;

	
	/**
	 * Constructor to initialize the disk index writer with the path where the index is stored.
	 * @param path
	 */
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

	
	
	/**
	 * Method that calls the methods to create the files vocab.bin, postings.bin and
	 * vocabTable.bin
	 */
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

	/**
	 * 
	 * @param index - The index to be written to the postings.bin file
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
			outputStream = new FileOutputStream(path + "//postings.bin");
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
	 * @param outStream - The stream used to write the wdt values for Default Weighting scheme
	 * @param path - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being calculated.
	 * @param documentID - Document Id.
	 * @return
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
	 * @param outStream - The stream used to write the wdt values for TfIdf Weighting scheme
	 * @param path - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being calculated.
	 * @param documentID - Document Id.
	 * @return
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
	 * @param outStream - The stream used to write the wdt values for OKAPI BM Weighting scheme
	 * @param path - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being calculated.
	 * @param documentID - Document Id.
	 * @return
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

	public double getDocLengthA() {
		return docLengthA;
	}

	/**
	 * @param outStream - The stream used to write the wdt values for Wacky Weighting scheme
	 * @param path - Path of file to be written
	 * @param termFrequency - Frequency of the term for which wdt is being calculated.
	 * @param documentID - Document Id.
	 * @return
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
	 * 
	 * @param path - Path where file is to be created.
	 * @param vocabulary - List of vocabulary for the corpus.
	 * @return - Returns a list of positions where start of each vocabulary term is written to the vocab.bin file.
	 * @throws IOException
	 */
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
	
	/**
	 * 
	 * @param path - Path where file is to be created.
	 * @param vocabulary - List of vocabulary for the corpus.
	 * @param vocabPositions - List of positions where each position represents start position of a vocabulary term in the vocab.bin file
	 * @param postingsPositions - List of positions where each position represents start position of a posting in the postings.bin file
	 * @throws IOException
	 */
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
	 * This method creates the docWeights.bin file which contains all the document related values.
	 * Contains:
	 * Document Weight : Ld - Double
	 * Document Length : docLengthd - Long
	 * Document Size : byteSize - Long
	 * Document average term frequency : avgtfd - Double
	 * @param doc - The documentb object for which the values are to be written
	 * @param cd - The ClusterDoc object which contains all the above values wrapped in it.
	 */
	public void createDocWeightsFile(Document doc, ClusterDoc cd) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//docWeights.bin", "rw");

			raf.seek(raf.length());
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
	 * This method writes out the average document length for the whole corpus
	 * @param avgDocLength - Contains a list of document lengths for each of the documents present in the corpus.
	 */
	public void writeAvgDocLength(List<Long> avgDocLength) {
		RandomAccessFile raf = null;
		long sumDocLength = avgDocLength.stream().mapToLong(Long::longValue).sum();
		double avgTfd = (((double) sumDocLength) / ((double) avgDocLength.size()));
		try {
			raf = new RandomAccessFile(path + "//docWeights.bin", "rw");
			System.out.println("Average doc Length for corpus: " + avgTfd);
			raf.seek(raf.length());
			raf.writeDouble(avgTfd);
			docLengthA = avgTfd;
			raf.writeInt(avgDocLength.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setDocLengthA(double docLengthA) {
		this.docLengthA = docLengthA;
	}

	public Index getIndex() {
		return index;
	}

	public void setIndex(Index index) {
		this.index = index;
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
}
