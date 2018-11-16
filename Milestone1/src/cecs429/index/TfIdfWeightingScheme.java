package cecs429.index;

public class TfIdfWeightingScheme implements IWeightingScheme {

	private String path;
	private DiskPositionalIndex dp;
	
	public TfIdfWeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
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
		Double Ld = dp.readLdFromDisk(docID, path);
		return Ld;
	}

}
