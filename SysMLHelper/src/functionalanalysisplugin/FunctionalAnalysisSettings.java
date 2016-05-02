package functionalanalysisplugin;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPTag;

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
