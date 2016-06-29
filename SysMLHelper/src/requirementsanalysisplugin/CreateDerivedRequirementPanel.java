package requirementsanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.GraphNodeInfo;
import functionalanalysisplugin.RequirementSelectionPanel;
import generalhelpers.CreateStructuralElementPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.NamedElementMap;
import generalhelpers.UserInterfaceHelpers;

public class CreateDerivedRequirementPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	NamedElementMap m_NamedElementMap;
	private JComboBox<Object> m_FromDependencyComboBox = null;
	private JTextArea m_Specification = null;
	private IRPProject m_Project;
	private IRPGraphElement m_SourceGraphElement;
	private RequirementSelectionPanel m_RequirementSelectionPanel;
	
	public static void launchThePanel(
			final IRPGraphElement theSourceGraphElement,
			final Set<IRPRequirement> forHigherLevelReqts,
			final IRPProject inTheProject ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame(
						"Derive a new requirement?");

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateDerivedRequirementPanel thePanel = 
						new CreateDerivedRequirementPanel( 
								theSourceGraphElement, forHigherLevelReqts, inTheProject );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	CreateDerivedRequirementPanel(
			final IRPGraphElement theSourceGraphElement,
			final Set<IRPRequirement> forHigherLevelReqts,
			final IRPProject inTheProject ){
		
		super();
		
		m_Project = inTheProject;
		m_SourceGraphElement = theSourceGraphElement;
		
		m_RequirementSelectionPanel = new RequirementSelectionPanel( 
				forHigherLevelReqts, "Create «deriveReqt» dependencies to:" );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		add( m_RequirementSelectionPanel, BorderLayout.PAGE_START );
		add( createWestCentrePanel(), BorderLayout.CENTER );
		add( createPageEndPanel(), BorderLayout.PAGE_END );
	}
	
	private Component createWestCentrePanel() {
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX( LEFT_ALIGNMENT );

		//JPanel theSpec = new JPanel();
		//GridLayout theLayout = new GridLayout(0, 1);
		//theLayout.setHgap( 1 );
		//theSpec.setLayout( theLayout );
		
		
		//thePanel.setLayout( theLayout );
		//thePanel.setAlignmentX( LEFT_ALIGNMENT );
		
		List<IRPModelElement> thePackages = 
				GeneralHelpers.findModelElementsNestedUnder( 
						m_Project, "Package", "from.*");
		
		m_NamedElementMap = new NamedElementMap( thePackages );
		m_FromDependencyComboBox = new JComboBox<Object>( m_NamedElementMap.getFullNames() );
		
		m_Specification = new JTextArea(5,20);
		m_Specification.setBorder( BorderFactory.createBevelBorder(1));
		
		thePanel.add( new JLabel("Specification:") ); 
		thePanel.add( m_Specification );
		thePanel.add( new JLabel("Move into:")  );
		thePanel.add( m_FromDependencyComboBox );
		
		return thePanel;
	}
	
	private Component createPageEndPanel() {

		JLabel theLabel = new JLabel( "Do you want to proceed?" );
		theLabel.setAlignmentX(CENTER_ALIGNMENT);
		theLabel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX(CENTER_ALIGNMENT);
		thePanel.add( theLabel );
		thePanel.add( createOKCancelPanel() );
		
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {

		String errorMsg = "";
		
		boolean isValid = true;

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelpers.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {

		// do silent check first
		if( checkValidity( false ) ){
			
			if( m_SourceGraphElement instanceof IRPGraphNode ){

				GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement );
				
				int x = theNodeInfo.getTopLeftX() + 140;
				int y = theNodeInfo.getTopLeftY() + 160;

				IRPDiagram theDiagram = m_SourceGraphElement.getDiagram();

				IRPRequirement theRequirement = null;
				
				if( theDiagram instanceof IRPActivityDiagram ){
					
					IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;
					IRPFlowchart theFC = (IRPFlowchart) theAD.getFlowchart();
					
					theRequirement = (IRPRequirement) theFC.addNewAggr( "Requirement", "" );
					
				} else if( theDiagram instanceof IRPObjectModelDiagram ){
				
					theRequirement = (IRPRequirement) theDiagram.addNewAggr( "Requirement", "" );
					
				} else if( theDiagram instanceof IRPStatechartDiagram ){
					
					Logger.writeLine( theDiagram, "is a statechart diagram!");
					theRequirement = (IRPRequirement) theDiagram.addNewAggr( "Requirement", "" );
					Logger.writeLine("Got here");
				}
				
				if( theRequirement != null ){
					
					theRequirement.setSpecification( m_Specification.getText() );
					
					List<IRPRequirement> theUpstreamReqts = 
							m_RequirementSelectionPanel.getSelectedRequirementsList();
					
					if (theUpstreamReqts.isEmpty()){
						
						Logger.writeLine("Warning in performAction, no upstream requirement was selected");
						
					} else {
						for (IRPRequirement theUpstreamReqt : theUpstreamReqts) {
							
							IRPDependency theDeriveReqtDependency = theRequirement.addDependencyTo(theUpstreamReqt);
							theDeriveReqtDependency.addStereotype("deriveReqt", "Dependency");
								
							Logger.writeLine("Adding deriveReqt dependency from " + Logger.elementInfo(theRequirement) +
									" to " + Logger.elementInfo(theUpstreamReqt));
						}				
					}
					
					moveAndStereotype( theRequirement );
					
					IRPGraphNode theNode = theDiagram.addNewNodeForElement( theRequirement, x, y, 300, 100 );

					IRPCollection theCollection = 
							RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();
					
					theCollection.addGraphicalItem( m_SourceGraphElement );
					theCollection.addGraphicalItem( theNode );
					
					theDiagram.completeRelations(theCollection, 0);
				}
			}			
		} else {
			Logger.writeLine("Error in CreateDerivedRequirementPanel.performAction, checkValidity returned false");
		}			
	}

	private void moveAndStereotype( 
			IRPRequirement theRequirement ) {
			
		IRPModelElement theChosenPkg = 
				m_NamedElementMap.getElementUsingFullName( 
						m_FromDependencyComboBox.getSelectedItem() );
			
		if( theChosenPkg != null && theChosenPkg instanceof IRPPackage ){
				
			// check if already element of same name
			IRPModelElement alreadyExistingEl = 
					theChosenPkg.findNestedElement(
							theRequirement.getName(), "Requirement" );
				
			if (alreadyExistingEl != null){
				Logger.writeLine("Error: Unable to move " + Logger.elementInfo( theRequirement ) 
						+ " as requirement of same name already exists under " 
						+ Logger.elementInfo( theChosenPkg ) + ". Consider renaming it.");
			} else {
				IRPStereotype theStereotypeToApply = 
						GeneralHelpers.getStereotypeAppliedTo( theChosenPkg, "from.*" );
					
				Logger.writeLine(theStereotypeToApply, "is the stereotype to apply");
					
				if (theStereotypeToApply != null){
						
					theRequirement.setStereotype( theStereotypeToApply );

					Logger.writeLine("Moving " + Logger.elementInfo(theRequirement) 
							+ " into " + Logger.elementInfo(theChosenPkg));
						
					theRequirement.setOwner(theChosenPkg);
					
					GeneralHelpers.applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
							theRequirement, theStereotypeToApply );
						
					theRequirement.highLightElement();
				}
			}
				
		} else {
			Logger.writeLine("Error in moveAndStereotype, no package was selected for the move");
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #041 29-JUN-2016: Derive downstream requirement menu added for reqts on diagrams (F.J.Chadburn) 

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

