package functionalanalysisplugin;

import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JScrollPane;

import com.telelogic.rhapsody.core.*;

public class CopyActivityDiagramsPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<IRPUseCase, CopyActivityDiagramsInfo> m_RadioButtonMap = new HashMap<IRPUseCase, CopyActivityDiagramsInfo>();
	private IRPModelElement m_ToElement = null;
	//private IRPModelElement m_UnderneathTheEl = null;
	private JCheckBox m_ApplyMoreDetailedADCheckBox; 
	private JCheckBox m_CopyAllCheckBox;
	private JCheckBox m_OpenDiagramsCheckBox;

	public static void main(String[] args) {
		launchThePanel();
	}
	
	public static void launchThePanel(){
		
		final String theAppID = 
				FunctionalAnalysisPlugin.getRhapsodyApp().getApplicationConnectionString();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame("Copy Activity Diagams");
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CopyActivityDiagramsPanel thePanel = 
						new CopyActivityDiagramsPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	private IRPPackage getToPackage( 
			IRPModelElement fromSelectedEl ){
		
		IRPPackage thePkg = null;
		
		if( fromSelectedEl instanceof IRPPackage &&
				GeneralHelpers.hasStereotypeCalled( 
						StereotypeAndPropertySettings.getUseCasePackageWorkingStereotype( fromSelectedEl ), 
						fromSelectedEl )){
			
			thePkg = (IRPPackage) fromSelectedEl;
			
		} else if( fromSelectedEl instanceof IRPPackage && 
				GeneralHelpers.hasStereotypeCalled( 
						StereotypeAndPropertySettings.getSimulationPackageStereotype( fromSelectedEl ),
						fromSelectedEl )){
			
			List<IRPModelElement> theWorkingPkgs = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype(
							"Package", 
							StereotypeAndPropertySettings.getUseCasePackageWorkingStereotype( fromSelectedEl ), 
							fromSelectedEl, 
							0 ); // not recursive
			
			if( theWorkingPkgs.size() == 1 ){
				thePkg  = (IRPPackage) theWorkingPkgs.get( 0 );
			}
		}
		
		if( thePkg == null ){
			
			@SuppressWarnings("unchecked")
			List<IRPPackage> theNestedPkgs = 
					fromSelectedEl.getProject().getNestedElementsByMetaClass(
							"Package", 1 ).toList();

			for( IRPPackage theNestedPkg : theNestedPkgs ){

				List<IRPModelElement> theDependencies = 
						GeneralHelpers.findElementsWithMetaClassAndStereotype(
								"Dependency", "AppliedProfile", theNestedPkg, 0 );

				for (IRPModelElement theDependencyElement : theDependencies) {

					IRPDependency theDependency = (IRPDependency)theDependencyElement;
					IRPModelElement theDependsOn = theDependency.getDependsOn();

					if (theDependsOn.getName().equals("RequirementsAnalysisProfile")){
						IRPModelElement theDependent = theDependency.getDependent();
						thePkg = (IRPPackage) theDependent;
						break;
					}
				}
			}
			
			if( thePkg == null ){
				Logger.writeLine("Unable to find working package based on " + 
						Logger.elementInfo( fromSelectedEl ) );
			}
		}
		
		return thePkg;
	}
	
	@SuppressWarnings("unchecked")
	public CopyActivityDiagramsPanel(
			String appID ) {
		
		super( appID );

		IRPApplication theRhpApp = 
				RhapsodyAppServer.getActiveRhapsodyApplicationByID( appID );
				
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		Set<IRPPackage> thePullFromPkgs = 
				GeneralHelpers.getPullFromPackage( 
						theSelectedEl );
		
		List<IRPUseCase> theUseCases = new ArrayList<IRPUseCase>();
		
		for( IRPPackage thePullFromPkg : thePullFromPkgs ){
			theUseCases.addAll( 
					thePullFromPkg.getNestedElementsByMetaClass(
							"UseCase", 1 ).toList() );
		}
		
		m_ToElement = getToPackage( theSelectedEl );
		
		for (IRPUseCase theUseCase : theUseCases) {	
			m_RadioButtonMap.put( theUseCase, new CopyActivityDiagramsInfo( theUseCase ) );
		}

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		Box theBox = Box.createVerticalBox();

		if( theUseCases.isEmpty() ){
			JLabel theLabel = new JLabel("There are no use cases");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
		} else {
			m_CopyAllCheckBox = new JCheckBox("Copy and link to existing upstream activity diagrams");
			m_CopyAllCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			m_CopyAllCheckBox.setSelected(true);
			
			m_CopyAllCheckBox.addActionListener( new ActionListener() {
				
				public void actionPerformed(ActionEvent actionEvent) {

					AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

					boolean selected = abstractButton.getModel().isSelected();

					for (Entry<IRPUseCase, CopyActivityDiagramsInfo> entry : m_RadioButtonMap.entrySet()){

						CopyActivityDiagramsInfo theValue = entry.getValue();

						if (!selected){
							theValue.getDoNothingButton().setSelected(true);
						} else {
							if (theValue.hasActivityDiagrams()){
								theValue.getCopyExistingButton().setSelected(true);
							}
						}
					}

				}} );
			
			theBox.add( m_CopyAllCheckBox );

			JPanel theRadioButtonTable = createCopyADChoicesPanel();
			theRadioButtonTable.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			JScrollPane theScrollPane = new JScrollPane( theRadioButtonTable );
			
			if( m_RadioButtonMap.size() > 10 ){
				theScrollPane.setPreferredSize( new Dimension( 450, 311 ) );				
			}
			
			theBox.add( theScrollPane );
			
		}
		
		m_OpenDiagramsCheckBox = new JCheckBox("Open the copied/newly created activity diagrams?");
		m_OpenDiagramsCheckBox.setSelected( true );
		m_OpenDiagramsCheckBox.setVisible( true );
		
		theBox.add( m_OpenDiagramsCheckBox );
		
		boolean isConvertToDetailedADOptionEnabled = 
				StereotypeAndPropertySettings.getIsConvertToDetailedADOptionEnabled(
						m_ToElement.getProject() );
		
		boolean isConvertToDetailedADOptionWantedByDefault = 
				StereotypeAndPropertySettings.getIsConvertToDetailedADOptionWantedByDefault(
						m_ToElement.getProject() );
		
		m_ApplyMoreDetailedADCheckBox = new JCheckBox("Switch toolbars and formatting to more detailed AD ready for conversion?");
		m_ApplyMoreDetailedADCheckBox.setSelected( isConvertToDetailedADOptionWantedByDefault );
		m_ApplyMoreDetailedADCheckBox.setVisible( isConvertToDetailedADOptionEnabled );
		
		if( !m_RadioButtonMap.isEmpty() ){
			theBox.add( m_ApplyMoreDetailedADCheckBox );			
		}

		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {
		
		try {
			if( checkValidity( false ) ){
				
				int clonedCount = 0;
				int createdCount = 0;
				
				for (Entry<IRPUseCase, CopyActivityDiagramsInfo> entry : m_RadioButtonMap.entrySet()){
					
					IRPUseCase theUseCase = entry.getKey();
					CopyActivityDiagramsInfo theValue = entry.getValue();
				    
				    if( theValue.getCopyExistingButton().isSelected() ){
				    	
				    	List<IRPFlowchart> theFlowcharts = theValue.getFlowcharts();
				    	
			    		Logger.writeLine("Copying " + theFlowcharts.size() + " nested ADs for " + 
			    				Logger.elementInfo(theUseCase) );

				    	for( IRPFlowchart theFlowchart : theFlowcharts ){
				    		
				    		IRPFlowchart theNewFlowchart = cloneTheFlowchart( 
				    				m_ToElement, 
				    				theFlowchart, 
				    				m_ApplyMoreDetailedADCheckBox.isSelected() );

				    		if( m_OpenDiagramsCheckBox.isSelected() ){
				    			theNewFlowchart.highLightElement();			   
				    		}
				    		
				    		clonedCount++;
						}    
				    	
				    } else if( theValue.getCreateNewButton().isSelected() ){
				    					    	
			    		Logger.writeLine( "Create new ADs for " + 
			    				Logger.elementInfo( theUseCase ) );

			    		String theUniqueName = GeneralHelpers.determineUniqueNameBasedOn(
			    				"Working - AD - " + theUseCase.getName(), "ActivityDiagram", m_ToElement );
			    		
			    		Logger.writeLine("Creating new AD on " + 
			    				Logger.elementInfo( m_ToElement ) + " with unique name " + theUniqueName );
			    		
			    		IRPFlowchart theNewFlowchart = 
			    				(IRPFlowchart) m_ToElement.addNewAggr( "ActivityDiagram", theUniqueName );
			    		
			    		IRPDependency theDependency = theNewFlowchart.addDependencyTo( theUseCase );
			    		theDependency.changeTo( "Refinement" );
			    		
			    		Logger.writeLine( "A " + Logger.elementInfo( theDependency ) + " from " + Logger.elementInfo( theNewFlowchart ) + 
			    				" to " + Logger.elementInfo( theUseCase ) + " has been added to the model." );
			    		
			    		AddNoteTo( theNewFlowchart );

			    		if( m_ApplyMoreDetailedADCheckBox.isSelected() ){
			    			theNewFlowchart.changeTo("ActivityDiagram");
			    		} else {
			    			theNewFlowchart.changeTo("Textual Activity Diagram");
			    		}
			    		
			    		createdCount++;

			    		if( m_OpenDiagramsCheckBox.isSelected() ){
			    			theNewFlowchart.highLightElement();			   
			    		}	
				    }
				}	
				
			    if( clonedCount > 0 || createdCount > 0 ){
			    	
			    	JDialog.setDefaultLookAndFeelDecorated(true);
					
					JOptionPane.showMessageDialog(
							null,  
				    		"Finished. " + clonedCount + " diagram(s) were copied " + 
				    		"and " + createdCount + " new diagram(s) created in Package \ncalled " + m_ToElement.getFullPathName(),
				    		"Information",
				    		JOptionPane.INFORMATION_MESSAGE);
					
					m_ToElement.highLightElement();
			    }
			} else {
				Logger.writeLine("Error in CreateNewActorPanel.performAction, checkValidity returned false");
			}	
		} catch (Exception e) {
			Logger.writeLine("Error in CopyActivityDiagramsPanel.performAction, unhandled exception was detected, e=" + e.getMessage());
		}
	}
	
	private JPanel createCopyADChoicesPanel(){
		
		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn3ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn4ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn3ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn4ParallelGroup );
		
		for (Entry<IRPUseCase, CopyActivityDiagramsInfo> entry : m_RadioButtonMap.entrySet()){
		    
			IRPUseCase theUseCase = entry.getKey();
			CopyActivityDiagramsInfo theValue = entry.getValue();

			JLabel theName = new JLabel( theUseCase.getName()) ;
			theName.setMinimumSize( new Dimension( 150, 22 ) );
			theName.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10));

			
			theColumn1ParallelGroup.addComponent( theName );   
			theColumn2ParallelGroup.addComponent( theValue.getCopyExistingButton() );    
			theColumn3ParallelGroup.addComponent( theValue.getCreateNewButton() );    
			theColumn4ParallelGroup.addComponent( theValue.getDoNothingButton() );    

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical1ParallelGroup.addComponent( theName );
			theVertical1ParallelGroup.addComponent( theValue.getCopyExistingButton() );
			theVertical1ParallelGroup.addComponent( theValue.getCreateNewButton() );
			theVertical1ParallelGroup.addComponent( theValue.getDoNothingButton() );
			
			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		  
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

	    return thePanel;

	}
	
	private IRPFlowchart cloneTheFlowchart(
			IRPModelElement toNewOwner,
			IRPFlowchart theFlowchart,
			boolean isSwitchToDetailedAD ) {
		
		String theUniqueName = GeneralHelpers.determineUniqueNameBasedOn(
				"Working - " + theFlowchart.getName(), "ActivityDiagram", toNewOwner);
		
		Logger.writeLine("Cloned " + Logger.elementInfo(theFlowchart) + " to " + 
				Logger.elementInfo(toNewOwner) + " with unique name " + theUniqueName);
		
		IRPFlowchart theNewFlowchart = 
				(IRPFlowchart) theFlowchart.clone( theUniqueName, toNewOwner );
		
		IRPDependency theDependency = theNewFlowchart.addDependencyTo( theFlowchart );
		theDependency.changeTo("Refinement");
		
		theNewFlowchart.setIsAnalysisOnly( 1 ); // to allow call op parameter sync-ing
		
		Logger.writeLine(theDependency, "was added between " + Logger.elementInfo( theNewFlowchart ) + 
				" and " + Logger.elementInfo( theFlowchart ) );
		
		AddNoteTo( theNewFlowchart );
		
		if( isSwitchToDetailedAD ){	
			theNewFlowchart.changeTo("ActivityDiagram");
		}
		
		return theNewFlowchart;
	}

	private void AddNoteTo(IRPFlowchart theNewFlowchart) {
		IRPGraphNode theNote = theNewFlowchart.addNewNodeByType("Note", 20, 44, 120, 70);
		
		theNote.setGraphicalProperty(
				"Text", 
				"This working copy of the use case steps can be used to generate operations, events & attributes \n" 
				+ "(e.g. for state-machine/iteraction model).");
		
		theNote.setGraphicalProperty(
				"BackgroundColor",
				"255,0,0" ); // red
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #026 31-MAY-2016: Add dialog to allow user to choose which Activity Diagrams to synch (F.J.Chadburn)
    #027 31-MAY-2016: Add new menu to launch dialog to copy Activity Diagrams (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #045 03-JUL-2016: Fix CopyActivityDiagramsPanel capability (F.J.Chadburn)
    #047 06-JUL-2016: Tweaked properties and added options to switch to MoreDetailedAD automatically (F.J.Chadburn)
    #057 13-JUL-2016: Enhanced Copy AD panel to list use cases and give a create new option (F.J.Chadburn)
    #093 23-AUG-2016: Added isPopulateOptionHidden tag to allow hiding of the populate check-box on dialogs (F.J.Chadburn)
    #122 25-NOV-2016: Scroll-bar added to Copy AD dialog to enable it to scale to large number of ADs (F.J.Chadburn)
    #128 25-NOV-2016: Improved usability/speed of Copy AD dialog by providing user choice to open diagrams (F.J.Chadburn)
    #143 18-DEC-2016: Add separate tag to enable/disable conversion to detailed option in Copy AD dialog (F.J.Chadburn)
    #244 11-OCT-2017: Default ADs to Analysis mode to better support call operation parameter sync (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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