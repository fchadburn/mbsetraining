package generalhelpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class AssociationInfo {

	protected IRPRelation m_OldRelation = null;
	protected IRPRelation m_NewRelation = null;
	protected IRPActor m_NewActor = null;
	protected Set<IRPGraphEdge> m_GraphEdges = new HashSet<>();
	protected Set<IRPDiagram> m_Diagrams = new HashSet<>();
	
	public AssociationInfo( 
			IRPRelation theOldRelation, 
			IRPRelation theNewRelation ) {

		IRPProject theProject = theOldRelation.getProject();
		
		m_OldRelation = theOldRelation;
		m_NewRelation = theNewRelation;
		
		IRPClassifier theOfClass = m_NewRelation.getOfClass();
		
		if( theOfClass == null || !( theOfClass instanceof IRPActor ) ){
			Logger.writeLine("Error in AssociationInfo constructor, actor not found");
		}
		
		m_NewActor = (IRPActor) theOfClass;

		@SuppressWarnings("unchecked")
		List<IRPDiagram> theDiagrams = 
			theProject.getNestedElementsByMetaClass( "UseCaseDiagram", 1 ).toList();
		
		for( IRPDiagram theDiagram : theDiagrams ){
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = 
					theDiagram.getCorrespondingGraphicElements( theOldRelation ).toList();
			
			for( IRPGraphElement theGraphEl : theGraphEls ){
				
				if( theGraphEl instanceof IRPGraphEdge ){
					m_GraphEdges.add( (IRPGraphEdge) theGraphEl );
					m_Diagrams.add( theDiagram );
				}
			}
		}
	}
	
	public IRPRelation getM_OldRelation() {
		return m_OldRelation;
	}

	public IRPRelation getM_NewRelation() {
		return m_NewRelation;
	}
	
	public IRPActor getM_NewActor() {
		return m_NewActor;
	}
	
	public Set<IRPGraphEdge> getM_GraphEdges() {
		return m_GraphEdges;
	}
	
	public Set<IRPDiagram> getM_Diagrams() {
		return m_Diagrams;
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #212 04-JUL-2017: Added a MergeActors helper, currently only invoked via Eclipse (F.J.Chadburn) 

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