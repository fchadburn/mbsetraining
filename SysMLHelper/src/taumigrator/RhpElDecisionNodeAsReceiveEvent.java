package taumigrator;

import com.telelogic.rhapsody.core.*;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElDecisionNodeAsReceiveEvent extends RhpElGraphNode {

	protected String _connectorType = null;
	protected String _text = null;

	public RhpElDecisionNodeAsReceiveEvent(
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

	public RhpElDecisionNodeAsReceiveEvent(
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
			RhpEl treeRoot ) throws Exception {

		_rhpEl = null;

		Logger.info("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		Logger.info("ReceiveEvent DecisionNode _text = " + _text );
		
		Logger.info( "The parent is " + Logger.elementInfo( parent.get_rhpEl() ) );

		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();

		IRPModelElement theParentOfDiagram = parent.getParent().get_rhpEl();
		Logger.info("The parent of diagram is " + Logger.elementInfo(theParentOfDiagram));

		IRPState theRootState = theActivityDiagram.getRootState();

		String theLegalName = 
				GeneralHelpers.toMethodName( "ev" + _text, 100 );

		IRPModelElement theEventEl =
				theParentOfDiagram.getProject().findNestedElementRecursive( 
						theLegalName, "Event" );

		if( theEventEl == null ){

			IRPModelElement thePkg = theParentOfDiagram.getOwner();

			theEventEl = thePkg.addNewAggr("Event", theLegalName );

			IRPEvent theEvent = (IRPEvent)theEventEl;
			
			@SuppressWarnings("unused")
			IRPArgument theArgument = theEvent.addArgument("value");
		}

		// condition connector; see also Fork, History, Join, and Termination
		IRPAcceptEventAction theAcceptEventAction = 
				theActivityDiagram.addAcceptEventAction( 
						"", 
						theRootState );

		theAcceptEventAction.setEvent( (IRPEvent) theEventEl );
		theAcceptEventAction.setDisplayName( _text );

		// condition connector; see also Fork, History, Join, and Termination
		_rhpEl = theAcceptEventAction;

		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				40 );

		for( Object o : theAcceptEventAction.getSubStateVertices().toList() ){
			if (o instanceof IRPPin) {
				IRPPin pin = (IRPPin) o;
				
				IRPGraphNode thePinGraphNode = (IRPGraphNode) GeneralHelpers.getCorrespondingGraphElement( 
						pin, theActivityDiagramGE );
				
//				GraphNodeInfo thePinGraphNodeInfo = new GraphNodeInfo(thePinGraphNode);
				
				int xPinPos = _xPosition + _nWidth/2;
				int yPinPos = _yPosition + _nHeight + 6;
				
				thePinGraphNode.setGraphicalProperty("Position", xPinPos + "," + yPinPos );
			}
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