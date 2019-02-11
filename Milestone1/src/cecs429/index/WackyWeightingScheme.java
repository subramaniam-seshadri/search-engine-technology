package cecs429.index;

public class WackyWeightingScheme implements IWeightingScheme{

	
	private String path;
	private DiskPositionalIndex dp;
	private DiskIndexWriter dw;
	private ClusterIndexDiskWriter cw;
	
	/**
	 * Constructor when writing values to disk.
	 * @param path - Path of index on disk.
	 * @param dw - DiskWriterIndex instance.
	 */
	public WackyWeightingScheme(String path,DiskIndexWriter dw) {
		this.path = path;
		this.dw = dw;
	}
	
	public WackyWeightingScheme(String path, ClusterIndexDiskWriter cw) {
		this.path = path;
		this.cw = cw;
	}
	
	/**
	 * Constructor when reading values from disk.
	 * @param path - Path of index on disk.
	 * @param dp - DiskPositionalIndex instance.
	 */
	public WackyWeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
	}
	
	@Override
	public double getWqt(Integer N, Integer dft) {
		
		return Math.max(0.0, Math.log( (N.doubleValue()-dft.doubleValue())/dft.doubleValue() ) );
	}

	@Override
	public double getWdt(Integer tfd, Integer docID) {
		Double numerator = (1 + Math.log(tfd));
		Double denominator = (1 +  Math.log(getAvgTfd(docID)));
		return (numerator/denominator);
	}

	@Override
	public double getLd(Integer docID) {
		
		return Math.sqrt(dp.getByteSizeForDocs(docID, path).doubleValue());
	}

	/**
	 * Read average term frequency value for a document.
	 * @param docID - Document ID
	 * @return - Average term frequency value for a given document. 
	 */
	public Double getAvgTfd(Integer docID) {
		Double avgTfd = 0.0;
		if(dw == null)
			avgTfd = cw.getAvgTdfD(docID);
		else
			avgTfd = dw.getAvgTdfD(docID);
		return avgTfd;
		
	}
}
