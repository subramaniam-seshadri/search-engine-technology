package cecs429.index;

import java.io.IOException;

public class DefaultWeightingScheme implements IWeightingScheme {

	private String path;
	
	public DefaultWeightingScheme(String path) {
		this.path = path;
	}
	
	@Override
	public double getWdt(Integer tfd, Integer docID) {
		double wdt = (1 + Math.log(tfd));
		return wdt;
	}

	@Override
	public double getLd(Integer docID) {
		DiskPositionalIndex dp = null;
		try {
			dp = new DiskPositionalIndex(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double Ld = dp.readLdFromDisk(docID);
		return Ld;
			
		/*FileInputStream inputStream = null;
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
				}else {
					indexIntoFile += 1;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ld in Default weighting scheme:" + Ld);
		return Ld;*/
	}

	@Override
	public double getWqt(Integer N, Integer dft) {
		double wqt = Math.log(1 + (N / dft));
		return wqt;
	}

}
