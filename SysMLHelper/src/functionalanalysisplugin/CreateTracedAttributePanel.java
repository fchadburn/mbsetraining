package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateTracedAttributePanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JTextField m_InitialValueTextField = null;
	private JCheckBox m_CheckOperationCheckBox;
	private String m_CheckOpName;
	
	public static void createSystemAttributesFor(
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
	
	public CreateTracedAttributePanel(
			IRPGraphElement forSourceGraphElement, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock) {
		
		super(forSourceGraphElement, withReqtsAlsoAdded, onTargetBlock);
		
		IRPModelElement theModelObject = m_SourceGraphElement.getModelObject();
		
		final String theSourceText = GeneralHelpers.getActionTextFrom( theModelObject );	
		
		Logger.writeLine("CreateTracedAttributePanel constructor called with text '" + theSourceText + "'");
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( theSourceText ), 
				"Attribute", 
				onTargetBlock );

		Logger.writeLine("The proposed name is '" + theProposedName + "'");
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create an attribute called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		thePageStartPanel.add( theNamePanel );

		JPanel theInitialValuePanel = createInitialValuePanel( "0" );
		theInitialValuePanel.setAlignmentX( LEFT_ALIGNMENT );
		thePageStartPanel.add( theInitialValuePanel );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );

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
		
		m_CheckOperationCheckBox.setSelected(true);
		m_CheckOperationCheckBox.setEnabled(true);
		theCenterPanel.add( m_CheckOperationCheckBox );

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( theCenterPanel, BorderLayout.WEST );
		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );
				
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createInitialValuePanel(String withValue){
		
		JLabel theLabel =  new JLabel(" with the initial value:  ");
		
		m_InitialValueTextField = new JTextField();
		m_InitialValueTextField.setText( withValue );
		m_InitialValueTextField.setPreferredSize( new Dimension( 100, 20 ) );
		m_InitialValueTextField.setMaximumSize( new Dimension( 100, 20 ) );
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		thePanel.add( theLabel );
		thePanel.add( m_InitialValueTextField );
		
		return thePanel;
	}
	
	public static void launchThePanel(
			final IRPGraphElement selectedDiagramEl, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			final IRPProject inProject){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				IRPClassifier theLogicalSystemBlock = 
						FunctionalAnalysisSettings.getBlockUnderDev( 
								inProject, 
								FunctionalAnalysisSettings.getIsEnableBlockSelectionByUser( inProject ) );

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame(
						"Create an attribute on " + theLogicalSystemBlock.getUserDefinedMetaClass() 
						+ " called " + theLogicalSystemBlock.getName());

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateTracedAttributePanel thePanel = new CreateTracedAttributePanel(
						selectedDiagramEl, 
						withReqtsAlsoAdded,
						theLogicalSystemBlock);

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});			
	}
	
	private void updateNames(){
		
		m_CheckOpName = determineBestCheckOperationNameFor(
				(IRPClassifier)m_TargetOwningElement, 
				m_ChosenNameTextField.getText() );
		
		m_CheckOperationCheckBox.setText(
				"Add a '" + m_CheckOpName + 
				"' operation to the block that returns the attribute value" );
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		String errorMessage = "";
		boolean isValid = true;
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		boolean isLegalName = GeneralHelpers.isLegalName( theChosenName );
		
		if( !isLegalName ){
			
			errorMessage += theChosenName + " is not legal as an identifier representing an executable attribute\n";				
			isValid = false;
			
		} else if( !GeneralHelpers.isElementNameUnique(
				m_ChosenNameTextField.getText(), 
				"Attribute", 
				m_TargetOwningElement, 
				1 ) ){

			errorMessage = "Unable to proceed as the name '" + m_ChosenNameTextField.getText() + "' is not unique";
			isValid = false;

		} else if( m_CheckOperationCheckBox.isSelected() && 
				   !GeneralHelpers.isElementNameUnique(
						   m_CheckOpName,
						   "Operation",
						   m_TargetOwningElement, 
						   1 ) ){

			errorMessage = "Unable to proceed as the derived check operation name '" + 
					m_CheckOpName + "' is not unique";
			
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
	protected void performAction() {
		
		// do silent check first
		if (checkValidity( false )){
			
			IRPAttribute theAttribute =
					((IRPClassifier)m_TargetOwningElement).addAttribute(
							m_ChosenNameTextField.getText() );				
			
			theAttribute.highLightElement();
			theAttribute.setDefaultValue( m_InitialValueTextField.getText() );
			
			List<IRPRequirement> selectedReqtsList = m_RequirementsPanel.getSelectedRequirementsList();
			
			addTraceabilityDependenciesTo( theAttribute, selectedReqtsList );
			
			if( m_CheckOperationCheckBox.isSelected() ){
				IRPOperation theCheckOp = addCheckOperationFor( theAttribute, m_CheckOpName );
				addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList );	
				theCheckOp.highLightElement();
			}
			
			bleedColorToElementsRelatedTo( m_SourceGraphElement );
			
			theAttribute.highLightElement();
			
		} else {
			Logger.writeLine("Error in CreateOperationPanel.performAction, checkValidity returned false");
		}	
		
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #028 01-JUN-2016: Add new menu to create a stand-alone attribute owned by the system (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #042 29-JUN-2016: launchThePanel renaming to improve Panel class design consistency (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive new requirement to CallOperations and Event Actions (F.J.Chadburn)
    #082 09-AUG-2016: Add a check operation check box added to the create attribute dialog (F.J.Chadburn)
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