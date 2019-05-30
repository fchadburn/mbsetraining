package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class StereotypeAndPropertySettings {

	public static IRPStereotype getStereotypeForTestbench(
			IRPModelElement basedOnContextEl ){
		
		String theTestbenchSterotype = getTestbenchStereotype( basedOnContextEl );
		
		IRPStereotype theStereotype = GeneralHelpers.getExistingStereotype( 
				theTestbenchSterotype, 
				basedOnContextEl.getProject() );
		
		if( theStereotype == null ){
			Logger.writeLine( "Error in getStereotypeForTestbench, no Stereotyped called " + 
					theTestbenchSterotype + " was found" );
		}	
		
		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeForTimeElapsedActor(
			IRPModelElement basedOnContextEl ){
		
		String theElapsedTimeActorStereotype = getElapsedTimeActorStereotype( basedOnContextEl );
		
		IRPStereotype theStereotype = GeneralHelpers.getExistingStereotype( 
				theElapsedTimeActorStereotype, 
				basedOnContextEl.getProject() );
		
		if( theStereotype == null ){
			Logger.writeLine( "Error in getStereotypeForTimeElapsedActor, no Stereotyped called " + 
					theElapsedTimeActorStereotype + " was found" );
		}	
		
		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeForTimeElapsedBlock(
			IRPModelElement basedOnContextEl ){
		
		String theElapsedTimeBlockStereotype = getElapsedTimeBlockStereotype( basedOnContextEl );

		IRPStereotype theStereotype = GeneralHelpers.getExistingStereotype( 
				theElapsedTimeBlockStereotype, 
				basedOnContextEl.getProject() );
		
		if( theStereotype == null ){
			Logger.writeLine( "Error in getStereotypeForTimeElapsedBlock, no Stereotyped called " + 
					theElapsedTimeBlockStereotype + " was found" );
		}	
		
		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeToUseForFunctions(
			IRPModelElement basedOnContextEl ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContextEl, 
				"SysMLHelper.FunctionalAnalysis.TraceabilityTypeToUseForFunctions" );
		
		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeToUseForActions(
			IRPModelElement basedOnContext ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"SysMLHelper.RequirementsAnalysis.TraceabilityTypeToUseForActions" );
		
		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeToUseForUseCases(
			IRPModelElement basedOnContext ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"SysMLHelper.RequirementsAnalysis.TraceabilityTypeToUseForUseCases" );
		
		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeForUseCaseDiagram(
			IRPModelElement basedOnContext ){
		
		IRPStereotype theStereotype = getStereotypeBasedOn(
				basedOnContext, 
				"SysMLHelper.RequirementsAnalysis.NewTermForUseCaseDiagram" );
		
		if( theStereotype == null ){
			Logger.writeLine( "Warning, no stereotype was set for NewTermForUseCaseDiagram" );
		}
		
		return theStereotype;
	}
	
	static public List<IRPActor> getMasterActorList(
			IRPProject forProject ) {
		
		List<IRPActor> theMasterActors = new ArrayList<>();

		List<IRPModelElement> theMasterActorStereotypes =
				StereotypeAndPropertySettings.getStereotypesForMasterActorPackage( 
						forProject );
		
		for( IRPModelElement theMasterActorStereotype : theMasterActorStereotypes ){
			
			List<IRPModelElement> theActorPackages = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"Package", 
							theMasterActorStereotype.getName(), 
							forProject, 
							1 );
			
			Logger.writeLine( "Looking for packages with metaclass " + 
					theMasterActorStereotype.getName() + ", found " + theActorPackages.size() );

			for( IRPModelElement theActorPackage : theActorPackages ){
				
				@SuppressWarnings("unchecked")
				List<IRPModelElement> theExistingActorEls = 
						theActorPackage.getNestedElementsByMetaClass(
								"Actor", 1 ).toList();
				
				if( theExistingActorEls.isEmpty() ){
					
					UserInterfaceHelpers.showWarningDialog("The existing " + 
							Logger.elementInfo( theActorPackage ) + 
							" has no actors in it, hence none will be created" );
				} else {
					for( IRPModelElement theExistingActorEl : theExistingActorEls ){
						theMasterActors.add( (IRPActor) theExistingActorEl );
					}
				}
			}	
		}
		
		if( theMasterActors.isEmpty() ){
			
			Logger.writeLine( "As no master actors were found, " +
					"I'm creating a new master actors package");
			
			IRPPackage theMasterActorsPkg = 
					(IRPPackage) forProject.addNewAggr(
							"Package", "MasterActorsPkg" );
			
			if( !theMasterActorStereotypes.isEmpty() ){

				IRPModelElement theMasterActorPackageStereotype =
						theMasterActorStereotypes.get(0);
				
				theMasterActorsPkg.changeTo( theMasterActorPackageStereotype.getName() );
			}
			
			List<String> theDefaultActorNames =
					StereotypeAndPropertySettings.getDefaultActorsForMasterActorsPackage( forProject );
			
			for( String theDefaultActorName : theDefaultActorNames ){
				
				theMasterActors.add( 
						theMasterActorsPkg.addActor( 
								theDefaultActorName ) );
			}
		}
		
		return theMasterActors;
	}
	
	public static IRPFlowchart getTemplateForActivityDiagram(
			IRPModelElement basedOnContext,
			String basedOnPropertyKey ){
		
		IRPFlowchart theTemplate = null;
		
		String theTemplateName = 
				basedOnContext.getPropertyValue( 
						basedOnPropertyKey );
		
		if( theTemplateName != null && 
			!theTemplateName.trim().isEmpty() ){
			
			theTemplate = (IRPFlowchart) basedOnContext.getProject().findNestedElementRecursive(
							theTemplateName, 
							"ActivityDiagram" );
			
			if( theTemplate == null ){
				Logger.writeLine( "Warning, unable to find template called " + 
						theTemplateName + " named in TemplateForActivityDiagram property" );
			}
		}
		
		Logger.writeLine("getTemplateForActivityDiagram, found " + Logger.elementInfo( theTemplate ) );
		return theTemplate;
	}
	
	public static String getDefaultActorPackageName(
			IRPModelElement basedOnContext ){
	
		String theValue = basedOnContext.getPropertyValue(
				"SysMLHelper.RequirementsAnalysis.DefaultActorPackageName");
		
		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}
		
		return theValue;
	}
	
	public static String getDefaultUseCasePackageName(
			IRPModelElement basedOnContext ){
	
		String theValue = basedOnContext.getPropertyValue(
				"SysMLHelper.RequirementsAnalysis.DefaultUseCasePackageName");
		
		if( theValue == null || theValue.isEmpty() ){
			theValue = "Error";
		}
		
		return theValue;
	}
	
	public static List<String> getStoreUnitInSeparateDirectoryNewTerms(
			IRPModelElement theContextEl ){
		
		return getListFromCommaSeparatedString(
				theContextEl, 
				"SysMLHelper.General.StoreUnitInSeparateDirectoryNewTerms" );
	}
	
	public static List<String> getDontCreateSeparateUnitNewTerms(
			IRPModelElement theContextEl ){
		
		return getListFromCommaSeparatedString(
				theContextEl, 
				"SysMLHelper.General.DontCreateSeparateUnitNewTerms" );
	}
	
	public static List<String> getListFromCommaSeparatedString(
			IRPModelElement theContextEl,
			String thePropertyKey ){
		
		List<String> theNewTerms = new ArrayList<>();
		
		String theValue = theContextEl.getPropertyValue( 
				thePropertyKey );
		
		if( theValue != null && !theValue.isEmpty() ){
			
			String[] split = theValue.split(",");
			
			for (int i = 0; i < split.length; i++) {
				String theNewTerm = split[ i ].trim();
				theNewTerms.add( theNewTerm );
			}
		}
		
		return theNewTerms;
	}
	
	public static List<String> getDefaultActorsForMasterActorsPackage(
			IRPPackage theContextEl ){
		
		return getDefaultLegalActorNamesFor(
				theContextEl, 
				"SysMLHelper.General.DefaultActorsForMasterActorsPackage");
	}
	
	public static List<String> getDefaultActorsForUseCaseDiagram(
			IRPPackage theContextEl ){
		
		return getDefaultLegalActorNamesFor(
				theContextEl, 
				"SysMLHelper.RequirementsAnalysis.DefaultActorsForUseCaseDiagram");
	}
	
	private static List<String> getDefaultLegalActorNamesFor(
			IRPPackage theContextEl,
			String basedOnPropertyKey ){
	
		List<String> theActorNames = new ArrayList<>();
		
		String theValue = theContextEl.getPropertyValue( 
				basedOnPropertyKey );
		
		String theRegEx = theContextEl.getPropertyValue( 
				"General.Model.NamesRegExp" );
		
		if( theValue != null && !theValue.isEmpty() ){
			
			String[] split = theValue.split(",");
			
			for (int i = 0; i < split.length; i++) {
				String theActorName = split[ i ].trim();
				
				if( theActorName.matches( theRegEx ) ){
					theActorNames.add( theActorName );
				}
			}
		}
		
		return theActorNames;
	}
	
	public static void setSavedInSeparateDirectoryIfAppropriateFor(
			IRPModelElement modelElement) {
		
//		Logger.info("setSavedInSeparateDirectoryIfAppropriateFor was invoked for " + Logger.elementInfo(modelElement) );
		
		if( modelElement != null && 
			modelElement instanceof IRPPackage ){
						
			List<String> theNewTerms = 
					StereotypeAndPropertySettings.getStoreUnitInSeparateDirectoryNewTerms( 
							modelElement );
			
			if( theNewTerms.contains( modelElement.getUserDefinedMetaClass() ) ){

				IRPPackage thePackage = (IRPPackage) modelElement;
				thePackage.setSavedInSeperateDirectory( 1 );
			}
		}
	}
	
	public static void setDontSaveAsSeparateUnitIfAppropriateFor(
			IRPModelElement modelElement ){
		
		Logger.info("setDontSaveAsSeparateUnitIfAppropriateFor was invoked for " + Logger.elementInfo(modelElement) );
		
		if( modelElement != null ){
						
			List<String> theNewTerms = 
					StereotypeAndPropertySettings.getDontCreateSeparateUnitNewTerms(
							modelElement );
			
			if( theNewTerms.contains( modelElement.getUserDefinedMetaClass() ) ){
				((IRPUnit) modelElement).setSeparateSaveUnit( 0 );		
			}
		}
	}
	
	public static List<IRPModelElement> getStereotypesForFunctionalDesignRootPackage(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.General.RootPackageStereotypes");
	}

	public static List<IRPModelElement> getStereotypesForMasterActorPackage(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.General.MasterActorPackageStereotypes");
	}
	
	public static List<IRPModelElement> getStereotypesForSystemDesignPackage(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.General.SystemLevelPackageStereotypes");
	}
	
	public static List<IRPModelElement> getStereotypesForBlockPartCreation(
			IRPModelElement forContextEl ){
		
		return getStereotypesBasedOnProperty(
				forContextEl, 
				"SysMLHelper.FunctionalAnalysis.StereotypesForBlockCreation");
	}
	
	public static List<IRPModelElement> getStereotypesBasedOnProperty(
			IRPModelElement forContextEl,
			String thePropertyKey ){
		
		List<IRPModelElement> theStereotypes = new ArrayList<>();
		
		String theStereotypeList = forContextEl.getPropertyValue(
				thePropertyKey );
				
		if( theStereotypeList != null){

			String[] split = theStereotypeList.split(",");
			
			for( String theString : split ){
			
				IRPModelElement theStereotypeElement = 
						GeneralHelpers.findElementWithMetaClassAndName( 
								"Stereotype", theString.trim(), forContextEl );
				
				if( theStereotypeElement != null ){
					theStereotypes.add( theStereotypeElement );
				}
			}
			
			Logger.writeLine( "getStereotypesForBlockPartCreation was invoked and found " + theStereotypeList + 
					", it is returning a list of x" + theStereotypes.size() + " stereotypes");
		}
		
		return theStereotypes;
	}
	
	private static IRPStereotype getStereotypeBasedOn(
			IRPModelElement theContextEl,
			String andPropertyValue ){
		
		IRPStereotype theStereotype = null;
		
		String theStereotypeName = 
				theContextEl.getPropertyValue( 
						andPropertyValue );
		
		if( theStereotypeName != null && 
			!theStereotypeName.trim().isEmpty() ){
			
			theStereotype = GeneralHelpers.getExistingStereotype( 
					theStereotypeName, 
					theContextEl.getProject() );
			
			if( theStereotype == null ){
				Logger.writeLine( "Error in getStereotypeBasedOn, no Stereotyped called " + theStereotypeName + " was found" );

				//theStereotype = selectAndPersistStereotype( basedOnContext.getProject(), thePkg, theTag );

			} else {				
				Logger.writeLine( "Using " + Logger.elementInfo( theStereotype ) + " for " + andPropertyValue );
			}		
		}
		
		return theStereotype;
	}
	
	public static String getElapsedTimeBlockStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.ElapsedTimeBlockStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getElapsedTimeActorStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.ElapsedTimeActorStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getTestbenchStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.TestbenchStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getMasterActorPackageStereotype(
			IRPModelElement basedOnContextEl ) {

		return basedOnContextEl.getPropertyValue( 
				"SysMLHelper.General.MasterActorPackageStereotype" );

	}

	public static String getActorPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.ActorPackageStereotype" );

			return thePropertyValue;
		}
	
	public static String getUseCasePackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.UseCasePackageStereotype" );

			return thePropertyValue;
		}

	public static String getUseCasePackageWorkingStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.UseCasePackageWorkingStereotype" );

			return thePropertyValue;
		}
	
	public static String getRequirementPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.RequirementPackageStereotype" );

			return thePropertyValue;
	}
	
	public static String getSimulationPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
			String thePropertyValue = 
					basedOnContextEl.getPropertyValue( 
							"SysMLHelper.General.SimulationPackageStereotype" );

			return thePropertyValue;
		}
	
	public static String getInterfacesPackageStereotype(
		IRPModelElement basedOnContextEl ) {
			
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.InterfacesPackageStereotype" );

		return thePropertyValue;
	}

	public static String getDesignPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.DesignPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getFunctionsPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.FunctionsPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getSystemContextPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.SystemContextPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getParametricsPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.ParametricsPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getTestPackageStereotype(
			IRPModelElement basedOnContextEl ) {
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.General.TestPackageStereotype" );
		
		return thePropertyValue;
	}
	
	public static String getUseCaseNoteText(
			IRPModelElement basedOnContextEl ){
		
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.RequirementsAnalysis.UseCaseNoteText" );
		
		if( thePropertyValue == null ){
			thePropertyValue = "Error in getUseCaseNoteText";
		}
		
		return thePropertyValue;
	}
	
	public static String getCreateRequirementTextForPrefixing(
			IRPModelElement basedOnContextEl ){
				
		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"SysMLHelper.RequirementsAnalysis.CreateRequirementTextForPrefixing" );
	
		if( thePropertyValue == null ){
			thePropertyValue = "Error in getCreateRequirementTextForPrefixing";
		}
		
		//#005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
		thePropertyValue = thePropertyValue.replaceAll(
				"ProjectName", basedOnContextEl.getProject().getName() );

		return thePropertyValue;
	}
	
	public static boolean getIsEnableAutoMoveOfRequirements(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.RequirementsAnalysis.IsEnableAutoMoveOfRequirements" );
		
		return result;
	}
	
	public static boolean getIsApplyAutoShowToSequenceDiagramTemplate(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsApplyAutoShowToSequenceDiagramTemplate" );
		
		return result;
	}
	
	public static boolean getIsCreateSDWithAutoShowApplied(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsCreateSDWithAutoShowApplied" );
		
		return result;
	}
	
	public static boolean getIsCreateSDWithTestDriverLifeline(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsCreateSDWithTestDriverLifeline" );
		
		return result;
	}
	
	public static boolean getIsEnableAutoMoveOfInterfaces(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsEnableAutoMoveOfInterfaces" );
		
		return result;
	}
	
	public static boolean getIsEnableGatewayTypes(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.RequirementsAnalysis.IsEnableGatewayTypes" );
		
		return result;
	}
	
	public static boolean getIsSendEventViaPanelOptionEnabled(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsSendEventViaPanelOptionEnabled" );
		
		return result;
	}
	
	public static boolean getIsSendEventViaPanelWantedByDefault(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsSendEventViaPanelWantedByDefault" );
		
		return result;
	}
	
	public static boolean getIsPopulateWantedByDefault(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl, 
				"General.Graphics.IsPopulateWantedByDefault" );
		
		return result;
	}
	
	public static boolean getIsPopulateOptionHidden(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsPopulateOptionHidden" );
				
		return result;
	}
	
	public static boolean getIsCreateParametricSubpackageSelected(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.General.IsCreateParametricSubpackageSelected" );
				
		return result;
	}
	
	public static boolean getIsCallOperationSupportEnabled(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"General.Graphics.IsCallOperationSupportEnabled" );
		
		return result;
	}
	
	public static boolean getIsConvertToDetailedADOptionEnabled(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl, 
				"SysMLHelper.FunctionalAnalysis.IsConvertToDetailedADOptionEnabled" );

		return result;
	}

	public static boolean getIsConvertToDetailedADOptionWantedByDefault(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsConvertToDetailedADOptionWantedByDefault" );

		return result;
	}
	
	public static boolean getIsAllowInheritanceChoices(
			IRPModelElement forContextEl ){
		
		boolean result = getBooleanPropertyValue(
				forContextEl,
				"SysMLHelper.FunctionalAnalysis.IsAllowInheritanceChoices" );
				
		return result;
	}
	
	public static boolean getBooleanPropertyValue(
			IRPModelElement forContextEl,
			String thePropertyKey ){
		
		boolean result = false;
		
		String theValue = null;
		
		try {
			theValue = forContextEl.getPropertyValue(
					thePropertyKey );
		} catch (Exception e) {
			Logger.writeLine("Exception in getBooleanPropertyValue, e=" + e.getMessage() );
		}
				
		if( theValue != null ){
			result = theValue.equals( "True" );
		} else {
			Logger.writeLine("Error in getBooleanPropertyValue, " +
					"unable to get thePropertyKey property value from " + 
					Logger.elementInfo( forContextEl ) );
		}
		
		return result;
	}
	
	IRPModelElement m_ContextEl;
	
	public StereotypeAndPropertySettings( 
			IRPModelElement theContectEl ) {
		
		m_ContextEl = theContectEl;
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)

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