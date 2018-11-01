package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index {

	List<Long> vocabPositionTable = new ArrayList<Long>();
	List<Long> postingPositionTable = new ArrayList<Long>();

	@Override
	public List<Posting> getPostings(String term) {
		readVocabTablePositions();
		System.out.println("Vocab Position List Size:" + vocabPositionTable.size());
		System.out.println("Posting Position List Size:" + postingPositionTable.size());
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

	public Long returnSearchPosition(String term, List<Long> vocabPositionTable) {
		if (vocabPositionTable.size() >= 1) {
			int midVocabTable = vocabPositionTable.size() / 2;
			Long midPosition = vocabPositionTable.get(midVocabTable);
			Long nextMidPosition = vocabPositionTable.get(midVocabTable + 1);
			Long noOfBytesToRead = nextMidPosition - midPosition;
			
			FileInputStream inputStream = null;
			DataInputStream inStream = null;
			try {
				inputStream = new FileInputStream("src//index" + "//vocab.bin");
				inStream = new DataInputStream(inputStream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			searchTerm(midPosition, noOfBytesToRead, term, inStream);
		}
		return null;
	}

	public int searchTerm(Long startPosition, Long noOfBytesToRead, String term, DataInputStream inStream) {

		return 0;
	}

	@Override
	public List<String> getVocabulary() {

		return null;
	}

}
