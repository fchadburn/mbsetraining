package functionalanalysisplugin;

import java.util.List;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class FunctionalAnalysisSettings {
	
	private static final String tagNameForPackageUnderDev = "packageUnderDev";
	private static final String tagNameForDependency = "traceabilityTypeToUseForFunctions";	
	
	public static IRPPackage getPackageUnderDev(IRPProject inTheProject){
		
		IRPPackage thePackage = null;
		
		IRPModelElement theRootPackage = inTheProject.findNestedElementRecursive(
				"FunctionalAnalysisPkg", "Package");
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( tagNameForPackageUnderDev );
			String thePackageName = theTag.getValue();
			
			thePackage = (IRPPackage) inTheProject.findNestedElementRecursive(thePackageName, "Package");
		} else {
			Logger.writeLine("Error in getPackageUnderDev, unable to find FunctionalAnalysisPkg");
		}
		
		if (thePackage==null){
			Logger.writeLine("Error in getPackageUnderDev, unable to determine packageUnderDev from the tag value");
		}
		
		return thePackage;
	}
	
	public static IRPPackage getEventPkgForPkgUnderDev(IRPProject inTheProject){
		
		IRPPackage theEventPkg = null;
		
		IRPClass theLogicalBlock = getBlockUnderDev( inTheProject );
		
		if( theLogicalBlock != null ){
			IRPModelElement theOwner = theLogicalBlock.getOwner();
			
			if( theOwner instanceof IRPPackage ){
				theEventPkg = (IRPPackage)theOwner;
			} else {
				Logger.writeLine( "Error in getEventPkgForPkgUnderDev: Can't find event pkg for " + Logger.elementInfo( theLogicalBlock ) );
			}
		}
		
		return theEventPkg;
	}
	
	public static IRPPackage getWorkingPkgUnderDev(IRPProject inTheProject){
		
		IRPPackage theWorkingPkg = null;
		
		int count = 0;
		IRPPackage thePackageUnderDev = getPackageUnderDev( inTheProject );
		
		if( thePackageUnderDev != null ){
			
			@SuppressWarnings("unchecked")
			List<IRPPackage> theNestedPkgs = 
					thePackageUnderDev.getNestedElementsByMetaClass("Package", 1).toList();
			
			for (IRPPackage theNestedPkg : theNestedPkgs) {
				
				List<IRPModelElement> theDependencies = 
						GeneralHelpers.findElementsWithMetaClassAndStereotype(
								"Dependency", "AppliedProfile", theNestedPkg);
				
				for (IRPModelElement theDependencyElement : theDependencies) {
					
					IRPDependency theDependency = (IRPDependency)theDependencyElement;
					
					IRPModelElement theDependsOn = theDependency.getDependsOn();
					
					if (theDependsOn.getName().equals("RequirementsAnalysisProfile")){
						IRPModelElement theDependent = theDependency.getDependent();
						theWorkingPkg = (IRPPackage) theDependent;
						count++;
					}
				}
			}
		}
		
		if (count==0){
			Logger.writeLine("Error in getWorkingPkgUnderDev, no working package was found");
		} else if (count > 1){
			Logger.writeLine("Error in getWorkingPkgUnderDev, " + count + " working packages were found when expecting one");
		}
		
		return theWorkingPkg;
	}
	
	public static IRPClass getBlockUnderDev(
			IRPProject inTheProject ){
		
		IRPClass theBlock = null;
		
		IRPPackage thePackageUnderDev = getPackageUnderDev( inTheProject );
		
		if( thePackageUnderDev != null ){
			
			List<IRPModelElement> theBlocks = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"Class", "LogicalSystem", thePackageUnderDev );
			
			if( theBlocks.size()==1 ){				
				theBlock = (IRPClass) theBlocks.get( 0 );
			} else {
				Logger.writeLine( "Error in getBlockUnderDev: Can't find LogicalSystem block for " + Logger.elementInfo( thePackageUnderDev ) );
			}		
		} else {
			Logger.writeLine("Error in getBlockUnderDev for " + Logger.elementInfo(inTheProject) + 
					", unable to determine package under development");
		}

		return theBlock;
	}
	
	public static IRPInstance getPartUnderDev(
			IRPProject inTheProject ){
		
		IRPInstance thePart = null;
		
		IRPPackage thePackageUnderDev = getPackageUnderDev( inTheProject );
		
		if( thePackageUnderDev != null ){
			
			List<IRPModelElement> theParts = 
						GeneralHelpers.findElementsWithMetaClassAndStereotype(
								"Part", "LogicalSystem", thePackageUnderDev );
				
			if( theParts.size()==1 ){
					
				thePart = (IRPInstance) theParts.get( 0 );				
			} else {
				Logger.writeLine( "Error in getPartUnderDev: Can't find LogicalSystem part for " + Logger.elementInfo( thePackageUnderDev ) );
			}		
		} else {
			Logger.writeLine( "Error in getPartUnderDev for " + Logger.elementInfo(inTheProject) + 
					", unable to determine package under development");
		}

		return thePart;
	}
	
	public static IRPStereotype getStereotypeForFunctionTracing(IRPProject inTheProject){
		
		IRPStereotype theStereotype = null;
		
		IRPModelElement theRootPackage = inTheProject.findNestedElementRecursive(
				"FunctionalAnalysisPkg", "Package");
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( tagNameForDependency );
			String theTagValue = theTag.getValue();
			
			theStereotype = (IRPStereotype) inTheProject.findNestedElementRecursive(theTagValue, "Stereotype");
		} else {
			Logger.writeLine("Error in getPackageUnderDev, unable to find FunctionalAnalysisPkg");
		}
		
		return theStereotype;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #054 13-JUL-2016: Create a nested BlockPkg package to contain the Block and events (F.J.Chadburn)
    
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
