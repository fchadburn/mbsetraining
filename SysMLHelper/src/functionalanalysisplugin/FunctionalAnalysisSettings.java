package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.List;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class FunctionalAnalysisSettings {
	
	public static final String tagNameForPackageUnderDev = "packageUnderDev";
	public static final String tagNameForDependency = "traceabilityTypeToUseForFunctions";	
	public static final String tagNameForIsPopulateOptionHidden = "isPopulateOptionHidden";
	public static final String tagNameForPopulateWantedByDefault = "isPopulateWantedByDefault";
	public static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	public static final String tagNameForPackageForActorsAndTest = "packageForActorsAndTest";
	public static final String tagNameForIsUserBlockChoiceEnabled = "isUserBlockChoiceEnabled";

	public static IRPPackage getPackageUnderDev(IRPProject inTheProject){
		
		IRPPackage thePackage = null;
		
		IRPModelElement theRootPackage = inTheProject.findNestedElementRecursive(
				"FunctionalAnalysisPkg", "Package");
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( tagNameForPackageUnderDev );
			
			if( theTag != null ){
				String thePackageName = theTag.getValue();
				
				thePackage = (IRPPackage) inTheProject.findNestedElementRecursive(thePackageName, "Package");
				
				if( thePackage==null){
					Logger.writeLine("Error in getPackageUnderDev, unable to find package called " + thePackageName);
				}
			} else {
				Logger.writeLine("Error in getPackageUnderDev, unable to find tag called " + tagNameForPackageUnderDev + 
						" underneath " + Logger.elementInfo( theRootPackage ) );
			}
		} else {
			Logger.writeLine("Error in getPackageUnderDev, unable to find FunctionalAnalysisPkg");
		}
		
		if (thePackage==null){
			Logger.writeLine("Error in getPackageUnderDev, unable to determine packageUnderDev from the tag value");
		}
		
		return thePackage;
	}
	
	public static List<IRPModelElement> getNonActorOrTestBlocks(
			IRPClass withInstancesUnderTheBlock ){
	
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
				withInstancesUnderTheBlock.getNestedElementsByMetaClass( "Instance", 1 ).toList();
					
		List<IRPModelElement> theNonActorOrTestBlocks = new ArrayList<IRPModelElement>();
		
		for( IRPModelElement theCandidatePart : theCandidateParts ) {
			
			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			// don't add actors or test driver
			if( theClassifier != null && 
				theClassifier instanceof IRPClass &&
				!GeneralHelpers.hasStereotypeCalled( "TestDriver", theClassifier ) &&
				!theNonActorOrTestBlocks.contains( theClassifier ) ){
				
				theNonActorOrTestBlocks.add( theClassifier );
			}
		}
		
		return theNonActorOrTestBlocks;
	}
	
	public static IRPClass getBuildingBlock( 
			IRPPackage underneathThePkg ){
		
		int count = 0;
		
		IRPClass theBuildingBlock = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateBlocks = 
				underneathThePkg.getNestedElementsByMetaClass( "Class", 1 ).toList();
		
		for( IRPModelElement theCandidateBlock : theCandidateBlocks ) {
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theInstances = 
					theCandidateBlock.getNestedElementsByMetaClass( "Instance", 0 ).toList();
			
			boolean isBuildingBlock = false;
			
			for( IRPModelElement theInstance : theInstances ) {
					
				if( theInstance.getUserDefinedMetaClass().equals("Object") ){
						
					isBuildingBlock = true;
					break;
				}
			}

			if( isBuildingBlock && theCandidateBlock instanceof IRPClass){
				Logger.writeLine( "getBuildingBlock called for " + Logger.elementInfo( underneathThePkg ) + 
						" successfully found " + Logger.elementInfo( theCandidateBlock ));
				
				theBuildingBlock = (IRPClass) theCandidateBlock;
				count++;
			}
		}
		
		if( count > 1 ){
			Logger.writeLine( "Warning in getBuildingBlock, " + count + 
					" building blocks were found when expecting just one." );
		
		} else if( count == 0 ){
			Logger.writeLine( "Error in getBuildingBlock, no building block was found in " + 
					Logger.elementInfo( underneathThePkg ) );
		}
		
		return theBuildingBlock;
	}
	
	public static IRPPackage getPackageForActorsAndTest(
			IRPProject inTheProject ){
		
		IRPPackage thePackage = null;

		IRPModelElement theRootPackage = inTheProject.findNestedElementRecursive(
				"FunctionalAnalysisPkg", "Package");
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( tagNameForPackageForActorsAndTest );
			
			if( theTag != null ){
				String thePackageName = theTag.getValue();
				
				thePackage = (IRPPackage) inTheProject.findNestedElementRecursive(
						thePackageName, "Package");
				
				if( thePackage==null){
					Logger.writeLine("Error in getPackageForActorsAndTest, unable to find package called " + thePackageName);
				}
			} else {
				Logger.writeLine("Error in getPackageForActorsAndTest, unable to find tag called " + tagNameForPackageForActorsAndTest + 
						" underneath " + Logger.elementInfo( theRootPackage ) );
			}
		} else {
			Logger.writeLine("Error in getPackageForActorsAndTest, unable to find FunctionalAnalysisPkg");
		}
		
		if (thePackage==null){
			Logger.writeLine("Error in getPackageForActorsAndTest, unable to determine package from the tag value");
		
			IRPClass theLogicalBlock = getBlockUnderDev( inTheProject, false );
			
			// old projects may not have an test package hence use the package the block is in
			IRPModelElement theOwner = theLogicalBlock.getOwner();
			
			if( theOwner instanceof IRPPackage ){
				thePackage = (IRPPackage)theOwner;
			} else {
				Logger.writeLine( "Error in getPackageForActorsAndTest: Can't find pkg for " + Logger.elementInfo( theLogicalBlock ) );
			}
		}
		
		return thePackage;
	}
	
	public static IRPPackage getPkgThatOwnsEventsAndInterfaces(
			IRPProject inTheProject ){
		
		IRPPackage thePackage = null;

		IRPModelElement theRootPackage = inTheProject.findNestedElementRecursive(
				"FunctionalAnalysisPkg", "Package");
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( tagNameForPackageForEventsAndInterfaces );
			
			if( theTag != null ){
				String thePackageName = theTag.getValue();
				
				thePackage = (IRPPackage) inTheProject.findNestedElementRecursive(
						thePackageName, "Package");
				
				if( thePackage==null){
					Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to find package called " + thePackageName);
				}
			} else {
				Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to find tag called " + tagNameForPackageUnderDev + 
						" underneath " + Logger.elementInfo( theRootPackage ) );
			}
		} else {
			Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to find FunctionalAnalysisPkg");
		}
		
		if (thePackage==null){
			Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to determine packageUnderDev from the tag value");
		
			IRPClass theLogicalBlock = getBlockUnderDev( inTheProject, false );
			
			// old projects may not have an InterfacesPkg hence use the package the block is in
			IRPModelElement theOwner = theLogicalBlock.getOwner();
			
			if( theOwner instanceof IRPPackage ){
				thePackage = (IRPPackage)theOwner;
			} else {
				Logger.writeLine( "Error in getPkgThatOwnsEventsAndInterfaces: Can't find event pkg for " + Logger.elementInfo( theLogicalBlock ) );
			}
		}
		
		return thePackage;
	}
	
	public static IRPPackage getWorkingPkgUnderDev(IRPProject inTheProject){
		
		IRPPackage theWorkingPkg = null;
		
		int count = 0;
		IRPPackage thePackageUnderDev = getPackageUnderDev( inTheProject );
		
		if( thePackageUnderDev != null ){
			
			@SuppressWarnings("unchecked")
			List<IRPPackage> theNestedPkgs = 
					thePackageUnderDev.getNestedElementsByMetaClass("Package", 1).toList();
			
			for (IRPPackage theNestedPkg : theNestedPkgs) {
				
				List<IRPModelElement> theDependencies = 
						GeneralHelpers.findElementsWithMetaClassAndStereotype(
								"Dependency", "AppliedProfile", theNestedPkg);
				
				for (IRPModelElement theDependencyElement : theDependencies) {
					
					IRPDependency theDependency = (IRPDependency)theDependencyElement;
					
					IRPModelElement theDependsOn = theDependency.getDependsOn();
					
					if (theDependsOn.getName().equals("RequirementsAnalysisProfile")){
						IRPModelElement theDependent = theDependency.getDependent();
						theWorkingPkg = (IRPPackage) theDependent;
						count++;
					}
				}
			}
		}
		
		if (count==0){
			Logger.writeLine("Error in getWorkingPkgUnderDev, no working package was found");
		} else if (count > 1){
			Logger.writeLine("Error in getWorkingPkgUnderDev, " + count + " working packages were found when expecting one");
		}
		
		return theWorkingPkg;
	}
	
	private static IRPClass launchDialogToSelectBlock(
			IRPPackage underPackage ){
		
		IRPClass theBlock = null;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
				underPackage.getNestedElementsByMetaClass( "Instance", 1 ).toList();
					
		List<IRPModelElement> theBlocks = new ArrayList<IRPModelElement>();
		
		for( IRPModelElement theCandidatePart : theCandidateParts ) {
			
			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			// don't add actors or test driver
			if( theClassifier != null && 
				theClassifier instanceof IRPClass &&
				!GeneralHelpers.hasStereotypeCalled("TestDriver", theClassifier) &&
				!theBlocks.contains( theClassifier ) ){
				
				theBlocks.add( theClassifier );
			}
		}

		theBlock = (IRPClass) GeneralHelpers.launchDialogToSelectElement(
				theBlocks, "Select Block to add the element to:", true );
		
		if( theBlock == null ){
			Logger.writeLine("Warning in launchDialogToSelectBlock, user did not select a block");
		}
		
		return theBlock;
	}
	
	public static IRPClass getBlockUnderDev(
			IRPProject inTheProject, 
			boolean withSelection ){
		
		IRPClass theBlockUnderDev = null;
		
		IRPPackage thePackageUnderDev = getPackageUnderDev( inTheProject );
		
		if( thePackageUnderDev == null ){
			
			Logger.writeLine( "Error in getBlockUnderDev, no package under development was found in " + 
					Logger.elementInfo( inTheProject ) );	
		} else {
			
			IRPClass theBuildingBlock = 
					FunctionalAnalysisSettings.getBuildingBlock( thePackageUnderDev );
			
			if( theBuildingBlock == null ){
				
				Logger.writeLine( "Error in getBlockUnderDev, no building block was found underneath " + 
						Logger.elementInfo( thePackageUnderDev ) );
			
			} else {
				
				List<IRPModelElement> theCandidates = 
						getNonActorOrTestBlocks( theBuildingBlock );
				
				if( theCandidates.isEmpty() ){
					
					Logger.writeLine("Error in getBlockUnderDev, no parts typed by Blocks were found underneath " + 
							Logger.elementInfo( theBuildingBlock ) );
				} else {
				
					if( withSelection ){
						
						if( theCandidates.size() > 1 ){
							final IRPModelElement theChosenBlockEl = 
									GeneralHelpers.launchDialogToSelectElement(
											theCandidates, "Select Block to add operation to:", true );
							
							if( theChosenBlockEl != null && theChosenBlockEl instanceof IRPClass ){
								theBlockUnderDev = (IRPClass) theChosenBlockEl;
							}
						} else {
							theBlockUnderDev = (IRPClass) theCandidates.get( 0 );
						}

					} else {
						
						for( IRPModelElement theCandidate : theCandidates ) {
							
							if( theCandidate instanceof IRPClass && 
								GeneralHelpers.hasStereotypeCalled( "LogicalSystem", theCandidate )){
								theBlockUnderDev = (IRPClass) theCandidate;
							}
							
						}
					}
				}
			}
		}

		return theBlockUnderDev;
	}
	
	public static IRPInstance getPartUnderDev(
			IRPProject inTheProject ){
		
		IRPInstance thePart = null;
		
		IRPPackage thePackageUnderDev = getPackageUnderDev( inTheProject );
		
		if( thePackageUnderDev != null ){
			
			if( getIsEnableBlockSelectionByUser( inTheProject ) ){
				
				@SuppressWarnings("unchecked")
				List<IRPModelElement> theCandidateParts = 
						thePackageUnderDev.getNestedElementsByMetaClass( "Instance", 1 ).toList();
							
				IRPModelElement theSelectedBlock =
						launchDialogToSelectBlock( thePackageUnderDev );
				
				for( IRPModelElement theCandidatePart : theCandidateParts ) {
					
					IRPInstance theInstance = (IRPInstance)theCandidatePart;
					IRPClassifier theClassifier = theInstance.getOtherClass();
					
					if( theClassifier != null && theClassifier.equals(theSelectedBlock)){
						thePart = (IRPInstance) theCandidatePart;
					}
				}
				
			} else {
				List<IRPModelElement> theParts = 
						GeneralHelpers.findElementsWithMetaClassAndStereotype(
								"Part", "LogicalSystem", thePackageUnderDev );
				
				if( theParts.size()==1 ){	
					thePart = (IRPInstance) theParts.get( 0 );				
				} else {
					Logger.writeLine( "Error in getPartUnderDev: Can't find LogicalSystem part for " + Logger.elementInfo( thePackageUnderDev ) );
				}	
			}
	
		} else {
			Logger.writeLine( "Error in getPartUnderDev for " + Logger.elementInfo(inTheProject) + 
					", unable to determine package under development");
		}

		return thePart;
	}
	
	public static IRPStereotype getStereotypeForFunctionTracing(IRPProject inTheProject){
		
		IRPStereotype theStereotype = null;
		
		IRPModelElement theRootPackage = inTheProject.findNestedElementRecursive(
				"FunctionalAnalysisPkg", "Package");
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( tagNameForDependency );
			String theTagValue = theTag.getValue();
			
			theStereotype = (IRPStereotype) inTheProject.findNestedElementRecursive(theTagValue, "Stereotype");
		} else {
			Logger.writeLine("Error in getPackageUnderDev, unable to find FunctionalAnalysisPkg");
		}
		
		return theStereotype;
	}
	
	public static boolean getTagBooleanValue(
			IRPProject inTheProject, String forTagName ){
		
		boolean result = false;
		
		IRPModelElement theRootPackage = 
				inTheProject.findNestedElementRecursive(
						"FunctionalAnalysisPkg", "Package" );
		
		if (theRootPackage != null){
			IRPTag theTag = theRootPackage.getTag( forTagName );
			
			if( theTag != null ){
				
				String theTagValue = theTag.getValue();
				
				if( theTagValue.contains("true")){
					result = true;
				}

			} else {
				Logger.writeLine( "Warning in getTagBooleanValue, unable to find tag called " + 
						forTagName + " underneath " + Logger.elementInfo( theRootPackage ) );
			}
		} else {
			Logger.writeLine( "Error in getTagBooleanValue, unable to find FunctionalAnalysisPkg" );
		}
		
		return result;
	}
	
	public static boolean getIsPopulateWantedByDefault(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForPopulateWantedByDefault );
		
		return result;
	}
	
	public static boolean getIsPopulateOptionHidden(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsPopulateOptionHidden );
		
		return result;
	}
	
	public static boolean getIsEnableBlockSelectionByUser(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsUserBlockChoiceEnabled );
		
		return result;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #054 13-JUL-2016: Create a nested BlockPkg package to contain the Block and events (F.J.Chadburn)
    #062 17-JUL-2016: Create InterfacesPkg and correct build issues by adding a Usage dependency (F.J.Chadburn)
    #078 28-JUL-2016: Added isPopulateWantedByDefault tag to FunctionalAnalysisPkg to give user option (F.J.Chadburn)
    #079 28-JUL-2016: Improved robustness of post add CallOp behaviour to prevent Rhapsody hanging (F.J.Chadburn)
    #087 09-AUG-2016: Added packageForEventsAndInterfaces tag to give user flexibility to change (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #093 23-AUG-2016: Added isPopulateOptionHidden tag to allow hiding of the populate check-box on dialogs (F.J.Chadburn)
    #106 03-NOV-2016: Ease usage by renaming UsageDomain block to SystemAssembly and moving up one package (F.J.Chadburn)
    #108 03-NOV-2016: Added tag for packageForActorsAndTest to FunctionalAnalysisPkg settings (F.J.Chadburn)
    
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
