package functionalanalysisplugin;

import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.telelogic.rhapsody.core.*;

public abstract class CreateTracedElementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RequirementSelectionPanel m_RequirementsPanel = null;
	protected IRPClassifier m_TargetBlock = null;
	protected JTextField m_ChosenNameTextField = null;
	protected IRPGraphElement m_SourceGraphElement = null;
	
	public CreateTracedElementPanel(
			IRPGraphElement forSourceGraphElement, 
			IRPClassifier onTargetBlock) {
		
		super();

		m_TargetBlock = onTargetBlock;		
		m_SourceGraphElement = forSourceGraphElement;
		
		IRPModelElement theModelObject = m_SourceGraphElement.getModelObject();
		
		List<IRPRequirement> tracedToReqts = TraceabilityHelper.getRequirementsThatTraceFrom( theModelObject );
		
		m_RequirementsPanel = new RequirementSelectionPanel( tracedToReqts );
	}
	
	// implementation specific provided by parent
	abstract boolean checkValidity(boolean isMessageEnabled);
	
	// implementation specific provided by parent
	abstract void performAction();
		
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
	
	protected static void bleedColorToElementsRelatedTo( IRPGraphElement theGraphEl ){
		
		String theColorSetting = "255,0,0";
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		IRPModelElement theEl = theGraphEl.getModelObject();
		
		if (theEl != null){
			
			Logger.writeLine("Setting color to red for " + theEl.getName());
			theGraphEl.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theExistingDeps = theEl.getDependencies().toList();
			
			for (IRPDependency theDependency : theExistingDeps) {
				
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				if (theDependsOn != null && theDependsOn instanceof IRPRequirement){					
					bleedColorToGraphElsRelatedTo( theDependsOn, theColorSetting, theDiagram );
					bleedColorToGraphElsRelatedTo( theDependency, theColorSetting, theDiagram );
				}
			}
		}
	}

	private static void bleedColorToGraphElsRelatedTo(
			IRPModelElement theEl, String theColorSetting, IRPDiagram onDiagram){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphElsRelatedToElement = 
				onDiagram.getCorrespondingGraphicElements( theEl ).toList();
		
		for (IRPGraphElement irpGraphElement : theGraphElsRelatedToElement) {
			
			irpGraphElement.setGraphicalProperty("ForegroundColor", theColorSetting);
			
			IRPModelElement theModelObject = irpGraphElement.getModelObject();
			
			if (theModelObject != null){
				Logger.writeLine("Setting color to red for " + theModelObject.getName());
			}
		}
	}
	
	protected static void addTraceabilityDependenciesTo(
			IRPModelElement theElement, List<IRPRequirement> theReqtsToAdd){
	
		IRPStereotype theDependencyStereotype = 
				FunctionalAnalysisSettings.getStereotypeForFunctionTracing(theElement.getProject());
		
		if (theDependencyStereotype != null){
			for (IRPRequirement theReqt : theReqtsToAdd) {
				
				IRPDependency theDep = theElement.addDependencyTo(theReqt);
				theDep.setStereotype(theDependencyStereotype);		
				Logger.writeLine("Added a " + theDependencyStereotype.getName() + " dependency to " + Logger.elementInfo( theElement ));
			}
		} else {
			Logger.writeLine("Error in addTraceabilityDependenciesTo, unable to find stereotype to apply to dependencies");
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 

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
