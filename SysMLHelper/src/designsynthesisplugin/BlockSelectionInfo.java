package designsynthesisplugin;

import javax.swing.JCheckBox;

import com.telelogic.rhapsody.core.IRPModelElement;

public class BlockSelectionInfo {
 
	public BlockSelectionInfo(
			JCheckBox theCheckBox, 
			IRPModelElement theModelElement) {
		
		super();
		this.theCheckBox = theCheckBox;
		this.theModelElement = theModelElement;
	}
	
	private JCheckBox theCheckBox;
	private IRPModelElement theModelElement;
	
	public boolean isSelected(){
		return theCheckBox.isSelected();
	}
	
	public String getName(){
		return theModelElement.getName();
	}
	
	public IRPModelElement getModelElement(){
		return theModelElement;
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
