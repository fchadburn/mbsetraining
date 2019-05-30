package executablembse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.ActivityDiagramChecker;
import requirementsanalysisplugin.LayoutHelper;
import requirementsanalysisplugin.MarkedAsDeletedPanel;
import requirementsanalysisplugin.MoveRequirements;
import requirementsanalysisplugin.NestedActivityDiagram;
import requirementsanalysisplugin.PopulateRelatedRequirementsPanel;
import requirementsanalysisplugin.PopulateTransitionRequirementsPanel;
import requirementsanalysisplugin.RenameActions;
import requirementsanalysisplugin.RequirementsHelper;
import requirementsanalysisplugin.SmartLinkPanel;
import sysmlhelperplugin.DependencySelector;
import executablembse.CreateFunctionalExecutablePackagePanel;
import executablembse.CreateUseCasesPackagePanel;
import functionalanalysisplugin.CreateIncomingEventPanel;
import functionalanalysisplugin.CreateDerivedRequirementPanel;
import functionalanalysisplugin.CreateNewActorPanel;
import functionalanalysisplugin.CreateNewBlockPartPanel;
import functionalanalysisplugin.CreateOperationPanel;
import functionalanalysisplugin.CreateOutgoingEventPanel;
import functionalanalysisplugin.CreateTracedAttributePanel;
import functionalanalysisplugin.EventDeletion;
import functionalanalysisplugin.OperationCreator;
import functionalanalysisplugin.PopulateFunctionalAnalysisPkg;
import functionalanalysisplugin.SequenceDiagramHelper;
import functionalanalysisplugin.TestCaseCreator;
import functionalanalysisplugin.UpdateTracedAttributePanel;
import functionalanalysisplugin.PopulateFunctionalAnalysisPkg.SimulationType;
import generalhelpers.*; 

import com.telelogic.rhapsody.core.*;

public class ExecutableMBSE_RPUserPlugin extends RPUserPlugin {

	static protected IRPApplication _rhpApp = null;
	static protected ConfigurationSettings _configSettings = null;

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		// keep the application interface for later use
		_rhpApp = theRhapsodyApp;

		_configSettings = new ConfigurationSettings(
				"ExecutableMBSE.properties", 
				"ExecutableMBSE_MessagesBundle" );

		final String legalNotice = 
				"Copyright (C) 2015-2019  MBSE Training and Consulting Limited (www.executablembse.com)"
						+ "\n"
						+ "SysMLHelperPlugin is free software: you can redistribute it and/or modify "
						+ "it under the terms of the GNU General Public License as published by "
						+ "the Free Software Foundation, either version 3 of the License, or "
						+ "(at your option) any later version."
						+ "\n"
						+ "SysMLHelperPlugin is distributed in the hope that it will be useful, "
						+ "but WITHOUT ANY WARRANTY; without even the implied warranty of "
						+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
						+ "GNU General Public License for more details."
						+ "You should have received a copy of the GNU General Public License "
						+ "along with SysMLHelperPlugin. If not, see <http://www.gnu.org/licenses/>. "
						+ "Source code is made available on https://github.com/fchadburn/mbsetraining";

		String msg = "The ExecutableMBSE component of the SysMLHelperPlugin V" + _configSettings.getProperty("PluginVersion") + " was loaded successfully.\n" +
				legalNotice +
				"\nNew right-click 'MBSE Method' commands have been added.";		

		Logger.info( msg );

		ExecutableMBSE_RPApplicationListener listener = 
				new ExecutableMBSE_RPApplicationListener( 
						theRhapsodyApp, 
						"ExecutableMBSEProfile" );
		
		listener.connect( theRhapsodyApp );
	}

	public static IRPApplication getRhapsodyApp(){

		if( _rhpApp == null ){
			_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		}

		return _rhpApp;
	}

	public static IRPProject getActiveProject(){

		return getRhapsodyApp().activeProject();
	} 

	// called when the plug-in pop-up menu  is selected
	public void OnMenuItemSelect(
			String menuItem ){

		try {
			final String theAppID = 
					UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

			if( theAppID != null ){

				IRPApplication theRhpApp = 
						RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );

				IRPProject theRhpPrj = theRhpApp.activeProject();

				IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

				@SuppressWarnings("unchecked")
				List<IRPModelElement> theSelectedEls = 
				theRhpApp.getListOfSelectedElements().toList();

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theSelectedGraphEls = 
				theRhpApp.getSelectedGraphElements().toList();

				Logger.info( "Right-click menu item '" + menuItem + "' was called with " + 
						theSelectedEls.size() + " selected elements...");

				if( !theSelectedEls.isEmpty() ){

					if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateRAStructureMenu" ) ) ){

						if( theSelectedEl instanceof IRPPackage ){

							ProfileVersionManager.checkAndSetProfileVersion( 
									false, 
									_configSettings,
									true );

							CreateUseCasesPackagePanel.launchTheDialog( 
									theAppID );
						} else {
							Logger.error( menuItem + " invoked out of context and only works for packages" );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SetupRAProperties" ) ) ){

						if( theSelectedEl instanceof IRPPackage ){

								_configSettings.setPropertiesValuesRequestedInConfigFile( 
										theRhpPrj,
										"setPropertyForExecutableMBSEModel" );
						} else {
							Logger.error( menuItem + " invoked out of context and only works for packages" );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateFullSimFAStructureMenu" ) ) ){

						if( theSelectedEl instanceof IRPProject ){

							boolean isProceed = UserInterfaceHelpers.askAQuestion(
									"The project will need some settings changed. \n" +
									"Do you want to proceed?");

							if( isProceed ){

								_configSettings.setPropertiesValuesRequestedInConfigFile( 
										theRhpPrj,
										"setPropertyForFunctionalAnalysisModel" );

								CreateFunctionalExecutablePackagePanel.launchThePanel( 
//										(IRPPackage) theSelectedEl, 
										SimulationType.FullSim );
							} else {
								Logger.info( "User chose not to proceed" );	
							}
						} else {
							Logger.error( menuItem + " invoked out of context and only works for packages" );
						} 
						
					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.QuickHyperlinkMenu"))){

						try { 
							IRPHyperLink theHyperLink = (IRPHyperLink) theSelectedEl.addNewAggr("HyperLink", "");
							theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");
							theHyperLink.highLightElement();
							theHyperLink.openFeaturesDialog(0);

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Quick hyperlink");
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependsOnElementsMenu" ) ) ){

						try { 					
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependsOnElementsFor( 
									theCombinedSet, null );

						} catch( Exception e ) {
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\All" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependentElementsMenu" ) ) ){

						try {
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependentElementsFor( 
									theCombinedSet, null );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\All" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependsOnDeriveOnlyElementsMenu" ) ) ){

						try { 					
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependsOnElementsFor( 
									theCombinedSet, "derive" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Derives" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependentDeriveOnlyElementsMenu" ) ) ){

						try {
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependentElementsFor( 
									theCombinedSet, "derive" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Derives" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependsOnSatisfyOnlyElementsMenu" ) ) ){

						try { 					
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependsOnElementsFor( 
									theCombinedSet, "satisfy" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Satisfies" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependentSatisfyOnlyElementsMenu" ) ) ){

						try {
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependentElementsFor( 
									theCombinedSet, "satisfy" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Satisfies" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependsOnVerifyOnlyElementsMenu" ) ) ){

						try { 					
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependsOnElementsFor( 
									theCombinedSet, "verify" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Verifies" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependentVerifyOnlyElementsMenu" ) ) ){

						try {
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependentElementsFor( 
									theCombinedSet, "verify" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Verifies" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependsOnRefineOnlyElementsMenu" ) ) ){

						try { 					
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependsOnElementsFor( 
									theCombinedSet, "refine" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Refines" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependentRefineOnlyElementsMenu" ) ) ){

						try {
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependentElementsFor( 
									theCombinedSet, "refine" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Refines" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependsOnDeriveReqtOnlyElementsMenu" ) ) ){

						try { 					
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependsOnElementsFor( 
									theCombinedSet, "deriveReqt" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Depends On element(s)\\Derive Requirement" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.SelectDependentDeriveReqtOnlyElementsMenu" ) ) ){

						try {
							Set<IRPModelElement> theCombinedSet = 
									GeneralHelpers.getSetOfElementsFromCombiningThe(
											theSelectedEls, theSelectedGraphEls );

							DependencySelector.selectDependentElementsFor( 
									theCombinedSet, "deriveReqt" );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking MBSE Method: General\\Select Dependent element(s)\\Derive Requirement" + e.getMessage() );
						}				
					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.SetupGatewayProjectMenu"))){

						if (theSelectedEl instanceof IRPProject){
							try { 
								CreateGatewayProjectPanel.launchThePanel( (IRPProject)theSelectedEl, ".*.rqtf$", _configSettings );

							} catch (Exception e) {
								Logger.error("Error: Exception in OnMenuItemSelect when invoking CreateGatewayProjectPanel.launchThePanel");
							}					
						}
					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.AddRelativeUnitMenu"))){

						try { 
							RelativeUnitHandler.browseAndAddUnit( theSelectedEl.getProject(), true );

						} catch (Exception e) {
							Logger.error("Error: Exception in OnMenuItemSelect when invoking RelativeUnitHandler.browseAndAddUnit");
						}	

					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.CreateNestedADMenu" ) ) ){

						try {
							NestedActivityDiagram.createNestedActivityDiagramsFor( theSelectedEls );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking NestedActivityDiagram.createNestedActivityDiagramsFor" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.ReportOnNamingAndTraceabilityMenu" ) ) ){

						try {
							ActivityDiagramChecker.createActivityDiagramCheckersFor( theSelectedEls );

						} catch( Exception e ){
							Logger.error("Exception in OnMenuItemSelect when invoking new ActivityDiagramChecker.createActivityDiagramCheckersFor e=" + e.getMessage() );
						}


					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.MoveUnclaimedReqtsMenu" ) ) ){

						try {
							MoveRequirements.moveUnclaimedRequirementsReadyForGatewaySync( 
									theSelectedEls, 
									theRhpPrj );

						} catch( Exception e ){
							Logger.error( "Error: Exception in OnMenuItemSelect when invoking MoveRequirements.moveUnclaimedRequirementsReadyForGatewaySync, e=" + e.getMessage() );
						}


					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.CreateNewRequirementMenu" ) ) ){

						try {
							RequirementsHelper.createNewRequirementsFor( theSelectedGraphEls );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RequirementsHelper.createNewRequirementsFor e=" + e.getMessage() );
						}

					} else if (menuItem.equals( _configSettings.getString( 
							"executablembseplugin.PerformRenameInBrowserMenu" ))){

						try {				
							RenameActions.performRenamesFor( theSelectedEls );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking RenameActions.performRenamesFor e=" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.UpdateNestedADNamesMenu" ) ) ){

							NestedActivityDiagram.renameNestedActivityDiagramsFor( 
									theSelectedEls );

					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.DeleteTaggedAsDeletedAtHighLevelMenu" ))){

						try {
							MarkedAsDeletedPanel.launchThePanel( theSelectedEls );

						} catch (Exception e) {
							Logger.error("Error: Exception in OnMenuItemSelect when invoking MarkedAsDeletedPanel.launchThePanel e=" + e.getMessage() );
						}
					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.StartLinkMenu" ) ) ){

						try {
							SmartLinkPanel.selectStartLinkEls( 
									theSelectedEls, 
									theSelectedGraphEls );

						} catch( Exception e ){
							Logger.error( "Error: Exception in OnMenuItemSelect when invoking " +
									"SmartLinkPanel.selectStartLinkEls, e=" + e.getMessage() );
						}
					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.EndLinkMenu" ) ) ){

						try {				
							SmartLinkPanel.launchTheEndLinkPanel( 
									theSelectedEls, 
									theSelectedGraphEls );

						} catch (Exception e) {
							Logger.error( "Error: Exception in OnMenuItemSelect when invoking " +
									"SmartLinkPanel.launchTheEndLinkPanel, e=" + e.getMessage() );
						}
					} else if (menuItem.equals(_configSettings.getString( "executablembseplugin.RollUpTraceabilityUpToTransitionLevel" ))){

						try {
							if( theSelectedGraphEls != null ){
								IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get( 0 );
								PopulateTransitionRequirementsPanel.launchThePanel( theSelectedGraphEl );
							}

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking " +
									"PopulateTransitionRequirementsPanel.launchThePanel e=" + e.getMessage() );
						}	
					} else if( menuItem.equals( _configSettings.getString( 
							"executablembseplugin.layoutDependencies" ) ) ){

						try {
							if( theSelectedGraphEls.size() > 0 ){

								LayoutHelper.centerDependenciesForTheGraphEls( 
										theSelectedGraphEls );

							} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagramGE" ) ){

								LayoutHelper.centerDependenciesForTheDiagram( 
										(IRPDiagram) theSelectedEl );

							} else if( theSelectedEl.getMetaClass().equals( "ActivityDiagram" ) ){

								@SuppressWarnings("unchecked")
								List<IRPModelElement> theDiagrams = 
									theSelectedEl.getNestedElementsByMetaClass( 
											"ActivityDiagramGE", 0 ).toList();

								if( theDiagrams.size()==1 ){

									LayoutHelper.centerDependenciesForTheDiagram( 
											(IRPDiagram) theDiagrams.get( 0 ) );
								} else {
									Logger.error( "Error in OnMenuItemSelect, unable to find an ActivityDiagramGE" );
								}

							} else if( theSelectedEl instanceof IRPDiagram ){

								LayoutHelper.centerDependenciesForTheDiagram( 
										(IRPDiagram) theSelectedEl );

							} else if( theSelectedEl instanceof IRPPackage ){

								LayoutHelper.centerDependenciesForThePackage( 
										(IRPPackage) theSelectedEl );
							}

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking LayoutHelper, e=" + e.getMessage() );
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.PopulateRequirementsForSDsMenu"))){

						if (theSelectedEl instanceof IRPSequenceDiagram){
							try {
								PopulateRelatedRequirementsPanel.launchThePanel( (IRPSequenceDiagram) theSelectedEl );

							} catch (Exception e) {
								Logger.error("Error: Exception in OnMenuItemSelect when invoking SequenceDiagramHelper.populateRequirementsForSequenceDiagramsBasedOn" );
							}
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.UpdateVerificationDependenciesForSDsMenu"))){

						if (!theSelectedEls.isEmpty()){
							try {
								SequenceDiagramHelper.updateVerificationsForSequenceDiagramsBasedOn( theSelectedEls );

							} catch (Exception e) {
								Logger.error("Error: Exception in OnMenuItemSelect when invoking SequenceDiagramHelper.updateVerificationsForSequenceDiagramsBasedOn e=" + e.getMessage() );
							}
						}				

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateIncomingEventMenu" ) ) ){

						try {
							CreateIncomingEventPanel.launchThePanel();
							
						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking " +
									"CreateIncomingEventPanel.launchThePanel(), e=" + e.getMessage() );
						}
					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateAnOperationMenu" ) ) ){

						try {
							CreateOperationPanel.launchThePanel();

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking " +
									"CreateOperationPanel.launchThePanel()" );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateOutgoingEventMenu"))){

						try {
							CreateOutgoingEventPanel.launchThePanel( _configSettings );

						} catch( Exception e ){
							Logger.error( "Exception in OnMenuItemSelect when invoking " +
									"CreateOutgoingEventPanel.launchThePanel(), e=" + e.getMessage() );
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateAttributeMenu" ) ) ){


						try{
							CreateTracedAttributePanel.launchThePanel( _configSettings );

						} catch( Exception e ){
							Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking " +
									"CreateOperationPanel.launchThePanel e=" + e.getMessage() );
						}

						//						if( theSelectedGraphEls.isEmpty() && ( 
						//								theSelectedEl instanceof IRPClass ||
						//								theSelectedEl instanceof IRPInstance ||
						//								theSelectedEl instanceof IRPDiagram ) ){
						//
						//							Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
						//
						//							CreateTracedAttributePanel.launchThePanel(
						//									null,
						//									theSelectedEl, 
						//									theReqts, 
						//									theRhpPrj );
						//
						//						} else if( !theSelectedGraphEls.isEmpty() ){
						//							try {
						//								CreateTracedAttributePanel.createSystemAttributesFor( 
						//										theRhpPrj, theSelectedGraphEls );
						//
						//							} catch( Exception e ){
						//								Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking CreateTracedAttributePanel.createSystemAttributeFor");
						//							}
						//						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.UpdateAttributeOrCheckOpMenu" ) ) ){

						if( theSelectedEl instanceof IRPAttribute ){
							try {
								Set<IRPRequirement> theReqts = 
										TraceabilityHelper.getRequirementsThatTraceFrom( 
												theSelectedEl, false );

								UpdateTracedAttributePanel.launchThePanel( 
										(IRPAttribute)theSelectedEl, 
										theReqts, 
										theRhpPrj );

							} catch (Exception e) {
								Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking UpdateTracedAttributePanel.launchThePanel" );
							}
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CreateEventForAttributeMenu" ) ) ){

						if( theSelectedEl instanceof IRPAttribute ){
							try {
								//								Set<IRPRequirement> theReqts = 
								//										TraceabilityHelper.getRequirementsThatTraceFrom( 
								//												theSelectedEl, false );
								//
								//								CreateIncomingEventPanel.launchThePanel( 
								//										null, 
								//										(IRPAttribute)theSelectedEl, 
								//										theReqts, 
								//										theRhpPrj );

							} catch (Exception e) {
								Logger.writeLine( "Error: Exception in OnMenuItemSelect when invoking UpdateTracedAttributePanel.launchThePanel");
							}
						}				

					} else if( menuItem.equals(_configSettings.getString("executablembseplugin.DeriveDownstreamRequirementMenu"))){

						if (!theSelectedGraphEls.isEmpty()){
							try {
								CreateDerivedRequirementPanel.deriveDownstreamRequirement( theSelectedGraphEls );

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking CreateDerivedRequirementPanel.launchThePanel");
							}
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.CreateNewTestCaseForTestDriverMenu"))){

						if (theSelectedEl instanceof IRPClass){
							try {
								OperationCreator.createTestCaseFor( (IRPClass) theSelectedEl );

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking OperationCreator.createTestCaseFor");
							}
						} else if (theSelectedEl instanceof IRPSequenceDiagram){

							try {
								TestCaseCreator.createTestCaseFor( (IRPSequenceDiagram) theSelectedEl );

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking TestCaseCreator.createTestCaseFor");
							}
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.AddNewActorToPackageMenu"))){

						if (theSelectedEl instanceof IRPPackage){
							try {
								CreateNewActorPanel.launchThePanel( );

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.addNewActorToPackageUnderDevelopement");
							}
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.AddNewBlockPartToPackageMenu"))){

						if (theSelectedEl instanceof IRPPackage || theSelectedEl instanceof IRPDiagram ){
							try {
								CreateNewBlockPartPanel.launchThePanel();

							} catch (Exception e) {
								Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.AddNewBlockPartToPackageMenu");
							}
						}

					} else if( menuItem.equals( _configSettings.getString(
							"executablembseplugin.CopyActivityDiagramsMenu" ) ) ){

						if( theSelectedEl instanceof IRPPackage ){
							
							try {
								PopulateFunctionalAnalysisPkg.copyActivityDiagrams( 
										(IRPPackage)theSelectedEl ); 

							} catch (Exception e ){
								Logger.error( "Error: Exception in OnMenuItemSelect when invoking PopulateFunctionalAnalysisPkg.copyActivityDiagrams " + e.getMessage() );
							}
						}							

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.DeleteEventsAndRelatedElementsMenu"))){

						try {
							EventDeletion.deleteEventAndRelatedElementsFor( theSelectedEls );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking EventDeletion.deleteEventAndRelatedElementsFor");
						}

						//					} else if (menuItem.equals(m_configSettings.getString("functionalanalysisplugin.SwitchMenusToMoreDetailedADMenu"))){
						//
						//						try {
						//							if( theSelectedEl instanceof IRPActivityDiagram ){
						//								
						//								IRPActivityDiagram theAD = (IRPActivityDiagram)theSelectedEl;
						//								
						//								int isOpen = theAD.isOpen();
						//								
						//								PopulateFunctionalAnalysisPkg.switchToMoreDetailedAD( 
						//										(IRPActivityDiagram)theSelectedEl );
						//								
						//								if( isOpen==1 ){
						//									theAD.highLightElement();
						//								}
						//							}
						//							
						//						} catch (Exception e) {
						//							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Switch menus to «MoreDetailedAD»");
						//						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.SwitchMenusToFullSim"))){

						try {
							PopulateFunctionalAnalysisPkg.switchFunctionalAnalysisPkgProfileFrom(
									"FunctionalAnalysisSimpleProfile", "FunctionalAnalysisProfile", theRhpPrj );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Switch menus to full sim");
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.SwitchMenusToSimpleSim"))){

						try {
							PopulateFunctionalAnalysisPkg.switchFunctionalAnalysisPkgProfileFrom(
									"FunctionalAnalysisProfile", "FunctionalAnalysisSimpleProfile", theRhpPrj );

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Switch menus to full sim");
						}

					} else if (menuItem.equals(_configSettings.getString("executablembseplugin.RecreateAutoShowSequenceDiagramMenu"))){

						try {
							if( theSelectedEl instanceof IRPSequenceDiagram ){

								SequenceDiagramHelper.updateLifelinesToMatchPartsInActiveBuildingBlock(
										(IRPSequenceDiagram) theSelectedEl );

							}	

						} catch (Exception e) {
							Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: Functional Analysis\\Re-create «AutoShow» sequence diagram");
						}

					} else {
						Logger.writeLine( theSelectedEl, " was invoked with menuItem='" + menuItem + "'");
					}
				}

				Logger.writeLine( "'" + menuItem + "' completed.");
			}
		} catch( Exception e ){
			Logger.error( "Exception in OnMenuItemSelect, e=" + e.getMessage() );
		}



	}

	// if true is returned the plugin will be unloaded
	public boolean RhpPluginCleanup() {

		_rhpApp = null;
		return true; // plug-in will be unloaded now (on project close)
	}

	@Override
	public void RhpPluginFinalCleanup() {
	}

	@Override
	public void RhpPluginInvokeItem() {

	}

	public static List<IRPRequirement> getRequirementsThatTraceFrom(
			IRPModelElement theElement){

		List<IRPRequirement> theReqts = new ArrayList<IRPRequirement>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

		for (IRPDependency theDependency : theExistingDeps) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){
				theReqts.add( (IRPRequirement) theDependsOn );
			}
		}

		return theReqts;
	}

	public String traceabilityReportHtml( IRPModelElement theModelEl ) {

		String retval = "";

		if( theModelEl != null ){

			List<IRPRequirement> theTracedReqts;

			if( theModelEl instanceof IRPDependency ){

				IRPDependency theDep = (IRPDependency) theModelEl;
				IRPModelElement theDependsOn = theDep.getDependsOn();

				if( theDependsOn != null && 
						theDependsOn instanceof IRPRequirement ){

					// Display text of the requirement that the dependency traces to
					theTracedReqts = new ArrayList<>();
					theTracedReqts.add( (IRPRequirement) theDependsOn );
				} else {
					theTracedReqts = getRequirementsThatTraceFrom( theModelEl );
				}
			} else {
				theTracedReqts = getRequirementsThatTraceFrom( theModelEl );
			}

			if( theTracedReqts.isEmpty() ){

				retval = "<br>This element has no traceability to requirements<br><br>";
			} else {
				retval = "<br><b>Requirements:</b>";				
				retval += "<table border=\"1\">";			
				retval += "<tr><td><b>ID</b></td><td><b>Specification</b></td></tr>";

				for( IRPRequirement theReqt : theTracedReqts ){
					retval += "<tr><td>" + theReqt.getName() + "</td><td>"+ theReqt.getSpecification() +"</tr>";
				}

				retval += "</table><br>";
			}				
		}

		return retval;
	}

	public String InvokeTooltipFormatter(String html) {

		String theOutput = html;

		try{
			@SuppressWarnings("rawtypes")
			List theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();

			if( theAppIDs.size() == 1 ){

				IRPProject theRhpProject = RhapsodyAppServer.getActiveRhapsodyApplication().activeProject();

				String guidStr = html.substring(1, html.indexOf(']'));

				IRPModelElement theModelEl = theRhpProject.findElementByGUID( guidStr );

				if( theModelEl != null ){
					guidStr = theModelEl.getGUID();
				}

				html = html.substring(html.indexOf(']') + 1);

				String thePart1 =  html.substring(
						0,
						html.indexOf("[[<b>Dependencies:</b>"));

				String thePart2 = traceabilityReportHtml( theModelEl );
				String thePart3 = html.substring(html.lastIndexOf("[[<b>Dependencies:</b>") - 1);

				theOutput = thePart1 + thePart2 + thePart3;
			}

		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in InvokeTooltipFormatter");
		}

		return theOutput;
	}

	@Override
	public void OnTrigger(String trigger) {

	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #249 29-MAY-2019: First official version of new ExecutableMBSEProfile  (F.J.Chadburn)

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