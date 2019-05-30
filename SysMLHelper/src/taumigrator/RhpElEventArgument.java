package taumigrator;

import com.telelogic.rhapsody.core.IRPModelElement;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElEventArgument extends RhpElElement {

	public RhpElEventArgument(
			String theElementName, 
			String theElementType,
			String theElementGuid ) throws Exception{
		
		super(theElementName, theElementType, theElementGuid);
		
		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Create " + this.getString() + "\n";
		theMsg += "===================================\n";		
		Logger.info( theMsg );
	}

	public RhpElEventArgument(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent ) throws Exception {
		
		super(theElementName, theElementType, theElementGuid, theParent);
		
		dumpInfo();
	}
	
	@Override
	public IRPModelElement createRhpEl(
			RhpEl treeRoot ) throws Exception {

		Logger.writeLine("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		String theLegalName = GeneralHelpers.makeLegalName( _elementName );
		
		if( _elementName != theLegalName ){
			Logger.info("Changed name from " + _elementName + " to " + theLegalName);
		}
		
		IRPModelElement theOwner = parent.get_rhpEl();
		
		if( theOwner == null ){
			throw new Exception("Parent element was null");
		}
		
		_rhpEl = theOwner.addNewAggr("Argument", theLegalName );			
		
		return _rhpEl;
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #251 29-MAY-2019: First official version of new TauMigratorProfile (F.J.Chadburn)

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