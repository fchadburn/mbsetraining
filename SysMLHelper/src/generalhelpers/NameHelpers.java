package generalhelpers;

import java.util.Iterator;
import java.util.List;
import com.telelogic.rhapsody.core.*;
 
public class NameHelpers {
 
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theRhpApp.getSelectedGraphElements().toList();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			dumpGraphicalPropertiesFor(theGraphEl);
		}
	}
	
	public static String buildStringFromModelEls(
			List<? extends IRPModelElement> theEls,
			int max ){
		
		String theString = "";

		int count = 0;
		
		for( Iterator<? extends IRPModelElement> iterator = theEls.iterator(); iterator.hasNext(); ) {
			
			count++;
			IRPModelElement theEl = (IRPModelElement) iterator.next();

			theString += count + ". " + Logger.elementInfo( theEl ) + " \n";
			
			if( count >= max ){
				theString += "... (" + theEls.size() + " in list) \n";
				break;
			}
		}
		
		return theString;
	}
	
	public static String buildStringFrom(
			List<String> theList, 
			int max ){
		
		String theString = "";
		
		int count = 0;
		
		for( Iterator<String> iterator = theList.iterator(); iterator.hasNext(); ) {
			
			count++;
			String string = (String) iterator.next();

			theString += string + " \n";
			
			if( count >= max ){
				theString += "... (" + theList.size() + ") \n";
				break;
			}
		}
		
		return theString;
	}
	
	public static void dumpGraphicalPropertiesFor(
			IRPGraphElement theGraphEl){
	 
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		Logger.writeLine("---------------------------");
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			
			Logger.writeLine(theGraphicalProperty.getKey() + "=" + theGraphicalProperty.getValue());
		}
		Logger.writeLine("---------------------------"); 
	}
		
	public static String decapitalize(final String line){
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toLowerCase(line.charAt(0)) + line.substring(1);
		} else {
			theResult = line;
		}
		
		return theResult;	
	}
	
	public static String capitalize(final String line) {
		
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toUpperCase(line.charAt(0)) + line.substring(1);
		} else {
			Logger.writeLine("Error in capitalize");
			theResult = line;
		}
		
		return theResult;
	}
		
	public static boolean isElementNameUnique(
			String theProposedName, 
			String ofMetaClass, 
			IRPModelElement underneathTheEl,
			int recursive){
				
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theExistingEls = 
				underneathTheEl.getNestedElementsByMetaClass(ofMetaClass, recursive).toList();
		
		for (IRPModelElement theExistingEl : theExistingEls) {
			
			if (theExistingEl.getName().equals(theProposedName)){
				count++;
				break;
			}
		}
		
		if (count > 1){
			Logger.writeLine("Warning in isElementNameUnique, there are " + count + " elements called " + 
					theProposedName + " of type " + ofMetaClass + " in the project. This may cause issues.");
		}
				
		boolean isUnique = (count == 0);

		return isUnique;
	}
	
	public static String determineUniqueNameBasedOn(
			String theProposedName,
			String ofMetaClass,
			IRPModelElement underElement){
		
		int count = 0;
		
		String theUniqueName = theProposedName;
		
		while( !isElementNameUnique(
				theUniqueName, ofMetaClass, underElement, 1 ) ){
			
			count++;
			theUniqueName = theProposedName + count;
		}
		
		return theUniqueName;
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
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