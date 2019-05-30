package executablembse;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.TraceabilityHelper;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class ElementMover {

	protected IRPModelElement _element = null;
	protected IRPPackage _moveToPkg = null;
	protected String _whereMoveToHasStereotype = null;

	public ElementMover(
			IRPModelElement theElement,
			String whereMoveToHasStereotype ) {
		
		_element = theElement;
		_whereMoveToHasStereotype = whereMoveToHasStereotype;
		_moveToPkg = getMoveToPackage( theElement );
	}
	
	protected IRPPackage getMoveToPackage( 
			IRPModelElement basedOnEl ){
		
		IRPPackage theMoveToPkg = null;
		
		Set<IRPModelElement> theCandidateEls =
				TraceabilityHelper.getStereotypedElementsThatHaveDependenciesFrom( 
						basedOnEl, 
						_whereMoveToHasStereotype );
		
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

		if( _moveToPkg != null && 
			!_moveToPkg.equals( _element.getOwner() ) ){

			// check if already element of same name
			IRPModelElement alreadyExistingEl = 
					_moveToPkg.findNestedElement( 
							_element.getName(),
							_element.getMetaClass() );

			if( alreadyExistingEl != null ){

				String uniqueName = GeneralHelpers.determineUniqueNameBasedOn( 
						_element.getName(), 
						_element.getMetaClass(), 
						_moveToPkg );

				Logger.warning( "Warning: Same name as " + Logger.elementInfo( _element ) 
						+ " already exists under " + Logger.elementInfo( _moveToPkg ) + 
						", hence element was renamed to " + uniqueName );

				_element.setName( uniqueName );
			}

			Logger.info( "Moving " + Logger.elementInfo( _element ) + 
					" to " + Logger.elementInfo( _moveToPkg ) );

			_element.getProject().save();
			_element.setOwner( _moveToPkg );

			isSuccess = true;
			
			_element.highLightElement();
		}

		return isSuccess;
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