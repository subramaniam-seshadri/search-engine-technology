  package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class DiskPositionalIndex1 implements Index{

	@Override
	public List<Posting> getPostings(String term) {
		try {
			FileInputStream inputStream = new FileInputStream("src//index" + "//vocabTable.bin");
			DataInputStream inStream = new DataInputStream(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> getVocabulary() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
