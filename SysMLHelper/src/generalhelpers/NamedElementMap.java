package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class NamedElementMap {

	private List<String> m_ShortNames = new ArrayList<String>();
	private List<String> m_FullNames = new ArrayList<String>();
	private List<String> m_FullNamesIn = new ArrayList<String>();
	
	private List<IRPModelElement> m_ModelElements;
	
	public NamedElementMap(
			List<IRPModelElement> theModelEls) {
		
		m_ModelElements = theModelEls;
		
		for (int i = 0; i < m_ModelElements.size(); i++) {
			m_FullNames.add(i, m_ModelElements.get(i).getFullPathName());
			m_ShortNames.add(i, m_ModelElements.get(i).getName());
			m_FullNamesIn.add(i, m_ModelElements.get(i).getFullPathNameIn());
		} 	
	}
	
	public Object[] getShortNames(){
		return m_ShortNames.toArray();
	}
	
	public Object[] getFullNames(){
		return m_FullNames.toArray();
	}

	public Object[] getFullNamesIn(){
		return m_FullNamesIn.toArray();
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
	
	public IRPModelElement getElementUsingFullNameIn( Object theFullNameIn ){
		
		int index = m_FullNamesIn.indexOf( theFullNameIn );
		IRPModelElement theEl = m_ModelElements.get( index );
		return theEl;
	}
	
	public IRPModelElement getElementAt( int index ){
		
		IRPModelElement theEl = m_ModelElements.get( index );
		return theEl;
	}
	
	public void addList( List<IRPModelElement> theList ){
		
		for (IRPModelElement theModelEl : theList) {
			
			if (!m_ModelElements.contains(theModelEl)){
				
				m_ModelElements.add( theModelEl );
				m_FullNames.add( theModelEl.getFullPathName() );
				m_ShortNames.add( theModelEl.getName() );
				m_FullNamesIn.add( theModelEl.getFullPathNameIn() );
			}
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive new requirement to CallOperations and Event Actions (F.J.Chadburn)
    #155 25-JAN-2017: Added new panel to find and delete Gateway Deleted_At_High_Level req'ts with Rhp 8.2 (F.J.Chadburn)

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
