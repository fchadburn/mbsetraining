package generalhelpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import requirementsanalysisplugin.RequirementsAnalysisPlugin;
import sysmlhelperplugin.SysMLHelperPlugin;

import com.telelogic.rhapsody.core.*;

public class PopulatePkg {

	final private static String m_SimpleMenuStereotypeName = "SimpleMenu";

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
	
	
	public static IRPProfile addProfileIfNotPresentAndMakeItApplied(
			String theProfileName, 
			IRPPackage appliedToPackage){
		
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
//			Logger.writeLine("Unable to delete " + andMetaClass + " with the name " 
//					+ theElementWithName  + " as it was not found underneath " + Logger.elementInfo( nestedUnderEl ) );
		}
	}	
	
	public static void browseAndAddByReferenceIfNotPresent(
			String thePackageName, 
			IRPProject inProject, 
			boolean relative ){
		
    	IRPModelElement theExistingPkg = inProject.findElementsByFullName( thePackageName, "Package" );
    	
    	if( theExistingPkg == null ){
    		
    		JFileChooser theFileChooser = new JFileChooser( System.getProperty("user.dir") );
    		theFileChooser.setFileFilter( new FileNameExtensionFilter( "Package", "sbs" ) );
    		
    		int choice = theFileChooser.showDialog( null, "Choose " + thePackageName );
    		
    		if( choice==JFileChooser.CANCEL_OPTION ){
    			Logger.writeLine("Operation cancelled by user when trying to choose " + thePackageName);
    			
    		} else if( choice==JFileChooser.APPROVE_OPTION ){
    			
    			File theFile = theFileChooser.getSelectedFile();
    			
    			String theTargetPath;
    			
				try {
					theTargetPath = theFile.getCanonicalPath();
					
		  			SysMLHelperPlugin.getRhapsodyApp().addToModelByReference( theTargetPath );
	    			
	    			if( relative ){
	    				
	        			int trimSize = thePackageName.length()+5;
	        			
	        			Path targetPath = Paths.get( theTargetPath.substring(0, theTargetPath.length()-trimSize) );
	        			Path targetRoot = targetPath.getRoot();
	        			
	        			Path sourcePath = Paths.get( 
	        					inProject.getCurrentDirectory().replaceAll(
	        							inProject.getName()+"$", "") );
	        			
	        			Path sourceRoot = sourcePath.getRoot();
	        			
	        			if( !targetRoot.equals( sourceRoot ) ){
	        				Logger.writeLine("Unable to set Unit called " + thePackageName + " to relative, as the drive letters are different");
		        			Logger.writeLine("theTargetDir root =" + targetPath.getRoot());
		        			Logger.writeLine("theTargetDir=" + targetPath);
		        			Logger.writeLine("theSourceDir root =" + sourcePath.getRoot());
		        			Logger.writeLine("theSourceDir=" + sourcePath);
	        			} else {
		        			Path theRelativePath = sourcePath.relativize(targetPath);
		        		     
		        			IRPModelElement theCandidate = inProject.findAllByName( thePackageName, "Package" );
		        			
		        			if( theCandidate != null && theCandidate instanceof IRPPackage ){
		        				
		        				IRPPackage theAddedPackage = (IRPPackage)theCandidate;
		        				
		        				theAddedPackage.setUnitPath( "..\\..\\" + theRelativePath.toString() );
		        				
		        				Logger.writeLine( "Unit called " + thePackageName + 
		        						".sbs was changed from absolute path='" + theTargetPath + 
		        						"' to relative path='" + theRelativePath + "'" );
		        			}
	        			}
	    			}
				} catch (IOException e) {
					Logger.writeLine("Error, unhandled IOException in PopulatePkg.browseAndAddByReferenceIfNotPresent");
				}
    		}
    	}
	}
	
	public static IRPPackage addPackageFromProfileRpyFolder(
			String withTheName,
			IRPProject toTheProject, 
			boolean byReference ){
		
		IRPApplication theRhpApp = RequirementsAnalysisPlugin.getRhapsodyApp();
		
		if( byReference ){
			theRhpApp.addToModelByReference(
					"$OMROOT\\Profiles\\SysMLHelper\\SysMLHelper_rpy\\" + withTheName + ".sbs" );				
			
		} else {
			theRhpApp.addToModel(
					"$OMROOT\\Profiles\\SysMLHelper\\SysMLHelper_rpy\\" + withTheName + ".sbs", 1);				
		}
		
		IRPPackage thePackage = (IRPPackage) toTheProject.findElementsByFullName( withTheName, "Package");
				
		return thePackage;
	}
	
	protected static void applySimpleMenuStereotype(IRPProject toTheProject) {
		
		IRPModelElement theEl = toTheProject.findAllByName(m_SimpleMenuStereotypeName, "Stereotype");
		
		if (theEl != null && theEl instanceof IRPStereotype){
			IRPStereotype theStereotype = (IRPStereotype)theEl;
			
			toTheProject.setStereotype( theStereotype );
			toTheProject.changeTo("SysML");
			
			Logger.writeLine( toTheProject, "was changed to " + m_SimpleMenuStereotypeName );
			Logger.writeLine("Remove the �" + m_SimpleMenuStereotypeName + "� stereotype to return the 'Add New' menu");

		} else {
			Logger.writeLine("Error in applySimpleMenuStereotype, unable to find stereotype called " + m_SimpleMenuStereotypeName);
		}	
	}
	
	protected static void removeSimpleMenuStereotypeIfPresent(IRPProject onTheProject){
		
		
		IRPModelElement theEl = onTheProject.findAllByName(m_SimpleMenuStereotypeName, "Stereotype");
		
		if (theEl != null && theEl instanceof IRPStereotype){
			IRPStereotype theStereotype = (IRPStereotype)theEl;
			
			onTheProject.removeStereotype(theStereotype);
			onTheProject.changeTo("SysML");
			
			Logger.writeLine("�" + m_SimpleMenuStereotypeName + "� stereotype removed from project to return the full 'Add New' menu to Rhapsody");

		} else {
			Logger.writeLine("Error in removeSimpleMenuStereotypeIfPresent, unable to find stereotype called " + m_SimpleMenuStereotypeName);
		}		
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #046 06-JUL-2016: Fix external RequirementsAnalysisPkg reference to be created with relative path (F.J.Chadburn)
    #053 13-JUL-2016: Fix #046 issues in calc of relative path when adding RequirementsAnalysisPkg (F.J.Chadburn)
    #061 17-JUL-2016: Ensure BasePkg is added by reference from profile to aid future integration (F.J.Chadburn)
    #113 13-NOV-2016: Stereotypes moved to GlobalPreferencesProfile to simplify/remove orphaned ownership issues (F.J.Chadburn)

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