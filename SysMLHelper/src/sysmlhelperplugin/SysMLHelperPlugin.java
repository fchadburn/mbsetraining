package sysmlhelperplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.PopulateRequirementsAnalysisPkg;
import functionalanalysisplugin.PopulateFunctionalAnalysisPkg;
import functionalanalysisplugin.PopulateFunctionalAnalysisPkg.SimulationType;
import designsynthesisplugin.PopulateDesignSynthesisPkg;
import generalhelpers.*; 

import com.telelogic.rhapsody.core.*;

public class SysMLHelperPlugin extends RPUserPlugin {

	static protected IRPApplication m_rhpApplication = null;
	static protected ConfigurationSettings m_configSettings = null;
	
	final String legalNotice = 
			"Copyright (C) 2015-2017  MBSE Training and Consulting Limited (www.executablembse.com)"
			+ "\n"
			+ "SysMLHelperPlugin is free software: you can redistribute it and/or modify "
			+ "it under the terms of the GNU General Public License as published by "
			+ "the Free Software Foundation, either version 3 of the License, or "
			+ "(at your option) any later version."
			+ "\n"
			+ "SysMLHelperPlugin is distributed in the hope that it will be useful, "
			+ "but WITHOUT ANY WARRANTY; without even the implied warranty of "
			+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
			+ "GNU General Public License for more details."
			+ "You should have received a copy of the GNU General Public License "
			+ "along with SysMLHelperPlugin. If not, see <http://www.gnu.org/licenses/>. "
			+ "Source code is made available on https://github.com/fchadburn/mbsetraining";
                                                                                                                                                                                      
	// called when plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		 
		// keep the application interface for later use
		m_rhpApplication = theRhapsodyApp;
		
		m_configSettings = ConfigurationSettings.getInstance();

		String msg = "The SysMLHelperProfile plugin V" + m_configSettings.getProperty("PluginVersion") + " was loaded successfully.\n" + legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";
		
		Logger.writeLine(msg);
		
		// Added by F.J.Chadburn #001
		SysMLHelperTriggers listener = new SysMLHelperTriggers(theRhapsodyApp);
		listener.connect( theRhapsodyApp );
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
	
	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(String menuItem) {
		
		if( UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){
			IRPApplication theRhpApp = SysMLHelperPlugin.getRhapsodyApp();
			
			IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theSelectedEls = 
					theRhpApp.getListOfSelectedElements().toList();

			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = 
					theRhpApp.getSelectedGraphElements().toList();

			Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

			if( !theSelectedEls.isEmpty() ){

				if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.CreateRAStructureMenu"))){

					if (theSelectedEl instanceof IRPProject){
						try { 
							PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg( (IRPProject) theSelectedEl ); 

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg");
						}
					}
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.CreateFullSimFAStructureMenu"))){

					if (theSelectedEl instanceof IRPProject){
						try { 
							PopulateFunctionalAnalysisPkg.createFunctionalAnalysisPkg( (IRPProject) theSelectedEl, SimulationType.FullSim ); 

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.createFunctionalAnalysisPkg");
						}
					}
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.CreateSimpleSimFAStructureMenu"))){

					if (theSelectedEl instanceof IRPProject){
						try { 
							PopulateFunctionalAnalysisPkg.createFunctionalAnalysisPkg( (IRPProject) theSelectedEl, SimulationType.SimpleSim ); 

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.createFunctionalAnalysisPkg");
						}
					}
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.CreateNoSimFAStructureMenu"))){

					if (theSelectedEl instanceof IRPProject){
						try { 
							PopulateFunctionalAnalysisPkg.createFunctionalAnalysisPkg( (IRPProject) theSelectedEl, SimulationType.NoSim ); 

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.createFunctionalAnalysisPkg");
						}
					}
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.CreateDSStructureMenu"))){

					if (theSelectedEl instanceof IRPProject){
						try { 
							PopulateDesignSynthesisPkg.createDesignSynthesisPkg( (IRPProject) theSelectedEl ); 

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateDesignSynthesisPkg.createDesignSynthesisPkg");
						}
					}
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.QuickHyperlinkMenu"))){

					try { 
						IRPHyperLink theHyperLink = (IRPHyperLink) theSelectedEl.addNewAggr("HyperLink", "");
						theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");
						theHyperLink.highLightElement();
						theHyperLink.openFeaturesDialog(0);

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Quick hyperlink");
					}
					
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependsOnElementsMenu"))){

					try { 					
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );
						
						DependencySelector.selectDependsOnElementsFor( 
								theCombinedSet, null );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\All");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependentElementsMenu"))){

					try {
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );

						DependencySelector.selectDependentElementsFor( 
								theCombinedSet, null );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\All");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependsOnDeriveOnlyElementsMenu"))){

					try { 					
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );
						
						DependencySelector.selectDependsOnElementsFor( 
								theCombinedSet, "derive" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Derives");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependentDeriveOnlyElementsMenu"))){

					try {
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );

						DependencySelector.selectDependentElementsFor( 
								theCombinedSet, "derive" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Derives");
					}
					
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependsOnSatisfyOnlyElementsMenu"))){

					try { 					
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );
						
						DependencySelector.selectDependsOnElementsFor( 
								theCombinedSet, "satisfy" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Satisfies");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependentSatisfyOnlyElementsMenu"))){

					try {
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );

						DependencySelector.selectDependentElementsFor( 
								theCombinedSet, "satisfy" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Satisfies");
					}
					
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependsOnVerifyOnlyElementsMenu"))){

					try { 					
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );
						
						DependencySelector.selectDependsOnElementsFor( 
								theCombinedSet, "verify" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Verifies");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependentVerifyOnlyElementsMenu"))){

					try {
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );

						DependencySelector.selectDependentElementsFor( 
								theCombinedSet, "verify" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Verifies");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependsOnRefineOnlyElementsMenu"))){

					try { 					
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );
						
						DependencySelector.selectDependsOnElementsFor( 
								theCombinedSet, "refine" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Refines");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependentRefineOnlyElementsMenu"))){

					try {
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );

						DependencySelector.selectDependentElementsFor( 
								theCombinedSet, "refine" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Refines");
					}
					
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependsOnDeriveReqtOnlyElementsMenu"))){

					try { 					
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );
						
						DependencySelector.selectDependsOnElementsFor( 
								theCombinedSet, "deriveReqt" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Derive Requirement");
					}

				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SelectDependentDeriveReqtOnlyElementsMenu"))){

					try {
						Set<IRPModelElement> theCombinedSet = 
								GeneralHelpers.getSetOfElementsFromCombiningThe(
										theSelectedEls, theGraphEls );

						DependencySelector.selectDependentElementsFor( 
								theCombinedSet, "deriveReqt" );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Derive Requirement");
					}				
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.SetupGatewayProjectMenu"))){

					if (theSelectedEl instanceof IRPProject){
						try { 
							CreateGatewayProjectPanel.launchThePanel( (IRPProject)theSelectedEl, ".*.rqtf$" );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking CreateGatewayProjectPanel.launchThePanel");
						}					
					}
				} else if (menuItem.equals(m_configSettings.getString("sysmlhelperplugin.AddRelativeUnitMenu"))){

					try { 
						RelativeUnitHandler.browseAndAddUnit( theSelectedEl.getProject(), true );

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RelativeUnitHandler.browseAndAddUnit");
					}									
				}					
			}

			Logger.writeLine("... completed");
		}

	}

	// if true is returned the plugin will be unloaded
	public boolean RhpPluginCleanup() {

		m_rhpApplication = null;
		return true; // plug-in will be unloaded now (on project close)
	}

	@Override
	public void RhpPluginFinalCleanup() {
	}

	@Override
	public void RhpPluginInvokeItem() {

	}
	
	public static List<IRPRequirement> getRequirementsThatTraceFrom(
			IRPModelElement theElement){
		
		List<IRPRequirement> theReqts = new ArrayList<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();
		
		for (IRPDependency theDependency : theExistingDeps) {
			
			IRPModelElement theDependsOn = theDependency.getDependsOn();
			
			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){
				theReqts.add( (IRPRequirement) theDependsOn );
			}
		}
		
		return theReqts;
	}
	
	public String traceabilityReportHtml( IRPModelElement theModelEl ) {
		
		String retval = "";

		if( theModelEl != null ){
			
			List<IRPRequirement> theTracedReqts;
			
			if( theModelEl instanceof IRPDependency ){
				
				IRPDependency theDep = (IRPDependency) theModelEl;
				IRPModelElement theDependsOn = theDep.getDependsOn();
				
				if( theDependsOn != null && 
					theDependsOn instanceof IRPRequirement ){
					
					// Display text of the requirement that the dependency traces to
					theTracedReqts = new ArrayList<>();
					theTracedReqts.add( (IRPRequirement) theDependsOn );
				} else {
					theTracedReqts = getRequirementsThatTraceFrom( theModelEl );
				}
			} else {
				theTracedReqts = getRequirementsThatTraceFrom( theModelEl );
			}

			if( theTracedReqts.isEmpty() ){

				retval = "<br>This element has no traceability to requirements<br><br>";
			} else {
				retval = "<br><b>Requirements:</b>";				
				retval += "<table border=\"1\">";			
				retval += "<tr><td><b>ID</b></td><td><b>Specification</b></td></tr>";

				for( IRPRequirement theReqt : theTracedReqts ){
					retval += "<tr><td>" + theReqt.getName() + "</td><td>"+ theReqt.getSpecification() +"</tr>";
				}

				retval += "</table><br>";
			}				
		}

		return retval;
	}
	
	public String InvokeTooltipFormatter(String html) {

		String theOutput = html;
		
		try{
			@SuppressWarnings("rawtypes")
			List theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();
			
			if( theAppIDs.size() == 1 ){
	
				IRPProject theRhpProject = RhapsodyAppServer.getActiveRhapsodyApplication().activeProject();
				
				String guidStr = html.substring(1, html.indexOf(']'));
				
				IRPModelElement theModelEl = theRhpProject.findElementByGUID( guidStr );
				
				if( theModelEl != null ){
					guidStr = theModelEl.getGUID();
				}

				html = html.substring(html.indexOf(']') + 1);
				
				String thePart1 =  html.substring(
						0,
						html.indexOf("[[<b>Dependencies:</b>"));
				
				String thePart2 = traceabilityReportHtml( theModelEl );
				String thePart3 = html.substring(html.lastIndexOf("[[<b>Dependencies:</b>") - 1);
			
				theOutput = thePart1 + thePart2 + thePart3;
			}
			
		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in InvokeTooltipFormatter");
		}

		return theOutput;
	}

	@Override
	public void OnTrigger(String trigger) {
		
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #001 31-MAR-2016: Added ListenForRhapsodyTriggers (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #011 08-MAY-2016: Simplify version numbering mechanism (F.J.Chadburn)
    #016 11-MAY-2016: Add GPL advisory to the Log window (F.J.Chadburn)
    #017 11-MAY-2016: Double-click now works with both nested and hyper-linked diagrams (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #050 06-JUL-2016: Setup Gateway project based on rqtf template option now under General Utilities menu (F.J.Chadburn)
    #109 06-NOV-2016: Added .properties support for localisation of menus (F.J.Chadburn)
    #110 06-NOV-2016: PluginVersion now comes from Config.properties file, rather than hard wired (F.J.Chadburn)
    #111 13-NOV-2016: Added new Simple Sim (Guard only) functional analysis structure option (F.J.Chadburn)
    #112 13-NOV-2016: Added new No Sim functional analysis structure option (F.J.Chadburn)
    #162 05-FEB-2017: Add new menu to add a relative reference to an external unit (.sbs) (F.J.Chadburn)
    #165 15-FEB-2017: Added new menu to select end of Dependency relations to assist usability (F.J.Chadburn)
    #166 15-FEB-2017: Corrected Copyright information to 2017 (F.J.Chadburn)
    #172 02-APR-2017: Added new General Utilities > Select Dependent element(s) option (F.J.Chadburn)
    #207 25-JUN-2017: Significant bolstering of Select Depends On/Dependent element(s) menus (F.J.Chadburn)
    #219 12-JUL-2017: Added Select Depends On element(s)\Derive Requirement menus for deriveReqt relations (F.J.Chadburn)
    #231 27-SEP-2017: Enhanced tooltip now shows the req't text when hovering over traceability relations (F.J.Chadburn)
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