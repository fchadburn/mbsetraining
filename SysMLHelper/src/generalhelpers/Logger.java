package generalhelpers;

import requirementsanalysisplugin.RequirementsAnalysisPlugin;

import com.telelogic.rhapsody.core.*;
 
public class Logger {
	
	public enum LoggerLevel {
		Error,
		Warning,
		Info
	}
	
	public static void writeLine( String withMsg ){
		 
		RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow(
				"SysMLHelper", withMsg + "\n" );
	}
	
	public static void info( String withMsg ){
		 
//		System.out.println( withMsg );
		
		RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow(
				"SysMLHelper", withMsg + "\n" );
	}
	
	public static void warning( String withMsg ){
		 
		RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow(
				"SysMLHelper", "Warning: " + withMsg + "\n" );
	}
	
	public static void error( String withMsg ){
		 
		RhapsodyAppServer.getActiveRhapsodyApplication().writeToOutputWindow(
				"SysMLHelper", "Error: " + withMsg + "\n" );
	}
	
	public static void writeLine(IRPModelElement aboutTheEl, String withMsg){
		
		String elementInfo = "Error (Null Element)";
		
		if (aboutTheEl != null){
			elementInfo = elementInfo( aboutTheEl );
		} 
		
		RequirementsAnalysisPlugin.getRhapsodyApp().writeToOutputWindow("SysMLHelper", elementInfo + " " + withMsg + "\n");
	}
	
	public static String elementInfo(IRPModelElement forTheEl){
		
		String theInfo = "Error (Null Element)";
		
		if (forTheEl != null){
			theInfo = forTheEl.getUserDefinedMetaClass() + " called " + forTheEl.getName();
		}
		
		return theInfo;
	}
}

/**
 * Copyright (C) 2016=2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
    #060 13-JUL-2016: Changed elementInfo to return user defined meta-class for log entries (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    
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