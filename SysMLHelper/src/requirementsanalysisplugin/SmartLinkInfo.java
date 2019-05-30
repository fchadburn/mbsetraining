package requirementsanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.TraceabilityHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class SmartLinkInfo {

	private DiagramElementList m_StartLinkElements;
	private DiagramElementList m_EndLinkElements;
	private IRPStereotype m_RelationType;
	private boolean m_IsPopulatePossible;	
	private int m_CountRelationsNeeded;
	private Set<RelationInfo> m_RelationInfos;
	
	public SmartLinkInfo(
			List<IRPModelElement> theStartLinkEls,
			List<IRPGraphElement> theStartLinkGraphEls,
			List<IRPModelElement> theEndLinkEls,
			List<IRPGraphElement> theEndLinkGraphEls ){
		
		m_RelationInfos = new HashSet<RelationInfo>();
		
		if( m_StartLinkElements != null ){
			m_StartLinkElements.clear();			
		}
		
		m_StartLinkElements = new DiagramElementList( 
				theStartLinkEls, theStartLinkGraphEls );
		
		if( m_EndLinkElements != null ){
			m_EndLinkElements.clear();			
		}
		
		m_EndLinkElements = new DiagramElementList( 
				theEndLinkEls, theEndLinkGraphEls );
		
		m_IsPopulatePossible = false;
		
		
		IRPProject theRhpProject = RequirementsAnalysisPlugin.getActiveProject();

		IRPModelElement contextEl = theEndLinkEls.get(0);
		
		if( m_StartLinkElements.areElementsAllReqts() ){

			m_RelationType = GeneralHelpers.getExistingStereotype(
					"deriveReqt", theRhpProject );

		} else if( m_StartLinkElements.areElementsAllDeriveDependencySources() ){

			m_RelationType = StereotypeAndPropertySettings.getStereotypeToUseForActions( 
					contextEl );

		} else if( m_StartLinkElements.areElementsAllRefinementDependencySources() ){

			m_RelationType = StereotypeAndPropertySettings.getStereotypeToUseForUseCases( 
					contextEl );

		} else if( m_StartLinkElements.areElementsAllVerificationDependencySources() ){

			m_RelationType = GeneralHelpers.getExistingStereotype( "verify", theRhpProject );
			
		} else if( m_StartLinkElements.areElementsAllSatisfyDependencySources() ){

			m_RelationType = StereotypeAndPropertySettings.getStereotypeToUseForFunctions( 
					contextEl );

		} else {

			m_RelationType = null;
			Logger.error( "Unable to find relation type" );
		}
		
		Logger.info( "SmartLinkInfo: Determined that relation type needed is " + 
				Logger.elementInfo( m_RelationType ) );
		
		if( m_RelationType != null ){
			
			for( DiagramElementInfo theStartLinkEl : m_StartLinkElements ){
				
				for( DiagramElementInfo theEndLinkEl : m_EndLinkElements ){
					
					RelationInfo theRelationInfo = new RelationInfo(
							theStartLinkEl, theEndLinkEl, m_RelationType );

					m_RelationInfos.add( theRelationInfo );		
					
					boolean isPopulatePossibleForRelation = 
							performPopulateOnDiagram(
									theRelationInfo,
									true );
					
					if( isPopulatePossibleForRelation ){
						m_IsPopulatePossible = true;
					}
				}
			}
		}
		
		m_CountRelationsNeeded = 0;
		
		for( RelationInfo relationInfo : m_RelationInfos ){
			
			if( relationInfo.getExistingStereotypedDependency() == null ){
				m_CountRelationsNeeded++;
			}
		}		
	}

	private boolean performPopulateOnDiagram(
			RelationInfo theRelationInfo,
			boolean isJustCheckWithoutDoing ){
		
		IRPDependency existingDependency = 
				theRelationInfo.getExistingStereotypedDependency();

		boolean isPopulatePossible = false;
		
		for( IRPGraphElement theStartGraphEl : theRelationInfo.getStartElement().getGraphEls() ){

			for( IRPGraphElement theEndGraphEl : theRelationInfo.getEndElement().getGraphEls() ){

				if( theStartGraphEl.getDiagram().equals( theEndGraphEl.getDiagram() )){

					IRPDiagram theDiagram = theStartGraphEl.getDiagram();

					if( existingDependency == null ){
						
						isPopulatePossible = true;
						
					} else { // check if relation is already shown on diagram

						@SuppressWarnings("unchecked")
						List<IRPGraphElement> theExistingGraphEls =
								theDiagram.getCorrespondingGraphicElements( 
										existingDependency ).toList();

						if( theExistingGraphEls.isEmpty() ){

							Logger.info( "Determined graphEdge needed for " + 
									Logger.elementInfo( m_RelationType ) + " from " + 
									Logger.elementInfo( theStartGraphEl.getModelObject() ) + " to " + 
									Logger.elementInfo( theEndGraphEl.getModelObject() ) + " on " +
									Logger.elementInfo( theDiagram ) );

							isPopulatePossible = true;
							
							if( !isJustCheckWithoutDoing ){

								LayoutHelper.drawDependencyToMidPointsFor(
										existingDependency, 
										theStartGraphEl, 
										theEndGraphEl,
										theDiagram );
							}

						} else {
							
							Logger.info( "Determined graphEdge for " + 
									Logger.elementInfo( m_RelationType ) + " already exists from " + 
									Logger.elementInfo( theStartGraphEl.getModelObject() ) + " to " + 
									Logger.elementInfo( theEndGraphEl.getModelObject() ) + " on " +
									Logger.elementInfo( theDiagram ) );
						}
					}
				}

			}
		}
		return isPopulatePossible;
	}
	
	public String getDescriptionHTML(){
		
		String theMsg = "<html><div style=\"width:300px;\">";
		
		theMsg+= "<p style=\"text-align:center;font-weight:normal\">";
		
		if( m_StartLinkElements.size() == 1 && m_EndLinkElements.size()==1 ){
			theMsg+="Create a ";
		} else {
			theMsg+="Create ";
		}
		
		theMsg+= "<span style=\"font-weight:bold\">«" +  m_RelationType.getName() + "»</span>";
		
		if( m_StartLinkElements.size() == 1 && m_EndLinkElements.size()==1 ){
			theMsg+=" dependency from:</p>";
		} else {
			theMsg+=" dependencies from:</p>";
		}
		
		theMsg+="<p></p>";
		theMsg+="<p style=\"text-align:center;font-weight:normal\">";
		
		if( m_StartLinkElements.size() == 1 ){
			theMsg+= m_StartLinkElements.size() + " element (a ";
		} else {
			theMsg+= m_StartLinkElements.size() + " elements (a "; 
		}

		theMsg+= m_StartLinkElements.getCommaSeparatedListOfElementsHTML( 3 );
		theMsg+=")</p>";
		theMsg+="<p></p>";
		theMsg+="<p style=\"text-align:center;font-weight:normal\">to:</p>";
		theMsg+="<p></p>";
		theMsg+="<p style=\"text-align:center;font-weight:normal\">";
		
		if( m_EndLinkElements.size() == 1 ){
			theMsg+= m_EndLinkElements.size() + " element (a  ";
		} else {
			theMsg+= m_EndLinkElements.size() + " elements (a  ";
		}
		
		theMsg+= m_EndLinkElements.getCommaSeparatedListOfElementsHTML( 3 );						
		theMsg+= ")</p>";
		theMsg+="<p></p>";
		
		if( m_CountRelationsNeeded > 0 ){
			
			theMsg+= "<p style=\"text-align:center;font-weight:normal\">" + m_CountRelationsNeeded + " new dependencies will be created" + "</p>";
			
		} else if ( getIsPopulatePossible()==false ){
			
			theMsg+= "<p style=\"text-align:center;font-weight:normal\">" + "There is nothing to do, i.e. relations already exist and/or are shown" + "</p>";
			
		} else {
			
			theMsg+= "<p style=\"text-align:center;font-weight:normal\">" + "These relations already exist (but are not shown)" + "</p>";

		}
		
		theMsg+="<p></p>";
		theMsg+="<p></p>";
		theMsg+="</div></html>";
		
		return theMsg;
	}
	
	public boolean getIsPopulatePossible(){
		
		return m_IsPopulatePossible;
	}
	
	public boolean getAreNewRelationsNeeded(){
		
		return ( m_CountRelationsNeeded > 0 );
	}
	
	protected boolean isDeriveDependencyNeeded(){
		
		boolean isNeeded = 
				m_StartLinkElements.areElementsAllDeriveDependencySources() && 
				m_EndLinkElements.areElementsAllReqts();
		
		Logger.info( "isDeriveDependencyNeeded is returning " + isNeeded );
		
		return isNeeded;
	}
	
	public void createDependencies( 
			boolean withPopulateOnDiagram ){
		
		for( RelationInfo theRelationInfo : m_RelationInfos ){
			
			IRPDependency theDependency = 
					theRelationInfo.getExistingStereotypedDependency();
			
			if( theDependency == null ){
				
				theDependency = TraceabilityHelper.addStereotypedDependencyIfOneDoesntExist(
						theRelationInfo.getStartElement().getElement(), 
						theRelationInfo.getEndElement().getElement(), 
						m_RelationType );
			}
			
			performPopulateOnDiagram(
					theRelationInfo,
					false );
		}
	}
}

/**
 * Copyright (C) 2017-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    #204 18-JUN-2017: Refine menu for invoking Smart Link panel and add FlowPort/EventReceptions support (F.J.Chadburn)
    #221 12-JUL-2017: Fixed Smart Link dialog to draw from middle of IRPGraphNodes rather than top left (F.J.Chadburn)
    #224 25-AUG-2017: Added new menu to roll up traceability to the transition and populate on STM (F.J.Chadburn)
    #227 06-SEP-2017: Increased robustness to stop smart link panel using non new term version of <<refine>> (F.J.Chadburn)
    #242 04-OCT-2017: Get re-layout dependencies on diagrams(s) menu to centre on graph edges properly (F.J.Chadburn)
    #243 04-OCT-2017: Added ability to do smart link from a testcase to create a Verification (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)

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