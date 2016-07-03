package generalhelpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class TraceabilityHelper {

	public static IRPDependency addStereotypedDependencyIfOneDoesntExist(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName){
		
		IRPDependency theDependency = null;
		
		List<IRPModelElement> existingDeps = 
				GeneralHelpers.findElementsWithMetaClassAndStereotype(
						"Dependency", stereotypeName, fromElement );
		
		int isExistingFoundCount = 0;
		
		for (IRPModelElement theExistingDep : existingDeps) {
			
			IRPDependency theDep = (IRPDependency)theExistingDep;
			IRPModelElement theDependsOn = theDep.getDependsOn();
			
			if( theDependsOn.equals( toElement )){
				isExistingFoundCount++;
			}
		}
		
		if( isExistingFoundCount==0 ){
			IRPDependency theDeriveDependency = 
					fromElement.addDependencyTo( toElement );
			
			theDeriveDependency.addStereotype( stereotypeName, "Dependency" );
			
			Logger.writeLine( "Added a «" + stereotypeName + "» dependency to " + Logger.elementInfo(fromElement) );
		} else {
			Logger.writeLine( "Skipped adding a «" + stereotypeName + "» dependency to " + Logger.elementInfo(fromElement) + 
					" as " + isExistingFoundCount + " already exists" );
		}
		
		return theDependency;
	}
	
	public static Set<IRPRequirement> getRequirementsThatTraceFrom(
			IRPModelElement theElement, boolean withWarning){
		
		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();
		
		for (IRPDependency theDependency : theExistingDeps) {
			
			IRPModelElement theDependsOn = theDependency.getDependsOn();
			
			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){
				
				IRPRequirement theReqt = (IRPRequirement)theDependsOn; 
				
				if (!theReqts.contains( theReqt )){
					
					theReqts.add( (IRPRequirement) theDependsOn );
					
				} else if (withWarning){
					
					Logger.writeLine( "Duplicate dependency to " + Logger.elementInfo( theDependsOn ) + 
							" was found on " + Logger.elementInfo( theElement ));
				} 			
			}
		}
		
		return theReqts;
	}
	
	public static Set<IRPRequirement> getRequirementsThatTraceFromWithStereotype(
			IRPModelElement theElement, String withDependencyStereotype){
		
		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();
		
		for (IRPDependency theDependency : theExistingDeps) {
			
			IRPModelElement theDependsOn = theDependency.getDependsOn();
			
			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){
				
				if (GeneralHelpers.hasStereotypeCalled( withDependencyStereotype, theDependency) ){
					theReqts.add( (IRPRequirement) theDependsOn );
				}	
			}
		}
		
		return theReqts;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #013 10-MAY-2016: Add support for sequence diagram req't and verification relation population (F.J.Chadburn)
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)

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
