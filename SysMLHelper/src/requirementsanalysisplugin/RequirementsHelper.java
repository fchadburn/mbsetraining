package requirementsanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.telelogic.rhapsody.core.*;
   
public class RequirementsHelper {
 	
	private static List<IRPModelElement> getElementsThatFlowInto(
			IRPModelElement theElement, 
			IRPDiagram onTheDiagram){
		
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
						} else { // theGuard==null
							
							//  does the transition come from an event?
							IRPModelElement theSource = theTrans.getItsSource();
							
							if( theSource instanceof IRPAcceptEventAction ){
								theElementsFound.add( theSource );
							}
						}
					}				
				}				
			}
		}
		
		return theElementsFound;	
	}

	private static void createNewRequirementFor(
			IRPGraphElement theGraphEl ){
		
		IRPModelElement theModelObject = theGraphEl.getModelObject();
		IRPDiagram theDiagram = theGraphEl.getDiagram();
		
		if( theModelObject != null ){
			
			String theActionText = GeneralHelpers.getActionTextFrom( theModelObject );
			
			if( theActionText != null ){
				List<IRPModelElement> theRelations = getElementsThatFlowInto( theModelObject, theDiagram );

				String theText = null;
				
				if( theRelations.isEmpty() ){
						
					theText = getCreateRequirementTextForPrefixing( theModelObject.getProject() ) + theActionText;				
					
				} else {
					
					theText = "When ";
					
					Iterator<IRPModelElement> theRelatedModelElIter = theRelations.iterator();
					
					while( theRelatedModelElIter.hasNext() ) {
						
						IRPModelElement theRelatedModelEl = theRelatedModelElIter.next();
						
						if( theRelatedModelEl instanceof IRPTransition ){
							IRPTransition theTransition = (IRPTransition)theRelatedModelEl;
							String theGuardBody = theTransition.getItsGuard().getBody();
							
							theText+= theGuardBody;
						
						} else if( theRelatedModelEl instanceof IRPAcceptEventAction ){
							
							theText+= GeneralHelpers.decapitalize( 
								GeneralHelpers.getActionTextFrom( theRelatedModelEl ) );
						}
						
						if( theRelatedModelElIter.hasNext() ){
							theText+= " or ";
						}		
					}
					
					theText += " the feature shall " + theActionText;
				}
				
				IRPModelElement theReqtOwner = theDiagram;

				if( theReqtOwner instanceof IRPActivityDiagram ){
					theReqtOwner = theDiagram.getOwner();
				}

				IRPDependency theDependency = 
						addNewRequirementTracedTo( theModelObject, theReqtOwner, theText );	

				IRPRequirement theReqt = (IRPRequirement) theDependency.getDependsOn();

				int x = GraphElInfo.getMidX( theGraphEl );
				int y = GraphElInfo.getMidY( theGraphEl );

				IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement(
						theReqt, x+100, y+70, 300, 100 );

				if( theGraphEl instanceof IRPGraphNode ){

					IRPGraphNode theStartNode = (IRPGraphNode)theGraphEl;

					theDiagram.addNewEdgeForElement(
							theDependency, 
							theStartNode, 
							x, 
							y, 
							theGraphNode, 
							GraphElInfo.getMidX( theGraphNode ), 
							GraphElInfo.getMidY( theGraphNode ));

				} else if( theGraphEl instanceof IRPGraphEdge ){

					IRPCollection theGraphEls = 
							RequirementsAnalysisPlugin.getRhapsodyApp().createNewCollection();

					theGraphEls.addGraphicalItem( theGraphEl );
					theGraphEls.addGraphicalItem( theGraphNode );

					theDiagram.completeRelations( theGraphEls, 0);	

				} else {
					Logger.writeLine("Warning in populateDependencyOnDiagram, the graphEls are not handled types for drawing relations");
				}

			} // theActionText == null
		} else { // theModelObject == null
			Logger.writeLine("theModelObject == null");
		}
	}

	private static IRPDependency addNewRequirementTracedTo(
			IRPModelElement theModelObject, 
			IRPModelElement toOwner,
			String theText) {
		
		IRPRequirement theReqt = (IRPRequirement) toOwner.addNewAggr("Requirement", "");
		theReqt.setSpecification(theText);
		theReqt.highLightElement();	

		IRPDependency theDep = theModelObject.addDependencyTo( theReqt );

		IRPStereotype theDependencyStereotype = GeneralHelpers.getStereotypeIn( 
				toOwner.getProject(), "traceabilityTypeToUseForActions", "RequirementsAnalysisPkg" );
		
		if( theDependencyStereotype != null ){
			
			theDep.addSpecificStereotype( theDependencyStereotype );
		} else {
			theDep.addStereotype("derive", "Dependency");				
		}
		
		Logger.writeLine("Created a Requirement called " + theReqt.getName() + 
				" with the text '" + theText + "' related to " + 
				Logger.elementInfo(theModelObject) + " with a " + Logger.elementInfo(theDep));
		
		return theDep;
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
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
    #067 19-JUL-2016: Improvement to forming Event/Guard+Action text when creating new requirements (F.J.Chadburn) 
    #072 25-JUL-2016: Improved robustness when graphEls that don't have model elements are selected (F.J.Chadburn)
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    #170 08-MAR-2017: Tweak to Add new requirement on ADs to add to same owner as user created (F.J.Chadburn)

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

