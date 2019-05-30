package taumigrator;

import com.telelogic.rhapsody.core.*;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElCallOperation extends RhpElGraphNode {

	String _text = null;

	public RhpElCallOperation(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theText,
			String thePosition,
			String theSize ) throws Exception{

		super( theElementName, theElementType, theElementGuid, thePosition, theSize );

		//		_stateType = theStateType;
		_text = theText;

		dumpInfo();
	}

	public RhpElCallOperation(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theText,
			String thePosition,
			String theSize ) throws Exception {

		super( theElementName, theElementType, theElementGuid, theParent, thePosition, theSize );

		_text = theText;

		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Constructor called for " + this.getString() + "\n";
		theMsg += "_text                = " + _text + "\n";
		theMsg += "_xPosition     = " + _xPosition + "\n";
		theMsg += "_yPosition     = " + _yPosition + "\n";
		theMsg += "_nWidth        = " + _nWidth + "\n";
		theMsg += "_nHeight       = " + _nHeight + "\n";
		theMsg += "===================================\n";		
		Logger.info( theMsg );
	}

	@Override
	public IRPModelElement createRhpEl( 
			RhpEl treeRoot ) throws Exception {

		_rhpEl = null;

		Logger.writeLine("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		Logger.info( "The parent is " + Logger.elementInfo( parent.get_rhpEl() ) );			
		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();
		IRPModelElement theParentOfDiagram = parent.getParent().get_rhpEl();
		Logger.info("The parent of diagram is " + Logger.elementInfo(theParentOfDiagram));

		IRPState theRootState = theActivityDiagram.getRootState();

		IRPClassifier theClassifier = (IRPClassifier)theParentOfDiagram;
		
		String theOperationName = GeneralHelpers.toMethodName( _text, 100 );
		
		// re-use existing operation if matching
		IRPModelElement theOperation = 
				theParentOfDiagram.findNestedElement( 
						theOperationName, "Operation" );

		if( theOperation == null ){
			theOperation = theClassifier.addOperation( theOperationName );
		}
		
		theOperation.setDescription( _text );
			
		IRPCallOperation theCallOp = theActivityDiagram.addCallOperation( "", theRootState );
		
		theCallOp.setOperation( (IRPInterfaceItem) theOperation );
		
		_rhpEl = theCallOp;

		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				_nHeight );

		_graphNode.setGraphicalProperty("ForegroundColor", "0,0,0");
		_graphNode.setGraphicalProperty("BackgroundColor", "242,242,242");
		
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