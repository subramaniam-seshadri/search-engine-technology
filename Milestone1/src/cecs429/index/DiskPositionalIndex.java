package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index {

	List<Long> vocabPositionTable = new ArrayList<Long>();
	List<Long> postingPositionTable = new ArrayList<Long>();

	@Override
	public List<Posting> getPostings(String term) {
		readVocabTablePositions(); // initialize the lists
		System.out.println("Vocab Position List Size:" + vocabPositionTable.size());
		System.out.println("Posting Position List Size:" + postingPositionTable.size());
		// list lengths should be same. For each term position in vocabPositionTable,
		// there is a corresponding value in postingPositionTable list
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile("src//index" + "//vocab.bin","r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		
		
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream("src//index" + "//vocab.bin");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
		binarySearchVocab(term, 0, vocabPositionTable.size()-1, raf);
		return null;
	}

	public void readVocabTablePositions() {
		try {
			FileInputStream inputStream = new FileInputStream("src/index" + "//vocabTable.bin");
			DataInputStream inStream = new DataInputStream(inputStream);
			while (inStream.available() != 0) {
				vocabPositionTable.add(inStream.readLong());
				postingPositionTable.add(inStream.readLong());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// right till here

	/**
	 * 
	 * @param startPosition
	 * @param noOfBytesToRead
	 * @param term
	 * @param inStream
	 * @return 0 if term and fetched term match, 1 if term is greater than fetched
	 *         term, -1 if term is smaller than fetched term
	 */
	public int searchTerm(Long startPosition, Long noOfBytesToRead, String term, RandomAccessFile raf) {
		String data = "";
		try {
			raf.seek(startPosition);
			//inputStream.skip(startPosition);

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
			if (midIndex < vocabPositionTable.size() - 1) { // not the last element, can get the next element
				Long midPosition = vocabPositionTable.get(midIndex);
				Long nextMidPosition = vocabPositionTable.get(midIndex + 1);
				Long noOfBytesToRead = nextMidPosition - midPosition;
				int result = searchTerm(midPosition, noOfBytesToRead, term, raf);
				if (result == 0) {
					return midIndex;
				} else if (result < 0) {
					return binarySearchVocab(term,midIndex + 1, endIndex, raf);
				} else {
					return binarySearchVocab(term, startIndex, midIndex, raf);
				}
			} else { // last element of the list
				try {
					Long midPosition = vocabPositionTable.get(midIndex);
					Long noOfBytesToRead = raf.length();
					return searchTerm(midPosition, noOfBytesToRead, term, raf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	/*
	 * 
	 * public Long returnSearchPosition(String term, List<Long> vocabPositionTable)
	 * { if (vocabPositionTable.size() >= 1) { int midVocabTable =
	 * vocabPositionTable.size() / 2; Long midPosition =
	 * vocabPositionTable.get(midVocabTable); Long nextMidPosition =
	 * vocabPositionTable.get(midVocabTable + 1); Long noOfBytesToRead =
	 * nextMidPosition - midPosition;
	 * 
	 * FileInputStream inputStream = null; DataInputStream inStream = null; try {
	 * inputStream = new FileInputStream("src//index" + "//vocab.bin"); inStream =
	 * new DataInputStream(inputStream); } catch (FileNotFoundException e) {
	 * e.printStackTrace(); } searchTerm(midPosition, noOfBytesToRead, term,
	 * inStream); } return null; }
	 * 
	 */

	@Override
	public List<String> getVocabulary() {

		return null;
	}

}
