package requirementsanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.RequirementSelectionPanel;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;

public class PopulateTransitionRequirementsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RequirementSelectionPanel m_RequirementsPanel = null;
	protected IRPTransition m_Transition = null;
	protected IRPGraphElement m_TransitionGE = null;
	protected IRPStatechartDiagram m_StatechartDiagram = null;
	protected Set<IRPRequirement> m_ReqtsForTable;
	protected JCheckBox m_RemoveFromViewCheckBox;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		IRPProject theActiveProject = theRhpApp.activeProject();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = 
				theRhpApp.getSelectedGraphElements().toList();
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = 
			theRhpApp.getSelectedGraphElements().toList();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			
			PopulateTransitionRequirementsPanel.launchThePanel(
					theGraphEl );	
		}
	
	}
	
	public PopulateTransitionRequirementsPanel(
			IRPGraphElement theGraphEl ){
		
		IRPModelElement theModelObject = theGraphEl.getModelObject();
		
		if( theModelObject instanceof IRPTransition ){
			
			m_Transition = (IRPTransition)theModelObject;
			m_TransitionGE = theGraphEl;
			m_StatechartDiagram = (IRPStatechartDiagram) m_TransitionGE.getDiagram();
			
			m_ReqtsForTable = getRequirementsRelatedTo( m_Transition );
			
			m_RequirementsPanel = new RequirementSelectionPanel( 
					"Requirements related to trigger/guard/actions are:",
					m_ReqtsForTable, 
					m_ReqtsForTable );
			
			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
			
			JPanel thePageStartPanel = new JPanel();
			thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
			
			if( m_ReqtsForTable.isEmpty() ){
				
				JLabel theLabel = new JLabel( "There are no requirements to roll up" );
				theLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
				thePageStartPanel.add( theLabel );
			
				
				
			} else {
				
				JButton theSelectAllButton = new JButton( "Select All" );
				theSelectAllButton.setPreferredSize( new Dimension( 75,25 ) );

				theSelectAllButton.addActionListener( new ActionListener() {
					
					@Override
					public void actionPerformed( ActionEvent e ) {
						
						try {
							m_RequirementsPanel.selectedRequirementsIn( m_ReqtsForTable );
														
						} catch (Exception e2) {
							
							Logger.writeLine("Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
									"constructor on Select All button action listener");
						}
					}
				});
				
				JButton theDeselectAllButton = new JButton( "De-select All" );
				theDeselectAllButton.setPreferredSize( new Dimension( 75,25 ) );

				theDeselectAllButton.addActionListener( new ActionListener() {
					
					@Override
					public void actionPerformed( ActionEvent e ) {
						
						try {
							m_RequirementsPanel.deselectedRequirementsIn( m_ReqtsForTable );
														
						} catch (Exception e2) {
							
							Logger.writeLine("Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
									"constructor on De-select All button action listener");
						}
					}
				});
						
				thePageStartPanel.add( theSelectAllButton );
				thePageStartPanel.add( new JLabel("  ") );
				thePageStartPanel.add( theDeselectAllButton );
				
				add( m_RequirementsPanel, BorderLayout.WEST );
			}
			
			add( thePageStartPanel, BorderLayout.PAGE_START );
			add( createOKCancelPanel(), BorderLayout.PAGE_END );
		}

	}
		
	public JPanel createOKCancelPanel(){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout() );
		
		JButton theOKButton = new JButton("OK");
		theOKButton.setPreferredSize(new Dimension(75,25));

		theOKButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					boolean isValid = checkValidity( true );
					
					if (isValid){
						performAction();
						Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
						dialog.dispose();
					}		
												
				} catch (Exception e2) {
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on OK button action listener");
				}
			}
		});
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.setPreferredSize(new Dimension(75,25));
		
		theCancelButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Window dialog = SwingUtilities.windowForComponent( (Component) e.getSource() );
					dialog.dispose();
												
				} catch (Exception e2) {
					Logger.writeLine("Error, unhandled exception in CreateOperationPanel.createOKCancelPanel on Cancel button action listener");
				}
			}	
		});
		
		thePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		thePanel.add( theOKButton );
		thePanel.add( theCancelButton );
		
		return thePanel;
	}
	
	public static void launchThePanel(
			final IRPGraphElement theTransitionGraphEl ){
	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame(
						"Populate requirements on " + 
								Logger.elementInfo( theTransitionGraphEl.getModelObject() ) );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				
				PopulateTransitionRequirementsPanel thePanel = 
						new PopulateTransitionRequirementsPanel( theTransitionGraphEl );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public static Set<IRPRequirement> getRequirementsRelatedTo(
			IRPTransition theTransition ){
		
		Set<IRPRequirement> theDependsOns = new HashSet<>();
		
		IRPStereotype theDependencyStereotype = 
				GeneralHelpers.getStereotypeIn( 
						theTransition.getProject(), 
						"traceabilityTypeToUseForFunctions", 
						"FunctionalAnalysisPkg" );
				
		IRPModelElement theOwner = 
				GeneralHelpers.findOwningClassIfOneExistsFor( 
						theTransition );
		
		// Look for matches related to the trigger

		IRPTrigger theTrigger = theTransition.getItsTrigger();
		
		if( theTrigger != null ){
			
			IRPInterfaceItem theInterfaceItem = theTrigger.getItsOperation();
			
			if( theInterfaceItem != null ){
				
				theDependsOns.addAll(
						TraceabilityHelper.getRequirementsThatTraceFromWithStereotype(
								theInterfaceItem, 
								theDependencyStereotype.getName() ) );
			}	
		}

		
		// Look for matches related to the guard

		IRPGuard theGuard = theTransition.getItsGuard();
										
		if( theGuard != null ){
			
			String theBody = theGuard.getBody();
			
			if( theBody != null && !theBody.isEmpty() ){
				
				theDependsOns.addAll(
						getReqtsThatTraceFromRelatedToElsMentionedIn(
								theBody,
								(IRPClassifier) theOwner,
								theDependencyStereotype ) );
			}

		}
		
		// Look for matches related to the actions

		IRPAction theAction = theTransition.getItsAction();
		
		if( theAction != null ){
			
			String theBody = theAction.getBody();
			
			if( theBody != null && !theBody.isEmpty() ){
				
				theDependsOns.addAll(
						getReqtsThatTraceFromRelatedToElsMentionedIn(
								theBody,
								(IRPClassifier) theOwner,
								theDependencyStereotype ) );
			}
			
		}
		
		return theDependsOns;
	}

	private static Set<IRPRequirement> getReqtsThatTraceFromRelatedToElsMentionedIn(
			String theText,
			IRPClassifier relatedToTheOwner, 
			IRPStereotype theDependencyStereotype ){
		
		Set<IRPRequirement> theReqts = new HashSet<>();
		
		Set<IRPOperation> theOps = extractOperationsMentionedIn( 
				theText, 
				relatedToTheOwner );
		
		for( IRPOperation theOp : theOps ){
			
			theReqts.addAll(
					TraceabilityHelper.getRequirementsThatTraceFromWithStereotype(
							theOp, 
							theDependencyStereotype.getName() ) );							
		}
		
		Set<IRPAttribute> theAttributes = extractAttributesMentionedIn( 
				theText, 
				relatedToTheOwner );
		
		for( IRPAttribute theAttribute : theAttributes ){
			
			theReqts.addAll(
					TraceabilityHelper.getRequirementsThatTraceFromWithStereotype(
							theAttribute, 
							theDependencyStereotype.getName() ) );							
		}
		
		return theReqts;
	}
	
	public static Set<IRPOperation> extractOperationsMentionedIn( 
			String theText, 
			IRPClassifier ownedByClassifier ){
		
		Set<IRPOperation> theOperations = new HashSet<>();
		
		@SuppressWarnings("unchecked")
		List<IRPOperation> theClassifiersOps = 
				ownedByClassifier.getOperations().toList();
		
		for( IRPOperation theClassifiersOp : theClassifiersOps ){
			
			if( theText.contains( theClassifiersOp.getName() + "(") ){
				Logger.writeLine( theClassifiersOp, "match found" );
				theOperations.add( theClassifiersOp );
			}		
		}
		
		return theOperations;
	}
	
	public static Set<IRPAttribute> extractAttributesMentionedIn( 
			String theText, 
			IRPClassifier ownedByClassifier ){
		
		Set<IRPAttribute> theAttributes = new HashSet<>();
		
		@SuppressWarnings("unchecked")
		List<IRPAttribute> theOwnedAttributes = 
				ownedByClassifier.getAttributes().toList();
		
		for( IRPAttribute theOwnedAttribute : theOwnedAttributes ){
			
			if( theText.contains( theOwnedAttribute.getName() ) ){
				Logger.writeLine( theOwnedAttribute, "match found" );
				theAttributes.add( theOwnedAttribute );
			}		
		}
		
		return theAttributes;
	}
	
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		return true;
	}

	protected void performAction() {
		
		// do silent check first
		if( checkValidity( false ) ){
			
			List<IRPRequirement> theReqts = m_RequirementsPanel.getSelectedRequirementsList();
			
			if( theReqts.isEmpty() ){
				
				Logger.writeLine("Doing nothing as there are no requirements");
				
			} else {
			
				List<IRPModelElement> theStartLinkEls = new ArrayList<>();
				theStartLinkEls.add( m_Transition );
				
				List<IRPGraphElement> theStartLinkGraphEls = new ArrayList<>();
				theStartLinkGraphEls.add( m_TransitionGE );
				
				int x = GraphElInfo.getMidX( m_TransitionGE );
				int y = GraphElInfo.getMidY( m_TransitionGE );

				for( IRPRequirement theReqt : theReqts ){
					
					List<IRPModelElement> theEndLinkEls = new ArrayList<>();
					theEndLinkEls.add( theReqt );
					
					@SuppressWarnings("unchecked")
					List<IRPGraphElement> theReqtGEs = 
							m_StatechartDiagram.getCorrespondingGraphicElements( theReqt ).toList();
					
					if( theReqtGEs.isEmpty() ){
						
						IRPGraphNode theGraphNode = m_StatechartDiagram.addNewNodeForElement(
								theReqt, x+100, y+70, 300, 100 );
						
						x = x + 30;
						y = y + 30;
						
						theReqtGEs.add( theGraphNode );
					}
					
					SmartLinkInfo theSmartLinkInfo = new SmartLinkInfo(
							theStartLinkEls, theStartLinkGraphEls, theEndLinkEls, theReqtGEs );
					
					theSmartLinkInfo.createDependencies( true );
				}
			}
			
		} else {
			Logger.writeLine("Error in PopulateRelatedRequirementsPanel.performAction, checkValidity returned false");
		}	
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #224 25-AUG-2017: Added new menu to roll up traceability to the transition and populate on STM (F.J.Chadburn)

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