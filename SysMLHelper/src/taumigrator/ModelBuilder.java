package taumigrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelBuilder {

	private static final String CONTROL_FLOW = "ControlFlow";

	Set<String> _ignoreTypes = new HashSet<>();
	Set<String> _skipTypes = new HashSet<>();
	Set<String> _includeTypes = new HashSet<>();
	Set<String> _relationTypes = new HashSet<>();

	Map<String,Node> _guidToNodeCache = new HashMap<>();

	Pattern _p = Pattern.compile("\\s*Integer\\s+(\\w+)");

	public ModelBuilder() {

		_includeTypes.add("u2");
		_includeTypes.add("Package");
		_includeTypes.add("Class");
		_includeTypes.add("StatechartDiagram");

		_includeTypes.add("State");
		_includeTypes.add("StateSymbol");

		_includeTypes.add("InputSymbol");
		_includeTypes.add("FlowLine");
		_includeTypes.add("OutputSymbol");
		_includeTypes.add("DecisionSymbol");
		_includeTypes.add("StartSymbol");
		_includeTypes.add("TaskSymbol");
		////		_includeTypes.add("DecisionAnswerSymbol");
		_includeTypes.add("Signal");
		_includeTypes.add("Parameter");
		_includeTypes.add("TextSymbol");
		_includeTypes.add("JunctionSymbol");

		_ignoreTypes.add("Resource");
		_ignoreTypes.add("cStereotypeInstance");
		_ignoreTypes.add("cClientDependency");
		_ignoreTypes.add("cHiddenStereotypeInstance");
		_ignoreTypes.add("cState");
		_ignoreTypes.add("State");

		_skipTypes.add("cDiagram");
		_skipTypes.add("cInlineMethod");
		_skipTypes.add("cOwnedMember");
		_skipTypes.add("SimpleStateMachine");
		_skipTypes.add("StateMachine");
		_skipTypes.add("cDiagramElement");
		_skipTypes.add("cOwnedMember");
		_skipTypes.add("cDecisionAnswerSymbol");
		_skipTypes.add("StartSymbol");
		_skipTypes.add("cParameter");
	}	

	private Node getChildNodeWith(
			String theName,
			Node undernearthNode ){

		Node theChildNode = null;

		NodeList nodeList = undernearthNode.getChildNodes();

		for( int i = 0; i < nodeList.getLength(); i++ ){

			Node theCandidate = nodeList.item( i );

			//			Logger.info( "getChildNodeWith is looking at " + theCandidate.getNodeName() );

			if( theCandidate.getNodeType() == Node.ELEMENT_NODE &&
					theCandidate.getNodeName() == theName ){

				theChildNode = theCandidate;
				break;
			}
		}

		return theChildNode;
	}

	private Node getChildNodeWith(
			String[] theNames,
			Node undernearthNode ){

		Node childNode = getChildNodeWith(
				theNames[0], undernearthNode );

		int length = theNames.length;

		if( childNode != null && length > 1 ){

			String[] partialPath = new String[length-1];

			System.arraycopy(theNames,1,partialPath,0,length-1);

			childNode = getChildNodeWith(
					partialPath, 
					childNode );
		}

		return childNode;
	}

	private void buildGuidCache( 
			Node theParent ) throws Exception{

		//Recurse down the DOM tree
		NodeList nodeList = theParent.getChildNodes();

		for( int i = 0; i < nodeList.getLength(); i++ ){

			Node theChild = nodeList.item( i );

			if( theChild.getNodeType() == Node.ELEMENT_NODE ){

				Element theElement = (Element)theChild;

				String theGuid = theElement.getAttribute("Guid");

				if( theGuid != null && 
						!theGuid.isEmpty() ){

					Logger.info( "Adding " + theGuid + " of type " + 
							theElement.getNodeName() + " to the cache ");

					_guidToNodeCache.put( theGuid, theElement );
				}

				buildGuidCache( 
						theChild );
			}
		}
	}


	private void recurseDownXMLTree( 
			Node theRealParent, 
			Node thePerceivedParent,
			RhpEl theParentNode ) throws Exception{

		Logger.info( "recurseDownXMLTree invoked for " + thePerceivedParent.getNodeName() );

		//Recurse down the DOM tree
		NodeList nodeList = theRealParent.getChildNodes();

		for( int i = 0; i < nodeList.getLength(); i++ ){

			Node theChild = nodeList.item( i );

			if( theChild.getNodeType() == Node.ELEMENT_NODE ){

				String theNodeName = theChild.getNodeName();

				try {
					if( _skipTypes.contains( theNodeName ) ){

						Logger.info( "Recurse underneath " + theNodeName );

						recurseDownXMLTree( 
								theChild, 
								theRealParent,
								theParentNode );

					} else if( _includeTypes.contains( theNodeName ) ){

						createElementNodeUnderneath(
								theParentNode, 
								theChild );

					} else {
						Logger.info( "Ignoring " + theNodeName );
					}

				} catch (Exception e) {
					Logger.info("Unhandled exception e=" + e.getMessage());
				}
			}
		}
	}

	private boolean doesAFlowLineStartFrom(
			String theGuid,
			Node underneathNode ){

		boolean doesAFlowLineStartFrom = false;

		//Recurse down the DOM tree
		NodeList nodeList = underneathNode.getChildNodes();

		for( int i = 0; i < nodeList.getLength(); i++ ){

			Node theChild = nodeList.item( i );

			if( theChild.getNodeType() == Node.ELEMENT_NODE ){

				String theNodeName = theChild.getNodeName();

				if( theNodeName.equals( "FlowLine" ) ){

					String[] path = {
					"rSrc" };

					Node dstNode = getChildNodeWith( path, theChild );

					String theSrcGuid = getR( dstNode );

					if( theSrcGuid.equals(theGuid) ){
						Logger.info("A FlowLine match was found!");
						doesAFlowLineStartFrom = true;
						break;
					} else {
						Logger.info("A FlowLine match was not found");
					}					
				}
			}
		}

		return doesAFlowLineStartFrom;
	}

	private void createElementNodeUnderneath(
			RhpEl theParentNode,
			Node theChild ) throws Exception{

		List<RhpEl> theElements = new ArrayList<RhpEl>();

		String theNodeName = theChild.getNodeName();

		Logger.info("createElementNodeUnderneath " + theParentNode.getString() + " to create a " + theNodeName );

		Element theChildEl = (Element) theChild;
		String theGuid = theChildEl.getAttribute( "Guid" );

		if( theNodeName == "Package" ){

			String theName = theChildEl.getAttribute( "Name" );

			RhpEl theElement = new RhpElPackage(
					theName, 
					theNodeName, 
					theGuid );

			theElements.add( theElement );

		} else if( theNodeName == "Signal" ){

			String theName = theChildEl.getAttribute( "Name" );

			RhpEl theElement = new RhpElEvent(
					theName, 
					theNodeName, 
					theGuid );

			theElements.add( theElement );


		} else if( theNodeName == "JunctionSymbol" ){

			String thePosition = theChildEl.getAttribute("Position");
			String theSize = theChildEl.getAttribute("Size");
			String theText = theChildEl.getAttribute("Text");

			RhpEl theElement = new RhpElDiagramConnector(
					"", 
					theNodeName, 
					theGuid, 
					theText, 
					thePosition, 
					theSize);

			theElements.add( theElement );

		} else if( theNodeName == "TextSymbol" ){

			//			String theName = theChildEl.getAttribute( "Name" );
			String theText = theChildEl.getAttribute("Text");

			String split[] = theText.split(";");

			for (String string : split) {
				Matcher m = _p.matcher( string );

				if( m.matches() ){
					String theAttributeName = m.group( 1 );

					RhpEl theElement = new RhpElAttribute(
							theAttributeName, 
							theNodeName, 
							theGuid );

					RhpEl theParent = theParentNode.getParent();
					addChildElementToParent( theElement, theParent);
				}
			}

		} else if( theNodeName == "Parameter" ){

			String theName = theChildEl.getAttribute( "Name" );

			if( theName.equals("") ){

				Logger.info( "Parameter with no name" );
				theName = "value";
			} else {
				throw new Exception( "Wow, a parameter with a name" );
			}

			RhpEl theElement = new RhpElEventArgument(
					theName, 
					theNodeName, 
					theGuid );

			theElements.add( theElement );

		} else if( theNodeName == "InputSymbol" ){

			String theName = theChildEl.getAttribute( "Name" );

			String thePosition = theChildEl.getAttribute("Position");
			String theSize = theChildEl.getAttribute("Size");
			String theText = theChildEl.getAttribute("Text");

			Logger.info( "InputSymbol found with the text " + theText );
			//the

			Node rTriggeredTransition = 
					getChildNodeWith( "rTriggeredTransition", theChildEl );

			String theTTGuid = 
					((Element) rTriggeredTransition).getAttribute(
							"R" ).replaceFirst( "uid:", "" );

			Node theTriggeredTransition = _guidToNodeCache.get( theTTGuid );

			String[] path = {
					"cTrigger", 
					"Trigger", 
					"rDefinition", 
					"Ident", 
			"rDefinition" };

			Node dstNode = getChildNodeWith( path, theTriggeredTransition );

			String theSignalGuid = getR( dstNode );

			RhpEl theElement = new RhpElAcceptEventAction(
					theName, 
					theNodeName, 
					theGuid, 
					theSignalGuid,
					theText, 
					thePosition, 
					theSize);

			theElements.add( theElement );

		} else if( theNodeName == "Class" ){

			String theName = theChildEl.getAttribute( "Name" );

			RhpEl theElement = new RhpElClass(
					theName, 
					theNodeName, 
					theGuid );

			theElements.add( theElement );

		} else if( theNodeName == "StatechartDiagram" ){

			String theName = theChildEl.getAttribute( "Name" );

			RhpEl theElement = new RhpElActivityDiagram(
					theName, 
					theNodeName, 
					theGuid );

			theElements.add( theElement );

		} else if( theNodeName == "DecisionSymbol" ){

			String thePosition = theChildEl.getAttribute("Position");
			String theSize = theChildEl.getAttribute("Size");
			String theText = theChildEl.getAttribute("Text");

			Logger.info( "DecisionSymbol found with the text " + theText );

			if( theText.startsWith("\"") ){

				//				RhpEl theElement = new RhpElDecisionNodeAsReceiveEvent(
				//						"", 
				//						theNodeName, 
				//						theGuid, 
				//						"DecisionNode", 
				//						theText, 
				//						thePosition, 
				//						theSize);

				RhpEl theElement = new RhpElDecisionNodeAsCallOperation(
						"", 
						theNodeName, 
						theGuid, 
						theText, 
						thePosition, 
						theSize);

				theElements.add( theElement );

			} else {

				RhpEl theElement = new RhpElDecisionNode(
						"", 
						theNodeName, 
						theGuid, 
						theText, 
						thePosition, 
						theSize);

				theElements.add( theElement );
			}

		} else if( theNodeName == "DecisionAnswerSymbol" ){

			@SuppressWarnings("unused")
			String thePosition = theChildEl.getAttribute("Position");
			String theText = theChildEl.getAttribute("Text");

			Element theParentParentNode = 
					(Element) theChildEl.getParentNode().getParentNode();

			String theSegmentPoints = "";
			String theSrcGuid = theParentParentNode.getAttribute("Guid");
			String theDstGuid = null;


			Logger.info( "theSrcGuid = " + theSrcGuid );

			Node rDecisionAnswer = 
					getChildNodeWith( "rDecisionAnswer", theChildEl );

			if( rDecisionAnswer != null ){
				theDstGuid = getDestinationSrcGuid( rDecisionAnswer );
			}

			//			_guidToNodeCache.get(key);

			RhpEl theElement = new RhpElTransition( 
					"", 
					"Transition", 
					theGuid,
					theSegmentPoints,
					false,
					theSrcGuid,
					theDstGuid,
					theText,
					ModelBuilder.CONTROL_FLOW ); // theGuard

			theElements.add( theElement );

		} else if( theNodeName == "StateSymbol" ){ 

			String thePosition = theChildEl.getAttribute("Position");
			String theSize = theChildEl.getAttribute("Size");
			String theText = theChildEl.getAttribute("Text");

			if( doesAFlowLineStartFrom( theGuid, theChildEl.getParentNode() ) ){

				Logger.info("Ignoring the StateSymbol as it is unnecessary for execution");

			} else {
				RhpEl theElement = new RhpElFinalFlow(
						"", 
						theNodeName, 
						theGuid, 
						theText, 
						thePosition, 
						theSize);

				theElements.add( theElement );
			}

		} else if( theNodeName == "TaskSymbol" ){

			String thePosition = theChildEl.getAttribute("Position");
			String theSize = theChildEl.getAttribute("Size");
			String theText = theChildEl.getAttribute("Text");

			if( theText.startsWith("//") ){

				RhpEl theElement = new RhpElCallOperation(
						"", 
						theNodeName, 
						theGuid, 
						theText, 
						thePosition, 
						theSize);

				theElements.add( theElement );

			} else {
				RhpEl theElement = new RhpElState(
						"", 
						theNodeName, 
						theGuid, 
						"Action", 
						theText, 
						thePosition, 
						theSize);		
				theElements.add( theElement );
			}

		} else if( theNodeName == "OutputSymbol" ){

			String thePosition = theChildEl.getAttribute("Position");
			String theSize = theChildEl.getAttribute("Size");
			String theText = theChildEl.getAttribute("Text");

			Node rOutputAction = 
					getChildNodeWith( "rOutputAction", theChildEl );

			String theOAGuid = 
					((Element) rOutputAction).getAttribute(
							"R" ).replaceFirst( "uid:", "" );

			Node theOutputAction = _guidToNodeCache.get( theOAGuid );

			String[] path = {
					"cOutputItem", 
					"CallExpr", 
					"rCalled", 
					"Ident", 
			"rDefinition" };

			Node dstNode = getChildNodeWith( path, theOutputAction );

			String theSignalGuid = getR( dstNode );

			RhpEl theElement = new 
					RhpElSendAction(
							"", 
							theNodeName, 
							theGuid, 
							theSignalGuid, 
							theText, 
							thePosition, 
							theSize);

			theElements.add( theElement );

		} else if( theNodeName == "FlowLine" ){

			String theSegmentPoints = theChildEl.getAttribute("SegmentPoints");

			Element theSrc = (Element) getChildNodeWith("rSrc", theChild );
			Element theDst = (Element) getChildNodeWith("rDst", theChild );

			String theSrcGuid = theSrc.getAttribute("R").replaceFirst("uid:", "");
			String theDstGuid = theDst.getAttribute("R").replaceFirst("uid:", "");

			Element theSrcNode = (Element) _guidToNodeCache.get(theSrcGuid);
			Element theDstNode = (Element) _guidToNodeCache.get(theDstGuid);

			Logger.info("the Src node type is " + theSrcNode.getNodeName() + " with Text = " + theSrcNode.getAttribute("Text" ));
			Logger.info("the Dst node type is " + theDstNode.getNodeName() + " with Text = " + theDstNode.getAttribute("Text" ));

			if( theSrcNode != null && 
					theSrcNode.getNodeName() == "StartSymbol" ){

				RhpEl theElement = new RhpElTransition( 
						"", 
						"DefaultTransition", 
						theGuid,
						theSegmentPoints,
						true,
						theSrcGuid,
						theDstGuid,
						"",
						ModelBuilder.CONTROL_FLOW ); // theGuard

				theElements.add( theElement );

			} else if( theDstNode != null &&
					theDstNode.getNodeName() == "DecisionAnswerSymbol" ){

				// check to see if a transition has already been created?

				// do something special

				String theGuard = theDstNode.getAttribute("Text");

				RhpElTransition existingTrans = 
						getExistingChildTransitionMatchingSrcGuid(
								theParentNode, 
								theDstGuid );

				if( existingTrans != null ){

					Logger.info("Found existing transition");

					// skip the decision answer symbol to make a single transition

					Node theNewSrc = _guidToNodeCache.get( theSrcGuid );

					Logger.writeLine("Changing srcGuid from " + existingTrans.get_srcGuid() + " to " + theSrcGuid + " which is a " + theNewSrc.getNodeName() );
					existingTrans.set_srcGuid( theSrcGuid );
					existingTrans.set_guard( theGuard );
					existingTrans.appendSegmentPoints( theSegmentPoints );

					//					theElements.add( existingTrans );

				} else {

					RhpEl theElement = new RhpElTransition( 
							"", 
							"Transition", 
							theGuid,
							theSegmentPoints,
							false,
							theSrcGuid,
							theDstGuid,
							theGuard,
							ModelBuilder.CONTROL_FLOW ); // theGuard

					theElements.add( theElement );
				}

			} else if( theSrcNode != null && 
					theSrcNode.getNodeName() == "DecisionAnswerSymbol" ){

				// skip as this will be done by the flowline that ends at the decision answer symbol
				String theGuard = theSrcNode.getAttribute("Text");

				RhpElTransition existingTrans = 
						getExistingChildTransitionMatchingDstGuid(
								theParentNode, 
								theSrcGuid );

				if( existingTrans != null ){

					Logger.info("Found existing transition");
					Node theNewDst = _guidToNodeCache.get( theDstGuid );

					Logger.info("Changing dstGuid from " + existingTrans.get_srcGuid() + " to " + theSrcGuid + " which is a " + theNewDst.getNodeName() );

					// skip the decision answer symbol to make a single transition
					existingTrans.set_dstGuid( theDstGuid );
					existingTrans.set_guard( theGuard );
					existingTrans.appendSegmentPoints( theSegmentPoints );

					//					RhpElTransition laterTrans = 
					//							getExistingChildTransitionMatchingDstGuid(
					//									theParentNode, 
					//									theDstGuid );

				} else {
					RhpEl theElement = new RhpElTransition( 
							"", 
							"Transition", 
							theGuid,
							theSegmentPoints,
							false,
							theSrcGuid,
							theDstGuid,
							"", // theGuard
							ModelBuilder.CONTROL_FLOW ); 

					theElements.add( theElement );
				}

			} else {

				RhpEl theElement = new RhpElTransition( 
						"", 
						"Transition", 
						theGuid,
						theSegmentPoints,
						false,
						theSrcGuid,
						theDstGuid,
						"", // theGuard
						ModelBuilder.CONTROL_FLOW ); 

				theElements.add( theElement );
			}

		} else {

			throw new Exception("Unhandled type called " + theNodeName );
		}

		if( !theElements.isEmpty() ){
			Logger.info( theParentNode.getString() + " has " + theElements.size() + " child elements:" );

			for (RhpEl rhpEl : theElements) {

				theParentNode.addChild( rhpEl );

				recurseDownXMLTree( 
						theChild,
						theChild,
						rhpEl );
			}
		}
	}

	private void addChildElementToParent(RhpEl theElement, RhpEl theParent) {
		Logger.info("Adding " + theElement.getString() + " to parent " + theParent.getString() );
		theParent.addChild( theElement );
	}

	private RhpElTransition getExistingChildTransitionMatchingSrcGuid(
			RhpEl theParentNode, 
			String theGuid ){

		RhpElTransition existingTrans = null; 

		for( RhpEl child : theParentNode.children ){

			if( child instanceof RhpElTransition ){

				RhpElTransition theCandidate = (RhpElTransition)child;

				String theTransSrcGuid = theCandidate.get_srcGuid();

				if( theTransSrcGuid.equals( theGuid ) ){
					Logger.info( "Found a transition" );
					existingTrans = theCandidate;
					break;
				}
			}
		}

		return existingTrans;
	}

	private RhpElTransition getExistingChildTransitionMatchingDstGuid(
			RhpEl theParentNode, 
			String theGuid ){

		RhpElTransition existingTrans = null; 

		for( RhpEl child : theParentNode.children ){

			if( child instanceof RhpElTransition ){

				RhpElTransition theCandidate = (RhpElTransition)child;

				String theTransDstGuid = theCandidate.get_dstGuid();

				if( theTransDstGuid.equals( theGuid ) ){
					Logger.info( "Found a transition" );
					existingTrans = theCandidate;
					break;
				}
			}
		}

		return existingTrans;
	}

	private String getDestinationSrcGuid(
			Node rDecisionAnswer ){

		String theDstGuid = null;

		//		String theRGuid = ((Element) rDecisionAnswer).getAttribute("R");

		//		Logger.info( theRGuid );

		if( rDecisionAnswer != null ){

			String[] path = {
					"cAction", 
					"CompoundAction", 
					"cAction", 
					"OutputAction", 
					"cOutputItem",
					"CallExpr", 
					"rCalled",
					"Ident",
			"rDefinition" };

			Node dstNode = getChildNodeWith( path, rDecisionAnswer );

			theDstGuid = getR( dstNode );

		}

		return theDstGuid;
	}

	private String getR(
			Node dstNode ) {

		String theSignalGuid = null;

		if( dstNode != null ){

			theSignalGuid = 
					((Element) dstNode).getAttribute(
							"R" ).replaceFirst( "uid:", "" );

			Logger.info("theSignalGuid=" + theSignalGuid);

			Node theSignalNode = _guidToNodeCache.get( theSignalGuid );

			if( theSignalNode != null ){
				Logger.info("Success, found signal node");
			}
		}

		return theSignalGuid;
	}

	RhpEl parseXmlFile(
			String theFilename,
			RhpEl parentNode ){

		Document dom;

		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse( theFilename );

			//get the root of the XML document
			Node theXMLNode = dom.getLastChild();

			if( theXMLNode.getNodeType() == Node.ELEMENT_NODE ){

				Element theElement = (Element)theXMLNode;

				String theGuid = theElement.getAttribute("Guid");
				String theElementName = theElement.getAttribute("Name");
				String theType = theElement.getNodeName();

				Logger.info("theGuid=" + theGuid);
				Logger.info("theElementName=" + theElementName);
				Logger.info("theType=" + theType);
				Logger.info( "Found a root " + parentNode.getString() );

				buildGuidCache( theXMLNode );

				//Recurse down the DOM tree
				recurseDownXMLTree( theXMLNode, theXMLNode, parentNode );//, isIncludeSubElements );
			} else {

				UserInterfaceHelpers.showWarningDialog( "The file " + theFilename + 
						" is not valid as it doesn't start with an ELEMENT_NODE" );
			}

		} catch( Exception e ) {
			Logger.info( "The file is not valid XML. Error=" + e.getMessage() );
		}

		return parentNode;
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