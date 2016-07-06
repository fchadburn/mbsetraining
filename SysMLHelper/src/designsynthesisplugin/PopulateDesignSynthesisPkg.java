package designsynthesisplugin;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;

import generalhelpers.Logger;
import generalhelpers.PopulatePkg;

public class PopulateDesignSynthesisPkg extends PopulatePkg {

	public static void createDesignSynthesisPkg(IRPProject forProject){
		
		final String rootPackageName = "DesignSynthesisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null){
			Logger.writeLine("Doing nothing: " + Logger.elementInfo( forProject ) + " already has package called " + rootPackageName);
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for executable MBSE. \n" +
		    		"It creates a nested package structure for executable 'interaction-based' design synthesis,  \n" +
		    		"imports the appropriate profiles if not present, and sets default display and other options \n" +
		    		"to appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    	
		    	browseAndAddByReferenceIfNotPresent("RequirementsAnalysisPkg", forProject, true);
		    	browseAndAddByReferenceIfNotPresent("FunctionalAnalysisPkg", forProject, true);
		    	populateDesignSynthesisPkg(forProject);
		    	removeSimpleMenuStereotypeIfPresent(forProject);
		    	
		    	forProject.save();

		    } else {
		    	Logger.writeLine("Cancelled by user");
		    }
		}
	}
	
	static void populateDesignSynthesisPkg(IRPProject forProject) {
		
		addProfileIfNotPresent("SysML", forProject);		
		addProfileIfNotPresent("GlobalPreferencesProfile", forProject);
		addProfileIfNotPresent("RequirementsAnalysisProfile", forProject);
		addProfileIfNotPresent("FunctionalAnalysisProfile", forProject);
		addProfileIfNotPresent("DesignSynthesisProfile", forProject);
		
		forProject.changeTo("SysML");
		
		IRPPackage theDesignSynthesisPkg = addPackageFromProfileRpyFolder( forProject, "DesignSynthesisPkg" );
		
		if (theDesignSynthesisPkg != null){
		
			deleteIfPresent( "Structure1", "StructureDiagram", forProject );
	    	deleteIfPresent( "Default", "Package", forProject );
	    	
	    	setProperty( forProject, "Browser.Settings.ShowPredefinedPackage", "True" );
	    	setProperty( forProject, "General.Model.AutoSaveInterval", "5" );
	    	setProperty( forProject, "General.Model.HighlightElementsInActiveComponentScope", "True" );
	    	setProperty( forProject, "General.Model.ShowModelTooltipInGE", "Simple" );
	    	setProperty( forProject, "General.Model.BackUps", "One" );
	    	
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #046 06-JUL-2016: Fix external RequirementsAnalysisPkg reference to be created with relative path (F.J.Chadburn)
    
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
