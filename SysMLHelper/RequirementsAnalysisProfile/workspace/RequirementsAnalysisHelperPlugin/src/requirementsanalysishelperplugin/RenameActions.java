/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited

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

package requirementsanalysishelperplugin;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;
 
public class RenameActions {

	static IRPApplication m_rhpApplication = null;
	
	static public void performRenamesFor(List<IRPModelElement> theSelectedEls){
		
		List<IRPActivityDiagram> theADs = GeneralHelpers.buildListOfActivityDiagramsFor(theSelectedEls);
		
		Logger.writeLine("There are " + theADs.size() + " Activity Diagrams nested under the selected list");
		
		for (IRPActivityDiagram theAD : theADs) {
			
			IRPFlowchart theFC = (IRPFlowchart) theAD.getOwner();
			Logger.writeLine("Rename actions invoked for " + Logger.elementInfo( theFC ));
			
			ActionList actionsInfos = new ActionList( theAD );		
					
			if (actionsInfos.isRenamingNeeded()){

				JDialog.setDefaultLookAndFeelDecorated(true);

				String theMsg = "The checker has detected that " + actionsInfos.getNumberOfRenamesNeeded() + 
						" elements require renaming. Do you want to rename them?";

				int response = JOptionPane.showConfirmDialog(null, 
						theMsg, "Rename for " + Logger.elementInfo(theFC),
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (response == JOptionPane.CANCEL_OPTION){
					Logger.writeLine("Operation was cancelled by user with no changes made.");
				} else {
					if (response == JOptionPane.YES_OPTION) {
						actionsInfos.performRenames();
					} else if (response == JOptionPane.NO_OPTION){
						Logger.writeLine("Info: User chose not rename the actions.");
					} 
				}

			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				String theMsg = "No action necessary. The checker has checked " + actionsInfos.size() + 
						" elements on the diagram.";
				
				JOptionPane.showMessageDialog(null, theMsg, "Rename for " + Logger.elementInfo(theFC), JOptionPane.INFORMATION_MESSAGE);
			}
			
			Logger.writeLine("Rename actions has finished (" + actionsInfos.getNumberOfRenamesNeeded() + " out of " + actionsInfos.size() + " elements required renaming)");
		}
	}
}

