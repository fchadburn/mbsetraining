package taumigrator;

import com.telelogic.rhapsody.core.IRPModelElement;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElPackage extends RhpElElement {

	
	public RhpElPackage(
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

	public RhpElPackage(
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
		
		_rhpEl = theOwner.addNewAggr("Package", theLegalName );			
		
		return _rhpEl;
	}
}
