package taumigrator;

import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.CreateFunctionalBlockPackagePanel;
import functionalanalysisplugin.SequenceDiagramHelper;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class RhpElClass extends RhpElElement {

	public RhpElClass(
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

	public RhpElClass(
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
		
		_rhpEl = theOwner.addNewAggr("Class", theLegalName );
		_rhpEl.changeTo("Block");
		
		IRPClass theActiveClass = (IRPClass)_rhpEl;
		
		IRPPort theActivePort = (IRPPort) theActiveClass.addNewAggr("Port", "p");
		theActivePort.setIsBehavioral(1);
		
		// Create nested TestPkg package with components necessary for wiring up a simulation
		IRPPackage theTestPackage = (IRPPackage)theOwner.addNewAggr("Package", theLegalName + "_TestPkg");
		
		IRPClass theBuilder = (IRPClass) theTestPackage.addNewAggr( 
				"Class", theLegalName + "_Builder" );
		
		theBuilder.changeTo("Block");
		
		IRPClass theActor = (IRPClass) theTestPackage.addNewAggr( 
				"Class", theLegalName + "_External" );
		
		IRPPort theActorPort = (IRPPort) theActor.addNewAggr("Port", "p");
		theActorPort.setIsBehavioral(1);
		
		IRPInstance theActivePart = (IRPInstance) theBuilder.addNewAggr(
				"Part", "" );
		
		theActivePart.setOtherClass( theActiveClass );
		
		IRPInstance theActorPart = (IRPInstance) theBuilder.addNewAggr(
				"Part", "" );
		
		theActorPart.setOtherClass( theActor );
		
		IRPLink theLink = theBuilder.addLink(
				theActivePart, 
				theActorPart, 
				null, 
				theActivePort, 
				theActorPort );
		
		theLink.changeTo("connector");
		
		CreateFunctionalBlockPackagePanel.addAComponentWith( 
				theLegalName + "_EXE", 
				theTestPackage, 
				theBuilder, 
				"TokenOriented" );
		
		// Add a sequence diagram
		SequenceDiagramHelper.createSequenceDiagramFor(
				theBuilder, 
				theTestPackage, 
				"SD - " + theLegalName );
		
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