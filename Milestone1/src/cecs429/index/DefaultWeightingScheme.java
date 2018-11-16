package cecs429.index;

public class DefaultWeightingScheme implements IWeightingScheme {

	private String path;
	private DiskPositionalIndex dp;

	public DefaultWeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		double wdt = (1 + Math.log(tfd));
		return wdt;
	}

	@Override
	public double getLd(Integer docID) {
		Double Ld = dp.readLdFromDisk(docID, path);
		return Ld;
	}

	@Override
	public double getWqt(Integer N, Integer dft) {
		double wqt = Math.log(1 + (N / dft));
		return wqt;
	}

}
