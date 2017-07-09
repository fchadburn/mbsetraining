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
		
		IRPApplication theApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theApp.activeProject() );
	}
	
	public static void launchThePanel(
			IRPProject theProject ){
		
		final IRPPackage thePackageUnderDev = 
				FunctionalAnalysisSettings.getPackageUnderDev( 
						theProject );

		final IRPClass theBlockUnderDev = 
				FunctionalAnalysisSettings.getBlockUnderDev( 
						theProject, "Which Block/Part do you want to wire the Actor to?" );
		
		Logger.writeLine("Add new actor part to " + Logger.elementInfo( thePackageUnderDev ) + " was invoked");
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				String msg;
				
				if( theBlockUnderDev != null ){
					msg = "Create new actor connected to " + Logger.elementInfo( theBlockUnderDev );
				} else {
					msg = "Create new actor";
				}
				
				JFrame frame = new JFrame( msg );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewActorPanel thePanel = 
						new CreateNewActorPanel( 
								theBlockUnderDev, thePackageUnderDev );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateNewActorPanel(
			IRPClass forBlockToConnectTo, 
			IRPPackage theRootPackage ){
		
		super();

		m_RootPackage = theRootPackage;
		m_BlockToConnectTo = forBlockToConnectTo;
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		if( m_BlockToConnectTo != null ){
			add( createActorChoicePanel( m_BlockToConnectTo.getName() ), BorderLayout.PAGE_START );

		} else {
			add( createActorChoicePanel( "" ), BorderLayout.PAGE_START );

		}
		
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createActorChoicePanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setPreferredSize( new Dimension( 300, 20 ) );

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theExistingActors = 
				m_RootPackage.getNestedElementsByMetaClass( "Actor", 1 ).toList();
		
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
		
	    JLabel theLabel = new JLabel( "Inherit from:" );
	    
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
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

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