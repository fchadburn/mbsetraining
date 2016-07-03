package generalhelpers;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import requirementsanalysisplugin.RequirementsAnalysisPlugin;
import sysmlhelperplugin.SysMLHelperPlugin;

import com.telelogic.rhapsody.core.*;

public class PopulatePkg {

	public static IRPProfile addProfileIfNotPresent(String theProfileName, IRPProject toTheProject){
		
		IRPProfile theProfile = (IRPProfile) toTheProject.findNestedElement(theProfileName, "Profile");
		
		if (theProfile==null){

			IRPUnit theUnit = RequirementsAnalysisPlugin.getRhapsodyApp().addProfileToModel( theProfileName );
			
			if (theUnit != null){
				
				theProfile = (IRPProfile)theUnit;
				Logger.writeLine("Added profile called " + theProfile.getFullPathName());
				
			} else {
				Logger.writeLine("Error in addProfileIfNotPresent. No profile found with name " + theProfileName);
			}
			
		} else {
			Logger.writeLine(Logger.elementInfo(theProfile) + " is already present in the project");
		}
		
		return theProfile;		
	}
	
	
	public static IRPProfile addProfileIfNotPresentAndMakeItApplied(String theProfileName, IRPPackage appliedToPackage){
		
		IRPProject addToProject = appliedToPackage.getProject();
		
		IRPProfile theProfile = addProfileIfNotPresent(theProfileName, addToProject);
		
		IRPDependency theDependency = appliedToPackage.addDependencyTo( theProfile );
		theDependency.addStereotype("AppliedProfile", "Dependency");
		
		return theProfile;			
	}
	
	public static void setProperty(IRPModelElement onTheEl, String withKey, String toValue){
		
		Logger.writeLine("Setting " + withKey + " property on " + Logger.elementInfo(onTheEl) + " to " + toValue);
		onTheEl.setPropertyValue(withKey, toValue);
	}
	
	public static void deleteIfPresent(String theElementWithName, String andMetaClass, IRPModelElement nestedUnderEl){
		
		IRPModelElement theEl = nestedUnderEl.findNestedElementRecursive(theElementWithName, andMetaClass);
		
		if (theEl != null){
			
			IRPCollection theNestedEls = theEl.getNestedElementsRecursive();
			
			int count = theNestedEls.getCount();
			
			if (count > 1){
				Logger.writeLine("Decided against deleting " + Logger.elementInfo( theEl ) + " as it has unexpected contents");
			} else {
				Logger.writeLine(theEl, "was deleted from " + Logger.elementInfo( nestedUnderEl ));
				theEl.deleteFromProject();
			}
			
		} else {
			Logger.writeLine("Unable to delete " + andMetaClass + " with the name " 
					+ theElementWithName  + " as it was not found underneath " + Logger.elementInfo( nestedUnderEl ) );
		}
	}	
	
	public static void browseAndAddByReferenceIfNotPresent(String thePackageName, IRPProject inProject){
		
    	IRPModelElement theExistingPkg = inProject.findElementsByFullName(thePackageName, "Package");
    	
    	if (theExistingPkg == null){
    		JFileChooser theFileChooser = new JFileChooser(System.getProperty("user.dir"));
    		theFileChooser.setFileFilter(new FileNameExtensionFilter("Package", "sbs"));
    		
    		int choice = theFileChooser.showDialog(null, "Choose " + thePackageName);
    		
    		if (choice==JFileChooser.CANCEL_OPTION){
    			Logger.writeLine("Operation cancelled by user when trying to choose " + thePackageName);
    			
    		} else if (choice==JFileChooser.APPROVE_OPTION){
    			File theFile = theFileChooser.getSelectedFile();
    			String thePath = theFile.getAbsolutePath();
    			SysMLHelperPlugin.getRhapsodyApp().addToModelByReference(thePath);
    		}
    	}
	}
	
	protected static IRPPackage addPackageFromProfileRpyFolder(IRPProject toTheProject, String withTheName){
		
		RequirementsAnalysisPlugin.getRhapsodyApp().addToModel(
				"$OMROOT\\Profiles\\SysMLHelper\\SysMLHelper_rpy\\" + withTheName + ".sbs", 1);	
		
		IRPPackage thePackage = (IRPPackage) toTheProject.findElementsByFullName( withTheName, "Package");
				
		return thePackage;
	}
	
	protected static void applySimpleMenuStereotype(IRPProject toTheProject) {
		
		String theName = "RequirementsAnalysisProfile::SimpleMenu";
		
		IRPModelElement theEl = toTheProject.findElementsByFullName(theName, "Stereotype");
		
		if (theEl != null && theEl instanceof IRPStereotype){
			IRPStereotype theStereotype = (IRPStereotype)theEl;
			
			toTheProject.setStereotype( theStereotype );
			toTheProject.changeTo("SysML");
			
			Logger.writeLine(toTheProject, "was changed to " + theName);
			Logger.writeLine("Remove the «SimpleMenu» stereotype to return the 'Add New' menu");

		} else {
			Logger.writeLine("Error in applySimpleMenuStereotype, unable to find stereotype called " + theName);
		}	
	}
	
	protected static void removeSimpleMenuStereotypeIfPresent(IRPProject onTheProject){
		
		String theName = "RequirementsAnalysisProfile::SimpleMenu";
		
		IRPModelElement theEl = onTheProject.findElementsByFullName(theName, "Stereotype");
		
		if (theEl != null && theEl instanceof IRPStereotype){
			IRPStereotype theStereotype = (IRPStereotype)theEl;
			
			onTheProject.removeStereotype(theStereotype);
			onTheProject.changeTo("SysML");
			
			Logger.writeLine("«SimpleMenu» stereotype removed from project to return the full 'Add New' menu to Rhapsody");

		} else {
			Logger.writeLine("Error in applySimpleMenuStereotype, unable to find stereotype called " + theName);
		}		
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)

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