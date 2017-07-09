package designsynthesisplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class AutoConnectFlowPortsInfo {

	private String m_AttributeName;

	final private String m_DoNothing = "Do nothing";
	final private String m_CreateNew = "Create new attribute";
	final private String m_None = "<None>";
	
	private IRPAttribute m_SubscribingAttribute;
	private IRPSysMLPort m_SubscribingFlowPort;
	private IRPInstance m_SubscribingPart;
	private IRPClassifier m_SubscribingBlock;
	private Set<IRPStructureDiagram> m_DiagramsToUpdate = null;
	private IRPAttribute m_PublishingAttribute;
	private IRPSysMLPort m_PublishingFlowPort;
	private IRPInstance m_PublishingPart;
	private IRPClassifier m_BuildingBlock;
	private JComboBox<Object> m_BindingChoiceComboBox;

	private Set<IRPLink> m_Links = new HashSet<IRPLink>();
	
	protected JTextField m_ChosenNameTextField = new JTextField();
	
	private FlowType m_ExistingFlowType = FlowType.Nothing;
		
	public enum FlowType {
	    Nothing, Publish, Subscribe
	}	
	
	public static void main(String[] args) {
	
		IRPApplication theApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theApp.getSelectedElement();
		
		if( theSelectedEl instanceof IRPAttribute ){
			AutoConnectFlowPortsPanel.launchThePanel( (IRPAttribute) theSelectedEl );
		}
	}
	
	public List<IRPAttribute> getExistingAttributesLinkedTo(
			IRPAttribute theSrcAttribute,
			IRPInstance inTgtPart ){

		Logger.writeLine("getExistingAttributesLinkedTo invoked for " + 
				Logger.elementInfo( theSrcAttribute ) + " owned by " + 
				Logger.elementInfo( theSrcAttribute.getOwner() ) + " to see if there are links to " + 
				Logger.elementInfo( inTgtPart ) );
		
		List<IRPAttribute> theExistingAttributes = 
				new ArrayList<IRPAttribute>();
		
		IRPSysMLPort theSrcFlowPort = 
				GeneralHelpers.getExistingFlowPort( 
						theSrcAttribute );

		if( theSrcFlowPort == null ){

			Logger.writeLine("Warning in getExistingFlowPortsLinkedTo, " + 
					Logger.elementInfo( theSrcAttribute ) + 
					" has no flowport when one is expected");
			
		} else { // if( theSrcFlowPort != null ){

			IRPClassifier theAssemblyBlock = 
					(IRPClassifier) inTgtPart.getOwner();
			
			@SuppressWarnings("unchecked")
			List<IRPAttribute> theTgtAttributes = 
					inTgtPart.getOtherClass().getAttributes().toList();
			
			for( IRPAttribute theTgtAttribute : theTgtAttributes ){
				
				IRPSysMLPort theTgtFlowPort = 
						GeneralHelpers.getExistingFlowPort( 
								theTgtAttribute );

				if( theTgtFlowPort != null ){
					
					m_Links.addAll( GeneralHelpers.getLinksBetween(
							theSrcFlowPort, 
							m_PublishingPart,
							theTgtFlowPort, 
							inTgtPart,
							theAssemblyBlock ) );
					
					boolean isExistingLink = m_Links.size() > 0 ;
							
					if( isExistingLink ){
						Logger.writeLine("Found " + Logger.elementInfo( theTgtFlowPort ) );
						theExistingAttributes.add( theTgtAttribute );
					}
				}									
			}
		}
		
		return theExistingAttributes;
	}
	
	public AutoConnectFlowPortsInfo(
			IRPAttribute toPublishingAttribute,
			IRPInstance toPublishingPart,
			IRPInstance fromSubscribingPart ) {

		Logger.writeLine("AutoConnectFlowPortsInfo constructor was invoked for " + 
				Logger.elementInfo( toPublishingAttribute ) + " to subscriber " +
				Logger.elementInfo( fromSubscribingPart ) );
		
		m_PublishingAttribute = toPublishingAttribute;
		m_PublishingFlowPort = GeneralHelpers.getExistingFlowPort( m_PublishingAttribute );
		m_PublishingPart = toPublishingPart;
		
		m_AttributeName = toPublishingAttribute.getName();
		m_SubscribingPart = fromSubscribingPart;
		m_SubscribingBlock = fromSubscribingPart.getOtherClass();
		m_BuildingBlock = (IRPClassifier) fromSubscribingPart.getOwner();

		m_BindingChoiceComboBox = new JComboBox<Object>();
		
		m_BindingChoiceComboBox.addActionListener( new ActionListener(){
		    public void actionPerformed( ActionEvent e ){
		    	
		    	String selectedValue = m_BindingChoiceComboBox.getSelectedItem().toString();
		    	
		    	if( selectedValue.equals( m_DoNothing ) ){
		    		m_ChosenNameTextField.setText( m_None );
		    		m_ChosenNameTextField.setEnabled( false );
		    		
		    	} else if( selectedValue.equals( m_CreateNew ) ){
		    		
		    		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
		    				m_PublishingAttribute.getName(), 
		    				"Attribute", 
		    				m_SubscribingBlock );
		    		
		    		m_ChosenNameTextField.setText( theProposedName );
		    		m_ChosenNameTextField.setEnabled( true );		    		
		    	} else {
		    		m_ChosenNameTextField.setText( selectedValue );
		    		m_ChosenNameTextField.setEnabled( false );		 		    		
		    	}
		    	
		    	Logger.writeLine( selectedValue + " was selected" );
		    }
		});

		@SuppressWarnings("unchecked")
		List<IRPAttribute> theAttributes = 
				m_SubscribingBlock.getAttributes().toList();
				
		List<IRPAttribute> theExistingLinkedAttributes = 
				getExistingAttributesLinkedTo( 
						m_PublishingAttribute, m_SubscribingPart );
		
		if( theExistingLinkedAttributes.size() > 0 ){

			IRPAttribute theExistingAttribute = 
					theExistingLinkedAttributes.get(0);

			String theOption = "Already connected";
			m_BindingChoiceComboBox.addItem( theOption );			
			m_BindingChoiceComboBox.setSelectedItem( theOption );
			m_BindingChoiceComboBox.setEnabled( false );				
			
			if( theExistingLinkedAttributes.size() == 1 ){
				m_ChosenNameTextField.setText( theExistingAttribute.getName() );
			} else {
				m_ChosenNameTextField.setText( "<Multiple Connectors>" );
			}
			
			m_ChosenNameTextField.setEnabled( false );
		
		} else {
			
			IRPAttribute theAttributeWithSameName = 
					m_SubscribingBlock.findAttribute( toPublishingAttribute.getName() );

			m_BindingChoiceComboBox.addItem( m_DoNothing );
			m_BindingChoiceComboBox.addItem( m_CreateNew );

			for( IRPAttribute theAttribute : theAttributes ){			
				String theName = theAttribute.getName();
				m_BindingChoiceComboBox.addItem( theName );		
			}

			m_BindingChoiceComboBox.setSelectedItem( m_DoNothing );
			m_BindingChoiceComboBox.setEnabled( true );

			if( theAttributeWithSameName != null ){

				m_BindingChoiceComboBox.setSelectedItem( toPublishingAttribute.getName() );
				m_ChosenNameTextField.setText( toPublishingAttribute.getName() );
				m_ChosenNameTextField.setEnabled( false );

			} else {
				m_ChosenNameTextField.setText( m_None );
				m_ChosenNameTextField.setEnabled( false );
			}
		}
		
		IRPModelElement theEl = 
				fromSubscribingPart.getOtherClass().findNestedElement( 
						m_AttributeName, "Attribute" );
		
		if( theEl != null ){
			
			m_SubscribingAttribute = (IRPAttribute) theEl;
			m_SubscribingFlowPort = GeneralHelpers.getExistingFlowPort( m_SubscribingAttribute );
			
			if( m_SubscribingFlowPort != null ){
				
				m_Links.addAll( GeneralHelpers.getLinksBetween(
						m_SubscribingFlowPort, 
						m_SubscribingPart,
						m_PublishingFlowPort, 
						m_PublishingPart,
						m_BuildingBlock ) );
						
				
			} else {
				//m_LinkToExistingButton.setSelected( true );
			}

		} else {
			
			Logger.writeLine( fromSubscribingPart, "has no attribute called " + m_AttributeName );

			m_SubscribingAttribute = null;
			m_SubscribingFlowPort = null;
			m_ExistingFlowType = FlowType.Nothing;

		}
	}
	
	public IRPSysMLPort getSubscribingFlowPort() {
		
		Logger.writeLine("getSubscribingFlowPort was invoked for " + Logger.elementInfo( m_SubscribingPart ) + " and is returning " + Logger.elementInfo (m_SubscribingFlowPort) + " for attribute " + Logger.elementInfo( m_SubscribingAttribute ));
		return m_SubscribingFlowPort;
	}
	
	public IRPSysMLPort getPublishingFlowPort() {
		return m_PublishingFlowPort;
	}
	
	public IRPClassifier getOwningBlock() {
		return m_SubscribingBlock;
	}
	
	public IRPClassifier getBuildingBlock() {
		return m_BuildingBlock;
	}
	
	public IRPInstance getOwnedByPart() {
		return m_SubscribingPart;
	}
	
	public boolean isThereAnExistingAttribute(){
		return m_SubscribingAttribute != null;
	}
	
	public FlowType getExistingAttributeFlowType(){
		return m_ExistingFlowType;
	}
	
	public String getIDString(){
		return m_SubscribingPart.getName() + ":" + m_SubscribingPart.getOtherClass().getName();
	}
	
	public boolean isCreateNewSelected(){
		return m_BindingChoiceComboBox.isEnabled() &&
			   m_BindingChoiceComboBox.getSelectedItem().equals( m_CreateNew );
	}
		
	public void performSelectedOperations(){
		
		IRPLink theLink = null;
		
		m_DiagramsToUpdate = new HashSet<IRPStructureDiagram>();
		
		if( isCreateNewSelected() ){

				Logger.writeLine("Add a new " + Logger.elementInfo( m_PublishingAttribute ) + 
						" to " + getIDString() );
				
				IRPAttribute theAttribute = (IRPAttribute) m_PublishingAttribute.clone( 
						m_ChosenNameTextField.getText(), 
						getOwningBlock() );
				
				m_SubscribingFlowPort = PortCreator.createSubscribeFlowportFor( theAttribute );

				theLink = GeneralHelpers.addConnectorBetweenSysMLPortsIfOneDoesntExist(
						m_PublishingFlowPort,
						m_PublishingPart, 
						m_SubscribingFlowPort, 
						m_SubscribingPart );
				
				m_Links.add( theLink );

			
		} else if( !m_BindingChoiceComboBox.getSelectedItem().equals( m_DoNothing ) ) {
			
			m_SubscribingFlowPort = PortCreator.createSubscribeFlowportFor( m_SubscribingAttribute );

			theLink = GeneralHelpers.addConnectorBetweenSysMLPortsIfOneDoesntExist(
					m_PublishingFlowPort,
					m_PublishingPart, 
					m_SubscribingFlowPort, 
					m_SubscribingPart );

			m_Links.add( theLink );

		}
		
		if( theLink != null ){
			
			IRPSysMLPort fromPort = theLink.getFromSysMLPort();
			IRPSysMLPort toPort = theLink.getToSysMLPort();
			
			m_DiagramsToUpdate.addAll( getIBDsWith( fromPort ) );
			m_DiagramsToUpdate.retainAll( getIBDsWith( toPort ) );
		}		
	}
	
	Set<IRPStructureDiagram> getIBDsWith( 
			IRPSysMLPort theFlowPort ){
		
		Set<IRPStructureDiagram> theIBDsWith = new HashSet<IRPStructureDiagram>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = 
				theFlowPort.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPStructureDiagram ){
				
				theIBDsWith.add( (IRPStructureDiagram) theReference );
				
				Logger.writeLine( theFlowPort, " owned by " + 
						Logger.elementInfo( theFlowPort.getOwner() ) + 
						" has a reference to " + Logger.elementInfo( theReference ) );
			}
		}
		
		return theIBDsWith;
	}
	
	public IRPClassifier getM_SubscribingBlock() {
		return m_SubscribingBlock;
	}
	
	public JComboBox<Object> getM_BindingChoiceComboBox() {
		return m_BindingChoiceComboBox;
	}
	
	public Set<IRPStructureDiagram> getM_DiagramsToUpdate(){
		return m_DiagramsToUpdate;
	}
	
	JTextField getM_ChosenNameTextField() {
		return m_ChosenNameTextField;
	}
	
	Set<IRPLink> getM_Links(){
		return m_Links;
	}

}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #213 09-JUL-2017: Add dialogs to auto-connect «publish»/«subscribe» FlowPorts for white-box simulation (F.J.Chadburn)
        
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
