package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.telelogic.rhapsody.core.*;

public class CopyActivityDiagramsPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<IRPFlowchart, JCheckBox> m_CheckBoxMap = new HashMap<IRPFlowchart, JCheckBox>();
	private IRPModelElement m_ToElement = null;
	private IRPModelElement m_UnderneathTheEl = null;

	@SuppressWarnings("unchecked")
	public CopyActivityDiagramsPanel(
			IRPModelElement underneathTheEl, 
			IRPModelElement toElement) {
		
		super();

		m_UnderneathTheEl = underneathTheEl;
		m_ToElement = toElement;
		
		List<IRPFlowchart> allTheFlowcharts = new ArrayList<IRPFlowchart>();
		
		List<IRPUseCase> theUseCases = 
				m_UnderneathTheEl.getNestedElementsByMetaClass("UseCase", 1).toList();	
		
		for (IRPUseCase theUseCase : theUseCases) {
			
			allTheFlowcharts.addAll( 
					theUseCase.getNestedElementsByMetaClass("ActivityDiagram", 1).toList() );		
		}
		
		for (IRPFlowchart theFlowchart : allTheFlowcharts) {
			
			JCheckBox theCheckbox = new JCheckBox( theFlowchart.getName() );
			theCheckbox.setSelected(true);
			
			m_CheckBoxMap.put(theFlowchart, theCheckbox);
		}
		
		Box theBox = Box.createVerticalBox();

		theBox.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));

		if (allTheFlowcharts.isEmpty()){
			JLabel theLabel = new JLabel("There are no activity diagrams to copy");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
		} else {
			JLabel theLabel = new JLabel("Copy the following activity diagrams to " + Logger.elementInfo(toElement) + ": ");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
		}
		
	    for (Entry<IRPFlowchart, JCheckBox> entry : m_CheckBoxMap.entrySet()){
			
			JCheckBox theCheckBox = entry.getValue();		
		    theBox.add( theCheckBox );
		}
	    
		setLayout( new BorderLayout() );
		
		add( theBox, BorderLayout.PAGE_START);
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
		
		add( theBox );
	}

	
	@Override
	boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	void performAction() {
		
		if (checkValidity( false )){
			
			for (Entry<IRPFlowchart, JCheckBox> entry : m_CheckBoxMap.entrySet()){
				
				JCheckBox theCheckBox = entry.getValue();		
			    IRPFlowchart theFlowchart = entry.getKey();
			    
			    if (theCheckBox.isSelected()){
			    	cloneTheFlowchart( m_ToElement, theFlowchart );
			    }
			}				
		} else {
			Logger.writeLine("Error in CreateNewActorPanel.performAction, checkValidity returned false");
		}	
	}
	
	private void cloneTheFlowchart(
			IRPModelElement toElement,
			IRPFlowchart theFlowchart) {
			
		String theUniqueName = GeneralHelpers.determineUniqueNameBasedOn(
				"Working - " + theFlowchart.getName(), "ActivityDiagram", toElement);
		
		Logger.writeLine("Cloned " + Logger.elementInfo(theFlowchart) + " to " + 
				Logger.elementInfo(toElement) + " with unique name " + theUniqueName);
		
		IRPFlowchart theNewFlowchart = (IRPFlowchart) theFlowchart.clone(theUniqueName, toElement);
		
		IRPDependency theDependency = theNewFlowchart.addDependencyTo(theFlowchart);
		theDependency.changeTo("Refinement");
		
		Logger.writeLine(theDependency, "was added");
		
		IRPGraphNode theNote = theNewFlowchart.addNewNodeByType("Note", 20, 44, 120, 70);
		
		theNote.setGraphicalProperty("Text", "This working copy of the use case steps can be used to generate the state machine.");
		
		theNewFlowchart.highLightElement();
		theNewFlowchart.getFlowchartDiagram().openDiagram();
	}
	
	public static void launchCopyActivityDiagramPanel(
			final IRPModelElement underneathTheEl, 
			final IRPModelElement toElement){
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame("Copy Activity Diagams to " + Logger.elementInfo( toElement ));
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CopyActivityDiagramsPanel thePanel = 
						new CopyActivityDiagramsPanel( underneathTheEl, toElement );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #027 31-MAY-2016: Add new menu to launch dialog to copy Activity Diagrams (F.J.Chadburn)
    
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