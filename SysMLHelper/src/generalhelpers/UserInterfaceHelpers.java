package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.IRPModelElement;

public class UserInterfaceHelpers {

	public static boolean askAQuestion(
			String question ){
		 
		JDialog.setDefaultLookAndFeelDecorated(true);
		 
		int answer = JOptionPane.showConfirmDialog(
				null, 
				question, 
				"Question?", 
				JOptionPane.YES_NO_OPTION);
		
		return (answer == JOptionPane.YES_OPTION);
	}
	
	public static void showWarningDialog(
			String theMsg ){
		
		Logger.writeLine("Warning: " + theMsg);
		
	    JDialog.setDefaultLookAndFeelDecorated( true );
	    
	    JOptionPane.showMessageDialog(
	    		null,  
	    		theMsg,
	    		"Warning",
	    		JOptionPane.WARNING_MESSAGE);	
	}
	
	public static void showInformationDialog(
			String theMsg ){
		
		Logger.writeLine("Information: " + theMsg);
		
	    JDialog.setDefaultLookAndFeelDecorated( true );
	    
	    JOptionPane.showMessageDialog(
	    		null,  
	    		theMsg,
	    		"Information",
	    		JOptionPane.INFORMATION_MESSAGE);	
	}
	
	public static IRPModelElement launchDialogToSelectElement(
			List<IRPModelElement> inList, 
			String messageToDisplay, 
			Boolean isFullPathRequested ){
		
		IRPModelElement theEl = null;
		
		List<String> nameList = new ArrayList<String>();
		
		for (int i = 0; i < inList.size(); i++) {
			if (isFullPathRequested){
				nameList.add(i, inList.get(i).getFullPathName());
			} else {
				nameList.add(i, inList.get(i).getName());
			}
		} 	
		
		Object[] options = nameList.toArray();
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		String selectedElementName = (String) JOptionPane.showInputDialog(
				null,
				messageToDisplay,
				"Input",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		
		if (selectedElementName != null){
			int index = nameList.indexOf(selectedElementName);
			theEl = inList.get(index);
		}
		
		return theEl;
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #003 09-APR-2016: Added double-click UC to open ACT (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #017 11-MAY-2016: Double-click now works with both nested and hyper-linked diagrams (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #173 02-APR-2017: cleanUpAutoRippleDependencies now gives an information rather than warning dialog (F.J.Chadburn)

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
