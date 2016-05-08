package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class OperationCreator {
    static IRPApplication m_rhpApplication = null;
	
    public enum OperationType {
        INCOMING_EVENT, OUTGOING_EVENT, SYSTEM_OPERATION} 
        
    // test only
    public static void main(String[] args) {
	
    	@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = FunctionalAnalysisPlugin.getRhapsodyApp().getSelectedGraphElements().toList();
    	createOperationTypesFor( theSelectedGraphEls, FunctionalAnalysisPlugin.getActiveProject(), OperationType.INCOMING_EVENT );	
    }
    
	public static IRPInstance getPartUnderDev(IRPModelElement relatedToEl, IRPPackage inThePackage){
		
		IRPInstance partUnderDev = null;
		
		List<IRPModelElement> theBlocks = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype("Part", "LogicalSystem", inThePackage);
			
		if (theBlocks.size()==1){
				
			partUnderDev = (IRPInstance) theBlocks.get(0);
				
			Logger.writeLine(partUnderDev, "Found");
		} else {
			Logger.writeLine("Error in getPartUnderDev: Can't find LogicalSystem block");
		}

		return partUnderDev;
	}
	
	public static IRPModelElement getOwningClassifierFor(IRPModelElement theState){
		
		IRPModelElement theOwner = theState.getOwner();
		
		while (theOwner.getMetaClass().equals("State") || theOwner.getMetaClass().equals("Statechart")){
			theOwner = theOwner.getOwner();
		}
		
		Logger.writeLine("The owner for " + Logger.elementInfo(theState) + " is " + Logger.elementInfo(theOwner));
			
		return theOwner;
	}
	
	private static IRPModelElement createOperationTypeFor(
			IRPModelElement theModelElement, 
			IRPPackage forPackageUnderDev, 
			List<IRPRequirement> tracedToReqts, 
			OperationType theTypeToCreate){
		
		IRPModelElement theCreatedElement = null;
		
		switch (theTypeToCreate) {
		
		case INCOMING_EVENT:
			theCreatedElement = createIncomingEventFor(theModelElement, forPackageUnderDev, tracedToReqts);	
			break;
			
		case OUTGOING_EVENT:
			theCreatedElement = createOutgoingEventFor(theModelElement, forPackageUnderDev, tracedToReqts);	
			break;

		case SYSTEM_OPERATION:
			theCreatedElement = createSystemOperationFor(theModelElement, forPackageUnderDev, tracedToReqts);
			break;
			
		default:
			break;
		} 
		
		theCreatedElement.highLightElement();
		
		return theCreatedElement;
	}
	
	public static void createOperationTypesFor(
			List<IRPGraphElement> theSelectedGraphEls, 
			IRPProject inTheProject, 
			OperationType theTypeToCreate){
		
		IRPPackage thePackageUnderDev = FunctionalAnalysisSettings.getPackageUnderDev( inTheProject );
		
		if (doElementsAllHaveTheMetaClassCalled("Requirement", theSelectedGraphEls)){
			
			List<IRPRequirement> theReqts = getRequirementsIn(theSelectedGraphEls);
			
			if (!theReqts.isEmpty()){
				IRPModelElement theFirstReqt = theReqts.get(0);
				createOperationTypeFor(theFirstReqt, thePackageUnderDev, theReqts, theTypeToCreate);
			}
			
			for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
				bleedColorToElementsRelatedTo(theGraphEl);
			}
			
		} else {
			for (IRPGraphElement selectedGraphEl : theSelectedGraphEls) {
				IRPModelElement theModelObject = selectedGraphEl.getModelObject();
				
				if (theModelObject != null){
					Logger.writeLine(theModelObject, "is being processed");
					
					List<IRPRequirement> tracedToReqts = TraceabilityHelper.getRequirementsThatTraceFrom(theModelObject);
					createOperationTypeFor(theModelObject, thePackageUnderDev, tracedToReqts, theTypeToCreate);
					bleedColorToElementsRelatedTo( selectedGraphEl );
				}
			}
		}
	}
	
	public static IRPState getStateCalled(
			String theName, 
			IRPStatechart inTheDiagram, 
			IRPModelElement ownedByEl){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsInDiagram = inTheDiagram.getElementsInDiagram().toList();
		
		IRPState theState = null;
		
		int count = 0;
		
		for (IRPModelElement theEl : theElsInDiagram) {
			
			if (theEl instanceof IRPState 
					&& theEl.getName().equals(theName)
					&& getOwningClassifierFor(theEl).equals(ownedByEl)){
				
				Logger.writeLine("Found state called " + theEl.getName() + " owned by " + theEl.getOwner().getFullPathName());
				
				theState = (IRPState) theEl;
				count++;
			}
		}
		
		if (count != 1){
			Logger.writeLine("Warning in getStateCalled (" + count + ") states called " + theName + " were found");
		}
		
		return theState;
	}
	
	public static IRPModelElement createTestBenchSendFor(
			IRPEvent theEvent, IRPActor onTheActor, String withSendEventName){
		
		IRPEvent sendEvent = null;
		
		IRPStatechart theStatechart = onTheActor.getStatechart();
		
		if (theStatechart != null){
			
			IRPState theReadyState = getStateCalled("Ready", theStatechart, onTheActor);
			
			if (theReadyState != null){
				
				Logger.writeLine("Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName());
				
				sendEvent = (IRPEvent) theEvent.clone(withSendEventName, onTheActor.getOwner());
				
				Logger.writeLine("The state called " + theReadyState.getFullPathName() + " is owned by " + theReadyState.getOwner().getFullPathName());
				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );
				
				String actionText = "OPORT(pLogicalSystem)->GEN(" + theEvent.getName() + "(";
				
				@SuppressWarnings("unchecked")
				List<IRPArgument> theArguments = theEvent.getArguments().toList();
				
				for (Iterator<IRPArgument> iter = theArguments.iterator(); iter.hasNext();) {
					IRPArgument theArgument = (IRPArgument) iter.next();
					actionText += "params->" + theArgument.getName();
					
					if (iter.hasNext()) actionText += ",";
				}
				
				actionText += "));";
				
				theTransition.setItsAction(actionText);
				
				sendEvent.addStereotype("Web Managed", "Event");
						
			} else {
				Logger.writeLine("Error in createTestBenchSendFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state");
			}
		} else {
			Logger.writeLine("Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart");
		}
		
		return sendEvent;
	}
	
	public static void bleedColorToElementsRelatedTo( IRPGraphElement theGraphEl ){
		
		String theColorSetting = "255,0,0";
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		IRPModelElement theEl = theGraphEl.getModelObject();
		
		if (theEl != null){
			
			Logger.writeLine("Setting color to red for " + theEl.getName());
			theGraphEl.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theExistingDeps = theEl.getDependencies().toList();
			
			for (IRPDependency theDependency : theExistingDeps) {
				
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				if (theDependsOn != null && theDependsOn instanceof IRPRequirement){					
					bleedColorToGraphElsRelatedTo( theDependsOn, theColorSetting, theDiagram );
					bleedColorToGraphElsRelatedTo( theDependency, theColorSetting, theDiagram );
				}
			}
		}
	}

	private static void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, String theColorSetting, IRPDiagram onDiagram){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsRelatedToElement = 
				onDiagram.getCorrespondingGraphicElements( theEl ).toList();
		
		for (IRPGraphElement irpGraphElement : theGraphElsRelatedToElement) {
			
			irpGraphElement.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			IRPModelElement theModelObject = irpGraphElement.getModelObject();
			
			if (theModelObject != null){
				Logger.writeLine("Setting color to red for " + theModelObject.getName());
			}
		}
	}
	
	private static boolean doElementsAllHaveTheMetaClassCalled(
			String theMetaClass, List<IRPGraphElement> theGraphEls){
		
		boolean areAllTheSame = true;
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			
			IRPModelElement theModelObject = theGraphEl.getModelObject();
			
			if (theModelObject != null){
				if (!theModelObject.getMetaClass().equals(theMetaClass)){
					areAllTheSame = false;
					break;
				}
			}
		}
		
		return areAllTheSame;
	}
	
	private static List<IRPRequirement> getRequirementsIn(List<IRPGraphElement> theGraphElsList){
		
		List<IRPRequirement> theModelObjects = new ArrayList<IRPRequirement>();
		
		for (IRPGraphElement theGraphEl : theGraphElsList) {
			IRPModelElement theModelObject = theGraphEl.getModelObject();
			
			if (theModelObject != null && (theModelObject instanceof IRPRequirement)){
				IRPRequirement theReqt = (IRPRequirement)theModelObject;
				theModelObjects.add( theReqt );	
			}
		}
		
		return theModelObjects;
	}
	
	private static IRPOperation createSystemOperationFor(
			IRPModelElement selectedDiagramEl, 
			IRPPackage forPackageUnderDev, 
			List<IRPRequirement> tracedToReqts){
	
		IRPOperation theOperation = null;
		
		IRPInstance partUnderDev = getPartUnderDev(selectedDiagramEl, forPackageUnderDev);
		
		Logger.writeLine(selectedDiagramEl, " is being analyzed");
		
		String theSourceInfo = GeneralHelpers.getActionTextFrom(selectedDiagramEl);			
		Logger.writeLine("The sourceInfo is '" + theSourceInfo + "'");
	
		String theProposedName = GeneralHelpers.toMethodName(theSourceInfo);
		Logger.writeLine("The theProposedName is '" + theProposedName + "'");
		
		JPanel panel = new JPanel();
		panel.add(new JLabel("Operation to create:"));
		panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JTextField theTextField = new JTextField(theProposedName.length());	
		panel.add( theTextField );
		theTextField.setText(theProposedName);
		
		int choice = JOptionPane.showConfirmDialog(
				null, panel, "Please enter Operation name", JOptionPane.OK_CANCEL_OPTION);
		
		if( choice==JOptionPane.OK_OPTION ){
			String theName = theTextField.getText(); 
			
			if (!theName.isEmpty()){
			
				IRPClassifier theLogicalSystem = partUnderDev.getOtherClass();
				
				theOperation = theLogicalSystem.addOperation(theName);				
				TraceabilityHelper.addTraceabilityDependenciesTo(theOperation, tracedToReqts);			
				
			} else {
				Logger.writeLine("No text was entered");
			}
		}
		
		return theOperation;
	}
	
	private static IRPEvent createOutgoingEventFor(
			IRPModelElement selectedDiagramEl, 
			IRPPackage forPackageUnderDev, 
			List<IRPRequirement> tracedToReqts){
		
		IRPEvent theEvent = null;
		
		IRPInstance partUnderDev = getPartUnderDev(selectedDiagramEl, forPackageUnderDev);
		
		Logger.writeLine(selectedDiagramEl, " is being analyzed");
		
		IRPModelElement theActor = 
				GeneralHelpers.launchDialogToSelectElement(getActorsRelatedTo( partUnderDev ), "Select Actor to send Event to", true);
		
		if (theActor != null){
			
			String theSourceInfo = GeneralHelpers.getActionTextFrom(selectedDiagramEl);			
			Logger.writeLine("The sourceInfo is '" + theSourceInfo + "'");
		
			String theProposedName = GeneralHelpers.toMethodName("reqInform" + theActor.getName() + theSourceInfo);
			Logger.writeLine("The theProposedName is '" + theProposedName + "'");
			
			JPanel panel = new JPanel();
			panel.add(new JLabel("Event to create:"));
			panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			JCheckBox checkAddSend = new JCheckBox(
					"Add corresponding operation to send the Event via port");
			
			checkAddSend.setSelected(true);
			
			JCheckBox checkAddActiveFlag = new JCheckBox(
					"Add an 'active' argument to the event (e.g. for on/off conditions)");
			
			checkAddActiveFlag.setSelected(false);
			
			JTextField theTextField = new JTextField(theProposedName.length());
			
			panel.add( theTextField );
			panel.add( checkAddSend );
			panel.add( checkAddActiveFlag );
			
			theTextField.setText(theProposedName);
			
			int choice = JOptionPane.showConfirmDialog(
					null, panel, "Please enter event name", JOptionPane.OK_CANCEL_OPTION);
			
			if( choice==JOptionPane.OK_OPTION ){
				String theEventName = theTextField.getText(); 
				
				if (!theEventName.isEmpty()){
					
					theEvent = forPackageUnderDev.addEvent(theEventName);
					TraceabilityHelper.addTraceabilityDependenciesTo( theEvent, tracedToReqts );
					
					if (checkAddActiveFlag.isSelected()){
						theEvent.addArgument("active");
					}
					
					IRPModelElement theReception = theActor.addNewAggr("Reception", theEventName);
					TraceabilityHelper.addTraceabilityDependenciesTo( theReception, tracedToReqts );
					
					if (checkAddSend.isSelected()){
						
						@SuppressWarnings("unchecked")
						List<IRPModelElement> thePorts = 
								partUnderDev.getOtherClass().getNestedElementsByMetaClass("Port", 0).toList();
						
						IRPModelElement thePort = 
								GeneralHelpers.launchDialogToSelectElement(thePorts, "Select Port to send Event to", false);
						
						Logger.writeLine("Adding an inform Operation");
						IRPClassifier theLogicalSystem = partUnderDev.getOtherClass(); 
						IRPOperation informOp = theLogicalSystem.addOperation( GeneralHelpers.decapitalize( theEventName.replace("req", "") ) );
						TraceabilityHelper.addTraceabilityDependenciesTo( informOp, tracedToReqts );
						
						if (checkAddActiveFlag.isSelected()){
							informOp.addArgument("active");
							informOp.setBody("OPORT(" + thePort.getName()+")->GEN(" + theEventName + "( active ) );");
						} else {
							informOp.setBody("OPORT(" + thePort.getName()+")->GEN(" + theEventName + ");");
						}				
					}			
				}
			}
		}
		
		return theEvent;
	}

		
	private static IRPEvent createIncomingEventFor(
			IRPModelElement theModelElement, 
			IRPPackage forPackageUnderDev, 
			List<IRPRequirement> tracedToReqts){
		
		IRPEvent theEvent = null;
		
		IRPInstance partUnderDev = getPartUnderDev(theModelElement,forPackageUnderDev);
		
		Logger.writeLine(theModelElement, " is being analyzed");
		
		IRPModelElement theActor = 
				GeneralHelpers.launchDialogToSelectElement(
						getActorsRelatedTo( partUnderDev ), "Select Actor", true);
		
		if (theActor != null){
			
			String theSourceInfo = GeneralHelpers.getActionTextFrom(theModelElement);			
			Logger.writeLine("The action text is '" + theSourceInfo + "'");
			
			String theSourceMinusActor = theSourceInfo.replaceFirst( "^" + theActor.getName(), "" );
			Logger.writeLine("The source minus actor is '" + theSourceInfo + "'");
			
			String theProposedName = GeneralHelpers.toMethodName("req"+ theActor.getName() + theSourceMinusActor);
			Logger.writeLine("The theProposedName is '" + theProposedName + "'");

			JPanel panel = new JPanel();
			panel.add(new JLabel("Event to create:"));
			panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			JCheckBox checkAddSend = new JCheckBox(
					"Add corresponding 'send' event to the actor testbench");
			
			checkAddSend.setSelected(true);
			
			JCheckBox checkAddValueAttribute = new JCheckBox(
					"This is a continuous (or true/false) signal so add a value argument");
			
			JTextField theTextField = new JTextField(theProposedName.length());
			
			panel.add( theTextField );
			panel.add( checkAddSend );
			panel.add( checkAddValueAttribute );
			
			theTextField.setText(theProposedName);
			
			int choice = JOptionPane.showConfirmDialog(
					null, panel, "Please enter event name", JOptionPane.OK_CANCEL_OPTION);
			
			if( choice==JOptionPane.OK_OPTION ){
				String theEventName = theTextField.getText(); 
				
				if (!theEventName.isEmpty()){
					
					theEvent = forPackageUnderDev.addEvent( theEventName );
					TraceabilityHelper.addTraceabilityDependenciesTo( theEvent, tracedToReqts );
					
					IRPClassifier theLogicalSystem = partUnderDev.getOtherClass();
					IRPModelElement theReception = theLogicalSystem.addNewAggr("Reception", theEventName);
					TraceabilityHelper.addTraceabilityDependenciesTo( theReception, tracedToReqts );					
					
				    if (checkAddSend.isSelected()) {
				    	
				    	// add value argument before cloning the event to create the test-bench send
				    	if (checkAddValueAttribute.isSelected()){
				    		theEvent.addArgument( "value" );
				    	}
				    	
				    	String theSendEventName = "send_" + theEvent.getName().replaceFirst("req","");
				    	
				    	Logger.writeLine("Send event option was enabled, create event called " + theSendEventName);

				    	IRPModelElement theTestbenchReception = 
				    			createTestBenchSendFor( theEvent, (IRPActor) theActor, theSendEventName );
				    	
				    	theTestbenchReception.highLightElement();
				    } else {
				    	
				    	Logger.writeLine("Send event option was not enabled, so skipping this");
				    	theReception.highLightElement();
				    }
				    
					if (checkAddValueAttribute.isSelected()){
						
						Logger.writeLine("Continuous signal option chose, so creating a corresponding attribute");
												
						String proposedAttributeName = GeneralHelpers.toMethodName("is " + theSourceInfo);

						JPanel attributePanel = new JPanel();
						
						attributePanel.add(new JLabel("Attribute name: "));
						
						JTextField theAttrTextField = new JTextField(proposedAttributeName.length());
						theAttrTextField.setText(proposedAttributeName);
						
						attributePanel.add( theAttrTextField );				
					    
						int addAttributeChoice = JOptionPane.showConfirmDialog(
								null, attributePanel, "Do you want to add an Attribute for the value argument?", JOptionPane.YES_NO_OPTION);
						
						if( addAttributeChoice==JOptionPane.OK_OPTION ){
							String attributeName = theAttrTextField.getText(); 
							
							if (!attributeName.isEmpty()){
								IRPAttribute theAttribute = theLogicalSystem.addAttribute( attributeName );
								TraceabilityHelper.addTraceabilityDependenciesTo( theAttribute, tracedToReqts );
								theAttribute.highLightElement();
								
								IRPOperation theCheckOp = addCheckOperationFor(theAttribute);		
								TraceabilityHelper.addTraceabilityDependenciesTo( theCheckOp, tracedToReqts);	
								theCheckOp.highLightElement();
								
								addAnAttributeToMonitoringStateWith(theAttribute, theEventName, theLogicalSystem );
							}					
						}
					}					
				} else {
					Logger.writeLine("No text was entered");
				}
			}
		}
		
		return theEvent;
	}

	private static IRPGraphElement findGraphEl(IRPClassifier theClassifier, String withTheName) {
		
		IRPGraphElement theFoundGraphEl = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStatechartDiagram> theStatechartDiagrams = 
				theClassifier.getStatechart().getNestedElementsByMetaClass("StatechartDiagram", 1).toList();
		
		for (IRPStatechartDiagram theStatechartDiagram : theStatechartDiagrams) {
			
			Logger.writeLine(theStatechartDiagram, "was found owned by " + Logger.elementInfo(theClassifier));
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = theStatechartDiagram.getGraphicalElements().toList();
			
			for (IRPGraphElement theGraphEl : theGraphEls) {
				
				IRPModelElement theEl = theGraphEl.getModelObject();
				
				if (theEl != null){
					Logger.writeLine("Found " + theEl.getMetaClass() + " called " + theEl.getName());
					
					if (theEl.getName().equals(withTheName)){
						
						Logger.writeLine("Success, found GraphEl called " + withTheName + " in statechart for " + Logger.elementInfo(theClassifier));
						theFoundGraphEl = theGraphEl;
						break;
					}
				}
			}
		}
		
		return theFoundGraphEl;
	}
	
	private static IRPClassifier findTypeCalled(String theName){
	
		IRPClassifier theTypeFound = null;
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theTypes = 
				FunctionalAnalysisPlugin.getActiveProject().getNestedElementsByMetaClass("Type", 1).toList();
		
		for (IRPModelElement irpModelElement : theTypes) {			
			
			if (irpModelElement.getName().equals(theName) 
					&& irpModelElement instanceof IRPClassifier){
				theTypeFound = (IRPClassifier) irpModelElement;
				Logger.writeLine(irpModelElement, "was found in findTypeCalled");
				count++;
			}
		}
		
		if (theTypeFound==null){
			Logger.writeLine("Error in findTypeCalled, unable to find type called '" + theName + "'");
		}
		
		if (count>1){
			Logger.writeLine("Warning in findTypeCalled, unexpectedly " + count + " types called '" + theName + "' were found");
		}
		
		return theTypeFound;
	}
	
	private static IRPOperation addCheckOperationFor(IRPAttribute theAttribute){
		
		IRPOperation theOperation = null;
		
		IRPModelElement theOwner = theAttribute.getOwner();
		
		if (theOwner instanceof IRPClassifier){
			IRPClassifier theClassifier = (IRPClassifier)theOwner;
			String theAttributeName = theAttribute.getName();
			
			theOperation = theClassifier.addOperation("check" + GeneralHelpers.capitalize(theAttributeName));
			
			theOperation.setBody("OM_RETURN( " + theAttributeName + "==" + "1 );");
			
			IRPClassifier theType = findTypeCalled("RhpBoolean");
			
			if (theType!=null){
				theOperation.setReturns(theType);
			}
		} else {
			Logger.writeLine("Error in addCheckOperationFor, owner of " + Logger.elementInfo(theAttribute) + " is not a Classifier");
		}
		
		return theOperation;
	}
	
	private static void addAnAttributeToMonitoringStateWith(
			IRPAttribute theAttribute, String triggeredByTheEventName, IRPClassifier theOwnerOfStatechart) {

		IRPStatechart theStatechart = theOwnerOfStatechart.getStatechart();

		IRPState theMonitoringState = 
				getStateCalled("MonitoringConditions", theStatechart, theOwnerOfStatechart);

		if (theMonitoringState != null){
			Logger.writeLine(theMonitoringState, "found");

			IRPTransition theTransition = theMonitoringState.addTransition(theMonitoringState);

			theTransition.setItsTrigger(triggeredByTheEventName);
			theTransition.setItsAction("set" + GeneralHelpers.capitalize(theAttribute.getName()) + "(params->value);");

			Logger.writeLine(theTransition, "was added");	

			GR_Node stNode = null;

			IRPGraphElement theGraphEl = findGraphEl(theOwnerOfStatechart, "MonitoringConditions");

			if (theGraphEl != null){
				IRPDiagram theGraphElDiagram = theGraphEl.getDiagram();
				Logger.writeLine(theGraphElDiagram, "related to " 
						+ Logger.elementInfo(theGraphEl.getModelObject()) 
						+ " is the diagram for the GraphEl");

				stNode = new GR_Node((IRPGraphNode) theGraphEl);

				IRPGraphEdge theEdge = theGraphElDiagram.addNewEdgeForElement(theTransition, stNode.node, stNode.midRightX, stNode.midRightY, stNode.node, stNode.midBotX, stNode.midBotY);
				Logger.writeLine("Added edge to " + theEdge.getModelObject().getFullPathName());
			} else {
				Logger.writeLine("Error in addAnAttributeToMonitoringStateWith, unable to find the MonitoringConditions state");
			}

		} else {
			Logger.writeLine("Error did not find MonitoringConditions state");
		}

	}
	
	private static List<IRPModelElement> getActorsRelatedTo(IRPInstance theLogicalSystemPart){
		
		List<IRPModelElement> theActors = new ArrayList<IRPModelElement>();
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = 
				theLogicalSystemPart.getOwner().getNestedElementsByMetaClass("Part", 0).toList();
		
		for (IRPInstance thePart : theParts) {
			
			IRPClassifier theOtherClass = thePart.getOtherClass();
			
			if (theOtherClass instanceof IRPActor){
				theActors.add((IRPActor) theOtherClass);
			}
		}
		
		return theActors;
	}
	

	public static IRPOperation createTestCaseFor( IRPClass theTestDriver ){
		
		IRPOperation theOp = null;
		
		if (GeneralHelpers.hasStereotypeCalled("TestDriver", theTestDriver)){
			
			Logger.writeLine("createTestCaseFor was invoked for " + Logger.elementInfo(theTestDriver));
			
			String[] theSplitName = theTestDriver.getName().split("_");
			
			String thePrefix = theSplitName[0] + "_Test_";
			
			Logger.writeLine("The prefix for TestCase was calculated as '" + thePrefix + "'");
			
			int count = 0;
			boolean isUniqueNumber = false;
			String nameToTry = null;
			
			while (isUniqueNumber==false){
				count++;
				nameToTry = thePrefix + String.format("%03d", count);
				
				if (theTestDriver.findNestedElement(nameToTry, "Operation") == null){
					isUniqueNumber = true;
				}
			}
			
			if (isUniqueNumber){
				theOp = theTestDriver.addOperation(nameToTry);
				theOp.highLightElement();
				theOp.changeTo("Test Case");
				
				IRPState theState = OperationCreator.getStateCalled("Ready", theTestDriver.getStatechart(), theTestDriver);
				
				String theEventName = "ev" + nameToTry;
						
				IRPEventReception theEventReception = theTestDriver.addReception( theEventName );
				
				if (theEventReception != null){
					IRPEvent theEvent = theEventReception.getEvent();
					
					Logger.writeLine("The state called " + theState.getFullPathName() + " is owned by " + theState.getOwner().getFullPathName());
					IRPTransition theTransition = theState.addInternalTransition( theEvent );
					theTransition.setItsAction( theOp.getName() + "();");
				}
			}		
			
		} else {
			Logger.writeLine("Warning: This operation only works if you right-click a «TestDriver» block");
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    JOptionPane.showMessageDialog(
		    		null,  
		    		"This operation only works if you right-click a «TestDriver» block",
		    		"Warning",
		    		JOptionPane.WARNING_MESSAGE);		    
		}
		
		return theOp;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #007 05-MAY-2016: Move FileHelper into generalhelpers and remove duplicate class (F.J.Chadburn)
    #010 08-MAY-2016: Remove white-space from actor names (F.J.Chadburn)
    #012 08-MAY-2016: Fix Send event without value plus re-word check box titles (F.J.Chadburn)
    
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
