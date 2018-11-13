package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index {

	private List<Long> vocabPositionList = new ArrayList<Long>();
	private List<Long> postingPositionList = new ArrayList<Long>();
	private String path = "";
	private RandomAccessFile randomAccessPosting = null;
	// Read vocab positions from Vocab.bin in vocabPositionList
	// Read posting positions from postings.bin in postingPositionList

	/**
	 * Constructor to read VocabTable.bin file and read positions into respective
	 * lists. Vocab positions will be read into vocabPositionList. Postings
	 * positions will be read into postingPositionList
	 * 
	 * @throws IOException
	 */
	public DiskPositionalIndex(String path) throws IOException {
		this.path = path;
		FileInputStream inputStream = null;
		DataInputStream inStream = null;
		try {
			inputStream = new FileInputStream(path + "//vocabTable.bin");
			inStream = new DataInputStream(inputStream);
			while (inStream.available() != 0) {
				vocabPositionList.add(inStream.readLong());
				postingPositionList.add(inStream.readLong());
			}
			randomAccessPosting = new RandomAccessFile(path + "//postings.bin", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			inputStream.close();
			inStream.close();
		}
	}

	@Override
	public List<Posting> getPositionalPostings(String term) {
		List<Posting> result = null;
		System.out.println("Vocab Position List Size:" + vocabPositionList.size());
		System.out.println("Posting Position List Size:" + postingPositionList.size());
		// list lengths should be same. For each term position in vocabPositionTable,
		// there is a corresponding value in postingPositionTable list
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path + "//vocab.bin", "r");
			int indexVocab = binarySearchVocab(term, 0, vocabPositionList.size() - 1, raf);
			Long startPosition = 0L;
			for (int i = 0; i <= indexVocab; i++) {
				startPosition += postingPositionList.get(i);
			}

			result = retrievePostings(startPosition);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return result;
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
	 *         term, -1 if term is smaller than fetched term.
	 */
	public int searchTerm(Long startPosition, Long noOfBytesToRead, String term, RandomAccessFile raf) {
		String data = "";
		try {
			raf.seek(startPosition);
			for (int i = 0; i < noOfBytesToRead; i++) {
				data += (char) raf.readByte();
			}
			System.out.println(data);
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
		return 0;
	}

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
				} else if (result < 0) {
					return binarySearchVocab(term, midIndex + 1, endIndex, raf);
				} else {
					return binarySearchVocab(term, startIndex, midIndex, raf);
				}
			} else { // if it is last element of the list, read till end of file
				try {
					Long midPosition = vocabPositionList.get(midIndex);
					Long noOfBytesToRead = raf.length();
					return searchTerm(midPosition, noOfBytesToRead, term, raf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	public List<Posting> retrievePostings(Long startPosition) {
		List<Posting> postings = null;
		try {
			randomAccessPosting.seek(startPosition);
			int postingListSize = randomAccessPosting.readInt();
			postings = new ArrayList<Posting>(postingListSize);
			for (int i = 0; i < postingListSize; i++) {
				int documentID = randomAccessPosting.readInt();
				int termFrequency = randomAccessPosting.readInt();
				List<Integer> positions = new ArrayList<Integer>(termFrequency);
				for (int j = 0; j < termFrequency; j++) {
					positions.add(randomAccessPosting.readInt());
				}
				Posting p = new Posting(documentID, positions);
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
				raf = new RandomAccessFile(path + "//vocab.bin", "r");
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
}
