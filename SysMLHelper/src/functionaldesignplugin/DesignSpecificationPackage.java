package functionaldesignplugin;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.HYPNameType;
import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPHyperLink;
import com.telelogic.rhapsody.core.IRPObjectModelDiagram;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;

public class DesignSpecificationPackage {
	
	String _packageName = null;
	String _newTermStereotypeName = null;
	String _shortName = null;
	String _description = null;
	String _functionName = null;
	String _functionDescription = null;
	boolean _isCreateParametricDiagram = false;
	IRPPackage _rootEl;
	List<IRPActor> _masterActors;
	
	IRPObjectModelDiagram m_FunctionHierarchyBDD = null;
	IRPObjectModelDiagram m_SystemContextDiagram = null;

	public DesignSpecificationPackage(
			IRPPackage theRootEl,
			List<IRPActor> theMasterActors,
			String _packageName,
			String _newTermStereotypeName, 
			String _shortName,
			String _description, 
			String _functionName,
			String _functionDescription, 
			boolean _isCreateParametricDiagram) {
		
		this._rootEl = theRootEl;
		this._masterActors = theMasterActors;
		this._packageName = _packageName;
		this._newTermStereotypeName = _newTermStereotypeName;
		this._shortName = _shortName;
		this._description = _description;
		this._functionName = _functionName;
		this._functionDescription = _functionDescription;
		this._isCreateParametricDiagram = _isCreateParametricDiagram;
	}
	
	public String get_packageName() {
		return _packageName;
	}
	public void set_packageName(String _packageName) {
		this._packageName = _packageName;
	}
	public String get_newTermStereotypeName() {
		return _newTermStereotypeName;
	}
	public void set_newTermStereotypeName(String _newTermStereotypeName) {
		this._newTermStereotypeName = _newTermStereotypeName;
	}
	public String get_shortName() {
		return _shortName;
	}
	public void set_shortName(String _shortName) {
		this._shortName = _shortName;
	}
	public String get_description() {
		return _description;
	}
	public void set_description(String _description) {
		this._description = _description;
	}
	public String get_functionName() {
		return _functionName;
	}
	public void set_functionName(String _functionName) {
		this._functionName = _functionName;
	}
	public String get_functionDescription() {
		return _functionDescription;
	}
	public void set_functionDescription(String _functionDescription) {
		this._functionDescription = _functionDescription;
	}
	public boolean is_isCreateParametricDiagram() {
		return _isCreateParametricDiagram;
	}
	public void set_isCreateParametricDiagram(boolean _isCreateParametricDiagram) {
		this._isCreateParametricDiagram = _isCreateParametricDiagram;
	}

	public void dumpPackage(){
		Logger.writeLine( "_packageName = " + _packageName );
		Logger.writeLine( "_newTermStereotypeName = " + _newTermStereotypeName );
		Logger.writeLine( "_shortName = " + _shortName );
		Logger.writeLine( "_description = " + _description );
		Logger.writeLine( "_functionName = " + _functionName );
		Logger.writeLine( "_functionDescription = " + _functionDescription );
		Logger.writeLine( "_isCreateParametricDiagram = " + _isCreateParametricDiagram );

	}
	
	public String getErrorMsg(){
		
		String errorMsg = ""; // valid
				
		boolean isUnique = GeneralHelpers.isElementNameUnique(
				this._packageName + "Pkg", "Package", this._rootEl, 0 );
		
		if( !isUnique ){
			
			errorMsg += "Sorry, " + this._packageName + "Pkg" +
					" will not be unique and clashes with existing package under " + 
					Logger.elementInfo( this._rootEl );
		}
		
		String theRegEx = this._rootEl.getPropertyValue( 
				"General.Model.NamesRegExp" );

		Logger.writeLine("Checking " + this._packageName + " against the NamesRegExp '" + theRegEx + "'");
								
		if( this._packageName != null && !this._packageName.matches( theRegEx ) ){

			errorMsg += "Sorry, " + this._packageName + 
					" is not valid name (NamesRegExp = " + theRegEx + ")";
		}
		
		return errorMsg;
	}
	
	public void createPackage() {
		
		if( _newTermStereotypeName == null ){
			Logger.error( "createPackage has detected that the _newTermStereotypeName is null" );
		}

		if( _packageName == null ){
			Logger.error( "createPackage has detected that the _packageName is null" );
		}
		
		IRPPackage theFDSPkg = (IRPPackage) this._rootEl.addNewAggr(
				"Package", _packageName + "Pkg" );

		theFDSPkg.changeTo( _newTermStereotypeName );

		// Create nested package for requirements		
		IRPPackage theActorsPkg = 
				GeneralHelpers.addNewTermPackageAndSetUnitProperties(
						"Actors_" + _packageName + "Pkg",
						theFDSPkg,
						StereotypeAndPropertySettings.getActorPackageStereotype  ( 
								theFDSPkg ) );

		List<IRPActor> theNewActors = new ArrayList<>();

		for( IRPActor theMasterActor : this._masterActors ){
			IRPActor theNewActor = 
					theActorsPkg.addActor( 
							theMasterActor.getName() + "_" + _shortName );

			theNewActor.addGeneralization( theMasterActor );
			theNewActors.add( theNewActor );
		}

		if( _isCreateParametricDiagram ){
			
			// Create nested package for parametrics		
			@SuppressWarnings("unused")
			IRPPackage theParametricsPkg = 
					GeneralHelpers.addNewTermPackageAndSetUnitProperties(
							"Parametrics_" + _packageName + "Pkg",
							theFDSPkg,
							StereotypeAndPropertySettings.getParametricsPackageStereotype( 
									theFDSPkg ) );
		}

		// Create nested package for requirements		
		@SuppressWarnings("unused")
		IRPPackage theReqtsPkg = 
				GeneralHelpers.addNewTermPackageAndSetUnitProperties(
						"Requirements_" + _packageName + "Pkg",
						theFDSPkg,
						StereotypeAndPropertySettings.getRequirementPackageStereotype ( 
								theFDSPkg ) );

		// Create nested package for block		
		IRPPackage theSystemContextPkg = 
				GeneralHelpers.addNewTermPackageAndSetUnitProperties(
						"SystemContext_" + _packageName + "Pkg",
						theFDSPkg,
						StereotypeAndPropertySettings.getSystemContextPackageStereotype( 
								theFDSPkg ) );

		IRPClass theBlock = theSystemContextPkg.addClass( _shortName );
		theBlock.changeTo( "Block" );
		theBlock.setDescription( _description );
		theBlock.setDisplayName( _packageName ); // Set Label to full name

		IRPClass theFunctionBlock = theBlock.addClass( _functionName );
		theFunctionBlock.changeTo( "Function Block" );
		theFunctionBlock.setDescription( _functionDescription );

		m_SystemContextDiagram =
				theSystemContextPkg.addObjectModelDiagram( 
						"System Context Diagram - " + _packageName );

		m_SystemContextDiagram.changeTo( "System Context Diagram" );

		IRPCollection theCollection = 
				RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();

		for( IRPActor theNewActor : theNewActors ){

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
						"Note", 21, 42, 156, 150 );

		String theNoteText = "The system context diagram is used to show the functional context of the system. It should only contain high-level functions, i.e. those that interact directly with external actors";

		theNote.setGraphicalProperty(
				"Text",
				theNoteText );

		int x0 = 520;
		int y0 = 370;
		int r = 300;

		int items = theNewActors.size();

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
					theNewActors.get(i), 
					x-(actorWidth/2), 
					y-(actorHeight/2), 
					actorWidth, 
					actorHeight );

			theCollection.addGraphicalItem( theActorGN );
		}

		m_SystemContextDiagram.completeRelations(
				theCollection, 
				1);

		IRPPackage theFunctionsPkg = (IRPPackage) theFDSPkg.addNewAggr(
				"Package", "Functions_" + _packageName + "Pkg" );

		theFunctionsPkg.changeTo("Function Breakdown Package");
		theFunctionsPkg.setSeparateSaveUnit( 0 );

		m_FunctionHierarchyBDD = 
				(IRPObjectModelDiagram) theFunctionsPkg.addNewAggr(
						"ObjectModelDiagram", 
						"Function Hierarchy BDD - " + _functionName );

		m_FunctionHierarchyBDD.changeTo( "Function Hierarchy - Block Definition Diagram" );
		m_FunctionHierarchyBDD.setSeparateSaveUnit( 0 );

		@SuppressWarnings("unused")
		IRPGraphNode theFBGraphNode = 
				m_FunctionHierarchyBDD.addNewNodeForElement( 
						theFunctionBlock, 200, 50, 250, 100 );

		IRPHyperLink theHyperLink = (IRPHyperLink) theFunctionBlock.addNewAggr("HyperLink", "");
		theHyperLink.setDisplayOption(HYPNameType.RP_HYP_NAMETEXT, "");
		theHyperLink.setTarget( m_FunctionHierarchyBDD );
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