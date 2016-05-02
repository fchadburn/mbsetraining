package functionalanalysisplugin;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.IRPModelElement;

public class ClassifierMappingInfo {

	public ClassifierMappingInfo(
			RhapsodyComboBox theRhapsodyComboBox,
			JCheckBox theActorCheckBox, 
			JTextField theActorName) {
		super();
		this.theRhapsodyComboBox = theRhapsodyComboBox;
		this.theActorCheckBox = theActorCheckBox;
		this.theActorName = theActorName;
	}
	
	private RhapsodyComboBox theRhapsodyComboBox;
	private JCheckBox theActorCheckBox;
	private JTextField theActorName;
	
	public boolean isSelected(){
		return theActorCheckBox.isSelected();
	}
	
	public String getName(){
		return theActorName.getText();
	}
	
	public IRPModelElement getInheritedFrom(){
		return theRhapsodyComboBox.getSelectedRhapsodyItem();
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