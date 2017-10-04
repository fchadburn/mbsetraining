package designsynthesisplugin;

import generalhelpers.ConfigurationSettings;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class DesignSynthesisPlugin extends RPUserPlugin {
  
	protected static IRPApplication m_rhpApplication = null;
	protected static ConfigurationSettings m_configSettings = null;
	
	// plug-in is loaded
	public void RhpPluginInit(final IRPApplication theRhapsodyApp) {
		
		m_rhpApplication = theRhapsodyApp;
		m_configSettings = ConfigurationSettings.getInstance();
		
		String msg = "The DesignSynthesisPlugin component of the SysMLHelperPlugin V" + m_configSettings.getProperty("PluginVersion") + " was loaded successfully. New right-click 'MBSE Method' commands have been added.";		
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
	
	// called when the plug-in pop-up menu (if applicable) is selected
	public void OnMenuItemSelect(String menuItem) {
	
		if( UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){
			IRPModelElement theSelectedEl = getRhapsodyApp().getSelectedElement();
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theSelectedEls = getRhapsodyApp().getListOfSelectedElements().toList();

			Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");
			
			if( !theSelectedEls.isEmpty() ){
				//selElemName = theSelectedEl.getName();	
							
				if (menuItem.equals(m_configSettings.getString("designsynthesisplugin.MakeAttributeAPublishFlowportMenu"))){
					
					if (theSelectedEl instanceof IRPAttribute){
						try {
							PortCreator.createPublishFlowportsFor( theSelectedEls );
							
						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking createPublishFlowportsFor");
						}
					}
					
				} else if (menuItem.equals(m_configSettings.getString("designsynthesisplugin.MakeAttributeASubscribeFlowportMenu"))){
					
					if (theSelectedEl instanceof IRPAttribute){
						try {
							PortCreator.createSubscribeFlowportsFor( theSelectedEls );
							
						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking createSubscribeFlowportsFor");
						}
					}

				} else if (menuItem.equals(m_configSettings.getString("designsynthesisplugin.DeleteAttributeAndRelatedElementsMenu"))){
					
					try {
						if( theSelectedEl instanceof IRPAttribute ){
							PortCreator.deleteAttributeAndRelatedEls( (IRPAttribute) theSelectedEl );
						} else if ( theSelectedEl instanceof IRPSysMLPort ){
							PortCreator.deleteFlowPortAndRelatedEls( (IRPSysMLPort) theSelectedEl );
						}

					} catch (Exception e) {
						Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking designsynthesisplugin.DeleteAttributeAndRelatedElementsMenu");
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
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #011 08-MAY-2016: Simplify version numbering mechanism (F.J.Chadburn)
    #016 11-MAY-2016: Add GPL advisory to the Log window (F.J.Chadburn)
    #109 06-NOV-2016: Added .properties support for localisation of menus (F.J.Chadburn)
    #110 06-NOV-2016: PluginVersion now comes from Config.properties file, rather than hard wired (F.J.Chadburn)
    #180 29-MAY-2017: Added new Design Synthesis menu to Delete attribute and related elements (F.J.Chadburn)
    #192 05-JUN-2017: Widened DeleteAttributeAndRelatedElementsMenu support to work with flow-ports as well (F.J.Chadburn)
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

