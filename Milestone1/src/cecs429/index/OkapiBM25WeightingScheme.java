package cecs429.index;

public class OkapiBM25WeightingScheme implements IWeightingScheme {

	private String path;
	private DiskPositionalIndex dp;
	private DiskIndexWriter dw;
	private ClusterIndexDiskWriter cw;

	public OkapiBM25WeightingScheme(String path,DiskIndexWriter dw) {
		this.path = path;
		this.dw = dw;
	}
	
	public OkapiBM25WeightingScheme(String path,ClusterIndexDiskWriter cw) {
		this.path = path;
		this.cw = cw;
	}
	
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
		
		Double denominator = (1.2 * (0.25 + (0.75 * (docLengthD.doubleValue() / docLengthA.doubleValue()))))+ tfd.doubleValue();
		return (numerator / denominator);
	}

	@Override
	public double getLd(Integer docID) {

		return 1.0;
	}

	@Override
	public double getWqt(Integer N, Integer dft) {
		Double value1 = Math.log(((N.doubleValue() - dft.doubleValue()) + 0.5) / (dft + 0.5));
		Double value2 = 0.1;
		return Math.max(value1, value2);
	}

	public Long getDocLengthD(Integer docID) {
		Long docLengthd = 0L;
		if(dw == null)
			docLengthd = cw.getDocLengthD(docID);
		else
			docLengthd = dw.getDocLengthD(docID);
		return docLengthd;
	}
	
	public Double getDocLengthA() {
		Double docLengthA = 0.0;
		if(dw == null)
			docLengthA = cw.getDocLengthA();
		else
			docLengthA = dw.getDocLengthA();
		return docLengthA;
	}
}
