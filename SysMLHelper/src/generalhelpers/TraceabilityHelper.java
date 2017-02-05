package generalhelpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class TraceabilityHelper {

	public static int countStereotypedDependencies(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName ){

		List<IRPModelElement> existingDeps = 
				GeneralHelpers.findElementsWithMetaClassAndStereotype(
						"Dependency", stereotypeName, fromElement, 0 );

		int isExistingFoundCount = 0;

		for( IRPModelElement theExistingDep : existingDeps ){

			IRPDependency theDep = (IRPDependency)theExistingDep;
			IRPModelElement theDependsOn = theDep.getDependsOn();

			if( theDependsOn.equals( toElement )){
				isExistingFoundCount++;
			}
		}
		
		return isExistingFoundCount;
	}
	
	public static IRPDependency getExistingStereotypedDependency(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName ){

		IRPDependency theExistingDependency = null;
		
		List<IRPModelElement> existingDeps = 
				GeneralHelpers.findElementsWithMetaClassAndStereotype(
						"Dependency", stereotypeName, fromElement, 0 );

		int isExistingFoundCount = 0;

		for( IRPModelElement theExistingDep : existingDeps ){

			IRPDependency theDependency = (IRPDependency)theExistingDep;
			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn.equals( toElement )){
				isExistingFoundCount++;
				theExistingDependency = theDependency;
			}
		}
		
		if( isExistingFoundCount > 1 ){
			Logger.writeLine( "Duplicate �" + stereotypeName + "� dependencies to " + Logger.elementInfo( toElement ) + 
					" were found on " + Logger.elementInfo( fromElement ) );
		}
		
		return theExistingDependency;
	}
	
	
	public static IRPDependency addStereotypedDependencyIfOneDoesntExist(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName ){
		
		IRPDependency theDependency = null;
		
		int isExistingFoundCount = 
				countStereotypedDependencies(
						fromElement, 
						toElement, 
						stereotypeName );
		
		if( isExistingFoundCount==0 ){
			theDependency = fromElement.addDependencyTo( toElement );
			theDependency.addStereotype( stereotypeName, "Dependency" );
			
			Logger.writeLine( "Added a �" + stereotypeName + "� dependency to " + 
					Logger.elementInfo( fromElement ) + " (to " + Logger.elementInfo( toElement ) + ")" );
		} else {
			Logger.writeLine( "Skipped adding a �" + stereotypeName + "� dependency to " + Logger.elementInfo( fromElement ) + 
					" (to " + Logger.elementInfo( toElement ) + 
					") as " + isExistingFoundCount + " already exists" );
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
	
	public static Set<IRPModelElement> getElementsThatHaveStereotypedDependenciesFrom(
			IRPModelElement theElement, 
			String withDependencyStereotype ){
		
		Set<IRPModelElement> theEls = new HashSet<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();
		
		for( IRPDependency theDependency : theExistingDeps ){
			
			IRPModelElement theDependsOn = theDependency.getDependsOn();
			
			if( theDependsOn != null && theDependsOn instanceof IRPModelElement ){
				
				if( GeneralHelpers.hasStereotypeCalled( 
						withDependencyStereotype, theDependency) ){
					
					theEls.add( theDependsOn );
				}	
			}
		}
		
		return theEls;
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #013 10-MAY-2016: Add support for sequence diagram req't and verification relation population (F.J.Chadburn)
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
	#083 09-AUG-2016: Add an Update attribute menu option and panel with add check operation option (F.J.Chadburn)
    #129 25-NOV-2016: Fixed addTraceabilityDependenciesTo to avoid creation of duplicate dependencies (F.J.Chadburn)
    #145 18-DEC-2016: Fix to remove warning with getWorkingPkgUnderDev unexpectedly finding 2 packages (F.J.Chadburn)
    #160 25-JAN-2017: Minor fixes to code found during development (F.J.Chadburn)
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)

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
