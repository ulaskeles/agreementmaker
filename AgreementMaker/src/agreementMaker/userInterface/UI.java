package agreementMaker.userInterface;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import agreementMaker.GSM;
import agreementMaker.application.Core;
import agreementMaker.application.evaluationEngine.OntologyController;
import agreementMaker.application.mappingEngine.AbstractMatcher;
import agreementMaker.application.mappingEngine.fakeMatchers.UserManualMatcher;
import agreementMaker.application.ontology.Ontology;
import agreementMaker.application.ontology.ontologyParser.TreeBuilder;
import agreementMaker.userInterface.vertex.VertexDescriptionPane;


/**
 * UI Class - 
 *
 * This class is responsible for creating the menu bar, displaying the canvas,  
 * the buttons, and checkboxes at botton of the screen.
 *
 * @author ADVIS Research Laboratory
 * @version 12/5/2004
 */
public class UI {
	
	static final long serialVersionUID = 1;
	
	private Canvas canvas;
	/** This class is going to be replaced later*/
	private OntologyController ontologyController;
	
	private JFrame frame;
	
	
	private JPanel panelCanvas, panelDesc;
	private MatchersControlPanel matcherControlPanel;
	private JScrollPane scrollPane;
	
	private JSplitPane splitPane;
	private UIMenu uiMenu;
	
	/**	 * Default constructor for UI class
	 */
	public UI()
	{
		init();
	}

	 
	/**
	 * @return canvas
	 */
	public Canvas getCanvas(){
		return this.canvas;
	}
	
	/**
	 * @return the ontologyController, a class containing some methods to work with canvas and ontologies
	 */
	public OntologyController getOntologyController(){
		return this.ontologyController;
	}
	
	/**
	 * @return
	 */
	public JPanel getCanvasPanel(){
		return this.panelCanvas;
	}
	/**
	 * @return
	 */
	public JPanel getDescriptionPanel(){
		return this.panelDesc;
	}

	
	public UIMenu getUIMenu(){
		return this.uiMenu;
	}
	/**
	 * @return
	 */
	public JFrame getUIFrame(){
		return this.frame;
	}
	/**
	 * @return
	 */
	public JSplitPane getUISplitPane(){
		return this.splitPane;
	}
	/**     
	 * Init method
	 * This function creates menu, canvas, and checkboxes to be displayed on the screen
	 */
	public void init()
	{
		//Setting the Look and Feel of the application to that of Windows
		//try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); }
		//catch (Exception e) { System.out.println(e); }

		//	Setting the Look and Feel of the application to that of Windows
		//try { javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
		//catch (Exception e) { System.out.println(e); }
		

		// Create a swing frame
		frame = new JFrame("Agreement Maker");
		frame.getContentPane().setLayout(new BorderLayout());	
		
		// Create the Menu Bar and Menu Items
		uiMenu = new UIMenu(this);	
		
		// create a new panel for the canvas 
		panelCanvas = new JPanel();
		
		// set the layout of the panel to be grid labyout of 1x1 grid
		panelCanvas.setLayout(new BorderLayout());
		
		// create a canvas class
		canvas = new Canvas(this);
		canvas.setFocusable(true);
		//canvas.setMinimumSize(new Dimension(0,0));
		//canvas.setPreferredSize(new Dimension(480,320));
		
		//add canvas to panel
		panelCanvas.add(canvas);
		//Added by Flavio: this class is needed to modularize the big canvas class, basically it contains some methods which could be in canvas class which works with the ontologies
		//it will be replaced later adding structures and methods inside the Core
	    ontologyController = new OntologyController(canvas);
		
	    //panelDesc = new VertexDescriptionPane(this); 
		//TODO: Add tabbed panes here for displaying the properties and descriptions		
		scrollPane = new JScrollPane(panelCanvas);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		//scrollPane.setPreferredSize(new Dimension((int)scrollPane.getSize().getHeight(), 5));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, null);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1.0);
		splitPane.setMinimumSize(new Dimension(640,480));
		splitPane.setPreferredSize(new Dimension(640,480));
		splitPane.getLeftComponent().setPreferredSize(new Dimension(640,480));
		// add scrollpane to the panel and add the panel to the frame's content pane
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		
		//panelControlPanel = new ControlPanel(this, uiMenu, canvas);
		matcherControlPanel = new MatchersControlPanel(this, uiMenu, canvas);
		frame.getContentPane().add(matcherControlPanel, BorderLayout.PAGE_END);		
		
		//Add the listener to close the frame.
		frame.addWindowListener(new WindowEventHandler());
		
		// set frame size (width = 1000 height = 700)
		//frame.setSize(900,600);
		frame.pack();
		frame.setExtendedState(Frame.MAXIMIZED_BOTH); // maximize the window
		
		
		// make sure the frame is visible
		frame.setVisible(true); 
	}

	/**
	 * @param jPanel
	 */
	public void setDescriptionPanel(JPanel jPanel){
		this.panelDesc = jPanel;
	}



	/** This function will open a file
	 *  Attention syntax and language are placed differently from other functions.
	 * @param ontoType the type of ontology, source or target
	 * 
	 * */

	public void openFile( String filename, int ontoType, int syntax, int language) {
		try{
			JPanel jPanel = null;
			
			if(language == GSM.RDFSFILE)//RDFS
				jPanel = new VertexDescriptionPane(GSM.RDFSFILE);//takes care of fields for XML files as well
			else if(language == GSM.ONTFILE)//OWL
				jPanel = new VertexDescriptionPane(GSM.ONTFILE);//takes care of fields for XML files as well
			else if(language == GSM.XMLFILE)//XML
				jPanel = new VertexDescriptionPane(GSM.XMLFILE);//takes care of fields for XML files as well 
	
			getUISplitPane().setRightComponent(jPanel);
			setDescriptionPanel(jPanel);
			
			//This function manage the whole process of loading, parsing the ontology and building data structures: Ontology to be set in the Core and Tree and to be set in the canvas
			TreeBuilder t = TreeBuilder.buildTreeBuilder(filename, ontoType, language, syntax);
			//Set ontology in the Core
			Ontology ont = t.getOntology();
			if(ontoType == GSM.SOURCENODE) {
				Core.getInstance().setSourceOntology(ont);
			}
			else Core.getInstance().setTargetOntology(ont);
			//Set the tree in the canvas
			getCanvas().setTree(t);
			if(Core.getInstance().ontologiesLoaded()) {
				//Ogni volta che ho caricato un ontologia e le ho entrambe, devo resettare o settare se � la prima volta, tutto lo schema dei matchings
				matcherControlPanel.resetMatchings();
			}
		}catch(Exception ex){
			JOptionPane.showConfirmDialog(null,"Can not parse the file '" + filename + "'. Please check the policy.","Parser Error",JOptionPane.PLAIN_MESSAGE);
			ex.printStackTrace();
		}
	}

	/**
	 * Class to close the frame and exit the application
	 */
	public class WindowEventHandler extends WindowAdapter
	{
		/**
		 * Function which closes the window
		 * @param e WindowEvent Object
		 */
		public void windowClosing(WindowEvent e)
	{
		e.getWindow().dispose();
		//System.exit(0);   
	}
	}
	
	public void redisplayCanvas() {
		canvas.repaint();
	}    

}
