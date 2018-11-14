package cecs429.index;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WackyWeightingScheme implements IWeightingScheme{

	
	private String path;
	
	public WackyWeightingScheme(String path) {
		this.path = path;
	}
	
	@Override
	public double getWqt(Integer N, Integer dft) {
		
		return Math.max(0.0, Math.log((N-dft)/dft));
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		Double numerator = (1 + Math.log(tfd));
		Double denominator = (1 +  Math.log(getAvgTfd(docID)));
		return (numerator/denominator);
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
							inStream.skip(16);
							Ld = Math.sqrt(inStream.readLong());
						}
					} catch (EOFException e) {
						e.printStackTrace();
					}
				}else {
					indexIntoFile += 1;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ld in Wacky weighting scheme:" + Ld);
		return Ld;
	}

	public Double getAvgTfd(Integer docID) {
		FileInputStream inputStream = null;
		DataInputStream inStream = null;
		Double avgTfd = 0.0;
		try {
			// read file
			inputStream = new FileInputStream(path + "//docAvgWeight.bin");
			inStream = new DataInputStream(inputStream);
			Integer indexIntoFile = 0;
			while (inStream.available() != 0) {
				if (indexIntoFile == docID) {
					try {
						inStream.skip(32 * indexIntoFile);
						if (inStream.available() != 0) {
							avgTfd = inStream.readDouble();
						}
					} catch (EOFException e) {
						e.printStackTrace();
					}
				}else {
					indexIntoFile += 1;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Avg tfd in Wacky weighting scheme:" + avgTfd);
		return avgTfd;
		
	}
}
