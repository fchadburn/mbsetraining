package generalhelpers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.telelogic.rhapsody.core.*;

public class CreateGatewayProjectPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static IRPProject m_Project;
	private GatewayFileParser m_ChosenTypesFile;
	private GatewayFileParser m_ChosenProjectFile;
	private List<GatewayDocumentPanel> m_GatewayDocumentPanel = new ArrayList<GatewayDocumentPanel>();
	
	// test only
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		if (theSelectedEl instanceof IRPProject){
			try { 
				CreateGatewayProjectPanel.launchThePanel( (IRPProject)theSelectedEl, ".*.rqtf$" );

			} catch (Exception e) {
				Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Quick hyperlink");
			}					
		}
	}
	
	public static void launchThePanel(
			final IRPProject forTheProject,
			final String lookingForRqtfTemplatesThatMatchRegEx ){
	
		String theReqtsPkgExpectedName = "RequirementsPkg";
		
		final List<IRPModelElement> theRequirementsPkgs = 
				GeneralHelpers.findElementsWithMetaClassAndName(
						"Package", theReqtsPkgExpectedName, forTheProject );
		
		String theSysMLHelperProfilePath =
				RhapsodyAppServer.getActiveRhapsodyApplication().getOMROOT() + 
				"\\Profiles\\SysMLHelper\\SysMLHelper_rpy";
		
		if( theRequirementsPkgs != null && !theRequirementsPkgs.isEmpty() ){
				
			File theChosenRqtfFile = getFile(
					lookingForRqtfTemplatesThatMatchRegEx,
					theSysMLHelperProfilePath,
					"Which Gateway project template do you want to use?" );
				
			if( theChosenRqtfFile != null ){
				
				String theCandidateTypesFileName = 
						theChosenRqtfFile.getName().substring(
								0, theChosenRqtfFile.getName().length()-5 ) + ".types";
				
				Logger.writeLine( "The corresponding types file is " + theCandidateTypesFileName );
				
				final File theChosenTypesFile = getFile(
						theCandidateTypesFileName, 
						theSysMLHelperProfilePath,
						"Which Gateway Types template do you want to use?" );

				if( theChosenTypesFile != null ){
					
					final GatewayFileParser theTemplateProjectFile = new GatewayFileParser( theChosenRqtfFile );
					final GatewayFileParser theTemplateTypesFile = new GatewayFileParser( theChosenTypesFile );

					final File theExistingRqtfFile = getFile(
							"^" + forTheProject.getName() + ".rqtf$",
							forTheProject.getCurrentDirectory() + "\\" + forTheProject.getName() + "_rpy",
							"Which existing Types file do you want to use?");
					
					// if project has an existing types file then we need to consider how to merge in its contents
					if( theExistingRqtfFile != null ){
						
						final GatewayFileParser theExistingProjectFile = 
								new GatewayFileParser( theExistingRqtfFile );
						
						updateTheRqtfFile(
								theTemplateProjectFile,
								theExistingProjectFile);
					
					} else { // no existing rqtf file
						
						// check to see if model references external packages 
						for( IRPModelElement theReqtsPkg : theRequirementsPkgs ) {
							
							IRPPackage thePkg = (IRPPackage)theReqtsPkg;
							
							if( thePkg.isReferenceUnit()==1 ){
								
								Logger.writeLine("Detected that " + Logger.elementInfo( thePkg ) + " is added by reference");
								
								IRPUnit theUnit = thePkg.getSaveUnit();
								
								String theReferencedDir = theUnit.getCurrentDirectory();
								
								String theProjectName = extractProjectNameFrom( theReferencedDir );
								
								final File theReferencedRqtfFile = getFile(
										theProjectName + ".rqtf$",
										theReferencedDir,
										"Which Gateway project template in the referenced project do you want to use?" );
	
								final GatewayFileParser theExistingProjectFile = 
										new GatewayFileParser( theReferencedRqtfFile );
								
								updateTheRqtfFile(
										theTemplateProjectFile,
										theExistingProjectFile);
							}
						}
					}
					
					javax.swing.SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {				
							JFrame.setDefaultLookAndFeelDecorated( true );
							
							JFrame frame = new JFrame(
									"Setup the Rhapsody Gateway for the project?");
							
							frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
							
							CreateGatewayProjectPanel thePanel = 
									new CreateGatewayProjectPanel( 
											theRequirementsPkgs, 
											theTemplateProjectFile,
											theTemplateTypesFile );

							frame.setContentPane( thePanel );
							frame.pack();
							frame.setLocationRelativeTo( null );
							frame.setVisible( true );
						}
					});
				} else {
					
					Logger.writeLine("Error in CreateGatewayProjectPanel.launchThePanel, no types file matching '" + 
							theCandidateTypesFileName + "' was found in " + theSysMLHelperProfilePath);

				}

			} // no rqtf file candidate was found
		} else {
			Logger.writeLine("Error in CreateGatewayProjectPanel.launchThePanel, unable to proceed as no packages called " + theReqtsPkgExpectedName + " were found in the project");
		}
	}

	CreateGatewayProjectPanel(
			List<IRPModelElement> forSelectablePackages,
			GatewayFileParser usingGatewayProject,
			GatewayFileParser andGatewayTypes ){
		
		super();

		m_Project = forSelectablePackages.get(0).getProject();
		m_ChosenProjectFile = usingGatewayProject;
		m_ChosenTypesFile = andGatewayTypes;

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		add( createPanelWithTextCentered(
				"Set up the Gateway project to import the requirements"), 
						BorderLayout.PAGE_START );

		add( createWestCentrePanel( forSelectablePackages ), BorderLayout.CENTER );
		add( createPageEndPanel(), BorderLayout.PAGE_END );
	}
	
	private Component createWestCentrePanel(
			List<IRPModelElement> forSelectablePackages) {
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX(CENTER_ALIGNMENT);
		
		List<GatewayFileSection> theDocs = m_ChosenProjectFile.getAllTheFileSections();
		
		GatewayFileSection theTypesDoc = m_ChosenTypesFile.getFileSectionWith("Types");
		String theTypesNames = theTypesDoc.getValueFor("Names");
		String[] theAnalysisTypes = theTypesNames.split(",");
		
		String theRhapsodyTypeRegEx = ".*Rhapsody.*";
		
		GatewayFileSection theRhapsodySection = m_ChosenProjectFile.getFileSectionWith( "UML Model" );
		
		String theReqtsPackageValue = null;
		
		if( theRhapsodySection != null ){
			
			theReqtsPackageValue = theRhapsodySection.getVariableXValue("requirementsPackage");
			
			if( theReqtsPackageValue != null ){
				Logger.writeLine("Found that requirementsPackage=" + theReqtsPackageValue );
			}
		} else {
			Logger.writeLine( "Error in createWestCentrePanel, no section was found that matches the regex=" + theRhapsodyTypeRegEx );
		}
		
		for (GatewayFileSection gatewayDoc : theDocs) {
			try {
				
				String theDocName = gatewayDoc.getSectionName();
				
				Logger.writeLine("Found gatewayDoc=" + theDocName + ", isImmutable=" + gatewayDoc.isImmutable());
				
				// ignore Files and Rhapsody docs
				if (!theDocName.equals("Files") && 
						!gatewayDoc.getValueFor("Type").contains("Rhapsody")){
				
					IRPModelElement theSelectedPkg = extractPreselectedPackageFor(
							gatewayDoc.getSectionName(), m_Project, theReqtsPackageValue );
					
					if( theSelectedPkg == null ){
						Logger.writeLine("Error in CreateGatewayProjectPanel.createWestCentrePanel, extractPreselectedPackageFor returned null for selected package for " + gatewayDoc.getSectionName() );
						
					} else if ( !forSelectablePackages.contains( theSelectedPkg ) ){
						Logger.writeLine("Error in CreateGatewayProjectPanel.createWestCentrePanel, " + Logger.elementInfo( theSelectedPkg ) + " was not found in the selectable list");
						theSelectedPkg = forSelectablePackages.get(0);						
					}
					
					GatewayDocumentPanel theGatewayDocumentPanel = 
							new GatewayDocumentPanel(
									theDocName, 
									theAnalysisTypes, 
									gatewayDoc.getValueFor("Type"),
									gatewayDoc.getValueFor("Path"), 
									forSelectablePackages,
									theSelectedPkg,
									gatewayDoc.isImmutable(),
									m_Project );
					
					m_GatewayDocumentPanel.add( theGatewayDocumentPanel );
					
					thePanel.add( theGatewayDocumentPanel );
				}
				
			} catch (FileNotFoundException e) {
				Logger.writeLine("Error in createWestCentrePanel, Unhandled FileNotFoundException detected");
			}
		}

		return thePanel;
	}
	
	private Component createPageEndPanel() {

		JLabel theLabel = new JLabel( "Do you want to proceed?" );
		theLabel.setAlignmentX(CENTER_ALIGNMENT);
		theLabel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX(CENTER_ALIGNMENT);
		thePanel.add( theLabel );
		thePanel.add( createOKCancelPanel() );
		
		return thePanel;
	}
	
	private static void updateTheRqtfFile(
			final GatewayFileParser theProjectFileToUpdate,
			final GatewayFileParser theExistingProjectFile) {
		
		List<GatewayFileSection> existingGatewayDocs = theExistingProjectFile.getAllTheFileSections();
		
		for (GatewayFileSection existingGatewayDoc : existingGatewayDocs) {
			
			String gatewayDocType = existingGatewayDoc.getValueFor( "Type" );
			
			if( gatewayDocType != null && !gatewayDocType.contains( "Rhapsody" ) ){
				
				Logger.writeLine("===========================");
				
				Logger.writeLine("The existing project has a gatewayDoc called " + existingGatewayDoc.getSectionName() + 
						" with Type=" + gatewayDocType );
				
				GatewayFileSection theTemplateDoc = theProjectFileToUpdate.getFileSectionWithType( gatewayDocType );
				
				if (theTemplateDoc != null ){
					Logger.writeLine("A match was found between template doc called " + theTemplateDoc.getSectionName() +
								" and the existing doc called " + existingGatewayDoc.getSectionName());
					
					theProjectFileToUpdate.renameFileSection( theTemplateDoc.getSectionName(), existingGatewayDoc.getSectionName() );
					theTemplateDoc.setIsImmutable( true );
					theProjectFileToUpdate.replaceGatewayDoc( gatewayDocType, existingGatewayDoc );
				}							
			}		
		}
	}

	IRPModelElement extractPreselectedPackageFor(
			String theGatewayDocName,
			IRPProject inTheProject,
			String basedOnString ){
		
		IRPModelElement theEl = null;
		
		String[] split = basedOnString.replaceAll("Packages/","").split("¥");
		
		String thePath = null;
		
		for( int i = split.length-1; i>=0; i-- ) {
			if( split[i].equals( theGatewayDocName ) ){
				thePath = split[i-1].replaceFirst("^\\w+/", "").replace("/", "::");
				break;
			}
		}
		
		if( thePath != null ){

			theEl = inTheProject.findElementsByFullName( thePath, "Package" );
			
			if( theEl != null ){
				Logger.writeLine( "Successfully found the specified " + theEl.getFullPathName() + " import package in the project");
			} else {
				Logger.writeLine( "Warning in extractPreselectedPackageFor, " + thePath + " was not found" );
			}
		}
		
		return theEl;	
	}
	
	private static File getFile(
			final String matchingTheRegEx,
			final String inPathToSearch,
			final String withChoiceOfFileMsg ) {
			
		Logger.writeLine("Looking in " + inPathToSearch + " for a file names matching '" + matchingTheRegEx +"'");
			
		List<File> theFiles = getFilesMatching( matchingTheRegEx, inPathToSearch );
		
		int fileCount = theFiles.size();
		
		File theCandidateRqtfFile = null;
		
		if (fileCount==0){
			Logger.writeLine("No file matching " + matchingTheRegEx + " was found in the " + inPathToSearch + " folder");
			
		} else if (fileCount==1){
			
			// don't bother with selection dialog
			theCandidateRqtfFile = theFiles.get(0);
			
		} else {
			/// get user to select
			Object[] options = theFiles.toArray();
			
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			Object selectedElement = JOptionPane.showInputDialog(
					null,
					withChoiceOfFileMsg,
					"Input",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);
			
			if (selectedElement != null){
			
				theCandidateRqtfFile = (File)selectedElement;
				Logger.writeLine("The chosen file was " + theCandidateRqtfFile.getAbsolutePath());
			}
		}
		
		return theCandidateRqtfFile;
	}
	
	private static List<File> getFilesMatching(
			String theRegEx, 
			String inThePath ){
		
		List<File> theFiles = new ArrayList<File>();
		
		File theDirectory = new File( inThePath );
		
	    File[] theCandidateFiles = theDirectory.listFiles();
	    
	    if( theCandidateFiles != null ){
	        for (File theCandidateFile : theCandidateFiles){
	        	
	        	if (theCandidateFile.isFile() && 
	        			theCandidateFile.getName().matches( theRegEx )){
	        		
	        		Logger.writeLine("Found: " + theCandidateFile.getAbsolutePath());
	        		theFiles.add( theCandidateFile );
	        	}		            
	        }		    		        
	    }
	    
	    return theFiles;
	}

	public static String extractProjectNameFrom( 
			String theUnitPath ){
		
		String theProjectName = null;
		
		String theRegEx = ".*[/\\\\](.*)_rpy.*"; 
		
		Pattern thePattern = Pattern.compile( theRegEx );
		
		Matcher theMatcher = thePattern.matcher( theUnitPath );
		
		if( theMatcher.find() ){
			theProjectName = theMatcher.group( 1 );
		}
		
		if (theProjectName==null){
			Logger.writeLine( "Error in extractProjectNameFrom, project name could not be extracted in theUnitPath=" + theUnitPath );
		}
		
		return theProjectName;
	}
	
	private void createDesignatedReqtPackagesInTheModel() {
		
		for( GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel ) {
			
			IRPPackage theRootPkg = gatewayDocumentPanel.getRootPackage();
			String thePkgName = gatewayDocumentPanel.getReqtsPkgName();
			String theStereotypeName = "from" + gatewayDocumentPanel.getAnalysisTypeName();
			
			IRPModelElement existingPkg = 
					GeneralHelpers.findElementWithMetaClassAndName(
							"Package", thePkgName, m_Project );

			if( existingPkg != null ){

				Logger.writeLine( "Skipping creation of " + thePkgName + 
						" as package already exists with the name " + thePkgName );
				
			} else if( theRootPkg.isReadOnly() == 1 ) { // check to see if root package is writable	 

				Logger.writeLine( "Skipping creation of " + thePkgName + 
						" as unable to write to " + Logger.elementInfo( theRootPkg ) );
				
			} else {
				Logger.writeLine( "Create package called '" + thePkgName + " with the type of analysis '" + 
						gatewayDocumentPanel.getAnalysisTypeName() + "' in the root " + 
						Logger.elementInfo( theRootPkg ) );

				IRPPackage theReqtsDocPkg = (IRPPackage) theRootPkg.addNewAggr( "Package", thePkgName );
				theReqtsDocPkg.highLightElement();

				IRPModelElement theFoundStereotype = 
						m_Project.findAllByName( theStereotypeName , "Stereotype" );

				IRPStereotype theFromStereotype = null;

				if( theFoundStereotype == null ){
					theFromStereotype = theReqtsDocPkg.addStereotype( theStereotypeName, "Package" );

					theFromStereotype.addMetaClass( "Dependency" );
					theFromStereotype.addMetaClass( "HyperLink" );
					theFromStereotype.addMetaClass( "Requirement" );
					theFromStereotype.addMetaClass( "Type" );

					theFromStereotype.setOwner( theReqtsDocPkg );
					theFromStereotype.highLightElement();

				} else {
					theFromStereotype = theReqtsDocPkg.addStereotype( theStereotypeName, "Package" );
				}
			}
		}
	}
	
	private void updateGatewayDocInfoBasedOnUserSelections() {
		
		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {
			
    		String theOriginalName = gatewayDocumentPanel.getOriginalName();
    		String theNewName = gatewayDocumentPanel.getReqtsPkgName();	
    		
    		if (!theOriginalName.equals( theNewName )){
    			
    			Logger.writeLine("Detected that user changed the name from " + 
    					theOriginalName + " to " + theNewName);
    			
    			m_ChosenProjectFile.renameFileSection( theOriginalName, theNewName );
    		}
		}
				
		for( GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel ) {
			
    		String theNewName = gatewayDocumentPanel.getReqtsPkgName();	
			GatewayFileSection theGatewayDoc = m_ChosenProjectFile.getFileSectionWith( theNewName );
    		    		
    		theGatewayDoc.setValueFor("Type", gatewayDocumentPanel.getAnalysisTypeName());
      	    theGatewayDoc.setValueFor("Path", gatewayDocumentPanel.getPathName());
		}
				
		GatewayFileSection theUMLModelDoc = m_ChosenProjectFile.getFileSectionWith("UML Model");
		
		// change the requirementsPackage variable in the UML Model document
		String theReqtsPkgValue = buildRequirementsPackageValueFor();
		theUMLModelDoc.setVariableXValue( "requirementsPackage", theReqtsPkgValue );
		theUMLModelDoc.setVariableXValue( "previousReqsPackage", theReqtsPkgValue );
		
		String theUMLModelPath = "..\\" + m_Project.getName() + ".rpy";
		theUMLModelDoc.setValueFor( "Path", theUMLModelPath );
	}

	private String buildRequirementsPackageValueFor(){
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = m_GatewayDocumentPanel.size() - 1; i >= 0; i--) {
			
    		String targetPkgName = m_GatewayDocumentPanel.get(i).getReqtsPkgName();	
    		IRPPackage theRootPkg = m_GatewayDocumentPanel.get(i).getRootPackage();
    		
    		sb.append( m_Project.getName() );

    		String[] split = theRootPkg.getFullPathName().split("::");
    		
    		for (String section : split) {
    			sb.append( "/Packages/" );
    			sb.append( section );
    		}
    		
    		sb.append( "¥" );
    		sb.append( targetPkgName );
    		
    		if(i>0){
    			sb.append( "¥" );
    		}
		}
		
		Logger.writeLine("buildRequirementsPackageValueFor returning " + sb.toString() );
		return sb.toString();
	}
	
	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		
		String errorMsg = null;
		
		boolean isValid = true;
		
		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {
			
			String theChosenName = gatewayDocumentPanel.getReqtsPkgName();
			
			boolean isLegalName = GeneralHelpers.isLegalName( theChosenName );
			
			if (!isLegalName){
				
				if (errorMsg==null){
					errorMsg = theChosenName + " is not legal as a package name\n";
				} else {
					errorMsg += theChosenName + " is not legal as a package name\n";				
				}
				
				isValid = false;
			}
		}

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;		
	}

	@Override
	protected void performAction() {
		
		try {
			// do silent check first
			if( checkValidity( false ) ){

				updateGatewayDocInfoBasedOnUserSelections();
				
				String theProjectPath = m_Project.getCurrentDirectory() + "/" + m_Project.getName() + "_rpy" + "/";

				final String theGatewayPkgName = "GatewayProjectFiles";
				
				IRPModelElement theExistingGWPackage = 
						GeneralHelpers.findElementWithMetaClassAndName(
								"Package", theGatewayPkgName, m_Project);
				
				IRPPackage theGatewayProjectFilesPkg = null;
				
				if( theExistingGWPackage != null && theExistingGWPackage instanceof IRPPackage ){
					theGatewayProjectFilesPkg = (IRPPackage)theExistingGWPackage;
				} else {
					// mimic Rhapsody by adding the GatewayProjectFiles package containing the GW files
					theGatewayProjectFilesPkg = m_Project.addNestedPackage("GatewayProjectFiles");
				}
				
				String theRqtfFileName = m_Project.getName() + ".rqtf";
				String theRqtfFullFilePathForProject = theProjectPath + theRqtfFileName;
				
				m_ChosenProjectFile.writeGatewayFileTo( theRqtfFullFilePathForProject );
				
				if( GeneralHelpers.findElementWithMetaClassAndName(
						"ControlledFile", theRqtfFileName, theGatewayProjectFilesPkg ) == null ){
					
					theGatewayProjectFilesPkg.addNewAggr(
							"ControlledFile",  theRqtfFileName);
				}

				String theTypesFileName = m_Project.getName() + ".types";
				String theTypesFullFilePathForProject = theProjectPath + theTypesFileName;
				m_ChosenTypesFile.writeGatewayFileTo( theTypesFullFilePathForProject );
				
				if( GeneralHelpers.findElementWithMetaClassAndName(
						"ControlledFile", theTypesFileName, theGatewayProjectFilesPkg ) == null ){
				
					theGatewayProjectFilesPkg.addNewAggr(
							"ControlledFile",  theTypesFileName);
				}
				
				createDesignatedReqtPackagesInTheModel();
			
			} else {
				Logger.writeLine("Error in CreateGatewayProjectPanel.performAction, checkValidity returned false");
			}	
		} catch (Exception e) {
			Logger.writeLine("Error, unhandled exception in CreateGatewayProjectPanel.performAction");
		}

	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 5-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #039 17-JUN-2016: Minor fixes and improvements to robustness of Gateway project setup (F.J.Chadburn)
    #051 06-JUL-2016: Re-factored the GW panel to allow it to incrementally add to previous setup (F.J.Chadburn)
    #063 17-JUL-2016: Gateway project creator now mimics GatewayProjectFiles pkg creation if necessary (F.J.Chadburn)

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