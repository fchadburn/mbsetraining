package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;
import generalhelpers.UserInterfaceHelpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class SequenceDiagramHelper {	
	
	public static void updateVerificationsForSequenceDiagramsBasedOn(
			List<IRPModelElement> theSelectedEls){
		
		Set<IRPModelElement> theEls = buildSetOfElementsFor(theSelectedEls, "SequenceDiagram", true);
		
		for (IRPModelElement theEl : theEls) {
			updateVerificationsFor( (IRPSequenceDiagram)theEl );
		}
	}
	
	private static Set<IRPModelElement> buildSetOfElementsFor(
			List<IRPModelElement> theSelectedEls, String withMetaClass, boolean isRecursive) {
		
		Set<IRPModelElement> theMatchingEls = new HashSet<IRPModelElement>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			addElementIfItMatches(withMetaClass, theMatchingEls, theSelectedEl);
			
			if (isRecursive){
				
				@SuppressWarnings("unchecked")
				List<IRPModelElement> theCandidates = theSelectedEl.getNestedElementsByMetaClass(withMetaClass, 1).toList();
				
				for (IRPModelElement theCandidate : theCandidates) {				
					addElementIfItMatches(withMetaClass, theMatchingEls, theCandidate);
				}
			}
		}
		
		return theMatchingEls;
	}

	private static void addElementIfItMatches(
			String withMetaClass,
			Set<IRPModelElement> theMatchingEls, 
			IRPModelElement elementToAdd) {
		
		if (elementToAdd.getMetaClass().equals( withMetaClass )){
			
			if (elementToAdd instanceof IRPUnit){
				
				IRPUnit theUnit = (IRPUnit) elementToAdd;
				
				if (theUnit.isReadOnly()==0){
					theMatchingEls.add( elementToAdd );
				}
			} else {
				theMatchingEls.add( elementToAdd );
			}
		}
	}
	
	private static void updateVerificationsFor(IRPDiagram theDiagram){
		
		Set<IRPRequirement> theReqtsOnDiagram = buildSetOfRequirementsAlreadyOn(theDiagram);
		
		Set<IRPRequirement> theReqtsWithVerificationRelationsToDiagram = 
				TraceabilityHelper.getRequirementsThatTraceFromWithStereotype(
						theDiagram, "verify");
		
		Set<IRPRequirement> theRequirementsToRemove= new HashSet<IRPRequirement>( theReqtsWithVerificationRelationsToDiagram );
		theRequirementsToRemove.removeAll( theReqtsOnDiagram );
		
		if (!theRequirementsToRemove.isEmpty()){
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theDiagram.getNestedElementsByMetaClass("Dependency", 0).toList();
			
			for (IRPDependency theDependency : theDependencies) {
				
				String userDefinedMetaClass = theDependency.getUserDefinedMetaClass();
				
				if (userDefinedMetaClass.equals("Verification")){
					
					IRPModelElement dependsOn = theDependency.getDependsOn();
					
					if (dependsOn instanceof IRPRequirement &&
							theRequirementsToRemove.contains(dependsOn)){
						
						Logger.writeLine(dependsOn, "removed verification link");
						
						theDependency.deleteFromProject();
					}
					
				}
			}
		}
		
		Set<IRPRequirement> theRequirementsToAdd = new HashSet<IRPRequirement>( theReqtsOnDiagram );
		theRequirementsToAdd.removeAll( theReqtsWithVerificationRelationsToDiagram );
		
		theDiagram.highLightElement();
		
		for (IRPRequirement theReq : theRequirementsToAdd) {
			IRPDependency theDep = theDiagram.addDependencyTo( theReq );
			theDep.changeTo("Verification");
			Logger.writeLine(theReq, "added verification link");
			theDep.highLightElement();
		}
	}
	
	private static Set<IRPRequirement> buildSetOfRequirementsAlreadyOn(IRPDiagram theDiagram){
		
		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			
			if (theGraphEl instanceof IRPGraphNode){
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				if (theModelObject instanceof IRPRequirement){
					
					IRPRequirement theReqt = (IRPRequirement)theModelObject;
					theReqts.add( theReqt );
				}
			}
		}
			
		return theReqts;
	}
	
	public static void updateLifelinesToMatchPartsInActiveBuildingBlock(
			IRPSequenceDiagram theSequenceDiagram ){
		
		IRPPackage thePackageUnderDev =
				FunctionalAnalysisSettings.getPackageUnderDev( theSequenceDiagram.getProject() );
		
		if( thePackageUnderDev != null ){

			IRPClass theBuildingBlock = 
					FunctionalAnalysisSettings.getBuildingBlock( thePackageUnderDev );
					
			if( theBuildingBlock != null ){
				
				createSequenceDiagramFor(
						theBuildingBlock, 
						(IRPPackage) theSequenceDiagram.getOwner(), 
						theSequenceDiagram.getName() );
			
			} else {
				Logger.writeLine("Error, unable to find building block or tester pkg");
			}
		} else {
			Logger.writeLine( "Error, unable to find thePackageUnderDev" );
		}
	}
	
	public static void updateAutoShowSequenceDiagramFor(
			IRPClass theAssemblyBlock) {
		
		IRPPackage thePackageForSD = 
				FunctionalAnalysisSettings.getPackageForActorsAndTest(
						theAssemblyBlock.getProject() );
		
		if( thePackageForSD != null ){
			
			List<IRPModelElement> theSDs = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"SequenceDiagram", 
							"AutoShow", 
							thePackageForSD, 
							0 );
			
			if( theSDs.size()==1 ){
				
				IRPSequenceDiagram theSD = (IRPSequenceDiagram) theSDs.get( 0 );
				
				SequenceDiagramHelper.createSequenceDiagramFor(
						theAssemblyBlock, 
						thePackageForSD, 
						theSD.getName() );
			}
		}
	}
	
	public static void createSequenceDiagramFor(
			IRPClass theAssemblyBlock, 
			IRPPackage inPackage,
			String withName ){
		
		boolean isCreateSD = true;
		
		IRPModelElement theExistingDiagram = 
				inPackage.findNestedElement( withName, "SequenceDiagram" );
		
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts =
		    theAssemblyBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();
		
		if( theExistingDiagram != null ){
			
			String theMsg = Logger.elementInfo( theExistingDiagram ) + " already exists in " + 
					Logger.elementInfo( inPackage ) + "\nDo you want to recreate it with x" + theParts.size() + 
					" lifelines for:\n";
			
			for( Iterator<IRPInstance> iterator = theParts.iterator(); iterator.hasNext(); ){
				
				IRPInstance thePart = (IRPInstance) iterator.next();
				IRPClassifier theType = thePart.getOtherClass();
				theMsg += theType.getName() + "\n"; 
			}
					
			isCreateSD = UserInterfaceHelpers.askAQuestion( theMsg );
			
			if( isCreateSD ){
				theExistingDiagram.deleteFromProject();
			}
		}
		
		if( isCreateSD ){
			
			IRPSequenceDiagram theSD = inPackage.addSequenceDiagram( withName );

			int xPos = 30;
			int yPos = 0;
			int nWidth = 100;
			int nHeight = 1000;
			int xGap = 30;

			// Do Test Driver first
			for( IRPInstance thePart : theParts ) {

				if( GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) ){

					IRPClassifier theType = thePart.getOtherClass();
					theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
					xPos=xPos+nWidth+xGap;
				}
			}
			
			// Then actors
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( theType instanceof IRPActor ){
					theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
					xPos=xPos+nWidth+xGap;
				}
			}

			// Then components
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();

				if( !( theType instanceof IRPActor ) &&
					!GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) ){

					theSD.addNewNodeForElement( theType, xPos, yPos, nWidth, nHeight );
					xPos=xPos+nWidth+xGap;
				}
			}

			GeneralHelpers.applyExistingStereotype( "AutoShow", theSD );
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #013 10-MAY-2016: (new) Add support for sequence diagram req't and verification relation population (F.J.Chadburn)
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
    #179 29-MAY-2017: Add new Functional Analysis menu to Re-create «AutoShow» sequence diagram (F.J.Chadburn)
    #187 29-MAY-2017: Provide option to re-create «AutoShow» sequence diagram when adding new actor (F.J.Chadburn)
    #209 04-JUL-2017: Populate requirements for SD(s) based on messages now supported with Dialog (F.J.Chadburn)
    #216 09-JUL-2017: Added a new Add Block/Part command added to the Functional Analysis menus (F.J.Chadburn)

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

