package requirementsanalysisplugin;

import generalhelpers.Logger;

import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class NestedActivityDiagram {
 
	public static void createNestedActivityDiagramsFor(List<IRPModelElement> theElements){
		 
		for (IRPModelElement theElement : theElements) {
			
			if (theElement instanceof IRPUseCase){
				Logger.writeLine("Creating a nested Activity Diagram underneath " + Logger.elementInfo(theElement));
				createNestedActivityDiagram( (IRPUseCase)theElement, "AD - " + theElement.getName() );
			} 
		}
	}
	
	public static void createNestedActivityDiagram(
			IRPUseCase forUseCase, String withUnadornedName ){
		
		String theName = withUnadornedName;
		 
		// check if existing AD with same name
		IRPFlowchart theAD = (IRPFlowchart) forUseCase.findNestedElement( theName , "ActivityDiagram");
		int count = 0;
		
		while (theAD != null){
			
			Logger.writeLine(forUseCase, "already has a nested activity diagram called " + theName);
			count++;
			theName = withUnadornedName + " " + count;
			theAD = (IRPFlowchart) forUseCase.findNestedElement( theName , "ActivityDiagram");
		}
		
		IRPModelElement theTemplate = forUseCase.getProject().findNestedElementRecursive("template_for_act", "ActivityDiagram");
		
		if (theTemplate != null){
			Logger.writeLine("Found template for " + Logger.elementInfo(theTemplate));
			IRPFlowchart theFlowchart = (IRPFlowchart) theTemplate.clone(theName, forUseCase);
			theFlowchart.highLightElement();
			Logger.writeLine(theFlowchart, "was created under " + Logger.elementInfo( theFlowchart.getOwner() ) );
			
			IRPStatechartDiagram theStatechart = theFlowchart.getStatechartDiagram();
			theStatechart.createGraphics();
			theStatechart.openDiagram();
			theFlowchart.setAsMainBehavior();
		} else {
			Logger.writeLine("Error, Could not find template");
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #080 28-JUL-2016: Added activity diagram name to the create AD dialog for use cases (F.J.Chadburn)
        
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

