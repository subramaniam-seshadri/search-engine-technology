package cecs429.index;

public class TfIdfWeightingScheme implements IWeightingScheme {

	private String path;
	private DiskIndexWriter dw;
	public TfIdfWeightingScheme() {
		super();
	}

	private DiskPositionalIndex dp;
	
	/**
	 * Constructor when reading values from disk.
	 * @param path - Path of index on disk.
	 * @param dp - DiskPositionalIndex instance.
	 */
	public TfIdfWeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
	}
	
	/**
	 * Constructor when writing values to disk.
	 * @param path - Path of index on disk.
	 * @param dw - DiskWriterIndex instance.
	 */
	public TfIdfWeightingScheme(String path, DiskIndexWriter dw) {
		this.path = path;
		this.dw = dw;
	}
	
	@Override
	public double getWqt(Integer N, Integer dft) {
		
		return Math.log(N.doubleValue()/dft.doubleValue());
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		
		return tfd;
	}

	@Override
	public double getLd(Integer docID) {
		Double Ld = dp.getDocWeightD(docID);
		return Ld;
	}

}
