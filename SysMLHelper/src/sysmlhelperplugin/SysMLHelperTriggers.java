package sysmlhelperplugin;

import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.List;

import requirementsanalysisplugin.NestedActivityDiagram;

import com.telelogic.rhapsody.core.*;

public class SysMLHelperTriggers extends RPApplicationListener {

	public SysMLHelperTriggers(IRPApplication app) {
		Logger.writeLine("SysMLHelperPlugin is Loaded - Listening for Events\n"); 
	}

	public boolean afterAddElement(IRPModelElement modelElement) {
		
		boolean doDefault = false;

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
		}

		return doDefault;
	}
	
	public static IRPStereotype getStereotypeAppliedTo(IRPModelElement theElement, String thatMatchesRegEx){
		
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
			
			if (pModelElement instanceof IRPUseCase){
				
				Logger.writeLine("The " + Logger.elementInfo(pModelElement) + " was double-clicked");
				
				IRPUseCase theUseCase = (IRPUseCase)pModelElement;
				
				@SuppressWarnings("unchecked")
				List<IRPModelElement> theADs = theUseCase.getNestedElementsByMetaClass( "ActivityDiagram", 0).toList();
				
				if (theADs.isEmpty()){
					boolean theAnswer = UserInterfaceHelpers.askAQuestion("This use case has no nested Activity Diagram. Do you want to create one?");
					
					if (theAnswer==true){
						Logger.writeLine("User chose to create a new activity diagram");
						NestedActivityDiagram.createNestedActivityDiagram(theUseCase);
					}
					
					theReturn = true; // don't launch the Features  window
					
				} else if (theADs.size()==1){
					
					IRPFlowchart theAD = (IRPFlowchart) theADs.get( 0 );
					
					boolean theAnswer = UserInterfaceHelpers.askAQuestion("The use case has a nested " + Logger.elementInfo(theAD) + ". \n\nDo you want to open it?");
					
					if (theAnswer==true){
						
						IRPStatechartDiagram theStatechart = theAD.getStatechartDiagram();
						theStatechart.openDiagram();
						theReturn = true; // don't launch the Features  window
						
					} else {
						theReturn = false; // open the eatures dialog
					}

				} else {
					
					IRPModelElement theSelection = UserInterfaceHelpers.launchDialogToSelectElement(
							theADs, 
							"This use case has " + theADs.size() + " nested Activity Diagrams.\n" +
							"Which one do you want to open?",
							false);
					
					IRPFlowchart theAD = (IRPFlowchart) theSelection;
					IRPStatechartDiagram theStatechart = theAD.getStatechartDiagram();
					theStatechart.createGraphics();
					theStatechart.openDiagram();
					
					theReturn = true; // don't launch the Features  window
				}
			}
		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in onDoubleClick()");
			
		}
		
		return theReturn;
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
	
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #001 31-MAR-2016: Added ListenForRhapsodyTriggers (F.J.Chadburn)
    #003 09-APR-2016: Added double-click UC to open ACT (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    
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
