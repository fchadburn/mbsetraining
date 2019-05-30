package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import com.telelogic.rhapsody.core.IRPModelElement;

public class RhapsodyComboBox extends JComboBox<Object>{
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<IRPModelElement> m_List = null;
	List<String> nameList;
	
	public RhapsodyComboBox(List<IRPModelElement> inList, Boolean isFullPathRequested){
		
		super();
		
		m_List = inList;
		
		nameList = new ArrayList<String>();
		
		for (int i = 0; i < m_List.size(); i++) {
			if (isFullPathRequested){
				nameList.add(i, m_List.get(i).getFullPathName());
			} else {
				nameList.add(i, m_List.get(i).getName());
			}
		} 	
		
		
		insertItemAt("Nothing", 0);
		
		for (String theName : nameList) {
			this.addItem( theName );
		}
		
		this.setSelectedItem("Nothing");
		
	}
	
	public IRPModelElement getSelectedRhapsodyItem(){
		
		IRPModelElement theModelEl = null;
		
		Object theSelectedItem = this.getSelectedItem();	
		
		if (!theSelectedItem.equals("Nothing")){
			int index = nameList.indexOf(theSelectedItem);		
			theModelEl = m_List.get(index);
		}

		return theModelEl;
	}
	
	public void setSelectedRhapsodyItem(
			IRPModelElement toTheElement ){
				
		for( int i = 0; i < m_List.size(); i++ ){

			IRPModelElement theElement = m_List.get( i );
			
			if( theElement != null && 
				theElement.equals( toTheElement ) ){
				
				setSelectedIndex( i+1 );
				break;
			}
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #220 12-JUL-2017: Added customisable Stereotype choice to the Block and block/Part creation dialogs (F.J.Chadburn) 

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
