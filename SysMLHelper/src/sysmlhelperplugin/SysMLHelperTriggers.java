package sysmlhelperplugin;

import functionalanalysisplugin.CreateOperationPanel;
import functionalanalysisplugin.FunctionalAnalysisSettings;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.NestedActivityDiagram;

import com.telelogic.rhapsody.core.*;

public class SysMLHelperTriggers extends RPApplicationListener {

	IRPApplication theApp = null;
	
	public SysMLHelperTriggers(IRPApplication app) {
		Logger.writeLine("SysMLHelperPlugin is Loaded - Listening for Events\n"); 
		theApp = app;
	}

	public boolean afterAddElement(
			IRPModelElement modelElement) {

		boolean doDefault = false;
		
		try {
			if (modelElement != null && 
				modelElement instanceof IRPDependency && 
				modelElement.getUserDefinedMetaClass().equals("Derive Requirement")){
				
				IRPDependency theDependency = (IRPDependency)modelElement;
				Logger.writeLine(modelElement, "was found");
				
				IRPModelElement theDependent = theDependency.getDependent();
				
				if (theDependent instanceof IRPRequirement){
					
					IRPStereotype theExistingGatewayStereotype = getStereotypeAppliedTo(theDependent, "from.*");
					
					if (theExistingGatewayStereotype != null){
						
						modelElement.setStereotype(theExistingGatewayStereotype);
						modelElement.changeTo("Derive Requirement");
					}			
				}
			} else if (modelElement != null && 
					modelElement instanceof IRPCallOperation){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theSelectedGraphEls = 
					theApp.getSelectedGraphElements().toList();
				
				IRPCallOperation theCallOp = (IRPCallOperation)modelElement;
				IRPInterfaceItem theOp = theCallOp.getOperation();
				
				if( theOp==null ){
					IRPClass theBlock = FunctionalAnalysisSettings.getBlockUnderDev(
							modelElement.getProject() );
					
					if (theBlock != null){
						boolean answer = UserInterfaceHelpers.askAQuestion("Do you want to add an Operation to " + 
								Logger.elementInfo( theBlock ) + "?");
						
						if( answer==true ){
							
							Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
							
							CreateOperationPanel.launchThePanel( 
									theSelectedGraphEls.get(0), 
									theReqts, 
									theApp.activeProject(),
									true );
						}
					}
				} // Operation already exists, i.e. element was dragged on so do nothing
			}

		} catch (Exception e) {
			Logger.writeLine("Error in SysMLHelperTriggers.afterAddElement, unhandled exception was detected related to " + Logger.elementInfo(modelElement));
		}

		return doDefault;
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
			
			if (numberOfDiagrams > 0){
				
				theReturn = openNestedDiagramDialogFor( optionsList, pModelElement);
				
			} else if (pModelElement instanceof IRPUseCase){
				
				String theUnadornedName = "AD - " + pModelElement.getName();
				
				boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"This use case has no nested text-based Activity Diagram.\n"+
						"Do you want to create one called '" + theUnadornedName + "'?");

				if (theAnswer==true){
					Logger.writeLine("User chose to create a new activity diagram");
					NestedActivityDiagram.createNestedActivityDiagram( 
							(IRPUseCase)pModelElement, "AD - " + pModelElement.getName());
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
					
					boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"The " + relatedToModelEl.getUserDefinedMetaClass() + " called '" +
								relatedToModelEl.getName() + "' has an \nassociated " + theType + " called '" + theName + "'.\n" +
						"Do you want to open it?");
					
					if (theAnswer==true){
						
						theDiagramToOpen.openDiagram();	
						theReturn = true; // don't launch the Features  window
						
					} else {
						theReturn = false; // open the features dialog
					}
				}
				
			} else if ( numberOfDiagrams>1 ){
				
				IRPModelElement theSelection = UserInterfaceHelpers.launchDialogToSelectElement(
						theListOfDiagrams, 
						"The " + relatedToModelEl.getUserDefinedMetaClass() + " called '" +
								relatedToModelEl.getName() + "' has " + numberOfDiagrams + " associated diagrams.\n" +
						"Which one do you want to open?",
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
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #001 31-MAR-2016: Added ListenForRhapsodyTriggers (F.J.Chadburn)
    #003 09-APR-2016: Added double-click UC to open ACT (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #017 11-MAY-2016: Double-click now works with both nested and hyper-linked diagrams (F.J.Chadburn)
    #035 15-JUN-2016: Re-factored SysMLHelperTriggers to make a little more extensible (F.J.Chadburn)
    #058 13-JUL-2016: Dropping CallOp on diagram now gives option to create Op on block (F.J.Chadburn)
    #068 19-JUL-2016: Newline added to Open diagram dialog (F.J.Chadburn)
    #075 28-JUL-2016: Fix unintended pop-up of Features dialog when opening diagrams with double-click (F.J.Chadburn)
    #076 28-JUL-2016: Support IBD drill down by making double-click of part consider diagrams of Block (F.J.Chadburn)
    #079 28-JUL-2016: Improved robustness of post add CallOp behaviour to prevent Rhapsody hanging (F.J.Chadburn)
    #080 28-JUL-2016: Added activity diagram name to the create AD dialog for use cases (F.J.Chadburn)
    #081 28-JUL-2016: Dragging a CallOp on to diagram should not ask to add a new one (F.J.Chadburn)
    
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
