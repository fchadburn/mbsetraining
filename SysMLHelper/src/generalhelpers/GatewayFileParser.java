package generalhelpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayFileParser {

	File m_File = null;

	List<String> m_SectionNames;
	List<GatewayFileSection> m_SectionContents;

	GatewayFileParser(
			File theFile ) {
	
		m_File = theFile;
		m_SectionNames = new ArrayList<String>();
		m_SectionContents = new ArrayList<GatewayFileSection>();
		
		Scanner theScanner;
		
		try {
			Pattern theDocNamePattern = Pattern.compile("^\\[(.*)\\]");
			Pattern theNamesPattern = Pattern.compile("^Names=(.*)");
			Pattern theSettingPattern = Pattern.compile("^(.*)=(.*)");
			
			theScanner = new Scanner( m_File );
			
			GatewayFileSection theFileSection = null;
			
			while( theScanner.hasNextLine() ){
				
				String theLine = theScanner.nextLine();
				
				Matcher theDocNameMatcher = theDocNamePattern.matcher( theLine );
				Matcher theNamesMatcher = theNamesPattern.matcher( theLine );
				Matcher theSettingMatcher = theSettingPattern.matcher( theLine );
				
				if (theDocNameMatcher.find()){
					
					// save previous doc and create a new one
					if (theFileSection != null){
						m_SectionContents.add( theFileSection );
					}
					
					String theDocName = theDocNameMatcher.group( 1 );
					theFileSection = new GatewayFileSection( theDocName );
					
				} else if (theFileSection != null){
					
					if (theNamesMatcher.find()){
						
						String theNamesValue = theNamesMatcher.group( 1 );
						theFileSection.setValueFor("Names", theNamesValue);
						
						String theSubstring = theLine.substring(6, theLine.length());
						Logger.writeLine( theSubstring + " was found");
						
						String[] split = theSubstring.split(",");
						
						m_SectionNames = new ArrayList<String>(Arrays.asList( split ));
						
					} else if (theSettingMatcher.find()){
						
						String theSettingName = theSettingMatcher.group( 1 );
						String theSettingValue = theSettingMatcher.group( 2 );
						
						theFileSection.setValueFor( theSettingName, theSettingValue );
					}
				}
			}
			
			// save previous doc and create a new one
			if (theFileSection != null){
				m_SectionContents.add( theFileSection );
			}
			
			theScanner.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<GatewayFileSection> getAllTheFileSections() {
		return m_SectionContents;
	}
	
	public GatewayFileSection getFileSectionWith(
			String theName ){
		
		GatewayFileSection theDoc = null;
		
		for (GatewayFileSection gatewayDoc : m_SectionContents) {
			if (gatewayDoc.getSectionName().equals(theName)){
				theDoc = gatewayDoc;
				break;
			}
		}
		
		return theDoc;
	}
	
	public GatewayFileSection getFileSectionWithType(
			String theType ){
		
		GatewayFileSection theDoc = null;
		
		for (GatewayFileSection gatewayDoc : m_SectionContents) {
			
			String theValue = gatewayDoc.getValueFor("Type");
			
			if( theValue != null && theValue.equals( theType ) ){
				theDoc = gatewayDoc;
				break;
			}
		}

		return theDoc;
	}
	
	public boolean renameFileSection(
			String withTheName, 
			String toNewName ){

		boolean success = false;
		
		GatewayFileSection theSection = getFileSectionWith( withTheName );
		
		if( theSection != null ){
			theSection.setSectionName( toNewName );
			
			// update the Names variable in the Files section also, if one exists that is
			GatewayFileSection theFilesSection = getFileSectionWith( "Files" );
			
			if( theFilesSection != null ){
				String theOriginalNamesValue = theFilesSection.getValueFor( "Names" );
				String theUpdatedNamesValue = theOriginalNamesValue;	
				theUpdatedNamesValue.replaceAll( withTheName, toNewName );
				
    			// Change all the covers links
    			for( GatewayFileSection theFileSection : m_SectionContents ) {		
    				theFileSection.renameStringInAllValues( withTheName, toNewName );
    			}
			} else {
				Logger.writeLine("Error in renameFileSection, no 'Files' section was found when trying to change the Names value");
			}
			
			success = true;
		}
		
		return success;
	}
	
	public void replaceGatewayDoc(
			String withTheTypeOfAnalysis, 
			GatewayFileSection withReplacementDoc ){
		
		GatewayFileSection theExistingDoc = getFileSectionWithType( withTheTypeOfAnalysis );
		
		if (theExistingDoc != null){
			theExistingDoc.MergeInChangesIn( withReplacementDoc );
		}
		
		
	}

	public void dumpRqtfFileToOutputWindow() {

		Logger.writeLine("dumpRqtfFileToOutputWindow invoked");

		for (GatewayFileSection gatewayDoc : m_SectionContents) {

			List<String> theDocContents = gatewayDoc.getRqtfLinesForDocument();

			for (String theDocLine : theDocContents) {	

				Logger.writeLine("Contains:" + theDocLine );
			}
		}


	}
	
	public void writeGatewayFileTo(String theFileName) {
		try {
			Logger.writeLine("Building file called " + theFileName);
			
			PrintWriter printWriter;
			
			printWriter = new PrintWriter( theFileName );
			
			for (GatewayFileSection gatewayDoc : m_SectionContents) {
				
				List<String> theDocContents = gatewayDoc.getRqtfLinesForDocument();
				
				for (String theDocLine : theDocContents) {	
			        
					printWriter.println ( theDocLine );
					Logger.writeLine("Output:" + theDocLine );
				}
			}
			
			printWriter.close();
			
		} catch (FileNotFoundException e) {
			
			Logger.writeLine("Error in writeGatewayFileTo, unhandled FileNotFoundException detected");
		}
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #051 06-JUL-2016: Re-factored the GW panel to allow it to incrementally add to previous setup (F.J.Chadburn)
    #063 17-JUL-2016: Gateway project creator now mimics GatewayProjectFiles pkg creation if necessary (F.J.Chadburn)

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
