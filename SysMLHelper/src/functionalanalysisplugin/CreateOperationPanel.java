package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class CreateOperationPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CreateOperationPanel(
			IRPGraphElement forSourceGraphElement, 
			IRPClassifier onTargetBlock) {
		
		super(forSourceGraphElement, onTargetBlock);
		 
		IRPModelElement theModelObject = m_SourceGraphElement.getModelObject();
		
		final String theSourceText = GeneralHelpers.getActionTextFrom( theModelObject );	
		
		Logger.writeLine("CreateOperationPanel constructor called with text '" + theSourceText + "'");
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( theSourceText ), 
				"Operation", 
				onTargetBlock );					
		
		Logger.writeLine("The proposed name is '" + theProposedName + "'");
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
				
		add( createChosenNamePanel( theProposedName ), BorderLayout.PAGE_START );
		add( m_RequirementsPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	public JPanel createChosenNamePanel(
			String theProposedEventName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		JLabel theLabel =  new JLabel("Create an operation called:  ");
		thePanel.add( theLabel );
		
		m_ChosenNameTextField = new JTextField( theProposedEventName.length() );
		m_ChosenNameTextField.setText( theProposedEventName );
		m_ChosenNameTextField.setMaximumSize( new Dimension( 300,20 ) );
		
		thePanel.add( m_ChosenNameTextField );
		
		return thePanel;
	}
	
	boolean checkValidity(
			boolean isMessageEnabled){
		
		String errorMessage = null;
		boolean isValid = true;
		
		if (!GeneralHelpers.isElementNameUnique(
				m_ChosenNameTextField.getText(), "Operation", m_TargetBlock, 1)){

			errorMessage = "Unable to proceed as the name '" + m_ChosenNameTextField.getText() + "' is not unique";
			isValid = false;
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

	@Override
	void performAction() {
		// do silent check first
		if (checkValidity( false )){
			
			IRPOperation theOperation = m_TargetBlock.addOperation( m_ChosenNameTextField.getText() );				
			theOperation.highLightElement();
			addTraceabilityDependenciesTo( theOperation, m_RequirementsPanel.getSelectedRequirementsList() );
			bleedColorToElementsRelatedTo( m_SourceGraphElement );
			
		} else {
			Logger.writeLine("Error in CreateOperationPanel.performAction, checkValidity returned false");
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
