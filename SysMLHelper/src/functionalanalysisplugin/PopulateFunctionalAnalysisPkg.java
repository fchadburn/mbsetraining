package functionalanalysisplugin;

import generalhelpers.ConfigurationSettings;
import generalhelpers.CreateGatewayProjectPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.PopulatePkg;
import generalhelpers.UserInterfaceHelpers;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import requirementsanalysisplugin.PopulateRequirementsAnalysisPkg;
import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class PopulateFunctionalAnalysisPkg extends PopulatePkg {
	
	public enum SimulationType {
	    FullSim, SimpleSim, NoSim
	}
	
	public static void main(String[] args) {
	
		IRPApplication theApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theApp.getSelectedElement();
		
		if (theSelectedEl instanceof IRPProject){

			copyActivityDiagrams( (IRPProject) theSelectedEl );
		}
	}
	
	public static void createFunctionalAnalysisPkg(
			IRPProject forProject, 
			final SimulationType withSimulationType ){
		 
		final String rootPackageName = "FunctionalAnalysisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null && theExistingPkg instanceof IRPPackage ){
			
	    	boolean answer = UserInterfaceHelpers.askAQuestion( 
	    			"This project already has a " + Logger.elementInfo( theExistingPkg ) + ". \n" +
	    			"Do you want to create a " + withSimulationType + " package stucture underneath it?");
	    	
	    	if( answer==true ){
		    	createFunctionalBlockPackageHierarchy( (IRPPackage)theExistingPkg, withSimulationType );
	    	}

	    	ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    String introText;
		    
		    if( withSimulationType==SimulationType.NoSim){
			    introText = 
			    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for MBSE. \n" +
			    		"It creates a nested package structure for simple functional analysis, imports the \n" +
			    		"appropriate profiles if not present, and sets default display and other options \n" +
			    		"to appropriate values for the task using Rhapsody profile and property settings. \n" +
			    		"This will remove the SimpleMenu stereotype if applied.\n\n" +
			    		"Do you want to proceed?";		    	
		    } else {
			    introText = 
			    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for executable MBSE. \n" +
			    		"It creates a nested package structure for executable 'state-based functional analysis',  \n" +
			    		"imports the appropriate profiles if not present, and sets default display and other options \n" +
			    		"to appropriate values for the task using Rhapsody profile and property settings. \n" +
			    		"This will remove the SimpleMenu stereotype if applied.\n\n" +
			    		"Do you want to proceed?";
		    }
		    
		    int response = JOptionPane.showConfirmDialog(
		    		null, 
		    		introText, 
		    		"Confirm",
		    		JOptionPane.YES_NO_OPTION,
		    		JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    	
		    	IRPModelElement theRequirementsAnalysisPkg = 
		    			forProject.findElementsByFullName("RequirementsAnalysisPkg", "Package");
		    	
		    	if (theRequirementsAnalysisPkg != null){
		    		
			    	populateFunctionalAnalysisPkg(forProject, withSimulationType);
			    	removeSimpleMenuStereotypeIfPresent(forProject);
			    	
			    	forProject.save();
			    	
		    	} else { // theRequirementsanalysisPkg == null
		    		
				    int confirm = JOptionPane.showConfirmDialog(null, 
				    		"The project does not contain a root RequirementsAnalysisPkg. This package is used by the\n" +
				    		"plugin/method to populate the actors for functional analysis simulation purposes and/or \n" +
				    	    "higher-level requirements for traceability purposes. \n\n" +
				    		"Do you want to add a RequirementsAnalysisPkg.sbs from another model by reference?\n\n" + 
				    		"NOTE:\n" +
				    		"The recommendation is to create a folder that will contain both this project and its\n" +
				    		"referenced projects to treat them as a consistent project set. If you haven't done this\n" +
				    		"yet then consider cancelling and doing this first. The unit will be added by relative \n"+
				    		"path, hence locating the models in a common root folder is recommended to enable \n"+
				    		"sharing across file systems as a consistent set of projects.\n\n " + 
				    		"Clicking 'Yes' will allow you to select a RequirementsAnalysisPkg by reference.\n\n" +
				    		"Clicking 'No' will create a RequirementsAnalysisPkg structure as the starting point in \n" + 
				    		"this project (so you can import higher-level requirements and define actors). You will \n" +
				    		"then be able to re-run FunctionalAnalysisPkg creation once the actors and use case \n" +
				    		"context have been defined. \n\n"+
				    		"Clicking 'Cancel' will do nothing.\n\n",
				    		"Confirm choice",
				        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				    
				    if (confirm == JOptionPane.YES_OPTION){
				    	browseAndAddByReferenceIfNotPresent("RequirementsAnalysisPkg", forProject, true);
				    	
				    	populateFunctionalAnalysisPkg(forProject, withSimulationType);
				    	removeSimpleMenuStereotypeIfPresent(forProject);
				    	forProject.save();
				    	
				    } else if (confirm == JOptionPane.NO_OPTION){
					    
				    	PopulateRequirementsAnalysisPkg.populateRequirementsAnalysisPkg(forProject);		
						CreateGatewayProjectPanel.launchThePanel( forProject, "^RequirementsAnalysisPkg.rqtf$" );
							    
				    } else {
				    	Logger.writeLine("Cancelled by user");
				    }
		    	}

		    } else {
		    	Logger.writeLine("Cancelled by user");
		    }
		}
	}
		
	static void populateFunctionalAnalysisPkg(
			IRPProject forProject, final SimulationType withSimulationType ) {
		
		addProfileIfNotPresent("SysML", forProject);		
		addProfileIfNotPresent("GlobalPreferencesProfile", forProject);
		addProfileIfNotPresent("RequirementsAnalysisProfile", forProject);
		
		IRPPackage theFunctionalAnalysisPkg = forProject.addPackage( "FunctionalAnalysisPkg" );
		
		if( withSimulationType==SimulationType.FullSim ){		

			addProfileIfNotPresentAndMakeItApplied("FunctionalAnalysisProfile", theFunctionalAnalysisPkg);
			addProfileIfNotPresentAndMakeItApplied("DesignSynthesisProfile", theFunctionalAnalysisPkg);
		
		} else { // withSimulationType==SimulationType.SimpleSim || withSimulationType==SimulationType.NoSim
			
			addProfileIfNotPresentAndMakeItApplied("FunctionalAnalysisSimpleProfile", theFunctionalAnalysisPkg);
			addProfileIfNotPresentAndMakeItApplied("DesignSynthesisProfile", theFunctionalAnalysisPkg);
		}
		
		if( theFunctionalAnalysisPkg != null ){
			
			addPackageFromProfileRpyFolder( "BasePkg", forProject, true );
		
			deleteIfPresent( "Structure1", "StructureDiagram", forProject );
	    	deleteIfPresent( "Default", "Package", forProject );
	    	
	    	ConfigurationSettings theConfigSettings = ConfigurationSettings.getInstance();
	    	
	    	theConfigSettings.setPropertiesValuesRequestedInConfigFile( 
	    			forProject,
	    			"setPropertyForFunctionalAnalysisModel" );
	    		    	
	    	createFunctionalBlockPackageHierarchy( theFunctionalAnalysisPkg, withSimulationType );
		}
	}
	
	public static void createFunctionalBlockPackageHierarchy(
			IRPPackage theRootPackage, 
			final SimulationType withSimulationType ){
		
		if (theRootPackage.getName().equals("FunctionalAnalysisPkg")){
			
			IRPPackage theRequirementsAnalysisPkg = (IRPPackage) theRootPackage.getProject().findElementsByFullName("RequirementsAnalysisPkg", "Package");
			
			if (theRequirementsAnalysisPkg == null && withSimulationType==SimulationType.FullSim){
				
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				JOptionPane.showMessageDialog(
						null,  
			    		"Unable to do functional block creation as this only works if the project contains a RequirementsAnalysisPkg.",
			    		"Information",
			    		JOptionPane.INFORMATION_MESSAGE);
			} else {
				
				CreateFunctionalBlockPackagePanel.launchThePanel(
						theRootPackage, 
						theRequirementsAnalysisPkg, 
						withSimulationType );
			}
		    
		} else {
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    JOptionPane.showMessageDialog(
		    		null,  
		    		"This operation only works if you right-click the FunctionalAnalysisPkg.",
		    		"Warning",
		    		JOptionPane.WARNING_MESSAGE);	    
		}
	}
	
	public static void addNewActorToPackageUnderDevelopement(
			IRPModelElement theSelectedEl ){
		
		final String rootPackageName = "FunctionalAnalysisPkg";
		
		IRPProject theProject = theSelectedEl.getProject();
		
		final IRPModelElement theRootPackage = 
				theProject.findElementsByFullName( rootPackageName, "Package" );
		
		if( theRootPackage != null ){
			CreateNewActorPanel.launchThePanel( theProject );
		}
	}
	
	public static void copyActivityDiagrams(IRPProject forProject){
		
    	IRPModelElement theRequirementsAnalysisPkg = 
    			forProject.findElementsByFullName("RequirementsAnalysisPkg", "Package");
     	
    	if (theRequirementsAnalysisPkg==null){
    		
			JDialog.setDefaultLookAndFeelDecorated(true);

			JOptionPane.showMessageDialog(
					null,  
					"The project does not contain a root RequirementsAnalysisPkg. This package is used by the\n" +
				    "plugin to populate the Activity Diagrams for functional analysis simulation purposes.\n\n",
					"Warning",
					JOptionPane.WARNING_MESSAGE);	
    	} else {
    		
    		IRPPackage theWorkingPackage = 
    				FunctionalAnalysisSettings.getWorkingPkgUnderDev( forProject );
    		
    		if (theWorkingPackage != null){
        		CopyActivityDiagramsPanel.launchThePanel(
        				theRequirementsAnalysisPkg, 
        				theWorkingPackage);    			
    		} else {
    			Logger.writeLine("Error in copyActivityDiagrams, no working package was found");
    		}
    	}
	}
	
	public static void switchFunctionalAnalysisPkgProfileFrom(
			String theProfileName,
			String toTheProfileName,
			IRPProject forProject ) {
		
		final String rootPackageName = "FunctionalAnalysisPkg";

		IRPModelElement theFunctionalAnalysisPkg = 
				forProject.findElementsByFullName( rootPackageName, "Package" );
		
		if( theFunctionalAnalysisPkg==null ){
			
			Logger.writeLine( "Doing nothing: " + Logger.elementInfo( forProject ) + " does not have a " + rootPackageName );
		} else {

			String infoMsg =  "Do you want to change the menus by replacing the profile called '" + theProfileName + "' \n " +
					 		  "with the profile called '" + toTheProfileName + "'?" + "\n\n" +
					          "Note: After running this you will need to close Rhapsody completely and re-open the project for the new menus to appear.";
			
			boolean result = UserInterfaceHelpers.askAQuestion( infoMsg );
			
			if( result==true ){
				
				addProfileIfNotPresentAndMakeItApplied( toTheProfileName, (IRPPackage)theFunctionalAnalysisPkg );
				
				IRPModelElement theProfileToDelete = forProject.findAllByName( theProfileName, "Profile" );
				
				if( theProfileToDelete==null ){
					
					Logger.writeLine("Unable to find a profile called " + theProfileName + " to delete");
				} else {
					theProfileToDelete.deleteFromProject();
				}
			}
		}
	}
	
	public static void switchToMoreDetailedAD(
			IRPActivityDiagram theDiagram ) {
		
		final String theStereotypeName = "MoreDetailedAD";
		
		if( GeneralHelpers.hasStereotypeCalled( theStereotypeName, theDiagram ) ){
			
			Logger.writeLine( "Doing nothing as diagram already has the stereotype «" + theStereotypeName + "» applied." );
		
		} else {
			
			theDiagram.addStereotype( theStereotypeName, "ActivityDiagramGE" );
			IRPFlowchart theFC = theDiagram.getFlowchart();
			theFC.setIsAnalysisOnly( 1 );
			
			if( theDiagram.isOpen()==1 ){
				theDiagram.closeDiagram();
			}
			
			Logger.writeLine( "Applied stereotype «" + theStereotypeName + "» to " + 
					Logger.elementInfo( theDiagram ) + " to add additional tools to the toolbar" );
			
			setProperty( theDiagram.getFlowchart(), "Activity_diagram.AcceptEventAction.ShowNotation", "Event" );
			setProperty( theDiagram.getFlowchart(), "Activity_diagram.SendAction.ShowNotation", "Event" );		
		}
	}

	public static void addNewBlockPartToPackageUnderDevelopement(
			IRPModelElement theSelectedEl ){
		
		final String rootPackageName = "FunctionalAnalysisPkg";
		
		IRPProject theProject = theSelectedEl.getProject();
		
		final IRPModelElement theRootPackage = 
				theProject.findElementsByFullName( rootPackageName, "Package" );
		
		if( theRootPackage != null ){
			CreateNewBlockPartPanel.launchThePanel( theProject );
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #008 05-MAY-2016: Fix the OMROOT problem with add profile functionality
    #010 08-MAY-2016: Remove white-space from actor names (F.J.Chadburn)
    #014 10-MAY-2016: Fix Component/Configuration creation to include derived and web-enabled settings (F.J.Chadburn)
    #018 11-MAY-2016: Provide advisory before add by reference of an external RequirementsAnalysisPkg (F.J.Chadburn)
    #019 15-MAY-2016: Improvements to Functional Analysis Block default naming approach (F.J.Chadburn)
    #023 30-MAY-2016: Added form to support validation checks for analysis block hierarchy creation (F.J.Chadburn) 
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #027 31-MAY-2016: Add new menu to launch dialog to copy Activity Diagrams (F.J.Chadburn)
    #045 03-JUL-2016: Fix CopyActivityDiagramsPanel capability (F.J.Chadburn)
    #047 06-JUL-2016: Tweaked properties and added options to switch to MoreDetailedAD automatically (F.J.Chadburn)
    #059 13-JUL-2016: Improvements so ADs in FunctionalAnalysisPkg now include full tools/menus (F.J.Chadburn)
	#061 17-JUL-2016: Ensure BasePkg is added by reference from profile to aid future integration (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #091 23-AUG-2016: Turn off the Activity::General::AutoSelectControlOrObjectFlow property by default (F.J.Chadburn)
    #100 14-SEP-2016: Add option to create RequirementsAnalysisPkg if FunctionalAnalysisPkg not possible (F.J.Chadburn)
    #111 13-NOV-2016: Added new Simple Sim (Guard only) functional analysis structure option (F.J.Chadburn)
    #112 13-NOV-2016: Added new No Sim functional analysis structure option (F.J.Chadburn)
    #114 13-NOV-2016: DesignSynthesisProfile to create publish/subscribe flow ports now added by default (F.J.Chadburn)
    #115 13-NOV-2016: Removed use of isEnableBlockSelectionByUser tag and <<LogicalSystem>> by helper (F.J.Chadburn)
    #126 25-NOV-2016: Fixes to CreateNewActorPanel to cope better when multiple blocks are in play (F.J.Chadburn)
    #128 25-NOV-2016: Improved usability/speed of Copy AD dialog by providing user choice to open diagrams (F.J.Chadburn)
    #142 18-DEC-2016: Project properties now set via config.properties, e.g., to easily switch off backups (F.J.Chadburn)
    #146 18-DEC-2016: Allow block hierarchy creation from project level if there's an existing FA package (F.J.Chadburn)
    #147 18-DEC-2016: Fix Actor part creation not being created in correct place if multiple hierarchies (F.J.Chadburn)
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