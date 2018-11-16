package cecs429.index;

public class WackyWeightingScheme implements IWeightingScheme{

	
	private String path;
	private DiskPositionalIndex dp;
	
	public WackyWeightingScheme(String path, DiskPositionalIndex dp) {
		this.path = path;
		this.dp = dp;
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
		
		return Math.sqrt(dp.getByteSizeForDocs(docID, path));
	}

	public Double getAvgTfd(Integer docID) {
		Double avgTfd = dp.getAvgTfdFromDisk(docID, path);		
		return avgTfd;
		
	}
}
