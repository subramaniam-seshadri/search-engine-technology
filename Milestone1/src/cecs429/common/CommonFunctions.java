package cecs429.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cecs429.cluster.ClusterDoc;
import cecs429.index.IWeightingScheme;
import cecs429.index.Posting;

public class CommonFunctions {

	/**
	 * This method wraps all the necessary value required for calculation of similarity in a CLusterDoc object
	 * @param termFrequencyMap - The term-frequency in a document.
	 * @return - The cluster doc object which contains all the values relating to that document.
	 */
	public ClusterDoc getDocValues(HashMap<String, Integer> termFrequencyMap) {
		HashMap<String, Double> termWdtMap = new HashMap<String, Double>();
		List<Double> wdtList = new ArrayList<Double>();
		List<Integer> frequencyList = new ArrayList<Integer>();
		Long docLength = 0L;
		for (String key : termFrequencyMap.keySet()) {
			frequencyList.add(termFrequencyMap.get(key));
			docLength += termFrequencyMap.get(key);
			double wdt = (1 + Math.log(termFrequencyMap.get(key)));
			termWdtMap.put(key, wdt);
			wdtList.add(wdt * wdt);
		}
		double Ld = Math.sqrt(wdtList.stream().mapToDouble(Double::doubleValue).sum());
		int sumFrequency = frequencyList.stream().mapToInt(Integer::intValue).sum();
		double avgTfd = ((double) sumFrequency) / ((double) frequencyList.size());

		ClusterDoc cd = new ClusterDoc();
		cd.setAvgTfd(avgTfd);
		cd.setLd(Ld);
		cd.setTermWdtMap(termWdtMap);
		cd.setDocLength(docLength);

		return cd;
	}

	/**
	 * This method gets the wdt value from postings based on weighting scheme
	 * selected.
	 * 
	 * @param weightScheme - The IWeightingScheme object which can reflect any of
	 *                       the weighting classes that implement it.
	 * @param p            - Posting object.
	 * @return - 			 Returns the wdt object.
	 */
	public double getWdt(IWeightingScheme weightScheme, Posting p) {
		double wdt = 0.0;
		switch (weightScheme.getClass().getSimpleName()) {
		case "DefaultWeightingScheme": {
			wdt = p.getWdtDefaultValue();
			break;
		}
		case "TfIdfWeightingScheme": {
			wdt = p.getWdtTfIdfValue();
			break;
		}
		case "OkapiBM25WeightingScheme": {
			wdt = p.getWdtOkapiValue();
			break;
		}
		case "WackyWeightingScheme": {
			wdt = p.getWdtWackyValue();
			break;
		}
		}
		return wdt;
	}
}
