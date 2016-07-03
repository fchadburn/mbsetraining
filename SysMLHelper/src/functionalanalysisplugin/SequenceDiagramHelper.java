package functionalanalysisplugin;

import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class SequenceDiagramHelper {	
	
	public static void populateRequirementsForSequenceDiagramsBasedOn(
			List<IRPModelElement> theSelectedEls){
		
		Set<IRPModelElement> theEls = buildSetOfElementsFor(theSelectedEls, "SequenceDiagram", true);
		
		for (IRPModelElement theEl : theEls) {
			populateRequirementsFor( (IRPSequenceDiagram)theEl );
		}
	}
	
	public static void updateVerificationsForSequenceDiagramsBasedOn(
			List<IRPModelElement> theSelectedEls){
		
		Set<IRPModelElement> theEls = buildSetOfElementsFor(theSelectedEls, "SequenceDiagram", true);
		
		for (IRPModelElement theEl : theEls) {
			updateVerificationsFor( (IRPSequenceDiagram)theEl );
		}
	}

	private static void removeExistingRequirementsFrom(IRPDiagram theDiagram){
		
		Set<IRPRequirement> theReqtsOnDiagram = buildSetOfRequirementsAlreadyOn( theDiagram );
		
		for (IRPRequirement theRequirement : theReqtsOnDiagram) {
			removeElementFromDiagram( theRequirement, theDiagram );
		}
	}

	private static void populateRequirementsFor(IRPSequenceDiagram theSD) {
		
		Logger.writeLine("Populate requirements invoked for " + Logger.elementInfo( theSD ) );
		removeExistingRequirementsFrom( theSD );
		
		Set<IRPRequirement> theRequirementsAdded = new HashSet<IRPRequirement>();
		
		IRPCollaboration theCollaboration = theSD.getLogicalCollaboration();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theMessagePoints = theCollaboration.getMessagePoints().toList();
		
		for (IRPModelElement irpModelElement : theMessagePoints) {
			
			IRPMessagePoint theMessagePoint = (IRPMessagePoint)irpModelElement;
			IRPMessage theMessage = theMessagePoint.getMessage();
			IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

			if (theInterfaceItem instanceof IRPEvent || 
				theInterfaceItem instanceof IRPOperation){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = theSD.getCorrespondingGraphicElements( theMessage ).toList();

				for (IRPGraphElement irpGraphElement : theGraphEls) {
					
					if (irpGraphElement instanceof IRPGraphEdge){
											
						IRPGraphEdge theGraphEdge = (IRPGraphEdge) irpGraphElement;

						IRPGraphicalProperty theGraphicalProperty = theGraphEdge.getGraphicalProperty("TargetPosition");
						String theValue = theGraphicalProperty.getValue();

						String[] xy = theValue.split(",");
						int top_left_x = Integer.parseInt(xy[0]);
						int top_left_y = Integer.parseInt(xy[1]);
						int x = top_left_x + 100;
						int xinc = 300;
						
						Set<IRPRequirement> theReqtsThatTraceFrom = 
								TraceabilityHelper.getRequirementsThatTraceFrom( theInterfaceItem, true );
						
						for (IRPRequirement theReqt : theReqtsThatTraceFrom) {
							
							// only populate once per diagram, i.e. first instance only
							if (!theRequirementsAdded.contains(theReqt)){
								
								//Logger.writeLine(theInterfaceItem, "traces to " + Logger.elementInfo( theReqt ));
								theSD.addNewNodeForElement(theReqt, x, top_left_y-20, 298, 58);
								theRequirementsAdded.add(theReqt);
								x=x+xinc;
							}

						}
					}
				}	
			}
		}
	}
	
	private static Set<IRPModelElement> buildSetOfElementsFor(
			List<IRPModelElement> theSelectedEls, String withMetaClass, boolean isRecursive) {
		
		Set<IRPModelElement> theMatchingEls = new HashSet<IRPModelElement>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			addElementIfItMatches(withMetaClass, theMatchingEls, theSelectedEl);
			
			if (isRecursive){
				
				@SuppressWarnings("unchecked")
				List<IRPModelElement> theCandidates = theSelectedEl.getNestedElementsByMetaClass(withMetaClass, 1).toList();
				
				for (IRPModelElement theCandidate : theCandidates) {				
					addElementIfItMatches(withMetaClass, theMatchingEls, theCandidate);
				}
			}
		}
		
		return theMatchingEls;
	}

	private static void addElementIfItMatches(
			String withMetaClass,
			Set<IRPModelElement> theMatchingEls, 
			IRPModelElement elementToAdd) {
		
		if (elementToAdd.getMetaClass().equals( withMetaClass )){
			
			if (elementToAdd instanceof IRPUnit){
				
				IRPUnit theUnit = (IRPUnit) elementToAdd;
				
				if (theUnit.isReadOnly()==0){
					theMatchingEls.add( elementToAdd );
				}
			} else {
				theMatchingEls.add( elementToAdd );
			}
		}
	}
	
	private static void removeElementFromDiagram(
			IRPModelElement theElementToRemove, 
			IRPDiagram fromDiagram){
		
		IRPCollection theGraphElsToDelete = FunctionalAnalysisPlugin.getRhapsodyApp().createNewCollection();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = fromDiagram.getGraphicalElements().toList();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			
			IRPModelElement theModelObject = theGraphEl.getModelObject();
				
			if (theModelObject.equals( theElementToRemove )){
				theGraphElsToDelete.addGraphicalItem( theGraphEl );
			}			
		}
		
		if (theGraphElsToDelete.getCount() != 0){
			fromDiagram.removeGraphElements( theGraphElsToDelete );
		} else {
			Logger.writeLine(theElementToRemove,"Error: Unable to remove from " + Logger.elementInfo(fromDiagram) + "as it doesn't exist");
		}
	}
	
	private static void updateVerificationsFor(IRPDiagram theDiagram){
		
		Set<IRPRequirement> theReqtsOnDiagram = buildSetOfRequirementsAlreadyOn(theDiagram);
		
		Set<IRPRequirement> theReqtsWithVerificationRelationsToDiagram = 
				TraceabilityHelper.getRequirementsThatTraceFromWithStereotype(
						theDiagram, "verify");
		
		Set<IRPRequirement> theRequirementsToRemove= new HashSet<IRPRequirement>( theReqtsWithVerificationRelationsToDiagram );
		theRequirementsToRemove.removeAll( theReqtsOnDiagram );
		
		if (!theRequirementsToRemove.isEmpty()){
			
			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theDiagram.getNestedElementsByMetaClass("Dependency", 0).toList();
			
			for (IRPDependency theDependency : theDependencies) {
				
				String userDefinedMetaClass = theDependency.getUserDefinedMetaClass();
				
				if (userDefinedMetaClass.equals("Verification")){
					
					IRPModelElement dependsOn = theDependency.getDependsOn();
					
					if (dependsOn instanceof IRPRequirement &&
							theRequirementsToRemove.contains(dependsOn)){
						
						Logger.writeLine(dependsOn, "removed verification link");
						
						theDependency.deleteFromProject();
					}
					
				}
			}
		}
		
		Set<IRPRequirement> theRequirementsToAdd = new HashSet<IRPRequirement>( theReqtsOnDiagram );
		theRequirementsToAdd.removeAll( theReqtsWithVerificationRelationsToDiagram );
		
		theDiagram.highLightElement();
		
		for (IRPRequirement theReq : theRequirementsToAdd) {
			IRPDependency theDep = theDiagram.addDependencyTo( theReq );
			theDep.changeTo("Verification");
			Logger.writeLine(theReq, "added verification link");
			theDep.highLightElement();
		}
	}
	
	private static Set<IRPRequirement> buildSetOfRequirementsAlreadyOn(IRPDiagram theDiagram){
		
		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			
			if (theGraphEl instanceof IRPGraphNode){
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				if (theModelObject instanceof IRPRequirement){
					
					IRPRequirement theReqt = (IRPRequirement)theModelObject;
					theReqts.add( theReqt );
				}
			}
		}
			
		return theReqts;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #013 10-MAY-2016: (new) Add support for sequence diagram req't and verification relation population (F.J.Chadburn)
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)

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

