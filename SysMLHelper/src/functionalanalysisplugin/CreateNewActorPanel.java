package functionalanalysisplugin;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
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
	private IRPClass m_BlockToConnectTo = null;
	
	public static void main(String[] args) {		
		launchThePanel();
	}
	
	public static void launchThePanel(){
		
		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		Logger.writeLine("Add new actor part" );
		// to " + Logger.elementInfo( theBlockUnderDev ) + " was invoked");
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame( "Create new Actor" );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewActorPanel thePanel = 
						new CreateNewActorPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateNewActorPanel( String theAppID ){
//			IRPClass forBlockToConnectTo, 
//			IRPPackage theRootPackage ){
		
		super( theAppID );
		
		IRPClass theBuildingBlock = 
				m_ElementContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null
			
			IRPClass theBlock = m_ElementContext.getBlockUnderDev(
					"Which Block/Part do you want to wire the Actor to?" );
			
			if( theBlock == null ){
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run this command because \n" +
						"there was no execution context or block found in the model. \n " +
						"You need to add the relevant package structure first." );
			} else {
				m_RootPackage = m_ElementContext.getSimulationSettingsPackageBasedOn( theBlock );
				m_BlockToConnectTo = m_ElementContext.getChosenBlock();
				
				setLayout( new BorderLayout(10,10) );
				setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
				
				if( m_BlockToConnectTo != null ){
					add( createActorChoicePanel( m_BlockToConnectTo.getName() ), BorderLayout.PAGE_START );

				} else {
					add( createActorChoicePanel( "" ), BorderLayout.PAGE_START );

				}
				
				add( createOKCancelPanel(), BorderLayout.PAGE_END );

			}
		}
	}

	@SuppressWarnings("unchecked")
	private JPanel createActorChoicePanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setPreferredSize( new Dimension( 300, 20 ) );

		List<IRPModelElement> theExistingActors;
		
		boolean isAllowInheritanceChoices = 
				StereotypeAndPropertySettings.getIsAllowInheritanceChoices( m_RootPackage );
		
		if( isAllowInheritanceChoices ){
			
			theExistingActors = m_RootPackage.getNestedElementsByMetaClass( 
					"Actor", 1 ).toList();
		} else {
			theExistingActors = new ArrayList<>();
		}
				
		RhapsodyComboBox theInheritedActorComboBox = 
				new RhapsodyComboBox( theExistingActors, false );
		
		JCheckBox theActorCheckBox = new JCheckBox( "Create actor called:" );
		    
		theActorCheckBox.setSelected( true );
			
		m_ClassifierMappingInfo = 
				new ActorMappingInfo(
						theInheritedActorComboBox, 
						theActorCheckBox, 
						m_ChosenNameTextField, 
						null,
						m_RootPackage.getProject() );
		
		m_ClassifierMappingInfo.updateToBestActorNamesBasedOn( theBlockName );
		
	    thePanel.add( theActorCheckBox );
	    thePanel.add( m_ChosenNameTextField );
	    
	    if( isAllowInheritanceChoices ){
		    JLabel theLabel = new JLabel( "Inherit from:" );
		    thePanel.add( theLabel );
		    thePanel.add( theInheritedActorComboBox );	    	
	    }
	    
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
			boolean isLegalBlockName = GeneralHelpers.isLegalName( theChosenName, m_BlockToConnectTo );
			
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
		
		if( checkValidity( false ) ){
			
			IRPClass theAssemblyBlock = 
					FunctionalAnalysisSettings.getBuildingBlock( m_RootPackage );
			
			if( m_RootPackage != null ){
				
				IRPInstance theActorPart =
						m_ClassifierMappingInfo.performActorPartCreationIfSelectedIn( 
								theAssemblyBlock, m_BlockToConnectTo );
				
				if( theActorPart != null ){
					
					SequenceDiagramHelper.updateAutoShowSequenceDiagramFor( 
							theAssemblyBlock );
				}
			
			} else {
				Logger.writeLine("Error in CreateNewActorPanel.performAction, unable to find " + Logger.elementInfo( m_RootPackage ) );
			}
						
		} else {
			Logger.writeLine("Error in CreateNewActorPanel.performAction, checkValidity returned false");
		}		
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #126 25-NOV-2016: Fixes to CreateNewActorPanel to cope better when multiple blocks are in play (F.J.Chadburn)
    #147 18-DEC-2016: Fix Actor part creation not being created in correct place if multiple hierarchies (F.J.Chadburn)
    #186 29-MAY-2017: Add context string to getBlockUnderDev to make it clearer for user when selecting (F.J.Chadburn)
    #187 29-MAY-2017: Provide option to re-create «AutoShow» sequence diagram when adding new actor (F.J.Chadburn)
    #216 09-JUL-2017: Added a new Add Block/Part command added to the Functional Analysis menus (F.J.Chadburn)
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