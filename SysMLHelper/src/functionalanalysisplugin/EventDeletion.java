package functionalanalysisplugin;

import generalhelpers.Logger;

import java.util.List;

import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;

public class EventDeletion {

	public static void deleteEventAndRelatedElementsFor(List<IRPModelElement> theEls){
		
		ModelElementList justEvents = new ModelElementList();
		
		for (IRPModelElement theEl : theEls) {
			
			if (theEl instanceof IRPEventReception){
				
				// add the events related to event receptions to the list
				IRPEventReception theEventReception = (IRPEventReception) theEl;
				justEvents.add( theEventReception.getEvent() );
				
			} else if (theEl instanceof IRPEvent){
				
				// if an element is not an event or event reception then remove it
				justEvents.add( theEl );
			}
		}
		
		justEvents.removeDuplicates();
		
		int numberOfEventsToDelete = justEvents.size();
		
		if (numberOfEventsToDelete > 1){
			
			String deleteMessage = "Do you want to delete the following " + numberOfEventsToDelete + " Events from the\n"
					+ "model together with related usages and receptions?\n\n";
			
			for (IRPModelElement theEl : justEvents) {
				deleteMessage += Logger.elementInfo(theEl) + "\n";
			}
			
			deleteMessage += "\n";
			
			int chosenOption = JOptionPane.showConfirmDialog(
					null, deleteMessage, "Confirm", JOptionPane.YES_NO_OPTION);
			
			if (chosenOption == JOptionPane.YES_OPTION){
				
				for (IRPModelElement theEl : justEvents) {
					IRPEvent theEventToDelete = (IRPEvent) theEl;			
					deleteEventAndRelatedElementsFor(theEventToDelete, false);
					
					
				}
			}

		} else if (numberOfEventsToDelete == 1){
			
			IRPEvent theEventToDelete = (IRPEvent) justEvents.get(0);
			deleteEventAndRelatedElementsFor( theEventToDelete, true );
		} else {
			Logger.writeLine("Unexpected error in deleteEventAndRelatedElementsFor, there were no events in the selected list" );
		}
		
		RhapsodyAppServer.getActiveRhapsodyApplication().refreshAllViews();
		

	}
	
	private static void deleteTheList(ModelElementList theList){
		
		ModelElementList theTransitions = theList.getListFilteredBy("Transition");
		theTransitions.deleteFromProject();

		ModelElementList theMessages = theList.getListFilteredBy("Message");
		theMessages.deleteFromProject();
		
		ModelElementList theEventReceptions = theList.getListFilteredBy("EventReception");
		theEventReceptions.deleteFromProject();
		
		ModelElementList theEvents = theList.getListFilteredBy("Event");
		theEvents.deleteFromProject();
	}
	
	private static void deleteEventAndRelatedElementsFor(IRPEvent theEvent, boolean withPrompt){
	
		ModelElementList toDeleteList = new ModelElementList();
	
		boolean isUnexpectedElement = false;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theRefs = theEvent.getReferences().toList();
		
		for (IRPModelElement theRef : theRefs) {
			if ( theRef instanceof IRPEventReception ){
				Logger.writeLine(theRef, "was added to list to delete");
				toDeleteList.add( theRef );
			} else if ( theRef instanceof IRPTransition ){
				Logger.writeLine(theRef, "was added to list to delete");
				toDeleteList.add( theRef );
			} else if ( theRef instanceof IRPMessage ){
				Logger.writeLine(theRef, "was added to list to delete");
				toDeleteList.add( theRef );
			} else {
				Logger.writeLine("Error in deleteEventAndRelatedElementsFor, as " + Logger.elementInfo(theRef) + " was not considered");
				isUnexpectedElement = true;
			}
		}
		if (!isUnexpectedElement){
			
			if (!withPrompt){
				deleteTheList(toDeleteList);
				
				Logger.writeLine("Deleting " + Logger.elementInfo(theEvent) + " from the project");
				theEvent.deleteFromProject();
				
			} else { // withPrompt
				
				String deleteMessage = "";
				
				if (toDeleteList.isEmpty()){
					deleteMessage = "Do you want to delete " + Logger.elementInfo( theEvent ) + "\n";
				} else {
					deleteMessage = "Do you want to delete " + Logger.elementInfo( theEvent ) + " and its following references:\n";
				}
				
				for (IRPModelElement theEl : toDeleteList) {
					deleteMessage += Logger.elementInfo(theEl) + "\n";
				}
				
				int chosenOption = JOptionPane.showConfirmDialog(
						null, deleteMessage, "Confirm", JOptionPane.YES_NO_OPTION);
				
				if (chosenOption == JOptionPane.YES_OPTION){
				
					deleteTheList(toDeleteList);
					
					Logger.writeLine("Deleting " + Logger.elementInfo(theEvent) + " from the project");
					theEvent.deleteFromProject();					
				}
			}		
		} else {
			
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    
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
