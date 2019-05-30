package executablembse;

import java.util.List;

import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;

import com.telelogic.rhapsody.core.*;

public class RequirementMover extends ElementMover {

	private IRPStereotype _moveToStereotype = null;
	
	public RequirementMover(
			IRPModelElement theElement ){
		
		super( theElement, StereotypeAndPropertySettings.getRequirementPackageStereotype( theElement ) );
		
		_moveToStereotype = getMoveToStereotype( _moveToPkg );
	}
	
	private IRPStereotype getMoveToStereotype( 
			IRPPackage basedOnPackage ){
		
		IRPStereotype theMoveToStereotype = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStereotype> theStereotypes = basedOnPackage.getStereotypes().toList();
		
		for( IRPStereotype theStereotype : theStereotypes ){
			
			if( theStereotype.getName().startsWith("from") ){
				theMoveToStereotype = theStereotype;
				Logger.info( "Found move to " + Logger.elementInfo( theStereotype ) );
				break;
			}
		}
		
		return theMoveToStereotype;
	}

	public boolean performMove(){
		
		boolean isSuccess = super.performMove();
		
		if( isSuccess ){
			
			if( _moveToStereotype != null ){
				try {
					_element.setStereotype( _moveToStereotype );
					
				} catch( Exception e ){
					Logger.error( "Error in RequirementsMover.performMove, " +
							"unable exception trying to apply " + 
							Logger.elementInfo( _moveToStereotype ) + 
							" to " + Logger.elementInfo( _element ) );
				}
			}
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