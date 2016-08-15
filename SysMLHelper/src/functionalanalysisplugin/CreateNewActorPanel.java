package functionalanalysisplugin;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class CreateNewActorPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage m_RootPackage;
	protected JTextField m_ChosenNameTextField = null;
	private ActorMappingInfo m_ClassifierMappingInfo;
	
	public CreateNewActorPanel(String forBlockName, IRPPackage theRootPackage) {
		
		super();

		m_RootPackage = theRootPackage;
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( createActorChoicePanel( forBlockName ), BorderLayout.PAGE_START );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createActorChoicePanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setPreferredSize( new Dimension( 300,20 ) );

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theExistingActors = 
				m_RootPackage.getNestedElementsByMetaClass("Actor", 1).toList();
		
		RhapsodyComboBox theInheritedActorComboBox = new RhapsodyComboBox(theExistingActors, false);
		
		//theInheritedActorComboBox.setPreferredSize(new Dimension(100, 20));
		
		JCheckBox theActorCheckBox = new JCheckBox("Create actor called:");
		    
		theActorCheckBox.setSelected(true);
			
		m_ClassifierMappingInfo = 
				new ActorMappingInfo(
						theInheritedActorComboBox, 
						theActorCheckBox, 
						m_ChosenNameTextField, 
						null,
						m_RootPackage.getProject() );
		
		m_ClassifierMappingInfo.updateToBestActorNamesBasedOn( theBlockName );
		
	    JLabel theLabel = new JLabel("Inherit from:");
	    
	    thePanel.add( theActorCheckBox );
	    thePanel.add( m_ChosenNameTextField );
	    thePanel.add( theLabel );
	    thePanel.add( theInheritedActorComboBox );
	    
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {
		
		boolean isValid = true;
		String errorMsg = "";
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		if ( theChosenName.contains( m_ClassifierMappingInfo.m_ActorBlankName ) ){
			
			errorMsg += "Please choose a valid name for the Actor";
			isValid = false;
			
		} else {
			boolean isLegalBlockName = GeneralHelpers.isLegalName( theChosenName );
			
			if (!isLegalBlockName){
				
				errorMsg += theChosenName + " is not legal as an identifier representing an executable Actor\n";				
				isValid = false;
				
			} else if (!GeneralHelpers.isElementNameUnique(
					
				theChosenName, "Actor", m_RootPackage, 1)){

				errorMsg += "Unable to proceed as the Actor name '" + theChosenName + "' is not unique";
				isValid = false;
			}
		}
		
		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {
		
		if (checkValidity( false )){
			
			IRPInstance partUnderDev = 
					FunctionalAnalysisSettings.getPartUnderDev(
							m_RootPackage.getProject() );
			
			if (partUnderDev != null){
				IRPClass theUsageBlock = (IRPClass)partUnderDev.getOwner();
				m_ClassifierMappingInfo.performActorPartCreationIfSelectedTo( theUsageBlock );
			}
						
		} else {
			Logger.writeLine("Error in CreateNewActorPanel.performAction, checkValidity returned false");
		}		
	}	
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    
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