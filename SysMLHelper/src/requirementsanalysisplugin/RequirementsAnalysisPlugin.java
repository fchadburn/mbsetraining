package requirementsanalysisplugin;

import generalhelpers.ConfigurationSettings;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class RequirementsAnalysisPlugin extends RPUserPlugin {
  
	protected static IRPApplication m_rhpApplication = null;
	protected static ConfigurationSettings m_configSettings = null;
	
	// plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		
		m_rhpApplication = theRhapsodyApp;	
		m_configSettings = ConfigurationSettings.getInstance();
		
		String msg = "The RequirementsAnalysisPlugin component of the SysMLHelperPlugin V" + m_configSettings.getProperty("PluginVersion") + " was loaded successfully. New right-click 'MBSE Method' commands have been added.";		
		Logger.writeLine(msg); 
	}

	public static IRPApplication getRhapsodyApp(){
		
		if (m_rhpApplication==null){
			m_rhpApplication = RhapsodyAppServer.getActiveRhapsodyApplication();
		}
		
		return m_rhpApplication;
	}
	
	public static IRPProject getActiveProject(){
		
		return getRhapsodyApp().activeProject();
	}
	
	// For use with Tables
	public String getRequirementSpecificationText(String guid) {
	
		//Logger.writeLine("Was invoked with guid " + guid);
		String theSpec = "Not found";
		
		try {
			@SuppressWarnings("rawtypes")
			List theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();
			
			if( theAppIDs.size() == 1 ){
	
				IRPProject theRhpProject = RhapsodyAppServer.getActiveRhapsodyApplication().activeProject();
				IRPModelElement theEl = theRhpProject.findElementByGUID(guid);
				
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
			}
			


		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in getRequirementSpecificationText");
		}

		return theSpec;
	}
	
	// called when the plug-in pop-up menu (if applicable) is selected
	public void OnMenuItemSelect(String menuItem) {
	
		if( UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){
			IRPModelElement theSelectedEl = getRhapsodyApp().getSelectedElement();
			IRPProject theActiveProject = getRhapsodyApp().activeProject();
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theSelectedGraphEls = getRhapsodyApp().getSelectedGraphElements().toList();
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theSelectedEls = getRhapsodyApp().getListOfSelectedElements().toList();

			Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");
			
			if( !theSelectedEls.isEmpty() ){
				//selElemName = theSelectedEl.getName();	
							
				if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.CreateNestedADMenu" ))){

					try {
						NestedActivityDiagram.createNestedActivityDiagramsFor( theSelectedEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking NestedActivityDiagram.createNestedActivityDiagramsFor");
					}

				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.ReportOnNamingAndTraceabilityMenu" ))){

					try {
						ActivityDiagramChecker.createActivityDiagramCheckersFor( theSelectedEls );
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking new ActivityDiagramChecker.createActivityDiagramCheckersFor");
					}

					
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.MoveUnclaimedReqtsMenu" ))){

					try {
						MoveRequirements.moveUnclaimedRequirementsReadyForGatewaySync( theSelectedEls, theActiveProject );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MoveRequirements.moveUnclaimedRequirementsReadyForGatewaySync");
					}

					
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.CreateNewRequirementMenu" ))){

					try {
						Logger.writeLine("Creating new requirements");
						RequirementsHelper.createNewRequirementsFor( theSelectedGraphEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RequirementsHelper.createNewRequirementsFor");
					}

				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.PerformRenameInBrowserMenu" ))){

					try {				
						RenameActions.performRenamesFor( theSelectedEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RenameActions.performRenamesFor");
					}
					
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.UpdateNestedADNamesMenu" ))){

					try {				
						NestedActivityDiagram.renameNestedActivityDiagramsFor( theSelectedEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking NestedActivityDiagram.renameNestedActivityDiagramsFor");
					}	

				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.DeleteTaggedAsDeletedAtHighLevelMenu" ))){

					try {
						MarkedAsDeletedPanel.launchThePanel( theSelectedEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MarkedAsDeletedPanel.launchThePanel");
					}
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.StartLinkMenu" ))){

					try {
						SmartLinkPanel.launchTheStartLinkPanel( theSelectedEls, theSelectedGraphEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking SmartLinkPanel.launchTheStartLinkPanel");
					}
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.EndLinkMenu" ))){

					try {				
						SmartLinkPanel.launchTheEndLinkPanel( theSelectedEls, theSelectedGraphEls );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking SmartLinkPanel.launchTheEndLinkPanel");
					}
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.RollUpTraceabilityUpToTransitionLevel" ))){

					try {
						if( theSelectedGraphEls != null ){
							IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get( 0 );
							PopulateTransitionRequirementsPanel.launchThePanel( theSelectedGraphEl );
						}

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking SmartLinkPanel.launchTheEndLinkPanel");
					}	
				} else if (menuItem.equals(m_configSettings.getString( "requirementsanalysisplugin.layoutDependencies" ))){

					try {
						if( theSelectedGraphEls.size() > 0 ){
							
							LayoutHelper.centerDependenciesForTheGraphEls( 
									theSelectedGraphEls );

						} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagramGE" ) ){
							
							LayoutHelper.centerDependenciesForTheDiagram( 
									(IRPDiagram) theSelectedEl );

						} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagram" ) ){
							
							@SuppressWarnings("unchecked")
							List<IRPModelElement> theDiagrams = 
									theSelectedEl.getNestedElementsByMetaClass( "ActivityDiagramGE", 0 ).toList();
							
							if( theDiagrams.size()==1 ){
								
								LayoutHelper.centerDependenciesForTheDiagram( 
										(IRPDiagram) theDiagrams.get( 0 ) );
							} else {
								Logger.writeLine( "Error in OnMenuItemSelect, unable to find an ActivityDiagramGE" );
							}

						} else if( theSelectedEl instanceof IRPDiagram ){

							LayoutHelper.centerDependenciesForTheDiagram( 
									(IRPDiagram) theSelectedEl );
							
						} else if( theSelectedEl instanceof IRPPackage ){
							
							LayoutHelper.centerDependenciesForThePackage( 
									(IRPPackage) theSelectedEl );
						}

					} catch (Exception e) {
						Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking LayoutHelper" );
					}
				} else {
					Logger.writeLine(theSelectedEl, " was invoked with menuItem='" + menuItem + "'");
				}
			} // else No selected element

			Logger.writeLine("... completed");
		}

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
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)    
    #011 08-MAY-2016: Simplify version numbering mechanism (F.J.Chadburn)
    #016 11-MAY-2016: Add GPL advisory to the Log window (F.J.Chadburn)
    #041 29-JUN-2016: Derive downstream requirement menu added for reqts on diagrams (F.J.Chadburn) 
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #049 06-JUL-2016: Derive new requirement now under Functional Analysis not Requirements Analysis menu (F.J.Chadburn)
    #102 03-NOV-2016: Add right-click menu to auto update names of ADs from UC names (F.J.Chadburn)
    #109 06-NOV-2016: Added .properties support for localisation of menus (F.J.Chadburn)
    #110 06-NOV-2016: PluginVersion now comes from Config.properties file, rather than hard wired (F.J.Chadburn)
    #155 25-JAN-2017: Added new panel to find and delete Gateway Deleted_At_High_Level req'ts with Rhp 8.2 (F.J.Chadburn)
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    #224 25-AUG-2017: Added new menu to roll up traceability to the transition and populate on STM (F.J.Chadburn)
    #229 20-SEP-2017: Add re-layout dependencies on diagram(s) menu to ease beautifying when req't tracing (F.J.Chadburn)
    #239 04-OCT-2017: Improve warning/behaviour if multiple Rhapsodys are open or user switches app (F.J.Chadburn)

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

