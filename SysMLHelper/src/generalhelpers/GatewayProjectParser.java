package generalhelpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayProjectParser {

	File m_File = null;

	List<String> m_ProjectNames;
	List<GatewayDoc> m_GatewayDocs;

	GatewayProjectParser(
			File theFile ) {
	
		m_File = theFile;
		m_ProjectNames = new ArrayList<String>();
		m_GatewayDocs = new ArrayList<GatewayDoc>();
		
		Scanner theScanner;
		
		try {
			Pattern theDocNamePattern = Pattern.compile("^\\[(.*)\\]");
			Pattern theNamesPattern = Pattern.compile("^Names=(.*)");
			Pattern theSettingPattern = Pattern.compile("^(.*)=(.*)");
			
			theScanner = new Scanner( m_File );
			
			GatewayDoc theGatewayDoc = null;
			
			while( theScanner.hasNextLine() ){
				
				String theLine = theScanner.nextLine();
				
				Matcher theDocNameMatcher = theDocNamePattern.matcher( theLine );
				Matcher theNamesMatcher = theNamesPattern.matcher( theLine );
				Matcher theSettingMatcher = theSettingPattern.matcher( theLine );
				
				if (theDocNameMatcher.find()){
					
					// save previous doc and create a new one
					if (theGatewayDoc != null){
						m_GatewayDocs.add( theGatewayDoc );
					}
					
					String theDocName = theDocNameMatcher.group( 1 );
					theGatewayDoc = new GatewayDoc( theDocName );
					
				} else if (theGatewayDoc != null){
					
					if (theNamesMatcher.find()){
						
						String theNamesValue = theNamesMatcher.group( 1 );
						theGatewayDoc.setValueFor("Names", theNamesValue);
						
						String theSubstring = theLine.substring(6, theLine.length());
						Logger.writeLine( theSubstring + " was found");
						
						String[] split = theSubstring.split(",");
						
						m_ProjectNames = new ArrayList<String>(Arrays.asList( split ));
						
					} else if (theSettingMatcher.find()){
						
						String theSettingName = theSettingMatcher.group( 1 );
						String theSettingValue = theSettingMatcher.group( 2 );
						
						theGatewayDoc.setValueFor( theSettingName, theSettingValue );
					}
				} else {
					Logger.writeLine("Error in GatewayProjectParser, theGatewayDoc is unexpectedly null");
				}
			}
			
			// save previous doc and create a new one
			if (theGatewayDoc != null){
				m_GatewayDocs.add( theGatewayDoc );
			}
			
			theScanner.close();
			
			for (GatewayDoc theDoc : m_GatewayDocs) {
				Logger.writeLine(
						"Document called " + theDoc.getDocumentName() + " has analysis type '" + 
						theDoc.getValueFor("Type") + "' and the path '" + theDoc.getValueFor("Path") + "'" );
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

	public List<GatewayDoc> getGatewayDocs() {
		return m_GatewayDocs;
	}
	
	public GatewayDoc getGatewayDocWith(
			String theName){
		
		GatewayDoc theDoc = null;
		
		for (GatewayDoc gatewayDoc : m_GatewayDocs) {
			if (gatewayDoc.getDocumentName().equals(theName)){
				theDoc = gatewayDoc;
				break;
			}
		}
		return theDoc;
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
