package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.FunctionalAnalysisSettings;

public class TraceabilityHelper {

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
	
	public static void addTraceabilityDependenciesTo(
			IRPModelElement theElement, List<IRPRequirement> theReqtsToAdd){
	
		IRPStereotype theDependencyStereotype = 
				FunctionalAnalysisSettings.getStereotypeForFunctionTracing(theElement.getProject());
		
		if (theDependencyStereotype != null){
			for (IRPRequirement theReqt : theReqtsToAdd) {
				
				IRPDependency theDep = theElement.addDependencyTo(theReqt);
				theDep.setStereotype(theDependencyStereotype);		
				Logger.writeLine("Added a " + theDependencyStereotype.getName() + " dependency to " + Logger.elementInfo( theElement ));
			}
		} else {
			Logger.writeLine("Error in addTraceabilityDependenciesTo, unable to find stereotype to apply to dependencies");
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
