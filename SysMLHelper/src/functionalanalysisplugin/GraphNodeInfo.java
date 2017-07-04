package functionalanalysisplugin;

import java.util.List;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class GraphNodeInfo {

	public static void dumpGraphicalProperties(IRPGraphElement theElement) {
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphicalProperties = theElement.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theGraphicalProperties) {

			System.out.println(theGraphicalProperty.getKey() + "::" + theGraphicalProperty.getValue());
			Logger.writeLine(theGraphicalProperty.getKey() + "::" + theGraphicalProperty.getValue());
		}
	}
	
	private IRPGraphNode m_GraphNode = null;
	
	public GraphNodeInfo(IRPGraphNode theGraphNode) {		
		m_GraphNode = theGraphNode;
	}
	
	public int getWidth(){
	
		return getBottomRightX() - getTopLeftX();
	}
	
	public int getHeight(){
		
		return getBottomRightY() - getTopLeftY();
	}
	
	public int getTopLeftX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 1 );
	}
	
	public int getTopLeftY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 2 );
	}

	public int getTopRightX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 3 );
	}
	
	public int getTopRightY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 4 );
	}
	
	public int getBottomRightX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 5 );
	}
	
	public int getBottomRightY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 6 );
	}
	
	public int getBottomLeftX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 7 );
	}
	
	public int getBottomLeftY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 8 );
	}
	
	public int getMiddleX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 3 ) - thePolygonInfo.getValueAt( 1 );
	}
	
	public int getMiddleY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphNode );		
		return thePolygonInfo.getValueAt( 8 ) - thePolygonInfo.getValueAt( 2 );
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #019 15-MAY-2016: (new) Improvements to Functional Analysis Block default naming approach (F.J.Chadburn)
    #212 04-JUL-2017: Added a MergeActors helper, currently only invoked via Eclipse (F.J.Chadburn) 

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
