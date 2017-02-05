package requirementsanalysisplugin;

import generalhelpers.TraceabilityHelper;

import com.telelogic.rhapsody.core.*;

public class RelationInfo {
	
	private DiagramElementInfo m_StartElement;
	private DiagramElementInfo m_EndElement;
	private IRPStereotype m_RelationType;
	
	public RelationInfo(
			DiagramElementInfo fromStartElement,
			DiagramElementInfo toEndElement, 
			IRPStereotype withRelationType ){
		
		super();
		this.m_StartElement = fromStartElement;
		this.m_EndElement = toEndElement;
		this.m_RelationType = withRelationType;
	}
	
	public DiagramElementInfo getStartElement() {
		return m_StartElement;
	}
	
	public DiagramElementInfo getEndElement() {
		return m_EndElement;
	}
	
	public IRPStereotype getRelationType() {
		return m_RelationType;
	}
	
	public int getExistingCount(){
		
		return TraceabilityHelper.countStereotypedDependencies(
				m_StartElement.getElement(),
				m_EndElement.getElement(),
				m_RelationType.getName() );
	}
	
	public IRPDependency getExistingStereotypedDependency(){
		
		return TraceabilityHelper.getExistingStereotypedDependency(
				m_StartElement.getElement(),
				m_EndElement.getElement(),
				m_RelationType.getName() );
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    
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
