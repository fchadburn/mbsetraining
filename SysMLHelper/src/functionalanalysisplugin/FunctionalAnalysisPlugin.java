package functionalanalysisplugin;

import generalhelpers.Logger;

import java.util.List;

import sysmlhelperplugin.SysMLHelperPlugin;

import com.telelogic.rhapsody.core.*;
 
public class FunctionalAnalysisPlugin extends RPUserPlugin {
  
	protected static IRPApplication m_rhpApplication = null;
	protected static IRPProject m_rhpProject = null;

	// plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		
		m_rhpApplication = theRhapsodyApp;
		
		String msg = "The FunctionalAnalysisPlugin component of the SysMLHelperPlugin V" + SysMLHelperPlugin.getVersion() 
				+ " was loaded successfully. New right-click 'MBSE Method' commands have been added.";		
		
		Logger.writeLine(msg); 
	}

	public static IRPApplication getRhapsodyApp(){
		
		if (m_rhpApplication==null){
			m_rhpApplication = RhapsodyAppServer.getActiveRhapsodyApplication();
		}
		
		return m_rhpApplication;
	}
	
	public static IRPProject getActiveProject(){
		
		if (m_rhpProject==null){
			m_rhpProject = getRhapsodyApp().activeProject();
		}
		
		return m_rhpProject;
	}

	
	// called when the plug-in pop-up menu (if applicable) is selected
	public void OnMenuItemSelect(String menuItem) {
	
		IRPModelElement theSelectedEl = getRhapsodyApp().getSelectedElement();
		IRPProject theActiveProject = getRhapsodyApp().activeProject();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = getRhapsodyApp().getSelectedGraphElements().toList();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = getRhapsodyApp().getListOfSelectedElements().toList();

		Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");
		
		if( !theSelectedEls.isEmpty() ){
			//selElemName = theSelectedEl.getName();	
						
			if (menuItem.equals("MBSE Method: Functional Analysis\\Populate package hierarchy for an analysis block")){

				try {
					if (theSelectedEl instanceof IRPPackage){
						PopulateFunctionalAnalysisPkg.createFunctionalBlockPackageHierarchy( (IRPPackage)theSelectedEl );
					}

				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking NestedActivityDiagram.createNestedActivityDiagramsFor");
				}

			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Create an incoming event sent by an actor")){

				if (!theSelectedGraphEls.isEmpty()){
					try {
						CreateIncomingEventPanel.createIncomingEventsFor( theActiveProject, theSelectedGraphEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking OperationCreator.createIncomingEventsFor");
					}
				}

			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Create an operation that the system does")){

				if (!theSelectedGraphEls.isEmpty()){
					try {
						CreateOperationPanel.createSystemOperationsFor( theActiveProject, theSelectedGraphEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking OperationCreator.createSystemOperationsFor");
					}
				}
				
			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Create an outgoing event sent to an actor")){

				if (!theSelectedGraphEls.isEmpty()){
					try {
						CreateOutgoingEventPanel.createOutgoingEventsFor( theActiveProject, theSelectedGraphEls );
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking OperationCreator.createOutgoingEventsFor");
					}
				}

			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Create stand-alone attribute owned by the system")){

				if (!theSelectedGraphEls.isEmpty()){
					try {
						CreateTracedAttributePanel.createSystemAttributesFor( theActiveProject, theSelectedGraphEls );
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking CreateTracedAttributePanel.createSystemAttributeFor");
					}
				}
				
			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Create new TestCase for «TestDriver»")){

				if (theSelectedEl instanceof IRPClass){
					try {
						OperationCreator.createTestCaseFor( (IRPClass) theSelectedEl );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking OperationCreator.createTestCaseFor");
					}
				} 

			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Add new actor to package under development")){

				if (theSelectedEl instanceof IRPPackage){
					try {
						PopulateFunctionalAnalysisPkg.addNewActorToPackageUnderDevelopement( theSelectedEl ); 

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.addNewActorToPackageUnderDevelopement");
					}
				}
				
			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Copy activity diagrams to package under development")){

				if (theSelectedEl instanceof IRPPackage){
					try {
						PopulateFunctionalAnalysisPkg.copyActivityDiagrams( theActiveProject ); 

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.addNewActorToPackageUnderDevelopement");
					}
				}							

			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Populate requirements for SD(s) based on messages")){

				if (!theSelectedEls.isEmpty()){
					try {
						SequenceDiagramHelper.populateRequirementsForSequenceDiagramsBasedOn( theSelectedEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking SequenceDiagramHelper.populateRequirementsForSequenceDiagramsBasedOn");
					}
				}
				
			} else if (menuItem.equals("MBSE Method: Functional Analysis\\Update Verification dependencies for SD(s) based on populated requirements")){

				if (!theSelectedEls.isEmpty()){
					try {
						SequenceDiagramHelper.updateVerificationsForSequenceDiagramsBasedOn( theSelectedEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking SequenceDiagramHelper.updateVerificationsForSequenceDiagramsBasedOn");
					}
				}				
			} else if (menuItem.equals("MBSE Method: Delete Event and related elements from Model")){

				try {
					EventDeletion.deleteEventAndRelatedElementsFor( theSelectedEls );
					
				} catch (Exception e) {
					Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking EventDeletion.deleteEventAndRelatedElementsFor");
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

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #011 08-MAY-2016: Simplify version numbering mechanism (F.J.Chadburn)
    #013 10-MAY-2016: Add support for sequence diagram req't and verification relation population (F.J.Chadburn)
    #016 11-MAY-2016: Add GPL advisory to the Log window (F.J.Chadburn)
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #027 31-MAY-2016: Add new menu to launch dialog to copy Activity Diagrams (F.J.Chadburn)
    #028 01-JUN-2016: Add new menu to create a stand-alone attribute owned by the system (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    
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

