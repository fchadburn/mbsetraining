package generalhelpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.telelogic.rhapsody.core.*;

public abstract class CreateStructuralElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected CreateStructuralElementPanel(){	
		super();
	}
	
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
					Logger.writeLine("Unhandled exception in createOKCancelPanel->theOKButton.actionPerformed");
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
	
	protected static void addGeneralization(
			IRPClassifier fromElement, 
			String toBlockWithName, 
			IRPPackage underneathTheRootPackage){
		
		IRPModelElement theBlock = 
				underneathTheRootPackage.findNestedElementRecursive( toBlockWithName, "Block" );
		
		if (theBlock != null){
			fromElement.addGeneralization( (IRPClassifier) theBlock );
		} else {
			Logger.writeLine("Error: Unable to find element with name " + toBlockWithName);
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #106 03-NOV-2016: Ease usage by renaming UsageDomain block to SystemAssembly and moving up one package (F.J.Chadburn)
    
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