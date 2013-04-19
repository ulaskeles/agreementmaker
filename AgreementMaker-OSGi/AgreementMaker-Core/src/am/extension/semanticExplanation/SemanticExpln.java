package am.extension.semanticExplanation;

import java.util.List;

import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.Mapping;

public class SemanticExpln {
	

	static int rowSize;
	static int colSize;
	private ExplanationNode[][] classExplanationMatrix;
	private ExplanationNode[][] propertiesExplanationMatrix;

	private static SemanticExpln instance = null;

	
	private void SemanticExpln(){

	}
	 
	  public static SemanticExpln getInstance(){
		    if(instance==null){
		       instance = new SemanticExpln();
		      }
		      return instance;
	  }
	  
  
	/**
	 *  Finds the least significant path for the whole ontology. 
	 * @param nodeMatrix
	 * @param alignmentMappings
	 * @return
	 */
	public static ExplanationNode findUniversalPaths( Alignment<Mapping> alignmentMappings){
		ExplanationNode[][] nodeMatrix = null;
		ExplanationNode returnStructure = null;
		for(int i =0;i<rowSize;i++){
			for(int j=0;j<colSize;j++) {
				if(SemanticExpln.getInstance().getClassExplanationMatrix()[i][j] != null) {
					returnStructure = SemanticExpln.getInstance().getClassExplanationMatrix()[i][j].deepCopyStructure();
					break;
				}
			}
		}
		returnStructure.setUniversalUse(true);
		
		for(Mapping m:alignmentMappings){

			if(m.getEntity1().isClass() && m.getEntity2().isClass()) {
    			nodeMatrix =  SemanticExpln.getInstance().getClassExplanationMatrix();
    		} else if(m.getEntity1().isProp() && m.getEntity2().isProp()) {
    			nodeMatrix =  SemanticExpln.getInstance().getPropertiesExplanationMatrix();
    		}
			
			ExplanationNode explanationNode = nodeMatrix[m.getEntity1().getIndex()][m.getEntity2().getIndex()];
			
			List<ExplanationNode> leastSP = ExplanationNode.findLeastSignificantPath(explanationNode);
			for(ExplanationNode node:leastSP){
				returnStructure.addCountIntelligently(node, true);
			}
			
			List<ExplanationNode> mostSP = ExplanationNode.findMostSignificantPath(explanationNode);
			for(ExplanationNode node:mostSP){
				returnStructure.addCountIntelligently(node, false);
			}
		}
		
//			returnStructure.describeExplanation();
		return returnStructure;
	}
		
	/**
	 *  Finds the Most significant path for the whole ontology. 
	 * @param nodeMatrix
	 * @param alignmentMappings
	 * @return
	 */
	public static ExplanationNode findUniversalMostSignificantPath(Alignment<Mapping> alignmentMappings){
		ExplanationNode[][] nodeMatrix = null;

		ExplanationNode returnStructure = SemanticExpln.getInstance().getClassExplanationMatrix()[1][1].deepCopyStructure();

		for(Mapping m:alignmentMappings){
			if(m.getEntity1().isClass() && m.getEntity2().isClass()) {
    			nodeMatrix =  SemanticExpln.getInstance().getClassExplanationMatrix();
    		} else if(m.getEntity1().isProp() && m.getEntity2().isProp()) {
    			nodeMatrix =  SemanticExpln.getInstance().getPropertiesExplanationMatrix();
    		}
			
			ExplanationNode explanationNode = nodeMatrix[m.getEntity1().getIndex()][m.getEntity2().getIndex()];
			List<ExplanationNode> significantPathForNode = ExplanationNode.findMostSignificantPath(explanationNode);

			for(ExplanationNode node:significantPathForNode){
				returnStructure.addCountIntelligently(node, false);
			}
		}
		
//		returnStructure.describeExplanation();
		return returnStructure;
	}


	public ExplanationNode[][] getClassExplanationMatrix() {
		return this.classExplanationMatrix;
	}


	public void setClassExplanationMatrix(int row, int col) {
		this.classExplanationMatrix = new ExplanationNode[row][col];
		this.rowSize = row;
		this.colSize = col;
	}

	public ExplanationNode[][] getPropertiesExplanationMatrix() {
		return propertiesExplanationMatrix;
	}

	public void setPropertiesExplanationMatrix(int row, int col) {
		this.propertiesExplanationMatrix = new ExplanationNode[row][col];
		this.rowSize = row;
		this.colSize = col;
	}

	
}