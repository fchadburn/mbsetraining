package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.telelogic.rhapsody.core.*;

public abstract class CreateTracedElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RequirementSelectionPanel m_RequirementsPanel = null;
	protected IRPModelElement m_TargetOwningElement = null;
	protected JTextField m_ChosenNameTextField = null;
	protected IRPGraphElement m_SourceGraphElement = null;
	protected IRPModelElement m_SourceModelElement = null;
	
	public CreateTracedElementPanel(
			IRPGraphElement forSourceGraphElement, 
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetClassifier) {
		
		super();

		m_TargetOwningElement = onTargetClassifier;		
		m_SourceGraphElement = forSourceGraphElement;
		m_SourceModelElement = m_SourceGraphElement.getModelObject();
			
		setupRequirementsPanel( withReqtsAlsoAdded );
	}

	public CreateTracedElementPanel(
			IRPModelElement forSourceModelElement, 
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetClassifier) {
		
		super();

		m_TargetOwningElement = onTargetClassifier;		
		m_SourceGraphElement = null;
		m_SourceModelElement = forSourceModelElement;
		
		setupRequirementsPanel( withReqtsAlsoAdded );
	}
	
	
	private void setupRequirementsPanel(
			Set<IRPRequirement> withReqtsAlsoAdded ){
		
		Set<IRPRequirement> tracedToReqts = TraceabilityHelper.getRequirementsThatTraceFrom( m_SourceModelElement, true );
		
		tracedToReqts.addAll( withReqtsAlsoAdded );
		
		if (tracedToReqts.isEmpty()){	
			m_RequirementsPanel = new RequirementSelectionPanel( 
					tracedToReqts, "There are no requirements to establish «satisfy» dependencies to" );
		} else {
			m_RequirementsPanel = new RequirementSelectionPanel( 
					tracedToReqts, "With «satisfy» dependencies to:" );
		}
		
	}
	
	public JPanel createChosenNamePanelWith(
			String theLabelText,
			String andInitialChosenName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		JLabel theLabel =  new JLabel( theLabelText );
		thePanel.add( theLabel );
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setText( andInitialChosenName );
		m_ChosenNameTextField.setMinimumSize( new Dimension( 350,20 ) );
		m_ChosenNameTextField.setPreferredSize( new Dimension( 350,20 ) );
		m_ChosenNameTextField.setMaximumSize( new Dimension( 350,20 ) );
		
		thePanel.add( m_ChosenNameTextField );
		
		return thePanel;
	}
	
	protected IRPOperation addCheckOperationFor(
			IRPAttribute theAttribute,
			String withTheName ){
		
		IRPOperation theOperation = null;
		
		IRPModelElement theOwner = theAttribute.getOwner();
		
		if( theOwner instanceof IRPClassifier ){
			IRPClassifier theClassifier = (IRPClassifier)theOwner;
			String theAttributeName = theAttribute.getName();
			
			theOperation = theClassifier.addOperation( withTheName );
			
			theOperation.setBody("OM_RETURN( " + theAttributeName + " );");
			
			IRPDependency theAutoRippleDependency = theAttribute.addDependencyTo(theOperation);
			theAutoRippleDependency.addStereotype("AutoRipple", "Dependency");
			IRPClassifier theType = findTypeCalled("int");
			
			if (theType!=null){
				theOperation.setReturns(theType);
			}
		} else {
			Logger.writeLine("Error in addCheckOperationFor, owner of " + Logger.elementInfo(theAttribute) + " is not a Classifier");
		}
		
		return theOperation;
	}
	
	private IRPClassifier findTypeCalled(String theName){
		
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

	
	// implementation specific provided by parent
	protected abstract boolean checkValidity(boolean isMessageEnabled);
	
	// implementation specific provided by parent
	protected abstract void performAction();
		
	public JPanel createOKCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton("OK");
		theOKButton.setPreferredSize(new Dimension(75,25));

		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if (isValid){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}		
												
				} catch (Exception e2) {
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on OK button action listener");
				}
			}
		});
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize(new Dimension(75,25));
		
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on Cancel button action listener");
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	protected static void bleedColorToElementsRelatedTo( 
			IRPGraphElement theGraphEl ){
		
		// only bleed on activity diagrams		
		if (theGraphEl.getDiagram() instanceof IRPActivityDiagram){
			
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
	}

	private static void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, 
			String theColorSetting, 
			IRPDiagram onDiagram){

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
	
	protected static void addTraceabilityDependenciesTo(
			IRPModelElement theElement, 
			List<IRPRequirement> theReqtsToAdd){
	
		IRPStereotype theDependencyStereotype = 
				FunctionalAnalysisSettings.getStereotypeForFunctionTracing(theElement.getProject());
		
		if (theDependencyStereotype != null){
			for (IRPRequirement theReqt : theReqtsToAdd) {
				
				IRPDependency theDep = theElement.addDependencyTo(theReqt);
				theDep.setStereotype(theDependencyStereotype);		
				Logger.writeLine("Added a " + theDependencyStereotype.getName() + " dependency to " + Logger.elementInfo( theElement ));
			}
		} else {
			Logger.writeLine("Error in addTraceabilityDependenciesTo, unable to find stereotype to apply to dependencies");
		}
	}
	
	protected static List<IRPModelElement> getActorsRelatedTo(
			IRPInstance theLogicalSystemPart){
		
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
	
	protected static List<IRPModelElement> getActorsRelatedTo(
			 IRPClassifier theBuildingBlock ){
		
		List<IRPModelElement> theActors = new ArrayList<IRPModelElement>();
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = 
 			theBuildingBlock.getNestedElementsByMetaClass("Part", 0).toList();
		
		for (IRPInstance thePart : theParts) {
			
			IRPClassifier theOtherClass = thePart.getOtherClass();
			
			if (theOtherClass instanceof IRPActor){
				theActors.add((IRPActor) theOtherClass);
			}
		}
		
		return theActors;
	}
	
	protected int getSourceElementX(){
		
		int x = 10;
		
		if( m_SourceGraphElement != null ){

			if (m_SourceGraphElement instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement );
				
				x = theNodeInfo.getTopLeftX() + 20;
				
			} else if (m_SourceGraphElement instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) m_SourceGraphElement );
				
				x = theNodeInfo.getMidX();
			}
		}
		
		return x;
	}
	
	protected int getSourceElementY(){
		
		int y = 10;
		
		if( m_SourceGraphElement != null ){

			if (m_SourceGraphElement instanceof IRPGraphNode){
				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement );
				
				y = theNodeInfo.getTopLeftY() + 20;
				
			} else if (m_SourceGraphElement instanceof IRPGraphEdge){
				GraphEdgeInfo theNodeInfo = new GraphEdgeInfo( (IRPGraphEdge) m_SourceGraphElement );
				
				y = theNodeInfo.getMidY();
			}
		}
		
		return y;
	}

	protected void populateCallOperationActionOnDiagram(
			IRPOperation theOperation) {

		try {
			IRPApplication theRhpApp = FunctionalAnalysisPlugin.getRhapsodyApp();

			IRPDiagram theDiagram = m_SourceGraphElement.getDiagram();

			if (theDiagram instanceof IRPActivityDiagram){

				IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;

				IRPFlowchart theFlowchart = theAD.getFlowchart();

				if( m_SourceGraphElement.getModelObject() instanceof IRPCallOperation ){

					IRPCallOperation theCallOp = (IRPCallOperation) m_SourceGraphElement.getModelObject();
					theCallOp.setOperation(theOperation);

				} else {
					IRPCallOperation theCallOp = 
							(IRPCallOperation) theFlowchart.addNewAggr(
									"CallOperation", theOperation.getName() );

					theCallOp.setOperation(theOperation);

					theFlowchart.addNewNodeForElement(
							theCallOp, getSourceElementX(), getSourceElementY(), 300, 40 );

					theRhpApp.highLightElement( theCallOp );
				}

			} else if (theDiagram instanceof IRPObjectModelDiagram){				

				IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theDiagram;

				IRPGraphNode theEventNode = theOMD.addNewNodeForElement( 
						theOperation, getSourceElementX() + 50, getSourceElementY() + 50, 300, 40 );	

				IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
				theGraphElsToDraw.addGraphicalItem( m_SourceGraphElement );
				theGraphElsToDraw.addGraphicalItem( theEventNode );

				theOMD.completeRelations( theGraphElsToDraw, 1 );

				theRhpApp.highLightElement( theOperation );

			} else {
				Logger.writeLine("Error in populateCallOperationActionOnDiagram, expected an IRPActivityDiagram");
			}

		} catch (Exception e) {
			Logger.writeLine("Error in populateCallOperationActionOnDiagram, unhandled exception was detected");
		}
	}
	
	public static String determineBestCheckOperationNameFor(
			IRPClassifier onTargetBlock,
			String theAttributeName){
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( "check" + GeneralHelpers.capitalize( theAttributeName ) ), 
				"Attribute", 
				onTargetBlock );
		
		return theProposedName;
	}
	
	protected static IRPClass selectBlockBasedOn(
			IRPActor theActor,
			IRPClass inTheBuildingBlock,
			String withMsg,
			boolean withSelection ){
	
		IRPClass theBlock = null;

		List<IRPModelElement> theCandidates = 
				GeneralHelpers.getNonActorOrTestingClassifiersConnectedTo( 
						(IRPActor)theActor, inTheBuildingBlock );

		if( theCandidates.isEmpty() ){

			Logger.writeLine("Error in launchDialogsToSelectBlockBasedOn, no parts typed by Blocks were found underneath " + 
					Logger.elementInfo( inTheBuildingBlock ) );

		} else if ( theCandidates.size() == 1 ){

			theBlock = (IRPClass) theCandidates.get( 0 );

		} else { // theCandidates.size() > 1

			if( withSelection ){

				IRPModelElement theUserSelectedEl = GeneralHelpers.launchDialogToSelectElement(
						theCandidates, withMsg, true );

				if( theUserSelectedEl != null && theUserSelectedEl instanceof IRPClass ){

					theBlock = (IRPClass)theUserSelectedEl;

				} else {

					Logger.writeLine("Error in launchDialogsToSelectBlockBasedOn, no user selection");
				}

			} else {

				for( IRPModelElement theCandidate : theCandidates ) {

					if( theCandidate instanceof IRPClass && 
							GeneralHelpers.hasStereotypeCalled( "LogicalSystem", theCandidate )){
						theBlock = (IRPClass) theCandidate;
					}
				}
			}
		}

		return theBlock;
	}

}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #040 17-JUN-2016: Extend populate event/ops to work on OMD, i.e., REQ diagrams (F.J.Chadburn)
    #041 29-JUN-2016: Derive downstream requirement menu added for reqts on diagrams (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #058 13-JUL-2016: Dropping CallOp on diagram now gives option to create Op on block (F.J.Chadburn)
    #069 20-JUL-2016: Fix population of events/ops on diagram when creating from a transition (F.J.Chadburn)
    #082 09-AUG-2016: Add a check operation check box added to the create attribute dialog (F.J.Chadburn)
    #083 09-AUG-2016: Add an Update attribute menu option and panel with add check operation option (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #090 15-AUG-2016: Fix check operation name issue introduced in fixes #083 and #084 (F.J.Chadburn)

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
