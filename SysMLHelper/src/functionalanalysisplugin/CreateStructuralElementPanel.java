package functionalanalysisplugin;

import generalhelpers.Logger;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPInstance;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;

public abstract class CreateStructuralElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	CreateStructuralElementPanel(){	
		super();
	}
	
	// implementation specific provided by parent
	abstract boolean checkValidity(boolean isMessageEnabled);
	
	// implementation specific provided by parent
	abstract void performAction();
	
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
	
	protected static IRPInstance addPartTo(
			IRPClassifier theElement, 
			IRPClassifier typedByElement){
		
		IRPInstance thePart = (IRPInstance) theElement.addNewAggr("Part", "its" + typedByElement.getName());
		thePart.setOtherClass(typedByElement);
		
		return thePart;
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
	
	protected static boolean isLegalName(String theName){
		
		String regEx = "^(([a-zA-Z_][a-zA-Z0-9_]*)|(operator.+))$";
		
		boolean isLegal = theName.matches( regEx );
		
		if (!isLegal){
			Logger.writeLine("Warning, detected that " + theName 
					+ " is not a legal name as it does not conform to the regex=" + regEx);
		}
		
		return isLegal;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    
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