package am.evaluation.repairExtended;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;

import am.app.mappingEngine.Mapping;
import am.app.mappingEngine.referenceAlignment.MatchingPair;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentMatcher;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentParameters;
import am.app.mappingEngine.referenceAlignment.ReferenceEvaluationData;
import am.app.mappingEngine.referenceAlignment.ReferenceEvaluator;
import am.evaluation.repair.AlignmentRepairUtilities;
import am.utility.RunTimer;
import am.utility.referenceAlignment.AlignmentUtilities;
import am.utility.referenceAlignment.MappingsOutput;

/**
 * @author Pavan
 *
 *	Repair alignment class is used to repair alignments after matching. The class merges the 
 */
public class RepairAlignment {

	private static Logger log = Logger.getLogger(RepairAlignment.class);

	//INPUT
	private File referenceFile = new File("/home/pavan/MS/WebSemantics/Ontologies/Anatomy/reference.rdf");
	private File sourceOwl = new File("/home/pavan/MS/WebSemantics/Ontologies/Anatomy/mouse.owl");
	private File targetOwl = new File("/home/pavan/MS/WebSemantics/Ontologies/Anatomy/human.owl");
	private File alignmentFile = new File("/home/pavan/MS/WebSemantics/Ontologies/Anatomy/human-mouse-best.rdf");
	private File repairedAlignmentFile = new File("/home/pavan/MS/WebSemantics/Ontologies/Anatomy/repairedAlignment.rdf");
	private Boolean verbose = false;
	//INPUT END
	
	private AlignmentRepairUtilities util = new AlignmentRepairUtilities(log);
	
	private List<MatchingPair> alignment;
	private List<MatchingPair> refAlignment;
	private List<MatchingPair> repariedAlignment;
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		log.setLevel(Level.INFO);
		new RepairAlignment().repair();
	}
	
	public void repair(){
		
		Report report = new Report();
		OWLOntology mergedOntology;
		ConflictSetList inconsistentSets;
		ArrayList<OWLAxiom> minHittingSet = new ArrayList<OWLAxiom>();
		
		try {			
			
			log.info("Merging ontolgoies...");
			mergedOntology = mergeOntologies();
			
			RunTimer timer = new RunTimer();
			timer.start();
						
			log.info("Identifying inconsistent classes and corresponding equivalence axioms...");
			inconsistentSets = getInconsistentSets(alignment, mergedOntology);
			
			inconsistentSets.setMergedOntology(mergedOntology);
			
			log.info(inconsistentSets.getAxiomCount() + " inconsistent axioms identified (" + inconsistentSets.getDistinctAxiomCount() + ") unique axioms");
			
			log.info("Computing Minimal unsatisfiable Preserving Sub-tboxes (MUPS)...");
			ConflictSetList mups = inconsistentSets.computeMUPS();
			if(verbose)
				report.printConflictSetList(mups);
			
			log.info("Ranking inconsistent axioms by frequency...");
			mups.rankAxioms();
			if(verbose)
				report.printAxiomRanks(mups);

			log.info("Compute hitting set...");
			minHittingSet = inconsistentSets.computeHittingSet(mups);
			if(verbose)
				report.printMUPS(minHittingSet);
			
			inconsistentSets.setSourceOntology(sourceOwl);
			inconsistentSets.setTargetOntology(targetOwl);
			
			log.info("Repairing inconsistent mappings...");
			inconsistentSets.repairMappings(minHittingSet,sourceOwl.toString(),targetOwl.toString());
			
			log.info("Generating repaired alignment file...");
			createRepairedAlignment(inconsistentSets.getAddMappings(),inconsistentSets.getRemoveMappings());
			
			log.info("******************Evaluation Report*****************");
			
			if(verbose){			
				//report.getIncorrectMappings(alignmentFile, referenceFile);			
				//report.mergePrecision(inconsistentSets.getAllAxioms());
				//report.hittingSetPrecison(minHittingSet);
			}
			
			report.initialMeasure(alignmentFile.toString(), referenceFile.toString());
			report.finalMeasure(repairedAlignmentFile.toString(),referenceFile.toString());
			log.info( "Total run time: " + timer.stop().getFormattedRunTime() );
			
		} catch (OWLOntologyCreationException e) {
			log.error("OWL ontology merge failed");
			e.printStackTrace();
		} 
		catch (Exception e) {
			log.error("Repair process failed");
			e.printStackTrace();
		}
		finally{
			log.info("Process complete");
		}		
	}
	
	private void createRepairedAlignment(ArrayList<MatchingPair> addMappings, ArrayList<MatchingPair> removeMappings) {
		 
		repariedAlignment = alignment;
		
		//log.info(alignment.size());
		//log.info(removeMappings.size());
		//log.info(addMappings.size());				
		
		for(MatchingPair p : removeMappings){
			repariedAlignment.remove(getMatchingPair(p));
		}		
		
		repariedAlignment.addAll(addMappings);
	    
	    log.info("Repaired alignment");
	    MappingsOutput.writeMappingsOnDisk(repairedAlignmentFile.toString(), repariedAlignment);		
	}

	private MatchingPair getMatchingPair(MatchingPair p) {
		
		MatchingPair pair = null;
				
		for(MatchingPair mp : alignment){
			
			//log.info(mp);
			if(mp.sameSource(p) && mp.sameTarget(p))
				pair = mp;
		}
		//log.info(p);
		return pair;
	}

	public OWLOntology mergeOntologies()
			throws OWLOntologyCreationException{
		
		alignment = AlignmentUtilities.getMatchingPairsOAEI(alignmentFile.getAbsolutePath());
		log.info("Loaded alignment. The alignment contains " + alignment.size() + " mappings.");
		
		return util.loadOntologies(alignment);
	}
	
	/*public List<OWLAxiom> getInconsistentAxioms(List<MatchingPair> alignment,OWLOntology mergedOntology){
		
		Node<OWLClass> unsatisfiedClasses;
		List<OWLAxiom> inconsistentAxioms = new ArrayList<OWLAxiom>();
		
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		Reasoner reasoner = util.loadReasoner(mergedOntology);		
				
		unsatisfiedClasses = reasoner.getUnsatisfiableClasses();
		
		for(OWLClass cls : unsatisfiedClasses){		

		if(! cls.isBottomEntity()){
			Iterator<MatchingPair> mpIter = alignment.iterator();
					
			while(mpIter.hasNext()){
				MatchingPair pair = mpIter.next();
				if(pair.sourceURI.equals(cls.getIRI().toString()) || pair.targetURI.equals(cls.getIRI().toString())){
					mpIter.remove();
					
					log.info("The conflicting class: " + cls);
					inconsistentAxioms.addAll((ArrayList<OWLAxiom>)getConflictingAxioms(mergedOntology, reasonerFactory, reasoner, cls));		
					}
				}
			}
		}
		
		log.info(unsatisfiedClasses.getSize() + " Inconsistent classes identified");
		
		return inconsistentAxioms;		
	}*/
	
	public ConflictSetList getInconsistentSets(List<MatchingPair> alignment,OWLOntology mergedOntology){
		
		Node<OWLClass> unsatisfiedClasses;
		//HashMap<OWLClass,ArrayList<OWLAxiom>> axiomHashMap = new HashMap<OWLClass,ArrayList<OWLAxiom>>();
		ConflictSetList inconsistentSets = new ConflictSetList();
	
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		Reasoner reasoner = util.loadReasoner(mergedOntology);		
				
		unsatisfiedClasses = reasoner.getUnsatisfiableClasses();
		
		for(OWLClass cls : unsatisfiedClasses){		

			if(! cls.isBottomEntity()){
				Iterator<MatchingPair> mpIter = alignment.iterator();
					
				while(mpIter.hasNext()){
					MatchingPair pair = mpIter.next();
					if(pair.sourceURI.equals(cls.getIRI().toString()) || pair.targetURI.equals(cls.getIRI().toString())){
						//mpIter.remove();

						//log.info("The conflicting class: " + cls);
						//axiomHashMap.put(cls, (ArrayList<OWLAxiom>)getConflictingAxioms(mergedOntology, reasonerFactory, reasoner, cls));
						inconsistentSets.addDistinct(new ConflictSet(cls,getConflictingAxioms(mergedOntology, reasonerFactory, reasoner, cls),inconsistentSets.getClassCount() + 1));
					
					}
				}
			}
		}		
		//log.info(unsatisfiedClasses.getSize() + " Inconsistent classes identified");
		
		return inconsistentSets;		
	}
	
	private ArrayList<AxiomRank> getConflictingAxioms(OWLOntology mergedOntology,
			ReasonerFactory reasonerFactory, Reasoner reasoner,
			OWLClass unsatClass) {
		//*** Get an explanation for each of the unsatisfiable classes.
		//*** Save all the conflicting EquivalentClass axioms.
		BlackBoxExplanation exp = new BlackBoxExplanation(mergedOntology, reasonerFactory, reasoner);
		Set<OWLAxiom> expSet = exp.getExplanation(unsatClass);

		//List<OWLAxiom> conflictingAxioms = new ArrayList<OWLAxiom>();
		ArrayList<AxiomRank> conflictingAxioms = new ArrayList<AxiomRank>();

		//System.out.println("\n------------------------------ Unsatisfiable Class ------------------------------");
		//log.info("The Unsatisfiable class is: " + unsatClass);

		for(OWLAxiom causingAxiom : expSet) {
								
//			log.info(causingAxiom.getAxiomType());
			if(causingAxiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES) {
				//Get the Source and Target classes in the axiom signature.
				List<OWLClass> classList = new ArrayList<OWLClass>(causingAxiom.getClassesInSignature());
				String[] sourceURI = classList.get(0).getIRI().toString().split("#");
				String[] targetURI = classList.get(1).getIRI().toString().split("#");

				//Save the conflicting axiom (if its not a mapping between classes in same ontology).
				if(!sourceURI.equals(targetURI))
					conflictingAxioms.add(new AxiomRank(causingAxiom,0));
					//conflictingAxioms.add(new AxiomRank(causingAxiom,0,conflictingAxioms.size() + 1));
					//conflictingAxioms.add(causingAxiom);
			}
			else if(causingAxiom.getAxiomType() == AxiomType.EQUIVALENT_DATA_PROPERTIES) {
				//Get the Source and Target classes in the axiom signature.
				List<OWLClass> classList = new ArrayList<OWLClass>(causingAxiom.getClassesInSignature());
				String[] sourceURI = classList.get(0).getIRI().toString().split("#");
				String[] targetURI = classList.get(1).getIRI().toString().split("#");

				//Save the conflicting axiom (if its not a mapping between classes in same ontology).
				if(!sourceURI.equals(targetURI))
					conflictingAxioms.add(new AxiomRank(causingAxiom,0));
					//conflictingAxioms.add(new AxiomRank(causingAxiom,0,conflictingAxioms.size() + 1));
					//conflictingAxioms.add(causingAxiom);
			}
			else if(causingAxiom.getAxiomType() == AxiomType.EQUIVALENT_OBJECT_PROPERTIES) {
				//Get the Source and Target classes in the axiom signature.
				List<OWLClass> classList = new ArrayList<OWLClass>(causingAxiom.getClassesInSignature());
				String[] sourceURI = classList.get(0).getIRI().toString().split("#");
				String[] targetURI = classList.get(1).getIRI().toString().split("#");

				//Save the conflicting axiom (if its not a mapping between classes in same ontology).
				if(!sourceURI.equals(targetURI))
					conflictingAxioms.add(new AxiomRank(causingAxiom,0));
					//conflictingAxioms.add(new AxiomRank(causingAxiom,0,conflictingAxioms.size() + 1));
					//conflictingAxioms.add(causingAxiom);
			}
		}
				
		/*for(AxiomRank confAx : conflictingAxioms){
			log.info("The conflicting axiom: " + confAx.getAxiom());
			//axiomCount++;
		}*/
						
		return conflictingAxioms;
	}	

	
}