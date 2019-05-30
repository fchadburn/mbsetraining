package functionaldesignplugin;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

import generalhelpers.ConfigurationSettings;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.ProfileVersionManager;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

public class TopLevelSystemDesignCreator {

	protected String m_LongName;
	protected String m_ShortName;
	protected String m_FunctionName;
	
	IRPObjectModelDiagram m_FunctionHierarchyBDD = null;
	IRPObjectModelDiagram m_SystemContextDiagram = null;

	// test only
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPProject theRhpPrj = theRhpApp.activeProject();
		
		List<IRPActor> theMasterActors = 
				StereotypeAndPropertySettings.getMasterActorList( theRhpPrj ); 
		
		ConfigurationSettings configSettings = new ConfigurationSettings(
				"FunctionalDesign.properties", 
				"FunctionalDesign_MessagesBundle" );
		
		createSampleModel( theRhpPrj, theMasterActors, configSettings );
	}
	
	public void openFunctionHierarchyBDD (){
		
		if( m_FunctionHierarchyBDD != null ){
			m_FunctionHierarchyBDD.openDiagram();
		}
	}
	
	public void openSystemContextDiagram (){
		
		if( m_SystemContextDiagram != null ){
			m_SystemContextDiagram.openDiagram();
		}
	}

	public static void createSampleModel(
			IRPPackage theRhpPrj,
			List<IRPActor> theMasterActors,
			ConfigurationSettings theConfigSettings ){

		ProfileVersionManager.checkAndSetProfileVersion( 
				false, 
				theConfigSettings, 
				true );

		String theNewTerm = "1.1 System-Level Design";

		TopLevelSystemDesignCreator theCreator = 
				new TopLevelSystemDesignCreator( 
						theRhpPrj, 
						theNewTerm,
						"PlatformManagementSystem", 
						"PMS", 
						"The top-level system",
						"Manage Platform",
						"Top-level system function.",
						theMasterActors );
		
		theCreator.openSystemContextDiagram();
		theCreator.openFunctionHierarchyBDD();
		
	}
	
	public TopLevelSystemDesignCreator(
			IRPPackage theRootEl,
			String theNewTerm,
			String theLongName,
			String theShortName,
			String theBlockDescription,
			String theFunctionName,
			String theFunctionDescription,
			List<IRPActor> theMasterActors ) {
		
		IRPPackage theProject = theRootEl.getProject();
		
		IRPPackage theRootPkg = (IRPPackage) theRootEl.addNewAggr(
				"Package", theLongName + "Pkg" );
		
		theRootPkg.changeTo( theNewTerm );
		
		IRPPackage theReqtsPkg = (IRPPackage) theRootPkg.addNewAggr(
				"Package", "Requirements_" + theLongName + "Pkg" );
		
		theReqtsPkg.changeTo("Requirements Package");
		theReqtsPkg.setSeparateSaveUnit( 0 );		
		
		IRPPackage theSystemContextPkg = (IRPPackage) theRootPkg.addNewAggr(
				"Package", "SystemContext_" + theLongName + "Pkg" );
		
		theSystemContextPkg.changeTo("System Context Package");
		theSystemContextPkg.setSeparateSaveUnit( 0 );

		IRPClass theBlock = theSystemContextPkg.addClass( theShortName );
		theBlock.changeTo( "Block" );
		theBlock.setDescription( theBlockDescription );
		
		IRPClass theFunctionBlock = theBlock.addClass( theFunctionName );
		theFunctionBlock.changeTo( "Function Block" );
		theFunctionBlock.setDescription( theFunctionDescription );
		
		m_SystemContextDiagram =
				theSystemContextPkg.addObjectModelDiagram( 
						"System Context Diagram - " + theLongName );
		
		m_SystemContextDiagram.changeTo( "System Context Diagram" );
		
		IRPCollection theCollection = 
				RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();
		
		for( IRPActor theNewActor : theMasterActors ){
			
			theNewActor.addRelationTo(
					(IRPClassifier) theFunctionBlock, 
					"", 
					"Association", 
					"1", 
					"", 
					"Association", 
					"1", 
					"" );
		}
		
		IRPGraphNode theNote =
				m_SystemContextDiagram.addNewNodeByType( 
						"Note", 21, 42, 156, 545 );
		
		String theNoteText = "The system context diagram is used to show the functional context of the system. It should only contain high-level functions, i.e. those that interact directly with external actors";
		
		theNote.setGraphicalProperty(
				"Text",
				theNoteText );
		
		int x0 = 520;
		int y0 = 370;
		int r = 300;

		int items = theMasterActors.size();
				
		String theDefaultActorSize = m_SystemContextDiagram.getPropertyValue("Format.Actor.DefaultSize");
		String[] theActorSplit = theDefaultActorSize.split(",");
		int actorWidth = Integer.parseInt( theActorSplit[2] );
		int actorHeight = Integer.parseInt( theActorSplit[3] );
		
		int useCaseWidth = 200;
		int useCaseHeight = 150;
		
		int xPadding = 100;
		int yPadding = 100;
		
	    IRPGraphNode theBlockGraphNode = 
	    		m_SystemContextDiagram.addNewNodeForElement( 
	    				theBlock, 
	    				x0-xPadding-(useCaseWidth/2), 
	    				y0-yPadding-(useCaseHeight/2), 
	    				useCaseWidth+(2*xPadding), 
	    				useCaseHeight+(2*yPadding) );
	    
	    theBlockGraphNode.setGraphicalProperty("StructureView", "True");
	   // StructureView=True
	    
	    IRPGraphNode theUCGraphNode = 
	    		m_SystemContextDiagram.addNewNodeForElement( 
	    				theFunctionBlock, 
	    				x0-(useCaseWidth/2), 
	    				y0-(useCaseHeight/2), 
	    				useCaseWidth, 
	    				useCaseHeight );

	    theCollection.addGraphicalItem( theUCGraphNode );
	    
		for(int i = 0; i < items; i++) {

		    int x = (int) (x0 + r * Math.cos(2 * Math.PI * i / items));
		    int y = (int) (y0 + r * Math.sin(2 * Math.PI * i / items));   
		    		    
		    IRPGraphNode theActorGN = m_SystemContextDiagram.addNewNodeForElement( 
		    		theMasterActors.get(i), 
		    		x-(actorWidth/2), 
		    		y-(actorHeight/2), 
		    		actorWidth, 
		    		actorHeight );
		    
		    theCollection.addGraphicalItem( theActorGN );
		}
	
		m_SystemContextDiagram.completeRelations(
				theCollection, 
				1);
						
		IRPPackage theFunctionsPkg = (IRPPackage) theRootPkg.addNewAggr(
				"Package", "Functions_" + theLongName + "Pkg" );
		
		theFunctionsPkg.changeTo("Function Breakdown Package");
		theFunctionsPkg.setSeparateSaveUnit( 0 );

		m_FunctionHierarchyBDD = 
				(IRPObjectModelDiagram) theFunctionsPkg.addNewAggr(
						"ObjectModelDiagram", 
						"Function Hierarchy BDD - " + theFunctionName );
		
		m_FunctionHierarchyBDD.changeTo( "Function Hierarchy - Block Definition Diagram" );
		m_FunctionHierarchyBDD.setSeparateSaveUnit( 0 );
		
		int width = 250;
		int height = 100;
		int x = 200;
		int y = 50;
		int shiftX = 50;
		int shiftY = 50;
		
	    @SuppressWarnings("unused")
		IRPGraphNode theFBGraphNode = 
	    		m_FunctionHierarchyBDD.addNewNodeForElement( 
	    				theFunctionBlock, x, y, width, height );
	    
		IRPHyperLink theHyperLink = (IRPHyperLink) theFunctionBlock.addNewAggr("HyperLink", "");
		theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");
		theHyperLink.setTarget( m_FunctionHierarchyBDD );
		
		List<IRPModelElement> theCandidateFunctions = 
				GeneralHelpers.findElementsWithMetaClassAndStereotype(
						"Class", 
						"Function Block", 
						theProject, 
						1 );
		
		List<IRPClass> theTopLevelFDSFunctions = new ArrayList<>();
		
		for (IRPModelElement theCandidateFunction : theCandidateFunctions) {
			
			if( theCandidateFunction.getOwner() instanceof IRPClass &&
					!theCandidateFunction.equals( theFunctionBlock ) ){
				
				theTopLevelFDSFunctions.add( (IRPClass) theCandidateFunction );
			}
		}
		
		for (IRPClass theTopLevelFDSFunction : theTopLevelFDSFunctions) {
			
			x += shiftX;
			y += shiftY;
			
		    @SuppressWarnings("unused")
			IRPGraphNode theGraphNode = 
		    		m_FunctionHierarchyBDD.addNewNodeForElement( 
		    				theTopLevelFDSFunction, x, y, width, height );
		}
		
		List<IRPModelElement> theMasterActorStereotypes =
				StereotypeAndPropertySettings.getStereotypesForMasterActorPackage( 
						theProject );
		
		for( IRPModelElement theMasterActorStereotype : theMasterActorStereotypes ){
			
			List<IRPModelElement> theActorPackages = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"Package", 
							theMasterActorStereotype.getName(), 
							theProject, 
							1 );

			for (IRPModelElement theActorPackage : theActorPackages) {
				
				boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"Do you want to nest " + Logger.elementInfo( theActorPackage ) + 
						" underneath " + Logger.elementInfo( theRootPkg ) + "?" );
				
				if( theAnswer ){
					theActorPackage.setOwner( theRootPkg );
				}
			}
		}
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #250 29-MAY-2019: First official version of new FunctionalDesignProfile  (F.J.Chadburn)

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