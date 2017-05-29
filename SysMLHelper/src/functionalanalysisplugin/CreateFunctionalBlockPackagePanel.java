package functionalanalysisplugin;

import functionalanalysisplugin.PopulateFunctionalAnalysisPkg.SimulationType;
import generalhelpers.CreateGatewayProjectPanel;
import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateFunctionalBlockPackagePanel extends CreateStructuralElementPanel {

	final private String theBlankName = "<Put Name Here>";
	private IRPPackage m_RootPackage;
	private IRPPackage m_RequirementsAnalysisPkg;
	private List<ActorMappingInfo> m_ActorChoices;
	private RhapsodyComboBox m_BlockInheritanceChoice;
	private JTextField m_BlockNameTextField;
	private RhapsodyComboBox m_TestDriverInheritanceChoice;
	private JTextField m_TestDriverNameTextField;
	private JCheckBox m_TestDriverCheckBox;
	private SimulationType m_SimulationType;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void launchThePanel(
			final IRPPackage theRootPackage,
			final IRPPackage theRequirementsAnalysisPkg,
			final SimulationType withSimulationType ) {
		
		String introText = null;
		
		if( withSimulationType==SimulationType.FullSim ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theActors = 
				theRequirementsAnalysisPkg.getNestedElementsByMetaClass("Actor", 1).toList();
			
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			introText = "This SysML-Toolkit helper sets up a nested package hierarchy for the functional analysis\n" +
					    "of a block from the perspective of the actors in the system. The initial structure will be\n" +
					    "created based on the " + theActors.size() + " actor(s) identified in the RequirementsAnalysisPkg called: " +
					    "\n";
			
			for (IRPModelElement theActor : theActors) {
				introText = "\t" + introText + theActor.getName() + "\n";
			}
			
		} else if ( withSimulationType==SimulationType.SimpleSim ){
			introText = "This SysML-Toolkit helper sets up a nested package hierarchy for the functional analysis of a block. \n" +
						"This 'Simple Sim' option supports guard-based state-machine logic simulation but does not support \n" +
						"injecting events via actors or test case creation. This can be added later, if necessary.\n";

		} else if ( withSimulationType==SimulationType.NoSim ){
			introText = "This SysML-Toolkit helper sets up a nested package hierarchy for the functional analysis of a block. \n" +
						"This 'No Sim' option supports activity-based analysis without simulation using state-machines.\n";

		} else {
			introText = "Error";
		}

		int response = JOptionPane.showConfirmDialog(null, 
				 introText +
				"\nDo you want to proceed?", "Confirm",
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if (response == JOptionPane.YES_OPTION) {
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );

					JFrame frame = new JFrame("Populate a 'system' block package hierarchy");
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateFunctionalBlockPackagePanel thePanel = 
							new CreateFunctionalBlockPackagePanel(
									theRootPackage, 
									theRequirementsAnalysisPkg, 
									withSimulationType );

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		}
	}

	CreateFunctionalBlockPackagePanel(
			IRPPackage theRootPackage,
			IRPPackage theRequirementsAnalysisPkg,
			final SimulationType withSimulationType ){
		super();
		
		m_RootPackage = theRootPackage;
		m_RequirementsAnalysisPkg = theRequirementsAnalysisPkg;
		m_SimulationType = withSimulationType;
		
		setLayout( new BorderLayout() );
		
		String theBlockName = GeneralHelpers.determineUniqueNameBasedOn(
				theBlankName, "Class", m_RootPackage.getProject() );
		
		add( createTheNameTheBlockPanel( theBlockName ), BorderLayout.PAGE_START );
		add( createContent( theBlockName ), BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	private void clearActorNamesIfNeeded(){
		
		for (ActorMappingInfo theInfo : m_ActorChoices) {	
			JTextField theField = theInfo.getTextField();
			theField.setVisible( theInfo.isSelected() );			
		}		
		
	}
	private void updateRelatedElementNames(){
		
		String theBlockName = m_BlockNameTextField.getText();
		
		if( m_ActorChoices != null ){
			for( ActorMappingInfo theInfo : m_ActorChoices ){
				theInfo.updateToBestActorNamesBasedOn( theBlockName );				
			}	
		}
		
		if( m_TestDriverNameTextField != null ){
			
			m_TestDriverNameTextField.setText(
					determineTestDriverName( theBlockName ) );
		}
	}
	
	private String determineTestDriverName(
			String basedOnBlockName ){
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toLegalClassName( basedOnBlockName ) + "_TestDriver", 
				"Class", 
				m_RootPackage.getProject() );
		
		return theProposedName;
	}
	
	private JPanel createTheNameTheBlockPanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		
		List<IRPModelElement> theExistingBlocks = 
				GeneralHelpers.findElementsWithMetaClassAndStereotype(
						"Class", "LogicalSystem", m_RootPackage, 1);
			
		m_BlockNameTextField = new JTextField( theBlockName );
		m_BlockNameTextField.setPreferredSize( new Dimension( 200, 20 ));
		
		m_BlockNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}	
				});
		
		m_BlockInheritanceChoice = new RhapsodyComboBox(theExistingBlocks, false);
		
		thePanel.add( new JLabel("                       Block name:") );
        thePanel.add( m_BlockNameTextField );			
        thePanel.add( new JLabel("  Inherit from:  ") );	
		thePanel.add( m_BlockInheritanceChoice );
			
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled){
		
		boolean isValid = true;
		String errorMsg = "";
		
		String theChosenBlockName = m_BlockNameTextField.getText();
		
		if ( theChosenBlockName.contains( theBlankName ) ){
			
			errorMsg += "Please choose a valid name for the Block";
			isValid = false;
			
		} else {
			boolean isLegalBlockName = GeneralHelpers.isLegalName( theChosenBlockName );
			
			if (!isLegalBlockName){
				
				errorMsg += theChosenBlockName + " is not legal as an identifier representing an executable Block\n";				
				isValid = false;
				
			} else if (!GeneralHelpers.isElementNameUnique(
				theChosenBlockName, "Class", m_RootPackage, 1)){

				errorMsg += "Unable to proceed as the Block name '" + theChosenBlockName + "' is not unique";
				isValid = false;
			
			} else {
				
				for (ActorMappingInfo actorChoice : m_ActorChoices) {
					
					String theChosenActorName = actorChoice.getName();
					
					if (actorChoice.isSelected()){
						
						boolean isLegalActorName = GeneralHelpers.isLegalName( theChosenActorName );
						
						if (!isLegalActorName){
							errorMsg += theChosenActorName + "is not legal as an identifier representing an executable actor\n";
							isValid = false;
							
						} else if (!GeneralHelpers.isElementNameUnique(
								theChosenActorName, "Actor", m_RootPackage, 1)){
							
							errorMsg += theChosenActorName + " is not unique, please choose again\n";
						}
					}
				}
			}
		}
		
		if (isMessageEnabled && !isValid && errorMsg != null){
			
			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}
	
	private JPanel createContent(
			String theBlockName ){
	
	    JPanel thePanel = new JPanel();
	    thePanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

	    GroupLayout theGroupLayout = new GroupLayout( thePanel );
	    thePanel.setLayout( theGroupLayout );
	    theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();
		
		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn3ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn4ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
	    theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
	    theHorizSequenceGroup.addGroup( theColumn3ParallelGroup );
	    theHorizSequenceGroup.addGroup( theColumn4ParallelGroup ); 
	    
	    m_ActorChoices = new ArrayList<ActorMappingInfo>();
	    
	    if( m_SimulationType==SimulationType.FullSim ){
	    	
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theRequirementsAnalysisActors = 
				m_RequirementsAnalysisPkg.getNestedElementsByMetaClass( "Actor", 1 ).toList();

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theExistingActors = 
					m_RootPackage.getNestedElementsByMetaClass( "Actor", 1 ).toList();
			
			for (IRPModelElement theActor : theRequirementsAnalysisActors) {
				
				Logger.writeLine("Creating actor '"+ theActor.getName() + "'");
				
			    JCheckBox theActorCheckBox = new JCheckBox("Create actor called:");
			    
				theActorCheckBox.setSelected(true);
							    
				theActorCheckBox.addActionListener( new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
				        clearActorNamesIfNeeded();		
					}
				});
				
				JTextField theActorNameTextField = new JTextField();
				theActorNameTextField.setPreferredSize( new Dimension( 200, 20 ));
				
				RhapsodyComboBox theInheritedActorComboBox = new RhapsodyComboBox(theExistingActors, false);			
				theInheritedActorComboBox.setPreferredSize(new Dimension(100, 20));
				
				ActorMappingInfo theMappingInfo = 
						new ActorMappingInfo(
								theInheritedActorComboBox, 
								theActorCheckBox, 
								theActorNameTextField, 
								(IRPActor)theActor,
								
								theActor.getProject() );
				
				theMappingInfo.updateToBestActorNamesBasedOn( theBlockName );
				
				m_ActorChoices.add( theMappingInfo );
			    
			    JLabel theLabel = new JLabel("Inherit from:");
			    
			    theColumn1ParallelGroup.addComponent( theActorCheckBox );   
			    theColumn2ParallelGroup.addComponent( theActorNameTextField );    
			    theColumn3ParallelGroup.addComponent( theLabel ); 
			    theColumn4ParallelGroup.addComponent( theInheritedActorComboBox  );
	    
			    ParallelGroup theVertical1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE);
			    theVertical1ParallelGroup.addComponent( theActorCheckBox );
			    theVertical1ParallelGroup.addComponent( theActorNameTextField );
			    theVertical1ParallelGroup.addComponent( theLabel  );	    
			    theVertical1ParallelGroup.addComponent( theInheritedActorComboBox );
			    
			    theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		    		    
			}
			
			m_TestDriverCheckBox = new JCheckBox("Create TestDriver called:");
			m_TestDriverCheckBox.setEnabled( false );
			m_TestDriverCheckBox.setSelected( true );
			
			m_TestDriverNameTextField = new JTextField( determineTestDriverName( theBlockName ) );
			m_TestDriverNameTextField.setPreferredSize( new Dimension( 200, 20 ));
			m_TestDriverNameTextField.setEnabled( false );
			m_TestDriverNameTextField.setEditable( false );
			
			List<IRPModelElement> theExistingBlocks = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"Class", "TestDriver", m_RootPackage, 1);
			
			m_TestDriverInheritanceChoice = new RhapsodyComboBox( theExistingBlocks, false );			
			m_TestDriverInheritanceChoice.setPreferredSize( new Dimension(100, 20) );
			
			JLabel theLabel = new JLabel("Inherit from:");
			
			theColumn1ParallelGroup.addComponent( m_TestDriverCheckBox );   
		    theColumn2ParallelGroup.addComponent( m_TestDriverNameTextField );    
		    theColumn3ParallelGroup.addComponent( theLabel ); 
		    theColumn4ParallelGroup.addComponent( m_TestDriverInheritanceChoice );
    
		    ParallelGroup theVertical1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE);
		    theVertical1ParallelGroup.addComponent( m_TestDriverCheckBox );
		    theVertical1ParallelGroup.addComponent( m_TestDriverNameTextField );
		    theVertical1ParallelGroup.addComponent( theLabel  );	    
		    theVertical1ParallelGroup.addComponent( m_TestDriverInheritanceChoice );
		    
		    theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );	
	    }
		
		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );
		
	    return thePanel;
	}
	
	private static void addAComponentWith(
			String theName,
			IRPPackage theBlockTestPackage, 
			IRPClass theUsageDomainBlock) {
		
		IRPComponent theComponent = 
				(IRPComponent) theBlockTestPackage.addNewAggr(
						"Component", theName + "_EXE");
		
		theComponent.setPropertyValue("Activity.General.SimulationMode", "StateOriented");

		IRPConfiguration theConfiguration = (IRPConfiguration) theComponent.findConfiguration("DefaultConfig");
		theConfiguration.setName("Cygwin");
		theConfiguration.addInitialInstance(theUsageDomainBlock);
		theConfiguration.setScopeType("implicit");
		theConfiguration.setPropertyValue("WebComponents.WebFramework.GenerateInstrumentationCode", "True");
		
		theConfiguration.getProject().setActiveConfiguration(theConfiguration);		
	}

	private static Set<IRPClassifier> getBaseClassesOf( 
			Set<IRPClassifier> theClassifiers ){
		
		Set<IRPClassifier> theBaseClasses = new HashSet<IRPClassifier>();
		
		for (IRPModelElement theEl : theClassifiers ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theGeneralizations = 
					theEl.getNestedElementsByMetaClass("Generalization", 0).toList();
			
			for (IRPModelElement theGenEl : theGeneralizations) {
				IRPGeneralization theGeneralization = (IRPGeneralization)theGenEl;
				
				IRPClassifier theBaseClass = theGeneralization.getBaseClass();
				theBaseClasses.add( theBaseClass );
			}
		}
		
		return theBaseClasses;
	}
	
	private static void createBDDFor(
			IRPClass theAssemblyBlock, 
			String withName){
		
		IRPObjectModelDiagram theBDD = 
				(IRPObjectModelDiagram) theAssemblyBlock.getOwner().addNewAggr(
						"ObjectModelDiagram", withName );
		
		theBDD.changeTo("Block Definition Diagram");
		
		String[] theClassFormatComponent = theBDD.getPropertyValue("Format.Class.DefaultSize").split(",");	
		
		int theClassWidth = 400; //Integer.parseInt( theClassFormatComponent[2] );
		int theClassHeight = Integer.parseInt( theClassFormatComponent[3] );
		
		String[] theActorFormatComponent = theBDD.getPropertyValue("Format.Actor.DefaultSize").split(",");	
		
		int theActorWidth = Integer.parseInt( theActorFormatComponent[2] );
		int theActorHeight = Integer.parseInt( theActorFormatComponent[3] );
		
		IRPCollection theGraphElsToDraw = FunctionalAnalysisPlugin.getRhapsodyApp().createNewCollection();
		
		@SuppressWarnings("unchecked")
		List<IRPRelation> theRelations = theAssemblyBlock.getRelations().toList();
		
		Set<IRPClassifier> theActors = new HashSet<IRPClassifier>();
		Set<IRPClassifier> theBlocks = new HashSet<IRPClassifier>();
		
		boolean toggle = false;
		
		for( IRPRelation theRelation : theRelations ){
			IRPClassifier theOtherClass = theRelation.getOtherClass();
			
			if( theOtherClass instanceof IRPActor ){
				theActors.add( theOtherClass );
			} else {
				theBlocks.add( theOtherClass );
			}
		}
		
		int xPos = 30;
		int yPos = 40;
		int xGapActors = 50;
		int xGapBlocks = -150;
		int yGap = 70;
		int yOffset = 180;

		float theActorsWidth = (float) ((theActors.size()*(xGapActors+theActorWidth))/2.0);
		float theClassesWidth = (float) ((theBlocks.size()*(xGapBlocks+theClassWidth))/2.0);
		
		IRPGraphNode theAssemblyNode = theBDD.addNewNodeForElement( 
				theAssemblyBlock, xPos, yPos, (int) (theActorsWidth + theClassesWidth)*2, theClassHeight);
		
		theGraphElsToDraw.addGraphicalItem( theAssemblyNode );
		
		yPos = yPos + theClassHeight + yGap;

		for( IRPClassifier theActor : theActors ) {
			
			IRPGraphNode theNode;
			
			if( toggle ){
				theNode = theBDD.addNewNodeForElement(
						theActor, xPos, yPos, theActorWidth, theActorHeight);
			} else {
				theNode = theBDD.addNewNodeForElement(
						theActor, xPos, yPos+yOffset, theActorWidth, theActorHeight);
			}
			
			toggle = !toggle;
			
			theGraphElsToDraw.addGraphicalItem( theNode );
			xPos = xPos + theActorWidth + xGapActors;
		}
		
		for( IRPClassifier theBlock : theBlocks ) {
			
			IRPGraphNode theNode;
			
			if( toggle ){
				theNode = theBDD.addNewNodeForElement(
						theBlock, xPos, yPos, theClassWidth, theClassHeight );
			} else {
				theNode = theBDD.addNewNodeForElement(
						theBlock, xPos, yPos+yOffset, theClassWidth, theClassHeight );
			}
			
			toggle = !toggle;
			
			theGraphElsToDraw.addGraphicalItem( theNode );
			xPos = xPos + theClassWidth + xGapBlocks;
		}
		
		IRPGraphElement theLastEl = 
				(IRPGraphElement) theGraphElsToDraw.getItem( theGraphElsToDraw.getCount() );
		
		int maxX = 1000;
		
		if( theLastEl != null && theLastEl instanceof IRPGraphNode ){
			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theLastEl );
			maxX = theNodeInfo.getBottomRightX();
		}

		Set<IRPClassifier> theBaseClassifiers = getBaseClassesOf( theActors );
		theBaseClassifiers.addAll( getBaseClassesOf( theBlocks ) );
		
		if( !theBaseClassifiers.isEmpty() ){
			
			int xGap = (maxX-30)/theBaseClassifiers.size();
			xPos = 30 + xGap/2;
			
			if( toggle ){
				yPos = yPos + theClassHeight + yGap*2;
			} else {
				yPos = yPos + (theClassHeight + yGap*2) + yOffset;
			}
			
			for( IRPClassifier theBaseClassifier : theBaseClassifiers ) {
				

				IRPGraphNode theNode;
				
				theNode = theBDD.addNewNodeForElement(
						theBaseClassifier, xPos, yPos, theActorWidth, theActorHeight);

				theGraphElsToDraw.addGraphicalItem( theNode );
				xPos = xPos + xGap;
			}
		}
		
		theBDD.completeRelations( theGraphElsToDraw, 1 );
	}
	
	private static void createIBDFor(
			IRPClass theAssemblyBlock, 
			String withName){
		
		IRPStructureDiagram theIBD = (IRPStructureDiagram) theAssemblyBlock.addNewAggr(
						"StructureDiagram", withName );
		
		theIBD.changeTo("Internal Block Diagram");
		
		IRPCollection theGraphElsToDraw = FunctionalAnalysisPlugin.getRhapsodyApp().createNewCollection();
		
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts =
		    theAssemblyBlock.getNestedElementsByMetaClass("Part", 0).toList();

		int countTestDrivers = 0;
		int countActors = 0;
		int countBlocks = 0;
		int maxCount = 0;
		
		// Count actors vs. internal parts vs test drivers
		for( IRPInstance thePart : theParts ) {

			IRPClassifier theType = thePart.getOtherClass();
	
			if( GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) ){
				countTestDrivers++;
			} else if( theType instanceof IRPActor ){
				countActors++;
			} else {
				countBlocks++;
			}
		}
		
		if( countBlocks > countActors ){
			maxCount = countBlocks;
		} else {
			maxCount = countActors;
		}
		
		int xPos = 30;
		int yPos = 40;
		int nWidth = 400;
		int nHeight = 120;
		int xGap = 30;
		int yGap = 200;

		float xMiddle = (float) ((float) ((float)(maxCount*(xGap+nWidth))/2.0)+(xGap/2.0));
		
		xPos = (int) (xMiddle - nWidth/2);
		
		if( countTestDrivers>0 ){
			// Do Test Driver first
			for( IRPInstance thePart : theParts ) {

				if( GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) ){
					IRPGraphNode theNode = theIBD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					theGraphElsToDraw.addGraphicalItem( theNode );
					xPos = xPos + nWidth + xGap;
				}
			}

			xPos = 30;
			yPos = yPos + yGap;
		}

		if( countActors>0 ){
			// Now do actors
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();
				
				if( theType instanceof IRPActor ){			
					IRPGraphNode theNode = theIBD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					theGraphElsToDraw.addGraphicalItem( theNode );
					xPos = xPos + nWidth + xGap;
				}
			}
		
			xPos = 30 + (maxCount/2)*(nWidth+xGap);	
			
		}

		xPos = (int) (xMiddle - nWidth/2);
		yPos = yPos + yGap;
		
		if( countBlocks>0 ){
			// Do normal blocks last
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();
				
				if( !GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) && !( theType instanceof IRPActor )){	
					theIBD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}
		}
		
		theIBD.completeRelations( theGraphElsToDraw, 1 );
	}
	
	@Override
	protected void performAction(){
		
		if( checkValidity( false ) ){
			
			String theName = m_BlockNameTextField.getText();
			
			// Create packages first
			IRPPackage theFunctionalBlockPkg = 
					m_RootPackage.addNestedPackage( theName + "Pkg" );  
			
			// Create nested package for block
			IRPPackage theBlockPackage = 
					theFunctionalBlockPkg.addNestedPackage(
							theName + "Block" + "Pkg" );
			
			// Create nested RequirementsPkg
			IRPModelElement theReqtsPkg =
					m_RootPackage.findNestedElement( "RequirementsPkg", "Package" );
			
			if( theReqtsPkg == null ){	
				// Create nested package for requirements & RD
				theReqtsPkg = m_RootPackage.addNestedPackage( "RequirementsPkg" );
			}
			
			// Create nested package for events and interfaces
			IRPPackage theInterfacesPkg = 
					theFunctionalBlockPkg.addNestedPackage( 
							theName + "Interfaces" + "Pkg" );
			
			// Create nested TestPkg package with components necessary for wiring up a simulation
			IRPPackage theTestPackage = 
					theFunctionalBlockPkg.addNestedPackage( 
							theName + "Test" + "Pkg" );
			
			// Create nested package for housing the ADs
			IRPPackage theWorkingPackage = 
					theFunctionalBlockPkg.addNestedPackage(
							theName + "Working" + "Pkg" );
			
			// Apply the same profile as the source ADs so that same rules apply
			IRPDependency theRAProfileDependency = 
					theWorkingPackage.addDependency(
							"RequirementsAnalysisProfile", "Profile" );
		
			theRAProfileDependency.addStereotype( "AppliedProfile", "Dependency" );
			
			FunctionalAnalysisSettings.setupFunctionalAnalysisTagsFor( 
					m_RootPackage.getProject(), 
					theFunctionalBlockPkg,
					theInterfacesPkg,
					theTestPackage );
			
			// Populate content for the BlockPkg
			IRPClass theLogicalSystemBlock = theBlockPackage.addClass( theName );
			GeneralHelpers.applyExistingStereotype( "LogicalSystem", theLogicalSystemBlock );
			theLogicalSystemBlock.changeTo( "Block" );
			theLogicalSystemBlock.highLightElement();

			IRPProject theProject = theLogicalSystemBlock.getProject();
			
			// only apply generalisation to create the state chart if simulation applies
			if( m_SimulationType==SimulationType.FullSim || 
			    m_SimulationType==SimulationType.SimpleSim ){
				
				IRPModelElement theChosenOne = m_BlockInheritanceChoice.getSelectedRhapsodyItem();
				
				if (theChosenOne==null ){
					addGeneralization( theLogicalSystemBlock, "TimeElapsedBlock", theProject );
					
				} else {
					theLogicalSystemBlock.addGeneralization( (IRPClassifier) theChosenOne );
					Logger.writeLine( theChosenOne, "was the chosen one" );
				}
			}
			
			if( theReqtsPkg != null && theReqtsPkg instanceof IRPPackage ){
				
				IRPObjectModelDiagram theRD = 
						((IRPPackage) theReqtsPkg).addObjectModelDiagram( "RD - " + theName );
				
				theRD.changeTo( "Requirements Diagram" );
			}
			
			if( m_SimulationType==SimulationType.FullSim || 
			    m_SimulationType==SimulationType.SimpleSim ){
				
				// Add Usage dependency to the interfaces package that will contain the system events
				IRPDependency theBlocksUsageDep = theBlockPackage.addDependencyTo( theInterfacesPkg );
				theBlocksUsageDep.addStereotype( "Usage", "Dependency" );
				
				// Add Usage dependency to the interfaces package that will contain the events
				IRPDependency theUsageDep = theTestPackage.addDependencyTo( theInterfacesPkg );
				theUsageDep.addStereotype( "Usage", "Dependency" );
			}	

			IRPClass theSystemAssemblyBlock = 
					theFunctionalBlockPkg.addClass( theName + "_SystemAssembly" );
			
			theSystemAssemblyBlock.changeTo("Block");

			// Make the LogicalSystem a part of the SystemAssembly block
			IRPInstance theLogicalSystemPart = 
					(IRPInstance) theSystemAssemblyBlock.addNewAggr(
							"Part", "" );
			
			theLogicalSystemPart.setOtherClass( theLogicalSystemBlock );
			
			GeneralHelpers.applyExistingStereotype( "LogicalSystem", theLogicalSystemPart );	
			
			// Populate nested TestPkg package with components necessary for wiring up a simulation

			if( m_SimulationType==SimulationType.FullSim ||
				m_SimulationType==SimulationType.SimpleSim ){
				
				// Make ElapsedTime actor part of the SystemAssembly block
				IRPModelElement theElapsedTimeActor = 
						theProject.findNestedElementRecursive( "ElapsedTime", "Actor" );
				
				IRPInstance theElapsedTimePart = null;
				
				if( theElapsedTimeActor != null ){
					
					theElapsedTimePart = 
							(IRPInstance) theSystemAssemblyBlock.addNewAggr(
									"Part", "");
					
					theElapsedTimePart.setOtherClass( 
							(IRPClassifier) theElapsedTimeActor );
					
					IRPSysMLPort theActorsElapsedTimePort = 
							(IRPSysMLPort) GeneralHelpers.findNestedElementUnder( 
									(IRPClassifier) theElapsedTimeActor,
									"elapsedTime",
									"SysMLPort",
									true );
							
					IRPSysMLPort theBlocksElapsedTimePort = 
							(IRPSysMLPort) GeneralHelpers.findNestedElementUnder( 
									(IRPClassifier) theLogicalSystemBlock,
									"elapsedTime",
									"SysMLPort",
									true );
					
					if( theActorsElapsedTimePort != null &&
						theBlocksElapsedTimePort != null ){
						
						GeneralHelpers.AddConnectorBetweenSysMLPortsIfOneDoesntExist(
								theActorsElapsedTimePort, 
								theElapsedTimePart, 
								theBlocksElapsedTimePort, 
								theLogicalSystemPart );
						
					} else {
						Logger.writeLine("Error in CreateFunctionalBlockPackagePanel.performAction(), unable to find elapsedTime ports") ;
					}
						
				} else {
					Logger.writeLine("Error in CreateFunctionalBlockPackagePanel.performAction: Unable to find ElapsedTime actor in project. You may be missing the BasePkg");
				}
				
				IRPPanelDiagram thePD = 
						theTestPackage.addPanelDiagram(
								"PD - " + theLogicalSystemBlock.getName() );
				
				if( m_SimulationType==SimulationType.FullSim ){
					
					IRPClass theTesterBlock = 
							theTestPackage.addClass( m_TestDriverNameTextField.getText() );
					
					GeneralHelpers.applyExistingStereotype( "TestDriver", theTesterBlock );
					
					theTesterBlock.changeTo( "Block" );
					
					IRPModelElement theTestDriverBase = 
							m_TestDriverInheritanceChoice.getSelectedRhapsodyItem();
					
					if (theTestDriverBase==null ){
						addGeneralization( theTesterBlock, "TestDriverBlock", theProject );
						
					} else {
						theTesterBlock.addGeneralization( 
								(IRPClassifier) theTestDriverBase );
						
						Logger.writeLine( theTestDriverBase, "was the chosen test driver base" );
					}

					// Make the TestDriver a part of the UsageDomain block
					IRPInstance theTestDriverPart = 
							(IRPInstance) theSystemAssemblyBlock.addNewAggr(
									"Part", "" );
					
					theTestDriverPart.setOtherClass( theTesterBlock );
					
					GeneralHelpers.applyExistingStereotype( "TestDriver", theTestDriverPart );

					for( ActorMappingInfo theInfo : m_ActorChoices ){
						
						theInfo.performActorPartCreationIfSelectedIn( 
								theSystemAssemblyBlock,
								theLogicalSystemBlock );		
					}
					
					// Connect TestDriver to elapsedTime actor
					IRPPort theElapsedTimePortOnTesterBlock = 
							(IRPPort) theTesterBlock.addNewAggr( "Port", "pElapsedTime" );
					
					IRPPort theTesterPortOnElapsedTimeActor =
							(IRPPort) theElapsedTimeActor.findNestedElement( "pTester", "Port" );

					if( theTesterPortOnElapsedTimeActor != null &&
						theElapsedTimePart != null ){

						theSystemAssemblyBlock.addLink(
								theElapsedTimePart, 
								theTestDriverPart, 
								null, 
								theTesterPortOnElapsedTimeActor,
								theElapsedTimePortOnTesterBlock );
					} else {
						Logger.writeLine( "Error, either part or port is null" );
					}
					
				} else {
					// assume panel diagram simulation will be used (esp. for simple sim)
					GeneralHelpers.applyExistingStereotype("AutoShow", thePD);
				} // end FullSim only				
				
				// Add a sequence diagram
				SequenceDiagramHelper.createSequenceDiagramFor(
						theSystemAssemblyBlock, 
						theTestPackage, 
						"SD - " + theName );
				
				IRPStatechartDiagram theStatechart = 
						theLogicalSystemBlock.getStatechart().getStatechartDiagram();

				if( theStatechart != null ){
					theStatechart.highLightElement();
					theStatechart.openDiagram();
				}
				
				// Add a component
				addAComponentWith( theName, theTestPackage, theSystemAssemblyBlock );
			}
			
			createBDDFor(
					theSystemAssemblyBlock,
					"BDD - " + theSystemAssemblyBlock.getName() );

			createIBDFor( 
					theSystemAssemblyBlock, 
					"IBD - " + theSystemAssemblyBlock.getName() );
			
			CreateGatewayProjectPanel.launchThePanel( 
					theProject, 
					"^FunctionalAnalysisPkg.rqtf$" );
			
			CopyActivityDiagramsPanel.launchThePanel(
					m_RequirementsAnalysisPkg, 
					theWorkingPackage);
			
		} else {
			Logger.writeLine("Error in CreateFunctionalBlockPackagePanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #023 30-MAY-2016: Added form to support validation checks for analysis block hierarchy creation (F.J.Chadburn) 
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #039 17-JUN-2016: Minor fixes and improvements to robustness of Gateway project setup (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
    #045 03-JUL-2016: Fix CopyActivityDiagramsPanel capability (F.J.Chadburn)
    #048 06-JUL-2016: RequirementsPkg now created in FunctionalAnalysisPkg.sbs rather than nested deeper (F.J.Chadburn)
    #054 13-JUL-2016: Create a nested BlockPkg package to contain the Block and events (F.J.Chadburn)
    #062 17-JUL-2016: Create InterfacesPkg and correct build issues by adding a Usage dependency (F.J.Chadburn)
    #087 09-AUG-2016: Added packageForEventsAndInterfaces tag to give user flexibility to change (F.J.Chadburn)
    #106 03-NOV-2016: Ease usage by renaming UsageDomain block to SystemAssembly and moving up one package (F.J.Chadburn)
    #111 13-NOV-2016: Added new Simple Sim (Guard only) functional analysis structure option (F.J.Chadburn)
    #112 13-NOV-2016: Added new No Sim functional analysis structure option (F.J.Chadburn)
    #118 13-NOV-2016: Default FunctionalAnalysisPkg tags now set in Config.properties file (F.J.Chadburn)
    #120 25-NOV-2016: Enable TestDriver inheritance in the FullSim block creation dialog (F.J.Chadburn)
    #131 25-NOV-2016: Added initial auto-populate of IBD/BDD (F.J.Chadburn)
    #138 02-DEC-2016: Highlight Block when creating functional analysis structure (F.J.Chadburn)
    #139 02-DEC-2016: Improve robustness in block naming dialog when m_TestDriverNameTextField is null (F.J.Chadburn)
    #145 18-DEC-2016: Fix to remove warning with getWorkingPkgUnderDev unexpectedly finding 2 packages (F.J.Chadburn)
    #179 29-MAY-2017: Added new Functional Analysis menu to Re-create «AutoShow» sequence diagram (F.J.Chadburn)
    #184 29-MAY-2017: Create a connector between pElapsedTime port when creating block hierarchy (F.J.Chadburn)
    #185 29-MAY-2017: Change Block hierarchy creation to use implicit not explicit names for parts (F.J.Chadburn)

    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
*/
