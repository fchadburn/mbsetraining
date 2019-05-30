package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import generalhelpers.ConfigurationSettings;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import com.telelogic.rhapsody.core.*;

public class FunctionalAnalysisSettings {

	private static final String tagNameForAssemblyBlockUnderDev = "assemblyBlockUnderDev";
	private static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	private static final String tagNameForPackageForActorsAndTest = "packageForActorsAndTest";
	private static final String tagNameForPackageForBlocks = "packageForBlocks";
	private static final String tagNameForPackageForWorkingCopies = "packageForWorkingCopies";

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		@SuppressWarnings("unused")
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
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
			IRPModelElement basedOnContextEl ){

		Logger.writeLine("getBuildingBlock was invoked for " + Logger.elementInfo( basedOnContextEl ) );
		IRPClass theBuildingBlock =
				(IRPClass) getElementNamedInFunctionalPackageTag(
						basedOnContextEl, 
						tagNameForAssemblyBlockUnderDev );

		Logger.writeLine("... getBuildingBlock completed (" + Logger.elementInfo(theBuildingBlock) + " was found)");

		return theBuildingBlock;
	}

	public static IRPPackage getPackageForActorsAndTest(
			IRPModelElement basedOnContextEl ){

		IRPPackage thePackage = getPkgNamedInFunctionalPackageTag(
				basedOnContextEl, 
				tagNameForPackageForActorsAndTest );

		return thePackage;
	}

	public static IRPModelElement getElementNamedInFunctionalPackageTag(
			IRPModelElement basedOnContextEl,
			String theTagName ){

		IRPModelElement theEl = null;

		IRPModelElement theSettingsPkg = 
				getSimulationSettingsPackageBasedOn( basedOnContextEl );

		if( theSettingsPkg != null ){
			IRPTag theTag = theSettingsPkg.getTag( theTagName );

			if( theTag != null ){

				// retrieve tag value collection
				IRPCollection valSpecs = theTag.getValueSpecifications();

				@SuppressWarnings("rawtypes")
				Iterator looper = valSpecs.toList().iterator();

				// retrieve each element instance set as the tag value
				while( looper.hasNext() ){

					IRPInstanceValue ins = (IRPInstanceValue)looper.next();
					theEl = ins.getValue();
					break;
				}
			}
		}

		//		if( theEl == null ){
		//			Logger.writeLine( "Error in getElementNamedInFunctionalPackageTag, " + 
		//					"unable to find value for tag called " + theTagName + " under " + 
		//					Logger.elementInfo( basedOnContextEl ) );
		//		}

		return theEl;
	}

	public static IRPPackage getPkgNamedInFunctionalPackageTag(
			IRPModelElement basedOnContextEl,
			String theTagName ){

		IRPPackage thePackage = null;

		IRPModelElement theSettingsPkg = 
				getSimulationSettingsPackageBasedOn( basedOnContextEl );

		if( theSettingsPkg != null ){
			IRPTag theTag = theSettingsPkg.getTag( theTagName );

			if( theTag != null ){
				String thePackageName = theTag.getValue();

				thePackage = (IRPPackage) basedOnContextEl.getProject().findNestedElementRecursive(
						thePackageName, "Package");

				if( thePackage == null ){
					Logger.writeLine( "getPkgNamedInFunctionalPackageTag was unable to find package called " + 
							thePackageName );
				}
			} else {
				Logger.writeLine( "getPkgNamedInFunctionalPackageTag was unable to find tag called " + 
						theTagName + " underneath " + Logger.elementInfo( theSettingsPkg ) );
			}
		} else {
			Logger.writeLine("getPkgNamedInFunctionalPackageTag was unable to find a functional analysis pkg based on " + 
					Logger.elementInfo( basedOnContextEl ) );
		}

		if( thePackage == null ){

			IRPClass theLogicalBlock = getBlockUnderDev( 
					basedOnContextEl, 
					"Unable to determine Logical Block, please pick one" );

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

	public static IRPPackage getPkgThatOwnsEventsAndInterfaces(
			IRPModelElement basedOnContextEl ){

		IRPPackage thePackage = 
				(IRPPackage) getElementNamedInFunctionalPackageTag(
						basedOnContextEl, 
						tagNameForPackageForEventsAndInterfaces );

		return thePackage;
	}

	public static IRPPackage getWorkingPkgUnderDev(
			IRPModelElement basedOnContextEl ){

		IRPPackage theWorkingPkg = 
				(IRPPackage) getElementNamedInFunctionalPackageTag(
						basedOnContextEl, 
						tagNameForPackageForWorkingCopies );

		return theWorkingPkg;
	}

	public static IRPClass getBlockUnderDev(
			IRPModelElement basedOnContextEl,
			String theMsg ){

		IRPClass theBlockUnderDev = null;

		IRPClass theBuildingBlock = 
				FunctionalAnalysisSettings.getBuildingBlock( basedOnContextEl );

		if( theBuildingBlock == null ){

			Logger.writeLine( "Error in getBlockUnderDev, no building block was found underneath " + 
					Logger.elementInfo( basedOnContextEl ) );

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

		return theBlockUnderDev;
	}

	public static IRPClass getTestBlock(
			IRPClass withInstanceUnderTheBlock ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
		withInstanceUnderTheBlock.getNestedElementsByMetaClass( "Object", 0 ).toList();

		IRPClass theTestBlock = null;

		for( IRPModelElement theCandidatePart : theCandidateParts ) {

			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			Logger.writeLine( "The instance is " + Logger.elementInfo( theInstance) + 
					" typed by " + Logger.elementInfo( theClassifier ) );

			// don't add actors or test driver
			if( theClassifier != null && 
					theClassifier instanceof IRPClass &&
					GeneralHelpers.hasStereotypeCalled( "TestDriver", theClassifier ) ){

				Logger.writeLine("Found " + Logger.elementInfo( theClassifier ) );
				theTestBlock = (IRPClass) theClassifier;
			}
		}

		return theTestBlock;
	}

	public static List<IRPActor> getActors(
			IRPClass withInstancesUnderTheBlock ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
		withInstancesUnderTheBlock.getNestedElementsByMetaClass( "Instance", 1 ).toList();

		List<IRPActor> theActors = new ArrayList<IRPActor>();

		for( IRPModelElement theCandidatePart : theCandidateParts ) {

			IRPInstance theInstance = (IRPInstance)theCandidatePart;
			IRPClassifier theClassifier = theInstance.getOtherClass();

			// only add actors
			if( theClassifier != null && 
					theClassifier instanceof IRPActor ){

				theActors.add( (IRPActor) theClassifier );
			}
		}

		return theActors;
	}

	public static void setupFunctionalAnalysisTagsFor(
			IRPPackage theRootPackage,
			IRPClass theAssemblyBlockUnderDev,
			IRPPackage thePackageForEventsAndInterfaces, 
			IRPPackage thePackageForActorsAndTest,
			IRPPackage thePackageForBlocks ){

		if( theRootPackage != null ){

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForAssemblyBlockUnderDev, 
					"Class",
					theAssemblyBlockUnderDev );

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForPackageForActorsAndTest,
					"Package",
					thePackageForActorsAndTest );

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForPackageForEventsAndInterfaces, 
					"Package",
					thePackageForEventsAndInterfaces );

			setModelElementTagValueOn( 
					theRootPackage, 
					tagNameForPackageForBlocks, 
					"Package",
					thePackageForBlocks );	
		}
	}


	public static void setupFunctionalAnalysisTagsFor2(
			IRPPackage theRootPackage,
			IRPClass theAssemblyBlockUnderDev,
			IRPPackage thePackageForEventsAndInterfaces, 
			IRPPackage thePackageForActorsAndTest,
			IRPPackage thePackageForBlocks ){

		if( theRootPackage != null ){

			String theStereotypeName = 
					StereotypeAndPropertySettings.getSimulationPackageStereotype( theRootPackage );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForAssemblyBlockUnderDev, 
					theAssemblyBlockUnderDev );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForActorsAndTest, 
					thePackageForActorsAndTest );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForEventsAndInterfaces, 
					thePackageForEventsAndInterfaces );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForBlocks, 
					thePackageForBlocks );	
		}	
	}

	private static void setElementTagValueOn( 
			IRPModelElement theOwner, 
			String theStereotypeName,
			String theTagName, 
			IRPModelElement theValue ){

		// In order to set a value for a tag that comes from a stereotype, you need to quote 
		// the stereotype(stereotype_0)'s tag(tag_0) as its "base" tag

		IRPStereotype theStereotype = 
				GeneralHelpers.getExistingStereotype(
						theStereotypeName, theOwner.getProject() );

		IRPTag baseTag = theStereotype.getTag( theTagName );

		// Return the newly created tag with value "value_A" and set it to class_0
		IRPTag newTag = theOwner.setTagValue( baseTag, "" );

		// Add other tags with different value to class_0, if the multiplicity > 1
		newTag.addElementDefaultValue( theValue );
	}

	public static void setModelElementTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			String theTagTypeDeclaration,
			IRPModelElement theValue ){

		IRPTag theTag = theOwner.getTag( theTagName );

		if( theTag != null ){
			String theExistingTagValue = theTag.getValue();
			theTag.deleteFromProject();
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theNewTag.setDeclaration( theTagTypeDeclaration );
			theOwner.setTagElementValue( theNewTag, theValue );

			Logger.writeLine(theOwner, "already has a tag called " + theTagName + ", changing it from '" + theExistingTagValue + "'" + " to '" + theNewTag.getValue() + "'");

		} else {
			IRPTag theNewTag = (IRPTag)theOwner.addNewAggr( "Tag", theTagName );
			theNewTag.setDeclaration( theTagTypeDeclaration );
			theOwner.setTagElementValue( theNewTag, theValue );
			Logger.writeLine( theNewTag, "has been added to " + Logger.elementInfo(theOwner) + " and set to '" + theNewTag.getValue() + "'");
		}
	}

	public static IRPPackage getSimulationSettingsPackageBasedOn(
			IRPModelElement theContextEl ){

		IRPPackage theSettingsPkg = null;

		if( theContextEl instanceof IRPProject ){

			List<IRPModelElement> thePackageEls = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"Package", 
							StereotypeAndPropertySettings.getSimulationPackageStereotype( theContextEl ), 
							theContextEl.getProject(), 
							1 );

			if( thePackageEls.isEmpty() ){

				IRPModelElement theFunctionalAnalysisPkg = 
						theContextEl.findElementsByFullName( "FunctionalAnalysisPkg", "Package" );

				if( theFunctionalAnalysisPkg == null ){
					Logger.writeLine( "Warning in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

				} else {
					theSettingsPkg = (IRPPackage) theFunctionalAnalysisPkg;
				}

			} else if( thePackageEls.size()==1){

				theSettingsPkg = (IRPPackage) thePackageEls.get(0);

			} else {
				Logger.writeLine( "Error in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

				IRPModelElement theUserSelectedPkg = 
						UserInterfaceHelpers.launchDialogToSelectElement(
								thePackageEls, 
								"Choose which settings to use", 
								true );

				if( theUserSelectedPkg != null ){
					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
				}
			}

		} else if( theContextEl instanceof IRPPackage &&
				GeneralHelpers.hasStereotypeCalled(
						StereotypeAndPropertySettings.getSimulationPackageStereotype( theContextEl ), 
						theContextEl ) ){

			Logger.writeLine( "getSimulationSettingsPackageBasedOn, is returning " + Logger.elementInfo( theContextEl ) );

			theSettingsPkg = (IRPPackage) theContextEl;

		} else if( theContextEl instanceof IRPPackage &&
				GeneralHelpers.hasStereotypeCalled(
						StereotypeAndPropertySettings.getUseCasePackageStereotype( theContextEl ), 
						theContextEl ) ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theContextEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency ){

					IRPDependency theDependency = (IRPDependency)theReference;
					IRPModelElement theDependent = theDependency.getDependent();

					if( theDependent instanceof IRPPackage &&
							GeneralHelpers.hasStereotypeCalled(
									StereotypeAndPropertySettings.getSimulationPackageStereotype( theContextEl ), 
									theDependent ) ){

						theSettingsPkg = (IRPPackage) theDependent;
					}
				}
			}

		} else {

			// recurse
			theSettingsPkg = getSimulationSettingsPackageBasedOn(
					theContextEl.getOwner() );
		}

		return theSettingsPkg;
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

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
    #216 09-JUL-2017: Added a new Add Block/Part command added to the Functional Analysis menus (F.J.Chadburn)
    #220 12-JUL-2017: Added customisable Stereotype choice to the Block and block/Part creation dialogs (F.J.Chadburn) 
    #230 20-SEP-2017: Initial alpha trial for create test case script from a sequence diagram (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic  for profile/settings loading (F.J.Chadburn)

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
