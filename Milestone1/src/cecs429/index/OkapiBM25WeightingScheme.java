package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OkapiBM25WeightingScheme implements IWeightingScheme {

	private String path;
	private DiskPositionalIndex dp;

	public OkapiBM25WeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		Double numerator = (2.2 * tfd);
		Long docLengthD = 0L;
		Double docLengthA = 0.0;
			docLengthD = getDocLengthD(docID);
			docLengthA = getDocLengthA();
		
		Double denominator = (1.2 * (0.25 + (0.75 * (docLengthD.doubleValue() / docLengthA))))+ tfd;
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

	public Long getDocLengthD(Integer docID) {
		Long docLengthd = dp.readDocLengthDFromDisk(docID, path);
		return docLengthd;
	}

	public Double getDocLengthA() {
	
		Double docLengthA = dp.readDocLengthAFromDisk(path);
		
		return docLengthA;
	}
}
