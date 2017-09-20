package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.List;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

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
		
		String theDesiredName;
		
		if( theBlockName.isEmpty() ){
			theDesiredName = GeneralHelpers.toLegalClassName( theOriginalActorName );
		} else {
			theDesiredName = GeneralHelpers.toLegalClassName( theOriginalActorName ) + "_" + theBlockName;
		}
		
		String theProposedActorName = GeneralHelpers.determineUniqueNameBasedOn( 
				theDesiredName, 
				"Actor", 
				m_Project );
		
		m_ActorNameTextField.setText( theProposedActorName );
	}
	
	@SuppressWarnings("unchecked")
	static IRPLink getExistingLinkBetweenBaseClassifiersOf(
			IRPClassifier theClassifier, 
			IRPClassifier andTheClassifier ){
		
		int isLinkFoundCount = 0;
		IRPLink theExistingLink = null;
		
		IRPModelElement theFAPackage = 
				theClassifier.getProject().findNestedElementRecursive(
						"FunctionalAnalysisPkg", "Package" );

		if( theFAPackage != null && theFAPackage instanceof IRPPackage ){
			
			List<IRPClassifier> theOtherEndsBases = new ArrayList<>();
			theOtherEndsBases.add( andTheClassifier );
			theOtherEndsBases.addAll( andTheClassifier.getBaseClassifiers().toList() );
			
			List<IRPClassifier> theSourcesBases = new ArrayList<>();
			theSourcesBases.add( theClassifier );
			theSourcesBases.addAll( theClassifier.getBaseClassifiers().toList() );
			
			List<IRPClass> theBuildingBlocks = 
					FunctionalAnalysisSettings.getBuildingBlocks( 
							(IRPPackage) theFAPackage );

			for( IRPClass theBuildingBlock : theBuildingBlocks ){
				
				Logger.writeLine("Found theBuildingBlock: " + Logger.elementInfo( theBuildingBlock ) );
				
				List<IRPLink> theLinks = theBuildingBlock.getLinks().toList();
			
				for( IRPLink theLink : theLinks ){
					
					IRPModelElement fromEl = theLink.getFromElement();
					IRPModelElement toEl = theLink.getToElement();
					
					if( fromEl != null && 
						fromEl instanceof IRPInstance && 
						toEl != null && 
						toEl instanceof IRPInstance ){
					
						IRPClassifier fromClassifier = ((IRPInstance)fromEl).getOtherClass();
						IRPClassifier toClassifier = ((IRPInstance)toEl).getOtherClass();
						
						if( ( theOtherEndsBases.contains( toClassifier ) &&
						      theSourcesBases.contains( fromClassifier ) ) ||
								
							( theSourcesBases.contains( toClassifier ) &&
							  theOtherEndsBases.contains( fromClassifier ) ) ){
							
							Logger.writeLine("Found that " + Logger.elementInfo( fromClassifier ) 
									+ " is already linked to " + Logger.elementInfo( toClassifier ) );
							
							theExistingLink = theLink;
							isLinkFoundCount++;
						}						
					}
				}
			}
		}
		
		if( isLinkFoundCount > 1 ){
			Logger.writeLine("Warning in getExistingLinkBetweenBaseClassifiersOf, there are " + isLinkFoundCount );
		}
		
		return theExistingLink;
	}
	
	public IRPInstance performActorPartCreationIfSelectedIn(
			IRPClass theAssemblyBlock,
			IRPClass connectedToBlock ){
		
		IRPInstance theActorPart = null;
		
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
						(connectedToBlock != null) &&
						theOtherClass.equals( connectedToBlock ) ){

						theConnectedToPart = thePart;

						Logger.writeLine( theConnectedToPart, "was found to connect the actors to, and is typed by " + 
								Logger.elementInfo( connectedToBlock ) );

					} else if ( isTestDriver ){

						theTesterPart = thePart;
						theTesterBlock = theOtherClass;

						Logger.writeLine( theTesterPart, "was found as the test driver, and is typed by " + 
								Logger.elementInfo( theTesterBlock ) );

					}
				}				
			}

			IRPPackage thePackageForActor = 
					FunctionalAnalysisSettings.getPackageForActorsAndTest(
							theAssemblyBlock.getProject() );

			IRPActor theActor = thePackageForActor.addActor( theLegalActorName );
			theActor.highLightElement();

			IRPModelElement theInheritedFrom = 
					m_InheritedFromComboBox.getSelectedRhapsodyItem();

			String theText = "Create actor called " + m_ActorNameTextField.getText();

			// Make each of the actors a part of the SystemAssembly block
			theActorPart = 
					(IRPInstance) theAssemblyBlock.addNewAggr(
							"Part", "" );

			theActorPart.highLightElement();
			theActorPart.setOtherClass( theActor );

			if( theInheritedFrom != null ){

				theText = theText + " inherited from " + theInheritedFrom.getName();

				theActor.addGeneralization( (IRPClassifier) theInheritedFrom );
				theActor.highLightElement();

			} else {

				IRPActor theTestbench = 
						(IRPActor) theActor.getProject().findNestedElementRecursive(
								"Testbench", "Actor" );

				if( theTestbench != null ){
					theActor.addGeneralization( theTestbench );
				} else {
					Logger.writeLine("Error: Unable to find Actor with name Testbench");
				}
			}

			if( theConnectedToPart != null ){

				connectActorPartWithBlockPartIn(
						theAssemblyBlock,
						theConnectedToPart, 
						theActorPart );					
			}

			if( theTesterBlock == null ){

				UserInterfaceHelpers.showWarningDialog(
						"A new Actor part called " + theActorPart.getName() + " was added to " + Logger.elementInfo( theAssemblyBlock ) + ". \n" +
						"However, no TestDriver part was found hence skipping the creation of links to this. In future, you \n" +
						"may want to consider using the FullSim structure to get the benefits of test driver creation. " );
			} else {

				connectActorPartWithTesterPartIn(
						theAssemblyBlock,
						theTesterPart, 
						theActorPart );
			}

			Logger.writeLine("Finishing adding part connected to actor");

		} else {
			Logger.writeLine("Not selected");
		}
		
		return theActorPart;
	}

	private void connectActorPartWithTesterPartIn(
			IRPClass theAssemblyBlock,
			IRPInstance theTesterPart,
			IRPInstance theActorPart ){
		
		IRPClassifier theActor = theActorPart.getOtherClass();
		IRPClassifier theTesterBlock = theTesterPart.getOtherClass();
		
		IRPLink existingLinkConnectingTesterToActor = 
				getExistingLinkBetweenBaseClassifiersOf(
						theTesterBlock, theActor );
		
		IRPPort theActorToTesterPort = null;
		IRPPort theTesterToActorPort = null;
		
		if( existingLinkConnectingTesterToActor != null ){
			
			Logger.writeLine( "There are existing ports between " + 
					Logger.elementInfo( theTesterBlock ) + " and " + Logger.elementInfo( theActor ) );
		
			IRPPort fromPort = existingLinkConnectingTesterToActor.getFromPort();
			IRPPort toPort = existingLinkConnectingTesterToActor.getToPort();
			
			if( fromPort.getOwner() instanceof IRPActor ){
				theActorToTesterPort = fromPort;
				theTesterToActorPort = toPort;
			} else {
				theActorToTesterPort = toPort;
				theTesterToActorPort = fromPort;						
			}
			
		} else {

			Logger.writeLine( "Creating a new connector between " + 
					Logger.elementInfo( theTesterBlock ) + " and " + Logger.elementInfo( theActor ) );

			try {
				// and connect actor to the TestDriver block
		    	theActorToTesterPort = 
		    			(IRPPort) theActor.addNewAggr( "Port", "pTester" );
		    	
				theTesterToActorPort = 
						(IRPPort) theTesterBlock.addNewAggr(
								"Port", "p" + theActor.getName() );
			} catch (Exception e) {
				Logger.writeLine("Exception while trying to add ports");
			}
		}
		
		IRPLink theTesterLink = 
				(IRPLink) theAssemblyBlock.addLink(
						theActorPart, 
						theTesterPart, 
						null, 
						theActorToTesterPort, 
						theTesterToActorPort );
		
		theTesterLink.changeTo("connector");
	}

	private void connectActorPartWithBlockPartIn(
			IRPClass theAssemblyBlock,
			IRPInstance theConnectedToPart,
			IRPInstance theActorPart ){
		
		IRPClassifier theActor = theActorPart.getOtherClass();
		IRPClassifier connectedToBlock = theConnectedToPart.getOtherClass();
		
		IRPLink existingLinkConnectingBlockToActor = 
				getExistingLinkBetweenBaseClassifiersOf(
						connectedToBlock, theActor );
		
		IRPPort theActorToSystemPort = null;
		IRPPort theSystemToActorPort = null;
		
		if( existingLinkConnectingBlockToActor != null ){
			
			Logger.writeLine( "There is an existing connector between " + 
					Logger.elementInfo( connectedToBlock ) + " and " + Logger.elementInfo( theActor ) );
		
			IRPPort fromPort = existingLinkConnectingBlockToActor.getFromPort();
			IRPPort toPort = existingLinkConnectingBlockToActor.getToPort();
			
			if( fromPort.getOwner() instanceof IRPActor ){
				theActorToSystemPort = fromPort;
				theSystemToActorPort = toPort;
			} else {
				theActorToSystemPort = toPort;
				theSystemToActorPort = fromPort;						
			}	
		} else {

			Logger.writeLine( "Creating a new connector between " + 
					Logger.elementInfo( connectedToBlock ) + " and " + Logger.elementInfo( theActor ) );

			String theActorPortName = 
					GeneralHelpers.determineUniqueNameBasedOn(
							"p" + connectedToBlock.getName() , "Port", theActor);

			Logger.writeLine("Attempting to create port called " + theActorPortName + " owned by " + Logger.elementInfo( theActor ));

			// and connect actor to the LogicalSystem block
			theActorToSystemPort = 
					(IRPPort) theActor.addNewAggr( 
							"Port", theActorPortName);

			String theSystemPortName = 
					GeneralHelpers.determineUniqueNameBasedOn(
							"p" + theActor.getName() , "Port", connectedToBlock);

			Logger.writeLine("Attempting to create port called " + theSystemPortName + " owned by " + Logger.elementInfo( connectedToBlock ));

			try {
				theSystemToActorPort = 
						(IRPPort) connectedToBlock.addNewAggr(
								"Port", theSystemPortName );	
			} catch (Exception e) {
				Logger.writeLine("Exception while trying to create system to actor port");
			}			
		}
		
		try {
			IRPLink theLogicalSystemLink = 
					(IRPLink) theAssemblyBlock.addLink(
							theActorPart, 
							theConnectedToPart, 
							null, 
							theActorToSystemPort, 
							theSystemToActorPort );
			
			theLogicalSystemLink.changeTo("connector");
			
		} catch (Exception e) {
			Logger.writeLine("Exception while trying to addLink");
		}
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #023 30-MAY-2016: Added form to support validation checks for analysis block hierarchy creation (F.J.Chadburn) 
    #025 31-MAY-2016: Add new menu and dialog to add a new actor to package under development (F.J.Chadburn)
    #106 03-NOV-2016: Ease usage by renaming UsageDomain block to SystemAssembly and moving up one package (F.J.Chadburn)
    #108 03-NOV-2016: Added tag for packageForActorsAndTest to FunctionalAnalysisPkg settings (F.J.Chadburn)
    #120 25-NOV-2016: Enable TestDriver inheritance in the FullSim block creation dialog (F.J.Chadburn)
    #126 25-NOV-2016: Fixes to CreateNewActorPanel to cope better when multiple blocks are in play (F.J.Chadburn)
    #135 02-DEC-2016: Avoid port proliferation in inheritance tree for actors/system (F.J.Chadburn)
    #149 18-DEC-2016: Improve robustness to allow actor part creation if no TestDriver is present (F.J.Chadburn)
    #187 29-MAY-2017: Provide option to re-create «AutoShow» sequence diagram when adding new actor (F.J.Chadburn)
    #230 20-SEP-2017: Initial alpha trial for create test case script from a sequence diagram (F.J.Chadburn)

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