package functionalanalysisplugin;

import java.util.List;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class GraphEdgeInfo {

	public static void dumpGraphicalProperties(IRPGraphElement theElement) {
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphicalProperties = theElement.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theGraphicalProperties) {

			System.out.println(theGraphicalProperty.getKey() + "::" + theGraphicalProperty.getValue());
			Logger.writeLine(theGraphicalProperty.getKey() + "::" + theGraphicalProperty.getValue());
		}
	}
	
	private IRPGraphEdge m_GraphEdge = null;
	
	public GraphEdgeInfo(IRPGraphEdge theGraphEdge) {		
		m_GraphEdge = theGraphEdge;
	}
	
	public int getStartX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );		
		return thePolygonInfo.getValueAt( 1 );
	}
	
	public int getStartY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );		
		return thePolygonInfo.getValueAt( 2 );
	}

	public int getEndX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );		
		return thePolygonInfo.getValueAt( 3 );
	}
	
	public int getEndY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );		
		return thePolygonInfo.getValueAt( 4 );
	}
	
	public int getMidX(){
		int xOffset = (getBiggestX()-getSmallestX()) / 2;
		int x = getSmallestX()+xOffset;
		return x;
	}
	
	public int getMidY(){
		int yOffset = (getBiggestY()-getSmallestY()) / 2;
		int y = getSmallestY()+yOffset;
		return y;
	}
	
	private int getBiggestX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );	
		int n = thePolygonInfo.getValueAt( 0 );
		int x = 0;
		
		for (int i = 0; i < n; i++) {
			int val = thePolygonInfo.getValueAt( i*2+1 );
			if( val > x ){
				x = val;
			}
		}
		
		return x;
	}
	
	private int getSmallestX(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );	
		int n = thePolygonInfo.getValueAt( 0 );
		int x = Integer.MAX_VALUE;
		
		for (int i = 0; i < n; i++) {
			int val = thePolygonInfo.getValueAt( i*2+1 );
			if( val < x ){
				x = val;
			}
		}
		
		return x;
	}
	
	private int getBiggestY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );	
		int n = thePolygonInfo.getValueAt( 0 );
		int y = 0;
		
		for (int i = 0; i < n; i++) {
			int val = thePolygonInfo.getValueAt( i*2+2 );
			if( val > y ){
				y = val;
			}
		}
		
		return y;
	}
	
	private int getSmallestY(){
		
		PolygonInfo thePolygonInfo = new PolygonInfo( m_GraphEdge );	
		int n = thePolygonInfo.getValueAt( 0 );
		int y = Integer.MAX_VALUE;
		
		for (int i = 0; i < n; i++) {
			int val = thePolygonInfo.getValueAt( i*2+2 );
			if( val < y ){
				y = val;
			}
		}
		
		return y;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
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
