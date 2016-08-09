package functionalanalysisplugin;

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
import java.util.List;

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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void launchThePanel(
			final IRPPackage theRootPackage,
			final IRPPackage theRequirementsAnalysisPkg) {
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theActors = 
			theRequirementsAnalysisPkg.getNestedElementsByMetaClass("Actor", 1).toList();
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		String introText = "This SysML-Toolkit helper sets up a nested package hierarchy for the functional analysis\n" +
				"of a block from the perspective of the actors in the system. The initial structure will be\n" +
				"created based on the " + theActors.size() + " actor(s) identified in the RequirementsAnalysisPkg called: " +
				"\n";
		
		for (IRPModelElement theActor : theActors) {
			introText = "\t" + introText + theActor.getName() + "\n";
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

					JFrame frame = new JFrame("Populate package hierarchy for an analysis block");
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateFunctionalBlockPackagePanel thePanel = 
							new CreateFunctionalBlockPackagePanel(
									theRootPackage, theRequirementsAnalysisPkg);

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
			IRPPackage theRequirementsAnalysisPkg){
		super();
		
		m_RootPackage = theRootPackage;
		m_RequirementsAnalysisPkg = theRequirementsAnalysisPkg;
		
		setLayout( new BorderLayout() );
		
		String theBlockName = GeneralHelpers.determineUniqueNameBasedOn(
				theBlankName, "Class", m_RootPackage.getProject());
		
		add( createTheNameTheBlockPanel( theBlockName ), BorderLayout.PAGE_START);
		add( createContent( theBlockName ), BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	private void clearActorNamesIfNeeded(){
		
		for (ActorMappingInfo theInfo : m_ActorChoices) {	
			JTextField theField = theInfo.getTextField();
			theField.setVisible( theInfo.isSelected() );			
		}		
		
	}
	private void updateActorName(){
		
		for (ActorMappingInfo theInfo : m_ActorChoices) {
			theInfo.updateToBestActorNamesBasedOn( m_BlockNameTextField.getText() );			
		}		
	}
	
	private JPanel createTheNameTheBlockPanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		
		List<IRPModelElement> theExistingBlocks = 
				GeneralHelpers.findElementsWithMetaClassAndStereotype(
						"Class", "LogicalSystem", m_RootPackage);
			
		m_BlockNameTextField = new JTextField( theBlockName );
		m_BlockNameTextField.setPreferredSize( new Dimension( 200, 20 ));
		
		m_BlockNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateActorName();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateActorName();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateActorName();
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
	
	private JPanel createContent(String theBlockName){
	
	    JPanel thePanel = new JPanel();
	    thePanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

	    GroupLayout theGroupLayout = new GroupLayout( thePanel );
	    thePanel.setLayout( theGroupLayout );
	    theGroupLayout.setAutoCreateGaps( true );
	    
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theRequirementsAnalysisActors = 
			m_RequirementsAnalysisPkg.getNestedElementsByMetaClass("Actor", 1).toList();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theExistingActors = 
				m_RootPackage.getNestedElementsByMetaClass("Actor", 1).toList();
		
		m_ActorChoices = new ArrayList<ActorMappingInfo>();

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
		
		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

	    return thePanel;
	}
	
	private static void addAComponentWith(
			String theName,
			IRPPackage theBlockTestPackage, 
			IRPClass theUsageDomainBlock) {
		
		IRPComponent theComponent = (IRPComponent) theBlockTestPackage.addNewAggr("Component", theName + "_EXE");
		theComponent.setPropertyValue("Activity.General.SimulationMode", "StateOriented");

		IRPConfiguration theConfiguration = (IRPConfiguration) theComponent.findConfiguration("DefaultConfig");
		theConfiguration.setName("Cygwin");
		theConfiguration.addInitialInstance(theUsageDomainBlock);
		theConfiguration.setScopeType("implicit");
		theConfiguration.setPropertyValue("WebComponents.WebFramework.GenerateInstrumentationCode", "True");
		
		theConfiguration.getProject().setActiveConfiguration(theConfiguration);		
	}

	private static void createSequenceDiagramFor(
			IRPClass theUsageDomainBlock, 
			String withName){
		
		IRPModelElement theOwner = theUsageDomainBlock.getOwner();
		
		if (theOwner instanceof IRPPackage){
			IRPPackage thePackage = (IRPPackage)theOwner;
			
			IRPSequenceDiagram theSD = thePackage.addSequenceDiagram(withName);
			
			@SuppressWarnings("unchecked")
			List<IRPInstance> theParts = theUsageDomainBlock.getNestedElementsByMetaClass("Part", 0).toList();
			
			int xPos = 30;
			int yPos = 0;
			int nWidth = 100;
			int nHeight = 1000;
			int xGap = 30;
			
			for (IRPInstance thePart : theParts) {
				
				if (GeneralHelpers.hasStereotypeCalled("TestDriver", thePart)){;
					
					IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement(theType, xPos, yPos, nWidth, nHeight);
					xPos=xPos+nWidth+xGap;
				}
			}
			
			for (IRPInstance thePart : theParts) {
				
				if (!GeneralHelpers.hasStereotypeCalled("TestDriver", thePart) && !GeneralHelpers.hasStereotypeCalled("LogicalSystem", thePart)){;
					
					IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement(theType, xPos, yPos, nWidth, nHeight);
					xPos=xPos+nWidth+xGap;
				}
			}
			
			for (IRPInstance thePart : theParts) {
				
				if (GeneralHelpers.hasStereotypeCalled("LogicalSystem", thePart)){;
					
					IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement(theType, xPos, yPos, nWidth, nHeight);
					xPos=xPos+nWidth+xGap;
				}
			}
			
			GeneralHelpers.applyExistingStereotype("AutoShow", theSD);
			
		} else {
			Logger.writeLine("Error in createSequenceDiagramFor: Expected owner to be a Package");
		}
	}

	@Override
	protected void performAction(){
		
		if (checkValidity( false )){
			String theName = m_BlockNameTextField.getText();
			
			IRPPackage theFunctionalBlockPkg = m_RootPackage.addNestedPackage( theName + "Pkg" );  
			
			// Create nested package for block
			IRPPackage theBlockPackage = theFunctionalBlockPkg.addNestedPackage(theName + "Block" + "Pkg");
			IRPClass theLogicalSystemBlock = theBlockPackage.addClass( theName );
			GeneralHelpers.applyExistingStereotype("LogicalSystem", theLogicalSystemBlock);
			theLogicalSystemBlock.changeTo("Block");

			IRPModelElement theChosenOne = m_BlockInheritanceChoice.getSelectedRhapsodyItem();

			IRPProject theProject = theLogicalSystemBlock.getProject();
			
			if (theChosenOne==null){
				addGeneralization(theLogicalSystemBlock, "TimeElapsedBlock", theProject);
			} else {
				theLogicalSystemBlock.addGeneralization( (IRPClassifier) theChosenOne );
				Logger.writeLine(theChosenOne, "was the chosen one");
			}
			
			// Create nested package for events and interfaces
			IRPPackage theInterfacesPkg = theFunctionalBlockPkg.addNestedPackage(theName + "Interfaces" + "Pkg");

			// Update tag to point to the nested package
			setTagOn( m_RootPackage, FunctionalAnalysisSettings.tagNameForPackageForEventsAndInterfaces, theInterfacesPkg );
								
			// Add Usage dependency to the interfaces package that will contain the events
			IRPDependency theBlocksUsageDep = theBlockPackage.addDependencyTo( theInterfacesPkg );
			theBlocksUsageDep.addStereotype( "Usage", "Dependency" );
			
			// Create nested package with components necessary for wiring up a simulation
			IRPPackage theBlockTestPackage = theFunctionalBlockPkg.addNestedPackage(theName + "Test" + "Pkg");
			
			// Add Usage dependency to the interfaces package that will contain the events
			IRPDependency theUsageDep = theBlockTestPackage.addDependencyTo( theInterfacesPkg );
			theUsageDep.addStereotype( "Usage", "Dependency" );

			IRPClass theUsageDomainBlock = theBlockTestPackage.addClass(theName + "_UsageDomain");
			theUsageDomainBlock.changeTo("Block");

			IRPObjectModelDiagram theBDD = 
					theBlockTestPackage.addObjectModelDiagram("BDD - " + theUsageDomainBlock.getName());
			
			theBDD.changeTo("Block Definition Diagram");

			IRPStructureDiagram theIBD = 
					(IRPStructureDiagram) theUsageDomainBlock.addNewAggr(
							"StructureDiagram", "IBD - " + theUsageDomainBlock.getName());
			
			theIBD.changeTo("Internal Block Diagram");					    	

			// Make the LogicalSystem a part of the UsageDomain block
			IRPInstance theLogicalSystemPart = addPartTo(theUsageDomainBlock, theLogicalSystemBlock);
			GeneralHelpers.applyExistingStereotype("LogicalSystem", theLogicalSystemPart);	

			IRPClass theTesterBlock = theBlockTestPackage.addClass(theName + "_Tester");
			GeneralHelpers.applyExistingStereotype("TestDriver", theTesterBlock);
			theTesterBlock.changeTo("Block");
			addGeneralization(theTesterBlock, "TestDriverBlock", theProject);

			// Make the TestDriver a part of the UsageDomain block
			IRPInstance theTestDriverPart = addPartTo(theUsageDomainBlock, theTesterBlock);
			GeneralHelpers.applyExistingStereotype("TestDriver", theTestDriverPart);

			for (ActorMappingInfo theInfo : m_ActorChoices) {
				theInfo.performActorPartCreationIfSelectedTo( theUsageDomainBlock );
			}

			// Add a sequence diagram
			createSequenceDiagramFor(theUsageDomainBlock, "SD - " + theName);
	
			setTagOn( m_RootPackage, FunctionalAnalysisSettings.tagNameForPackageUnderDev, theFunctionalBlockPkg );
								
			IRPStatechartDiagram theStatechart = theLogicalSystemBlock.getStatechart().getStatechartDiagram();

			if (theStatechart != null){
				theStatechart.highLightElement();
				theStatechart.openDiagram();
			}
			
			// Create nested package for housing the ADs
			IRPPackage theWorkingPackage = theFunctionalBlockPkg.addNestedPackage(theName + "Working" + "Pkg");
			
			// This dependency is also used to locate the working package
			IRPDependency theRAProfileDependency = 
					theWorkingPackage.addDependency("RequirementsAnalysisProfile", "Profile");
		
			theRAProfileDependency.addStereotype("AppliedProfile", "Dependency");
			
			// Add a component
			addAComponentWith(theName, theBlockTestPackage, theUsageDomainBlock);
			
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

	private void setTagOn(
			IRPPackage theRootPackage,
			final String withTagName,
			IRPPackage toPackage ) {
		
		// Set up the settings
		IRPTag theTag = m_RootPackage.getTag( withTagName );
		
		if( theTag == null ){
			Logger.writeLine( "Error in setTagOn " + Logger.elementInfo( theRootPackage ) + 
					", unable to find tag called " + withTagName );
			
		} else {
			Logger.writeLine( "Setting " + withTagName + 
					" tag owned by " + Logger.elementInfo( theRootPackage ) + " to " + 
					Logger.elementInfo( toPackage ));
			
			m_RootPackage.setTagElementValue( theTag, toPackage );
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

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
