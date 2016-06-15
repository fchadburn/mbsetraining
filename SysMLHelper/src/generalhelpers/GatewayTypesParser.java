package generalhelpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GatewayTypesParser {

	File m_File = null;
	String[] m_AnalysisTypes = null;
	
	public GatewayTypesParser(
			File theFile ) {
		
		m_File = theFile;
		
		try {
			Scanner theScanner = new Scanner( m_File );
			
			while( theScanner.hasNextLine() ){
				
				String theLine = theScanner.nextLine();
			
				String preFix = "Names=";
				
				if (theLine.startsWith( preFix )){
					String theSubstring = theLine.substring(6, theLine.length());
					Logger.writeLine( theSubstring + " was found");
					
					m_AnalysisTypes = theSubstring.split(",");
				}
			}
			
			theScanner.close();
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
	}
	
	public String[] getAnalysisTypes(){
		
		return m_AnalysisTypes;
	}
	
	public File getFile(){
		return m_File;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)

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
