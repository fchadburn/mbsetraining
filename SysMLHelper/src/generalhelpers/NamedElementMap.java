package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class NamedElementMap {

	private List<String> m_ShortNames = new ArrayList<String>();
	private List<String> m_FullNames = new ArrayList<String>();
	private List<IRPModelElement> m_ModelElements;
	
	public NamedElementMap(
			List<IRPModelElement> theModelEls) {
		
		m_ModelElements = theModelEls;
		
		for (int i = 0; i < m_ModelElements.size(); i++) {
			m_FullNames.add(i, m_ModelElements.get(i).getFullPathName());
			m_ShortNames.add(i, m_ModelElements.get(i).getName());
		} 	
	}
	
	public Object[] getShortNames(){
		return m_ShortNames.toArray();
	}
	
	public Object[] getFullNames(){
		return m_FullNames.toArray();
	}
	
	
	public IRPModelElement getElementUsingShortName( Object theShortName ){
		
		int index = m_ShortNames.indexOf( theShortName );
		IRPModelElement theEl = m_ModelElements.get( index );
		return theEl;
	}
	
	public IRPModelElement getElementUsingFullName( Object theFullName ){
		
		int index = m_FullNames.indexOf( theFullName );
		IRPModelElement theEl = m_ModelElements.get( index );
		return theEl;
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
