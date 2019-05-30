package requirementsanalysisplugin;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.telelogic.rhapsody.core.*;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

public class SmartLinkPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SmartLinkInfo m_SmartLinkInfo;
	private JCheckBox m_PopulateOnDiagramCheckBox; 
	
	private static List<IRPModelElement> m_StartLinkEls;
	private static List<IRPGraphElement> m_StartLinkGraphEls;
	
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		@SuppressWarnings("unused")
		IRPProject theRhpPrj = theRhpApp.activeProject();

		@SuppressWarnings("unused")
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = 
		theRhpApp.getListOfSelectedElements().toList();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = 
		theRhpApp.getSelectedGraphElements().toList();
		
		selectStartLinkEls( theSelectedEls, theSelectedGraphEls );
	}
	
	public static void selectStartLinkEls(
			final List<IRPModelElement> theStartLinkEls,
			final List<IRPGraphElement> theStartLinkGraphEls ){
		
		if( !theStartLinkEls.isEmpty() ){
			
			Logger.info( "The following " + theStartLinkEls.size() + 
					" elements were selected in Start Link command:" );

			for( IRPModelElement theStartLinkEl : theStartLinkEls ){
				Logger.info( Logger.elementInfo( theStartLinkEl ) ); 
			}
			
			m_StartLinkEls = theStartLinkEls;
			m_StartLinkGraphEls = theStartLinkGraphEls;
			
		} else {
			Logger.error( "Error in SmartLinkPanel.launchTheStartLinkPanel, " +
					"no elements were selected" );
		}
	}
	
	public static void launchTheEndLinkPanel(
			final List<IRPModelElement> theEndLinkEls,
			final List<IRPGraphElement> theEndLinkGraphEls){
		
		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();
		
		if( m_StartLinkEls == null || m_StartLinkEls.isEmpty() ){
			
			UserInterfaceHelpers.showWarningDialog("You need to Start a link before you can end it");
		} else {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );

					String theCaption;
					
					if( theEndLinkEls.size()==1 ){
						theCaption = "End Link invoked on " + 
								Logger.elementInfo( theEndLinkEls.get( 0 ) );
					} else {
						theCaption = "End Link invoked for " + 
								theEndLinkEls.size() + " selected elements";
					}
					
					JFrame frame = new JFrame( theCaption );
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					SmartLinkPanel thePanel = 
							new SmartLinkPanel( 
									theAppID,
									m_StartLinkEls, 
									m_StartLinkGraphEls, 
									theEndLinkEls, 
									theEndLinkGraphEls );

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		}
	}
	
	public SmartLinkPanel(
		String theAppID,
		List<IRPModelElement> theStartLinkEls,
		List<IRPGraphElement> theStartLinkGraphEls,
		List<IRPModelElement> theEndLinkEls,
		List<IRPGraphElement> theEndLinkGraphEls ){
		
		super( theAppID );
		
		boolean isCyclical = false;
		
		for( IRPModelElement theStartLinkEl : theStartLinkEls ){
			
			if( theEndLinkEls.contains( theStartLinkEl ) ){
				isCyclical = true;
				break;
			}
		}
		
		m_PopulateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
		m_PopulateOnDiagramCheckBox.setSelected( false );
		m_PopulateOnDiagramCheckBox.setVisible( false );
		
		if( isCyclical ){

			add( new JLabel( "Unable to proceed as you've selected cyclical start and end elements" ), BorderLayout.PAGE_START );

		} else {
			m_SmartLinkInfo = new SmartLinkInfo(
					theStartLinkEls, theStartLinkGraphEls, theEndLinkEls, theEndLinkGraphEls);
			
			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
			
			add( new JLabel( m_SmartLinkInfo.getDescriptionHTML() ), BorderLayout.PAGE_START );
				
			if( m_SmartLinkInfo.getIsPopulatePossible() ){
				
				m_PopulateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
				m_PopulateOnDiagramCheckBox.setSelected( true );
				m_PopulateOnDiagramCheckBox.setVisible( true );
				
				add( m_PopulateOnDiagramCheckBox, BorderLayout.CENTER );
			}
				
			//add( thePageStartPanel );//, BorderLayout.PAGE_START );
		}
		

		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {

		String errorMsg = "";
		
		boolean isValid = true;

		if (isMessageEnabled && !isValid && errorMsg != null){
			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {
		try {
			// do silent check first
			if( checkValidity( false ) ){
				
				if( m_SmartLinkInfo.getAreNewRelationsNeeded() || 
					m_SmartLinkInfo.getIsPopulatePossible() ){
					
					m_SmartLinkInfo.createDependencies( 
							m_PopulateOnDiagramCheckBox.isSelected() );
				}
				
			} else {
				Logger.error( "Error in SmartLinkPanel.performAction, " +
						"checkValidity returned false" );
			}	
		} catch (Exception e) {
			Logger.error( "Error, unhandled exception detected in SmartLinkPanel.performAction" );
		}	
	}
}

/**
 * Copyright (C) 2017-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
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
