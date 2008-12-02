package agreementMaker.application.mappingEngine.qualityCombination;

import java.util.ArrayList;

import agreementMaker.application.mappingEngine.AbstractParameters;

public class QualityCombinationParameters extends AbstractParameters {

	final static String MAXCOMB = "Max similarity";
	final static String MINCOMB = "Min similarity";
	final static String AVERAGECOMB = "Average of similarities";
	final static String WEIGHTAVERAGE = "Weighted Average of similarities";
	
	public String combinationType;
	public double[] weights; 
		

}
