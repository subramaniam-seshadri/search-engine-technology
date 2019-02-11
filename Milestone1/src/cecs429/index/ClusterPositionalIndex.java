package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClusterPositionalIndex implements Index {

	private List<Long> vocabPositionList = new ArrayList<Long>();
	private List<Long> postingPositionList = new ArrayList<Long>();
	private String path = "";
	private RandomAccessFile randomAccessPosting = null;
	private HashMap<Integer, Double> docIDWeightsMap = new HashMap<Integer, Double>();
	private HashMap<Integer, Long> docIDByteSizeMap = new HashMap<Integer, Long>();

	/**
	 * Constructor to read VocabTableLeader.bin file and read positions into respective
	 * lists. Vocab positions will be read into vocabPositionList. Postings
	 * positions will be read into postingPositionList
	 * 
	 * @throws IOException
	 */
	public ClusterPositionalIndex(String path) throws IOException {
		this.path = path;
		FileInputStream inputStream = null;
		DataInputStream inStream = null;
		try {
			inputStream = new FileInputStream(path + "//vocabTableLeader.bin");
			inStream = new DataInputStream(inputStream);
			while (inStream.available() != 0) {
				vocabPositionList.add(inStream.readLong());
				postingPositionList.add(inStream.readLong());
			}

			randomAccessPosting = new RandomAccessFile(path + "//postingsLeader.bin", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			inputStream.close();
			inStream.close();
		}
	}

	/**
	 * This method initializes the document specific values which may be used for
	 * efficient ranked retrieval.
	 * 
	 * @param corpusSize
	 */
	public void readDocValues(Integer corpusSize) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//docWeightsLeaders.bin", "r");
			for (int i = 0; i < corpusSize; i++) {
				int docID = raf.readInt();
				docIDWeightsMap.put(docID, raf.readDouble());
				raf.seek(raf.getFilePointer() + 8);
				docIDByteSizeMap.put(docID, raf.readLong());
				raf.seek(raf.getFilePointer() + 8);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("error read from docWeightsLeaders");
			e.printStackTrace();
		}
	}

	@Override
	public List<Posting> getPositionalPostings(String term) {
		List<Posting> result = null;
		// list lengths should be same. For each term position in vocabPositionTable,
		// there is a corresponding value in postingPosition list
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//vocabLeader.bin", "r");
			int indexVocab = binarySearchVocab(term, 0, vocabPositionList.size() - 1, raf);
			Long startPosition = 0L;
			if(indexVocab < 0)
				return result;
			for (int i = 0; i <= indexVocab; i++) {
				startPosition += postingPositionList.get(i);
			}

			result = retrievePositionalPostings(startPosition);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * This method performs the binary search to efficiently find the position of a
	 * term in the vocab.bin file.
	 * 
	 * @param term       - The term to be searched.
	 * @param startIndex - The start index for the window to search.
	 * @param endIndex   - The end index for the window to search.
	 * @param raf        - The random access file object used to seek to searched
	 *                   position.
	 * @return
	 */
	public int binarySearchVocab(String term, int startIndex, int endIndex, RandomAccessFile raf) {
		while (startIndex <= endIndex) {
			int midIndex = (startIndex + endIndex) / 2;
			if (midIndex < vocabPositionList.size() - 1) { // not the last element, can get the next element
				Long midPosition = vocabPositionList.get(midIndex);
				Long nextMidPosition = vocabPositionList.get(midIndex + 1);
				Long noOfBytesToRead = nextMidPosition - midPosition;
				int result = searchTerm(midPosition, noOfBytesToRead, term, raf);
				if (result == 0) {
					return midIndex;
				} else if (result == -1) {
					return binarySearchVocab(term, midIndex + 1, endIndex, raf);
				} else if (result == 1) {
					return binarySearchVocab(term, startIndex, midIndex - 1, raf);
				} else if (result == -2) {
					return -1;
				}
			} else { // if it is last element of the list, read till end of file
				try {
					Long midPosition = vocabPositionList.get(midIndex);
					Long noOfBytesToRead = raf.length() - midPosition;
					return searchTerm(midPosition, noOfBytesToRead, term, raf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	/**
	 * 
	 * @param startPosition   - Byte position where the term begins in the vocab.bin
	 *                        file.
	 * @param noOfBytesToRead - How long the word is in number of bytes.
	 * @param term            - Term for which we need to get the postings.
	 * @param raf             - The random access file object used to seek to the
	 *                        file position and read bytes.
	 * @return 0 if term and fetched term match, 1 if term is greater than fetched
	 *         term, -1 if term is smaller than fetched term, -2 if the term is not present in the corpus vocabulary.
	 */
	public int searchTerm(Long startPosition, Long noOfBytesToRead, String term, RandomAccessFile raf) {
		String data = "";
		try {
			raf.seek(startPosition);
			for (int i = 0; i < noOfBytesToRead; i++) {
				data += (char) raf.readByte();
			}
			if (term.equals(data)) {
				return 0;
			} else {
				int result = data.compareTo(term);
				if (result < 0) {
					raf.seek(0);
					return -1;
				} else {
					raf.seek(0);
					return 1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -2;
	}

	/**
	 * This method is used to retrieve postings along with positions, given a start
	 * position.
	 * 
	 * @param startPosition - Position from where the reading for the postings
	 *                      starts.
	 * @return - Returns a list of postings read from the start position.
	 */
	public List<Posting> retrievePositionalPostings(Long startPosition) {
		List<Posting> postings = new ArrayList<Posting>();
		try {
			randomAccessPosting.seek(startPosition);
			int postingListSize = randomAccessPosting.readInt();
			postings = new ArrayList<Posting>(postingListSize);
			for (int i = 0; i < postingListSize; i++) {
				int documentID = randomAccessPosting.readInt();
				double wdtDefault = randomAccessPosting.readDouble();
				double wdtTfIdf = randomAccessPosting.readDouble();
				double wdtOkapi = randomAccessPosting.readDouble();
				double wdtWacky = randomAccessPosting.readDouble();
				int termFrequency = randomAccessPosting.readInt();
				List<Integer> positions = new ArrayList<Integer>(termFrequency);
				for (int j = 0; j < termFrequency; j++) {
					positions.add(randomAccessPosting.readInt());
				}
				Posting p = new Posting(documentID, positions);
				p.setWdtDefaultValue(wdtDefault);
				p.setWdtTfIdfValue(wdtTfIdf);
				p.setWdtOkapiValue(wdtOkapi);
				p.setWdtWackyValue(wdtWacky);
				postings.add(p);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return postings;
	}

	@Override
	public List<String> getVocabulary() {
		List<String> vocabularyList = new ArrayList<String>();
		RandomAccessFile raf = null;
		for (int i = 0; i < vocabPositionList.size(); i++) {
			try {
				raf = new RandomAccessFile(path + "//vocabLeader.bin", "r");
				if (i == (vocabPositionList.size() - 1)) {
					// last element of list, read from this position till end of file
					Long startPosition = vocabPositionList.get(i);
					raf.seek(0);
					Long nextPosition = raf.length();
					vocabularyList.add(readTerm(startPosition, nextPosition, raf));
				} else {
					// not last element read till next position
					Long startPosition = vocabPositionList.get(i);
					Long nextPosition = vocabPositionList.get(i + 1);
					vocabularyList.add(readTerm(startPosition, nextPosition, raf));
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return vocabularyList;
	}

	/**
	 * This method reads a term from the vocab.bin file.
	 * 
	 * @param startPosition - start position in vocab.bin file.
	 * @param nextPosition  - Position of next term in vocab.bin file.
	 * @param raf           - Random access file object used to access the file.
	 * @return
	 */
	public static String readTerm(Long startPosition, Long nextPosition, RandomAccessFile raf) {
		Long numberOfBytesToRead = nextPosition - startPosition;
		String data = "";
		try {
			raf.seek(startPosition);
			for (int i = 0; i < numberOfBytesToRead; i++) {
				data += (char) raf.readByte();
			}
			System.out.println(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	
	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> result = null;
		// list lengths should be same. For each term position in vocabPositionTable,
		// there is a corresponding value in postingPositionTable list
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//vocabLeader.bin", "r");
			int indexVocab = binarySearchVocab(term, 0, vocabPositionList.size() - 1, raf);
			Long startPosition = 0L;
			for (int i = 0; i <= indexVocab; i++) {
				startPosition += postingPositionList.get(i);
			}
			result = retrievePostings(startPosition);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * This method is used to retrieve postings without positions, given a start
	 * position.
	 * 
	 * @param startPosition - Position from where the reading for the postings
	 *                      starts.
	 * @return - Returns a list of postings read from the start position.
	 */
	public List<Posting> retrievePostings(Long startPosition) {
		List<Posting> postings = new ArrayList<Posting>();
		try {
			randomAccessPosting.seek(startPosition);
			int postingListSize = randomAccessPosting.readInt();
			postings = new ArrayList<Posting>(postingListSize);
			for (int i = 0; i < postingListSize; i++) {
				int documentID = randomAccessPosting.readInt();
				double wdtDefault = randomAccessPosting.readDouble();
				double wdtTfIdf = randomAccessPosting.readDouble();
				double wdtOkapi = randomAccessPosting.readDouble();
				double wdtWacky = randomAccessPosting.readDouble();
				int termFrequency = randomAccessPosting.readInt();
				// skip postings
				randomAccessPosting.seek((randomAccessPosting.getFilePointer() + termFrequency * 4));
				Posting p = new Posting(documentID, termFrequency);
				p.setWdtDefaultValue(wdtDefault);
				p.setWdtTfIdfValue(wdtTfIdf);
				p.setWdtOkapiValue(wdtOkapi);
				p.setWdtWackyValue(wdtWacky);
				postings.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return postings;
	}

	public ClusterPositionalIndex() {
		super();
	}
}
