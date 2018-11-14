package cecs429.index;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TfIdfWeightingScheme implements IWeightingScheme {

	private String path;
	
	public TfIdfWeightingScheme(String path) {
		this.path = path;
	}
	
	@Override
	public double getWqt(Integer N, Integer dft) {
		
		return Math.log(N/dft);
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		
		return tfd;
	}

	@Override
	public double getLd(Integer docID) {
		FileInputStream inputStream = null;
		DataInputStream inStream = null;
		Double Ld = 0.0;
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
							Ld = inStream.readDouble();
						}
					} catch (EOFException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ld in tfIdf weighting scheme:" + Ld);
		return Ld;
	}

}
