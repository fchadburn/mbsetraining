package generalhelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

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
	
	public static Set<IRPRequirement> getRequirementsThatTraceFromWithStereotypedRelation(
			IRPModelElement theElement, String withDependencyStereotype){
		
		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();
		
		for (IRPDependency theDependency : theExistingDeps) {
			
			IRPModelElement theDependsOn = theDependency.getDependsOn();
			
			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){
				
				if (GeneralHelpers.hasStereotypeCalled("verify", theDependency)){
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
