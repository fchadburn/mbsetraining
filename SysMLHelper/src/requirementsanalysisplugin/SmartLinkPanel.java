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
	
	public static void launchTheStartLinkPanel(
			final List<IRPModelElement> theStartLinkEls,
			final List<IRPGraphElement> theStartLinkGraphEls ){
		
		if( !theStartLinkEls.isEmpty() ){
			
			Logger.writeLine("The following " + theStartLinkEls.size() + " elements were selected in Start Link command:");

			for( IRPModelElement theStartLinkEl : theStartLinkEls ){
				Logger.writeLine( Logger.elementInfo( theStartLinkEl ) ); 
			}
			
			m_StartLinkEls = theStartLinkEls;
			m_StartLinkGraphEls = theStartLinkGraphEls;
			
		} else {
			Logger.writeLine("Error in SmartLinkPanel.launchTheStartLinkPanel, no elements were selected");
		}
		
	}
	
	public static void launchTheEndLinkPanel(
			final List<IRPModelElement> theEndLinkEls,
			final List<IRPGraphElement> theEndLinkGraphEls){
		
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
		List<IRPModelElement> theStartLinkEls,
		List<IRPGraphElement> theStartLinkGraphEls,
		List<IRPModelElement> theEndLinkEls,
		List<IRPGraphElement> theEndLinkGraphEls ){
		
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
				Logger.writeLine("Error in SmartLinkPanel.performAction, checkValidity returned false");
			}	
		} catch (Exception e) {
			Logger.writeLine("Error, unhandled exception detected in SmartLinkPanel.performAction");
		}	
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    
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
