package functionalanalysisplugin;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class PolygonInfo {

	private IRPGraphElement m_GraphElement = null;
	private String[] component; 
	
	public PolygonInfo(IRPGraphElement theGraphNode) {
		
		m_GraphElement = theGraphNode;
		
		IRPGraphicalProperty theGraphicalProperty = 
				m_GraphElement.getGraphicalProperty("Polygon");
		
		String theValue = theGraphicalProperty.getValue();		
		component = theValue.split(",");		
	}
	
	public int getValueAt(int theIndex){
				
		int theResult = -999;
		
		try {
			theResult = Integer.parseInt( component[theIndex] );
			
		} catch (Exception e) {
			Logger.writeLine("Error, exception in getValueAt("+ theIndex + ")");
		}	
		
		return theResult;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #019 15-MAY-2016: (new) Improvements to Functional Analysis Block default naming approach (F.J.Chadburn)
    #069 20-JUL-2016: Fix population of events/ops on diagram when creating from a transition (F.J.Chadburn)

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