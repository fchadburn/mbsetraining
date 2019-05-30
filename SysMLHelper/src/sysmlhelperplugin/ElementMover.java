package sysmlhelperplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class ElementMover {

	protected IRPModelElement m_Element = null;
	protected IRPPackage m_MoveToPkg = null;
	protected String m_WhereMoveToHasStereotype = null;

	public ElementMover(
			IRPModelElement theElement,
			String whereMoveToHasStereotype ) {
		
		m_Element = theElement;
		m_WhereMoveToHasStereotype = whereMoveToHasStereotype;
		m_MoveToPkg = getMoveToPackage( theElement );
	}
	
	protected IRPPackage getMoveToPackage( 
			IRPModelElement basedOnEl ){
		
		IRPPackage theMoveToPkg = null;
		
		Set<IRPModelElement> theCandidateEls =
				TraceabilityHelper.getStereotypedElementsThatHaveDependenciesFrom( 
						basedOnEl, 
						m_WhereMoveToHasStereotype );
		
		if( theCandidateEls.size()==1 ){
			
			IRPModelElement theCandidate = null;
			
			for (IRPModelElement theCandidateEl : theCandidateEls) {
				theCandidate = theCandidateEl;
			}
			
			if( theCandidate instanceof IRPPackage ){
				theMoveToPkg = (IRPPackage) theCandidate;
			}
			
		} else if( theCandidateEls.size()==0 ){
			
			IRPModelElement theOwner = basedOnEl.getOwner();
			
			if( !(theOwner instanceof IRPProject) ){
				
				theMoveToPkg = getMoveToPackage( 
						basedOnEl.getOwner() );

			} else {
				// Unable to find a matching package in corresponding ownership tree
			}
		}
		
		return theMoveToPkg;
	}
	
	public boolean performMove(){

		boolean isSuccess = false;

		Logger.writeLine("m_MoveToPkg = " + Logger.elementInfo( m_MoveToPkg ));
		Logger.writeLine("m_Element.getOwner() = " + Logger.elementInfo(  m_Element.getOwner() ));

		if( m_MoveToPkg != null && 
			!m_MoveToPkg.equals( m_Element.getOwner() ) ){

			// check if already element of same name
			IRPModelElement alreadyExistingEl = 
					m_MoveToPkg.findNestedElement( 
							m_Element.getName(),
							m_Element.getMetaClass() );

			if( alreadyExistingEl != null ){

				String uniqueName = GeneralHelpers.determineUniqueNameBasedOn( 
						m_Element.getName(), 
						m_Element.getMetaClass(), 
						m_MoveToPkg );

				Logger.warning( "Warning: Same name as " + Logger.elementInfo( m_Element ) 
						+ " already exists under " + Logger.elementInfo( m_MoveToPkg ) + 
						", hence element was renamed to " + uniqueName );

				m_Element.setName( uniqueName );
			}

			Logger.info( "Moving " + Logger.elementInfo( m_Element ) + 
					" to " + Logger.elementInfo( m_MoveToPkg ) );

			m_Element.getProject().save();
			m_Element.setOwner( m_MoveToPkg );

			isSuccess = true;
			
			m_Element.highLightElement();
		}

		return isSuccess;
	}
	
	public boolean isMovePossible(){
		
		boolean isMovePossible = 
				m_Element != null &&
				m_MoveToPkg != null;
		
		return isMovePossible;
		
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
