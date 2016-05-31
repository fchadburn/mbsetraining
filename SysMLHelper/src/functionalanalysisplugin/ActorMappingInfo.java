package functionalanalysisplugin;

import java.util.List;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class ActorMappingInfo {

	public ActorMappingInfo(
			RhapsodyComboBox theRhapsodyComboBox,
			JCheckBox theActorCheckBox, 
			JTextField theActorName,
			IRPActor theSourceActor,
			IRPProject theProject) {
		
		super();
		this.m_InheritedFromComboBox = theRhapsodyComboBox;
		this.m_ActorCheckBox = theActorCheckBox;
		this.m_ActorNameTextField = theActorName;
		this.m_SourceActor = theSourceActor;
		this.m_Project = theProject;
	}

	final public String m_ActorBlankName = "EnterActorName";
	
	private RhapsodyComboBox m_InheritedFromComboBox;
	private JCheckBox m_ActorCheckBox;
	private JTextField m_ActorNameTextField;
	private IRPActor m_SourceActor;
	private IRPProject m_Project;
	
	
	public JTextField getTextField(){
		return m_ActorNameTextField;
	}
	
	public boolean isSelected(){
		return m_ActorCheckBox.isSelected();
	}
	
	public String getName(){
		return m_ActorNameTextField.getText();
	}
	
	public String getSourceActorName(){
		return m_SourceActor.getName();
	}
	
	public IRPActor getSourceActor(){
		return m_SourceActor;
	}
	
	public void updateToBestActorNamesBasedOn(String theBlockName){
		
		String theOriginalActorName;
		
		if (m_SourceActor != null){
			theOriginalActorName = m_SourceActor.getName();
		} else {
			theOriginalActorName = m_ActorBlankName;
		}
		
		String theProposedActorName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toLegalClassName( theOriginalActorName ) + "_" + theBlockName, 
				"Actor", 
				m_Project );
		
		m_ActorNameTextField.setText( theProposedActorName );
	}
	
	public IRPInstance addActorPartTo(IRPClass theUsageBlock, String withNameForActor){
		
		IRPInstance theActorPart = null;
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = theUsageBlock.getNestedElementsByMetaClass("Part", 0).toList();
		
		IRPClassifier theLogicalSystemBlock = null;
		IRPInstance theLogicalSystemPart = null;
		
		IRPClassifier theTesterBlock = null;
		IRPInstance theTesterPart = null;
		
		for (IRPInstance thePart : theParts) {
			
			if (GeneralHelpers.hasStereotypeCalled("LogicalSystem", thePart)){
				theLogicalSystemPart = thePart;
				theLogicalSystemBlock = thePart.getOtherClass();
				Logger.writeLine(theLogicalSystemPart, "is the LogicalSystem part");
				Logger.writeLine(theLogicalSystemBlock, "is the LogicalSystem block");
			}
			if (GeneralHelpers.hasStereotypeCalled("TestDriver", thePart)){	
				theTesterPart = thePart;
				theTesterBlock = thePart.getOtherClass();
				Logger.writeLine(theTesterPart, "is the Tester part");
				Logger.writeLine(theTesterBlock, "is the Tester block");
			}
		}
		
		if (theLogicalSystemBlock != null && theTesterBlock != null){
			IRPActor theTestActor = ((IRPPackage) theUsageBlock.getOwner()).addActor( withNameForActor );
			
			IRPActor theTestbench = (IRPActor) theTestActor.getProject().findNestedElementRecursive("Testbench", "Actor");
			
			if (theTestbench != null){
				theTestActor.addGeneralization( theTestbench );
			} else {
				Logger.writeLine("Error: Unable to find Actor with name Testbench");
			}
			
			// Make each of the actors a part of the UsageDomain block
			theActorPart = addPartTo(theUsageBlock, theTestActor);
			
			// and connect actor to the LogicalSystem block
	    	IRPPort theActorToSystemPort = (IRPPort) theTestActor.addNewAggr("Port", "pLogicalSystem");
			IRPPort theSystemToActorPort = (IRPPort) theLogicalSystemBlock.addNewAggr("Port", "p" + theTestActor.getName());
			IRPLink theLogicalSystemLink = (IRPLink) theUsageBlock.addLink(
					theActorPart, theLogicalSystemPart, null, theActorToSystemPort, theSystemToActorPort);
			theLogicalSystemLink.changeTo("connector");
			
			// and connect actor to the TestDriver block
	    	IRPPort theActorToTesterPort = (IRPPort) theTestActor.addNewAggr("Port", "pTester");
			IRPPort theTesterToActorPort = (IRPPort) theTesterBlock.addNewAggr("Port", "p" + theTestActor.getName());
			IRPLink theTesterLink = (IRPLink) theUsageBlock.addLink(
					theActorPart, theTesterPart, null, theActorToTesterPort, theTesterToActorPort);
			theTesterLink.changeTo("connector");
		}
		
		return theActorPart;
	}
	
	private static IRPInstance addPartTo(
			IRPClassifier theElement, 
			IRPClassifier typedByElement){
		
		IRPInstance thePart = (IRPInstance) theElement.addNewAggr("Part", "its" + typedByElement.getName());
		thePart.setOtherClass(typedByElement);
		
		return thePart;
	}
	
	public void performActorPartCreationIfSelectedTo(
			IRPClass theUsageDomainBlock){
		
		if (isSelected()){

			String theLegalActorName = getName().replaceAll(" ", "");
			IRPInstance theActorPart = addActorPartTo(theUsageDomainBlock, theLegalActorName);	
			theActorPart.highLightElement();

			String theText = "Create actor called " + m_ActorNameTextField.getText();
			
			IRPClassifier theClassifier = theActorPart.getOtherClass();
			theClassifier.highLightElement();

			IRPModelElement theInheritedFrom = m_InheritedFromComboBox.getSelectedRhapsodyItem();

			if (theInheritedFrom != null){
				
				theText = theText + " inherited from " + theInheritedFrom.getName();
				
				theClassifier.addGeneralization( (IRPClassifier) theInheritedFrom );
			}

			Logger.writeLine(theText);
		} else {
			Logger.writeLine("Not selected");
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #023 30-MAY-2016: Added form to support validation checks for analysis block hierarchy creation (F.J.Chadburn) 
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    
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