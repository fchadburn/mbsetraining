package designsynthesisplugin;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import com.telelogic.rhapsody.core.*;

public class ConfirmDiagramUpdatePanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AutoConnectFlowPortsMap m_AutoConnectFlowPortsMap = 
			new AutoConnectFlowPortsMap();
	
	private Map<IRPStructureDiagram, JCheckBox> m_DiagramCheckBoxMap = 
			new HashMap<IRPStructureDiagram, JCheckBox>();
	
	public static void launchThePanel(
			final AutoConnectFlowPortsMap theAutoConnectFlowPortsMap ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame("Update diagrams");

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				ConfirmDiagramUpdatePanel thePanel = 
						new ConfirmDiagramUpdatePanel( 
								theAutoConnectFlowPortsMap );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public ConfirmDiagramUpdatePanel(
			final AutoConnectFlowPortsMap theAutoConnectFlowPortsMap ){
		
		super();
		
		m_AutoConnectFlowPortsMap = theAutoConnectFlowPortsMap;
		m_DiagramCheckBoxMap = getDiagramCheckBoxMap();
		
		setLayout( new BorderLayout( 10, 10 ) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		Box theBox = Box.createVerticalBox();

		if( m_DiagramCheckBoxMap.isEmpty() ){
			
			JLabel theLabel = new JLabel( "There are no diagrams to update" );
			theLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
			theBox.add( theLabel );
			
		} else {
			
			JPanel theRadioButtonTable = createMakeChoicesPanel( m_DiagramCheckBoxMap );
			theRadioButtonTable.setAlignmentX( Component.LEFT_ALIGNMENT );
			
			JScrollPane theScrollPane = new JScrollPane( theRadioButtonTable );
			
			if( m_AutoConnectFlowPortsMap.size() > 10 ){
				theScrollPane.setPreferredSize( new Dimension( 450, 311 ) );				
			}
			
			theBox.add( new JLabel( "Do you want to update the following diagrams to show new connections?") );
			theBox.add( new JLabel( "   " ) );

			theBox.add( theScrollPane );
		}

		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	private Map<IRPStructureDiagram, JCheckBox> getDiagramCheckBoxMap(){
		
		Map<IRPStructureDiagram, JCheckBox> theDiagramCheckBoxMap = 
				new HashMap<IRPStructureDiagram, JCheckBox>();
		
		for (Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : m_AutoConnectFlowPortsMap.entrySet()){
		    
			AutoConnectFlowPortsInfo theValue = entry.getValue();	
			
			Set<IRPStructureDiagram> theDiagrams = 
					theValue.getM_DiagramsToUpdate();
			
			for( IRPStructureDiagram theDiagram : theDiagrams ){
				
				if( !theDiagramCheckBoxMap.containsKey( theDiagram ) ){
					
					JCheckBox theCheckBox = new JCheckBox( theDiagram.getName() );
					theCheckBox.setSelected( true );
					
					theDiagramCheckBoxMap.put( 
							theDiagram, 
							theCheckBox );
				}
			}
		}
		
		return theDiagramCheckBoxMap;
	}
	
	private Set<IRPGraphElement> getGraphElementsFor(
			IRPModelElement theModelElement, 
			IRPDiagram onTheDiagram ){
		
		Set<IRPGraphElement> theMatchingGraphEls = 
				new HashSet<IRPGraphElement>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsOnDiagram = 
				onTheDiagram.getGraphicalElements().toList();
				
		for( IRPGraphElement theGraphElOnDiagram : theGraphElsOnDiagram ){
			
			IRPModelElement theModelObject = 
					theGraphElOnDiagram.getModelObject();
			
			if( theModelObject != null &&
				theModelObject.equals( theModelElement ) ){
				
				theMatchingGraphEls.add( theGraphElOnDiagram );
			}
			
			Logger.writeLine( theModelObject, " is on " + Logger.elementInfo( onTheDiagram ) );
		}
		
		return theMatchingGraphEls;
	}
	
	private void updateDiagramBasedOn( 
			AutoConnectFlowPortsMap theAutoConnectFlowPortsMap,
			IRPStructureDiagram theDiagramToUpdate ){
						
		Set<IRPGraphElement> theSetOfGraphEls = 
				new HashSet<IRPGraphElement>();
		
		for (Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : m_AutoConnectFlowPortsMap.entrySet()){

			AutoConnectFlowPortsInfo theValue = entry.getValue();
			
			Set<IRPLink> theLinks = theValue.getM_Links();
			
			for( IRPLink theLink : theLinks ){
				
				if( theLink != null ){
					IRPSysMLPort theFromPort = theLink.getFromSysMLPort();
					IRPSysMLPort theToPort = theLink.getToSysMLPort();
					
					if( theFromPort != null && theToPort != null ){
						
						theSetOfGraphEls.addAll( 
								getGraphElementsFor( theFromPort, theDiagramToUpdate ) );
						
						theSetOfGraphEls.addAll( 
								getGraphElementsFor( theToPort, theDiagramToUpdate ) );
					}
				}

			}
		}
		
		IRPCollection theGraphEls = 
				DesignSynthesisPlugin.getRhapsodyApp().createNewCollection();

		for( IRPGraphElement theGraphEl : theSetOfGraphEls ){
			theGraphEls.addGraphicalItem( theGraphEl );
		}
		
		Logger.writeLine("Completing relations for x" + theGraphEls.getCount() + 
				" graph elements on " + Logger.elementInfo( theDiagramToUpdate ) );
		
		theDiagramToUpdate.completeRelations( theGraphEls, 0);		
	}
	
	private JPanel createMakeChoicesPanel(
			Map<IRPStructureDiagram, JCheckBox> theDiagramCheckBoxMap ){
		
		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		//ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		//theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		
		for (Entry<IRPStructureDiagram, JCheckBox> entry : theDiagramCheckBoxMap.entrySet()){
		    
			IRPStructureDiagram theKey = entry.getKey();
			JCheckBox theValue = entry.getValue();
								    
			JLabel theName = new JLabel( theKey.getName() );
			theName.setMinimumSize( new Dimension( 150, 22 ) );
			theName.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );

			theColumn1ParallelGroup.addComponent( theValue );  
//			theColumn2ParallelGroup.addComponent( theValue );    

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical1ParallelGroup.addComponent( theValue );
//			theVertical1ParallelGroup.addComponent( theValue );
			
			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		  
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

	    return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		boolean isValid = true;
		String errorMsg = "";		
				
		if( isMessageEnabled && !isValid && errorMsg != null ){
			
			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {
		
		try {
			if( checkValidity( false ) ){										
				
				for (Entry<IRPStructureDiagram, JCheckBox> entry : m_DiagramCheckBoxMap.entrySet()){

					IRPStructureDiagram theKey = entry.getKey();
					JCheckBox theValue = entry.getValue();
					
					if( theValue.isSelected() ){
						
						updateDiagramBasedOn( 
								m_AutoConnectFlowPortsMap,
								theKey );
					}
				}
				
			} else {
				Logger.writeLine("Error in ConfirmDiagramUpdatePanel.performAction, checkValidity returned false");
			}	
			
		} catch (Exception e) {
			Logger.writeLine("Error in ConfirmDiagramUpdatePanel.performAction, unhandled exception was detected");
		}
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #213 09-JUL-2017: Add dialogs to auto-connect «publish»/«subscribe» FlowPorts for white-box simulation (F.J.Chadburn)
        
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