package functionalanalysisplugin;

import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.telelogic.rhapsody.core.*;

public class CopyActivityDiagramsInfo {

	private IRPUseCase m_UseCase;
	private JRadioButton m_CopyExistingButton;
	private JRadioButton m_CreateNewButton;
	private JRadioButton m_DoNothingButton;
	private List<IRPFlowchart> m_NestedFlowcharts;
	private ButtonGroup m_ButtonGroup;
	
	@SuppressWarnings("unchecked")
	public CopyActivityDiagramsInfo(
			IRPUseCase forTheUseCase ) {
		
		m_UseCase = forTheUseCase;
	
		m_NestedFlowcharts = m_UseCase.getNestedElementsByMetaClass(
				"ActivityDiagram", 1).toList();

		m_CopyExistingButton = new JRadioButton("Copy Existing");
		m_CreateNewButton = new JRadioButton("Create New");
		m_DoNothingButton = new JRadioButton("Do Nothing");
		
		if (m_NestedFlowcharts.isEmpty()){
			
			m_CopyExistingButton.setSelected(false);
			m_CreateNewButton.setSelected(false);
			m_DoNothingButton.setSelected(true);
			
			m_CopyExistingButton.setEnabled(false);
			
		} else {
			m_CopyExistingButton.setSelected(true);
			m_CreateNewButton.setSelected(false);
			m_DoNothingButton.setSelected(false);
		}
		
		m_ButtonGroup = new ButtonGroup();
		m_ButtonGroup.add(m_CopyExistingButton);
		m_ButtonGroup.add(m_CreateNewButton);
		m_ButtonGroup.add(m_DoNothingButton);
	}
	
	public JRadioButton getCopyExistingButton() {
		return m_CopyExistingButton;
	}
	
	public JRadioButton getCreateNewButton() {
		return m_CreateNewButton;
	}
	
	public JRadioButton getDoNothingButton() {
		return m_DoNothingButton;
	}
	
	public boolean hasActivityDiagrams(){
		return !m_NestedFlowcharts.isEmpty();
	}
	
	public List<IRPFlowchart> getFlowcharts(){
		return m_NestedFlowcharts;	
	}
	
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #057 13-JUL-2016: Enhanced Copy AD panel to list use cases and give a create new option (F.J.Chadburn)
    #104 03-NOV-2016: Minor tweak to copy ADs panel to do nothing if upstream AD doesn't exist (F.J.Chadburn)
        
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
