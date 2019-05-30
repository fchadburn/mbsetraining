package generalhelpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPGeneralization;
import com.telelogic.rhapsody.core.IRPGraphElement;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPInstance;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPObjectModelDiagram;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPRelation;
import com.telelogic.rhapsody.core.IRPStructureDiagram;

import functionalanalysisplugin.FunctionalAnalysisPlugin;
import functionalanalysisplugin.GraphNodeInfo;

public class BlockDiagramHelper {

	public static void createBDDFor(
			IRPClassifier theAssemblyBlock, 
			IRPPackage underEl,
			String withName,
			String withNewTerm,
			Set<String> excludeMetaClasses ){
		
		IRPObjectModelDiagram theBDD = 
				(IRPObjectModelDiagram) underEl.addNewAggr(
						"ObjectModelDiagram", withName );
		
		theBDD.changeTo( withNewTerm );
		
		String[] theClassFormatComponent = theBDD.getPropertyValue("Format.Class.DefaultSize").split(",");	
		
		int theClassWidth = 400; //Integer.parseInt( theClassFormatComponent[2] );
		int theClassHeight = Integer.parseInt( theClassFormatComponent[3] );
		
		String[] theActorFormatComponent = theBDD.getPropertyValue("Format.Actor.DefaultSize").split(",");	
		
		int theActorWidth = Integer.parseInt( theActorFormatComponent[2] );
		int theActorHeight = Integer.parseInt( theActorFormatComponent[3] );
		
		IRPCollection theGraphElsToDraw = FunctionalAnalysisPlugin.getRhapsodyApp().createNewCollection();
		
		@SuppressWarnings("unchecked")
		List<IRPRelation> theRelations = theAssemblyBlock.getRelations().toList();
		
		Set<IRPClassifier> theActors = new HashSet<IRPClassifier>();
		Set<IRPClassifier> theBlocks = new HashSet<IRPClassifier>();
		
		boolean toggle = false;
		
		for( IRPRelation theRelation : theRelations ){
			IRPClassifier theOtherClass = theRelation.getOtherClass();
			
			// excluded list
			String theUserDefinedMetaClass = theOtherClass.getUserDefinedMetaClass();
			
			if( !excludeMetaClasses.contains( theUserDefinedMetaClass ) ){
				
				if( theOtherClass instanceof IRPActor ){
					theActors.add( theOtherClass );
				} else {
					theBlocks.add( theOtherClass );
				}
			}

		}
		
		int xPos = 30;
		int yPos = 40;
		int xGapActors = 50;
		int xGapBlocks = -150;
		int yGap = 70;
		int yOffset = 180;

		float theActorsWidth = (float) ((theActors.size()*(xGapActors+theActorWidth))/2.0);
		float theClassesWidth = (float) ((theBlocks.size()*(xGapBlocks+theClassWidth))/2.0);
		
		IRPGraphNode theAssemblyNode = theBDD.addNewNodeForElement( 
				theAssemblyBlock, xPos, yPos, (int) (theActorsWidth + theClassesWidth)*2, theClassHeight);
		
		theGraphElsToDraw.addGraphicalItem( theAssemblyNode );
		
		yPos = yPos + theClassHeight + yGap;

		for( IRPClassifier theActor : theActors ) {
			
			IRPGraphNode theNode;
			
			if( toggle ){
				theNode = theBDD.addNewNodeForElement(
						theActor, xPos, yPos, theActorWidth, theActorHeight);
			} else {
				theNode = theBDD.addNewNodeForElement(
						theActor, xPos, yPos+yOffset, theActorWidth, theActorHeight);
			}
			
			toggle = !toggle;
			
			theGraphElsToDraw.addGraphicalItem( theNode );
			xPos = xPos + theActorWidth + xGapActors;
		}
		
		for( IRPClassifier theBlock : theBlocks ) {
			
			IRPGraphNode theNode;
			
			if( toggle ){
				theNode = theBDD.addNewNodeForElement(
						theBlock, xPos, yPos, theClassWidth, theClassHeight );
			} else {
				theNode = theBDD.addNewNodeForElement(
						theBlock, xPos, yPos+yOffset, theClassWidth, theClassHeight );
			}
			
			toggle = !toggle;
			
			theGraphElsToDraw.addGraphicalItem( theNode );
			xPos = xPos + theClassWidth + xGapBlocks;
		}
		
		IRPGraphElement theLastEl = 
				(IRPGraphElement) theGraphElsToDraw.getItem( theGraphElsToDraw.getCount() );
		
		int maxX = 1000;
		
		if( theLastEl != null && theLastEl instanceof IRPGraphNode ){
			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theLastEl );
			maxX = theNodeInfo.getBottomRightX();
		}

		Set<IRPClassifier> theBaseClassifiers = getBaseClassesOf( theActors );
		theBaseClassifiers.addAll( getBaseClassesOf( theBlocks ) );
		
		if( !theBaseClassifiers.isEmpty() ){
			
			int xGap = (maxX-30)/theBaseClassifiers.size();
			xPos = 30 + xGap/2;
			
			if( toggle ){
				yPos = yPos + theClassHeight + yGap*2;
			} else {
				yPos = yPos + (theClassHeight + yGap*2) + yOffset;
			}
			
			for( IRPClassifier theBaseClassifier : theBaseClassifiers ) {
				

				IRPGraphNode theNode;
				
				theNode = theBDD.addNewNodeForElement(
						theBaseClassifier, xPos, yPos, theActorWidth, theActorHeight);

				theGraphElsToDraw.addGraphicalItem( theNode );
				xPos = xPos + xGap;
			}
		}
		
		theBDD.completeRelations( theGraphElsToDraw, 1 );
	}
	
	public static void createIBDFor(
			IRPClass theAssemblyBlock, 
			String withName,
			String withNewTerm ){
		
		IRPStructureDiagram theIBD = 
				(IRPStructureDiagram) theAssemblyBlock.addNewAggr(
						"StructureDiagram", withName );
		
		theIBD.changeTo( withNewTerm );
		
		IRPCollection theGraphElsToDraw = 
				FunctionalAnalysisPlugin.getRhapsodyApp().createNewCollection();
		
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts =
		    theAssemblyBlock.getNestedElementsByMetaClass("Part", 0).toList();

		int countTestDrivers = 0;
		int countActors = 0;
		int countBlocks = 0;
		int maxCount = 0;
		
		// Count actors vs. internal parts vs test drivers
		for( IRPInstance thePart : theParts ) {

			IRPClassifier theType = thePart.getOtherClass();
	
			if( GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) ){
				countTestDrivers++;
			} else if( theType instanceof IRPActor ){
				countActors++;
			} else {
				countBlocks++;
			}
		}
		
		if( countBlocks > countActors ){
			maxCount = countBlocks;
		} else {
			maxCount = countActors;
		}
		
		int xPos = 30;
		int yPos = 40;
		int nWidth = 400;
		int nHeight = 120;
		int xGap = 30;
		int yGap = 200;

		float xMiddle = (float) ((float) ((float)(maxCount*(xGap+nWidth))/2.0)+(xGap/2.0));
		
		xPos = (int) (xMiddle - nWidth/2);
		
		if( countTestDrivers>0 ){
			// Do Test Driver first
			for( IRPInstance thePart : theParts ) {

				if( GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) ){
					IRPGraphNode theNode = theIBD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					theGraphElsToDraw.addGraphicalItem( theNode );
					xPos = xPos + nWidth + xGap;
				}
			}

			xPos = 30;
			yPos = yPos + yGap;
		}

		if( countActors>0 ){
			// Now do actors
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();
				
				if( theType instanceof IRPActor ){			
					IRPGraphNode theNode = theIBD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					theGraphElsToDraw.addGraphicalItem( theNode );
					xPos = xPos + nWidth + xGap;
				}
			}
		
			xPos = 30 + (maxCount/2)*(nWidth+xGap);	
			
		}

		xPos = (int) (xMiddle - nWidth/2);
		yPos = yPos + yGap;
		
		if( countBlocks>0 ){
			// Do normal blocks last
			for( IRPInstance thePart : theParts ) {

				IRPClassifier theType = thePart.getOtherClass();
				
				if( !GeneralHelpers.hasStereotypeCalled( "TestDriver", thePart ) && !( theType instanceof IRPActor )){	
					theIBD.addNewNodeForElement( thePart, xPos, yPos, nWidth, nHeight );
					xPos = xPos + nWidth + xGap;
				}
			}
		}
		
		theIBD.completeRelations( theGraphElsToDraw, 1 );
	}
	
	private static Set<IRPClassifier> getBaseClassesOf( 
			Set<IRPClassifier> theClassifiers ){
		
		Set<IRPClassifier> theBaseClasses = new HashSet<IRPClassifier>();
		
		for (IRPModelElement theEl : theClassifiers ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theGeneralizations = 
					theEl.getNestedElementsByMetaClass("Generalization", 0).toList();
			
			for (IRPModelElement theGenEl : theGeneralizations) {
				IRPGeneralization theGeneralization = (IRPGeneralization)theGenEl;
				
				IRPClassifier theBaseClass = theGeneralization.getBaseClass();
				theBaseClasses.add( theBaseClass );
			}
		}
		
		return theBaseClasses;
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #250 29-MAY-2019: First official version of new FunctionalDesignProfile  (F.J.Chadburn)

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