package designsynthesisplugin;

import functionalanalysisplugin.FunctionalAnalysisPlugin;
import generalhelpers.ConfigurationSettings;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;
import generalhelpers.UserInterfaceHelpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;

import com.telelogic.rhapsody.core.*;

public class PortCreator {

	// test only
	public static void main(String[] args) {

		IRPModelElement theSelectedEl = FunctionalAnalysisPlugin.getRhapsodyApp().getSelectedElement();
		
		ConfigurationSettings configSettings = new ConfigurationSettings(
				"SysMLHelper.properties", 
				"SysMLHelper_MessagesBundle" );
		
		if( theSelectedEl instanceof IRPAttribute ){
			createPublishFlowportFor( (IRPAttribute) theSelectedEl, configSettings );
		}
	}
	
	public static void createPublishFlowportsFor(
			List<IRPModelElement> theSelectedEls,
			ConfigurationSettings theConfigSettings ){

		for (IRPModelElement selectedEl : theSelectedEls) {

			if (selectedEl instanceof IRPAttribute){

				IRPAttribute theAttribute = (IRPAttribute)selectedEl;
				Logger.writeLine(theAttribute, "is being processed");

				createPublishFlowportFor(theAttribute, theConfigSettings);
			} else {
				Logger.writeLine("Doing nothing for " + Logger.elementInfo(selectedEl) 
						+ " as it is not an Atttribute");
			}
		}
	}

	public static IRPSysMLPort createPublishFlowportFor(
			IRPAttribute theAttribute,
			ConfigurationSettings theConfigSettings ){

		IRPSysMLPort thePort = getExistingOrCreateNewFlowPortFor( theAttribute );

		if( thePort != null ){
			
			String theDesiredPortName = theAttribute.getName();
			
			// does port require renaming?
			if( !thePort.getName().equals( theDesiredPortName ) ){
				
				Logger.writeLine( "Renaming " + Logger.elementInfo( thePort ) + " to " + theDesiredPortName );
				thePort.setName(theDesiredPortName);
			}
			
			thePort.setType( theAttribute.getType() );
			thePort.setPortDirection( "Out" );

			IRPStereotype existingSubscribeStereotype = 
					GeneralHelpers.getStereotypeCalled( "subscribe", theAttribute );

			if( existingSubscribeStereotype != null ){
				theAttribute.removeStereotype( existingSubscribeStereotype );
			}
			
			cleanUpAutoRippleDependencies( theAttribute );
			applyStereotypeAndChangeBackToValuePropertyIfNeeded( theAttribute, "publish" );
			TraceabilityHelper.copyRequirementTraceabilityFrom( theAttribute, thePort );
			thePort.highLightElement();
			theAttribute.highLightElement();
			
			AutoConnectFlowPortsPanel.launchThePanel( theAttribute, theConfigSettings );

		} else {
			Logger.writeLine("Error in createPublishFlowportFor, no port was created");
		}
		
		return thePort;
	}

	private static void applyStereotypeAndChangeBackToValuePropertyIfNeeded(
			IRPAttribute theAttribute,
			String andStereotype ){
				
		Logger.writeLine( "Applying «" + andStereotype + "» stereotype to " + Logger.elementInfo( theAttribute ) );

		GeneralHelpers.applyExistingStereotype( andStereotype, theAttribute );
		
		// Switch ValueProeprty back if 8.2+
		IRPModelElement theValuePropertyStereotype = 
				GeneralHelpers.findElementWithMetaClassAndName( 
						"Stereotype", "ValueProperty", theAttribute.getProject() );
		
		if( theValuePropertyStereotype != null ){
			theAttribute.changeTo( "ValueProperty" );
		}
	}
	
	public static IRPSysMLPort createSubscribeFlowportFor(
			IRPAttribute theAttribute ){

		IRPSysMLPort thePort = getExistingOrCreateNewFlowPortFor( theAttribute );

		if( thePort != null ){

			thePort.setType( theAttribute.getType() );
			thePort.setPortDirection( "In" );

			IRPModelElement thePortOwner = thePort.getOwner();
			
			if( thePortOwner instanceof IRPClassifier ){
				
				IRPClassifier theClassifier = (IRPClassifier)thePortOwner;
				
				String theChangeEventName = "ch" + GeneralHelpers.capitalize(theAttribute.getName());
				
				Logger.writeLine("Ensure there is a change event reception called " + 
						theChangeEventName + " on " + Logger.elementInfo( theClassifier ) );
				
				IRPModelElement theReception = GeneralHelpers.getExistingOrCreateNewElementWith(
						theChangeEventName,
						"Reception",
						theClassifier );
				
				TraceabilityHelper.addAutoRippleDependencyIfOneDoesntExist( 
						theAttribute, theReception );
				
			} else {
				Logger.writeLine("Error in createSubscribeFlowportFor, element is not a classifier");
			}
			
			IRPStereotype existingStereotype = 
					GeneralHelpers.getStereotypeCalled( "publish", theAttribute );

			if( existingStereotype != null ){
				theAttribute.removeStereotype( existingStereotype );
			}

			cleanUpAutoRippleDependencies( theAttribute );
			applyStereotypeAndChangeBackToValuePropertyIfNeeded( theAttribute, "subscribe" );
			TraceabilityHelper.copyRequirementTraceabilityFrom( theAttribute, thePort );
			thePort.highLightElement();
			theAttribute.highLightElement();

		} else {
			Logger.writeLine( "Error in createSubscribeFlowportFor, no port was created" );
		}

		return thePort;
	}
	
	private static IRPSysMLPort getExistingOrCreateNewFlowPortFor(
			IRPAttribute theAttribute ){
		
		String theDesiredPortName = theAttribute.getName();
		
		IRPSysMLPort thePort = GeneralHelpers.getExistingFlowPort( theAttribute );

		if( thePort == null ){
			Logger.writeLine( "Creating an flowport for " + Logger.elementInfo( theAttribute ) + " called " + theDesiredPortName );
			thePort = (IRPSysMLPort) theAttribute.getOwner().addNewAggr( "FlowPort", theDesiredPortName );
			
			TraceabilityHelper.addAutoRippleDependencyIfOneDoesntExist( 
					theAttribute, thePort );
						
		} else if( !thePort.getName().equals( theDesiredPortName ) ){ // does port require renaming?
			
			Logger.writeLine( "Renaming " + Logger.elementInfo( thePort ) + " to " + theDesiredPortName );
			thePort.setName(theDesiredPortName);
		}
		
		return thePort;
	}

	public static void createSubscribeFlowportsFor(
			List<IRPModelElement> theSelectedEls ){

		for( IRPModelElement selectedEl : theSelectedEls ){

			if( selectedEl instanceof IRPAttribute ){

				IRPAttribute theAttribute = (IRPAttribute)selectedEl;
				Logger.writeLine(theAttribute, "is being processed");

				createSubscribeFlowportFor(theAttribute);
			} else {
				Logger.writeLine("Doing nothing for " + Logger.elementInfo(selectedEl) 
						+ " as it is not an Atttribute");
			}
		}
	}

	public static void deleteFlowPortAndRelatedEls(
			IRPSysMLPort theFlowPort ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theFlowPort.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPDependency && 
				GeneralHelpers.hasStereotypeCalled( "AutoRipple", theReference ) ){
				
				IRPDependency theDependency = (IRPDependency)theReference;
				IRPModelElement theDependent = theDependency.getDependent();
				
				if( theDependent instanceof IRPAttribute ){
					deleteAttributeAndRelatedEls( (IRPAttribute) theDependent );
				} else {
					UserInterfaceHelpers.showWarningDialog( 
							"Unable to delete as " + Logger.elementInfo( theFlowPort ) + " has no related attribute" );
				}
			}
		}
	}
	
	public static void deleteAttributeAndRelatedEls(
			IRPAttribute theAttribute ){
	
		Set<IRPModelElement> theRelatedEls = 
				TraceabilityHelper.getElementsThatHaveStereotypedDependenciesFrom(
						theAttribute, "AutoRipple" );
	
		JDialog.setDefaultLookAndFeelDecorated(true);

		String infoText = "Do you want to delete:\n" + "1. " + Logger.elementInfo( theAttribute );
				
		if( !theRelatedEls.isEmpty() ){
			
			int count = 1;
			
			infoText = infoText + "\n";
			
			for( IRPModelElement theRelatedEl : theRelatedEls ){
				
				count++;
				
				infoText = infoText + count + ". " + Logger.elementInfo( theRelatedEl ) + 
						"\n";
			}			
		}

		boolean answer = UserInterfaceHelpers.askAQuestion( infoText );
		
		if( answer ){
		
			for( IRPModelElement theRelatedEl : theRelatedEls ){
				Logger.writeLine( "Deleting " + Logger.elementInfo( theRelatedEl ) + " from the project" );

				try {
					theRelatedEl.deleteFromProject();

				} catch (Exception e) {
					Logger.writeLine("Exception in deleteAttributeAndRelatedEls, trying to delete " + Logger.elementInfo( theRelatedEl ) + " failed");
				}
			}

			Logger.writeLine( "Deleting " + Logger.elementInfo( theAttribute ) + " from the project" );
			theAttribute.getOwner().highLightElement();

			try {
				theAttribute.deleteFromProject();

			} catch (Exception e) {
				Logger.writeLine("Exception in deleteAttributeAndRelatedEls, trying to delete attribute failed");
			}			
		}	
	}
	
	private static void cleanUpAutoRippleDependencies(
			IRPAttribute theAttribute ){
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theAttribute.getDependencies().toList();

		Set<IRPDependency> dependenciesToDelete = new HashSet<IRPDependency>();

		for( IRPDependency theDependency : theExistingDeps ){

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn != null && 
				theDependsOn instanceof IRPModelElement &&
				GeneralHelpers.hasStereotypeCalled( "AutoRipple", theDependency ) ){

				IRPModelElement theElementOwner = theDependsOn.getOwner();
				IRPModelElement theAttributeOwner = theAttribute.getOwner();

				boolean isCheckOperation = 
						theDependsOn instanceof IRPOperation && theDependsOn.getName().contains( "check" );

				boolean isReception =
						theDependsOn.getUserDefinedMetaClass().equals( "Reception" );

				boolean isFlowPort =
						theDependsOn instanceof IRPSysMLPort;

				boolean isTransition =
						theDependsOn instanceof IRPTransition;
				
				if( isCheckOperation || isReception || isFlowPort || isTransition ){

					if( !theElementOwner.equals( theAttributeOwner ) ){

						Logger.writeLine( "Detected a need to delete the «AutoRipple» dependency to " + Logger.elementInfo( theDependsOn ) + 
								" owned by " + Logger.elementInfo( theElementOwner ) + ", as it is not owned by " + 
								Logger.elementInfo( theAttributeOwner ) );

						dependenciesToDelete.add( theDependency );
						theDependency.highLightElement();
					} else {
						Logger.writeLine( theDependsOn, "was found based on «AutoRipple» dependency");
					}
				}	
			}
		}

		if( !dependenciesToDelete.isEmpty() ){
			
			JDialog.setDefaultLookAndFeelDecorated(true);

			String infoText = "To maintain consistency the following «AutoRipple» dependencies will be deleted: " +
					"\n";

			int count = 0;
			
			for (IRPDependency theDependency : dependenciesToDelete) {
				
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				IRPModelElement theElementOwner = theDependsOn.getOwner();
				
				count++;
				
				infoText = infoText + count + ". " + Logger.elementInfo( theDependsOn ) + 
						" owned by " + Logger.elementInfo( theElementOwner ) + " \n";
			}

			Logger.writeLine( infoText );
			
			theAttribute.getOwner().highLightElement();
			
			for( IRPDependency theDependency : dependenciesToDelete ){	
				theDependency.deleteFromProject();
			}
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #095 23-AUG-2016: Turned off the "Do you want to add subscribe ports to other Blocks?" question (F.J.Chadburn)
    #123 25-NOV-2016: Improved Publish/Subscribe ports to clean up AutoRipple dependencies when doing copy/paste (F.J.Chadburn)
    #124 25-NOV-2016: Cleaned up unused code from PortCreator (F.J.Chadburn)
    #164 15-FEB-2017: Fixed .hep for Publish/Subscribe flow ports to work with ValueProperty's in 8.2 (F.J.Chadburn)
    #173 02-APR-2017: cleanUpAutoRippleDependencies now gives an information rather than warning dialog (F.J.Chadburn)
    #174 02-APR-2017: Improved flowPort creation to highlight port after creation (F.J.Chadburn)
    #175 02-APR-2017: Improved flowPort creation to copy req'ts traceability from attribute to flow-port (F.J.Chadburn)
    #180 29-MAY-2017: Added new Design Synthesis menu to Delete attribute and related elements (F.J.Chadburn)
    #181 29-MAY-2017: Replace dialog with Log message in cleanUpAutoRippleDependencies (F.J.Chadburn)
    #182 29-MAY-2017: Tweak to keep attribute selected after switching to publish or subscribe flow-port (F.J.Chadburn)
    #192 05-JUN-2017: Widened DeleteAttributeAndRelatedElementsMenu support to work with flow-ports as well (F.J.Chadburn)
    #193 05-JUN-2017: Added Transitions to «AutoRipple» list so they're also cleaned up when attribute is deleted (F.J.Chadburn)
    #213 09-JUL-2017: Add dialogs to auto-connect «publish»/«subscribe» FlowPorts for white-box simulation (F.J.Chadburn)
    #227 06-SEP-2017: Increased robustness to stop smart link panel using non new term version of <<refine>> (F.J.Chadburn)
    #228 06-SEP-2017: Refine subscribe port creation to include change event reception creation (F.J.Chadburn)

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
