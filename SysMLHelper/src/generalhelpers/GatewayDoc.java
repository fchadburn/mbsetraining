package generalhelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class GatewayDoc {

	private String m_DocumentName = null;
	
	LinkedHashMap<String, String> m_SettingsMap = new LinkedHashMap<String, String>();
	
	public GatewayDoc(
			String theDocumentName ) {
		
		super();
		this.m_DocumentName = theDocumentName;
	}
	
	private String getVariableXKey(
			String forVariableXName){
		
		String theVariableXKey = null;
		
        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while( theIterator.hasNext() ) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           String theFullKey = theEntry.getKey();
           String theFullValue = theEntry.getValue();
           
           if (theFullValue.equals( forVariableXName )){
        	   
        	   // take Name off string and replace with Value
        	   theVariableXKey = theFullKey.substring(0, theFullKey.length()-4) + "Value";
        	   break;
           }   
        }
        
        return theVariableXKey;
	}
	
	public String getVariableXValue(
			String forVariableXName){
		
		String theValue = null;
		String theKey = getVariableXKey( forVariableXName );
	
        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while( theIterator.hasNext() ) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           String theFullKey = theEntry.getKey();
           String theFullValue = theEntry.getValue();
           
           if (theFullKey.equals( theKey )){
        	   theValue=theFullValue;
           }
        }
        
        return theValue;
	}
	
	public void setVariableXValue(
			String forVariableXName, String toNewValue){
		
		String theKey = getVariableXKey( forVariableXName );
		
		if (theKey != null){
			setValueFor( theKey, toNewValue);
		} else {
			Logger.writeLine("Error in setVariableXValue, " + forVariableXName + " was not found");
		}
	}
	
	public String getValueFor(
			String theSettingName){
		
		String theValue = m_SettingsMap.get( theSettingName );
		return theValue;
	}
	
	public void setValueFor(
			String theSettingName, 
			String toValue){	
		
		m_SettingsMap.put( theSettingName, toValue );
	}
	
	public String getDocumentName(){
		return m_DocumentName;
	}
	
	public void setDocumentName(String theNewName){
		m_DocumentName = theNewName;
	}
	
	public List<String> getRqtfLinesForDocument(){
		
		List<String> theFullDoc = new ArrayList<String>();

		theFullDoc.add( "[" + getDocumentName() + "]" );
		
        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while(theIterator.hasNext()) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           theFullDoc.add( theEntry.getKey() + "=" + theEntry.getValue() );
        }
        
        theFullDoc.add("");
		
		return theFullDoc;
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