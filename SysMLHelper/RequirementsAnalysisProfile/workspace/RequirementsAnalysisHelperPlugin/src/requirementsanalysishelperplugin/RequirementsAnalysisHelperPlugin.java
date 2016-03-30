/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited

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

package requirementsanalysishelperplugin;

import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class RequirementsAnalysisHelperPlugin extends RPUserPlugin {

	protected IRPApplication m_rhpApplication = null;

	String version = "1.0 (Release)";
	
	// plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		
		m_rhpApplication = theRhapsodyApp;
		
		String msg = "The RequirementsAnalysisHelper plugin V" + version + " was loaded successfully. New right-click 'MBSE Method' commands have been added.";		
		Logger.writeLine(msg); 
	}

	// For use with Tables
	public String getRequirementSpecificationText(String guid) {
	
		Logger.writeLine("Was invoked with guid " + guid);
		String theSpec = "Not found";
		
		IRPModelElement theEl = m_rhpApplication.activeProject().findElementByGUID(guid);
		
		if (theEl != null){
			
			if (theEl instanceof IRPRequirement){
				
				IRPRequirement theReq = (IRPRequirement)theEl;
				theSpec = theReq.getSpecification();
				
			} else if (theEl instanceof IRPDependency){
				
				IRPDependency theDep = (IRPDependency)theEl;
				IRPModelElement theDependsOn = theDep.getDependsOn();
				
				if (theDependsOn instanceof IRPRequirement){
					
					IRPRequirement theReq = (IRPRequirement)theDependsOn;
					theSpec = theReq.getSpecification();
					
					Logger.writeLine(theDependsOn, "is the depends on with the text '" + theSpec + "'");
				}
			}
		} else {
			Logger.writeLine("Error: getRequirementSpecificationText unable to find element with guid=" + guid);
		}

		return theSpec;
	}
	
	// called when the plug-in pop-up menu (if applicable) is selected
	public void OnMenuItemSelect(String menuItem) {
	
		IRPModelElement theSelectedEl = m_rhpApplication.getSelectedElement();
		IRPProject theActiveProject = m_rhpApplication.activeProject();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = m_rhpApplication.getSelectedGraphElements().toList();
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = m_rhpApplication.getListOfSelectedElements().toList();

		Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");
		
		if( !theSelectedEls.isEmpty() ){
			//selElemName = theSelectedEl.getName();	
						
			if (menuItem.equals("MBSE Method: Requirements Analysis\\Create nested Activity Diagram for this use case")){

				try {
					NestedActivityDiagram.createNestedActivityDiagramsFor( theSelectedEls );

				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking NestedActivityDiagram.createNestedActivityDiagramsFor");
				}

			} else if (menuItem.equals("MBSE Method: Requirements Analysis\\Report on naming and traceability checks for elements on Activity Diagram")){

				try {
					ActivityDiagramChecker.createActivityDiagramCheckersFor( theSelectedEls );
					
				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking new ActivityDiagramChecker.createActivityDiagramCheckersFor");
				}

				
			} else if (menuItem.equals("MBSE Method: Requirements Analysis\\Move unclaimed requirements ready for Gateway sync back to DOORS")){

				try {
					MoveRequirements.moveUnclaimedRequirementsReadyForGatewaySync( theSelectedEls, theActiveProject );

				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MoveRequirements.moveUnclaimedRequirementsReadyForGatewaySync");
				}

				
			} else if (menuItem.equals("MBSE Method: Requirements Analysis\\Create a new requirement")){

				try {
					Logger.writeLine("Creating new requirements");
					RequirementsHelper.createNewRequirementsFor( theSelectedGraphEls );

				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RequirementsHelper.createNewRequirementsFor");
				}

			} else if (menuItem.equals("MBSE Method: Requirements Analysis\\Perform rename in browser for elements on Activity Diagrams")){

				try {				
					RenameActions.performRenamesFor( theSelectedEls );

				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RenameActions.performRenamesFor");
				}

			} else {
				Logger.writeLine(theSelectedEl, " was invoked with menuItem='" + menuItem + "'");
			}
		} // else No selected element

		Logger.writeLine("... completed");
	}
	
	public boolean RhpPluginCleanup() {
		m_rhpApplication = null;
		return true; // true=unload plugin
	}

	@Override
	public void RhpPluginInvokeItem() {		
	}

	@Override
	public void RhpPluginFinalCleanup() {		
	}

	@Override
	public void OnTrigger(String trigger) {		
	}
}
