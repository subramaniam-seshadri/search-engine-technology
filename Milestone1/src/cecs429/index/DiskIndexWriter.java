package cecs429.index;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskIndexWriter {

	public void writeIndex(Index index, String path) {
		List<String> vocabulary = index.getVocabulary();
		try {
			createVocabFile(path, vocabulary);
			createPostingsFile(index, path, vocabulary);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createPostingsFile(Index index, String path, List<String> vocabulary) throws IOException {
		// Need to be opened and written in binary mode
		PositionalInvertedIndex pIndex = (PositionalInvertedIndex) index;
		try {
			FileOutputStream outputStream = new FileOutputStream(path + "//postings.bin");
			long currentPosition = 0;
			DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(outputStream));
			for (String term : vocabulary) {
				System.out.println(term + "-" + currentPosition);
				List<Posting> postings = pIndex.vocabulary.get(term);
				// DataOutputStream outStream = new DataOutputStream(new
				// BufferedOutputStream(outputStream));
				outStream.writeInt(postings.size()); // doc frequency
				currentPosition += Integer.BYTES;
				for (Posting p : postings) {
					outStream.writeInt(p.getDocumentId()); // docID
					currentPosition += Integer.BYTES;
					outStream.writeInt(p.getPositionsInDoc().size()); // term frequency
					currentPosition += Integer.BYTES;
					for (int pos : p.getPositionsInDoc()) {
						outStream.writeLong(pos); // positions in doc
						currentPosition += Long.BYTES;
					}
				}
			}
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public List<Integer> createVocabFile(String path, List<String> vocabulary) throws IOException {

		List<Integer> bytePositions = new ArrayList<Integer>();
		FileOutputStream outputStream = new FileOutputStream(path + "//vocab.bin");
		int currentPosition = 0;
		for (String term : vocabulary) {

			outputStream.write(term.getBytes());
			bytePositions.add(currentPosition);
			System.out.println(currentPosition);
			currentPosition += term.getBytes().length;
		}
		outputStream.close();

		return bytePositions;
	}

	public void createVocabTable() {
		// Need to be opened and written in binary mode
	}

}
