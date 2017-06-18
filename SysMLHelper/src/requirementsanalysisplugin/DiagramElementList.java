package requirementsanalysisplugin;

import generalhelpers.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class DiagramElementList extends HashSet<DiagramElementInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DiagramElementList(
			List<IRPModelElement> theModelEls,
			List<IRPGraphElement> theGraphEls ) {
		
		for( IRPModelElement theModelEl : theModelEls ){
			
			DiagramElementInfo theDiagramElementInfo = 
					new DiagramElementInfo( theModelEl, theGraphEls );
			
			add( theDiagramElementInfo );
		}
	}
	
	public boolean areElementsAllDeriveDependencySources(){

		final String[] activityDiagramSpecificMetaClasses = {
			"State", "Transition", "AcceptEventAction", "SendAction", "AcceptTimeEvent", "ControlFlow" };

		final String[] nonActivityDiagramSpecificMetaClasses = {
			"Comment", "Constraint", "Precondition", "Postcondition", };
		
		boolean isMatchFoundForAll = true;
		
		for( DiagramElementInfo theElInfo : this ){
			
			IRPModelElement theEl = theElInfo.getElement();
			
			if( !doesElementMatchOneOfTheTypes( 
					theEl, activityDiagramSpecificMetaClasses ) && 
				!doesElementMatchOneOfTheTypes( 
					theEl, nonActivityDiagramSpecificMetaClasses ) ){
				
				isMatchFoundForAll = false;
				break;
				
			} else if ( doesElementMatchOneOfTheTypes( 
				theEl, activityDiagramSpecificMetaClasses ) &&
				!(theEl.getOwner() instanceof IRPFlowchart) ) { // it does match but is not owned by an AD

				isMatchFoundForAll = false;
				Logger.writeLine( theEl.getOwner(), "is the owner of " + Logger.elementInfo( theEl));
				break;
			}
		}
		
		Logger.writeLine( "areElementsAllDeriveDependencySources is returning " + isMatchFoundForAll );
		
		return isMatchFoundForAll;
	}
	
	public boolean areElementsAllSatisfyDependencySources(){

		final String[] theSatisfyStatechartDiagramMetaClasses = {
			"Transition", "State" , "DefaultTransition" };

		final String[] theSatisfyNonDiagramMetaClasses = {
			"Operation", "Event", "EventReception", "SysMLPort", "Attribute" };
		
		boolean isMatchFoundForAll = true;
		
		for( DiagramElementInfo DiagramElementInfo : this ){
			
			IRPModelElement theEl = DiagramElementInfo.getElement();
			
			boolean isADiagramSatisfyMetaClass = 
					doesElementMatchOneOfTheTypes(
							theEl, theSatisfyStatechartDiagramMetaClasses );

			boolean isANonDiagramSatisfyMetaClass = 
					doesElementMatchOneOfTheTypes(
							theEl, theSatisfyNonDiagramMetaClasses );
			
			if( !isADiagramSatisfyMetaClass && !isANonDiagramSatisfyMetaClass ){
				
				isMatchFoundForAll = false;
				break;
				
			} else if (isADiagramSatisfyMetaClass && 
					   !(theEl.getOwner() instanceof IRPStatechart) ){
					
				isMatchFoundForAll = false;
				Logger.writeLine( theEl.getOwner(), "is the owner of " + Logger.elementInfo( theEl ));
				break;
			}
		}
		
		Logger.writeLine( "areElementsAllSatisfyDependencySources is returning " + isMatchFoundForAll );
		
		return isMatchFoundForAll;
	}
	
	public boolean areElementsAllRefinementDependencySources(){

		final String[] theRefinementDiagramMetaClasses = {
				"UseCase" };
		
		boolean isMatchFoundForAll = true;
		
		for( DiagramElementInfo theEl : this ){
			
			if( !doesElementMatchOneOfTheTypes( 
					theEl.getElement(), theRefinementDiagramMetaClasses ) ){
				
				isMatchFoundForAll = false;
				break;
			}
		}
		
		Logger.writeLine( "areElementsAllRefinementDependencySources is returning " + isMatchFoundForAll );
		
		return isMatchFoundForAll;
	}
	
	public boolean areElementsAllReqts(){
		
		final String[] reqtMetaClasses = { "Requirement" };
		
		boolean isMatchFoundForAll = true;
		
		for( DiagramElementInfo theEl : this ){
			
			if( !doesElementMatchOneOfTheTypes( 
					theEl.getElement(), reqtMetaClasses ) ){
				
				isMatchFoundForAll = false;
				break;
			}
		}
		
		Logger.writeLine( "areElementsAllReqts is returning " + isMatchFoundForAll );

		return isMatchFoundForAll;
		
	}
	
	protected boolean doesElementMatchOneOfTheTypes( 
			IRPModelElement theEl, 
			String[] theMetaClasses ){
		
		boolean isMatchFound = false;
		
		Logger.writeLine("doesElementMatchOneOfTheTypes was invoked...");
		
		for( String theMetaClass : theMetaClasses ){ 
			
			if( theEl.getMetaClass().equals( theMetaClass ) ){
				
				Logger.writeLine("doesElementMatchOneOfTheTypes invoked for " + Logger.elementInfo( theEl ) + " with match found to a " + theMetaClass );
				isMatchFound = true;
				break;
			} else {
				Logger.writeLine("doesElementMatchOneOfTheTypes invoked for " + Logger.elementInfo( theEl ) + " with no match found to a " + theMetaClass );

			}
		}
		
		Logger.writeLine("...doesElementMatchOneOfTheTypes has completed (returning " + isMatchFound + ")" );

		return isMatchFound;
	}
	
	protected boolean doesListHaveElementsOnADiagram(){
		
		boolean isOnDiagram = false;
		
		for( DiagramElementInfo theEl : this ){
			
			if( theEl.isThereAGraphElement() ){
				isOnDiagram = true;
			}
		}
		
		return isOnDiagram;
	}
	
	public String getCommaSeparatedListOfElementsHTML(int max){
		
		int count = 0;
		
		String theList = "";
		
		Iterator<DiagramElementInfo> it = this.iterator();
		
		while( it.hasNext() && count < max ){
			
			IRPModelElement theEl = it.next().getElement();
			
			theList+= "<span style=\"font-weight:italics\">" + theEl.getMetaClass() + "</span> " + 
			          " called <span style=\"font-weight:bold\">" + theEl.getName() + "</span>";
			count++;
			
			if( count == max && it.hasNext() ){
				theList+= ", ...";
			} else if( it.hasNext() ){
				theList+= ", ";
			}
		}
		
		return theList;
	}
	
	public Set<IRPDiagram> getDiagrams(){
		
		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();
		
		for( DiagramElementInfo theDiagramElementInfo : this ){
			
			Set<IRPGraphElement> theGraphEls = theDiagramElementInfo.getGraphEls();
			
			for (IRPGraphElement theGraphEl : theGraphEls) {
				
				IRPDiagram theDiagram = theGraphEl.getDiagram();
				theDiagrams.add( theDiagram );
			}
		}
		
		return theDiagrams;
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    #204 18-JUN-2017: Refine menu for invoking Smart Link panel and add FlowPort/EventReceptions support (F.J.Chadburn)
    
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
