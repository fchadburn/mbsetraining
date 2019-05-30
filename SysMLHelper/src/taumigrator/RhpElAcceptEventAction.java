package taumigrator;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.telelogic.rhapsody.core.*;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElAcceptEventAction extends RhpElGraphNode {

	String _text = null;
	String _signalGuid = null;
	
	Pattern _p = Pattern.compile(".*\\((\\w+)\\)");

	public RhpElAcceptEventAction(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theSignalGuid,
			String theText,
			String thePosition,
			String theSize ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, thePosition, theSize );

		_text = theText;
		_signalGuid = theSignalGuid;
		
		dumpInfo();
	}

	public RhpElAcceptEventAction(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theSignalGuid,
			RhpEl theParent,
			String theText,
			String thePosition,
			String theSize ) throws Exception {
		
		super(theElementName, theElementType, theElementGuid, theParent, thePosition, theSize);
		
		_text = theText;
		_signalGuid = theSignalGuid;

		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Create " + this.getString() + "\n";
		theMsg += "_text                = " + _text + "\n";
		theMsg += "_xPosition     = " + _xPosition + "\n";
		theMsg += "_yPosition     = " + _yPosition + "\n";
		theMsg += "_nWidth        = " + _nWidth + "\n";
		theMsg += "_nHeight       = " + _nHeight + "\n";
		theMsg += "_signalGuid    = " + _signalGuid + "\n";
		theMsg += "===================================\n";		
		Logger.info( theMsg );
	}

	
	@Override
	public IRPModelElement createRhpEl( 
			RhpEl treeRoot ) throws Exception {

		_rhpEl = null;
		
		Logger.writeLine("createRhpEl invoked for " + getString() + " owned by " + 
				parent.getString() + " with parent el=" + Logger.elementInfo( parent.get_rhpEl() ) );	
		
		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();

		String theLegalName = GeneralHelpers.makeLegalName(_text.replaceAll("[/(/)]", "") );
		
		if( theLegalName != _text ){
			Logger.info( "Changed name from " + _text + " to " + theLegalName);
		}
		
		IRPState theRootState = theActivityDiagram.getRootState();
		
		// condition connector; see also Fork, History, Join, and Termination
		IRPAcceptEventAction theAcceptEventAction = 
				theActivityDiagram.addAcceptEventAction( 
						theLegalName, 
						theRootState );
		
		theAcceptEventAction.setDisplayName( _text );
		
		RhpEl theModelEl = 
				treeRoot.findNestedElementWith( _signalGuid );
		
		if( theModelEl != null ){
			
			IRPModelElement theEventEl = theModelEl.get_rhpEl();
			
			if( theEventEl == null ){
				throw new Exception( "the EventEl is null, I'm expecting all events to havce been created in first pass" );
			}

			Logger.info( "Using signal " + Logger.elementInfo( theEventEl ) + " for accept event action" );

			IRPEvent theEvent = (IRPEvent) theEventEl;
			
			theAcceptEventAction.setEvent( (IRPEvent) theEvent );
			
			@SuppressWarnings("unchecked")
			List<IRPArgument> theArgs = theEvent.getArguments().toList();
			
			if( theArgs.size() == 1 ){
				
				Logger.writeLine("Found that signal has " + theArgs.size() + " args" );
			}
		} else {
			Logger.info("Odd. I could not find signal with guid " + 
					_signalGuid + " for accept event action" );
		}
		
		_rhpEl = theAcceptEventAction;
				
		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				40 ); // make all accept events height = 40 
		
		for (Object o:theAcceptEventAction.getSubStateVertices().toList()){
			if (o instanceof IRPPin) {
				IRPPin pin = (IRPPin) o;
//				Logger.info("Found that " + Logger.elementInfo( _rhpEl) + " has " + Logger.elementInfo( pin ) );
				
				Matcher m = _p.matcher( _text );
				
				if( m.matches() ){
					
					String theArgumentName = m.group( 1 );
					
					Logger.info("Found that " + Logger.elementInfo( _rhpEl) + " has " + 
							Logger.elementInfo( pin ) + " related to " + theArgumentName );
					
					// condition connector; see also Fork, History, Join, and Termination
					IRPState setValueAction = theRootState.addState( "" );

					setValueAction.setStateType( "Action" );
					
					setValueAction.setEntryAction( theArgumentName + " = " + pin.getName() + ";" );

					int xValueAction = _xPosition + _nWidth + 70;
					int yValueAction = _yPosition;  
					int nValueWidth = 100;
					int nValueHeight = 40;

					IRPGraphNode setValueGraphNode = theActivityDiagramGE.addNewNodeForElement(
							setValueAction, 
							xValueAction, 
							yValueAction, 
							nValueWidth, 
							nValueHeight );
					
					IRPTransition objFlow = pin.addTransition( setValueAction );

					objFlow.changeTo( "ObjectFlow" );
					
					theActivityDiagramGE.addNewEdgeForElement(
							objFlow,
							(IRPGraphNode)GeneralHelpers.getGraphElement( pin, theActivityDiagram ), 
							0, 
							0, 
							setValueGraphNode, 
							xValueAction=1, 
							yValueAction + nValueHeight/2 );
				}
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