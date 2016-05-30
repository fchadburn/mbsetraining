package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateOutgoingEventPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IRPActor m_DestinationActor;
	private IRPPackage m_PackageUnderDev;
	private JCheckBox m_SendOperationIsNeededCheckBox;
	private JCheckBox m_ActiveAgumentNeededCheckBox;

	public CreateOutgoingEventPanel(
			IRPGraphElement forSourceGraphElement,
			IRPClassifier onTargetBlock,
			IRPActor toDestinationActor,
			IRPPackage forPackageUnderDev) {
		
		super( forSourceGraphElement, onTargetBlock );
		
		m_DestinationActor = toDestinationActor;
		m_PackageUnderDev = forPackageUnderDev;
		
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
		
		m_ActiveAgumentNeededCheckBox = new JCheckBox(
				"Add an 'active' argument to the event (e.g. for on/off conditions)");
		
		m_ActiveAgumentNeededCheckBox.setSelected(false);
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( createEventNamingPanel( theProposedName ), BorderLayout.PAGE_START );
		
		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);

		
		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );
		theCenterPanel.add( m_RequirementsPanel );
		theCenterPanel.add( m_SendOperationIsNeededCheckBox );
		theCenterPanel.add( m_ActiveAgumentNeededCheckBox );
		
		updateNames();
		
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	private static IRPInstance getPartUnderDev(IRPPackage inThePackage){
		
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
	
	private IRPPort getPortForDestinationActor(){
		
		IRPPort thePort = null;
		
		IRPModelElement theContextEl = getPartUnderDev(m_PackageUnderDev).getOwner();
		
		if (theContextEl instanceof IRPClassifier){
			IRPClassifier theContextBlock = (IRPClassifier) theContextEl;
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theLinks = theContextBlock.getNestedElementsByMetaClass("Link", 1).toList();
			
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
				"Add an '" + determineBestInformNameFor( m_TargetBlock, m_ChosenNameTextField.getText() ) 
				+ "' operation that sends the event");
	}
	
	private JPanel createEventNamingPanel( 
			String theProposedEventName ){
	
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		JLabel theLabel =  new JLabel("Create an event called:  ");
		thePanel.add( theLabel );
		
		m_ChosenNameTextField = new JTextField( theProposedEventName );
		m_ChosenNameTextField.setMaximumSize( new Dimension( 400,20 ) );
		
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
		
		thePanel.add( m_ChosenNameTextField );
	
		return thePanel;
	}

	@Override
	boolean checkValidity(
			boolean isMessageEnabled) {

		String errorMessage = null;
		boolean isValid = true;
		
		if (!GeneralHelpers.isElementNameUnique(
				m_ChosenNameTextField.getText(), 
				"Event", 
				m_PackageUnderDev.getProject(), 
				1)){

			errorMessage = "Unable to proceed as the event name '" + m_ChosenNameTextField.getText() + "' is not unique";
			isValid = false;
		}		

		if (m_SendOperationIsNeededCheckBox.isSelected()){
			
			String theProposedName = determineBestInformNameFor(
					m_TargetBlock, 
					m_ChosenNameTextField.getText());

			if (!GeneralHelpers.isElementNameUnique(
					theProposedName, 
					"Operation", 
					m_TargetBlock, 
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

			JDialog.setDefaultLookAndFeelDecorated(true);

			JOptionPane.showMessageDialog(
					null,  
					errorMessage,
					"Warning",
					JOptionPane.WARNING_MESSAGE);	
		}
		
		return isValid;
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
	
	@Override
	void performAction() {
		
		// do silent check first
		if (checkValidity( false )){
			
			String theEventName = m_ChosenNameTextField.getText(); 
			
			if (!theEventName.isEmpty()){
				
				IRPEvent theEvent = m_PackageUnderDev.addEvent(theEventName);
				
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
					/*
					@SuppressWarnings("unchecked")
					List<IRPModelElement> thePorts = 
							m_TargetBlock.getNestedElementsByMetaClass("Port", 0).toList();
					
					IRPModelElement thePort = 
							GeneralHelpers.launchDialogToSelectElement(thePorts, "Select Port to send Event to", false);
					*/
					Logger.writeLine("Adding an inform Operation");
					

					IRPOperation informOp = m_TargetBlock.addOperation( 
							determineBestInformNameFor( m_TargetBlock, theEventName ) );
					
					informOp.highLightElement();
					
					addTraceabilityDependenciesTo( informOp, selectedReqtsList );
					
					if (m_ActiveAgumentNeededCheckBox.isSelected()){
						
						informOp.addArgument("active");
						informOp.setBody("OPORT(" + thePort.getName()+")->GEN(" + theEventName + "( active ) );");
					} else {
						informOp.setBody("OPORT(" + thePort.getName()+")->GEN(" + theEventName + ");");
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