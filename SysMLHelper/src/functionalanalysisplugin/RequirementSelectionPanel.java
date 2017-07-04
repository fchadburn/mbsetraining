package functionalanalysisplugin;

import generalhelpers.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import com.telelogic.rhapsody.core.*;

public class RequirementSelectionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<IRPRequirement, JCheckBox> m_CheckBoxMap = new HashMap<IRPRequirement, JCheckBox>();

	public RequirementSelectionPanel(
			String theLabelText,
			Set<IRPRequirement> theReqtsInTable,
			Set<IRPRequirement> theReqtsSelected ){
		
		super();

		GridLayout theLayout = new GridLayout(0, 1);
		
		setLayout( theLayout );

		if( !theReqtsInTable.isEmpty() ){
				
			setBorder( BorderFactory.createLineBorder( new Color(0,0,0) ));
			
			Box theBox = Box.createVerticalBox();

			theBox.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JLabel theLabel = new JLabel(theLabelText);
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );

			JPanel theReqtsTable = createContent( theReqtsInTable, theReqtsSelected );
			theReqtsTable.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theReqtsTable );
			
			add( theBox );
		}
	}
	
	private JPanel createContent(
			Set<IRPRequirement> theReqtsInTable,
			Set<IRPRequirement> theReqtsSelected ){
		
		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );

		for (IRPRequirement theReqt : theReqtsInTable) {

			JCheckBox theReqtCheckBox = new JCheckBox( theReqt.getName()) ;

			theReqtCheckBox.setSelected( theReqtsSelected.contains( theReqt ) );

			JTextArea theSpecification = new JTextArea( theReqt.getSpecification() );
			theSpecification.setEditable( false );
			JScrollPane scrollPane = new JScrollPane( theSpecification );

			scrollPane.setPreferredSize( new Dimension( 500, 35 ) );

			theColumn1ParallelGroup.addComponent( theReqtCheckBox );   
			theColumn2ParallelGroup.addComponent( scrollPane, 700, 700, 700 );    

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical1ParallelGroup.addComponent( theReqtCheckBox );
			theVertical1ParallelGroup.addComponent( scrollPane );

			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		  

			m_CheckBoxMap.put(theReqt, theReqtCheckBox);
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

	    return thePanel;
	}

	public void selectedRequirementsIn( Set<IRPRequirement> theReqts ){
		
		for( IRPRequirement theReqt : theReqts ){
			
			JCheckBox theCheckBox = m_CheckBoxMap.get( theReqt );
			
			if( theCheckBox != null ){
				theCheckBox.setSelected( true );
			} else {
				Logger.writeLine("Warning in RequirementSelectionPanel.selectedRequirementsIn, " + 
						Logger.elementInfo( theReqt ) + " is not in table of expected requirements");
			}
		}
	}

	public void deselectedRequirementsIn( Set<IRPRequirement> theReqts ){
		
		for( IRPRequirement theReqt : theReqts ){
			
			JCheckBox theCheckBox = m_CheckBoxMap.get( theReqt );
			
			if( theCheckBox != null ){
				theCheckBox.setSelected( false );
			} else {
				Logger.writeLine("Warning in RequirementSelectionPanel.selectedRequirementsIn, " + 
						Logger.elementInfo( theReqt ) + " is not in table of expected requirements");
			}
		}
	}
	public List<IRPRequirement> getSelectedRequirementsList(){
		
		List<IRPRequirement> theFilteredReqts = new ArrayList<IRPRequirement>();
		
		for (Entry<IRPRequirement, JCheckBox> entry : m_CheckBoxMap.entrySet())
		{
			JCheckBox theCheckBox = entry.getValue();
			
		    if (theCheckBox.isSelected()){
		    	IRPRequirement theRequirement = entry.getKey();
		    	
		    	Logger.writeLine( theRequirement, "was selected");
		    	theFilteredReqts.add( theRequirement );
		    } else {
		    	IRPRequirement theRequirement = entry.getKey();
		    	
		    	Logger.writeLine( theRequirement, "was not selected");
		    }
		}
		
		return theFilteredReqts;
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #031 01-JUN-2016: Scroll pane added for readability of requirement text on panels (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #041 29-JUN-2016: Derive downstream requirement menu added for reqts on diagrams (F.J.Chadburn)
    #051 06-JUL-2016: Re-factored the GW panel to allow it to incrementally add to previous setup (F.J.Chadburn)
    #056 13-JUL-2016: Minor fixes to RequirementsSelectionPanel to make it clearer (F.J.Chadburn)
    #058 13-JUL-2016: Dropping CallOp on diagram now gives option to create Op on block (F.J.Chadburn)
    #209 04-JUL-2017: Populate requirements for SD(s) based on messages now supported with Dialog (F.J.Chadburn)

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