package functionalanalysisplugin;

import java.util.List;

import generalhelpers.PopulatePkg;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.*;

public class PopulateFunctionalAnalysisPkg extends PopulatePkg {
	 
	public static void main(String[] args) {
	
		IRPApplication theApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theApp.getSelectedElement();
		
		if (theSelectedEl instanceof IRPPackage){
			IRPPackage thePackage = (IRPPackage) theSelectedEl;
			addNewActorToPackageUnderDevelopement( thePackage );
		}
	}
	
	public static void createFunctionalAnalysisPkg(IRPProject forProject){
		 
		final String rootPackageName = "FunctionalAnalysisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null){
			Logger.writeLine("Doing nothing: " + Logger.elementInfo( forProject ) + " already has package called " + rootPackageName);
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for executable MBSE. \n" +
		    		"It creates a nested package structure for executable 'interaction-based functional analysis',  \n" +
		    		"imports the appropriate profiles if not present, and sets default display and other options \n" +
		    		"to appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    	
		    	IRPModelElement theRequirementsAnalysisPkg = forProject.findElementsByFullName("RequirementsAnalysisPkg", "Package");
		    	
		    	if (theRequirementsAnalysisPkg==null){
		    		
				    int confirm = JOptionPane.showConfirmDialog(null, 
				    		"The project does not contain a root RequirementsAnalysisPkg. This package is used by the\n" +
				    		"plugin to populate the Actors for functional analysis simulation purposes.\n\n" +
				    		"Do you want to add a RequirementsAnalysisPkg.sbs from another model by reference?\n\n" + 
				    		"NOTE:\n" +
				    		"The recommendation is to create a folder that will contain both this project and its\n" +
				    		"referenced projects to treat them as a consistent project set. If you haven't done this\n" +
				    		"yet then consider cancelling and doing this first.\n\n",
				    		"Confirm",
				        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				    
				    if (confirm == JOptionPane.YES_OPTION){
				    	browseAndAddByReferenceIfNotPresent("RequirementsAnalysisPkg", forProject);
				    }
		    	}
		    	
		    	populateFunctionalAnalysisPkg(forProject);
		    	removeSimpleMenuStereotypeIfPresent(forProject);
		    	
		    	forProject.save();

		    } else {
		    	Logger.writeLine("Cancelled by user");
		    }
		}
	}
	
	static void populateFunctionalAnalysisPkg(IRPProject forProject) {
		
		addProfileIfNotPresent("SysML", forProject);		
		addProfileIfNotPresent("GlobalPreferencesProfile", forProject);
		addProfileIfNotPresent("RequirementsAnalysisProfile", forProject);
		addProfileIfNotPresent("FunctionalAnalysisProfile", forProject);
		
		forProject.changeTo("SysML");
		
		IRPPackage theFunctionalAnalysisPkg = addPackageFromProfileRpyFolder(forProject, "FunctionalAnalysisPkg");
		
		if (theFunctionalAnalysisPkg != null){
		
			deleteIfPresent( "Structure1", "StructureDiagram", forProject );
	    	deleteIfPresent( "Default", "Package", forProject );
	    	
	    	setProperty( forProject, "Browser.Settings.ShowPredefinedPackage", "True" );
	    	setProperty( forProject, "General.Model.AutoSaveInterval", "5" );
	    	setProperty( forProject, "General.Model.HighlightElementsInActiveComponentScope", "True" );
	    	setProperty( forProject, "General.Model.ShowModelTooltipInGE", "Enhanced" );
	    	setProperty( forProject, "General.Model.BackUps", "One" );
	    	
	    	createFunctionalBlockPackageHierarchy( theFunctionalAnalysisPkg );
		}
	}
	
	public static void createFunctionalBlockPackageHierarchy(IRPPackage theRootPackage){
		
		if (theRootPackage.getName().equals("FunctionalAnalysisPkg")){
			
			IRPPackage theRequirementsAnalysisPkg = (IRPPackage) theRootPackage.getProject().findElementsByFullName("RequirementsAnalysisPkg", "Package");
			
			if (theRequirementsAnalysisPkg == null){
				
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				JOptionPane.showMessageDialog(
						null,  
			    		"Unable to do functional block creation as this only works if the project contains a RequirementsAnalysisPkg.",
			    		"Information",
			    		JOptionPane.INFORMATION_MESSAGE);
			} else {
				
				createFunctionalAnalysisPkg(theRootPackage, theRequirementsAnalysisPkg);
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
	
	private static void createFunctionalAnalysisPkg(
			final IRPPackage theRootPackage,
			final IRPPackage theRequirementsAnalysisPkg) {
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theActors = 
			theRequirementsAnalysisPkg.getNestedElementsByMetaClass("Actor", 1).toList();
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		String introText = "This SysML-Toolkit helper sets up a nested package hierarchy for the functional analysis\n" +
				"of a block from the perspective of the actors in the system. The initial structure will be\n" +
				"created based on the " + theActors.size() + " actor(s) identified in the RequirementsAnalysisPkg called: " +
				"\n";
		
		for (IRPModelElement theActor : theActors) {
			introText = "\t" + introText + theActor.getName() + "\n";
		}
		
		int response = JOptionPane.showConfirmDialog(null, 
				 introText +
				"\nDo you want to proceed?", "Confirm",
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if (response == JOptionPane.YES_OPTION) {
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );

					JFrame frame = new JFrame("Populate package hierarchy for an analysis block");
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateFunctionalBlockPackagePanel thePanel = 
							new CreateFunctionalBlockPackagePanel(
									theRootPackage, theRequirementsAnalysisPkg);

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		}
	}
	
	public static void addNewActorToPackageUnderDevelopement(IRPModelElement theSelectedEl){
		
		final String rootPackageName = "FunctionalAnalysisPkg";
		
		final IRPModelElement theRootPackage = 
				theSelectedEl.getProject().findElementsByFullName(rootPackageName, "Package");
		
		final IRPPackage thePackageUnderDev = 
				FunctionalAnalysisSettings.getPackageUnderDev( theSelectedEl.getProject() );

		final IRPClass theBlockUnderDev = 
				FunctionalAnalysisSettings.getBlockUnderDev( theSelectedEl.getProject() );
		
		Logger.writeLine("Add new actor part to " + Logger.elementInfo( thePackageUnderDev ) + " was invoked");
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame("Create new actor connected to " 
						+ theBlockUnderDev.getUserDefinedMetaClass() + " called " + theBlockUnderDev.getName());
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateNewActorPanel thePanel = 
						new CreateNewActorPanel( theBlockUnderDev.getName(), (IRPPackage)theRootPackage);

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
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
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

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