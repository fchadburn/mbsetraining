package generalhelpers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
	private GatewayTypesParser m_TypesFile;
	private GatewayProjectParser m_ProjectFile;
	private List<GatewayDocumentPanel> m_GatewayDocumentPanel = new ArrayList<GatewayDocumentPanel>();
	
	// test only
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		if (theSelectedEl instanceof IRPProject){
			try { 
				CreateGatewayProjectPanel.launchThePanel( (IRPProject)theSelectedEl, theSelectedEl.getName() );

			} catch (Exception e) {
				Logger.writeLine("Error: Exception in OnMenuItemSelect when invoking MBSE Method: General\\Quick hyperlink");
			}					
		}
	}
	public static void launchThePanel(
			final IRPProject forTheProject,
			final String basedOnExecutionContext ){
	
		String theReqtsPkgExpectedName = "RequirementsPkg";
		
		final List<IRPModelElement> theRequirementsPkgs = 
				GeneralHelpers.findElementsWithMetaClassAndName(
						"Package", theReqtsPkgExpectedName, forTheProject);
		
		if (theRequirementsPkgs != null && !theRequirementsPkgs.isEmpty()){
			
			File theCandidateRqtfFile = null;
			
			if (basedOnExecutionContext.equals( forTheProject.getName())){
				
				// Allow user to select
				theCandidateRqtfFile = getRqtfFile( forTheProject, ".*.rqtf$" );
				
			} else {
				// Look for specific rqtf file (there will be one or zero)
				theCandidateRqtfFile = getRqtfFile( forTheProject, basedOnExecutionContext + ".rqtf$" );
			}
				
			if (theCandidateRqtfFile != null){
				
				String theRqtfFileName = theCandidateRqtfFile.getName();
				String theCorrespondingTypesFileName = theRqtfFileName.substring(0, theRqtfFileName.length()-5) + ".types";
				
				Logger.writeLine("The corresponding types file is " + theCorrespondingTypesFileName);
				
				List<File> theTypesFiles = getFilesInSysMLHelperRpyFolder( 
						theCorrespondingTypesFileName, forTheProject);
				
				final File theTypesFile = theTypesFiles.get( 0 );
				
				final File theChosenRqtfTemplate = theCandidateRqtfFile;
				
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
										theChosenRqtfTemplate,
										theTypesFile);

						frame.setContentPane( thePanel );
						frame.pack();
						frame.setLocationRelativeTo( null );
						frame.setVisible( true );
					}
				});
			}
		}
	}
	
	CreateGatewayProjectPanel(
			List<IRPModelElement> forSelectablePackages,
			File usingRqtfTemplate,
			File andTypesFile ){
		
		super();

		m_Project = forSelectablePackages.get(0).getProject();
		
		m_TypesFile = new GatewayTypesParser( andTypesFile );
		m_ProjectFile = new GatewayProjectParser( usingRqtfTemplate );

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
		
		List<GatewayDoc> theDocs = m_ProjectFile.getGatewayDocs();
		
		for (GatewayDoc gatewayDoc : theDocs) {
			try {
				
				String theDocName = gatewayDoc.getDocumentName();
				
				// ignore Files and Rhapsody docs
				if (!theDocName.equals("Files") && 
						!gatewayDoc.getValueFor("Type").contains("Rhapsody")){
					
					GatewayDocumentPanel theGatewayDocumentPanel = 
							new GatewayDocumentPanel(
									theDocName, 
									m_TypesFile.getAnalysisTypes(), 
									gatewayDoc.getValueFor("Type"),
									gatewayDoc.getValueFor("Path"), 
									forSelectablePackages,
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
	
	private static File getRqtfFile(
			final IRPProject forTheProject,
			final String matchingTheRegEx ) {
		
		List<File> theFiles = getFilesInSysMLHelperRpyFolder(matchingTheRegEx, forTheProject);
		
		int fileCount = theFiles.size();
		
		File theCandidateRqtfFile = null;
		
		if (fileCount==0){
			Logger.writeLine("Skipping Gateway dialog creation as no rqtf template matching " + matchingTheRegEx + 
					" was found in the <Share>/Profiles/SysMLHelper/SysMLHelper_rpy folder");
			
		} else if (fileCount==1){
			
			// don't bother with selection dialog
			theCandidateRqtfFile = theFiles.get(0);
			
		} else {
			/// get user to select
			Object[] options = theFiles.toArray();
			
			JDialog.setDefaultLookAndFeelDecorated(true);
			
			Object selectedElement = JOptionPane.showInputDialog(
					null,
					"Which Gateway project template do you want to use?",
					"Input",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);
			
			if (selectedElement != null){
			
				theCandidateRqtfFile = (File)selectedElement;
				Logger.writeLine("The chosen rqtf template is " + theCandidateRqtfFile.getAbsolutePath());
			}
		}
		
		return theCandidateRqtfFile;
	}
	
	private static List<File> getFilesInSysMLHelperRpyFolder(
			String matchingRegEx,
			IRPProject forTheProject){
		
		Logger.writeLine("invoked getFilesInSysMLHelperRpyFolder with matchingRegEx=" + matchingRegEx);
		
		List<File> theFiles = null;
		
		String theProfileName = "SysMLHelperProfile";
		
		IRPProfile theProfile = 
				(IRPProfile) forTheProject.findElementsByFullName(
						theProfileName, "Profile");
				
		if (theProfile != null){
			Logger.writeLine(theProfile, "was found");
			
			// #002 05-APR-2016: Improved robustness of copying .types file (F.J.Chadburn)
			IRPApplication myApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			String pathToSearch = myApp.getOMROOT() + "\\Profiles\\SysMLHelper\\SysMLHelper_rpy";
			
			Logger.writeLine("Looking in " + pathToSearch + " for a file names matching '" + matchingRegEx +"'");
			
			theFiles = getFilesMatching(matchingRegEx, pathToSearch);
			
		} else {
			Logger.writeLine(
					"Error in getGatewayTypesFile, unable to find profile called " + 
					theProfileName + " in the project");
		}
		
		return theFiles;
	}
	
	private static List<File> getFilesMatching(
			String theRegEx, 
			String inThePath){
		
		Logger.writeLine("getFilesWith invoked for " + theRegEx + " in the path=" + inThePath);
		List<File> theFiles = new ArrayList<File>();
		
		File theDirectory = new File( inThePath );
		
	    File[] theCandidateFiles = theDirectory.listFiles();
	    
	    if( theCandidateFiles != null ){
	        for (File theCandidateFile : theCandidateFiles){
	        	
	        	Logger.writeLine("Looking at candidate=" + theCandidateFile.getName());
	        	
	        	if (theCandidateFile.isFile() && 
	        			theCandidateFile.getName().matches( theRegEx )){
	        		
	        		Logger.writeLine("Found: " + theCandidateFile.getAbsolutePath());
	        		theFiles.add( theCandidateFile );
	        	}		            
	        }		    		        
	    }
	    
	    return theFiles;
	}

	private void createDesignatedReqtPackagesInTheModel() {
		
		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {
			
			// check to see if root package is writable
			IRPPackage theRootPkg = gatewayDocumentPanel.getRootPackage();
			String thePkgName = gatewayDocumentPanel.getReqtsPkgName();
			String theStereotypeName = "from" + gatewayDocumentPanel.getAnalysisTypeName();
			
			if (theRootPkg.isReadOnly()==1){
				
				Logger.writeLine("Skipping creation of " + thePkgName + 
						" as unable to write to " + Logger.elementInfo(theRootPkg) );
			} else {
				
				IRPModelElement existingPkg = GeneralHelpers.findElementWithMetaClassAndName("Package", thePkgName, m_Project);
				
				if (existingPkg != null){
					
					Logger.writeLine("Skipping creation of " + thePkgName + 
							" as package already exists with the name " + thePkgName );
					
				} else {	
					Logger.writeLine("Create package called '" + thePkgName + " with the type of analysis '" + 
							gatewayDocumentPanel.getAnalysisTypeName() + "' in the root package called '"+ Logger.elementInfo(theRootPkg)+"'");
				
					IRPPackage theReqtsDocPkg = (IRPPackage) theRootPkg.addNewAggr("Package", thePkgName);
					theReqtsDocPkg.highLightElement();
					
					IRPModelElement theFoundStereotype = 
							m_Project.findAllByName( theStereotypeName , "Stereotype" );
					
					IRPStereotype theFromStereotype = null;
					
					if (theFoundStereotype == null){
						theFromStereotype = theReqtsDocPkg.addStereotype(theStereotypeName, "Package");
						
						theFromStereotype.addMetaClass("Dependency");
						theFromStereotype.addMetaClass("HyperLink");
						theFromStereotype.addMetaClass("Requirement");
						theFromStereotype.addMetaClass("Type");
						
						theFromStereotype.setOwner( theReqtsDocPkg );
						theFromStereotype.highLightElement();

					} else {
						theFromStereotype = theReqtsDocPkg.addStereotype(theStereotypeName, "Package");
					}
				}
			}
		}
	}
	
	private void updateGatewayDocInfoBasedOnUserSelections() {
		
		updateFilesDocBasedOnNameChanges();
				
		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {
			
    		String theNewName = gatewayDocumentPanel.getReqtsPkgName();	
			GatewayDoc theGatewayDoc = m_ProjectFile.getGatewayDocWith( theNewName );
    		    		
    		theGatewayDoc.setValueFor("Type", gatewayDocumentPanel.getAnalysisTypeName());
      	    theGatewayDoc.setValueFor("Path", gatewayDocumentPanel.getPathName());
		}
				
		GatewayDoc theUMLModelDoc = m_ProjectFile.getGatewayDocWith("UML Model");
		
		// change the requirementsPackage variable in the UML Model document
		String theReqtsPkgValue = buildRequirementsPackageValueFor();
		theUMLModelDoc.setVariableXValue( "requirementsPackage", theReqtsPkgValue );
		
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
	
	private void updateFilesDocBasedOnNameChanges() {
		
		GatewayDoc theFilesDoc = m_ProjectFile.getGatewayDocWith("Files");
		
		String theOriginalNamesValue = theFilesDoc.getValueFor("Names");
		String theUpdatedNamesValue = theOriginalNamesValue;
		
		for (GatewayDocumentPanel gatewayDocumentPanel : m_GatewayDocumentPanel) {
			
    		String theOriginalName = gatewayDocumentPanel.getOriginalName();
    		String theNewName = gatewayDocumentPanel.getReqtsPkgName();	
    		
    		if (!theOriginalName.equals( theNewName )){
    			
    			Logger.writeLine("Detected that user changed the name from " + 
    					theOriginalName + " to " + theNewName);
    			
    			GatewayDoc theDoc = m_ProjectFile.getGatewayDocWith( theOriginalName );
    			theDoc.setDocumentName( theNewName );
    			
    			theUpdatedNamesValue = theOriginalNamesValue.replaceAll(theOriginalName, theNewName);
    		}
		}
		
		if (!theUpdatedNamesValue.equals(theOriginalNamesValue)){
			Logger.writeLine("Updated names value from " + theOriginalNamesValue + " to " + theUpdatedNamesValue);
			theFilesDoc.setValueFor("Names", theUpdatedNamesValue);
		}
	}

	private void writeTheNewRqtfFile() {
		try {
			String theFileName = m_Project.getCurrentDirectory() + "/" + 
					m_Project.getName() + "_rpy" + "/" + m_Project.getName() + ".rqtf";
			
			Logger.writeLine("Building file called " + theFileName);
			
			PrintWriter printWriter;
			
			printWriter = new PrintWriter ( theFileName );
			
			List<GatewayDoc> theGatewayDocs = m_ProjectFile.getGatewayDocs();
			
			for (GatewayDoc gatewayDoc : theGatewayDocs) {
				
				List<String> theDocContents = gatewayDoc.getRqtfLinesForDocument();
				
				for (String theDocLine : theDocContents) {	
			        
					printWriter.println ( theDocLine );
					Logger.writeLine("Output:" + theDocLine );
				}
			}
			
			printWriter.close();
			
		} catch (FileNotFoundException e) {
			
			Logger.writeLine("Error in writeTheNewRqtfFile, unhandled FileNotFoundException detected");
		}
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
		
		// do silent check first
		if (checkValidity( false )){
			
			FileHelper.copyTheFile(m_Project, m_TypesFile.getFile(), m_Project.getName() + ".types");
			updateGatewayDocInfoBasedOnUserSelections();
			writeTheNewRqtfFile();
			createDesignatedReqtPackagesInTheModel();
		
		} else {
			Logger.writeLine("Error in CreateGatewayProjectPanel.performAction, checkValidity returned false");
		}	
	}

}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)

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