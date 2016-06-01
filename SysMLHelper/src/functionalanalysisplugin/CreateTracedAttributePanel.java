package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class CreateTracedAttributePanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JTextField m_InitialValueTextField = null;
    
	public CreateTracedAttributePanel(
			IRPGraphElement forSourceGraphElement, 
			IRPClassifier onTargetBlock) {
		
		super(forSourceGraphElement, onTargetBlock);
		
		IRPModelElement theModelObject = m_SourceGraphElement.getModelObject();
		
		final String theSourceText = GeneralHelpers.getActionTextFrom( theModelObject );	
		
		Logger.writeLine("CreateTracedAttributePanel constructor called with text '" + theSourceText + "'");
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( theSourceText ), 
				"Attribute", 
				onTargetBlock );					
		 
		Logger.writeLine("The proposed name is '" + theProposedName + "'");
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		m_RequirementsPanel.setAlignmentX( LEFT_ALIGNMENT );
		
		JPanel theInitialValuePanel = createInitialValuePanel();
		theInitialValuePanel.setAlignmentX( LEFT_ALIGNMENT );
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		
		JPanel theNamePanel = createChosenNamePanel( theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		thePageStartPanel.add( theNamePanel );
		thePageStartPanel.add( theInitialValuePanel );
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( m_RequirementsPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	public JPanel createInitialValuePanel(){
		
		JLabel theLabel =  new JLabel(" with the initial value:  ");
		
		m_InitialValueTextField = new JTextField();
		m_InitialValueTextField.setText( "0" );
		m_InitialValueTextField.setPreferredSize( new Dimension( 100, 20 ) );
		m_InitialValueTextField.setMaximumSize( new Dimension( 100, 20 ) );
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		thePanel.add( theLabel );
		thePanel.add( m_InitialValueTextField );
		
		return thePanel;
	}

	public JPanel createChosenNamePanel(
			String theProposedEventName ){
		
		JLabel theLabel =  new JLabel("Create an attribute called:  ");
		
		m_ChosenNameTextField = new JTextField( theProposedEventName.length() );
		m_ChosenNameTextField.setText( theProposedEventName );
		m_ChosenNameTextField.setPreferredSize( new Dimension( 300,20 ) );
		m_ChosenNameTextField.setMaximumSize( new Dimension( 300,20 ) );

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );
		
		thePanel.add( theLabel );
		thePanel.add( m_ChosenNameTextField );
		
		return thePanel;
	}

	public static void createSystemAttributeFor(
			final IRPProject forProject,
			List<IRPGraphElement> theSelectedGraphEls ){
		
		int numberOfSelectedEls = theSelectedGraphEls.size();
		
		if( numberOfSelectedEls==1 ){
			
			final IRPGraphElement selectedDiagramEl = theSelectedGraphEls.get(0);
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					IRPClassifier theLogicalSystemBlock = FunctionalAnalysisSettings.getBlockUnderDev( forProject );
					
					JFrame.setDefaultLookAndFeelDecorated( true );
					
					JFrame frame = new JFrame(
							"Create an attribute on " + theLogicalSystemBlock.getUserDefinedMetaClass() 
							+ " called " + theLogicalSystemBlock.getName());
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
					
					CreateTracedAttributePanel thePanel = new CreateTracedAttributePanel(
							selectedDiagramEl, 
							theLogicalSystemBlock);

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
			
		} else if ( numberOfSelectedEls > 1 ){
			
			UserInterfaceHelpers.showWarningDialog(
					"This operation only works if you select a single graphical element");	
		}
	}
	
	@Override
	boolean checkValidity(
			boolean isMessageEnabled) {
		
		String errorMessage = "";
		boolean isValid = true;
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		boolean isLegalName = GeneralHelpers.isLegalName( theChosenName );
		
		if (!isLegalName){
			
			errorMessage += theChosenName + " is not legal as an identifier representing an executable attribute\n";				
			isValid = false;
			
		} else if (!GeneralHelpers.isElementNameUnique(
				m_ChosenNameTextField.getText(), "Attribute", m_TargetBlock, 1)){

			errorMessage = "Unable to proceed as the name '" + m_ChosenNameTextField.getText() + "' is not unique";
			isValid = false;
			
		} else if (!isInteger( m_InitialValueTextField.getText() )){
			
			errorMessage = "Unable to proceed as the initial value '" + m_ChosenNameTextField.getText() + "' is not an integer";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelpers.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	        
	    } catch(NumberFormatException e) { 
	        return false; 
	        
	    } catch(NullPointerException e) {
	        return false;
	    }
	    
	    return true;
	}

	@Override
	void performAction() {
		
		// do silent check first
		if (checkValidity( false )){
			
			IRPAttribute theAttribute = m_TargetBlock.addAttribute( m_ChosenNameTextField.getText() );				
			theAttribute.highLightElement();
			theAttribute.setDefaultValue( m_InitialValueTextField.getText() );
			
			addTraceabilityDependenciesTo( theAttribute, m_RequirementsPanel.getSelectedRequirementsList() );
			bleedColorToElementsRelatedTo( m_SourceGraphElement );
			
		} else {
			Logger.writeLine("Error in CreateOperationPanel.performAction, checkValidity returned false");
		}	
		
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #028 01-JUN-2016: Add new menu to create a stand-alone attribute owned by the system (F.J.Chadburn)
    
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