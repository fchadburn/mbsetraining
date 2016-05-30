package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class ClassifierMappingInfo {

	public ClassifierMappingInfo(
			RhapsodyComboBox theRhapsodyComboBox,
			JCheckBox theActorCheckBox, 
			JTextField theActorName,
			IRPActor theSourceActor) {
		
		super();
		this.m_InheritedFromComboBox = theRhapsodyComboBox;
		this.m_ActorCheckBox = theActorCheckBox;
		this.m_ActorNameTextField = theActorName;
		this.m_SourceActor = theSourceActor;
	}
	
	private RhapsodyComboBox m_InheritedFromComboBox;
	private JCheckBox m_ActorCheckBox;
	private JTextField m_ActorNameTextField;
	private IRPActor m_SourceActor;
	
	public JTextField getTextField(){
		return m_ActorNameTextField;
	}
	public boolean isSelected(){
		return m_ActorCheckBox.isSelected();
	}
	
	public String getName(){
		return m_ActorNameTextField.getText();
	}
	
	public IRPModelElement getInheritedFrom(){
		return m_InheritedFromComboBox.getSelectedRhapsodyItem();
	}
	
	public String getSourceActorName(){
		return m_SourceActor.getName();
	}
	
	public IRPActor getSourceActor(){
		return m_SourceActor;
	}
	
	public void updateToBestActorNamesBasedOn(String theBlockName){
		
		String theOriginalActorName = m_SourceActor.getName();
		
		String theProposedActorName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toLegalClassName( theOriginalActorName ) + "_" + theBlockName, 
				"Actor", 
				m_SourceActor.getProject());
		
		m_ActorNameTextField.setText( theProposedActorName );
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #023 30-MAY-2016: Added form to support validation checks for analysis block hierarchy creation (F.J.Chadburn) 
    
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