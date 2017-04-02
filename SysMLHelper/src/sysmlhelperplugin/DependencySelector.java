package sysmlhelperplugin;

import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.List;

import com.telelogic.rhapsody.core.*;

public class DependencySelector {

	public static void selectDependsOnElementsFor(
			List<IRPModelElement> theSelectedEls ){
		
		IRPApplication theRhpApp = SysMLHelperPlugin.getRhapsodyApp();
		
		IRPCollection theDependsOnEls = theRhpApp.createNewCollection();
		
		for( IRPModelElement theCandidateEl : theSelectedEls ){
			
			Logger.writeLine(theCandidateEl, "owned by " + Logger.elementInfo( theCandidateEl.getOwner() ) + " was selected for DependsOn analysis");
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theNestedElDependencies = 
					theCandidateEl.getNestedElementsByMetaClass( "Dependency", 1 ).toList();
			
			for( IRPModelElement theNestElDependency : theNestedElDependencies ){

				IRPModelElement theDependsOn = 
						((IRPDependency) theNestElDependency).getDependsOn();
				
				if( theDependsOn != null ){
					theDependsOnEls.addItem( theDependsOn );
				}
			}
			
			if( theCandidateEl instanceof IRPDependency ){

				IRPModelElement theDependsOn = 
						((IRPDependency) theCandidateEl).getDependsOn();
				
				if( theDependsOn != null ){
					theDependsOnEls.addItem( theDependsOn );
				}
			}
		}
		
		if( theDependsOnEls.getCount() > 0 ){
			
			theRhpApp.selectModelElements( theDependsOnEls );
		} else {
			String theMsg = 
					"There were no depends on relations found underneath the " + 
					theSelectedEls.size() + " selected elements.";
			
			UserInterfaceHelpers.showInformationDialog( theMsg );
		}
	}
	
	public static void selectDependentElementsFor(
			List<IRPModelElement> theSelectedEls ){
		
		IRPApplication theRhpApp = SysMLHelperPlugin.getRhapsodyApp();
		
		IRPCollection theDependentEls = theRhpApp.createNewCollection();
		
		for( IRPModelElement theCandidateEl : theSelectedEls ){
			
			Logger.writeLine(theCandidateEl, "was selected for Dependent analysis");

			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = 
					theCandidateEl.getReferences().toList();
			
			for( IRPModelElement theReference : theReferences ){

				Logger.writeLine(theReference, " is a reference");
				
				if( theReference instanceof IRPDependency ){
					
					IRPModelElement theDependent = ((IRPDependency)theReference).getDependent();
					
					if( theDependent != null ){
						theDependentEls.addItem( theDependent );
					}
				}
			}
			
			if( theCandidateEl instanceof IRPDependency ){

				IRPModelElement theDependsOn = 
						((IRPDependency) theCandidateEl).getDependent();
				
				if( theDependsOn != null ){
					theDependentEls.addItem( theDependsOn );
				}
			}
		}
		
		if( theDependentEls.getCount() > 0 ){
			
			theRhpApp.selectModelElements( theDependentEls );
		} else {
			String theMsg = 
					"There were no dependent elements for the " + 
					theSelectedEls.size() + " selected elements.";
			
			UserInterfaceHelpers.showInformationDialog( theMsg );
		}
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #172 02-APR-2017: Added new General Utilities > Select Dependent element(s) option (F.J.Chadburn)

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

