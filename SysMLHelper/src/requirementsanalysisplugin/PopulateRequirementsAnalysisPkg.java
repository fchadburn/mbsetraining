package requirementsanalysisplugin;

import generalhelpers.Logger;
import generalhelpers.PopulatePkg;
import generalhelpers.UserInterfaceHelpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;

public class PopulateRequirementsAnalysisPkg extends PopulatePkg {
	 
	public static void displayGraphicalPropertiesFor(IRPGraphElement theGraphEl){
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			String thePropertyname = theGraphicalProperty.getKey();
			String theValue = theGraphicalProperty.getValue();
			
			Logger.writeLine(thePropertyname + "=" + theValue);
		}
	}
	
	public static void copyGatewayFilesIfPresent(IRPProject forProject){
		
		String theProfileName = "SysMLHelperProfile";
		
		Logger.writeLine("Invoking copyGatewayFilesIfPresent");
		
		IRPProfile theProfile = (IRPProfile) forProject.findElementsByFullName(theProfileName, "Profile");
				
		if (theProfile != null){
			Logger.writeLine(theProfile, "was found");
			
			// #002 05-APR-2016: Improved robustness of copying .types file (F.J.Chadburn)
			IRPApplication myApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			String pathToSearch = myApp.getOMROOT() + "\\Profiles\\SysMLHelper\\SysMLHelper_rpy";
			
			Logger.writeLine("Looking in " + pathToSearch + " for a SysMLHelper.types file");
			
			File theTypesFile = FileHelper.getFileWith("SysMLHelper.types", pathToSearch);
			
			if (theTypesFile != null){
				FileHelper.copyTheFile(forProject, theTypesFile, forProject.getName() + ".types");
				
			    String rpyName = forProject.getName() + ".rpy";
			    
			    String fullPath = 
			    		forProject.getCurrentDirectory() + File.separator +
			    		forProject.getName()+"_rpy" + File.separator +
			    		forProject.getName() + ".rqtf";

			    try {
					File theRqtfFile = new File( fullPath );
					FileWriter fileWriter = new FileWriter( theRqtfFile );
					fileWriter.write("[Files]" + System.lineSeparator());
					fileWriter.write("Names=UML Model,UsageRequirementsPkg" + System.lineSeparator());
					fileWriter.write("RepositoryTool=Boost" + System.lineSeparator());
					fileWriter.write(System.lineSeparator());
					fileWriter.write("[UML Model]" + System.lineSeparator());
					fileWriter.write("Cover1=UML Model" + System.lineSeparator());
					fileWriter.write("Cover1Position=2200@1700" + System.lineSeparator());
					fileWriter.write("GraphicPosition=2200@2200" + System.lineSeparator());
					fileWriter.write("Variable1Name=addImages" + System.lineSeparator());
					fileWriter.write("Variable1Value=0" + System.lineSeparator());
					fileWriter.write("Variable2Name=addHLReqsDeleted" + System.lineSeparator());
					fileWriter.write("Variable2Value=0" + System.lineSeparator());
					fileWriter.write("Variable3Name=tagValueToDescription" + System.lineSeparator());
					fileWriter.write("Variable3Value=0" + System.lineSeparator());
					fileWriter.write("Variable4Name=notImportedRequirementsDocuments" + System.lineSeparator());
					fileWriter.write("Variable4Value=" + System.lineSeparator());
					fileWriter.write("Variable5Name=requirementsPackage" + System.lineSeparator());
					fileWriter.write("Variable5Value=" + forProject.getName() + "/Packages/RequirementsAnalysisPkg/Packages/RequirementsPkg¥UsageRequirementsPkg" + System.lineSeparator());
					fileWriter.write("Variable6Name=requirementDescription" + System.lineSeparator());
					fileWriter.write("Variable6Value=0" + System.lineSeparator());
					fileWriter.write("Variable7Name=addHLReqsModified" + System.lineSeparator());
					fileWriter.write("Variable7Value=0" + System.lineSeparator());
					fileWriter.write("Variable8Name=addHLReqsLocally" + System.lineSeparator());
					fileWriter.write("Variable8Value=0" + System.lineSeparator());
					fileWriter.write("Variable9Name=preserveHierarchy" + System.lineSeparator());
					fileWriter.write("Variable9Value=0" + System.lineSeparator());
					fileWriter.write("Variable10Name=lg" + System.lineSeparator());
					fileWriter.write("Variable10Value=C++" + System.lineSeparator());
					fileWriter.write("Variable11Name=createDependency" + System.lineSeparator());
					fileWriter.write("Variable11Value=0" + System.lineSeparator());
					fileWriter.write("Variable12Name=addHLRichText" + System.lineSeparator());
					fileWriter.write("Variable12Value=0" + System.lineSeparator());
					fileWriter.write("Type=Rhapsody SysML" + System.lineSeparator());
					fileWriter.write("Path=..\\" + rpyName + System.lineSeparator());
					fileWriter.write("AbsolutePath=" + forProject.getCurrentDirectory() + "\\" + rpyName + System.lineSeparator());
					fileWriter.write(System.lineSeparator());
					fileWriter.write("[UsageRequirementsPkg]" + System.lineSeparator());
					fileWriter.write("Cover1=UML Model" + System.lineSeparator());
					fileWriter.write("Cover1Position=2660@1950" + System.lineSeparator());
					fileWriter.write("GraphicPosition=3120@1700" + System.lineSeparator());
					fileWriter.write("Type=Usage Requirements" + System.lineSeparator());
					fileWriter.write("Path=" + System.lineSeparator());
					fileWriter.write("AbsolutePath=" + System.lineSeparator());
					fileWriter.flush();
					fileWriter.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}		     
			}    
		} else {
			Logger.writeLine("Error, unable to find Profile called " + theProfileName);
		}
	}
	
	public static void createRequirementsAnalysisPkg(IRPProject forProject){
		
		final String rootPackageName = "RequirementsAnalysisPkg";
		Boolean ok = true;
		
		IRPModelElement theExistingPkg = forProject.findElementsByFullName(rootPackageName, "Package");
		
		if (theExistingPkg != null){
			Logger.writeLine("Doing nothing: " + Logger.elementInfo( forProject ) + " already has package called " + rootPackageName);
			ok = false;
		}
		
		if (ok) {
			
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    
		    int response = JOptionPane.showConfirmDialog(null, 
		    		"This SysML-Toolkit helper is designed to set up a new Rhapsody project for simple activity\n" +
		    		"based use case analysis. It creates a nested package structure and use case diagram, imports\n" +
		    		"the appropriate profiles if not present, and sets default display and other options to \n" +
		    		"appropriate values for the task using Rhapsody profile and property settings.\n\n" +
		    		"Do you want to proceed?", "Confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    
		    if (response == JOptionPane.YES_OPTION) {
		    			
				boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"Do you initially want to simplify the 'Add New' menu for just\n" + 
				        "use case and requirements analysis?");
		    	
		    	populateRequirementsAnalysisPkg(forProject);
				
				if (theAnswer==true){
					applySimpleMenuStereotype(forProject);
				}
				
		    } else {
		    	Logger.writeLine("Cancelled by user");
		    }
		}
	}
	
	static void populateRequirementsAnalysisPkg(IRPProject forProject) {
		
		addProfileIfNotPresent("SysML", forProject);		
		addProfileIfNotPresent("GlobalPreferencesProfile", forProject);
		addProfileIfNotPresent("RequirementsAnalysisProfile", forProject);
		
		forProject.changeTo("SysML");
		
		IRPModelElement theRequirementsAnalysisPkg = addPackageFromProfileRpyFolder(forProject, "RequirementsAnalysisPkg" );
		
		if (theRequirementsAnalysisPkg != null){
			
			Logger.writeLine(theRequirementsAnalysisPkg, "was successfully copied from the SysMLHelper profile");
					
			deleteIfPresent( "Structure1", "StructureDiagram", forProject );
			deleteIfPresent( "Default", "Package", forProject );
			
			setProperty( forProject, "Browser.Settings.ShowPredefinedPackage", "False" );
			setProperty( forProject, "General.Model.AutoSaveInterval", "5" );
			setProperty( forProject, "General.Model.HighlightElementsInActiveComponentScope", "False" );
			setProperty( forProject, "General.Model.ShowModelTooltipInGE", "Enhanced" );			
			setProperty( forProject, "General.Model.BackUps", "One" );
			setProperty (forProject, "General.Model.RenameUnusedFiles", "True");
			
			copyGatewayFilesIfPresent( forProject );
			
			forProject.save();
			
			@SuppressWarnings("unchecked")
			List<IRPUseCaseDiagram> theUCDs = theRequirementsAnalysisPkg.getNestedElementsByMetaClass("UseCaseDiagram", 1).toList();
			
			for (IRPUseCaseDiagram theUCD : theUCDs) {
				Logger.writeLine(theUCD, "was added to the project");
				
				String oldName = theUCD.getName();
				String newName = oldName.replaceAll("ProjectName", forProject.getName());
				
				if (!newName.equals( oldName )){
					Logger.writeLine("Renaming " + oldName + " to " + newName);
					theUCD.setName( newName );
				}
				
				theUCD.highLightElement();
				theUCD.openDiagram();
			}	

		} else {
			Logger.writeLine("Error in createRequirementsAnalysisPkg, unable to add RequirementsAnalysisPkg package");
		}
	}

}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #002 05-APR-2016: Improved robustness of copying .types file (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    
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

