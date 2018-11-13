package cecs429.index;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OkapiBM25WeightingScheme implements IWeightingScheme {

	private String path;

	public OkapiBM25WeightingScheme(String path) {
		this.path = path;
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		Double numerator = (2.2 * tfd);
		getDocLength(docID);
		Double denominator = (1.2 * (0.25 + (0.75) + tfd));
		return (numerator / denominator);
	}

	@Override
	public double getLd(Integer docID) {

		return 1.0;
	}

	@Override
	public double getWqt(Integer N, Integer dft) {
		Double value1 = Math.log(((N - dft) + 0.5) / (dft + 0.5));
		Double value2 = 0.1;
		return Math.max(value1, value2);
	}

	public Long getDocLength(Integer docID) {
		FileInputStream inputStream = null;
		DataInputStream inStream = null;
		Long docLengthd = 0L;
		try {
			// read file
			inputStream = new FileInputStream(path + "//docWeights.bin");
			inStream = new DataInputStream(inputStream);
			Integer indexIntoFile = 0;
			while (inStream.available() != 0) {
				if (indexIntoFile == docID) {
					try {
						inStream.skip(32 * indexIntoFile);
						if (inStream.available() != 0) {
							inStream.skip(8);
							docLengthd = inStream.readLong();
						}
					} catch (EOFException e) {
						e.printStackTrace();
					}
				} else {
					indexIntoFile += 1;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return docLengthd;
	}

}
