package cecs429.index;

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
		//Double denominator = (1+
		return 0;
	}

	@Override
	public double getLd(Integer docID) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
