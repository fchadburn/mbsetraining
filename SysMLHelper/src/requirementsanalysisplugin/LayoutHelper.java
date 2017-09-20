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
		
		for( IRPGraphEdge theEdgeToRedraw : theGraphEdges ){
			
			IRPGraphElement theSourceGraphEl = theEdgeToRedraw.getSource();
			IRPGraphElement theTargetGraphEl = theEdgeToRedraw.getTarget();
			
			int startX = GraphElInfo.getMidX( theSourceGraphEl );
			int startY = GraphElInfo.getMidY( theSourceGraphEl );
			int endX = GraphElInfo.getMidX( theTargetGraphEl );
			int endY = GraphElInfo.getMidY( theTargetGraphEl );				
			
			theEdgeToRedraw.setGraphicalProperty( "SourcePosition", startX + "," + startY );
			theEdgeToRedraw.setGraphicalProperty( "TargetPosition", endX + "," + endY );
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
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #229 20-SEP-2017: Add re-layout dependencies on diagram(s) menu to ease beautifying when req't tracing (F.J.Chadburn)

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

