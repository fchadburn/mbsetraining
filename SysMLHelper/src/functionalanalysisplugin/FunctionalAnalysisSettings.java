package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.List;

import generalhelpers.ConfigurationSettings;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class FunctionalAnalysisSettings {
	
	private static final String tagNameForPackageUnderDev = "packageUnderDev";
	private static final String tagNameForTraceabilityTypeToUseForFunctions = "traceabilityTypeToUseForFunctions";	
	private static final String tagNameForIsPopulateOptionHidden = "isPopulateOptionHidden";
	private static final String tagNameForIsPopulateWantedByDefault = "isPopulateWantedByDefault";
	private static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	private static final String tagNameForPackageForActorsAndTest = "packageForActorsAndTest";
	private static final String tagNameForIsSendEventViaPanelOptionEnabled = "isSendEventViaPanelOptionEnabled";
	private static final String tagNameForIsSendEventViaPanelWantedByDefault = "isSendEventViaPanelWantedByDefault";
	private static final String tagNameForIsConvertToDetailedADOptionEnabled = "isConvertToDetailedADOptionEnabled";
	private static final String tagNameForIsConvertToDetailedADOptionWantedByDefault = "isConvertToDetailedADOptionWantedByDefault";
	private static final String tagNameForIsCallOperationSupportEnabled = "isCallOperationSupportEnabled";
	
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
	
	public static List<IRPClass> getBuildingBlocks(
			IRPPackage underneathThePkg ){
		
		List<IRPClass> theBuildingBlocks = new ArrayList<IRPClass>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateBlocks = 
				underneathThePkg.getNestedElementsByMetaClass( "Class", 1 ).toList();
		
		for( IRPModelElement theCandidateBlock : theCandidateBlocks ) {
			
			@SuppressWarnings("unchecked")
			List<IRPInstance> theInstances = 
					theCandidateBlock.getNestedElementsByMetaClass( "Instance", 0 ).toList();
			
			for (IRPInstance theInstance : theInstances) {
			
				if( theInstance.getUserDefinedMetaClass().equals("Object")){
					theBuildingBlocks.add( (IRPClass) theCandidateBlock );
					break;
				}
			}
		}
				
		return theBuildingBlocks;
	}
	
	public static IRPClass getBuildingBlock( 
			IRPPackage underneathThePkg ){
		
		List<IRPClass> theBuildingBlocks = getBuildingBlocks( underneathThePkg );

		IRPClass theBuildingBlock = null;

		int theSize = theBuildingBlocks.size();
		
		if( theSize == 0 ){

			Logger.writeLine( "Error in getBuildingBlock, no building block was found in " + 
					Logger.elementInfo( underneathThePkg ) );
			
		} else if( theSize == 1 ){
			
			theBuildingBlock = theBuildingBlocks.get( 0 );
			
			Logger.writeLine( "getBuildingBlock called for " + Logger.elementInfo( underneathThePkg ) + 
					" successfully found " + Logger.elementInfo( theBuildingBlock ));
						
		} else {

			theBuildingBlock = theBuildingBlocks.get( 0 );
			
			Logger.writeLine( "Warning in getBuildingBlock, " + theSize + 
					" building blocks were found when expecting just one." );
			
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
		
			IRPClass theLogicalBlock = getBlockUnderDev( inTheProject, "Unable to determine package for actors and test, which Block is under dev" );
			
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
		
			IRPClass theLogicalBlock = getBlockUnderDev( inTheProject, "Unable to determine Logical Block, please pick one" );
			
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
								"Dependency", "AppliedProfile", theNestedPkg, 0 );
				
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
	
	public static IRPClass getBlockUnderDev(
			IRPProject inTheProject,
			String theMsg ){
		
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

					if( theCandidates.size() > 1 ){
						final IRPModelElement theChosenBlockEl = 
								GeneralHelpers.launchDialogToSelectElement(
										theCandidates, theMsg, true ); 

						if( theChosenBlockEl != null && theChosenBlockEl instanceof IRPClass ){
							theBlockUnderDev = (IRPClass) theChosenBlockEl;
						}
					} else {
						theBlockUnderDev = (IRPClass) theCandidates.get( 0 );
					}
				}
			}
		}

		return theBlockUnderDev;
	}
	
	public static boolean getTagBooleanValue(
			IRPProject inTheProject, String forTagName, boolean withDefault ){
		
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
						forTagName + " underneath " + Logger.elementInfo( theRootPackage ) + " so using '" + withDefault + "' instead" );
				result = withDefault;
			}
		} else {
			Logger.writeLine( "Error in getTagBooleanValue, unable to find FunctionalAnalysisPkg while looking for tag called " + 
					forTagName + " underneath " + Logger.elementInfo( theRootPackage ) + " so using '" + withDefault + "' instead" );			
			result = withDefault;
		}
		
		return result;
	}
	
	public static boolean getIsSendEventViaPanelOptionEnabled(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsSendEventViaPanelOptionEnabled, true );
		
		return result;
	}
	
	public static boolean getIsSendEventViaPanelWantedByDefault(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsSendEventViaPanelWantedByDefault, true );
		
		return result;
	}
	
	public static boolean getIsPopulateWantedByDefault(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsPopulateWantedByDefault, false );
		
		return result;
	}
	
	public static boolean getIsPopulateOptionHidden(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsPopulateOptionHidden, true );
		
		return result;
	}
	
	public static boolean getIsConvertToDetailedADOptionEnabled(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsConvertToDetailedADOptionEnabled, false );
		
		return result;
	}

	public static boolean getIsConvertToDetailedADOptionWantedByDefault(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsConvertToDetailedADOptionWantedByDefault, false );
		
		return result;
	}
	
	public static boolean getIsCallOperationSupportEnabled(
			IRPProject inTheProject ){
		
		boolean result = getTagBooleanValue(
				inTheProject, tagNameForIsCallOperationSupportEnabled, true );
		
		return result;
	}
	
	public static void setupFunctionalAnalysisTagsFor(
			IRPProject theProject,
			IRPPackage thePackageUnderDev,
			IRPPackage thePackageForEventsAndInterfaces, 
			IRPPackage thePackageForActorsAndTest ){

		IRPModelElement theRootPackage = 
				theProject.findNestedElementRecursive(
						"FunctionalAnalysisPkg", "Package" );
		
		if( theRootPackage != null ){
			
			ConfigurationSettings theSettings = ConfigurationSettings.getInstance();

			setPackageTagValueOn( 
					theRootPackage, 
					tagNameForPackageUnderDev, 
					thePackageUnderDev );
			
			setStringTagValueOn( 
					theRootPackage, 
					tagNameForTraceabilityTypeToUseForFunctions, 
					theSettings.getProperty( tagNameForTraceabilityTypeToUseForFunctions, "satisfy" ) );
					
			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsPopulateOptionHidden, 
					theSettings.getProperty( tagNameForIsPopulateOptionHidden, "true" ) );
			
			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsPopulateWantedByDefault, 
					theSettings.getProperty( tagNameForIsPopulateWantedByDefault, "false" ) );

			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsSendEventViaPanelOptionEnabled, 
					theSettings.getProperty( tagNameForIsSendEventViaPanelOptionEnabled, "true" ) );
			
			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsSendEventViaPanelWantedByDefault, 
					theSettings.getProperty( tagNameForIsSendEventViaPanelWantedByDefault, "false" ) );
		
			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsConvertToDetailedADOptionEnabled, 
					theSettings.getProperty( tagNameForIsConvertToDetailedADOptionEnabled, "false" ) );
			
			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsConvertToDetailedADOptionWantedByDefault, 
					theSettings.getProperty( tagNameForIsConvertToDetailedADOptionWantedByDefault, "false" ) );

			setStringTagValueOn( 
					theRootPackage, 
					tagNameForIsCallOperationSupportEnabled, 
					theSettings.getProperty( tagNameForIsCallOperationSupportEnabled, "true" ) );
			
			setPackageTagValueOn( 
					theRootPackage, 
					tagNameForPackageForEventsAndInterfaces, 
					thePackageForEventsAndInterfaces );
			
			setPackageTagValueOn( 
					theRootPackage, 
					tagNameForPackageForActorsAndTest, 
					thePackageForActorsAndTest );			
		}
	}
	
	private static void setStringTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			String theValue ){
		
		IRPTag theTag = theOwner.getTag( theTagName );
		
		if( theTag != null ){
			String theExistingTagValue = theTag.getValue();
			Logger.writeLine(theOwner, "already has a tag called " + theTagName + ", set to '" + theExistingTagValue + "' (leaving unchanged)" );

		} else {
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theOwner.setTagValue(theNewTag, theValue);
			Logger.writeLine( theNewTag, "has been added to " + Logger.elementInfo(theOwner) + " and set to '" + theNewTag.getValue() + "'");
		}
	}
	
	public static void setPackageTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			IRPPackage theValue ){
		
		IRPTag theTag = theOwner.getTag( theTagName );
		
		if( theTag != null ){
			String theExistingTagValue = theTag.getValue();
			theTag.deleteFromProject();
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theNewTag.setDeclaration("Package");
			theOwner.setTagElementValue(theNewTag, theValue);
			
			Logger.writeLine(theOwner, "already has a tag called " + theTagName + ", changing it from '" + theExistingTagValue + "'" + " to '" + theNewTag.getValue() + "'");

		} else {
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theOwner.setTagElementValue(theNewTag, theValue);
			Logger.writeLine( theNewTag, "has been added to " + Logger.elementInfo(theOwner) + " and set to '" + theNewTag.getValue() + "'");
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

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
    #115 13-NOV-2016: Removed use of isEnableBlockSelectionByUser tag and <<LogicalSystem>> by helper (F.J.Chadburn)
    #116 13-NOV-2016: FunctionalAnalysisPkg tags now set programmatically to ease helper use in existing models (F.J.Chadburn)
    #118 13-NOV-2016: Default FunctionalAnalysisPkg tags now set in Config.properties file (F.J.Chadburn)
    #126 25-NOV-2016: Fixes to CreateNewActorPanel to cope better when multiple blocks are in play (F.J.Chadburn)
    #127 25-NOV-2016: Improved usability of ViaPanel event creation by enabling default selection via tags (F.J.Chadburn)
    #135 02-DEC-2016: Avoid port proliferation in inheritance tree for actors/system (F.J.Chadburn)
    #140 02-DEC-2016: Don't overwrite boolean tags in FunctionalAnalysisPkg to preserve user choice (F.J.Chadburn)
    #142 18-DEC-2016: Project properties now set via config.properties, e.g., to easily switch off backups (F.J.Chadburn)
    #143 18-DEC-2016: Add separate tag to enable/disable conversion to detailed option in Copy AD dialog (F.J.Chadburn)
    #144 18-DEC-2016: Add default behaviour to protect for instances where tags are not in model (F.J.Chadburn)
    #145 18-DEC-2016: Fix to remove warning with getWorkingPkgUnderDev unexpectedly finding 2 packages (F.J.Chadburn)
    #161 05-FEB-2017: Support nested diagram links in CallOperation description (F.J.Chadburn) 
    #171 08-MAR-2017: Added some dormant ops to GeneralHelpers to assist with 3rd party integration (F.J.Chadburn)
    #186 29-MAY-2017: Add context string to getBlockUnderDev to make it clearer for user when selecting (F.J.Chadburn)

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
