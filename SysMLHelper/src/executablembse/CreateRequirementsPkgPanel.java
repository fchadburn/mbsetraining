package executablembse;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.PopulatePkg;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;

import com.telelogic.rhapsody.core.*;

public class CreateRequirementsPkgPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IRPPackage _ownerPkg;
	private IRPApplication _rhpApp;
	private IRPProject _rhpPrj;

	private CreateRequirementsPkgChooser _createRequirementsPkgChooser;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launchTheDialog();		
	}
	
	public static void launchTheDialog(){
		
		UserInterfaceHelpers.setLookAndFeel();
		
		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		if( theAppID != null ){

			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					JFrame.setDefaultLookAndFeelDecorated( true );

					JFrame frame = new JFrame( "Populate requirements package" );

					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateRequirementsPkgPanel thePanel = 
							new CreateRequirementsPkgPanel(
									theAppID );

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		}
	}

	public CreateRequirementsPkgPanel( 
			String theAppID ){
		
		super( theAppID );
		
		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		_rhpPrj = _rhpApp.activeProject();
		_ownerPkg = _rhpApp.activeProject();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );
		
		String theUniqueName = 
				GeneralHelpers.determineUniqueNameForPackageBasedOn(
						StereotypeAndPropertySettings.getDefaultUseCasePackageName( _ownerPkg ),
						_ownerPkg );

		JPanel theReqtsAnalysisPanel = createContent( theUniqueName );
		theReqtsAnalysisPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
		
		String introText = 
	    		"This helper will create a package hierarchy for requirements underneath the " + 
	    				Logger.elementInfo( _ownerPkg ) + ". \n";
		
		JPanel theStartPanel = new JPanel();
		
		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
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
				
		_createRequirementsPkgChooser = new CreateRequirementsPkgChooser( 
				_ownerPkg, 
				theName );
				
		theColumn1ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );   

		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

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
	
	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {
		
		if( checkValidity( false ) ){
			
//			CreateRequirementsPkg m_CreateRequirementsPkgChooser.performActionAndReturnRequirementsPkg( 
//					m_OwnerPkg );
			
			PopulatePkg.deleteIfPresent( "Structure1", "StructureDiagram", _rhpPrj );
			PopulatePkg.deleteIfPresent( "Model1", "ObjectModelDiagram", _rhpPrj );
			PopulatePkg.deleteIfPresent( "Default", "Package", _rhpPrj );
			
			AutoPackageDiagram theAPD = new AutoPackageDiagram( _rhpPrj );
			theAPD.drawDiagram();
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