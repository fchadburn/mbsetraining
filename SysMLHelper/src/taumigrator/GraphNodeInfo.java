package taumigrator;

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
	
	private IRPGraphNode _graphNode = null;
	private int _posX;
	private int _posY;
	private int _nWidth;
	private int _nHeight;
	
	public GraphNodeInfo(IRPGraphNode theGraphNode) throws Exception {		
		
		_graphNode = theGraphNode;
		
		IRPGraphicalProperty thePropertyForHeight = 
				_graphNode.getGraphicalProperty( "Height" );
		
		if( thePropertyForHeight == null ){
			throw new Exception( "Found that property for Height is null" );
		}
		
		_nHeight = Integer.parseInt( thePropertyForHeight.getValue() );
		
		IRPGraphicalProperty thePropertyForWidth = 
				_graphNode.getGraphicalProperty( "Width" );		
		
		if( thePropertyForWidth == null ){
			throw new Exception( "Found that property for Width is null" );
		}
		
		_nWidth = Integer.parseInt( thePropertyForWidth.getValue() );
		
		IRPGraphicalProperty thePropertyForPosition = 
				_graphNode.getGraphicalProperty( "Position" );
		
		if( thePropertyForPosition == null ){
			throw new Exception( "Found that property for Position is null" );
		}
		
		String[] splitPosition = thePropertyForPosition.getValue().split(",");		
		
		_posX = Integer.parseInt( splitPosition[0] );
		_posY = Integer.parseInt( splitPosition[1] );
	}
	
	public int getWidth(){
	
		return _nWidth;
	}
	
	public int getHeight(){
		
		return _nHeight;
	}
	
	public int getTopLeftX(){
		
		return _posX;
	}
	
	public int getTopLeftY(){
		
		return _posY;
	}

	public int getTopRightX(){
		
		return _posX + _nWidth;
	}
	
	public int getTopRightY(){
		
		return _posY + _nHeight;
	}
	
	public int getBottomRightX(){
		
		return _posX + _nWidth;
	}
	
	public int getBottomRightY(){
		
		return _posY + _nHeight;
	}
	
	public int getBottomLeftX(){
		
		return _posX;
	}
	
	public int getBottomLeftY(){
		
		return _posY + _nHeight;
	}
	
	public int getMiddleX(){
		
		return _posX + _nWidth / 2;
	}
	
	public int getMiddleY(){
		
		return _posY+ _nHeight / 2;
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #251 29-MAY-2019: First official version of new TauMigratorProfile (F.J.Chadburn)

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