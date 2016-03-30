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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;
 
public class MoveRequirements {

	static IRPApplication m_rhpApplication = null;
	
	public static void main(String[] args) {
	}
	
	public static Set<IRPModelElement> buildSetOfUnclaimedRequirementsBasedOn(
			List<IRPModelElement> theSelectedEls, String theGatewayStereotypeName) {
		
		
		Set<IRPModelElement> theUnclaimedReqts = new HashSet<IRPModelElement>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			IRPModelElement theElementToSearchUnder = theSelectedEl;
			
			if( theSelectedEl instanceof IRPActivityDiagram ){	
				theElementToSearchUnder = theElementToSearchUnder.getOwner();
			}
			
			List<IRPModelElement> theReqtsToAdd = 
					findModelElementsWithoutStereotypeNestedUnder( 
							theElementToSearchUnder, "Requirement", theGatewayStereotypeName );
			
			theUnclaimedReqts.addAll( theReqtsToAdd );
		
		}

		return theUnclaimedReqts;
	}
	
	
	public static void moveUnclaimedRequirementsReadyForGatewaySync(
			List<IRPModelElement> theSelectedEls, IRPProject theProject){
		
		String theGatewayStereotypeName = "from.*";
		
		Set<IRPModelElement> theUnclaimedReqts = 
				buildSetOfUnclaimedRequirementsBasedOn( theSelectedEls, theGatewayStereotypeName );
		
		Logger.writeLine(theUnclaimedReqts.size() + " requirements unclaimed by the Gateway were found");
		
		if (theUnclaimedReqts.isEmpty()){
			
			String theMsg = "Nothing to do as there were no unclaimed requirements found";
		
			JOptionPane.showMessageDialog(null, theMsg);
			
			Logger.writeLine("Nothing to do as there were no unclaimed requirements were found");
			
		} else {
			
			List<IRPModelElement> thePackages;
			
			thePackages = findModelElementsNestedUnder( theProject, "Package", theGatewayStereotypeName);
			
			Object[] options = new Object[thePackages.size()];
			
			for (int i = 0; i < options.length; i++) {
				String theOptionName =  thePackages.get(i).getName() + " in " + thePackages.get(i).getOwner().getFullPathName();
				options[i] = theOptionName;
			}
			
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			if (thePackages.isEmpty()){
				
				String theMsg = "Nothing to do as no Gateway imported packages were found.\n"
						      + "Recommendation is to first Add high-level requirements to\n" 
						      + "the model using the Gateway to create the package(s).";
				
				Logger.writeLine(theMsg);
				JOptionPane.showMessageDialog(null, theMsg);

			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				Object theChoice = JOptionPane.showInputDialog(
						null,
						"Which package do you want to move the " + theUnclaimedReqts.size() + " unclaimed requirement(s) to?",
						"Based on " + theSelectedEls.size() + " selected elements",
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);
				
				if (theChoice == null){
					
					Logger.writeLine("Operation was cancelled by user with no changes made.");
					
				} else {			
					
					IRPModelElement thePackage = theProject.findElementsByFullName(theChoice.toString(), "Package");
					
					IRPStereotype theStereotypeToApply = getStereotypeAppliedTo(thePackage, theGatewayStereotypeName);
					
					int dialogResult = JOptionPane.showConfirmDialog (
							null, "Would you like to move the " + theUnclaimedReqts.size() + " unclaimed requirements into the Package \n" 
							   + "called " + thePackage.getName() + " which has the Gateway imported stereotype \n" 
							   + "«" + theStereotypeToApply.getName() + "» applied? ",
							"Confirm", JOptionPane.YES_NO_OPTION);
					
					if( dialogResult == JOptionPane.YES_OPTION ){
						
						int count = 0;
						
						for (IRPModelElement theReqt : theUnclaimedReqts) {
							// check if already element of same name
							IRPModelElement alreadyExistingEl = thePackage.findNestedElement(theReqt.getName(), "Requirement");
							
							if (alreadyExistingEl != null){
								Logger.writeLine("Error: Unable to move " + Logger.elementInfo( theReqt ) 
										+ " as requirement of same name already exists under " 
										+ Logger.elementInfo(thePackage) + ". Consider renaming it.");
							} else {
								Logger.writeLine("Moving " + Logger.elementInfo( theReqt ) + " from " 
										+ Logger.elementInfo(theReqt.getOwner()) + " to " + Logger.elementInfo(thePackage) 
										+ " and applying " + Logger.elementInfo(theStereotypeToApply));
								theReqt.setOwner( thePackage );
								theReqt.addStereotype(theStereotypeToApply.getName(), "Requirement");
								count++;
								theReqt.highLightElement();
								
								applyStereotypeToDeriveReqtDependenciesOriginatingFrom( theReqt, theStereotypeToApply );
							}
						}
						
						Logger.writeLine("Finished (" + count + " requirements were moved out of " + theUnclaimedReqts.size() + ")");
						
					} else {
						Logger.writeLine("Cancelled due to user choice not to continue with the move.");
					}
				}			
			}
		}
	}

	private static void applyStereotypeToDeriveReqtDependenciesOriginatingFrom( IRPModelElement theReqt, IRPStereotype theStereotypeToApply ) {
		
		Logger.writeLine(theReqt, "has been moved");
		
		List<IRPDependency> theDependencies = theReqt.getDependencies().toList();
		
		for (IRPDependency theDependency : theDependencies) {
			
			Logger.writeLine(theDependency,"was found");
			
			IRPStereotype theExistingGatewayStereotype = GeneralHelpers.getStereotypeAppliedTo(theDependency, "from.*");
			
			//Logger.writeLine(theExistingGatewayStereotype, "is the existing gateway stereotype");
			
			if (theExistingGatewayStereotype == null && GeneralHelpers.hasStereotypeCalled("deriveReqt", theDependency)){
							
				Logger.writeLine("Applying " + Logger.elementInfo(theStereotypeToApply) + " to " + Logger.elementInfo(theDependency));
				theDependency.setStereotype(theStereotypeToApply);
				theDependency.changeTo("Derive Requirement");
			}
		}
	}
	public static IRPStereotype getStereotypeAppliedTo(IRPModelElement theElement, String thatMatchesRegEx){
		
		IRPStereotype foundStereotype = null;
		
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
	
	public static boolean checkIsNestedUnderAProfile(IRPModelElement theElementToCheck){
		
		boolean isUnderAProfile = false;
		
		IRPModelElement theOwner = theElementToCheck.getOwner();
		
		if (theOwner!=null){
			
			if (theOwner instanceof IRPProfile){
				isUnderAProfile = true;
			} else {
				isUnderAProfile = checkIsNestedUnderAProfile( theOwner );
			}
		}
		
		return isUnderAProfile;
	}
	
	public static List<IRPModelElement> findModelElementsNestedUnder(IRPModelElement rootEl, String ofMetaClass, String withStereotypeMatchingRegEx){
		
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();
		List<IRPModelElement> theFound = new ArrayList<IRPModelElement>();
		
		for (IRPModelElement theEl : theCandidateEls) {
			
			IRPStereotype theStereotype = getStereotypeAppliedTo(theEl, withStereotypeMatchingRegEx);
			
			if (theStereotype!=null){
				// don't add if element is under the profile.
				if (!checkIsNestedUnderAProfile(theEl)){
					theFound.add(theEl);
				}
			}			
		}
		
		
		return theFound;
	}

	public static List<IRPModelElement> findModelElementsWithoutStereotypeNestedUnder(IRPModelElement rootEl, String ofMetaClass, String withStereotypeMatchingRegEx){
		
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();
		List<IRPModelElement> theFound = new ArrayList<IRPModelElement>();
		
		for (IRPModelElement theEl : theCandidateEls) {
			
			IRPStereotype theStereotype = getStereotypeAppliedTo(theEl, withStereotypeMatchingRegEx);
			
			if (theStereotype==null){
				theFound.add(theEl);
			}			
		}
		
		
		return theFound;
	}
}
