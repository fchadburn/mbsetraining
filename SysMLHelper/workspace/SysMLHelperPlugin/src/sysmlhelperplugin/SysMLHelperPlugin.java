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

package sysmlhelperplugin;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class SysMLHelperPlugin extends RPUserPlugin {

	protected IRPApplication m_rhpApplication = null;

	String version = "1.0 (Release)";
	
	// called when plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		
		// keep the application interface for later use
		m_rhpApplication = theRhapsodyApp;

		String msg = "The SysMLHelperProfile plugin V" + version + " was loaded successfully. New right-click 'MBSE Method' commands have been added.";
		Logger.writeLine(msg);
	}
	
	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(String menuItem) {
		
		IRPModelElement theSelectedEl = m_rhpApplication.getSelectedElement();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = m_rhpApplication.getListOfSelectedElements().toList();

		Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");
		
		if( !theSelectedEls.isEmpty() ){
						
			if (menuItem.equals("MBSE Method: Requirements Analysis\\Create the RequirementsAnalysisPkg package structure")){
				
				if (theSelectedEl instanceof IRPProject){
					try { 
						PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg( (IRPProject) theSelectedEl ); 
						
					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateRequirementsAnalysisPkg.createRequirementsAnalysisPkg");
					}
				}
			}	
		}
		
		Logger.writeLine("... completed");
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
	
	public String traceabilityReportHtml(String guid) {
		String retval = "";
		IRPModelElement modelElement = m_rhpApplication.activeProject().findElementByGUID(guid);
		
		if (modelElement==null){
			Logger.writeLine("Unable to find an element with guid=" + guid);
		}

		if(modelElement != null) {

			List<IRPRequirement> theTracedReqts = getRequirementsThatTraceFrom(modelElement);

			if (theTracedReqts.isEmpty()){

				retval = "<br>This element has no traceability to requirements<br><br>";
			} else {
				retval = "<br><b>Requirements:</b>";				
				retval += "<table border=\"1\">";			
				retval += "<tr><td><b>ID</b></td><td><b>Specification</b></td></tr>";

				for (IRPRequirement theReqt : theTracedReqts) {
					retval += "<tr><td>" + theReqt.getName() + "</td><td>"+ theReqt.getSpecification() +"</tr>";
				}

				retval += "</table><br>";
			}				
		}

		return retval;
	}
	
	public String InvokeTooltipFormatter(String html) {

		String guidStr = html.substring(1, html.indexOf(']'));
		
		IRPModelElement object = m_rhpApplication.activeProject().findElementByGUID(guidStr);
		
		if (object != null) {
			guidStr = object.getGUID();
		}

		html = html.substring(html.indexOf(']') + 1);
		
		String thePart1 =  html.substring(
				0,
				html.indexOf("[[<b>Dependencies:</b>"));
		
		String thePart2 = traceabilityReportHtml(guidStr);
		String thePart3 = html.substring(html.lastIndexOf("[[<b>Dependencies:</b>") - 1);
		
		return thePart1+thePart2+thePart3;
	}

	@Override
	public void OnTrigger(String trigger) {
		
	}
	
}
