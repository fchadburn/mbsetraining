package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateOutgoingEventPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JCheckBox m_ActionOnDiagramIsNeededCheckBox;
	private IRPActor m_DestinationActor;
	private IRPPackage m_PackageForEvent;
	private JCheckBox m_SendOperationIsNeededCheckBox;
	private JCheckBox m_ActiveAgumentNeededCheckBox;

	public static void createOutgoingEventsFor(
			IRPProject theActiveProject,
			List<IRPGraphElement> theSelectedGraphEls) {
		
		Set<IRPModelElement> theMatchingEls = 
				GeneralHelpers.findModelElementsIn( theSelectedGraphEls, "Requirement" );
		
		// cast to IRPRequirement
		@SuppressWarnings("unchecked")
		Set<IRPRequirement> theSelectedReqts = (Set<IRPRequirement>)(Set<?>) theMatchingEls;
		
		if (GeneralHelpers.doUnderlyingModelElementsIn( theSelectedGraphEls, "Requirement" )){
			
			// only requirements are selected hence assume only a single operation is needed
			launchThePanel(	theSelectedGraphEls.get(0), theSelectedReqts, theActiveProject );
		} else {
			
			// launch a dialog for each selected element that is not a requirement
			for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				if (theModelObject != null && !(theModelObject instanceof IRPRequirement)){
					
					// only launch a dialog for non requirement elements
					launchThePanel(	theGraphEl, theSelectedReqts, theActiveProject );
				}		
			}
		}
	}

	public CreateOutgoingEventPanel(
			IRPGraphElement forSourceGraphElement,
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPActor toDestinationActor,
			IRPPackage thePackageForEvent ) {
		
		super( forSourceGraphElement, withReqtsAlsoAdded, onTargetBlock );
		
		m_DestinationActor = toDestinationActor;
		m_PackageForEvent = thePackageForEvent;
		
		String theSourceText = GeneralHelpers.getActionTextFrom( forSourceGraphElement.getModelObject() );		
		
		Logger.writeLine("CreateOutgoingEventPanel constructor called with text '" + theSourceText + "'");
		
		String[] splitActorName = m_DestinationActor.getName().split("_");
		String theActorName = splitActorName[0];
		String theSourceMinusActor = theSourceText.replaceFirst( "^" + theActorName, "" );
		
		Logger.writeLine("The source minus actor is '" + theSourceMinusActor + "'");	
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( "reqInform" + theActorName + theSourceMinusActor ), 
				"Event", 
				onTargetBlock.getProject() );	
		
		m_SendOperationIsNeededCheckBox = new JCheckBox();
		
		m_SendOperationIsNeededCheckBox.setSelected(true);

		m_SendOperationIsNeededCheckBox.addActionListener( new ActionListener() {
			
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  
			        AbstractButton theSourceButton = (AbstractButton) actionEvent.getSource();
			        
			        boolean isSelected = theSourceButton.getModel().isSelected();
			        
			        // populate on diagram will use the send operation hence 
			        // disable if user de-selects the send operation option
			        if (!isSelected){
			        	m_ActionOnDiagramIsNeededCheckBox.setSelected( false );
				        m_ActionOnDiagramIsNeededCheckBox.setEnabled( false );
			        } else {
			        	m_ActionOnDiagramIsNeededCheckBox.setEnabled( true );
			        }
			      }} );
		
		m_ActiveAgumentNeededCheckBox = new JCheckBox(
				"Add an 'active' argument to the event (e.g. for on/off conditions)");
		
		m_ActiveAgumentNeededCheckBox.setSelected(false);
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		m_ActionOnDiagramIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		m_ActionOnDiagramIsNeededCheckBox.setSelected(false);
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( m_ActionOnDiagramIsNeededCheckBox );
							
		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
	
		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );
		theCenterPanel.add( m_RequirementsPanel );
		theCenterPanel.add( m_SendOperationIsNeededCheckBox );
		theCenterPanel.add( m_ActiveAgumentNeededCheckBox );
		
		m_ChosenNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateNames();
					}	
				});
		
		updateNames();
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private static void launchThePanel(
			final IRPGraphElement theSourceGraphElement, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			final IRPProject inProject){
		
		final IRPInstance partUnderDev = 
				FunctionalAnalysisSettings.getPartUnderDev( inProject );
		
		final IRPPackage thePackageForEvent = 
				FunctionalAnalysisSettings.getPkgThatOwnsEventsAndInterfaces( inProject );
		
		final IRPModelElement theActor = 
				GeneralHelpers.launchDialogToSelectElement(
						getActorsRelatedTo( partUnderDev ), "Select Actor to send Event to", true);
		
		if (theActor != null && theActor instanceof IRPActor){

			final IRPClassifier theLogicalSystem = partUnderDev.getOtherClass();
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );
					JFrame frame = new JFrame("Create an outgoing event to " + Logger.elementInfo( theActor ) );
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateOutgoingEventPanel thePanel = new CreateOutgoingEventPanel(
							theSourceGraphElement, 
							theLogicalSystem, 
							withReqtsAlsoAdded,
							(IRPActor)theActor, 
							thePackageForEvent );

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		} else {
			Logger.writeLine("No actor was selected");
		}
	}
	
	private IRPPort getPortForDestinationActor(){
		
		IRPPort thePort = null;
		
		IRPModelElement theContextEl = 
				FunctionalAnalysisSettings.getPartUnderDev(
						m_PackageForEvent.getProject() ).getOwner();
		
		if (theContextEl instanceof IRPClassifier){
			IRPClassifier theContextBlock = (IRPClassifier) theContextEl;
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theLinks = 
				theContextBlock.getNestedElementsByMetaClass("Link", 1).toList();
			
			for (IRPModelElement irpModelElement : theLinks) {
				
				if (irpModelElement instanceof IRPLink){
					
					IRPLink theLink = (IRPLink) irpModelElement;
					IRPPort toPort = theLink.getToPort();
					
					IRPInstance theFromObject = (IRPInstance) theLink.getFromElement();
					
					IRPModelElement theToType = theFromObject.getOtherClass();
					System.out.print( "theToType=" + Logger.elementInfo( theToType ) + "\n");
					
					if (theToType.equals( m_DestinationActor )){
						thePort = toPort;
						Logger.writeLine("Port to generate event on is " + Logger.elementInfo( toPort ));
					}
				}
			}
		}
	
		return thePort;
	}
	
	private void updateNames(){
		m_SendOperationIsNeededCheckBox.setText(
				"Add an '" + determineBestInformNameFor(
						(IRPClassifier)m_TargetOwningElement, m_ChosenNameTextField.getText() ) 
				+ "' operation that sends the event");
	}

	private static String determineBestInformNameFor(
			IRPClassifier onTargetBlock,
			String theEventName){
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( GeneralHelpers.decapitalize( theEventName.replace("req", "") ) ), 
				"Operation", 
				onTargetBlock );
		
		return theProposedName;
	}

	private void populateSendActionOnDiagram(
			IRPEvent theEvent) {
		
		IRPApplication theRhpApp = FunctionalAnalysisPlugin.getRhapsodyApp();
		
		if (m_SourceGraphElement instanceof IRPGraphNode){
			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement );
			
			int x = theNodeInfo.getTopLeftX() + 20;
			int y = theNodeInfo.getTopLeftY() + 20;
			
			IRPDiagram theDiagram = m_SourceGraphElement.getDiagram();
							
			if (theDiagram instanceof IRPActivityDiagram){
				
				IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;
				
				IRPFlowchart theFlowchart = theAD.getFlowchart();
				
				IRPState theState = 
						(IRPState) theFlowchart.addNewAggr(
								"State", theEvent.getName() );
				
				theState.setStateType("EventState");
			
				if( theState != null ){
					
					IRPSendAction theSendAction = theState.getSendAction();
					theSendAction.setEvent(theEvent);
				}
		
				theFlowchart.addNewNodeForElement( theState, x, y, 300, 40 );
				
				theRhpApp.highLightElement( theState );
			
			} else if (theDiagram instanceof IRPObjectModelDiagram){				
				
				IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theDiagram;
				
				IRPGraphNode theEventNode = theOMD.addNewNodeForElement(theEvent, x + 50, y + 50, 300, 40);	
				
				IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
				theGraphElsToDraw.addGraphicalItem( m_SourceGraphElement );
				theGraphElsToDraw.addGraphicalItem( theEventNode );
				
				theOMD.completeRelations( theGraphElsToDraw, 1 );
				
				theRhpApp.highLightElement( theEvent );
			
			} else {
				Logger.writeLine("Error in CreateOperationPanel.performAction, expected an IRPActivityDiagram");
			}
		}
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		String errorMessage = null;
		boolean isValid = true;
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		boolean isLegalName = GeneralHelpers.isLegalName( theChosenName );
		
		if (!isLegalName){
			
			errorMessage += theChosenName + " is not legal as an identifier representing an executable event\n";				
			isValid = false;
			
		} else if (!GeneralHelpers.isElementNameUnique(
				theChosenName, 
				"Event", 
				m_PackageForEvent.getProject(), 
				1)){

			errorMessage = "Unable to proceed as the event name '" + theChosenName + "' is not unique";
			isValid = false;
		}		

		if (m_SendOperationIsNeededCheckBox.isSelected()){
			
			String theProposedName = determineBestInformNameFor(
					(IRPClassifier)m_TargetOwningElement, 
					theChosenName );

			if (!GeneralHelpers.isElementNameUnique(
					theProposedName, 
					"Operation", 
					m_TargetOwningElement, 
					0)){

				if (errorMessage != null){
					errorMessage += "\nand the operation name  '" + theProposedName + "' is not unique";
				} else {
					errorMessage = "Unable to proceed as the operation name '" + theProposedName + "' is not unique";
				}
				isValid = false;
			}
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelpers.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}
	
	@Override
	protected void performAction() {
		
		// do silent check first
		if (checkValidity( false )){
			
			String theEventName = m_ChosenNameTextField.getText(); 
			
			if (!theEventName.isEmpty()){
				
				IRPEvent theEvent = m_PackageForEvent.addEvent(theEventName);
				
				List<IRPRequirement> selectedReqtsList = m_RequirementsPanel.getSelectedRequirementsList();
				
				addTraceabilityDependenciesTo( theEvent, selectedReqtsList );
				
				if (m_ActiveAgumentNeededCheckBox.isSelected()){
					theEvent.addArgument("active");
				}
				
				theEvent.highLightElement();
				
				IRPModelElement theReception = m_DestinationActor.addNewAggr("Reception", theEventName);
				addTraceabilityDependenciesTo( theReception, selectedReqtsList );
				
				theReception.highLightElement();
				
				if (m_SendOperationIsNeededCheckBox.isSelected()){
					
					IRPPort thePort = getPortForDestinationActor();
					
					Logger.writeLine("Adding an inform Operation");		

					IRPOperation informOp =
							((IRPClassifier)m_TargetOwningElement).addOperation(
									determineBestInformNameFor(
											(IRPClassifier)m_TargetOwningElement, theEventName ) );
					
					informOp.highLightElement();
					
					addTraceabilityDependenciesTo( informOp, selectedReqtsList );
					
					if (m_ActiveAgumentNeededCheckBox.isSelected()){
						
						informOp.addArgument("active");
						informOp.setBody("OPORT(" + thePort.getName()+")->GEN(" + theEventName + "( active ) );");
					} else {
						informOp.setBody("OPORT(" + thePort.getName()+")->GEN(" + theEventName + ");");
					}			
					
					if( m_ActionOnDiagramIsNeededCheckBox.isSelected() ){
						populateSendActionOnDiagram( theEvent );
					}
				}	
				
				bleedColorToElementsRelatedTo( m_SourceGraphElement );
			}
			
		} else {
			Logger.writeLine("Error in CreateOutgoingEventPanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #038 17-JUN-2016: Populate diagram now populates a SendAction in case of send events (F.J.Chadburn)
    #040 17-JUN-2016: Extend populate event/ops to work on OMD, i.e., REQ diagrams (F.J.Chadburn)
    #042 29-JUN-2016: launchThePanel renaming to improve Panel class design consistency (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #054 13-JUL-2016: Create a nested BlockPkg package to contain the Block and events (F.J.Chadburn)
    #062 17-JUL-2016: Create InterfacesPkg and correct build issues by adding a Usage dependency (F.J.Chadburn)

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