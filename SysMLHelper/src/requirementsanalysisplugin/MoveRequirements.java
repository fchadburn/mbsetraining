package requirementsanalysisplugin;

import executablembse.ExecutableMBSE_RPUserPlugin;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;
 
public class MoveRequirements {
	
	public static void main(String[] args) {
	
		IRPApplication theRhpApp = ExecutableMBSE_RPUserPlugin.getRhapsodyApp();
		IRPProject theRhpPrj = theRhpApp.activeProject();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = 
			theRhpApp.getListOfSelectedElements().toList();
		
		moveUnclaimedRequirementsReadyForGatewaySync(
				theSelectedEls, 
				theRhpPrj );
	}
	
	public static Set<IRPModelElement> buildSetOfUnclaimedRequirementsBasedOn(
			List<IRPModelElement> theSelectedEls, 
			String theGatewayStereotypeName) {
		
		Set<IRPModelElement> theUnclaimedReqts = new HashSet<IRPModelElement>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			IRPModelElement theElementToSearchUnder = theSelectedEl;
			
			if( theSelectedEl instanceof IRPActivityDiagram ){	
				theElementToSearchUnder = theElementToSearchUnder.getOwner().getOwner();
			}
			
			List<IRPModelElement> theReqtsToAdd = 
					GeneralHelpers.findModelElementsWithoutStereotypeNestedUnder( 
							theElementToSearchUnder, "Requirement", theGatewayStereotypeName );
			
			theUnclaimedReqts.addAll( theReqtsToAdd );	
		}

		return theUnclaimedReqts;
	}
	
	public static void moveUnclaimedRequirementsReadyForGatewaySync(
			List<IRPModelElement> theSelectedEls, 
			IRPProject theProject ){
		
		String theGatewayStereotypeName = "from.*";
		
		Set<IRPModelElement> theUnclaimedReqts = 
				buildSetOfUnclaimedRequirementsBasedOn( 
						theSelectedEls, 
						theGatewayStereotypeName );
		
		Logger.info( theUnclaimedReqts.size() + 
				" requirements unclaimed by the Gateway were found" );
		
		if( theUnclaimedReqts.isEmpty() ){
			
			String theMsg = "Nothing to do as there were no unclaimed requirements found";
		
			UserInterfaceHelpers.showInformationDialog( theMsg );
			
		} else {
			
			List<IRPModelElement> thePackageEls = 
					new ArrayList<IRPModelElement>(
							GeneralHelpers.findModelElementsNestedUnder(
									theProject, "Package", theGatewayStereotypeName) );
			
			List<IRPModelElement> theWritablePackages = new ArrayList<>();
			
			for( IRPModelElement thePackageEl : thePackageEls ){
				
				IRPPackage thePackage = (IRPPackage)thePackageEl;
				
				if( thePackage.getIsUnresolved()==0 && 
					thePackage.isReadOnly()==0 ){
				
					theWritablePackages.add( thePackage );
				}
			}
					
			Object[] options = new Object[theWritablePackages.size()];
			
			for (int i = 0; i < options.length; i++) {
				
				IRPPackage thePackage = (IRPPackage) theWritablePackages.get(i);
				Logger.writeLine("thePackage = " + thePackage.getFullPathNameIn());
				
//				String theOptionName =  theWritablePackages.get(i).getName() + " in " + 
//						theWritablePackages.get(i).getOwner().getFullPathName();
				options[i] = thePackage.getFullPathNameIn();
			}
			
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			if (theWritablePackages.isEmpty()){
				
				String theMsg = "Nothing to do as no writeable Gateway imported packages were found.\n" +
						"Recommendation is to either:\n" +
						"a) Add high-level requirements to the model using the Gateway to create the package(s), or\n" +
						"b) Create your own package with a from<X> stereotype to minimic the Gateway, or\n" + 
						"c) Assess whether there are existing from<X> stereotyped packages that are present but not writable and correct the situation.\n";
				
				Logger.info( theMsg );
				
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
					
					Logger.info( "Operation was cancelled by user with no changes made." );
					
				} else {			
					
					IRPModelElement thePackage = theProject.findElementsByFullName(theChoice.toString(), "Package");
					
					IRPStereotype theStereotypeToApply = 
							GeneralHelpers.getStereotypeAppliedTo( thePackage, theGatewayStereotypeName );
					
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
								
								String uniqueName = GeneralHelpers.determineUniqueNameBasedOn( 
										theReqt.getName(), "Requirement", thePackage );
								
								Logger.warning( "Warning: Same name as " + Logger.elementInfo( theReqt ) 
										+ " already exists under " + Logger.elementInfo(thePackage) + 
										", hence element was renamed to " + uniqueName );
								
								theReqt.setName( uniqueName );

							}

							Logger.info( "Moving " + Logger.elementInfo( theReqt ) + " from " 
									+ Logger.elementInfo( theReqt.getOwner() ) + " to " + Logger.elementInfo( thePackage ) 
									+ " and applying " + Logger.elementInfo( theStereotypeToApply ) );
							
							theReqt.setOwner( thePackage );
							theReqt.addStereotype(theStereotypeToApply.getName(), "Requirement");
							count++;
							theReqt.highLightElement();

							GeneralHelpers.applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
									theReqt, theStereotypeToApply );
						}
						
						Logger.info( "Finished (" + count + 
								" requirements were moved out of " + theUnclaimedReqts.size() + ")" );
						
					} else {
						Logger.info( "Cancelled due to user choice not to continue with the move." );
					}
				}			
			}
		}
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #121 25-NOV-2016: Move unclaimed requirements ready for Gateway synch now copes with duplicate names (F.J.Chadburn)
    #170 08-MAR-2017: Tweak to Add new requirement on ADs to add to same owner as user created (F.J.Chadburn)
    #232 27-SEP-2017: Improve move unclaimed req'ts needs so that it handles read-only packages better (F.J.Chadburn)
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

