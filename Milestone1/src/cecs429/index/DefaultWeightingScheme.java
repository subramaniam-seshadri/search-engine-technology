package cecs429.index;

public class DefaultWeightingScheme implements IWeightingScheme{

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
		double wqt = Math.log(1 + (N/dft));
		return wqt;
	}

}
