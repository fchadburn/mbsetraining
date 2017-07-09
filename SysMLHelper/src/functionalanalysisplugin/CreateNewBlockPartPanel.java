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
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class CreateNewBlockPartPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage m_RootPackage;
	private IRPClass m_AssemblyBlock;
	
	protected JTextField m_ChosenNameTextField = null;
	
	public static void main(String[] args) {
		
		IRPApplication theApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theApp.activeProject() );
	}
	
	public static void launchThePanel(
			IRPProject theProject ){
		
		final IRPPackage thePackageUnderDev = 
				FunctionalAnalysisSettings.getPackageUnderDev( 
						theProject );
		
		final IRPClass theAssemblyBlock = 
				FunctionalAnalysisSettings.getBuildingBlock(
						thePackageUnderDev );
		
		final IRPPackage theBlockPkg =
				FunctionalAnalysisSettings.getPackageForBlocks(
						theProject );
		
		Logger.writeLine("Add new block part to " + Logger.elementInfo( thePackageUnderDev ) + " was invoked");
		
		if( theAssemblyBlock == null ){
			Logger.writeLine("Error in CreateNewBlockPartPanel, unable to find assembly block");
		} else {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );
					
					String msg = "Create new Block/Part for " + Logger.elementInfo( theAssemblyBlock );
					
					JFrame frame = new JFrame( msg );
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateNewBlockPartPanel thePanel = 
							new CreateNewBlockPartPanel( 
									theBlockPkg,
									theAssemblyBlock );

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		}

	}
	
	public CreateNewBlockPartPanel(
			IRPPackage thePackageForBlock,
			IRPClass theAssemblyBlock ){
		
		super();

		m_RootPackage = thePackageForBlock;
		m_AssemblyBlock = theAssemblyBlock;
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( createBlockChoicePanel( "" ), BorderLayout.PAGE_START );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createBlockChoicePanel(
			String theBlockName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		m_ChosenNameTextField = new JTextField();
		m_ChosenNameTextField.setPreferredSize( new Dimension( 300, 20 ) );

		JCheckBox theBlockCheckBox = new JCheckBox( "Create block called:" );
		    
		theBlockCheckBox.setSelected( true );
	    thePanel.add( theBlockCheckBox );
	    thePanel.add( m_ChosenNameTextField );
	    
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {
		
		boolean isValid = true;
		String errorMsg = "";
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		if ( theChosenName.trim().isEmpty() ){
			
			errorMsg += "Please choose a valid name for the Block";
			isValid = false;
			
		} else {
			boolean isLegalBlockName = 
					GeneralHelpers.isLegalName( theChosenName );
			
			if( !isLegalBlockName ){
				
				errorMsg += theChosenName + " is not legal as an identifier representing an executable Block\n";				
				isValid = false;
				
			} else if( !GeneralHelpers.isElementNameUnique(
							theChosenName, 
							"Class", 
							m_RootPackage, 
							1 ) ){

				errorMsg += "Unable to proceed as the Block name '" + theChosenName + "' is not unique";
				isValid = false;
			}
		}
		
		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}

	private IRPInstance getElapsedTimeActorPartFor(
			IRPClass theAssemblyBlock ){
		
		IRPInstance theElapsedTimePart = null;
		
		@SuppressWarnings("unchecked")
		List<IRPInstance> theInstances = 
				theAssemblyBlock.getNestedElementsByMetaClass(
						"Instance", 0 ).toList();
		
		for( IRPInstance theInstance : theInstances ){
			
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			if( theClassifier != null &&
				theClassifier instanceof IRPActor &&
				theClassifier.getName().equals( "ElapsedTime" ) ){
				
				theElapsedTimePart = theInstance;
				break;
			}
		}
		
		return theElapsedTimePart;
	}
	
	@Override
	protected void performAction() {
		
		if( checkValidity( false ) ){
			
			if( m_RootPackage != null ){
				
				String theName = m_ChosenNameTextField.getText();
				
				IRPClass theClass = m_RootPackage.addClass( theName );
				theClass.changeTo( "Block" );
				theClass.highLightElement();
				
				IRPProject theProject = theClass.getProject();

				GeneralHelpers.addGeneralization( 
						theClass, 
						"TimeElapsedBlock", 
						theProject );
				
				IRPInstance thePart = 
						(IRPInstance) m_AssemblyBlock.addNewAggr(
								"Part", "" );
				
				thePart.setOtherClass( theClass );
				
				// Try and find ElapsedTime actor part 				
				IRPInstance theElapsedTimePart = 
						getElapsedTimeActorPartFor( m_AssemblyBlock );
				
				Logger.writeLine("Got here");
				if( theElapsedTimePart != null ){
					
					Logger.writeLine("Got here2");
					IRPClassifier theElapsedTimeActor = 
							theElapsedTimePart.getOtherClass();
					
					IRPSysMLPort theActorsElapsedTimePort = 
							(IRPSysMLPort) GeneralHelpers.findNestedElementUnder( 
									(IRPClassifier) theElapsedTimeActor,
									"elapsedTime",
									"SysMLPort",
									true );
							
					IRPSysMLPort theBlocksElapsedTimePort = 
							(IRPSysMLPort) GeneralHelpers.findNestedElementUnder( 
									(IRPClassifier) theClass,
									"elapsedTime",
									"SysMLPort",
									true );
					
					if( theActorsElapsedTimePort != null &&
						theBlocksElapsedTimePort != null ){
						
						GeneralHelpers.addConnectorBetweenSysMLPortsIfOneDoesntExist(
								theActorsElapsedTimePort, 
								theElapsedTimePart, 
								theBlocksElapsedTimePort, 
								thePart );
						
					} else {
						Logger.writeLine("Error in CreateNewBlockPartPanel.performAction(), unable to find elapsedTime ports") ;
					}
						
				} else {
					Logger.writeLine("Error in CreateNewBlockPartPanel.performAction: Unable to find ElapsedTime actor in project. You may be missing the BasePkg");
				}
				
				SequenceDiagramHelper.updateAutoShowSequenceDiagramFor( 
						m_AssemblyBlock );

			} else {
				Logger.writeLine("Error in CreateNewActorPanel.performAction, unable to find " + Logger.elementInfo( m_RootPackage ) );
			}
						
		} else {
			Logger.writeLine("Error in CreateNewActorPanel.performAction, checkValidity returned false");
		}		
	}	
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
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