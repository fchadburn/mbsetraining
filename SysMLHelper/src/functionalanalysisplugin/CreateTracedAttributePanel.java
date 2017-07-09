package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

import designsynthesisplugin.PortCreator;

public class CreateTracedAttributePanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JTextField m_InitialValueTextField = null;
	private JCheckBox m_CheckOperationCheckBox;
	private String m_CheckOpName;
	private JCheckBox m_CallOperationIsNeededCheckBox;
	
	private JRadioButton m_NoFlowPort;
	private JRadioButton m_PubFlowPort;
	private JRadioButton m_SubFlowPort;
	
	// test only
	public static void main(String[] args) {
		
		IRPProject theActiveProject = FunctionalAnalysisPlugin.getRhapsodyApp().activeProject();

		IRPModelElement theSelectedEl = FunctionalAnalysisPlugin.getRhapsodyApp().getSelectedElement();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = FunctionalAnalysisPlugin.getRhapsodyApp().getListOfSelectedElements().toList();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = FunctionalAnalysisPlugin.getRhapsodyApp().getSelectedGraphElements().toList();

		Logger.writeLine("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

		if( theSelectedGraphEls.isEmpty() && ( 
				theSelectedEl instanceof IRPClass ||
				theSelectedEl instanceof IRPInstance ||
				theSelectedEl instanceof IRPDiagram ) ){
			
			Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
			
			CreateTracedAttributePanel.launchThePanel(
					null,
					theSelectedEl, 
					theReqts, 
					theActiveProject );
			
		} else if (!theSelectedGraphEls.isEmpty()){
			try {
				CreateTracedAttributePanel.createSystemAttributesFor( 
						theActiveProject, theSelectedGraphEls );
				
			} catch (Exception e) {
				Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking CreateTracedAttributePanel.createSystemAttributeFor");
			}
		}
	}
	
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
			launchThePanel(	theSelectedGraphEls.get(0), null, theSelectedReqts, theActiveProject );
		} else {
			
			// launch a dialog for each selected element that is not a requirement
			for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				if (theModelObject != null && !(theModelObject instanceof IRPRequirement)){
					
					// only launch a dialog for non requirement elements
					launchThePanel(	theGraphEl, null, theSelectedReqts, theActiveProject );
				}		
			}
		}
	}
	
	public CreateTracedAttributePanel(
			IRPGraphElement forSourceGraphElement, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock) {
		
		super( forSourceGraphElement, withReqtsAlsoAdded, onTargetBlock, onTargetBlock.getProject() );
		 
		IRPModelElement theModelObject = m_SourceGraphElement.getModelObject();
		String theSourceText = GeneralHelpers.getActionTextFrom( theModelObject );	
		
		Logger.writeLine("CreateTracedAttributePanel constructor (1) called with text '" + theSourceText + "'");
		
		createCommonContent( onTargetBlock, theSourceText );
	}
	
	public CreateTracedAttributePanel(
			IRPModelElement forModelElement, 
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock ){
		
		super( forModelElement, withReqtsAlsoAdded, onTargetBlock, onTargetBlock.getProject() );
		
		String theSourceText = "attributeName";
		
		Logger.writeLine("CreateTracedAttributePanel constructor (2) called for " + Logger.elementInfo( forModelElement ) );
		
		createCommonContent( onTargetBlock, theSourceText );
	}

	private void createCommonContent(
			IRPClassifier onTargetBlock,
			String theSourceText) {
		
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

		m_CheckOperationCheckBox.addActionListener( new ActionListener() {
			
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  
			        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			        
			        boolean selected = abstractButton.getModel().isSelected();
					
					boolean isPopulate = 
							FunctionalAnalysisSettings.getIsPopulateWantedByDefault(
									m_TargetOwningElement.getProject() );
					
			        m_CallOperationIsNeededCheckBox.setEnabled(selected);
			        m_CallOperationIsNeededCheckBox.setSelected(selected && isPopulate);
			        			        
			      }} );
		
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
		
		m_CheckOperationCheckBox.setSelected(true);
		m_CheckOperationCheckBox.setEnabled(true);

		m_CallOperationIsNeededCheckBox = new JCheckBox("Populate the '" + m_CheckOpName + "' operation on diagram?");
		m_CallOperationIsNeededCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );
		setupPopulateCheckbox( m_CallOperationIsNeededCheckBox );
		
		updateNames();
		
		theCenterPanel.add( m_CheckOperationCheckBox );
		theCenterPanel.add( m_CallOperationIsNeededCheckBox );

		m_NoFlowPort  = new JRadioButton( "None", true );
		m_PubFlowPort = new JRadioButton( "«Pub»" );
		m_SubFlowPort = new JRadioButton( "«Sub»" );
		
		ButtonGroup group = new ButtonGroup();
		group.add( m_NoFlowPort );
		group.add( m_PubFlowPort );
		group.add( m_SubFlowPort );

		JPanel theFlowPortOptions = new JPanel();
		theFlowPortOptions.setLayout( new BoxLayout( theFlowPortOptions, BoxLayout.LINE_AXIS ) );
		theFlowPortOptions.setAlignmentX( LEFT_ALIGNMENT );
		theFlowPortOptions.add ( new JLabel("Create a FlowPort: ") );
		theFlowPortOptions.add( m_NoFlowPort );
		theFlowPortOptions.add( m_PubFlowPort );
		theFlowPortOptions.add( m_SubFlowPort );
	    
		theCenterPanel.add( theFlowPortOptions );
		
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
			final IRPModelElement orTheModelElement,
			final Set<IRPRequirement> withReqtsAlsoAdded,
			final IRPProject inProject){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				IRPClass theBlock = getBlock( 
						selectedDiagramEl, 
						orTheModelElement, 
						inProject, 
						"Select Block to add attribute to:" );
						
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame(
						"Create an attribute on " + theBlock.getUserDefinedMetaClass() 
						+ " called " + theBlock.getName());

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				if( selectedDiagramEl != null ){
					
					CreateTracedAttributePanel thePanel = 
							new CreateTracedAttributePanel(
									selectedDiagramEl, 
									withReqtsAlsoAdded,
									theBlock );

					frame.setContentPane( thePanel );
					
				} else if( orTheModelElement != null ){
					
					CreateTracedAttributePanel thePanel = 
							new CreateTracedAttributePanel(
									orTheModelElement, 
									withReqtsAlsoAdded,
									theBlock );

					frame.setContentPane( thePanel );
				}

				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});			
	}
	
	private void updateNames(){
		
		m_CheckOpName = GeneralHelpers.determineBestCheckOperationNameFor(
				(IRPClassifier)m_TargetOwningElement, 
				m_ChosenNameTextField.getText() );
		
		m_CheckOperationCheckBox.setText(
				"Add a '" + m_CheckOpName + 
				"' operation to the block that returns the attribute value" );
		
		m_CallOperationIsNeededCheckBox.setText(
				"Populate the '" + m_CheckOpName + "' operation on diagram?");
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

			List<IRPRequirement> selectedReqtsList = m_RequirementsPanel.getSelectedRequirementsList();

			IRPAttribute theAttribute = addAttributeTo( 
					(IRPClassifier) m_TargetOwningElement, 
					m_ChosenNameTextField.getText(), 
					m_InitialValueTextField.getText(),
					selectedReqtsList );
												
			if( m_CheckOperationCheckBox.isSelected() ){
				
				IRPOperation theCheckOp = addCheckOperationFor( theAttribute, m_CheckOpName );
				addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList );	
				
				if( m_CallOperationIsNeededCheckBox.isSelected() ){
					populateCallOperationActionOnDiagram( theCheckOp );
				}
				
				theCheckOp.highLightElement();
			}
			
			if( m_SourceGraphElement != null ){
				bleedColorToElementsRelatedTo( m_SourceGraphElement );
			}
			
			if( m_PubFlowPort.isSelected() ){
				PortCreator.createPublishFlowportFor( theAttribute );
			}

			if( m_SubFlowPort.isSelected() ){
				PortCreator.createSubscribeFlowportFor( theAttribute );
			}
			
			theAttribute.highLightElement();
			
		} else {
			Logger.writeLine("Error in CreateOperationPanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #028 01-JUN-2016: Add new menu to create a stand-alone attribute owned by the system (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #042 29-JUN-2016: launchThePanel renaming to improve Panel class design consistency (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive new requirement to CallOperations and Event Actions (F.J.Chadburn)
    #082 09-AUG-2016: Add a check operation check box added to the create attribute dialog (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #090 15-AUG-2016: Fix check operation name issue introduced in fixes #083 and #084 (F.J.Chadburn)
    #115 13-NOV-2016: Removed use of isEnableBlockSelectionByUser tag and <<LogicalSystem>> by helper (F.J.Chadburn)
    #119 13-NOV-2016: Add a populate check operation option to the add new attribute panel (F.J.Chadburn)
    #125 25-NOV-2016: AutoRipple used in UpdateTracedAttributePanel to keep check and FlowPort name updated (F.J.Chadburn)
    #130 25-NOV-2016: Improved consistency in handling of isPopulateOptionHidden and isPopulateWantedByDefault tags (F.J.Chadburn)
    #137 02-DEC-2016: Allow 'create attribute' menu command on AD/RD canvas right-click (F.J.Chadburn)
    #153 25-JAN-2017: Functional Analysis helper creates new term ValueProperty's rather than attributes in Rhp 8.2+ (F.J.Chadburn) 
    #176 02-APR-2017: Added option to create a flow-port at the same time as creating a traced attribute (F.J.Chadburn)
    #186 29-MAY-2017: Add context string to getBlockUnderDev to make it clearer for user when selecting (F.J.Chadburn)
    #196 05-JUN-2017: Enhanced create traced element dialogs to be context aware for blocks/parts (F.J.Chadburn)
    #197 05-JUN-2017: Fix 8.2 issue in Incoming Event panel, create ValueProperty rather than attribute (F.J.Chadburn)
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