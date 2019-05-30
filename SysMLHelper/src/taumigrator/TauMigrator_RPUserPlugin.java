package taumigrator;

import java.util.ArrayList;
import java.util.List;
import generalhelpers.*; 

import com.telelogic.rhapsody.core.*;

public class TauMigrator_RPUserPlugin extends RPUserPlugin {

	static protected IRPApplication m_rhpApplication = null;
	static protected ConfigurationSettings m_configSettings = null;
	
	final String legalNotice = 
			"Copyright (C) 2015-2019  MBSE Training and Consulting Limited (www.executablembse.com)"
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
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){
		 
		// keep the application interface for later use
		m_rhpApplication = theRhapsodyApp;
		
		m_configSettings = new ConfigurationSettings(
				"TauMigrator.properties", 
				"TauMigrator_MessagesBundle" );
		
		String msg = "The TauMigratorPlugin component of the SysMLHelperPlugin V" + m_configSettings.getProperty("PluginVersion") + " was loaded successfully.\n" + legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";
		
		Logger.writeLine(msg);
		
/*		// Added by F.J.Chadburn #001
		FunctionalDesign_RPApplicationListerner listener = new FunctionalDesign_RPApplicationListerner(theRhapsodyApp);
		listener.connect( theRhapsodyApp );*/
	}
	
	public static IRPApplication getRhapsodyApp(){
		
		if( m_rhpApplication == null ){
			m_rhpApplication = RhapsodyAppServer.getActiveRhapsodyApplication();
		}
		
		return m_rhpApplication;
	}
	
	public static IRPProject getActiveProject(){
	 	
		return getRhapsodyApp().activeProject();
	} 
	
	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(
			String menuItem ){
		
		if( UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){
			
			IRPApplication theRhpApp = TauMigrator_RPUserPlugin.getRhapsodyApp();
			
			IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

			IRPProject theRhpPrj = theRhpApp.activeProject();

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theSelectedEls = 
					theRhpApp.getListOfSelectedElements().toList();

			Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

			if( !theSelectedEls.isEmpty() ){

				if( menuItem.equals( m_configSettings.getString( 
						"taumigratorplugin.ImportTauModelFromXML" ) ) ){

						if( theSelectedEl instanceof IRPPackage ){
													
							PopulatePkg.addProfileIfNotPresent( "SysML", theRhpPrj );
							theRhpPrj.changeTo("SysML");
							
							try { 
								ProfileVersionManager.checkAndSetProfileVersion( true, m_configSettings, true );

								CreateRhapsodyModelElementsFromXML theCreator = 
										new CreateRhapsodyModelElementsFromXML(
												theRhpApp );
								
								theCreator.go();
								
								PopulatePkg.deleteIfPresent( "Structure1", "StructureDiagram", theRhpPrj );
								PopulatePkg.deleteIfPresent( "Model1", "ObjectModelDiagram", theRhpPrj );
								PopulatePkg.deleteIfPresent( "Default", "Package", theRhpPrj );
								
								//AutoPackageDiagram theAPD = new AutoPackageDiagram( theRhpPrj );
								//theAPD.drawDiagram();
								
								//PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg( (IRPProject) theSelectedEl ); 

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg");
							}
						}					
				} else if( menuItem.equals( m_configSettings.getString( 
						"taumigratorplugin.SetupTauMigratorProjectProperties" ) ) ){

						if (theSelectedEl instanceof IRPPackage){
							
							try {
								ProfileVersionManager.checkAndSetProfileVersion( 
										true, m_configSettings, true );

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg");
							}
						}
						
				} else {
					Logger.writeLine("Warning in OnMenuItemSelect, " + menuItem + " was not handled.");
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
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #256 11-SEP-2018: Add new Functional Design menu support as a variant of profile (F.J.Chadburn)

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