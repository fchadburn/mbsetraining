package functionalanalysisplugin;

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

import com.telelogic.rhapsody.core.*;

public class CreateOperationPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox m_CallOperationIsNeededCheckBox;

	public static void createSystemOperationsFor(
			IRPProject theActiveProject,
			List<IRPGraphElement> theSelectedGraphEls) {
		
		Set<IRPModelElement> theMatchingEls = 
				GeneralHelpers.findModelElementsIn( theSelectedGraphEls, "Requirement" );
		
		// cast to IRPRequirement
		@SuppressWarnings("unchecked")
		Set<IRPRequirement> theSelectedReqts = (Set<IRPRequirement>)(Set<?>) theMatchingEls;
		
		if (GeneralHelpers.doUnderlyingModelElementsIn( theSelectedGraphEls, "Requirement" )){
			
			// only requirements are selected hence assume only a single operation is needed
			createSystemOperationFor( 
					theSelectedGraphEls.get(0), theSelectedReqts, theActiveProject );
		} else {
			
			// launch a dialog for each selected element that is not a requirement
			for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				if (theModelObject != null && !(theModelObject instanceof IRPRequirement)){
					
					// only launch a dialog for non requirement elements
					createSystemOperationFor(
							theGraphEl, theSelectedReqts, theActiveProject );
				}		
			}
		}
	}
	
	public CreateOperationPanel(
			IRPGraphElement forSourceGraphElement, 
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock) {
		
		super(forSourceGraphElement, withReqtsAlsoAdded, onTargetBlock);
		 
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

		m_RequirementsPanel.setAlignmentX( LEFT_ALIGNMENT );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create an operation called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		
		m_CallOperationIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		m_CallOperationIsNeededCheckBox.setSelected(false);
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( theNamePanel );
		thePageStartPanel.add( m_CallOperationIsNeededCheckBox );
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( m_RequirementsPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	private static void createSystemOperationFor(
			final IRPGraphElement selectedDiagramEl, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			final IRPProject inProject){
	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				IRPClassifier theLogicalSystemBlock = FunctionalAnalysisSettings.getBlockUnderDev( inProject );
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame(
						"Create an operation on " + theLogicalSystemBlock.getUserDefinedMetaClass() 
						+ " called " + theLogicalSystemBlock.getName());
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				
				CreateOperationPanel thePanel = new CreateOperationPanel(
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
	
	@Override
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

			UserInterfaceHelpers.showWarningDialog( errorMessage );
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
			
			if (m_CallOperationIsNeededCheckBox.isSelected()){
				populateCallOperationActionOnDiagram( theOperation );
			}
			
		} else {
			Logger.writeLine("Error in CreateOperationPanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)

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
