package taumigrator;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class RhpElDecisionNodeAsCallOperation extends RhpElGraphNode {

	protected String _text = null;
	protected IRPConnector _decisionNode = null;
	protected IRPOperation _checkOp = null;
	protected IRPAttribute _attribute = null;
	
	public IRPAttribute get_attribute() {
		return _attribute;
	}

	public IRPOperation get_checkOp() {
		return _checkOp;
	}

	public IRPConnector get_decisionNode() {
		return _decisionNode;
	}

	public IRPGraphNode get_decisionNodeGraphNode() {
		return _decisionNodeGraphNode;
	}

	protected IRPGraphNode _decisionNodeGraphNode = null;

	public String get_text() {
		return _text;
	}

	public RhpElDecisionNodeAsCallOperation(
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

	public RhpElDecisionNodeAsCallOperation(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theConnectorType,
			String theText,
			String thePosition,
			String theSize ) throws Exception {

		super(theElementName, theElementType, theElementGuid, theParent, thePosition, theSize );

		_text = theText;

		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Constructor called for " + this.getString() + "\n";
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
		

		Logger.info("createRhpEl invoked for " + getString() + " owned by " + parent.getString() );
		Logger.info("DecisionNode _text = " + _text );

		Logger.info( "The parent is " + Logger.elementInfo( parent.get_rhpEl() ) );
		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();
		IRPState theRootState = theActivityDiagram.getRootState();

		IRPModelElement theOwner = parent.getParent().get_rhpEl();

//		Logger.info( "The owner is " + Logger.elementInfo( theOwner ) );
		
		IRPProject theProject = theOwner.getProject();

		boolean isQuotesString = _text.trim().startsWith("\"");
		
		String theStringWithoutQuotes = _text;
		
		if( isQuotesString ){
			theStringWithoutQuotes = _text.substring(1, _text.length()-1 );
		}
		
		String theAttributeName = 
				GeneralHelpers.capitalize( 
						GeneralHelpers.toMethodName( 
								theStringWithoutQuotes, 100 ) );
		
//		Logger.info( "The attribute name is " + theCapitalizedName + 
//				" with corresponding " + theOperationName + " operation" );
		
		IRPModelElement existingAttribute = theOwner.findNestedElement( 
				theAttributeName, "Attribute" );

		if( existingAttribute != null ){
			Logger.info("The attribute for '" + _text + " already exists = " + Logger.elementInfo( existingAttribute ) );
			
			_attribute = (IRPAttribute) existingAttribute;
			
			_checkOp = getCheckOperationFor( theOwner, theAttributeName, _text );
		
		} else {
			
			Logger.info("The attribute for '" + _text + " does not exist, creating one called = " + theAttributeName );

			_attribute = (IRPAttribute)theOwner.addNewAggr( "Attribute", theAttributeName );			

//			String theAttributeName = 
//			GeneralHelpers.determineUniqueNameBasedOn( 
//					theCapitalizedName, 
//					"Attribute", 
//					theOwner );

			_checkOp = getCheckOperationFor( theOwner, theAttributeName, _text );
		}
		
//		_rhpEl.setDisplayName( _text );

		IRPCallOperation theCallOp = theActivityDiagram.addCallOperation( "", theRootState );
		
		theCallOp.setOperation( _checkOp );
		
		// check that SysML profile is applied
		IRPStereotype theProjectNewTerm = theProject.getNewTermStereotype();
					
		if( theProjectNewTerm != null && theProjectNewTerm.getName().equals( "SysML" )){
			_attribute.changeTo( "ValueProperty" );
		}
		
		IRPModelElement theType = 
				theOwner.getProject().findElementsByFullName(
						"PredefinedTypes::RhpString", "Type" );
		
		_attribute.setType( (IRPClassifier) theType );
		_attribute.setDefaultValue( "\"Not Set\"" );
	
		_checkOp.setBody( "OM_RETURN( " + theAttributeName + " );" );
		
		_checkOp.setReturns( (IRPClassifier) theType );
		
		_rhpEl = theCallOp;

		_nHeight = 50; // override to fix at 50
		
		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				_nHeight );
		
		_graphNode.setGraphicalProperty("ForegroundColor", "255,128,0");
		_graphNode.setGraphicalProperty("BackgroundColor", "255,211,168");
		
		_decisionNode = theActivityDiagram.getRootState().addConnector(
				"Condition" );
		
		IRPTransition theNewTransition =
				theCallOp.addTransition( _decisionNode );
		
		theNewTransition.changeTo( "ControlFlow" );

		_decisionNodeGraphNode = theActivityDiagramGE.addNewNodeForElement(
				_decisionNode, 
				_xPosition + _nWidth/2 - 55, 
				_yPosition + _nHeight + 20, 
				110, 
				50 );
		
		@SuppressWarnings("unused")
		IRPGraphEdge theGraphEdge = theActivityDiagramGE.addNewEdgeForElement(
				theNewTransition, 
				_graphNode, 
				_xPosition + _nWidth/2, 
				_yPosition + _nHeight, 
				_decisionNodeGraphNode, 
				_xPosition + _nWidth/2, 
				_yPosition + _nHeight + 40 );
		
		//IRPGraphicalProperty theText =
		//		_graphNode.getGraphicalProperty("Text");

		//_graphNode.setGraphicalProperty("Text", _text.replaceAll("\"",""));

		return _rhpEl;
	}

	private IRPOperation getCheckOperationFor(
			IRPModelElement theOwner,
			String theCapitalizedName,
			String withText ) {
		
		IRPOperation theCheckOp = null;
		
		String theOperationName = GeneralHelpers.toMethodName( 
				"check" + GeneralHelpers.capitalize( theCapitalizedName ), 100 );
		
		IRPModelElement existingCheckOp = theOwner.findNestedElement( 
				theOperationName, "Operation" );
		
		if( existingCheckOp != null ){
			
			theCheckOp = (IRPOperation)existingCheckOp;
		} else {
			theCheckOp = (IRPOperation) theOwner.addNewAggr( 
							"Operation", theOperationName );
					
			theCheckOp.setDescription( withText + "?" );	
		}
		
		return theCheckOp;
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