package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

public class SelectedElementContext {

	private static final String tagNameForAssemblyBlockUnderDev = "assemblyBlockUnderDev";
	private static final String tagNameForPackageForEventsAndInterfaces = "packageForEventsAndInterfaces";
	private static final String tagNameForPackageForBlocks = "packageForBlocks";

	private IRPApplication _rhpApp = null;
	private List<IRPGraphElement> _selectedGraphEls;
	private IRPModelElement _selectedEl = null;
	private IRPModelElement _contextEl = null;
	private IRPClass _buildingBlock = null;
	private IRPClass _chosenBlock = null;
	private IRPDiagram _sourceGraphElDiagram = null;
	private Set<IRPRequirement> _selectedReqts = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		SelectedElementContext theElementContext = new SelectedElementContext( theAppID );

		IRPClass theBlock = 
				theElementContext.getBlockUnderDev( "Get me the Block" );

		UserInterfaceHelpers.showInformationDialog( "The Block is " + Logger.elementInfo (theBlock) );
	}

	@SuppressWarnings("unchecked")
	public SelectedElementContext(
			String theAppID ){
		
		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		_selectedGraphEls = _rhpApp.getSelectedGraphElements().toList();
		_selectedEl = _rhpApp.getSelectedElement();
		_contextEl = getContextEl();
		_buildingBlock = getBuildingBlock();
		_sourceGraphElDiagram = getSourceDiagram();
		_selectedReqts = getSelectedReqts();
	}
	
	@SuppressWarnings("unchecked")
	public Set<IRPRequirement> getSelectedReqts(){
		
		if( _selectedReqts == null ){

			Set<IRPModelElement> theMatchingEls = 
					GeneralHelpers.findModelElementsIn( 
							_selectedGraphEls, 
							"Requirement" );

			// cast to IRPRequirement
			_selectedReqts = 
				(Set<IRPRequirement>)(Set<?>) theMatchingEls;
		}
		
		return _selectedReqts;
	}

	public IRPModelElement getSelectedEl(){
		return _selectedEl;
	}
	
	public IRPGraphElement getSelectedGraphEl(){
		
		IRPGraphElement theGraphEl = null;
		
		if( _selectedGraphEls != null &&
				!_selectedGraphEls.isEmpty() ){
			
			theGraphEl = _selectedGraphEls.get( 0 );
		} else {
			Logger.writeLine( "getSelectedGraphEl is returning null");
		}
		
		return theGraphEl;
	}
	
	public IRPClass getChosenBlock(){
		return _chosenBlock;
	}
	
	public IRPDiagram getSourceDiagram(){

		if( _sourceGraphElDiagram == null ){
			
			if( _selectedEl instanceof IRPDiagram ){

				_sourceGraphElDiagram = (IRPDiagram) _selectedEl;
			} else if( _selectedGraphEls != null && !_selectedGraphEls.isEmpty() ){
				_sourceGraphElDiagram = _selectedGraphEls.get( 0 ).getDiagram();
			}
		}
		
		return _sourceGraphElDiagram;
	}
	
	private IRPModelElement getContextEl(){

		IRPModelElement theContextEl = _selectedEl;

		if( theContextEl == null ){

			if( _selectedGraphEls != null && !_selectedGraphEls.isEmpty() ){

				IRPGraphElement selectedGraphEl = _selectedGraphEls.get( 0 );
				theContextEl = selectedGraphEl.getModelObject();
			}
		}
//
//		if( theContextEl == null ){
//			Logger.writeLine( "Error in getContextEl, unable to determine theContextEl" );
//		}

		return theContextEl;
	}

	public IRPClass getBlockUnderDev(
			String theMsg ){

		if( _chosenBlock == null ){

			if( _selectedEl instanceof IRPClass ){

				if( GeneralHelpers.hasStereotypeCalled( "TestDriver", _selectedEl ) ){
					UserInterfaceHelpers.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );
				} else {
					_chosenBlock = (IRPClass) _selectedEl;
				}

			} else if( _selectedEl instanceof IRPInstance ){

				IRPInstance thePart = (IRPInstance) _selectedEl;
				IRPClassifier theOtherClass = thePart.getOtherClass();

				if( GeneralHelpers.hasStereotypeCalled( "TestDriver", theOtherClass ) ){

					UserInterfaceHelpers.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );

				} else if( !(theOtherClass instanceof IRPClass) ){

					UserInterfaceHelpers.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a " + _selectedEl.getUserDefinedMetaClass() );
				} else {
					_chosenBlock = (IRPClass) theOtherClass;
				}

			} else if( _selectedEl.getMetaClass().equals( "StatechartDiagram" ) ){

				IRPModelElement theOwner = 
						GeneralHelpers.findOwningClassIfOneExistsFor( _selectedEl );

				Logger.writeLine( theOwner, "is the Owner");

				if( GeneralHelpers.hasStereotypeCalled( "TestDriver", theOwner ) ){

					UserInterfaceHelpers.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a TestDriver part" );

				} else if( !(theOwner instanceof IRPClass) ){

					UserInterfaceHelpers.showWarningDialog( "Sorry, you cannot perform " + theMsg + " with a " + _selectedEl.getUserDefinedMetaClass() );
				} else {
					_chosenBlock = (IRPClass) theOwner;
				}
			} else {
				List<IRPModelElement> theCandidates = 
						getNonActorOrTestBlocks( _buildingBlock );

				if( theCandidates.isEmpty() ){

					Logger.writeLine("Error in getBlockUnderDev, no parts typed by Blocks were found underneath " + 
							Logger.elementInfo( _buildingBlock ) );
				} else {

					if( theCandidates.size() > 1 ){

						final IRPModelElement theChosenBlockEl = 
								GeneralHelpers.launchDialogToSelectElement(
										theCandidates, 
										theMsg, 
										true ); 

						if( theChosenBlockEl != null && 
								theChosenBlockEl instanceof IRPClass ){

							_chosenBlock = (IRPClass) theChosenBlockEl;
						}
					} else {
						_chosenBlock = (IRPClass) theCandidates.get( 0 );
					}
				}
			}
		}

		return _chosenBlock;
	}
	
	public IRPClass getBuildingBlock(){
		
		if( _buildingBlock == null && _contextEl != null ){
			
			try {
				
				IRPModelElement elementInTag = FunctionalAnalysisSettings.getElementNamedInFunctionalPackageTag(
						_contextEl, 
						tagNameForAssemblyBlockUnderDev );
				
				Logger.info( "Element named in " + tagNameForAssemblyBlockUnderDev + " is " + Logger.elementInfo( elementInTag ) );

				if( elementInTag != null && elementInTag instanceof IRPClass ){
					_buildingBlock = (IRPClass)elementInTag;
				}

			} catch (Exception e) {
				Logger.writeLine( "Exception in getBuildingBlock, " +
						"while trying to get " + tagNameForAssemblyBlockUnderDev );
			}
		}

//		Logger.writeLine( "getBuildingBlock completed (" + 
//				Logger.elementInfo( m_BuildingBlock ) + " was found)" );

		//		List<IRPClass> theBuildingBlocks = getBuildingBlocks( underneathThePkg );
		//
		//		IRPClass theBuildingBlock = null;
		//
		//		int theSize = theBuildingBlocks.size();
		//
		//		if( theSize == 0 ){
		//
		//			Logger.writeLine( "Error in getBuildingBlock, no building block was found in " + 
		//					Logger.elementInfo( underneathThePkg ) );
		//
		//		} else if( theSize == 1 ){
		//
		//			theBuildingBlock = theBuildingBlocks.get( 0 );
		//
		//			Logger.writeLine( "getBuildingBlock called for " + Logger.elementInfo( underneathThePkg ) + 
		//					" successfully found " + Logger.elementInfo( theBuildingBlock ));
		//
		//		} else {
		//
		//			int count = 0; 
		//
		//			for( IRPClass theCandidateBlock : theBuildingBlocks ){
		//
		//				IRPClass theTestBlock = getTestBlock( theCandidateBlock );
		//
		//				if( theTestBlock == null ){
		//					theBuildingBlock = theCandidateBlock;
		//					count++;
		//				}
		//			}
		//
		//			if( count > 1 ){
		//				Logger.writeLine( "Warning in getBuildingBlock, " + theSize + 
		//						" building blocks were found when expecting just one." );
		//			} else {
		//				Logger.writeLine( "Warning in getBuildingBlock, " + theSize + 
		//						" building blocks were found when expecting just one, but " + 
		//						Logger.elementInfo( theBuildingBlock ) + " was selectable" );
		//			}			
		//		}				

		return _buildingBlock;
	}

	private static List<IRPModelElement> getNonActorOrTestBlocks(
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

	public IRPPackage getPkgThatOwnsEventsAndInterfaces(){

		IRPPackage thePackage = 
				(IRPPackage) getElementNamedInFunctionalPackageTag(
						tagNameForPackageForEventsAndInterfaces );

		//		IRPModelElement theRootPackage = getSimulationSettingsPackageBasedOn( basedOnContextEl );
		//
		//		if( theRootPackage != null ){
		//			IRPTag theTag = theRootPackage.getTag( tagNameForPackageForEventsAndInterfaces );
		//
		//			if( theTag != null ){
		//				String thePackageName = theTag.getValue();
		//
		//				thePackage = (IRPPackage) basedOnContextEl.getProject().findNestedElementRecursive(
		//						thePackageName, "Package");
		//
		//				if( thePackage == null ){
		//					Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to find package called " + thePackageName);
		//				}
		//			} else {
		//				Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to find tag called " + tagNameForPackageUnderDev + 
		//						" underneath " + Logger.elementInfo( theRootPackage ) );
		//			}
		//		} else {
		//			Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to find FunctionalAnalysisPkg");
		//		}
		//
		//		if( thePackage == null ){
		//			
		//			Logger.writeLine("Error in getPkgThatOwnsEventsAndInterfaces, unable to determine packageUnderDev from the tag value");
		//
		//			IRPClass theLogicalBlock = getBlockUnderDev( 
		//					basedOnContextEl, 
		//					"Unable to determine Logical Block, please pick one" );
		//
		//			// old projects may not have an InterfacesPkg hence use the package the block is in
		//			IRPModelElement theOwner = theLogicalBlock.getOwner();
		//
		//			if( theOwner instanceof IRPPackage ){
		//				thePackage = (IRPPackage)theOwner;
		//			} else {
		//				Logger.writeLine( "Error in getPkgThatOwnsEventsAndInterfaces: Can't find event pkg for " + Logger.elementInfo( theLogicalBlock ) );
		//			}
		//		}

		return thePackage;
	}

	public IRPModelElement getElementNamedInFunctionalPackageTag(
			String theTagName ){

		IRPModelElement theEl = null;

		IRPModelElement theSettingsPkg = 
				getSimulationSettingsPackageBasedOn( _contextEl );

		if( theSettingsPkg != null ){
			IRPTag theTag = theSettingsPkg.getTag( theTagName );

			if( theTag != null ){

				//call getValueSpecifications() to retrieve tag value collection

				IRPCollection valSpecs = theTag.getValueSpecifications();

				@SuppressWarnings("rawtypes")
				Iterator looper = valSpecs.toList().iterator();

				//call getValue() to retrieve each element instance set as the tag value

				while( looper.hasNext() ){

					IRPInstanceValue ins = (IRPInstanceValue)looper.next();
					theEl = ins.getValue();
					break;
				}
			}
		}

		if( theEl == null ){
			Logger.writeLine( "Error in getElementNamedInFunctionalPackageTag, " + 
					"unable to find value for tag called " + theTagName + " under " + 
					Logger.elementInfo( _contextEl ) );
		}

		return theEl;
	}

	public IRPPackage getSimulationSettingsPackageBasedOn(
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
				Logger.writeLine( "Warning in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

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

			Logger.writeLine("Recursing to look at owner of " + Logger.elementInfo( theContextEl ) );

			// recurse
			theSettingsPkg = getSimulationSettingsPackageBasedOn(
					theContextEl.getOwner() );
		}

		return theSettingsPkg;
	}
	
	public void bleedColorToElementsRelatedTo(
			List<IRPRequirement> theSelectedReqts ){
		
		IRPGraphElement theSelectedGraphEl = getSelectedGraphEl();
		
		// only bleed on activity diagrams		
		if( theSelectedGraphEl != null &&
				theSelectedGraphEl.getDiagram() instanceof IRPActivityDiagram ){
			
			for( IRPGraphElement theGraphEl : _selectedGraphEls ) {
				bleedColorToElementsRelatedTo( theGraphEl, theSelectedReqts );
			}
		}
	}

	private void bleedColorToElementsRelatedTo(
			IRPGraphElement theGraphEl,
			List<IRPRequirement> theSelectedReqts ){
		
		String theColorSetting = "255,0,0";
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		IRPModelElement theEl = theGraphEl.getModelObject();
		
		if( theEl != null ){
								
			Logger.writeLine("Setting color to red for " + theEl.getName());
			theGraphEl.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theExistingDeps = theEl.getDependencies().toList();
			
			for (IRPDependency theDependency : theExistingDeps) {
				
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				if (theDependsOn != null && 
					theDependsOn instanceof IRPRequirement && 
					theSelectedReqts.contains( theDependsOn )){	
					
					bleedColorToGraphElsRelatedTo( theDependsOn, theColorSetting, theDiagram );
					bleedColorToGraphElsRelatedTo( theDependency, theColorSetting, theDiagram );
				}
			}
		}
	}

	private static void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, 
			String theColorSetting, 
			IRPDiagram onDiagram){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsRelatedToElement = 
				onDiagram.getCorrespondingGraphicElements( theEl ).toList();
		
		for (IRPGraphElement irpGraphElement : theGraphElsRelatedToElement) {
			
			irpGraphElement.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			IRPModelElement theModelObject = irpGraphElement.getModelObject();
			
			if (theModelObject != null){
				Logger.writeLine("Setting color to red for " + theModelObject.getName());
			}
		}
	}
	
	public IRPPackage getPackageForBlocks(){

		IRPPackage thePackage = FunctionalAnalysisSettings.getPkgNamedInFunctionalPackageTag(
				_contextEl, 
				tagNameForPackageForBlocks );
		
		return thePackage;
	}
	
	public IRPApplication get_rhpApp() {
		return _rhpApp;
	}

}


/**
 * Copyright (C) 2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
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