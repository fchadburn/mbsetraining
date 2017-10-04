package requirementsanalysisplugin;

import java.util.ArrayList;
import java.util.List;
import com.telelogic.rhapsody.core.*;

import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

public class LayoutHelper {

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		if( theSelectedEl instanceof IRPDiagram ){
			
			centerDependenciesForTheDiagram( (IRPDiagram) theSelectedEl );
		}
	}
	
	private static void centerAll( 
			List<IRPGraphEdge> theGraphEdges ){
				
		IRPDiagram theDiagram = theGraphEdges.get(0).getDiagram();
		
		for( IRPGraphEdge theEdgeToRedraw : theGraphEdges ){
			
			IRPGraphElement theSourceGraphEl = theEdgeToRedraw.getSource();
			IRPGraphElement theTargetGraphEl = theEdgeToRedraw.getTarget();
			
			IRPModelElement theModelObject = theEdgeToRedraw.getModelObject();
			
			if( theSourceGraphEl != null && 
				theTargetGraphEl != null &&
				theModelObject != null &&
				theModelObject instanceof IRPDependency ){
				
				IRPCollection theCollection = 
						RequirementsAnalysisPlugin.getRhapsodyApp().createNewCollection();
				theCollection.addGraphicalItem( theEdgeToRedraw );
				theDiagram.removeGraphElements( theCollection );
				
				drawDependencyToMidPointsFor(
						(IRPDependency) theModelObject, 
						theSourceGraphEl, 
						theTargetGraphEl, 
						theDiagram );
			}
		}
	}
	
	private static List<IRPGraphEdge> getAllDependencyGraphEdges( 
			List<IRPGraphElement> inTheGraphEls ){
		
		List<IRPGraphEdge> theDependencyGraphEdges = new ArrayList<>();

		for( IRPGraphElement theGraphEl : inTheGraphEls ){

			if( theGraphEl instanceof IRPGraphEdge ){

				IRPModelElement theModelObject = theGraphEl.getModelObject();

				if( theModelObject != null && 
					theModelObject instanceof IRPDependency ){

					theDependencyGraphEdges.add( (IRPGraphEdge) theGraphEl );
				}
			}
		}
		
		return theDependencyGraphEdges;
	}
	
	public static void centerDependenciesForTheGraphEls( 
			List<IRPGraphElement> theGraphEls ){
		
		List<IRPGraphEdge> theEdgesToRedraw = 
				getAllDependencyGraphEdges( theGraphEls );
		
		boolean answer = UserInterfaceHelpers.askAQuestion( "There are x" + 
				theEdgesToRedraw.size() + " dependencies selected.\n" +
				"Do you want to recentre them?");
		
		if( answer==true ){
			
			centerAll( theEdgesToRedraw );
		}
	}
	
	public static void centerDependenciesForThePackage( 
			IRPPackage thePackage ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theADs = 
				thePackage.getNestedElementsByMetaClass( 
						"ActivityDiagramGE", 1 ).toList();
	
		for( IRPModelElement theAD : theADs ){
			centerDependenciesForTheDiagram( (IRPDiagram) theAD );
		}
	}
	
	public static void centerDependenciesForTheDiagram( 
			IRPDiagram theDiagram ){
				
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = 
				theDiagram.getGraphicalElements().toList();
		
		List<IRPGraphEdge> theEdgesToRedraw = 
				getAllDependencyGraphEdges( theGraphEls );
		
		if( theEdgesToRedraw.size()== 0 ){
			
			UserInterfaceHelpers.showInformationDialog(
					"There are no dependencies on the diagram" );
		
		} else {	
			
			String theDiagramName;
			
			if( theDiagram instanceof IRPActivityDiagram ){
				theDiagramName = Logger.elementInfo( theDiagram.getOwner() );
			} else {
				theDiagramName = Logger.elementInfo( theDiagram );
			}
			
			boolean answer = UserInterfaceHelpers.askAQuestion( 
					"There are x" + theEdgesToRedraw.size() + 
					" dependencies on the " + theDiagramName + ".\n" +
					"Do you want to recentre them?");
			
			if( answer==true ){
				centerAll( theEdgesToRedraw );
			}
		}
	}
	
	static public void drawDependencyToMidPointsFor(
			IRPDependency existingDependency, 
			IRPGraphElement theStartGraphEl,
			IRPGraphElement theEndGraphEl, 
			IRPDiagram theDiagram ){
		
		Logger.writeLine("drawDependencyToMidPointsFor invoked for " + Logger.elementInfo(existingDependency) +
				"between " + Logger.elementInfo( theStartGraphEl.getModelObject() ) + " and " +
				Logger.elementInfo( theEndGraphEl.getModelObject() ) + " on " + Logger.elementInfo(theDiagram));
		
		if( theStartGraphEl instanceof IRPGraphNode && 
			theEndGraphEl instanceof IRPGraphNode ){

			IRPGraphNode theStartNode = (IRPGraphNode)theStartGraphEl;
			IRPGraphNode theEndNode = (IRPGraphNode)theEndGraphEl;

			theDiagram.addNewEdgeForElement(
					existingDependency, 
					theStartNode, 
					GraphElInfo.getMidX( theStartNode ), 
					GraphElInfo.getMidY( theStartNode ), 
					theEndNode, 
					GraphElInfo.getMidX( theEndNode ), 
					GraphElInfo.getMidY( theEndNode ));

		} else if( theStartGraphEl instanceof IRPGraphEdge || 
				   theEndGraphEl instanceof IRPGraphEdge ){
			
			Logger.writeLine("Populating relations");
			
			try {
				IRPCollection theGraphEls = 
						RequirementsAnalysisPlugin.getRhapsodyApp().createNewCollection();

				theGraphEls.addGraphicalItem( theStartGraphEl );
				theGraphEls.addGraphicalItem( theEndGraphEl );
				
				theDiagram.completeRelations( theGraphEls, 0);	
			} catch (Exception e) {
				Logger.writeLine("Oops");
			}
			

			
		} else {
			Logger.writeLine("Warning in redrawDependencyToMidPointsFor, the graphEls are not handled types for drawing relations");
		}
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #229 20-SEP-2017: Add re-layout dependencies on diagram(s) menu to ease beautifying when req't tracing (F.J.Chadburn)
    #242 04-OCT-2017: Get re-layout dependencies on diagrams(s) menu to centre on graph edges properly (F.J.Chadburn)

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

