package taumigrator;

import com.telelogic.rhapsody.core.*;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElFinalFlow extends RhpElGraphNode {

	String _text = null;

	public RhpElFinalFlow(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theText,
			String thePosition,
			String theSize ) throws Exception{

		super( theElementName, theElementType, theElementGuid, thePosition, theSize );

		_text = theText;

		dumpInfo();
	}

	public RhpElFinalFlow(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theStateType,
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
			RhpEl treeRoot ) {

		_rhpEl = null;

		Logger.writeLine("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		Logger.info( "The parent is " + Logger.elementInfo( parent.get_rhpEl() ) );			
		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();

		IRPState theRootState = theActivityDiagram.getRootState();

		String theLegalName = 
				GeneralHelpers.determineUniqueStateBasedOn(
						GeneralHelpers.makeLegalName( _text ), 
						theActivityDiagram.getRootState() );

		if( !_text.equals( theLegalName ) ){
			Logger.info("Changed name from " + _text + " to " + theLegalName);
		}

		// condition connector; see also Fork, History, Join, and Termination
		_rhpEl = theRootState.addState( theLegalName );

		IRPState theState = (IRPState)_rhpEl;

		theState.setStateType( "FlowFinal" );
		theState.setEntryAction( _text );

		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				_nHeight );

		return _rhpEl;
	}
}