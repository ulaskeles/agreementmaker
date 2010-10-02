package am.app.mappingEngine.oaei2010;

import am.app.mappingEngine.AbstractParameters;

public class OAEI2010MatcherParameters extends AbstractParameters {

	// values for running different combination
	public boolean usingASM;
	public boolean usingPSM;
	public boolean usingVMM;
	public boolean usingLWC1;
	public boolean usingGFM;
	public boolean usingFCM;
	public boolean usingLCM;
	public boolean usingLWC2;

	
	public static enum Track {
		Anatomy,
		Benchmarks,
		Conference,
		AllMatchers;
	}
	
	
	OAEI2010MatcherParameters( Track whichTrack ) { 
		super(); 
		initBooleansForOAEI2010(whichTrack); 
	}
	
	/** *********************************** SUPPORT METHODS *********************************************/
	void initBooleansForOAEI2010( Track whichTrack ){
		
		switch( whichTrack ) {
		case Anatomy:
		case Benchmarks:
		case Conference:
		case AllMatchers:
		default:
			usingASM = true;
			usingPSM = true;
			usingVMM = true;
			usingLWC1 = true;
			usingGFM = true;
			usingFCM = true;
			usingLCM = true;
			usingLWC2 = true;
		
		}
		
	}
	
}