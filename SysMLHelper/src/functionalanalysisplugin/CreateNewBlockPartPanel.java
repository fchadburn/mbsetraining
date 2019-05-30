package functionalanalysisplugin;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
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

public class CreateNewBlockPartPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage m_RootPackage;
	private IRPClass m_AssemblyBlock;

	protected JTextField m_BlockNameTextField = null;
	protected JTextField m_PartNameTextField = null;

	protected RhapsodyComboBox m_ChosenStereotype;

	public static void main(String[] args) {
		launchThePanel();
	}

	public static void launchThePanel(){

		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		Logger.writeLine("Add new block part");

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create new Block/Part" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewBlockPartPanel thePanel = 
						new CreateNewBlockPartPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateNewBlockPartPanel(
			String theAppID ){
		//			IRPPackage thePackageForBlock,
		//			IRPClass theAssemblyBlock ){

		super( theAppID );

		IRPClass theBuildingBlock = 
				m_ElementContext.getBuildingBlock();

		if( theBuildingBlock == null ){

			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
							"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );

		} else { // theBuildingBlock != null

			m_RootPackage = m_ElementContext.getPackageForBlocks();
			m_AssemblyBlock = m_ElementContext.getBuildingBlock();

			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			add( createBlockChoicePanel( "" ), BorderLayout.PAGE_START );
			add( createStereotypePanel(), BorderLayout.CENTER );	    
			add( createOKCancelPanel(), BorderLayout.PAGE_END );


		}
	}



private JPanel createStereotypePanel(){

	JPanel thePanel = new JPanel();
	thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

	List<IRPModelElement> theStereotypes = 
			StereotypeAndPropertySettings.getStereotypesForBlockPartCreation( 
					m_RootPackage.getProject() );

	m_ChosenStereotype = new RhapsodyComboBox( theStereotypes, false );
	m_ChosenStereotype.setMaximumSize( new Dimension( 250, 20 ) );

	if( theStereotypes.size() > 0 ){
		// set to first value in list
		m_ChosenStereotype.setSelectedRhapsodyItem( theStereotypes.get( 0 ) );	
		Logger.writeLine("Setting default stereotype to " + Logger.elementInfo(theStereotypes.get( 0 )));
	}

	thePanel.add( new JLabel( "  Stereotype as: " ) );
	thePanel.add( m_ChosenStereotype );

	return thePanel;
}

private JPanel createBlockChoicePanel(
		String theBlockName ){

	JPanel thePanel = new JPanel();
	thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

	m_BlockNameTextField = new JTextField();
	m_BlockNameTextField.setPreferredSize( new Dimension( 150, 20 ) );

	JCheckBox theBlockCheckBox = new JCheckBox( "Create block called:" );

	theBlockCheckBox.setSelected( true );
	thePanel.add( theBlockCheckBox );
	thePanel.add( m_BlockNameTextField );

	thePanel.add( new JLabel(" with part name (leave blank for default): ") );

	m_PartNameTextField = new JTextField();
	m_PartNameTextField.setPreferredSize( new Dimension( 150, 20 ) );

	thePanel.add( m_PartNameTextField );

	return thePanel;
}

@Override
protected boolean checkValidity(
		boolean isMessageEnabled) {

	boolean isValid = true;
	String errorMsg = "";

	String theBlockName = m_BlockNameTextField.getText();

	if ( theBlockName.trim().isEmpty() ){

		errorMsg += "Please choose a valid name for the Block";
		isValid = false;

	} else {
		boolean isLegalBlockName = 
				GeneralHelpers.isLegalName( theBlockName, m_RootPackage );

		if( !isLegalBlockName ){

			errorMsg += theBlockName + " is not legal as an identifier representing an executable Block\n";				
			isValid = false;

		} else if( !GeneralHelpers.isElementNameUnique(
				theBlockName, 
				"Class", 
				m_RootPackage, 
				1 ) ){

			errorMsg += "Unable to proceed as the Block name '" + theBlockName + "' is not unique";
			isValid = false;

		} else {

			String thePartName = m_PartNameTextField.getText();

			if ( !thePartName.trim().isEmpty() ){

				boolean isLegalPartName = 
						GeneralHelpers.isLegalName( thePartName, m_AssemblyBlock );

				if( !isLegalPartName ){

					errorMsg += thePartName + " is not legal as an identifier representing an executable Part\n";				
					isValid = false;

				} else if( !GeneralHelpers.isElementNameUnique(
						thePartName, 
						"Object", 
						m_AssemblyBlock, 
						0 ) ){

					errorMsg += "Unable to proceed as the Part name '" + thePartName + "' is not unique for " + 
							Logger.elementInfo( m_AssemblyBlock );

					isValid = false;
				}
			}
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

			String theName = m_BlockNameTextField.getText();

			IRPClass theClass = m_RootPackage.addClass( theName );
			theClass.highLightElement();				

			IRPProject theProject = theClass.getProject();

			GeneralHelpers.addGeneralization( 
					theClass, 
					"TimeElapsedBlock", 
					theProject );

			String thePartName = m_PartNameTextField.getText().trim();

			IRPInstance thePart = 
					(IRPInstance) m_AssemblyBlock.addNewAggr(
							"Part", thePartName );

			thePart.setOtherClass( theClass );
			thePart.highLightElement();

			IRPModelElement theSelectedStereotype = m_ChosenStereotype.getSelectedRhapsodyItem();

			if( theSelectedStereotype != null && 
					theSelectedStereotype instanceof IRPStereotype ){

				try {
					theClass.setStereotype( (IRPStereotype) theSelectedStereotype );

				} catch (Exception e) {
					Logger.writeLine("Exception in CreateNewBlockPartPanel.performAction, unable to apply " + 
							theSelectedStereotype.getName() + " to " + Logger.elementInfo( theClass ) );	
				}

				try {
					thePart.setStereotype( (IRPStereotype) theSelectedStereotype );

				} catch (Exception e) {
					Logger.writeLine("Exception in CreateNewBlockPartPanel.performAction, unable to apply " + 
							theSelectedStereotype.getName() + " to " + Logger.elementInfo( thePart ) );	
				}
			}

			theClass.changeTo( "Block" );

			// Try and find ElapsedTime actor part 				
			IRPInstance theElapsedTimePart = 
					getElapsedTimeActorPartFor( m_AssemblyBlock );

			if( theElapsedTimePart != null ){

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
 * Copyright (C) 2017-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #216 09-JUL-2017: Added a new Add Block/Part command added to the Functional Analysis menus (F.J.Chadburn)
    #220 12-JUL-2017: Added customisable Stereotype choice to the Block and block/Part creation dialogs (F.J.Chadburn) 
    #236 27-SEP-2017: Improved Add new Block/Part... dialog to allow naming of part (F.J.Chadburn)
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