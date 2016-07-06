package requirementsanalysisplugin;

import generalhelpers.CreateGatewayProjectPanel;
import generalhelpers.Logger;
import generalhelpers.PopulatePkg;
import generalhelpers.UserInterfaceHelpers;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;

public class PopulateRequirementsAnalysisPkg extends PopulatePkg {
	 
	public static void displayGraphicalPropertiesFor(IRPGraphElement theGraphEl){
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			String thePropertyname = theGraphicalProperty.getKey();
			String theValue = theGraphicalProperty.getValue();
			
			Logger.writeLine(thePropertyname + "=" + theValue);
		}
	}
	
	public static void createRequirementsAnalysisPkg(IRPProject forProject){
		
		final String rootPackageName = "RequirementsAnalysisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null){
			Logger.writeLine("Doing nothing: " + Logger.elementInfo( forProject ) + " already has package called " + rootPackageName);
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for simple activity\n" +
		    		"based use case analysis. It creates a nested package structure and use case diagram, imports\n" +
		    		"the appropriate profiles if not present, and sets default display and other options to \n" +
		    		"appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    			
				boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"Do you initially want to simplify the 'Add New' menu for just\n" + 
				        "use case and requirements analysis?");
		    	
		    	populateRequirementsAnalysisPkg(forProject);
				
				if (theAnswer==true){
					applySimpleMenuStereotype(forProject);
				}
				
				CreateGatewayProjectPanel.launchThePanel( forProject, "^RequirementsAnalysisPkg.rqtf$" );
				
		    } else {
		    	Logger.writeLine("Cancelled by user");
		    }
		}
	}
	
	static IRPPackage populateRequirementsAnalysisPkg(IRPProject forProject) {
		
		addProfileIfNotPresent("SysML", forProject);		
		addProfileIfNotPresent("GlobalPreferencesProfile", forProject);
		addProfileIfNotPresent("RequirementsAnalysisProfile", forProject);
		
		forProject.changeTo("SysML");
		
		IRPPackage theRequirementsAnalysisPkg = addPackageFromProfileRpyFolder(forProject, "RequirementsAnalysisPkg" );
		
		if (theRequirementsAnalysisPkg != null){
			
			Logger.writeLine(theRequirementsAnalysisPkg, "was successfully copied from the SysMLHelper profile");
					
			deleteIfPresent( "Structure1", "StructureDiagram", forProject );
			deleteIfPresent( "Default", "Package", forProject );
			
			setProperty( forProject, "Browser.Settings.ShowPredefinedPackage", "False" );
			setProperty( forProject, "General.Model.AutoSaveInterval", "5" );
			setProperty( forProject, "General.Model.HighlightElementsInActiveComponentScope", "False" );
			setProperty( forProject, "General.Model.ShowModelTooltipInGE", "Enhanced" );			
			setProperty( forProject, "General.Model.BackUps", "One" );
			setProperty (forProject, "General.Model.RenameUnusedFiles", "True");
			
			forProject.save();
			
			@SuppressWarnings("unchecked")
			List<IRPUseCaseDiagram> theUCDs = theRequirementsAnalysisPkg.getNestedElementsByMetaClass("UseCaseDiagram", 1).toList();
			
			for (IRPUseCaseDiagram theUCD : theUCDs) {
				Logger.writeLine(theUCD, "was added to the project");
				
				String oldName = theUCD.getName();
				String newName = oldName.replaceAll("ProjectName", forProject.getName());
				
				if (!newName.equals( oldName )){
					Logger.writeLine("Renaming " + oldName + " to " + newName);
					theUCD.setName( newName );
				}
				
				theUCD.highLightElement();
				theUCD.openDiagram();
			}	

		} else {
			Logger.writeLine("Error in createRequirementsAnalysisPkg, unable to add RequirementsAnalysisPkg package");
		}
		
		return theRequirementsAnalysisPkg;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #002 05-APR-2016: Improved robustness of copying .types file (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #007 05-MAY-2016: Move FileHelper into generalhelpers and remove duplicate class (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #051 06-JUL-2016: Re-factored the GW panel to allow it to incrementally add to previous setup (F.J.Chadburn)
    
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

