package functionalanalysisplugin;

import executablembse.PortBasedConnector;
import generalhelpers.ConfigurationSettings;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateOutgoingEventPanel extends CopyOfCreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox m_ActionOnDiagramIsNeededCheckBox;
	private IRPActor m_DestinationActor;
	private IRPPort m_DestinationActorPort;
	private IRPPackage m_PackageForEvent;
	private JCheckBox m_SendOperationIsNeededCheckBox;
	private JCheckBox m_ActiveAgumentNeededCheckBox;

	private ConfigurationSettings m_ConfigSettings;
	
	public static void main(String[] args) {

		ConfigurationSettings configSettings = new ConfigurationSettings(
				"SysMLHelper.properties", 
				"SysMLHelper_MessagesBundle" );
		
		launchThePanel( configSettings );
	}

	public static void launchThePanel(
			final ConfigurationSettings theConfigSettings ){

		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		Logger.writeLine("launchThePanel was invoked");

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create an outgoing event" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateOutgoingEventPanel thePanel = 
						new CreateOutgoingEventPanel(
								theAppID,
								theConfigSettings );

				frame.setContentPane( thePanel );

				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateOutgoingEventPanel(
			String theAppID,
			ConfigurationSettings theConfigSettings ){

		super( theAppID );
		
		m_ConfigSettings = theConfigSettings;

		IRPClass theBuildingBlock = 
				m_ElementContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null

			m_PackageForEvent = m_ElementContext.getPkgThatOwnsEventsAndInterfaces();

			List<IRPModelElement> theCandidateActors = 
					getNonElapsedTimeActorsRelatedTo( theBuildingBlock );

			if( theCandidateActors.isEmpty() ){

				UserInterfaceHelpers.showWarningDialog("There are no actor parts to send events to in the " + 
						Logger.elementInfo(theBuildingBlock) + ". \n\n" +
						"If you want to send an event then add an actor to the BDD and connect its part using ports. These steps are automated \n" +
						"by the '" + m_ConfigSettings.getString( "functionalanalysisplugin.AddNewActorToPackageMenu" ) + "' command. ");

			} else {

				final IRPModelElement theActorEl = 
						GeneralHelpers.launchDialogToSelectElement(
								theCandidateActors, 
								"Select Actor to send Event to", 
								true);

				if( theActorEl != null && 
						theActorEl instanceof IRPActor ){

					IRPActor theActor = (IRPActor)theActorEl;

					List<IRPModelElement> theCandidates = 
							GeneralHelpers.getNonActorOrTestingClassifiersConnectedTo( 
									theActor, 
									theBuildingBlock );

					if( theCandidates.isEmpty() ){

						UserInterfaceHelpers.showWarningDialog(
								"The " + Logger.elementInfo( theBuildingBlock ) + 
								" does not have any connectors that connect the " + Logger.elementInfo(theActor) + " with Blocks.\n\n" +
								"Fix this and then try again");

					} else {

						IRPClass theChosenBlock = 
								m_ElementContext.getBlockUnderDev( 
										"Which Block is sending the event?" );

						if( theChosenBlock != null ){

							final IRPPort thePort = GeneralHelpers.getPortThatConnects(
									(IRPClassifier)theChosenBlock,
									theActor, 
									theBuildingBlock );

							if( thePort == null ){

								UserInterfaceHelpers.showWarningDialog(
										"Unable to find a port that connects " + Logger.elementInfo( theActor ) + " to the " + 
												Logger.elementInfo( theChosenBlock ) + ". \n" +
												"You may want to add the necessary ports and connector to the IBD under " + 
												Logger.elementInfo( theBuildingBlock ) + " \nbefore trying this." );

								theBuildingBlock.highLightElement();

							} else { // thePort != null

								String theSourceText = GeneralHelpers.getActionTextFrom( m_ElementContext.getSelectedEl() );		

								if( theSourceText.isEmpty() ){
									theSourceText = m_Tbd;
								}

								createCommonContent(
										theSourceText, 
										m_ElementContext.getChosenBlock(), 
										m_ElementContext.getSelectedReqts(), 
										theActor, 
										thePort,
										m_PackageForEvent );
							}
						}
					}
				}
			}
		}

		Logger.writeLine( "CreateOutgoingEventPanel constructor called for " + 
				Logger.elementInfo( m_ElementContext.getSelectedEl() ) );

	}

	//	public CreateOutgoingEventPanel(
	//			IRPModelElement forSourceModelElement,
	//			IRPClassifier onTargetBlock,
	//			Set<IRPRequirement> withReqtsAlsoAdded,
	//			IRPActor toDestinationActor,
	//			IRPPort toDestinationActorPort,
	//			IRPPackage thePackageForEvent ) {
	//		
	//		super( forSourceModelElement, withReqtsAlsoAdded, onTargetBlock, onTargetBlock.getProject() );
	//		
	//		String theSourceText = m_Tbd;		
	//		
	//		Logger.writeLine("CreateOutgoingEventPanel constructor called for " + Logger.elementInfo( forSourceModelElement ));
	//		
	//		createCommonContent(
	//				theSourceText, 
	//				onTargetBlock, 
	//				withReqtsAlsoAdded, 
	//				toDestinationActor, 
	//				toDestinationActorPort,
	//				thePackageForEvent );
	//	}

	private void createCommonContent(
			String theSourceText,
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPActor toDestinationActor,
			IRPPort toDestinationActorPort,
			IRPPackage thePackageForEvent ) {	

		m_DestinationActor = toDestinationActor;
		m_DestinationActorPort = toDestinationActorPort;
		m_PackageForEvent = thePackageForEvent;

		String[] splitActorName = m_DestinationActor.getName().split("_");
		String theActorName = splitActorName[0];
		String theSourceMinusActor = theSourceText.replaceFirst( "^" + theActorName, "" );

		Logger.writeLine("The source minus actor is '" + theSourceMinusActor + "'");	

		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( "reqInform" + theActorName + theSourceMinusActor, 40 ), 
				"Event", 
				onTargetBlock.getProject() );	

		m_SendOperationIsNeededCheckBox = new JCheckBox();
		m_SendOperationIsNeededCheckBox.setSelected(true);

		m_ActiveAgumentNeededCheckBox = new JCheckBox(
				"Add an 'active' argument to the event (e.g. for on/off conditions)");

		m_ActiveAgumentNeededCheckBox.setSelected(false);

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		m_ActionOnDiagramIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		setupPopulateCheckbox( m_ActionOnDiagramIsNeededCheckBox );

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

	//	public static void launchThePanel(
	//			final IRPGraphElement theSourceGraphElement, 
	//			final IRPModelElement orTheModelElement,
	//			final Set<IRPRequirement> withReqtsAlsoAdded,
	//			final IRPProject inProject){
	//		
	//		IRPModelElement theContextEl = null;
	//		
	//		if( orTheModelElement != null ){
	//			theContextEl = orTheModelElement;
	//		} else if( theSourceGraphElement != null ){
	//			theContextEl = theSourceGraphElement.getModelObject();
	//		}
	//
	//			IRPClass theBuildingBlock = 
	//					FunctionalAnalysisSettings.getBuildingBlock( theContextEl );
	//			
	//			if( theBuildingBlock != null ){
	//				
	//				final IRPPackage thePackageForEvent = 
	//						FunctionalAnalysisSettings.getPkgThatOwnsEventsAndInterfaces( 
	//								theContextEl );
	//				
	//				List<IRPModelElement> theCandidateActors = 
	//						getNonElapsedTimeActorsRelatedTo( theBuildingBlock );
	//				
	//				if( theCandidateActors.isEmpty() ){
	//					
	//					ConfigurationSettings theConfigSettings = ConfigurationSettings.getInstance();
	//					
	//					UserInterfaceHelpers.showWarningDialog("There are no actor parts to send events to in the " + 
	//							Logger.elementInfo(theBuildingBlock) + ". \n\n" +
	//							"If you want to send an event then add an actor to the BDD and connect its part using ports. These steps are automated \n" +
	//							"by the '" + theConfigSettings.getString( "functionalanalysisplugin.AddNewActorToPackageMenu" ) + "' command. ");
	//				} else {
	//					
	//						}			
	//					}
	//				}
	//			//}
	//		}
	//	}

	private void updateNames(){
		m_SendOperationIsNeededCheckBox.setText(
				"Add an '" + determineBestInformNameFor(
						m_ElementContext.getChosenBlock(), 
						m_ChosenNameTextField.getText() ) 
						+ "' operation that sends the event");
	}

	private static String determineBestInformNameFor(
			IRPClassifier onTargetBlock,
			String theEventName){

		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( GeneralHelpers.decapitalize( theEventName.replace("req", "") ), 40 ), 
				"Operation", 
				onTargetBlock );

		return theProposedName;
	}

	private void populateSendActionOnDiagram(
			IRPEvent theEvent ){

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		IRPDiagram theSourceDiagram = m_ElementContext.getSourceDiagram();
		IRPGraphElement theGraphEl = m_ElementContext.getSelectedGraphEl();

		if( theSourceDiagram != null ){

			if( theSourceDiagram instanceof IRPActivityDiagram ){

				IRPActivityDiagram theAD = (IRPActivityDiagram)theSourceDiagram;

				IRPFlowchart theFlowchart = theAD.getFlowchart();

				IRPState theState = 
						(IRPState) theFlowchart.addNewAggr(
								"State", theEvent.getName() );

				theState.setStateType("EventState");

				if( theState != null ){

					IRPSendAction theSendAction = theState.getSendAction();
					theSendAction.setEvent(theEvent);
				}

				theFlowchart.addNewNodeForElement(
						theState, getSourceElementX(), getSourceElementY(), 300, 40 );

				theState.highLightElement();

			} else if( theSourceDiagram instanceof IRPObjectModelDiagram ){				

				IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theSourceDiagram;

				IRPGraphNode theEventNode = theOMD.addNewNodeForElement(
						theEvent, getSourceElementX() + 50, getSourceElementY() + 50, 300, 40 );	

				if( theGraphEl != null ){
					IRPCollection theGraphElsToDraw = theRhpApp.createNewCollection();
					theGraphElsToDraw.addGraphicalItem( theGraphEl );
					theGraphElsToDraw.addGraphicalItem( theEventNode );

					theOMD.completeRelations( theGraphElsToDraw, 1 );
				}

				theEvent.highLightElement();

			} else {
				Logger.writeLine( "Error in populateSendActionOnDiagram " + 
						Logger.elementInfo( theSourceDiagram ) + 
						" is not supported for populating on");
			}

		} else {	
			Logger.writeLine( "Error in populateSendActionOnDiagram, m_SourceGraphElement is null when value was expected" );
		}
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){

		String errorMessage = null;
		boolean isValid = true;

		String theChosenName = m_ChosenNameTextField.getText();
		IRPClass theChosenBlock = m_ElementContext.getChosenBlock();

		boolean isLegalName = GeneralHelpers.isLegalName( 
				theChosenName, 
				theChosenBlock );

		if( !isLegalName ){

			errorMessage += theChosenName + " is not legal as an identifier representing an executable event\n";				
			isValid = false;

		} else if( !GeneralHelpers.isElementNameUnique(
				theChosenName, 
				"Event", 
				m_PackageForEvent.getProject(), 
				1 ) ){

			errorMessage = "Unable to proceed as the event name '" + theChosenName + "' is not unique";
			isValid = false;
		}		

		if( m_SendOperationIsNeededCheckBox.isSelected() ){

			String theProposedName = determineBestInformNameFor(
					theChosenBlock, 
					theChosenName );

			if( !GeneralHelpers.isElementNameUnique(
					theProposedName, 
					"Operation", 
					theChosenBlock, 
					0 ) ){

				if( errorMessage != null ){
					errorMessage += "\nand the operation name  '" + theProposedName + "' is not unique";
				} else {
					errorMessage = "Unable to proceed as the operation name '" + theProposedName + "' is not unique";
				}
				isValid = false;
			}
		}

		if( isMessageEnabled && !isValid && errorMessage != null ){

			UserInterfaceHelpers.showWarningDialog( errorMessage );
		}

		return isValid;
	}

	@Override
	protected void performAction() {


		IRPClass theChosenBlock = m_ElementContext.getChosenBlock();

		String theEventName = m_ChosenNameTextField.getText(); 

		if( !theEventName.isEmpty() ){

			IRPEvent theEvent = m_PackageForEvent.addEvent( theEventName );

			List<IRPRequirement> selectedReqtsList = m_RequirementsPanel.getSelectedRequirementsList();

			addTraceabilityDependenciesTo( theEvent, selectedReqtsList );

			if( m_ActiveAgumentNeededCheckBox.isSelected() ){
				theEvent.addArgument( "active" );
			}

			theEvent.highLightElement();

			PortBasedConnector theExistingConnector = 
					new PortBasedConnector( 
							theChosenBlock, 
							m_DestinationActor );

			theExistingConnector.addEvent( theEvent );

			IRPModelElement theReception = m_DestinationActor.addNewAggr("Reception", theEventName);
			addTraceabilityDependenciesTo( theReception, selectedReqtsList );

			theReception.highLightElement();

			if (m_SendOperationIsNeededCheckBox.isSelected()){

				Logger.writeLine("Adding an inform Operation");		

				IRPOperation informOp =
						theChosenBlock.addOperation(
								determineBestInformNameFor(
										theChosenBlock, 
										theEventName ) );

				informOp.highLightElement();

				addTraceabilityDependenciesTo( informOp, selectedReqtsList );

				String thePortName = m_DestinationActorPort.getName();

				if (m_ActiveAgumentNeededCheckBox.isSelected()){

					informOp.addArgument("active");
					informOp.setBody("OPORT( " + thePortName + " )->GEN( " + theEventName + "( active ) );");
				} else {
					informOp.setBody("OPORT( " + thePortName + " )->GEN( " + theEventName + " );");
				}			
			}	

			if( m_ActionOnDiagramIsNeededCheckBox.isSelected() ){
				populateSendActionOnDiagram( theEvent );
			}

			m_ElementContext.bleedColorToElementsRelatedTo( selectedReqtsList );
		}
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

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
    #069 20-JUL-2016: Fix population of events/ops on diagram when creating from a transition (F.J.Chadburn)
    #078 28-JUL-2016: Added isPopulateWantedByDefault tag to FunctionalAnalysisPkg to give user option (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #093 23-AUG-2016: Added isPopulateOptionHidden tag to allow hiding of the populate check-box on dialogs (F.J.Chadburn)
    #099 14-SEP-2016: Allow event and operation creation from right-click on AD and RD diagram canvas (F.J.Chadburn)
    #117 13-NOV-2016: Get incoming and outgoing event dialogs to work without actors in the context (F.J.Chadburn)
    #125 25-NOV-2016: AutoRipple used in UpdateTracedAttributePanel to keep check and FlowPort name updated (F.J.Chadburn)
    #130 25-NOV-2016: Improved consistency in handling of isPopulateOptionHidden and isPopulateWantedByDefault tags (F.J.Chadburn)
    #148 18-DEC-2016: Add message if user tries to create an outgoing event when there are no actors (F.J.Chadburn)
    #186 29-MAY-2017: Add context string to getBlockUnderDev to make it clearer for user when selecting (F.J.Chadburn)
    #196 05-JUN-2017: Enhanced create traced element dialogs to be context aware for blocks/parts (F.J.Chadburn)
    #199 05-JUN-2017: Improved create event panel consistency to name event Tbd if no text provided (F.J.Chadburn)
    #200 05-JUN-2017: Hide Populate on diagram check-boxes if context is not valid (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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