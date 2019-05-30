package functionaldesignplugin;

import functionalanalysisplugin.RhapsodyComboBox;
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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
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

public class CreateFunctionalDesignSpecificationPackage extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IRPPackage m_OwnerPkg;
	private IRPApplication m_RhpApp;
	private IRPProject m_RhpPrj;

	final private String m_FullNameDefault = "<Enter Full Name>";
	final private String m_BlockDescriptionDefault = "<Enter Description for FDS Block>";
	final private String m_ShortNameDefault = "<Enter Short Name>";
	final private String m_FunctionNameDefault = "<Enter Function Name>";
	final private String m_FunctionBlockDescriptionDefault = "<Enter text describing what core function is>";

	private JTextField m_FullNameTextField = new JTextField( m_FullNameDefault );
	private JTextField m_BlockDescriptionTextField = new JTextField( m_BlockDescriptionDefault );
	private JTextField m_ShortNameTextField = new JTextField( m_ShortNameDefault );
	private JTextField m_FunctionNameTextField = new JTextField( m_FunctionNameDefault );
	private JTextField m_FunctionBlockDescriptionTextField = new JTextField( m_FunctionBlockDescriptionDefault );
	private JCheckBox m_CreateParametricCheckBox = new JCheckBox( "Create a parametric sub-package?" );
 
	private JLabel m_FullNameLabel = new JLabel( "Enter Full Name" );
	private JLabel m_BlockDescriptionLabel = new JLabel( "Enter Description for FDS Block" );
	private JLabel m_ShortNameLabel = new JLabel( "Enter Short Name" );
	private JLabel m_FunctionNameLabel = new JLabel( "Enter Top-Level Function Name" );
	private JLabel m_FunctionBlockDescriptionLabel = new JLabel( "Enter text describing what core function is" );
	private JLabel m_RootPackageStereotypeLabel = new JLabel( "Enter the type of package" );

	protected RhapsodyComboBox m_ChosenStereotype;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConfigurationSettings theConfigSettings = new ConfigurationSettings(
				"FunctionalDesign.properties", 
				"FunctionalDesign_MessagesBundle" );
		
		launchTheDialog( theConfigSettings );		
	}
	
	public static void launchTheDialog( 
			ConfigurationSettings theConfigSettings ){
		
		UserInterfaceHelpers.setLookAndFeel();
			
		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();
		
		if( theAppID != null ){
			
			ProfileVersionManager.checkAndSetProfileVersion( false, theConfigSettings, true );

			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					JFrame.setDefaultLookAndFeelDecorated( true );

					JFrame frame = new JFrame( "Populate functional design package structure" );

					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateFunctionalDesignSpecificationPackage thePanel = 
							new CreateFunctionalDesignSpecificationPackage(
									theAppID );

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		}
	}

	public CreateFunctionalDesignSpecificationPackage( 
			String theAppID ){
		
		super( theAppID );
		
		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );
				
		m_RhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		m_RhpPrj = m_RhpApp.activeProject();
		
		notifyReadWriteNeededFor( m_RhpPrj );
		
		if( !getLockedUnits().isEmpty() ){
				
			String introText = 
		    		"Sorry, this helper can't run as it doesn't have the necessary read/write access.";
			
			JPanel theStartPanel = new JPanel();
			
			theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
			theStartPanel.add( createPanelWithTextCentered( introText ) );
			
			add( theStartPanel, BorderLayout.PAGE_START );
			add( createOKCancelPanel(), BorderLayout.PAGE_END );	
			
		} else {
			m_OwnerPkg = m_RhpApp.activeProject();

			JPanel theCentrePanel = createContent();
			theCentrePanel.setAlignmentX( Component.LEFT_ALIGNMENT );
			
			String introText = 
		    		"This helper will create a " + 
		    		"package hierarchy underneath the " + Logger.elementInfo( m_OwnerPkg ) + ". \n" +
		    		"It creates a nested package structure including initial diagrams, and sets default \n" +
		    		"display and other options to appropriate values for this using Rhapsody profile and property settings.\n";
			
			JPanel theStartPanel = new JPanel();
			
			theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
			theStartPanel.add( createPanelWithTextCentered( introText ) );
			
			add( theStartPanel, BorderLayout.PAGE_START );
			add( theCentrePanel, BorderLayout.CENTER );
			add( createOKCancelPanel(), BorderLayout.PAGE_END );		
		}
	}
	
	private JPanel createContent(){
		
		JPanel thePanel = new JPanel();

		List<IRPModelElement> theStereotypes = 
				StereotypeAndPropertySettings.getStereotypesForFunctionalDesignRootPackage( 
						m_RhpPrj );

		m_ChosenStereotype = new RhapsodyComboBox( theStereotypes, false );
		m_ChosenStereotype.setMaximumSize( new Dimension( 330, 20 ) );
		
		if( theStereotypes.size() > 0 ){
			// set to first value in list
			m_ChosenStereotype.setSelectedRhapsodyItem( theStereotypes.get( 0 ) );	
			Logger.writeLine("Setting default stereotype to " + Logger.elementInfo(theStereotypes.get( 0 )));
		}
		
		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		
		theColumn1ParallelGroup.addComponent( m_RootPackageStereotypeLabel );    
		theColumn1ParallelGroup.addComponent( m_FullNameLabel );    
		theColumn1ParallelGroup.addComponent( m_ShortNameLabel ); 
		theColumn1ParallelGroup.addComponent( m_BlockDescriptionLabel ); 
		theColumn1ParallelGroup.addComponent( m_FunctionNameLabel ); 
		theColumn1ParallelGroup.addComponent( m_FunctionBlockDescriptionLabel ); 

		theColumn2ParallelGroup.addComponent( m_ChosenStereotype );  
		theColumn2ParallelGroup.addComponent( m_FullNameTextField );   
		theColumn2ParallelGroup.addComponent( m_ShortNameTextField );  
		theColumn2ParallelGroup.addComponent( m_BlockDescriptionTextField );   
		theColumn2ParallelGroup.addComponent( m_FunctionNameTextField );
		theColumn2ParallelGroup.addComponent( m_FunctionBlockDescriptionTextField );  
		theColumn2ParallelGroup.addComponent( m_CreateParametricCheckBox ); 
		
		m_CreateParametricCheckBox.setSelected( 
				StereotypeAndPropertySettings.
					getIsCreateParametricSubpackageSelected( m_RhpPrj ) );

		m_ShortNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}	
				});
		
		m_FullNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}	
				});
		
		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical1ParallelGroup.addComponent( m_RootPackageStereotypeLabel );
		theVertical1ParallelGroup.addComponent( m_ChosenStereotype );
		
		ParallelGroup theVertical2ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical2ParallelGroup.addComponent( m_FullNameLabel );
		theVertical2ParallelGroup.addComponent( m_FullNameTextField );

		ParallelGroup theVertical3ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical3ParallelGroup.addComponent( m_ShortNameLabel );
		theVertical3ParallelGroup.addComponent( m_ShortNameTextField );

		ParallelGroup theVertical4ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical4ParallelGroup.addComponent( m_BlockDescriptionLabel );
		theVertical4ParallelGroup.addComponent( m_BlockDescriptionTextField );
		
		ParallelGroup theVertical5ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical5ParallelGroup.addComponent( m_FunctionNameLabel );
		theVertical5ParallelGroup.addComponent( m_FunctionNameTextField );
		
		ParallelGroup theVertical6ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical6ParallelGroup.addComponent( m_FunctionBlockDescriptionLabel );
		theVertical6ParallelGroup.addComponent( m_FunctionBlockDescriptionTextField );

		ParallelGroup theVertical7ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical7ParallelGroup.addComponent( m_CreateParametricCheckBox );
		
		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical3ParallelGroup );			
		theVerticalSequenceGroup.addGroup( theVertical4ParallelGroup );			
		theVerticalSequenceGroup.addGroup( theVertical5ParallelGroup );			
		theVerticalSequenceGroup.addGroup( theVertical6ParallelGroup );			
		theVerticalSequenceGroup.addGroup( theVertical7ParallelGroup );		
		
		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );
		
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		boolean isValid = true;
		String errorMsg = "";
		
		if( m_OwnerPkg == null ){
			errorMsg += "Sorry, the helper doesn't have necessary read/write access to continue";
		} else {
			String theRegEx = m_OwnerPkg.getPropertyValue( 
					"General.Model.NamesRegExp" );
			
			String theFullName = m_FullNameTextField.getText();
			String theShortName = m_ShortNameTextField.getText();
			String theFunctionName = m_FunctionNameTextField.getText();			
					
			if( theFullName.isEmpty() ||
				theFullName.equals( m_FullNameDefault )){
				
				errorMsg += "Please enter a valid name for the Full Name.\n";
				isValid = false;
				
			} else if( theFullName != null && !theFullName.matches( theRegEx ) ){
				
				errorMsg += "Sorry, " + theFullName + 
						" is not valid name (NamesRegExp = " + theRegEx + ")\n";
				isValid = false;

			} else if( m_RhpPrj.findNestedElementRecursive( 
					theFullName, "Class" ) != null ){

				errorMsg += "Sorry, " + theFullName + 
						" is not unique, there is already a \n" +
						Logger.elementInfo( m_RhpPrj.findNestedElementRecursive(
								theFullName, "Class" ) )+
						", please enter a unique name.\n";
				
				isValid = false;
				
			} else if( m_RhpPrj.findNestedElementRecursive(
					theFullName + "Pkg", "Package" ) != null ){
				
				errorMsg += "Sorry, " + theFullName + "Pkg" +  
						" is not unique, there is already a \n" +
						Logger.elementInfo( m_RhpPrj.findNestedElementRecursive(
								theFullName + "Pkg", "Package" ) )+
						", please enter a unique name.\n";
				isValid = false;
			}

			if( theShortName.isEmpty() ||
					theShortName.equals( m_ShortNameDefault )){

				errorMsg += "Please enter a valid name for the Short Name.\n";
				isValid = false;

			} else if( theShortName != null && !theShortName.matches( theRegEx ) ){

				errorMsg += "Sorry, " + theShortName + 
						" is not valid name (NamesRegExp = " + theRegEx + ")\n";

				isValid = false;

			} else if( m_RhpPrj.findNestedElementRecursive( 
					theShortName, "Class" ) != null ){

				errorMsg += "Sorry, " + theShortName + 
						" is not unique, there is already a \n" +
						Logger.elementInfo( m_RhpPrj.findNestedElementRecursive(
								theShortName, "Class" ) )+
						", please enter a unique name.\n";

				isValid = false;
			}
			
			if( theFunctionName.isEmpty() ||
					theFunctionName.equals( m_FunctionNameDefault )){

				errorMsg += "Please enter a valid name for the Function Name.\n";
				isValid = false;

			} else if( theFunctionName != null && !theFunctionName.matches( theRegEx ) ){

				errorMsg += "Sorry, " + theShortName + 
						" is not valid name (NamesRegExp = " + theRegEx + ")\n";

				isValid = false;

			} else if( m_RhpPrj.findNestedElementRecursive( 
					theFunctionName, "Class" ) != null ){

				errorMsg += "Sorry, " + theFunctionName + 
						" is not unique, there is already a \n" +
						Logger.elementInfo( m_RhpPrj.findNestedElementRecursive(
								theFunctionName, "Class" ) )+
						", please enter a unique name.\n";
				
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
		
		if( m_OwnerPkg != null && checkValidity( false ) ){
						
			List<IRPActor> theMasterActors = 
					StereotypeAndPropertySettings.getMasterActorList( 
							m_RhpPrj );
			
			DesignSpecificationPackage thePackage =
					new DesignSpecificationPackage(
							m_OwnerPkg,
							theMasterActors,
							m_FullNameTextField.getText(), 
							m_ChosenStereotype.getSelectedRhapsodyItem().getName(), 
							m_ShortNameTextField.getText(), 
							m_BlockDescriptionTextField.getText(), 
							m_FunctionNameTextField.getText(), 
							m_FunctionBlockDescriptionTextField.getText(), 
							m_CreateParametricCheckBox.isSelected() );
			
			thePackage.createPackage();
			thePackage.openSystemContextDiagram();
			
			GeneralHelpers.cleanUpModelRemnants( m_RhpPrj );

	    	m_RhpPrj.save();
		}
	}

	private void updateRelatedElementNames(){
		
		if( m_FullNameTextField.getText().contains(" ") ){
			
			m_FunctionNameTextField.setText(
					"Manage " + m_FullNameTextField.getText() );
		} else {
			m_FunctionNameTextField.setText(
					"Manage" + m_FullNameTextField.getText() );
		}
		
		m_BlockDescriptionTextField.setText(
				"Functional subsystem for " + m_FullNameTextField.getText() );
		
		m_FunctionBlockDescriptionTextField.setText(
				"Manages " + m_FullNameTextField.getText() );
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #250 29-MAY-2019: First official version of new FunctionalDesignProfile  (F.J.Chadburn)

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