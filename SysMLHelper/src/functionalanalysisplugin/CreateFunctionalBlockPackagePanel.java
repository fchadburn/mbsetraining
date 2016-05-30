package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.PopulatePkg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateFunctionalBlockPackagePanel extends JPanel {

	final private String theBlankName = "<Put Name Here>";
	private IRPPackage m_RootPackage;
	private IRPPackage m_RequirementsAnalysisPkg;
	private List<ClassifierMappingInfo> m_ActorChoices;
	private RhapsodyComboBox m_BlockInheritanceChoice;
	private JTextField m_BlockNameTextField;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
		
		for (ClassifierMappingInfo theInfo : m_ActorChoices) {	
			JTextField theField = theInfo.getTextField();
			theField.setVisible( theInfo.isSelected() );			
		}		
		
	}
	private void updateActorName(){
		
		for (ClassifierMappingInfo theInfo : m_ActorChoices) {
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
	
	private boolean checkValidity(boolean isMessageEnabled){
		
		boolean isValid = true;
		String errorMsg = "";
		
		String theChosenBlockName = m_BlockNameTextField.getText();
		
		if ( theChosenBlockName.contains( theBlankName ) ){
			
			errorMsg += "Please choose a valid name for the Block";
			isValid = false;
			
		} else {
			boolean isLegalBlockName = isLegalName( theChosenBlockName );
			
			if (!isLegalBlockName){
				
				errorMsg += theChosenBlockName + " is not legal as an identifier representing an executable Block\n";				
				isValid = false;
				
			} else if (!GeneralHelpers.isElementNameUnique(
				theChosenBlockName, "Class", m_RootPackage, 1)){

				errorMsg += "Unable to proceed as the Block name '" + theChosenBlockName + "' is not unique";
				isValid = false;
			
			} else {
				
				for (ClassifierMappingInfo actorChoice : m_ActorChoices) {
					
					String theChosenActorName = actorChoice.getName();
					
					if (actorChoice.isSelected()){
						
						boolean isLegalActorName = isLegalName( theChosenActorName );
						
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

			JDialog.setDefaultLookAndFeelDecorated(true);

			JOptionPane.showMessageDialog(
					null,  
					errorMsg,
					"Warning",
					JOptionPane.WARNING_MESSAGE);	
		}
		
		return isValid;
	}
	
	private static boolean isLegalName(String theName){
		
		String regEx = "^(([a-zA-Z_][a-zA-Z0-9_]*)|(operator.+))$";
		
		boolean isLegal = theName.matches( regEx );
		
		if (!isLegal){
			Logger.writeLine("Warning, detected that " + theName 
					+ " is not a legal name as it does not conform to the regex=" + regEx);
		}
		
		return isLegal;
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
		
		m_ActorChoices = new ArrayList<ClassifierMappingInfo>();

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
			
			ClassifierMappingInfo theMappingInfo = 
					new ClassifierMappingInfo(
							theInheritedActorComboBox, 
							theActorCheckBox, 
							theActorNameTextField, 
							(IRPActor)theActor );
			
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
	
	public JPanel createOKCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton("OK");
		theOKButton.setPreferredSize(new Dimension(75,25));
		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Logger.writeLine("theOKButton.addActionListener");
					
					boolean isValid = checkValidity( true );
					
					if (isValid){
						Logger.writeLine("Is valid to close");
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}
												
				} catch (Exception e2) {
					Logger.writeLine("Unhandled exception in createOKCancelPanel->theOKButton.actionPerformed");
				}
			}
		});
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize( new Dimension( 75,25 ) );	
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Logger.writeLine("theCancelButton.addActionListener");
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					Logger.writeLine("Unhandled exception in createOKCancelPanel->theCancelButton.actionPerformed");
				}		
			}	
		});
		
		thePanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	private static void addGeneralization(
			IRPClassifier fromElement, 
			String toBlockWithName, 
			IRPPackage underneathTheRootPackage){
		
		IRPModelElement theBlock = 
				underneathTheRootPackage.findNestedElementRecursive( toBlockWithName, "Block" );
		
		if (theBlock != null){
			fromElement.addGeneralization( (IRPClassifier) theBlock );
		} else {
			Logger.writeLine("Error: Unable to find element with name " + toBlockWithName);
		}
	}
	
	private static IRPInstance addPartTo(
			IRPClassifier theElement, 
			IRPClassifier typedByElement){
		
		IRPInstance thePart = (IRPInstance) theElement.addNewAggr("Part", "its" + typedByElement.getName());
		thePart.setOtherClass(typedByElement);
		
		return thePart;
	}
	
	public static IRPInstance addActorPartTo(IRPClass theUsageBlock, String withNameForActor){
		
		IRPInstance theActorPart = null;
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = theUsageBlock.getNestedElementsByMetaClass("Part", 0).toList();
		
		IRPClassifier theLogicalSystemBlock = null;
		IRPInstance theLogicalSystemPart = null;
		
		IRPClassifier theTesterBlock = null;
		IRPInstance theTesterPart = null;
		
		for (IRPInstance thePart : theParts) {
			
			if (GeneralHelpers.hasStereotypeCalled("LogicalSystem", thePart)){
				theLogicalSystemPart = thePart;
				theLogicalSystemBlock = thePart.getOtherClass();
				Logger.writeLine(theLogicalSystemPart, "is the LogicalSystem part");
				Logger.writeLine(theLogicalSystemBlock, "is the LogicalSystem block");
			}
			if (GeneralHelpers.hasStereotypeCalled("TestDriver", thePart)){	
				theTesterPart = thePart;
				theTesterBlock = thePart.getOtherClass();
				Logger.writeLine(theTesterPart, "is the Tester part");
				Logger.writeLine(theTesterBlock, "is the Tester block");
			}
		}
		
		if (theLogicalSystemBlock != null && theTesterBlock != null){
			IRPActor theTestActor = ((IRPPackage) theUsageBlock.getOwner()).addActor( withNameForActor );
			
			IRPActor theTestbench = (IRPActor) theTestActor.getProject().findNestedElementRecursive("Testbench", "Actor");
			
			if (theTestbench != null){
				theTestActor.addGeneralization( theTestbench );
			} else {
				Logger.writeLine("Error: Unable to find Actor with name Testbench");
			}
			
			// Make each of the actors a part of the UsageDomain block
			theActorPart = addPartTo(theUsageBlock, theTestActor);
			
			// and connect actor to the LogicalSystem block
	    	IRPPort theActorToSystemPort = (IRPPort) theTestActor.addNewAggr("Port", "pLogicalSystem");
			IRPPort theSystemToActorPort = (IRPPort) theLogicalSystemBlock.addNewAggr("Port", "p" + theTestActor.getName());
			IRPLink theLogicalSystemLink = (IRPLink) theUsageBlock.addLink(
					theActorPart, theLogicalSystemPart, null, theActorToSystemPort, theSystemToActorPort);
			theLogicalSystemLink.changeTo("connector");
			
			// and connect actor to the TestDriver block
	    	IRPPort theActorToTesterPort = (IRPPort) theTestActor.addNewAggr("Port", "pTester");
			IRPPort theTesterToActorPort = (IRPPort) theTesterBlock.addNewAggr("Port", "p" + theTestActor.getName());
			IRPLink theTesterLink = (IRPLink) theUsageBlock.addLink(
					theActorPart, theTesterPart, null, theActorToTesterPort, theTesterToActorPort);
			theTesterLink.changeTo("connector");
		}
		
		return theActorPart;
	}
	
	private static void addAComponentWith(String theName,
			IRPPackage theBlockTestPackage, IRPClass theUsageDomainBlock) {
		
		IRPComponent theComponent = (IRPComponent) theBlockTestPackage.addNewAggr("Component", theName + "_EXE");
		theComponent.setPropertyValue("Activity.General.SimulationMode", "StateOriented");

		IRPConfiguration theConfiguration = (IRPConfiguration) theComponent.findConfiguration("DefaultConfig");
		theConfiguration.setName("Cygwin");
		theConfiguration.addInitialInstance(theUsageDomainBlock);
		theConfiguration.setScopeType("implicit");
		theConfiguration.setPropertyValue("WebComponents.WebFramework.GenerateInstrumentationCode", "True");
		
		theConfiguration.getProject().setActiveConfiguration(theConfiguration);		
	}

	private static void createSequenceDiagramFor(IRPClass theUsageDomainBlock, String withName){
		
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
	
	private void performAction(){
		
		if (checkValidity( false )){
			String theName = m_BlockNameTextField.getText();
			

			IRPPackage theBlockPackage = m_RootPackage.addNestedPackage( theName + "Pkg" );  
			
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

			// Create nested package with components necessary for wiring up a simulation
			IRPPackage theBlockTestPackage = theBlockPackage.addNestedPackage(theName + "Test" + "Pkg");

			IRPClass theUsageDomainBlock = theBlockTestPackage.addClass(theName + "_UsageDomain");
			theUsageDomainBlock.changeTo("Block");

			IRPObjectModelDiagram theBDD = theBlockPackage.addObjectModelDiagram("BDD - " + theUsageDomainBlock.getName());
			theBDD.changeTo("Block Definition Diagram");

			IRPStructureDiagram theIBD = (IRPStructureDiagram) theUsageDomainBlock.addNewAggr("StructureDiagram", "IBD - " + theUsageDomainBlock.getName());
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

			for (ClassifierMappingInfo theInfo : m_ActorChoices) {

				if (theInfo.isSelected()){

					String theLegalActorName = theInfo.getName().replaceAll(" ", "");
					IRPInstance theActorPart = addActorPartTo(theUsageDomainBlock, theLegalActorName);		

					String theText = "Create actor called " + theInfo.getName();

					IRPModelElement theInheritedFrom = theInfo.getInheritedFrom();

					if (theInheritedFrom != null){
						theText = theText + " inherited from " + theInheritedFrom.getName();
						IRPClassifier theClassifier = theActorPart.getOtherClass();

						theClassifier.addGeneralization( (IRPClassifier) theInheritedFrom );
					}

					Logger.writeLine(theText);
				} else {
					Logger.writeLine("Not selected");
				}
			}

			// Add a sequence diagram
			createSequenceDiagramFor(theUsageDomainBlock, "SD - " + theName);

			final String tagNameForPackageUnderDev = "packageUnderDev";
			
			// Set up the settings
			IRPTag theTagForPackageUnderDev = m_RootPackage.getTag( tagNameForPackageUnderDev );
			
			if (theTagForPackageUnderDev==null){
				Logger.writeLine("Error in setFunctionalAnalysisSettings, unable to find tag called " + tagNameForPackageUnderDev);
			} else {
				Logger.writeLine("Setting " + Logger.elementInfo(theTagForPackageUnderDev) 
						+ " owned by " + Logger.elementInfo( m_RootPackage ) + " to " 
						+ Logger.elementInfo(theBlockPackage));
				
				m_RootPackage.setTagElementValue(theTagForPackageUnderDev, theBlockPackage);
			}
								
			IRPStatechartDiagram theStatechart = theLogicalSystemBlock.getStatechart().getStatechartDiagram();

			if (theStatechart != null){
				theStatechart.highLightElement();
				theStatechart.openDiagram();
			}
			
			// Create nested package for housing the ADs
			IRPPackage theWorkingPackage = theBlockPackage.addNestedPackage(theName + "Working" + "Pkg");
			
	    	PopulatePkg.addProfileIfNotPresentAndMakeItApplied(
	    			"RequirementsAnalysisProfile", theWorkingPackage);
		
			copyActivityDiagramsForEachUseCase(
					m_RequirementsAnalysisPkg, theWorkingPackage);
			
			// Add a component
			addAComponentWith(theName, theBlockTestPackage, theUsageDomainBlock);
			
		} else {
			Logger.writeLine("Error in CreateFunctionalBlockPackagePanel.performAction, checkValidity returned false");
		}	
	}
	
	@SuppressWarnings("unchecked")
	private static void copyActivityDiagramsForEachUseCase(
			IRPModelElement underneathTheEl, IRPModelElement toElement){
		
		List<IRPFlowchart> allTheFlowcharts = new ArrayList<IRPFlowchart>();
		
		List<IRPUseCase> theUseCases = underneathTheEl.getNestedElementsByMetaClass("UseCase", 1).toList();	
		
		for (IRPUseCase theUseCase : theUseCases) {
			allTheFlowcharts.addAll( theUseCase.getNestedElementsByMetaClass("ActivityDiagram", 1).toList() );		
		}
		
		if (!allTheFlowcharts.isEmpty()){
				
			String msgText = "Do you want to copy the following " + allTheFlowcharts.size() 
					+ " activity diagrams \n "
					+ "from the " + theUseCases.size() + " use cases\n"
					+ "to " + Logger.elementInfo(toElement) + "?\n";

			for (IRPFlowchart theFlowchart : allTheFlowcharts) {
				msgText = msgText + theFlowchart.getName() + "\n";
			}

			int response = JOptionPane.showConfirmDialog(null, msgText, "Confirm",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.YES_OPTION) {
				Logger.writeLine("User confirmed to create");

				for (IRPFlowchart theFlowchart : allTheFlowcharts) {
					cloneTheFlowchart(toElement, theFlowchart);
				}		    	
			}	
		} else {
			Logger.writeLine("No Activity Diagrams were found underneath the " + Logger.elementInfo(underneathTheEl));
		}
	}
	
	private static void cloneTheFlowchart(
			IRPModelElement toElement,
			IRPFlowchart theFlowchart) {
		
		Logger.writeLine("Cloned " + Logger.elementInfo(theFlowchart) + " to " + Logger.elementInfo(toElement));
		
		IRPFlowchart theNewFlowchart = (IRPFlowchart) theFlowchart.clone("Working - " + theFlowchart.getName(), toElement);
		
		IRPDependency theDependency = theNewFlowchart.addDependencyTo(theFlowchart);
		theDependency.changeTo("Refinement");
		
		Logger.writeLine(theDependency, "was added");
		
		IRPGraphNode theNote = theNewFlowchart.addNewNodeByType("Note", 20, 44, 120, 70);
		
		theNote.setGraphicalProperty("Text", "This working copy of the use case steps can be used to generate the state machine.");
		
		theNewFlowchart.highLightElement();
		theNewFlowchart.getFlowchartDiagram().openDiagram();
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #023 30-MAY-2016: Added form to support validation checks for analysis block hierarchy creation (F.J.Chadburn) 

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
