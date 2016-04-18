package requirementsanalysisplugin;

import generalhelpers.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.telelogic.rhapsody.core.*;
   
public class RequirementsHelper {
	
	private static List<IRPModelElement> getElementsThatFlowInto(IRPModelElement theElement, IRPDiagram onTheDiagram){
		
		List<IRPModelElement> theElementsFound = new ArrayList<IRPModelElement>();
		 
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = onTheDiagram.getGraphicalElements().toList();
		
		for (IRPGraphElement irpGraphElement : theGraphEls) {
			
			if (irpGraphElement instanceof IRPGraphEdge){
				
				IRPModelElement theModelEl = (IRPModelElement) irpGraphElement.getModelObject();
				
				if (theModelEl instanceof IRPTransition){
					IRPTransition theTrans = (IRPTransition)theModelEl;
					IRPModelElement theTarget = theTrans.getItsTarget();
					
					if (theTarget != null && theTarget.getGUID().equals(theElement.getGUID())){
						
						IRPGuard theGuard = theTrans.getItsGuard();
						
						if (theGuard!=null){
							String theBody = theGuard.getBody();
							
							if (!theBody.isEmpty()){
								theElementsFound.add(theModelEl);
							}
						}
					}				
				}				
			}
		}
		
		return theElementsFound;	
	}

	private static void createNewRequirementFor(IRPGraphElement theGraphEl){
		
		IRPModelElement theModelObject = theGraphEl.getModelObject();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		IRPCollection theCollection = RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();

		IRPRequirement theReqt = null;
		
		if (theModelObject != null){
			
			List<IRPModelElement> theRelations = getElementsThatFlowInto( theModelObject, theDiagram);
			
			String theText = getCreateRequirementTextForPrefixing(theModelObject.getProject());
			
			if (!theRelations.isEmpty()){
				
				theText+= "when ";
				
				Iterator<IRPModelElement> theModelElIter = theRelations.iterator();
				while (theModelElIter.hasNext()) {
					
					IRPModelElement theModelEl = theModelElIter.next();
					
					if (theModelEl instanceof IRPTransition){
						IRPTransition theTransition = (IRPTransition)theModelEl;
						String theGuardBody = theTransition.getItsGuard().getBody();
						
						theText+= theGuardBody;
					}
					
					if (theModelElIter.hasNext()){
						theText+= " or ";
					}		
				}
				
				theText += " ";
			}
			
			String theActionText = GeneralHelpers.decapitalize( GeneralHelpers.getActionTextFrom(theModelObject) );
			
			if (theActionText.isEmpty()){
				Logger.writeLine("Warning, " + Logger.elementInfo( theModelObject ) + " has no text");
			}
			
			theText+= theActionText;
			theReqt = (IRPRequirement) theDiagram.addNewAggr("Requirement", "");
			theReqt.setSpecification(theText);
			theReqt.highLightElement();
			
			Logger.writeLine("Created a requirement called " + theReqt.getName() + " with the text '" + theText + "'");

			IRPDependency theDep = theModelObject.addDependencyTo(theReqt);

			IRPStereotype theDependencyStereotype = getStereotypeForActionTracing(theDiagram.getProject());
			
			if (theDependencyStereotype != null){
				Logger.writeLine(Logger.elementInfo(theDependencyStereotype) + " was found");
				theDep.addSpecificStereotype(theDependencyStereotype);
			} else {
				theDep.addStereotype("derive", "Dependency");				
			}			
		}
		
		IRPGraphicalProperty theGraphicalProperty = null;
		
		if (theGraphEl instanceof IRPGraphNode){
			theGraphicalProperty = theGraphEl.getGraphicalProperty("Position");
		} else if (theGraphEl instanceof IRPGraphEdge){
			theGraphicalProperty = theGraphEl.getGraphicalProperty("TargetPosition");
		}
		
		if (theGraphicalProperty != null){
			String theValue = theGraphicalProperty.getValue();
			String[] thePosition = theValue.split(",");

			int x = Integer.parseInt(thePosition[0]);
			int y = Integer.parseInt(thePosition[1]);

			IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement(theReqt, x+100, y+150, 300, 100);

			theCollection.addGraphicalItem(theGraphEl);
			theCollection.addGraphicalItem(theGraphNode);

			theDiagram.completeRelations(theCollection, 0);
		}	
	}
			
	public static void createNewRequirementsFor(List<IRPGraphElement> theGraphEls){
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			
			createNewRequirementFor( theGraphEl );
		}
	}
	
	private static String getCreateRequirementTextForPrefixing(IRPProject inTheProject){
		
		String theText = "The feature shall ";
		
		String thePackageName = "RequirementsAnalysisPkg";
		final String tagName = "createRequirementTextForPrefixing";	
		
		IRPModelElement theReqtsAnalysisPkg = inTheProject.findElementsByFullName(thePackageName, "Package");
		
		if (theReqtsAnalysisPkg==null){
			Logger.writeLine("Error in getCreateRequirementTextForPrefixing, no " + thePackageName + " was found");
			
		} else {
			
			IRPTag theTag = theReqtsAnalysisPkg.getTag( tagName );
			
			if (theTag != null){
				theText = theTag.getValue();
				
				//#005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
				theText = theText.replaceAll("ProjectName", inTheProject.getName());
			} else {
				Logger.writeLine("Warning in getCreateRequirementTextForPrefixing, no tag called " + tagName + " was found so creating one");	
				IRPTag theNewTag = (IRPTag) theReqtsAnalysisPkg.addNewAggr("Tag", tagName);
				theReqtsAnalysisPkg.setTagValue(theNewTag, theText);
			}
		}
		
		return theText;
	}
	
	public static IRPStereotype getStereotypeForActionTracing(IRPProject inTheProject){
		
		IRPStereotype theStereotype = null;
		
		String thePackageName = "RequirementsAnalysisPkg";
		final String tagNameForDependency = "traceabilityTypeToUseForActions";	
		
		IRPModelElement theReqtsAnalysisPkg = inTheProject.findElementsByFullName(thePackageName, "Package");
		
		if (theReqtsAnalysisPkg==null){
			Logger.writeLine("Error in getStereotypeForActionTracing, no " + thePackageName + " was found");
			
		} else {
			
			IRPTag theTag = theReqtsAnalysisPkg.getTag( tagNameForDependency );
			
			if (theTag == null){
				Logger.writeLine("Warning in getStereotypeForActionTracing, no tag called " + tagNameForDependency + " was found");				
				
				theTag = (IRPTag) theReqtsAnalysisPkg.addNewAggr("Tag", tagNameForDependency);
				theStereotype = selectAndPersistStereotypeToUseForActionTracing(inTheProject, theReqtsAnalysisPkg, theTag);
				
			} else { // tag is not null
				
				String theValue = theTag.getValue();
				
				Logger.writeLine("Read value of " + theValue + " from " + Logger.elementInfo(theTag));
				
				IRPModelElement theModelElement = GeneralHelpers.findElementWithMetaClassAndName("Stereotype", theValue, inTheProject);
				
				if (theModelElement==null){
					Logger.writeLine("Error in getStereotypeForActionTracing, no Stereotyped called " + theValue + " was found");

					theStereotype = selectAndPersistStereotypeToUseForActionTracing(inTheProject, theReqtsAnalysisPkg, theTag);

				} else if (theModelElement instanceof IRPStereotype){
					
					theStereotype = (IRPStereotype)theModelElement;
					
					Logger.writeLine("Using " + Logger.elementInfo(theStereotype) + " for action tracing");
				}
			}
		}
		
		return theStereotype;
	}

	private static IRPStereotype selectAndPersistStereotypeToUseForActionTracing(
			IRPProject inTheProject, IRPModelElement theReqtsAnalysisPkg, IRPTag theTag) {

		IRPStereotype theStereotype = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theStereotypes = inTheProject.getNestedElementsByMetaClass("Stereotype", 1).toList();

		if (theStereotypes.isEmpty()){
			Logger.writeLine("Error in getStereotypeForActionTracing, there are no stereotypes in project");
		} else {
			IRPModelElement theSelectedEl = GeneralHelpers.launchDialogToSelectElement(theStereotypes, "Pick a stereotype for action tracing", true);

			if (theSelectedEl != null && theSelectedEl instanceof IRPStereotype){
				
				theReqtsAnalysisPkg.setTagValue(theTag, theSelectedEl.getName());
				theStereotype = (IRPStereotype)theSelectedEl;
			}
		}

		return theStereotype;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
        
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
