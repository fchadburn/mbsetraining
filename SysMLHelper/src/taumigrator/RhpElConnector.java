package taumigrator;

import com.telelogic.rhapsody.core.*;

import generalhelpers.Logger;

public class RhpElConnector extends RhpElGraphNode {

	protected String _connectorType = null;
	protected String _text = null;
	
	public String get_text() {
		return _text;
	}

	public RhpElConnector(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theConnectorType,
			String theText,
			String thePosition,
			String theSize ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, thePosition, theSize );

		_connectorType = theConnectorType;
		_text = theText;
		
		dumpInfo();
	}

	public RhpElConnector(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theConnectorType,
			String theText,
			String thePosition,
			String theSize ) throws Exception {
		
		super(theElementName, theElementType, theElementGuid, theParent, thePosition, theSize );
		
		_connectorType = theConnectorType;
		_text = theText;
				
		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Constructor called for " + this.getString() + "\n";
		theMsg += "_connectorType = " + _connectorType + "\n";
		theMsg += "_text          = " + _text + "\n";
		theMsg += "_xPosition     = " + _xPosition + "\n";
		theMsg += "_yPosition     = " + _yPosition + "\n";
		theMsg += "_nWidth        = " + _nWidth + "\n";
		theMsg += "_nHeight       = " + _nHeight + "\n";
		theMsg += "===================================\n";		
		Logger.info( theMsg );
	}

	@Override
	public IRPModelElement createRhpEl(
			RhpEl treeRoot ) {

		_rhpEl = null;
		
		Logger.writeLine("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		if( _connectorType == "DecisionNode" ){
		
			Logger.info( "The parent is " + Logger.elementInfo( parent.get_rhpEl() ) );
			
			IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
			IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();

			// condition connector; see also Fork, History, Join, and Termination
			_rhpEl = theActivityDiagram.getRootState().addConnector(
					"Condition");
			
			_rhpEl.setDisplayName( _text );
			
			_graphNode = theActivityDiagramGE.addNewNodeForElement(
					_rhpEl, 
					_xPosition, 
					_yPosition, 
					_nWidth, 
					_nHeight );
			
//			@SuppressWarnings("unused")
//			IRPGraphicalProperty theText =
//					_graphNode.getGraphicalProperty("Text");
			
			_graphNode.setGraphicalProperty("Text", _text.replaceAll("\"",""));
		}
		
		return _rhpEl;
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