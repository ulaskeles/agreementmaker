package am.app.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import am.app.Core;
import am.app.feedback.CandidateConcept.ontology;
import am.app.feedback.measures.FamilialSimilarity;
import am.app.feedback.measures.RelevanceMeasure;
import am.app.feedback.measures.Specificity;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.AlignmentSet;
import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.ontology.Node;
import am.app.ontology.Ontology;

public class CandidateSelection {

	
	// relevance measures
	public enum MeasuresRegistry {
		FamilialSimilarity ( FamilialSimilarity.class ),
		//Specificity	( Specificity.class )
		;
		
		
		private String measure;
		
		MeasuresRegistry( Class<?> classname ) { measure = classname.getName(); }
		public String getMeasureClass() { return measure; }
		
	}
	
	
	
	ArrayList<RelevanceMeasure> measures;
	ArrayList<ConceptList> relevanceLists;
	FeedbackLoop fbL;
	
	
	public CandidateSelection(FeedbackLoop feedbackLoop) {
		fbL = feedbackLoop;
	}


	// create and run all the relevance measures
	public void runMeasures() {
	
		measures = new ArrayList<RelevanceMeasure>();

		EnumSet<MeasuresRegistry> mrs = EnumSet.allOf(MeasuresRegistry.class);
		
		Iterator<MeasuresRegistry> mi = mrs.iterator();
		
		while( mi.hasNext() ) {
			RelevanceMeasure m = getMeasureInstance( mi.next() );
			if( m != null ) {
				m.calculateRelevances();
				measures.add(m);
			}
		}
		
		return;
	}
	
	
	public AlignmentSet<Alignment> getCandidateAlignments( int k, int m ) {

		relevanceLists = new ArrayList<ConceptList>();
		
		// get the ConceptList from each relevance measure
		Iterator<RelevanceMeasure> m1 = measures.iterator();
		while( m1.hasNext() ) {
			relevanceLists.add( m1.next().getRelevances() );
		}

		double totalSpread = 0.00d;
		
		// calculate the total spread
		Iterator<ConceptList> itrRL = relevanceLists.iterator();
		while( itrRL.hasNext() ) {
			totalSpread += itrRL.next().getSpread();
		}
		
		// set the weights
		itrRL = relevanceLists.iterator();
		while( itrRL.hasNext() ) {
			itrRL.next().setWeight(totalSpread);
		}
		
		
		
		// now, do a linear combination of all the measures for each element in the ontologies
		
		Ontology s = Core.getInstance().getSourceOntology();
		Ontology t = Core.getInstance().getSourceOntology();
		
		ArrayList<CandidateConcept> masterList = new ArrayList<CandidateConcept>();
		
		ArrayList<Node> list = s.getClassesList();
		masterList.addAll( getCombinedRelevances( list, CandidateConcept.ontology.source, alignType.aligningClasses ) );

		list = s.getPropertiesList();
		masterList.addAll( getCombinedRelevances(list, CandidateConcept.ontology.source, alignType.aligningProperties));
		
		list = t.getClassesList();
		masterList.addAll( getCombinedRelevances( list, CandidateConcept.ontology.target, alignType.aligningClasses ) );
		
		list = t.getPropertiesList();
		masterList.addAll( getCombinedRelevances(list, CandidateConcept.ontology.target, alignType.aligningProperties) );
		
		
		
		// we now have the masterList, sort it.
		Collections.sort( masterList );
		
		ArrayList<CandidateConcept> topK = new ArrayList<CandidateConcept>();
		if(masterList.size() < k){
			topK.addAll( masterList);
		}
		else{
			topK.addAll( masterList.subList(0, k) );
		}
		
		
		Collections.sort(topK);
		
		
		
		// we have the topK, now convert the concepts to mappings
		
		AlignmentSet<Alignment> topMappings = new AlignmentSet<Alignment>();
		
		
		Iterator<CandidateConcept> itr1 = topK.iterator();
		while( itr1.hasNext() ) {
			CandidateConcept top1 = itr1.next();
			
			Alignment[] topM = null;
			
			if( top1.isType( alignType.aligningClasses ) ) {
				// we're looking in the classes matrix
				if( top1.isOntology(  CandidateConcept.ontology.source ) ) {
					// source concept
					topM = fbL.getClassesMatrix().getRowMaxValues( top1.getIndex(), m);
				} 
				else {
					// target concept
					topM = fbL.getClassesMatrix().getColMaxValues( top1.getIndex(), m);
				}
				
				if( topM != null ) {
					for( int i1 = 0; i1 < topM.length; i1++ ) {
						if( topM[i1] != null ) topM[i1].setAlignmentType( alignType.aligningClasses );
					}
				}
				
			} 
				
				
				
				
				
			else {
				// we're looking in the properties matrix
				if( top1.isOntology( CandidateConcept.ontology.source ) ) {
					// source concept
					topM = fbL.getPropertiesMatrix().getRowMaxValues( top1.getIndex(), m);
				} 
				else {
					// target concept
					topM = fbL.getPropertiesMatrix().getColMaxValues( top1.getIndex(), m);
				}
				
				if( topM != null ) {
					for( int i1 = 0; i1 < topM.length; i1++ ) {
						if( topM[i1] != null ) topM[i1].setAlignmentType( alignType.aligningProperties );
					}
				}

				
			};
			
			if( topM != null ) {
				for( int i1 = 0; i1 < m; i1++ ) {
					if( topM[i1] != null && topM[i1].getSimilarity() != -1 )	topMappings.addAlignment( topM[i1]);
				}
			};
			
			
		}; 
		
		return topMappings;
		
	}

	
	
	private ArrayList<CandidateConcept> getCombinedRelevances(ArrayList<Node> list, ontology source, alignType type) {
	
		ArrayList<CandidateConcept> subList = new ArrayList<CandidateConcept>();
		Iterator<Node> nodeItr = list.iterator();
		
		double combinedRelevance = 0.00d;
		
		while( nodeItr.hasNext() ) {
			Node currentNode = nodeItr.next();
			Iterator<ConceptList> cl = relevanceLists.iterator();
			while( cl.hasNext() ) {
				ConceptList currentList = cl.next();
				combinedRelevance += currentList.getWeight() * currentList.getRelevance( currentNode, source, type );
			}
			
			if( combinedRelevance > 0.00d ) {
				subList.add( new CandidateConcept( currentNode, combinedRelevance, source, type));
			}
		}
		
		
		
		return subList;
		
		
	}


	public AlignmentSet<ExtendedAlignment> getCurrentAlignments() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
	
	// get a new measure instance given the MeasuresRegistry
	private RelevanceMeasure getMeasureInstance(MeasuresRegistry name ) {
		
		Class<?> measureClass = null;
		try {
			measureClass = Class.forName( name.getMeasureClass() );
		} catch (ClassNotFoundException e) {
			System.out.println("DEVELOPER: You have entered a wrong class name in the MeasuresRegistry");
			e.printStackTrace();
			return null;
		}
		
		RelevanceMeasure a = null;
		try {
			a = (RelevanceMeasure) measureClass.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		a.setName(name);
		a.setFeedbackLoop(fbL);
		return a;
	}
	
}
