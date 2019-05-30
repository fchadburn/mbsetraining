package executablembse;

import generalhelpers.ConfigurationSettings;
import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.ProfileVersionManager;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateUseCasesPackagePanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPPackage _ownerPkg;
	private IRPApplication _rhpApp;
	private CreateActorPkgChooser _createActorChooser;
	private CreateRequirementsPkgChooser _createRequirementsPkgChooser;
	private JTextField _nameTextField;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String theAppID = UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		ConfigurationSettings configSettings = new ConfigurationSettings(
				"ExecutableMBSE.properties", 
				"ExecutableMBSE_MessagesBundle" );

		ProfileVersionManager.checkAndSetProfileVersion( 
				false, 
				configSettings,
				true );

		launchTheDialog( theAppID );
	}

	public static void launchTheDialog(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate use case package structure" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				Logger.info( "Invoking dialog to 'Populate use case package structure'..." );

				CreateUseCasesPackagePanel thePanel = 
						new CreateUseCasesPackagePanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateUseCasesPackagePanel( 
			String theAppID ){

		super( theAppID );

		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		_ownerPkg = _rhpApp.activeProject();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 0, 10, 10, 10 ) );

		String theUniqueName = 
				GeneralHelpers.determineUniqueNameForPackageBasedOn(
						StereotypeAndPropertySettings.getDefaultUseCasePackageName( _ownerPkg ),
						_ownerPkg );

		JPanel theReqtsAnalysisPanel = createContent( theUniqueName );
		theReqtsAnalysisPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will create a package hierarchy for simple activity-based use case analysis underneath the " + Logger.elementInfo( _ownerPkg ) + ". \n" +
						"It creates a nested package structure and use case diagram, imports the appropriate profiles if not present, and sets default \n" +
						"display and other options to appropriate values for this using Rhapsody profile and property settings.\n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createTheNameThePackagePanel( _ownerPkg, theUniqueName ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theReqtsAnalysisPanel, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(
			String theName ){

		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );

		_createActorChooser = new CreateActorPkgChooser( 
				_ownerPkg );

		_createRequirementsPkgChooser = new CreateRequirementsPkgChooser( 
				_ownerPkg, 
				theName );

		theColumn1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );    
		theColumn1ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );   

		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );
		theVertical1ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );

		ParallelGroup theVertical2ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() );
		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );

		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}

	private void updateRelatedElementNames(){

		_createRequirementsPkgChooser.updateRequirementsPkgNameBasedOn( 
				_nameTextField.getText() );
	}

	private JPanel createTheNameThePackagePanel(
			IRPModelElement basedOnContext,
			String theUniqueName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		_nameTextField = new JTextField( theUniqueName );
		_nameTextField.setPreferredSize( new Dimension( 200, 20 ));

		_nameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate( DocumentEvent arg0 ){
						updateRelatedElementNames();					
					}

					@Override
					public void insertUpdate( DocumentEvent arg0 ){
						updateRelatedElementNames();
					}

					@Override
					public void removeUpdate( DocumentEvent arg0 ){
						updateRelatedElementNames();
					}	
				});

		thePanel.add( new JLabel( "Choose a unique name:" ) );
		thePanel.add( _nameTextField );	
		thePanel.add( new JLabel( " (package post-fixed with Pkg will created under " + Logger.elementInfo(basedOnContext) + ")" ) );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		// TODO 	
		return true;
	}

	@Override
	protected void performAction(){

		if( checkValidity( false ) ){

			String theUnadornedName = _nameTextField.getText(); 

			@SuppressWarnings("unused")
			CreateUseCasesPackage theCreateUseCasesPackage = new CreateUseCasesPackage(
					theUnadornedName, 
					_ownerPkg, 
					_createRequirementsPkgChooser.getReqtsPkgChoice(), 
					_createRequirementsPkgChooser.getReqtsPkgOptionalName(), 
					_createRequirementsPkgChooser.getExistingReqtsPkgIfChosen(), 
					_createActorChooser.getCreateActorPkgOption(), 
					_createActorChooser.getActorsPkgNameIfChosen(), 
					_createActorChooser.getExistingActorPkgIfChosen(), 
					theUnadornedName );
		}
	}


}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #249 29-MAY-2019: First official version of new ExecutableMBSEProfile  (F.J.Chadburn)

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