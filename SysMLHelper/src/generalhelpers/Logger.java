package generalhelpers;

import requirementsanalysisplugin.RequirementsAnalysisPlugin;

import com.telelogic.rhapsody.core.*;
 
public class Logger {
  
	public static void writeLine(String withMsg){
		 
		RequirementsAnalysisPlugin.getRhapsodyApp().writeToOutputWindow("SysMLHelper", withMsg + "\n");
	}
	
	public static void writeLine(IRPModelElement aboutTheEl, String withMsg){
		
		String elementInfo = "Error (Null Element)";
		
		if (aboutTheEl != null){
			elementInfo = elementInfo( aboutTheEl );
		} 
		
		RequirementsAnalysisPlugin.getRhapsodyApp().writeToOutputWindow("SysMLHelper", elementInfo + " " + withMsg + "\n");
	}
	
	public static String elementInfo(IRPModelElement forTheEl){
		
		String theInfo = "";
		
		if (forTheEl != null){
			theInfo = forTheEl.getMetaClass() + " called " + forTheEl.getName();
		}
		
		return theInfo;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
        
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

