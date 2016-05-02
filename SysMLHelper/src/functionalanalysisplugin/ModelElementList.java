package functionalanalysisplugin;

import generalhelpers.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class ModelElementList extends ArrayList<IRPModelElement>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ModelElementList() {
		super();
	}

	public ModelElementList(Collection<? extends IRPModelElement> c) {
		super(c);
	}

	public ModelElementList(int initialCapacity) {
		super(initialCapacity);
	}

	public ModelElementList getListFilteredBy(String theMetaClass){
		
		ModelElementList theList = new ModelElementList();
		
		for (IRPModelElement theEl : this) {
			if (theEl.getMetaClass().equals(theMetaClass)){
				theList.add(theEl);
			}
		}
		
		return theList;
		
	}
	
	public ModelElementList getListFilteredBy(IRPModelElement theElement){
		
		ModelElementList theList = new ModelElementList();
		
		for (IRPModelElement theEl : this) {
			if (theEl.equals(theElement)){
				theList.add(theEl);
			}
		}
		
		return theList;
		
	}
	
	public boolean hasDuplicates(){
		
		boolean isDuplicatesFound = false;
		
		Set<IRPModelElement> theSet = new HashSet<IRPModelElement>(this);

		if (theSet.size() < this.size()){
		    isDuplicatesFound = true;
		}
		
		return isDuplicatesFound;
	}
	
	public void deleteFromProject(){
		this.removeDuplicates();
		
		for (IRPModelElement theEl : this) {
			Logger.writeLine("Deleting " + Logger.elementInfo(theEl) + " from the project");
			theEl.deleteFromProject();
		}
	}
	
	
	public void removeDuplicates(){
		
		Set<IRPModelElement> theSet = new HashSet<IRPModelElement>(this);
		this.clear();
		this.addAll(theSet);
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    
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
