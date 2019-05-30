package functionaldesignplugin;

import functionalanalysisplugin.CreateOperationPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import requirementsanalysisplugin.NestedActivityDiagram;
import sysmlhelperplugin.ElementMover;
import sysmlhelperplugin.RequirementMover;

import com.telelogic.rhapsody.core.*;

public class FunctionalDesign_RPApplicationListerner extends RPApplicationListener {
	
	public FunctionalDesign_RPApplicationListerner(IRPApplication app) {
		Logger.writeLine("SysMLHelperPlugin is Loaded - Listening for Events\n"); 
	}
	
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		afterAddForCallOperation( (IRPCallOperation) theSelectedEl );
		
	}
	
	public boolean afterAddElement(
			IRPModelElement modelElement) {

		boolean doDefault = false;
		
		try {
			StereotypeAndPropertySettings.setSavedInSeparateDirectoryIfAppropriateFor( 
					modelElement );
			
			if( modelElement != null && 
				modelElement instanceof IRPRequirement ){
				
				afterAddForRequirement( modelElement );
				
			} else if( modelElement != null && 
					modelElement instanceof IRPClass && 
					GeneralHelpers.hasStereotypeCalled( "Interface", modelElement )){
				
				Logger.writeLine("Interface=" + Logger.elementInfo(modelElement));
				afterAddForInterface( modelElement );
										
			} else if (modelElement != null && 
				modelElement instanceof IRPDependency && 
				modelElement.getUserDefinedMetaClass().equals("Derive Requirement")){
				
				afterAddForDeriveRequirement( (IRPDependency) modelElement );
				
			} else if (modelElement != null && 
					modelElement instanceof IRPCallOperation ){
				
				afterAddForCallOperation( (IRPCallOperation) modelElement );
			}

		} catch( Exception e ){
			Logger.writeLine("Error in SysMLHelperTriggers.afterAddElement, unhandled exception was detected related to " + Logger.elementInfo(modelElement));
		}

		return doDefault;
	}

	private static void afterAddForCallOperation(
			IRPCallOperation theCallOp ){

		// only do move if property is set
		boolean isEnabled = 
				StereotypeAndPropertySettings.getIsCallOperationSupportEnabled(
						theCallOp );
		
		if( isEnabled && UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){

			IRPApplication theRhpApp = 
					RhapsodyAppServer.getActiveRhapsodyApplication();

			IRPInterfaceItem theOp = theCallOp.getOperation();

				IRPDiagram theDiagram = theRhpApp.getDiagramOfSelectedElement();

				if( theDiagram != null ){ 
					CreateOperationPanel.launchThePanel();
				} // else probably drag from browser

			if( theOp != null ){

				// Use the operation name for the COA if possible
				try {
					String theProposedName = 
							GeneralHelpers.determineUniqueNameBasedOn( 
									GeneralHelpers.toMethodName( theOp.getName(), 40 ), 
									"CallOperation", 
									theCallOp.getOwner() );

					theCallOp.setName( theProposedName );

				} catch( Exception e ) {
					Logger.writeLine( theCallOp, "Error: Cannot rename Call Operation to match Operation" );
				}

				// If the operation has an Activity Diagram under it, then populate an RTF 
				// description with a link to the lower diagram
				IRPFlowchart theAD = theOp.getActivityDiagram();

				if( theAD != null ){

					IRPActivityDiagram theFC = theAD.getFlowchartDiagram();

					if( theFC != null ){
						Logger.writeLine("Creating Hyperlinks in Description of COA");

						IRPCollection targets = theRhpApp.createNewCollection();

						targets.setSize( 2 );
						targets.setModelElement( 1, theOp );
						targets.setModelElement( 2, theFC );

						String rtfText = "{\\rtf1\\fbidis\\ansi\\ansicpg1255\\deff0\\deflang1037{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}\n{\\colortbl;\\red0\\green0\\blue255;}\n\\viewkind4\\uc1 " + 
								"\\pard\\ltrpar\\qc\\fs18 Function: \\cf1\\ul\\protect " + theOp.getName() + "\\cf0\\ulnone\\protect0\\par" + 
								"\\pard\\ltrpar\\qc\\fs18 Decomposed by: \\cf1\\ul\\protect " + theFC.getName() + "\\cf0\\ulnone\\protect0\\par\n}";

						theCallOp.setDescriptionAndHyperlinks( rtfText, targets );
					}
				}
			}
		}

	}

	private void afterAddForDeriveRequirement(
			IRPDependency theDependency ){
				
		IRPModelElement theDependent = theDependency.getDependent();
		
		if (theDependent instanceof IRPRequirement){
			
			IRPStereotype theExistingGatewayStereotype = getStereotypeAppliedTo(theDependent, "from.*");
			
			if (theExistingGatewayStereotype != null){

				theDependency.setStereotype(theExistingGatewayStereotype);
				theDependency.changeTo("Derive Requirement");
			}			
		}
	}

	private void afterAddForInterface(
			IRPModelElement modelElement ){
		
		// only do move if property is set
		boolean isEnabled = 
				StereotypeAndPropertySettings.getIsEnableAutoMoveOfInterfaces(
						modelElement );
		
		if( isEnabled ){
			ElementMover theElementMover = new ElementMover( 
					modelElement, 
					StereotypeAndPropertySettings.getInterfacesPackageStereotype( modelElement ) );
			
			theElementMover.performMove();
		}
	}

	private void afterAddForRequirement(
			IRPModelElement modelElement ){
		
		// only do move if property is set
		boolean isEnabled = 
				StereotypeAndPropertySettings.getIsEnableAutoMoveOfRequirements(
						modelElement );
		
		if( isEnabled ){
			RequirementMover theElementMover = new RequirementMover( modelElement );
			
			if( theElementMover.isMovePossible() ){
				theElementMover.performMove();
			}
		}
	}
	
	public static IRPStereotype getStereotypeAppliedTo(
			IRPModelElement theElement, 
			String thatMatchesRegEx){
		
		IRPStereotype foundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStereotype> theStereotypes = theElement.getStereotypes().toList();
		
		int count=0;
		
		for (IRPStereotype theStereotype : theStereotypes) {
			
			count++;
			
			String theName = theStereotype.getName();
			
			if (theName.matches(thatMatchesRegEx)){
				foundStereotype = theStereotype;
				
				if (count > 1){
					Logger.writeLine("Error in getStereotypeAppliedTo related to " + Logger.elementInfo(theElement) + " count=" + count);
				}
			}		
		}
		
		return foundStereotype;
	}
	
	@Override
	public boolean afterProjectClose(String bstrProjectName) {
		return false;
	}

	@Override
	public boolean onDoubleClick(IRPModelElement pModelElement) {
		
		boolean theReturn = false;
		
		try {	
			List<IRPModelElement> optionsList = null;
			
			if (pModelElement instanceof IRPCallOperation) {
			
				IRPCallOperation theCallOp = (IRPCallOperation)pModelElement;
				
				IRPInterfaceItem theInterfaceItem = theCallOp.getOperation();
			
				if (theInterfaceItem instanceof IRPOperation){
					
					IRPOperation theOp = (IRPOperation)theInterfaceItem;
					
					optionsList = getDiagramsFor( theOp );
				}
				
			} else if (pModelElement instanceof IRPInstance){
				
				IRPInstance thePart = (IRPInstance)pModelElement;
				
				IRPClassifier theClassifier = thePart.getOtherClass();
				
				if( theClassifier != null ){
					optionsList = getDiagramsFor( theClassifier );
				}
			}
			
			if (optionsList == null){
				optionsList = getDiagramsFor(pModelElement);
			}
			
			int numberOfDiagrams = optionsList.size();
			
			if (pModelElement instanceof IRPClass &&
					(pModelElement.getUserDefinedMetaClass().equals("Function Block") ||
							pModelElement.getUserDefinedMetaClass().equals("Sequence Block") ) ){

				final String isCreateAD = "Create nested Activity Diagram";
				final String isCreateSD = "Create nested Sequence Diagram";
				final String isCreateSTM = "Create nested Statechart";

				Object[] theOptions = { isCreateAD, isCreateSD, isCreateSTM };
				
				Logger.writeLine("User chose to create a new activity diagram");
				JList<?> list = new JList<Object>( theOptions );
				
		        list.setSelectionMode(
		            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		        
		        JOptionPane pane = new JOptionPane( new JScrollPane( list )  );
		        
		        pane.add( new JLabel( "" ) );
		        pane.add( new JLabel( "The element already has " + optionsList.size() + " related diagrams." ) );
		        pane.add( new JLabel( "" ) );
		        
		        JDialog d = pane.createDialog( 
		        		null, 
		        		"Choose an option" );
		        
		        d.setVisible(true);

//		        int selection = getSelection(pane);
		        
		        List<?> theValues = list.getSelectedValuesList();
		        
		        for( Object theValue : theValues ){
		        	
		        	Logger.writeLine("Value is " + theValue.toString() );
		        	
					if( theValue.equals( isCreateAD ) ){
						
						NestedActivityDiagram.createNestedActivityDiagram( 
								(IRPClassifier)pModelElement, 
								"AD - " + pModelElement.getName(), 
								"SysMLHelper.FunctionalDesign.TemplateForActivityDiagram" );
						
					} else if( theValue.equals( isCreateSD ) ){
												
						createNestedDiagram( 
								(IRPClassifier) pModelElement, 
								"SD - " + pModelElement.getName(),
								"SequenceDiagram" );
						
					} else if( theValue.equals( isCreateSTM ) ){
						
						createNestedStatechartDiagram( 
								(IRPClassifier) pModelElement, 
								"STM - " + pModelElement.getName() );						
					}
				}
		        
				//CreateBehaviourDiagram.launchTheDialog();
				theReturn = true; // don't launch the Features  window	
				
			} else if (numberOfDiagrams > 0){
				
				theReturn = openNestedDiagramDialogFor( optionsList, pModelElement);
				
			} else if (pModelElement instanceof IRPUseCase){
				
				String theUnadornedName = "AD - " + pModelElement.getName();
				
				boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"This use case has no nested text-based Activity Diagram.\n"+
						"Do you want to create one called '" + theUnadornedName + "'?");

				if (theAnswer==true){
					Logger.writeLine("User chose to create a new activity diagram");
					
					NestedActivityDiagram.createNestedActivityDiagram( 
							(IRPClassifier)pModelElement, 
							"AD - " + pModelElement.getName(), 
							"SysMLHelper.RequirementsAnalysis.TemplateForActivityDiagram" );
				}

				theReturn = true; // don't launch the Features  window									

			} else {

				theReturn = false; // do default, i.e. open the features dialog
			}	

		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in onDoubleClick()");			
		}
		
		return theReturn;
	}
	
	private static Set<IRPDiagram> getHyperLinkDiagramsFor(
			IRPModelElement theElement){
		
		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();
		
		@SuppressWarnings("unchecked")
		List<IRPHyperLink> theHyperLinks = theElement.getHyperLinks().toList();
		
		for (IRPHyperLink theHyperLink : theHyperLinks) {
			
			IRPModelElement theTarget = theHyperLink.getTarget();
			
			if (theTarget != null){
				
				if (theTarget instanceof IRPDiagram){
					theDiagrams.add( (IRPDiagram) theTarget );
					
				} else if (theTarget instanceof IRPFlowchart){
					IRPFlowchart theFlowchart = (IRPFlowchart)theTarget;
					theDiagrams.add( theFlowchart.getStatechartDiagram() );
					
				} else if (theTarget instanceof IRPStatechart){
					IRPStatechart theStatechart = (IRPStatechart)theTarget;
					theDiagrams.add( theStatechart.getStatechartDiagram() );			
				}
			}
		}
		
		return theDiagrams;
	}
	
	private static Set<IRPDiagram> getNestedDiagramsFor(
			IRPModelElement pModelElement) {
		
		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theNestedElements = pModelElement.getNestedElementsRecursive().toList();
		
		for (IRPModelElement theNestedElement : theNestedElements) {
			
			if (theNestedElement instanceof IRPDiagram){
				theDiagrams.add( (IRPDiagram) theNestedElement );
			}
		}

		return theDiagrams;
	}

	@Override
	public boolean onFeaturesOpen(IRPModelElement pModelElement) {
		return false;
	}

	@Override
	public boolean onSelectionChanged() {
		return false;
	}

	@Override
	public boolean beforeProjectClose(IRPProject pProject) {
		return false;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public boolean onDiagramOpen(IRPDiagram pDiagram) {
		return false;
	}
	
	private boolean openNestedDiagramDialogFor(
			List<IRPModelElement> theListOfDiagrams, 
			IRPModelElement relatedToModelEl) {
		
		boolean theReturn = false;
		
		try {		
			int numberOfDiagrams = theListOfDiagrams.size();
			
			if( numberOfDiagrams==1 ){
				
				IRPDiagram theDiagramToOpen = (IRPDiagram) theListOfDiagrams.get(0);
				
				if (theDiagramToOpen != null){

					String theType = theDiagramToOpen.getUserDefinedMetaClass();
					String theName = theDiagramToOpen.getName();
					
					if (theDiagramToOpen instanceof IRPFlowchart){
					
						theDiagramToOpen = (IRPDiagram) theDiagramToOpen.getOwner();

						
					} else if (theDiagramToOpen instanceof IRPActivityDiagram){
						
						theType = theDiagramToOpen.getOwner().getUserDefinedMetaClass();
						theName = theDiagramToOpen.getOwner().getName();	
					}
					
					JDialog.setDefaultLookAndFeelDecorated(true);
					
					int confirm = JOptionPane.showConfirmDialog(null, 
							"The " + relatedToModelEl.getUserDefinedMetaClass() + " called '" +
							relatedToModelEl.getName() + "' has an associated " + theType + "\n" +
							"called '" + theName + "'.\n\n" +
							"Do you want to open it? (Click 'No' to open the Features dialog instead)\n\n",
				    		"Confirm choice",
				        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				    
				    if (confirm == JOptionPane.YES_OPTION){
						
						theDiagramToOpen.openDiagram();	
						theReturn = true; // don't launch the Features  window
						
					} else if (confirm == JOptionPane.NO_OPTION){
						
						theReturn = false; // open the features dialog
						
					} else { // Cancel
						
						theReturn = true; // don't open the features dialog
					}
				}
				
			} else if ( numberOfDiagrams>1 ){
				
				IRPModelElement theSelection = UserInterfaceHelpers.launchDialogToSelectElement(
						theListOfDiagrams, 
						"The " + relatedToModelEl.getUserDefinedMetaClass() + " called '" +
						relatedToModelEl.getName() + "' has " + numberOfDiagrams + " associated diagrams.\n\n" +
						"Which one do you want to open? (Click 'Cancel' to open Features dialog instead)\n",
						true);
				
				if (theSelection != null && theSelection instanceof IRPDiagram){

					IRPDiagram theDiagramToOpen = (IRPDiagram) theSelection;
					theDiagramToOpen.openDiagram();
					theReturn = true; // don't launch the Features  window
				}
			}

		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in onDoubleClick()");			
		}

		return theReturn;
	}

	private List<IRPModelElement> getDiagramsFor(
			IRPModelElement theModelEl){

		Set<IRPDiagram> allDiagrams = new HashSet<IRPDiagram>();

		Set<IRPDiagram> theHyperLinkedDiagrams = getHyperLinkDiagramsFor(theModelEl);	
		allDiagrams.addAll(theHyperLinkedDiagrams);

		Set<IRPDiagram> theNestedDiagrams = getNestedDiagramsFor(theModelEl);
		allDiagrams.addAll(theNestedDiagrams);

		List<IRPModelElement> optionsList = new ArrayList<IRPModelElement>();
		optionsList.addAll( allDiagrams );

		return optionsList;
	}	


	public static int getSelection(JOptionPane optionPane) {
		int returnValue = JOptionPane.OK_CANCEL_OPTION;

		Object selectedValue = optionPane.getValue();
		if (selectedValue != null) {
			Object options[] = optionPane.getOptions();
			if (options == null) {
				if (selectedValue instanceof Integer) {
					returnValue = ((Integer) selectedValue).intValue();
				}
			} else {
				for (int i = 0, n = options.length; i < n; i++) {
					if (options[i].equals(selectedValue)) {
						returnValue = i;
						break; // out of for loop
					}
				}
			}
		}
		return returnValue;
	}

	public static IRPDiagram createNestedDiagram(
			IRPClassifier pModelElement, 
			String withDesiredName,
			String andMetaClass ){

		String theName = 
				GeneralHelpers.determineUniqueNameBasedOn(
						withDesiredName, andMetaClass, pModelElement );

		Logger.writeLine("Creating a new " + andMetaClass + " with the name '" 
				+ theName + "' underneath " + Logger.elementInfo( pModelElement ) );

		IRPDiagram theDiagram = null;

		try {
			theDiagram = (IRPDiagram) pModelElement.addNewAggr( 
					andMetaClass,
					theName );
			
			Logger.writeLine( "Diagram is a " + theDiagram.getUserDefinedMetaClass() );
			
			theDiagram.openDiagram();
			theDiagram.highLightElement();
			
		} catch (Exception e) {
			Logger.writeLine("Error trying to create diagram, e=" + e.getLocalizedMessage());
		}
		
		return theDiagram;
	}


	public static IRPDiagram createNestedStatechartDiagram(
			IRPClassifier pModelElement, 
			String withDesiredName ){

		String theName = 
				GeneralHelpers.determineUniqueNameBasedOn(
						withDesiredName, "Statechart", pModelElement );

		Logger.writeLine("Creating a new " + "Statechart" + " with the name '" 
				+ theName + "' underneath " + Logger.elementInfo( pModelElement ) );

		IRPStatechart theDiagram = null;

		try {
			theDiagram = pModelElement.addStatechart();
			
			Logger.writeLine( "Diagram is a " + theDiagram.getUserDefinedMetaClass() );
			
			theDiagram.createGraphics();
			theDiagram.getStatechartDiagram().openDiagram();
			theDiagram.setName( theName );
			theDiagram.highLightElement();

		} catch (Exception e) {
			Logger.writeLine("Error trying to create diagram, e=" + e.getLocalizedMessage());
		}
		
		return theDiagram.getStatechartDiagram();
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #250 29-MAY-2019: First official version of new FunctionalDesignProfile  (F.J.Chadburn)

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