package taumigrator;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

import generalhelpers.Logger;

public class RhpElProject extends RhpElElement {
	
	public RhpElProject(
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

	public RhpElProject(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theText,
			String thePosition,
			String theSize ) throws Exception {
		
		super(theElementName, theElementType, theElementGuid, theParent);
		
		dumpInfo();
	}

	@Override
	public IRPModelElement createRhpEl( 
			RhpEl treeRoot ) {
		
		Logger.writeLine("createRhpEl invoked for " + getString() );

		IRPModelElement theOwner = RhapsodyAppServer.getActiveRhapsodyApplication().activeProject();
		
		_rhpEl = theOwner.addNewAggr("Package", "u2Pkg" );			
		
		return _rhpEl;
	}
}
