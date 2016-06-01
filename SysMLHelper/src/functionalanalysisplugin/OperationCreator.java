package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import com.telelogic.rhapsody.core.*;

public class OperationCreator {
        
    // test only
    public static void main(String[] args) {
	
    	@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = FunctionalAnalysisPlugin.getRhapsodyApp().getSelectedGraphElements().toList();
    	
    	for (IRPGraphElement irpGraphElement : theSelectedGraphEls) {
    		IRPPackage forPackageUnderDev = FunctionalAnalysisSettings.getPackageUnderDev( FunctionalAnalysisPlugin.getActiveProject() );
			createSystemOperationFor( irpGraphElement, forPackageUnderDev);
		}
    }
    
	public static void createIncomingEventsFor(
			IRPProject theActiveProject,
			List<IRPGraphElement> theSelectedGraphEls) {
		
		IRPPackage thePackageUnderDev = FunctionalAnalysisSettings.getPackageUnderDev( theActiveProject );
		
		for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
			createIncomingEventFor( theGraphEl, thePackageUnderDev );
		}
	}
    
	private static void createIncomingEventFor(
			final IRPGraphElement theSourceGraphElement, 
			final IRPPackage forPackageUnderDev){
		
		final IRPInstance partUnderDev = getPartUnderDev( forPackageUnderDev );
		
		final IRPModelElement theActor = 
				GeneralHelpers.launchDialogToSelectElement(
						getActorsRelatedTo( partUnderDev ), "Select Actor", true);
		
		if (theActor != null && theActor instanceof IRPActor){

			final IRPClassifier theLogicalSystem = partUnderDev.getOtherClass();
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );
					
					JFrame frame = new JFrame( "Create an incoming event from " + Logger.elementInfo( theActor ));
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateIncomingEventPanel thePanel = 
							new CreateIncomingEventPanel(
									theSourceGraphElement, 
									theLogicalSystem, 
									(IRPActor)theActor, 
									forPackageUnderDev );

					frame.setContentPane( thePanel );
					
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		} else {
			Logger.writeLine("No actor was selected");
		}
	}

	public static void createOutgoingEventsFor(
			IRPProject theActiveProject,
			List<IRPGraphElement> theSelectedGraphEls) {
		
		IRPPackage thePackageUnderDev = FunctionalAnalysisSettings.getPackageUnderDev( theActiveProject );
		
		for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
			createOutgoingEventFor( theGraphEl, thePackageUnderDev );
		}
	}
	
	private static void createOutgoingEventFor(
			final IRPGraphElement theSourceGraphElement, 
			final IRPPackage forPackageUnderDev){
		
		final IRPInstance partUnderDev = getPartUnderDev( forPackageUnderDev );
		
		final IRPModelElement theActor = 
				GeneralHelpers.launchDialogToSelectElement(
						getActorsRelatedTo( partUnderDev ), "Select Actor to send Event to", true);
		
		if (theActor != null && theActor instanceof IRPActor){

			final IRPClassifier theLogicalSystem = partUnderDev.getOtherClass();
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					
					JFrame.setDefaultLookAndFeelDecorated( true );
					JFrame frame = new JFrame("Create an outgoing event to " + Logger.elementInfo( theActor ) );
					
					frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

					CreateOutgoingEventPanel thePanel = new CreateOutgoingEventPanel(
							theSourceGraphElement, 
							theLogicalSystem, 
							(IRPActor)theActor, 
							forPackageUnderDev);

					frame.setContentPane( thePanel );
					frame.pack();
					frame.setLocationRelativeTo( null );
					frame.setVisible( true );
				}
			});
		} else {
			Logger.writeLine("No actor was selected");
		}
	}
	
	public static void createSystemOperationsFor(
			IRPProject theActiveProject,
			List<IRPGraphElement> theSelectedGraphEls) {
		
		IRPPackage thePackageUnderDev = FunctionalAnalysisSettings.getPackageUnderDev( theActiveProject );
		
		for (IRPGraphElement theGraphEl : theSelectedGraphEls) {
			createSystemOperationFor( theGraphEl, thePackageUnderDev );
		}
	}
	
	private static void createSystemOperationFor(
			final IRPGraphElement selectedDiagramEl, 
			final IRPPackage forPackageUnderDev){
	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				IRPClassifier theLogicalSystemBlock = getLogicalSystemBlock( forPackageUnderDev );
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame(
						"Create an operation on " + theLogicalSystemBlock.getUserDefinedMetaClass() 
						+ " called " + theLogicalSystemBlock.getName());
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				
				CreateOperationPanel thePanel = new CreateOperationPanel(
						selectedDiagramEl, 
						theLogicalSystemBlock);

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
    
    private static IRPClassifier getLogicalSystemBlock(IRPPackage inThePackage){
    	
		IRPInstance partUnderDev = null;
		
		List<IRPModelElement> theBlocks = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype("Part", "LogicalSystem", inThePackage);
			
		if (theBlocks.size()==1){
				
			partUnderDev = (IRPInstance) theBlocks.get(0);
				
			Logger.writeLine(partUnderDev, "Found");
		} else {
			Logger.writeLine("Error in getLogicalSystemBlock: Can't find LogicalSystem block");
		}
		
		final IRPClassifier theLogicalSystem = partUnderDev.getOtherClass();
		
		return theLogicalSystem;
    }
    
	private static IRPInstance getPartUnderDev(IRPPackage inThePackage){
		
		IRPInstance partUnderDev = null;
		
		List<IRPModelElement> theBlocks = 
					GeneralHelpers.findElementsWithMetaClassAndStereotype("Part", "LogicalSystem", inThePackage);
			
		if (theBlocks.size()==1){
				
			partUnderDev = (IRPInstance) theBlocks.get(0);
				
			Logger.writeLine(partUnderDev, "Found");
		} else {
			Logger.writeLine("Error in getPartUnderDev: Can't find LogicalSystem block");
		}

		return partUnderDev;
	}
	
	private static IRPModelElement getOwningClassifierFor(IRPModelElement theState){
		
		IRPModelElement theOwner = theState.getOwner();
		
		while (theOwner.getMetaClass().equals("State") || theOwner.getMetaClass().equals("Statechart")){
			theOwner = theOwner.getOwner();
		}
		
		Logger.writeLine("The owner for " + Logger.elementInfo(theState) + " is " + Logger.elementInfo(theOwner));
			
		return theOwner;
	}
	
	private static IRPState getStateCalled(
			String theName, 
			IRPStatechart inTheDiagram, 
			IRPModelElement ownedByEl){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsInDiagram = inTheDiagram.getElementsInDiagram().toList();
		
		IRPState theState = null;
		
		int count = 0;
		
		for (IRPModelElement theEl : theElsInDiagram) {
			
			if (theEl instanceof IRPState 
					&& theEl.getName().equals(theName)
					&& getOwningClassifierFor(theEl).equals(ownedByEl)){
				
				Logger.writeLine("Found state called " + theEl.getName() + " owned by " + theEl.getOwner().getFullPathName());
				
				theState = (IRPState) theEl;
				count++;
			}
		}
		
		if (count != 1){
			Logger.writeLine("Warning in getStateCalled (" + count + ") states called " + theName + " were found");
		}
		
		return theState;
	}
	
	private static List<IRPModelElement> getActorsRelatedTo(IRPInstance theLogicalSystemPart){
		
		List<IRPModelElement> theActors = new ArrayList<IRPModelElement>();
		
		// get the logical system part and block
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = 
				theLogicalSystemPart.getOwner().getNestedElementsByMetaClass("Part", 0).toList();
		
		for (IRPInstance thePart : theParts) {
			
			IRPClassifier theOtherClass = thePart.getOtherClass();
			
			if (theOtherClass instanceof IRPActor){
				theActors.add((IRPActor) theOtherClass);
			}
		}
		
		return theActors;
	}

	public static IRPOperation createTestCaseFor( IRPClass theTestDriver ){
		
		IRPOperation theOp = null;
		
		if (GeneralHelpers.hasStereotypeCalled("TestDriver", theTestDriver)){
			
			Logger.writeLine("createTestCaseFor was invoked for " + Logger.elementInfo(theTestDriver));
			
			String[] theSplitName = theTestDriver.getName().split("_");
			
			String thePrefix = theSplitName[0] + "_Test_";
			
			Logger.writeLine("The prefix for TestCase was calculated as '" + thePrefix + "'");
			
			int count = 0;
			boolean isUniqueNumber = false;
			String nameToTry = null;
			
			while (isUniqueNumber==false){
				count++;
				nameToTry = thePrefix + String.format("%03d", count);
				
				if (theTestDriver.findNestedElement(nameToTry, "Operation") == null){
					isUniqueNumber = true;
				}
			}
			
			if (isUniqueNumber){
				theOp = theTestDriver.addOperation(nameToTry);
				theOp.highLightElement();
				theOp.changeTo("Test Case");
				
				IRPState theState = OperationCreator.getStateCalled("Ready", theTestDriver.getStatechart(), theTestDriver);
				
				String theEventName = "ev" + nameToTry;
						
				IRPEventReception theEventReception = theTestDriver.addReception( theEventName );
				
				if (theEventReception != null){
					IRPEvent theEvent = theEventReception.getEvent();
					
					Logger.writeLine("The state called " + theState.getFullPathName() + " is owned by " + theState.getOwner().getFullPathName());
					IRPTransition theTransition = theState.addInternalTransition( theEvent );
					theTransition.setItsAction( theOp.getName() + "();");
				}
			}		
			
		} else {
			UserInterfaceHelpers.showWarningDialog(
					"This operation only works if you right-click a «TestDriver» block");	    
		}
		
		return theOp;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #007 05-MAY-2016: Move FileHelper into generalhelpers and remove duplicate class (F.J.Chadburn)
    #010 08-MAY-2016: Remove white-space from actor names (F.J.Chadburn)
    #012 08-MAY-2016: Fix Send event without value plus re-word check box titles (F.J.Chadburn)
    #019 15-MAY-2016: Improvements to Functional Analysis Block default naming approach (F.J.Chadburn)
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    
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
