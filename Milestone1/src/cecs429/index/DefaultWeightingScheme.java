package cecs429.index;

public class DefaultWeightingScheme implements IWeightingScheme {

	private String path;
	private DiskPositionalIndex dp;
	
	/*
	 * Empty constructor.
	 */
	public DefaultWeightingScheme() {
		super();
	}

	private DiskIndexWriter dw;

	/**
	 * Constructor to initialize the path where the index is stored and the disk positional index object.
	 * @param path
	 * @param dp
	 */
	public DefaultWeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
	}

	
	/**
	 * Constructor to initialize the path where the index is stored and the disk writer index object.
	 * @param path
	 * @param dp
	 */
	public DefaultWeightingScheme(String path, DiskIndexWriter dw) {
		this.path = path;
		this.dw = dw;
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		double wdt = (1 + Math.log(tfd));
		return wdt;
	}

	@Override
	public double getLd(Integer docID) {
		Double Ld = dp.getDocWeightD(docID);
		return Ld;
	}

	@Override
	public double getWqt(Integer N, Integer dft) {
		double wqt = Math.log(1 + (N / dft));
		return wqt;
	}

}
