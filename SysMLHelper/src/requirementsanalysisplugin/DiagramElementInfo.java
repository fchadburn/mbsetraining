package requirementsanalysisplugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class DiagramElementInfo {

	private IRPModelElement m_Element = null;
	
	public IRPModelElement getElement() {
		return m_Element;
	}

	private Set<IRPGraphElement> m_GraphEls = new HashSet<IRPGraphElement>();
	
	public DiagramElementInfo(
			IRPModelElement theModelEl,
			List<IRPGraphElement> theGraphEls ){
		
		m_Element = theModelEl;
		
		for( IRPGraphElement theCandidateGraphEl : theGraphEls ){
			
			if( theCandidateGraphEl.getModelObject().equals( theModelEl ) ){
				
				m_GraphEls.add( theCandidateGraphEl );
			}
		}
	}
	
	public boolean isThereAGraphElement(){
		
		return !m_GraphEls.isEmpty();
		
	}
	
	public Set<IRPGraphElement> getGraphEls(){
		
		return m_GraphEls;
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
