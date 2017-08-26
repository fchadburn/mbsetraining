package requirementsanalysisplugin;

import com.telelogic.rhapsody.core.IRPGraphEdge;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPGraphNode;

import functionalanalysisplugin.GraphEdgeInfo;
import functionalanalysisplugin.GraphNodeInfo;

public class GraphElInfo {

	public static int getMidX( IRPGraphElement theGraphEl ){
		
		int x = 10;
		
		if( theGraphEl != null ){

			if (theGraphEl instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl );
				
				x = theNodeInfo.getMiddleX();
				
			} else if (theGraphEl instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) theGraphEl );
				
				x = theNodeInfo.getMidX();
			}
		} else {
			x = 20; // default is top right
		}
		
		return x;
	}
	
	public static int getMidY( IRPGraphElement theGraphEl ){
		
		int y = 10;
		
		if( theGraphEl != null ){

			if (theGraphEl instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl );
				
				y = theNodeInfo.getMiddleY();
				
			} else if (theGraphEl instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) theGraphEl );
				
				y = theNodeInfo.getMidY();
			}
		} else {
			y = 20; // default is top right
		}
		
		return y;
	}	
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #224 25-AUG-2017: Added new menu to roll up traceability to the transition and populate on STM (F.J.Chadburn)

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
