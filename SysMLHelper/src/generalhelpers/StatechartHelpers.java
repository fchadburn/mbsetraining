package generalhelpers;

import java.util.List;

import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPState;
import com.telelogic.rhapsody.core.IRPStatechart;
import com.telelogic.rhapsody.core.IRPStatechartDiagram;

public class StatechartHelpers {
	
	static public IRPState getStateCalled(
			String theName, 
			IRPStatechart inTheDiagram, 
			IRPModelElement ownedByEl ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsInDiagram = 
			inTheDiagram.getElementsInDiagram().toList();
		
		IRPState theState = null;
		
		int count = 0;
		
		for( IRPModelElement theEl : theElsInDiagram ){
			
			if( theEl instanceof IRPState 
					&& theEl.getName().equals( theName )
					&& getOwningClassifierFor( theEl ).equals( ownedByEl ) ){
				
				Logger.writeLine( "Found state called " + theEl.getName() + 
						" owned by " + theEl.getOwner().getFullPathName() );
				
				theState = (IRPState) theEl;
				count++;
			}
		}
		
		if (count != 1){
			Logger.writeLine( "Warning in getStateCalled (" + count + 
					") states called " + theName + " were found" );
		}
		
		return theState;
	}
	
	private static IRPModelElement getOwningClassifierFor(
			IRPModelElement theState ){
		
		IRPModelElement theOwner = theState.getOwner();
		
		while( theOwner.getMetaClass().equals( "State" ) || 
			   theOwner.getMetaClass().equals( "Statechart" ) ){
			
			theOwner = theOwner.getOwner();
		}
		
		Logger.writeLine( "The owner for " + Logger.elementInfo( theState ) + 
				" is " + Logger.elementInfo( theOwner ) );
			
		return theOwner;
	}	

	static public IRPGraphElement findGraphEl(
			IRPClassifier theClassifier, 
			String withTheName ){
		
		IRPGraphElement theFoundGraphEl = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStatechartDiagram> theStatechartDiagrams = 
				theClassifier.getStatechart().getNestedElementsByMetaClass(
						"StatechartDiagram", 1 ).toList();
		
		for (IRPStatechartDiagram theStatechartDiagram : theStatechartDiagrams) {
			
			Logger.writeLine( theStatechartDiagram, "was found owned by " + 
					Logger.elementInfo( theClassifier ) );
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = 
				theStatechartDiagram.getGraphicalElements().toList();
			
			for( IRPGraphElement theGraphEl : theGraphEls ){
				
				IRPModelElement theEl = theGraphEl.getModelObject();
				
				if( theEl != null ){
					Logger.writeLine( "Found " + theEl.getMetaClass() + 
							" called " + theEl.getName() );
					
					if( theEl.getName().equals( withTheName ) ){
						
						Logger.writeLine( "Success, found GraphEl called " + 
								withTheName + " in statechart for " + 
								Logger.elementInfo( theClassifier ) );
						
						theFoundGraphEl = theGraphEl;
						break;
					}
				}
			}
		}
		
		return theFoundGraphEl;
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #198 05-JUN-2017: Support for adding MonitoringConditions transitions moved into shared StatechartHelpers (F.J.Chadburn)

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

