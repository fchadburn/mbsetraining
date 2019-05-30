package requirementsanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;
 
public class NestedActivityDiagram {

	private final static String m_Prefix = "AD - ";
	
	// for test only
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theSelectedEls = 
			theRhpApp.getListOfSelectedElements().toList();

		NestedActivityDiagram.createNestedActivityDiagramsFor( theSelectedEls );
	}
	
	public static void renameNestedActivityDiagramsFor(
			List<IRPModelElement> theSelectedEls){
		
		Map<IRPFlowchart, String> theNewNameMappings = new HashMap<IRPFlowchart, String>(); 
		
		List<IRPActivityDiagram> theADs = 
				GeneralHelpers.buildListOfActivityDiagramsFor( theSelectedEls );
		
		Logger.info( "There are " + theADs.size() + 
				" Activity Diagrams nested under the selected list" );

		for( IRPActivityDiagram theAD : theADs ){
			
			IRPModelElement theOwner = theAD.getOwner();
			IRPModelElement theOwnersOwner = theOwner.getOwner();
			
			if( theOwner instanceof IRPFlowchart && 
				theOwnersOwner instanceof IRPUseCase ){
				
				String theUseCaseName = theOwnersOwner.getName();
				String thePreferredName = m_Prefix + theUseCaseName;
				
				if( theOwner.getName().equals( thePreferredName ) ){
					Logger.info( "Determined that " + Logger.elementInfo( theOwner ) + 
							" already matches " + Logger.elementInfo( theOwnersOwner ) );
				} else {
					theNewNameMappings.put( (IRPFlowchart) theOwner, thePreferredName );	
				}
			}
		}
		
		if( theNewNameMappings.isEmpty() ){
	
			UserInterfaceHelpers.showInformationDialog( 
					"Nothing to do. The checker has determined that the " + 
					theADs.size() + " activity diagrams are correctly named." );
			
		} else {
			String theMsg = "The checker has determined that " + theNewNameMappings.size() + 
					" of the " + theADs.size() + " activity diagrams require " +
					"renaming to match the use cases:\n";
			
			int count = 0;
			
			for( Map.Entry<IRPFlowchart, String> entry : theNewNameMappings.entrySet() ){
			
				count++;
				
				if( count > 10 ){
					theMsg = theMsg + "...\n";
				} else {
					theMsg = theMsg + Logger.elementInfo(entry.getKey()) + "\n";
				}
			}
			
			theMsg = theMsg + "\nDo you want to rename them?";

			int response = JOptionPane.showConfirmDialog(
					null, 
					theMsg, 
					"Update activity diagram names",
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.CANCEL_OPTION){
				Logger.writeLine("Operation was cancelled by user with no changes made.");
			} else {
				if (response == JOptionPane.YES_OPTION) {

					for (Map.Entry<IRPFlowchart, String> entry : theNewNameMappings.entrySet()){
						
						IRPFlowchart theAD = (IRPFlowchart)entry.getKey();
					    
					    try {
					    	theAD.setName( entry.getValue() );		
						    Logger.writeLine("Renamed " + Logger.elementInfo( theAD ) + " to " + entry.getValue() );
						    
						} catch (Exception e) {
							Logger.writeLine("Error: Unable to rename " + Logger.elementInfo( theAD ) + " to " + entry.getValue());
						}			    
					}

				} else if (response == JOptionPane.NO_OPTION){
					Logger.writeLine("Info: User chose not rename the actions.");
				} 
			}
		}
	}
	
	public static void createNestedActivityDiagramsFor(
			List<IRPModelElement> theElements ){
		 
		for( IRPModelElement theElement : theElements ){
			
			if( theElement instanceof IRPUseCase ){
				
				Logger.info( "Creating a nested Activity Diagram underneath " + 
						Logger.elementInfo( theElement ) );
				
				createNestedActivityDiagram( 
						(IRPUseCase)theElement, 
						m_Prefix + theElement.getName(), 
						"SysMLHelper.RequirementsAnalysis.TemplateForActivityDiagram" );
			} 
		}
	}
	
	public static void createNestedActivityDiagram(
			IRPClassifier forClassifier, 
			String withUnadornedName,
			String basedOnPropertyKey ){
		
		String theName = withUnadornedName;
		 
		// check if existing AD with same name
		IRPFlowchart theAD = (IRPFlowchart) forClassifier.findNestedElement( 
				theName, 
				"ActivityDiagram" );
		
		int count = 0;
		
		while (theAD != null){
			
			Logger.warning( Logger.elementInfo( forClassifier ) + " already has a nested activity diagram called " + theName );
			count++;
			theName = withUnadornedName + " " + count;
			theAD = (IRPFlowchart) forClassifier.findNestedElement( theName , "ActivityDiagram" );
		}
		
		IRPModelElement theTemplate = null;
		
		try {
			theTemplate = 
					StereotypeAndPropertySettings.getTemplateForActivityDiagram( 
							forClassifier,
							basedOnPropertyKey );
		} catch (Exception e) {
			Logger.writeLine("Exception trying to find template based on property " + basedOnPropertyKey);
		}
		
		IRPFlowchart theFlowchart = null;
		
		if( theTemplate != null ){
			
			try {
				theFlowchart = (IRPFlowchart) theTemplate.clone( theName, forClassifier );
				Logger.writeLine( "the cloned flowchart is " + Logger.elementInfo( theFlowchart ) );

			} catch (Exception e) {
				Logger.writeLine("Exception while cloning");
			}

		} else {
			Logger.writeLine("Warning, Could not find template so creating fresh AD");
			theFlowchart = forClassifier.addActivityDiagram();
			theFlowchart.setName( theName );
			
			IRPStatechartDiagram theStatechart = theFlowchart.getStatechartDiagram();

			try {
				theStatechart.createGraphics();

			} catch (Exception e) {
				Logger.writeLine("Exception creating graphics");
			}
		}
		
		if( theFlowchart != null ){
			
			theFlowchart.setIsAnalysisOnly( 1 ); // so that call op right-click parameter sync menus appear
			IRPStatechartDiagram theStatechart = theFlowchart.getStatechartDiagram();
			theStatechart.highLightElement();
			theFlowchart.setAsMainBehavior();
		}
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #080 28-JUL-2016: Added activity diagram name to the create AD dialog for use cases (F.J.Chadburn)
    #102 03-NOV-2016: Add right-click menu to auto update names of ADs from UC names (F.J.Chadburn)
    #244 11-OCT-2017: Default ADs to Analysis mode to better support call operation parameter sync (F.J.Chadburn)
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

