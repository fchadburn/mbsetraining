package generalhelpers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.SelectedElementContext;

public abstract class CreateStructuralElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected SelectedElementContext m_ElementContext;

	protected CreateStructuralElementPanel(
			String theAppID ){
		
		super();
		m_ElementContext = new SelectedElementContext( theAppID );
	}
	
	List<IRPUnit> m_UnitsForReadWrite = new ArrayList<IRPUnit>();
	
	// implementation specific provided by parent
	protected abstract boolean checkValidity(boolean isMessageEnabled);
	
	// implementation specific provided by parent
	protected abstract void performAction();
	
	public JPanel createOKCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton("OK");
		theOKButton.setPreferredSize(new Dimension(75,25));
		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if (isValid){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}
												
				} catch (Exception e2) {
					Logger.writeLine("Unhandled exception in createOKCancelPanel->theOKButton.actionPerformed e2=" + e2.getMessage());
				}
			}
		});
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize( new Dimension( 75,25 ) );	
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					Logger.writeLine("Unhandled exception in createOKCancelPanel->theCancelButton.actionPerformed");
				}		
			}	
		});
		
		thePanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	protected Component createPanelWithTextCentered(
			String theText){
		
		JTextPane theTextPane = new JTextPane();
		theTextPane.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		theTextPane.setBackground( new Color( 238, 238, 238 ) );
		theTextPane.setEditable( false );
		theTextPane.setText( theText );
		
		StyledDocument theStyledDoc = theTextPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment( center, StyleConstants.ALIGN_CENTER );

		theStyledDoc.setParagraphAttributes( 0, theStyledDoc.getLength(), center, false );

		JPanel thePanel = new JPanel();
		thePanel.add( theTextPane );
		
		return thePanel;
	}
	
	protected void notifyReadWriteNeededFor(
			IRPModelElement theEl ){
				
		IRPUnit theUnit = theEl.getSaveUnit();
		
		Logger.writeLine("notifyReadWriteNeededFor has determined that the Unit for " + Logger.elementInfo(theEl) + " is " + Logger.elementInfo(theUnit));
		
		if( !m_UnitsForReadWrite.contains( theUnit ) ){
			m_UnitsForReadWrite.add( theUnit );
		}
	}
	
	protected List<IRPUnit> getLockedUnits(){
	
		List<IRPUnit> theLockedUnits = new ArrayList<>();
		
		for( IRPUnit theUnit : m_UnitsForReadWrite ){
			
			int isReadyOnly = theUnit.isReadOnly();
			
//			Logger.writeLine("Checking " + Logger.elementInfo( theUnit ) + 
//					", isReadyOnly = " + isReadyOnly );
			
			if( isReadyOnly==1 ){
				theLockedUnits.add( theUnit );
			}
		}
		
//		Logger.writeLine("getLockedUnits detected that " + theLockedUnits.size() + 
//				" units out of " + m_UnitsForReadWrite.size() + " are locked");

		return theLockedUnits;
	}
	
	protected void buildUnableToRunDialog(
			String withMsg ){
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createPanelWithTextCentered( withMsg ) );

		add( thePageStartPanel, BorderLayout.PAGE_START );
		
		add( createCancelPanel(), BorderLayout.PAGE_END );
	}
	
	public JPanel createCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
				
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize(new Dimension(75,25));
		
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on Cancel button action listener");
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theCancelButton );
		
		return thePanel;
	}


}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #106 03-NOV-2016: Ease usage by renaming UsageDomain block to SystemAssembly and moving up one package (F.J.Chadburn)
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