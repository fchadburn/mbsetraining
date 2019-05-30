package executablembse;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class AutoPackageDiagram {

	IRPProject m_RhpPrj;
	IRPApplication m_RhpApp;
	IRPObjectModelDiagram m_OMD = null; 
	
	private boolean isRootOwnerInProjectAProfile(
			IRPModelElement theEl ){
		
		boolean isAProfile;
		
		IRPModelElement theOwner = theEl.getOwner();
		
		if( theOwner instanceof IRPProject ){
			
			if( theEl instanceof IRPProfile ){
				isAProfile = true;
			} else {
				isAProfile = false;
			}
		} else {
			isAProfile = isRootOwnerInProjectAProfile( theOwner );
		}
		
		return isAProfile;
	}
	
	public AutoPackageDiagram( 
			IRPProject theProject ) {
		
		m_RhpPrj = theProject;
		m_RhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
	}
	
	private String getDiagramName(){
		return "PKG - " + m_RhpPrj.getName();
	}
	
	@SuppressWarnings("unchecked")
	public void drawDiagram() {

		String theName = getDiagramName();
		
		IRPModelElement theExistingDiagramEl =
				m_RhpPrj.findNestedElement( 
						theName, 
						"ObjectModelDiagram" );
		
		List<IRPModelElement> elementsAlreadyPresent = 
				new ArrayList<>();
				
		if( theExistingDiagramEl != null ){
			
			m_OMD = (IRPObjectModelDiagram) theExistingDiagramEl;
			elementsAlreadyPresent = m_OMD.getElementsInDiagram().toList();
		}
		
		List<IRPModelElement> theCandidates = 
				m_RhpPrj.getNestedElementsByMetaClass( 
						"Package", 1 ).toList();
		
		IRPCollection theCollection = m_RhpApp.createNewCollection();

		for( IRPModelElement thePkg : theCandidates ){
			
			if( !elementsAlreadyPresent.contains(thePkg) && 
				!isRootOwnerInProjectAProfile( thePkg ) ){
				theCollection.addItem( thePkg );
			}
		}
		
		IRPCollection theRelationsCollection = m_RhpApp.createNewCollection();
		theRelationsCollection.setSize( 1 );
		theRelationsCollection.setString( 1, "AllRelations" );
		
		if( m_OMD == null ){
			m_OMD = m_RhpPrj.addObjectModelDiagram( theName );
			m_OMD.changeTo( "Package Diagram" );
		}

		m_OMD.populateDiagram( theCollection, theRelationsCollection, "among" );
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #249 29-MAY-2019: First official version of new ExecutableMBSEProfile  (F.J.Chadburn)

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