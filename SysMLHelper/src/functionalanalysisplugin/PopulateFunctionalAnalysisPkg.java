package functionalanalysisplugin;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import generalhelpers.GeneralHelpers;
import generalhelpers.PopulatePkg;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class PopulateFunctionalAnalysisPkg extends PopulatePkg {

	public static void createFunctionalAnalysisPkg(IRPProject forProject){
		 
		final String rootPackageName = "FunctionalAnalysisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null){
			Logger.writeLine("Doing nothing: " + Logger.elementInfo( forProject ) + " already has package called " + rootPackageName);
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for executable MBSE. \n" +
		    		"It creates a nested package structure for executable 'interaction-based functional analysis',  \n" +
		    		"imports the appropriate profiles if not present, and sets default display and other options \n" +
		    		"to appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    	
		    	IRPModelElement theRequirementsAnalysisPkg = forProject.findElementsByFullName("RequirementsAnalysisPkg", "Package");
		    	
		    	if (theRequirementsAnalysisPkg==null){
		    		
				    int confirm = JOptionPane.showConfirmDialog(null, 
				    		"The project does not contain a root RequirementsAnalysisPkg. This package is used by the\n" +
				    		"plugin to populate the Actors for functional analysis simulation processes.\n\n" +
				    		"Do you want to add a RequirementsAnalysisPkg.sbs from another model by reference?", "Confirm",
				        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				    
				    if (confirm == JOptionPane.YES_OPTION){
				    	browseAndAddByReferenceIfNotPresent("RequirementsAnalysisPkg", forProject);
				    }
		    	}
		    	
		    	populateFunctionalAnalysisPkg(forProject);
		    	removeSimpleMenuStereotypeIfPresent(forProject);
		    	
		    	forProject.save();

		    } else {
		    	Logger.writeLine("Cancelled by user");
		    }
		}
	}
	
	static void populateFunctionalAnalysisPkg(IRPProject forProject) {
		
		addProfileIfNotPresent("SysML", forProject);		
		addProfileIfNotPresent("GlobalPreferencesProfile", forProject);
		addProfileIfNotPresent("RequirementsAnalysisProfile", forProject);
		addProfileIfNotPresent("FunctionalAnalysisProfile", forProject);
		
		forProject.changeTo("SysML");
		
		IRPPackage theFunctionalAnalysisPkg = addPackageFromProfileRpyFolder(forProject, "FunctionalAnalysisPkg");
		
		if (theFunctionalAnalysisPkg != null){
		
			deleteIfPresent( "Structure1", "StructureDiagram", forProject );
	    	deleteIfPresent( "Default", "Package", forProject );
	    	
	    	setProperty( forProject, "Browser.Settings.ShowPredefinedPackage", "True" );
	    	setProperty( forProject, "General.Model.AutoSaveInterval", "5" );
	    	setProperty( forProject, "General.Model.HighlightElementsInActiveComponentScope", "True" );
	    	setProperty( forProject, "General.Model.ShowModelTooltipInGE", "Enhanced" );
	    	setProperty( forProject, "General.Model.BackUps", "One" );
	    	
	    	createFunctionalBlockPackageHierarchy( theFunctionalAnalysisPkg );
		}
	}
	
	public static void createFunctionalBlockPackageHierarchy(IRPPackage theRootPackage){
		
		if (theRootPackage.getName().equals("FunctionalAnalysisPkg")){
			
			IRPPackage theRequirementsAnalysisPkg = (IRPPackage) theRootPackage.getProject().findElementsByFullName("RequirementsAnalysisPkg", "Package");
			
			if (theRequirementsAnalysisPkg == null){
				
				JDialog.setDefaultLookAndFeelDecorated(true);
				JOptionPane.showMessageDialog(null,  
			    		"This operation only works if the project contains a RequirementsAnalysisPkg.");
			} else {
				
				createFunctionalAnalysisPkg(theRootPackage, theRequirementsAnalysisPkg);
			}
		    
		} else {
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    JOptionPane.showMessageDialog(
		    		null,  
		    		"This operation only works if you right-click the FunctionalAnalysisPkg.",
		    		"Warning",
		    		JOptionPane.WARNING_MESSAGE);	    
		}
	}
	
	private static void createFunctionalAnalysisPkg(
			IRPPackage theRootPackage,
			IRPPackage theRequirementsAnalysisPkg) {
		
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

			List<IRPModelElement> theExistingBlocks = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype("Class", "LogicalSystem", theRootPackage);

			JPanel panel = new JPanel();

			panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			panel.add( new JLabel("What do you want to call the Block?") );

			JTextField theBlockNameTextField = new JTextField("LogicalSystem");
			panel.add( theBlockNameTextField );			

			panel.add( new JLabel("Inherit from:") );	
			RhapsodyComboBox blockChoice = new RhapsodyComboBox(theExistingBlocks, true);
			panel.add( blockChoice );

			List<ClassifierMappingInfo> theActorChoices = new ArrayList<ClassifierMappingInfo>();

			for (IRPModelElement theActor : theActors) {

				@SuppressWarnings("unchecked")
				List<IRPModelElement> theExistingActors = 
						theRootPackage.getNestedElementsByMetaClass("Actor", 1).toList();

				JCheckBox theActorCheckBox = new JCheckBox("Create actor called:");
				theActorCheckBox.setSelected(true);
				panel.add( theActorCheckBox );
				
				String theOriginalActorName = theActor.getName();
				String theProposedActorName = GeneralHelpers.toLegalClassName( theOriginalActorName );
				
				if (!theProposedActorName.equals( theOriginalActorName )){
					Logger.writeLine("Adjusted actor name '"+ theOriginalActorName + "' to legal name '" + theProposedActorName + "'" );
				}
				
				JTextField theActorNameTextField = new JTextField( theProposedActorName );
				panel.add( theActorNameTextField );		

				panel.add(new JLabel("Inherit from:"));
				RhapsodyComboBox actorCombo = new RhapsodyComboBox(theExistingActors, true);							
				panel.add( actorCombo );

				theActorChoices.add( new ClassifierMappingInfo(actorCombo, theActorCheckBox, theActorNameTextField) );
			}

			int choice = JOptionPane.showConfirmDialog(
					null, panel, "Create from an inherited behaviour?", JOptionPane.YES_NO_CANCEL_OPTION);

			if( choice==JOptionPane.OK_OPTION ){

				String theName = theBlockNameTextField.getText();

				IRPPackage theBlockPackage = theRootPackage.addNestedPackage( theName + "Pkg" );   	
				IRPClass theLogicalSystemBlock = theBlockPackage.addClass( theName );
				GeneralHelpers.applyExistingStereotype("LogicalSystem", theLogicalSystemBlock);
				theLogicalSystemBlock.changeTo("Block");

				IRPModelElement theChosenOne = blockChoice.getSelectedRhapsodyItem();

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

				for (ClassifierMappingInfo theInfo : theActorChoices) {

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
				IRPTag theTagForPackageUnderDev = theRootPackage.getTag( tagNameForPackageUnderDev );
				
				if (theTagForPackageUnderDev==null){
					Logger.writeLine("Error in setFunctionalAnalysisSettings, unable to find tag called " + tagNameForPackageUnderDev);
				} else {
					Logger.writeLine("Setting " + Logger.elementInfo(theTagForPackageUnderDev) 
							+ " owned by " + Logger.elementInfo(theRootPackage) + " to " 
							+ Logger.elementInfo(theBlockPackage));
					
					theRootPackage.setTagElementValue(theTagForPackageUnderDev, theBlockPackage);
				}
									
				IRPStatechartDiagram theStatechart = theLogicalSystemBlock.getStatechart().getStatechartDiagram();

				if (theStatechart != null){
					theStatechart.highLightElement();
					theStatechart.openDiagram();
				}
				
				// Create nested package for housing the ADs
				IRPPackage theWorkingPackage = theBlockPackage.addNestedPackage(theName + "Working" + "Pkg");
				
		    	addProfileIfNotPresentAndMakeItApplied("RequirementsAnalysisProfile", theWorkingPackage);

				copyActivityDiagramsForEachUseCase(theRequirementsAnalysisPkg, theWorkingPackage);
			
				theProject.save();
				
				// Add a component
				addAComponentWith(theName, theBlockTestPackage, theUsageDomainBlock);
			}
		}
	}
	
	public static void addGeneralization(IRPClassifier fromElement, String toBlockWithName, IRPPackage underneathTheRootPackage){
		
		IRPModelElement theBlock = underneathTheRootPackage.findNestedElementRecursive(toBlockWithName, "Block");
		
		if (theBlock != null){
			fromElement.addGeneralization( (IRPClassifier) theBlock );
		} else {
			Logger.writeLine("Error: Unable to find element with name " + toBlockWithName);
		}
	}
	
	private static IRPInstance addPartTo(IRPClassifier theElement, IRPClassifier typedByElement){
		
		IRPInstance thePart = (IRPInstance) theElement.addNewAggr("Part", "its" + typedByElement.getName());
		thePart.setOtherClass(typedByElement);
		
		return thePart;
	}
	
	public static void createSequenceDiagramFor(IRPClass theUsageDomainBlock, String withName){
		
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
	
	public static void addActorPartTo(IRPClass theUsageBlock){
		
		String theActorName = GeneralHelpers.promptUserForTextEntry("Enter name","Actor:","",12);
		addActorPartTo(theUsageBlock, theActorName); 
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
	
	
	@SuppressWarnings("unchecked")
	public static void copyActivityDiagramsForEachUseCase(
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
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theProperties = theNote.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theProperties) {
			Logger.writeLine(theGraphicalProperty.getKey() + ","+ theGraphicalProperty.getValue());
		}
		
		theNote.setGraphicalProperty("Text", "This working copy of the use case steps can be used to generate the state machine.");
		
		theNewFlowchart.highLightElement();
		theNewFlowchart.getFlowchartDiagram().openDiagram();
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #008 05-MAY-2016: Fix the OMROOT problem with add profile functionality
    #010 08-MAY-2016: Remove white-space from actor names (F.J.Chadburn)
    #014 10-MAY-2016: Fix Component/Configuration creation to include derived and web-enabled settings (F.J.Chadburn)
    #018 11-MAY-2016: Provide advisory before add by reference of an external RequirementsAnalysisPkg (F.J.Chadburn)

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
