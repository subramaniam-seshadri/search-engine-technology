package cecs429.index;

public class OkapiBM25WeightingScheme implements IWeightingScheme {
	
	private Integer docLength; 
	private double avgDocLength;
	
	public OkapiBM25WeightingScheme() {
		
	}

	@Override
	public double getWd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getLd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getWqt(Integer N, Integer dft) {
		// TODO Auto-generated method stub
		return 0;
	}

}
