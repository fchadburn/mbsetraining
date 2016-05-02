package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;
 
public class GeneralHelpers {
 
	// for test only
	public static void dumpGraphicalPropertiesFor(IRPGraphElement theGraphEl){
	 
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		Logger.writeLine("---------------------------");
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			
			Logger.writeLine(theGraphicalProperty.getKey() + "=" + theGraphicalProperty.getValue());
		}
		Logger.writeLine("---------------------------"); 
	}
	
	public static String decapitalize(final String line){
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toLowerCase(line.charAt(0)) + line.substring(1);
		} else {
			theResult = line;
		}
		
		return theResult;	
	}
	
	public static String capitalize(final String line) {
		
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toUpperCase(line.charAt(0)) + line.substring(1);
		} else {
			Logger.writeLine("Error in capitalize");
			theResult = line;
		}
		
		return theResult;
	}
	public static String getActionTextFrom(IRPModelElement theEl) {
		
		String theSourceInfo = "Null";
		
		if (theEl instanceof IRPState){
			IRPState theState = (IRPState)theEl;
			String theStateType = theState.getStateType();
			
			if (theStateType.equals("Action")){
				theSourceInfo = theState.getEntryAction();
				
			} else if (theStateType.equals("AcceptEventAction")){ // receive event
				
				IRPAcceptEventAction theAcceptEventAction = (IRPAcceptEventAction)theEl;
				IRPEvent theEvent = theAcceptEventAction.getEvent();
				
				if (theEvent==null){
					Logger.writeLine("Event has no name so using Label");
					theSourceInfo = theState.getDisplayName();
				} else {
					theSourceInfo = theEvent.getName();
				}
				
			} else if (theStateType.equals("EventState")){ // send event
				
				IRPSendAction theSendAction = (IRPSendAction)theEl;
				IRPEvent theEvent = theSendAction.getEvent();
				
				if (theEvent==null){
					Logger.writeLine("Event has no name so using Label");
					theSourceInfo = theState.getDisplayName();
				} else {
					theSourceInfo = theEvent.getName();
				}		
			}		
			
		} else if (theEl instanceof IRPTransition){
			theSourceInfo = ((IRPTransition) theEl).getItsGuard().getBody();
		} else if (theEl instanceof IRPComment){
			theSourceInfo = theEl.getDescription();
		} else if (theEl instanceof IRPRequirement){
			IRPRequirement theReqt = (IRPRequirement)theEl;
			theSourceInfo = theReqt.getSpecification();
		}
		
		return theSourceInfo;
	}
	
	public static IRPModelElement launchDialogToSelectElement(
			List<IRPModelElement> inList, String messageToDisplay, Boolean isFullPathRequested){
		
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
		
		String selectedElementName = (String) JOptionPane.showInputDialog(
				null,
				messageToDisplay,
				"Input",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		
		int index = nameList.indexOf(selectedElementName);
		
		theEl = inList.get(index);
		
		Logger.writeLine(theEl, "was chosen");
		
		return theEl;
	}
	
	public static void applyExistingStereotype(String withTheName, IRPModelElement toTheEl){
		
		IRPStereotype theStereotype = 
				(IRPStereotype) toTheEl.getProject().findNestedElementRecursive(
						withTheName, "Stereotype");
		
		if (theStereotype != null){
			toTheEl.setStereotype(theStereotype);
		} else {
			Logger.writeLine("Warning: Unable to find a stereotype with name " + withTheName + " in applyExistingStereotype");
		}
	}
	
	public static IRPStereotype getStereotypeCalled(String theName, IRPModelElement onTheEl){
		
		int count = 0;
		IRPStereotype theFoundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();
		
		for (IRPStereotype theStereotype : theStereotypes) {
			if (theStereotype.getName().equals(theName)){
				
				theFoundStereotype = theStereotype;
				count++;
			}
		}
		
		if (count > 1){
			Logger.writeLine("Warning in getStereotypeCalled, found " + count 
					+ " elements that are called " + theName);
		}
		
		return theFoundStereotype;
	}
	
	public static Boolean hasStereotypeCalled(String theName, IRPModelElement onTheEl){
		
		Boolean isFound = false;
		
		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();
		
		for (IRPStereotype theStereotype : theStereotypes) {
			if (theStereotype.getName().equals(theName)){
				isFound = true;
			}
		}
		
		return isFound;
	}
	
	public static IRPModelElement findElementWithMetaClassAndName(
			String theMetaClass, String andName, IRPModelElement underneathTheEl){
		
		int count = 0;
		
		IRPModelElement theElement = null;
		
		@SuppressWarnings("unchecked")
		List <IRPModelElement> theCandidates = 
				underneathTheEl.getNestedElementsByMetaClass(theMetaClass, 1).toList();
		
		for (IRPModelElement theCandidate : theCandidates) {
			if (theCandidate.getName().equals( andName )){
				theElement = theCandidate;
				count++;
			}
		}
		
		if (count==0){
			Logger.writeLine("Warning in findElementWithMetaClassAndName(" + theMetaClass + "," + andName + ","+Logger.elementInfo(underneathTheEl)+"), no elements were found");
		} else if (count>1){
			Logger.writeLine("Warning in findElementWithMetaClassAndName(" + theMetaClass + "," + andName + ","+Logger.elementInfo(underneathTheEl)+"), " + count + " elements were found when I was expecting only one");
		}
		
		return theElement;
	}
	
	public static List<IRPModelElement> findElementsWithMetaClassAndStereotype(
			String theMetaClass, String andStereotype, IRPModelElement underneathTheEl){
		
		List <IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List <IRPModelElement> theCandidates = 
				underneathTheEl.getNestedElementsByMetaClass(theMetaClass, 1).toList();

		for (IRPModelElement theCandidate : theCandidates) {
			
			if (hasStereotypeCalled(andStereotype, theCandidate)){
				theFilteredList.add(theCandidate);
			}
		}
		
		return theFilteredList;
	}
	
	public static String promptUserForTextEntry(
			String withTitle, String andQuestion, String andDefault, int size){
		
		String theEntry = andDefault;
		
		JPanel panel = new JPanel();
		
		panel.add(new JLabel(andQuestion));
		
		JTextField theTextField = new JTextField(size);
		panel.add( theTextField );
		
		if (!andDefault.isEmpty())
			theTextField.setText(andDefault);
		
		int choice = JOptionPane.showConfirmDialog(null, panel, withTitle, JOptionPane.OK_CANCEL_OPTION);
		
		if( choice==JOptionPane.OK_OPTION ){
			String theTextEntered = theTextField.getText(); 
			
			if (!theTextEntered.isEmpty()){
				theEntry = theTextField.getText();
			} else {
				Logger.writeLine("No text was entered, using default response of '" + andDefault + "'");
			}
		}
		
		return theEntry;
	}
	
	public static IRPStereotype getStereotypeAppliedTo(IRPModelElement theElement, String thatMatchesRegEx){
		
		IRPStereotype foundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStereotype> theStereotypes = theElement.getStereotypes().toList();
		
		int count=0;
		
		for (IRPStereotype theStereotype : theStereotypes) {
			
			count++;
			
			String theName = theStereotype.getName();
			
			if (theName.matches(thatMatchesRegEx)){
				foundStereotype = theStereotype;
				
				if (count > 1){
					Logger.writeLine("Error in getStereotypeAppliedTo related to " + Logger.elementInfo(theElement) + " count=" + count);
				}
			}		
		}
		
		return foundStereotype;
	}
	
	public static boolean askQuestion(String question){
		
		boolean isYes = false;
		
		int answer = javax.swing.JOptionPane.showConfirmDialog(
				null, 
				question, 
				null, 
				javax.swing.JOptionPane.YES_NO_OPTION);
		
		if (answer == javax.swing.JOptionPane.YES_OPTION){
			isYes = true;
		}
		
		return isYes;
	}
	
	public static List<IRPActivityDiagram> buildListOfActivityDiagramsFor(
			List<IRPModelElement> theSelectedEls) {
		
		List<IRPActivityDiagram> theADs = new ArrayList<IRPActivityDiagram>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			@SuppressWarnings("unchecked")
			List<IRPActivityDiagram> theCandidates = theSelectedEl.getNestedElementsByMetaClass("ActivityDiagramGE", 1).toList();
			
			for (IRPActivityDiagram theCandidate : theCandidates) {
				if (!theADs.contains(theCandidate)){
					
					if (theCandidate.isReadOnly()==0){
						theADs.add(theCandidate);			
						Logger.writeLine("Adding " + Logger.elementInfo(theCandidate.getOwner()) + " to the list to check");
					} else {
						Logger.writeLine("Skipping " + Logger.elementInfo(theCandidate.getOwner()) + " as it is read-only");
					}
				}
			}
		}
		return theADs;
	}
	
	public static List<IRPModelElement> findElementsIn(List<IRPModelElement> theList, String withMetaClass){
		
		List<IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		for (IRPModelElement theEl : theList) {
			Logger.writeLine(theEl, "is in list");
			if (theEl.getMetaClass().equals(withMetaClass)){
				theFilteredList.add( theEl );
			}
		}
		
		return theFilteredList;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
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

