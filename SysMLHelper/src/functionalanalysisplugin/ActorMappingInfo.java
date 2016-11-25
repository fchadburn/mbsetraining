package functionalanalysisplugin;

import java.util.List;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;

public class ActorMappingInfo {

	final public String m_ActorBlankName = "EnterActorName";
	
	private RhapsodyComboBox m_InheritedFromComboBox;
	private JCheckBox m_ActorCheckBox;
	private JTextField m_ActorNameTextField;
	private IRPActor m_SourceActor = null;
	private IRPProject m_Project = null;
	
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
	
	public void updateToBestActorNamesBasedOn(
			String theBlockName ){
		
		String theOriginalActorName;
		
		if( m_SourceActor != null ){
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

	public void performActorPartCreationIfSelectedIn(
			IRPClass theAssemblyBlock,
			IRPClass connectedToBlock ){
		
		if( isSelected() ){

			String theLegalActorName = getName().replaceAll(" ", "");
			
			// get the logical system part and block
			@SuppressWarnings("unchecked")
			List<IRPInstance> theParts = 
				theAssemblyBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();
			
			IRPInstance theConnectedToPart = null;
			
			IRPClassifier theTesterBlock = null;
			IRPInstance theTesterPart = null;
			
			for( IRPInstance thePart : theParts ) {
				
				IRPClassifier theOtherClass = thePart.getOtherClass();
				
				if( theOtherClass instanceof IRPClass ){
					
					boolean isTestDriver = 
							GeneralHelpers.hasStereotypeCalled( 
									"TestDriver", thePart );
					
					if( !isTestDriver &&
						theOtherClass.equals( connectedToBlock ) ){

						theConnectedToPart = thePart;

						Logger.writeLine( theConnectedToPart, "is the part to connect to" );
						Logger.writeLine( connectedToBlock, "is the block to connect to" );
						
					} else {
						
						theTesterPart = thePart;
						theTesterBlock = theOtherClass;
						
						Logger.writeLine(theTesterPart, "is the Tester part");
						Logger.writeLine(theTesterBlock, "is the Tester block");						
					}
				}				
			}
			
			if( connectedToBlock != null && theTesterBlock != null ){
			
				IRPPackage thePackageForActor = 
						FunctionalAnalysisSettings.getPackageForActorsAndTest(
								theAssemblyBlock.getProject() );
				
				IRPActor theActor = thePackageForActor.addActor( theLegalActorName );
				theActor.highLightElement();
				
				IRPModelElement theInheritedFrom = 
						m_InheritedFromComboBox.getSelectedRhapsodyItem();

				String theText = "Create actor called " + m_ActorNameTextField.getText();
				
				// Make each of the actors a part of the SystemAssembly block
				IRPInstance theActorPart = (IRPInstance) theAssemblyBlock.addNewAggr(
						"Part", "its" + theActor.getName() );
				
				theActorPart.highLightElement();
				theActorPart.setOtherClass( theActor );
				
				if( theInheritedFrom != null ){
					
					theText = theText + " inherited from " + theInheritedFrom.getName();
										
					theActor.addGeneralization( (IRPClassifier) theInheritedFrom );
					theActor.highLightElement();
				}
				
				IRPActor theTestbench = 
						(IRPActor) theActor.getProject().findNestedElementRecursive(
								"Testbench", "Actor" );
				
				if( theTestbench != null ){
					theActor.addGeneralization( theTestbench );
				} else {
					Logger.writeLine("Error: Unable to find Actor with name Testbench");
				}
				
				// and connect actor to the LogicalSystem block
		    	IRPPort theActorToSystemPort = 
		    			(IRPPort) theActor.addNewAggr( "Port", "pLogicalSystem" );
		    	
				IRPPort theSystemToActorPort = 
						(IRPPort) connectedToBlock.addNewAggr(
								"Port", "p" + theActor.getName() );
				
				IRPLink theLogicalSystemLink = 
						(IRPLink) theAssemblyBlock.addLink(
								theActorPart, 
								theConnectedToPart, 
								null, 
								theActorToSystemPort, 
								theSystemToActorPort );
				
				theLogicalSystemLink.changeTo("connector");
				
				// and connect actor to the TestDriver block
		    	IRPPort theActorToTesterPort = 
		    			(IRPPort) theActor.addNewAggr( "Port", "pTester" );
		    	
				IRPPort theTesterToActorPort = 
						(IRPPort) theTesterBlock.addNewAggr(
								"Port", "p" + theActor.getName() );
				
				IRPLink theTesterLink = 
						(IRPLink) theAssemblyBlock.addLink(
								theActorPart, 
								theTesterPart, 
								null, 
								theActorToTesterPort, 
								theTesterToActorPort );
				
				theTesterLink.changeTo("connector");
			}

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
    #106 03-NOV-2016: Ease usage by renaming UsageDomain block to SystemAssembly and moving up one package (F.J.Chadburn)
    #108 03-NOV-2016: Added tag for packageForActorsAndTest to FunctionalAnalysisPkg settings (F.J.Chadburn)
    #120 25-NOV-2016: Enable TestDriver inheritance in the FullSim block creation dialog (F.J.Chadburn)
    #126 25-NOV-2016: Fixes to CreateNewActorPanel to cope better when multiple blocks are in play (F.J.Chadburn)
    
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