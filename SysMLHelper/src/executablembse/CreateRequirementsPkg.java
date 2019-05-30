package executablembse;

import generalhelpers.CreateGatewayProjectPanel;
import generalhelpers.GatewayFileParser;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;

import java.io.File;

import com.telelogic.rhapsody.core.*;

public class CreateRequirementsPkg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
	}

	public enum CreateRequirementsPkgOption {
		DoNothing,
		CreateUnderProject,
		CreateUnderProjectWithStereotype,
		CreateUnderUseCasePkg,
		CreateUnderUseCasePkgWithStereotype,
		UseExistingReqtsPkg
	}

	CreateRequirementsPkg( 
			CreateRequirementsPkgOption theReqtsPkgChoice,
			IRPPackage theUseCasePkg,
			String theReqtsPkgOptionalName,
			IRPPackage theExistingReqtsPkgIfChosen ){

		IRPPackage theReqtsPkg = null;
		IRPProject theProject = theUseCasePkg.getProject();

		if( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProject ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProjectWithStereotype ){

			theReqtsPkg = createReqtsPackageWithDependencyTo(
					theUseCasePkg, 
					theReqtsPkgOptionalName, 
					theProject );

		} else if( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderUseCasePkg ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderUseCasePkgWithStereotype ){

			theReqtsPkg = createReqtsPackageWithDependencyTo(
					theUseCasePkg, 
					theReqtsPkgOptionalName, 
					theUseCasePkg );

		} else if( theReqtsPkgChoice == CreateRequirementsPkgOption.UseExistingReqtsPkg ){

			theUseCasePkg.addDependencyTo( theExistingReqtsPkgIfChosen );

		} else if( theReqtsPkgChoice == CreateRequirementsPkgOption.DoNothing ){	

			Logger.writeLine( "User chose to do nothing with Requirements package" );
		}

		if( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProjectWithStereotype ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderUseCasePkgWithStereotype ){

			String theCandidateTypesFileName = 
					theProject.getName() + ".types";

			File theExistingTypesFile = CreateGatewayProjectPanel.getFile(
					"^" + theProject.getName() + ".types$",
					theProject.getCurrentDirectory() + "\\" + theProject.getName() + "_rpy",
					"Which existing Types file do you want to use?");

			Logger.writeLine( "The corresponding types file is " + theCandidateTypesFileName );

			if( theExistingTypesFile != null ){

				@SuppressWarnings("unused")
				final GatewayFileParser theTemplateTypesFile = new GatewayFileParser( theExistingTypesFile );
			}

			String theStereotypeName = "from" + theReqtsPkgOptionalName; // or theName?

			IRPModelElement theFoundStereotype = 
					theProject.findAllByName( theStereotypeName , "Stereotype" );

			IRPStereotype theFromStereotype = null;

			if( theFoundStereotype == null ){
				theFromStereotype = theReqtsPkg.addStereotype( theStereotypeName, "Package" );

				theFromStereotype.addMetaClass( "Dependency" );
				theFromStereotype.addMetaClass( "HyperLink" );
				theFromStereotype.addMetaClass( "Requirement" );
				theFromStereotype.addMetaClass( "Type" );

				theFromStereotype.setOwner( theReqtsPkg );
				theFromStereotype.highLightElement();

			} else {
				theFromStereotype = theReqtsPkg.addStereotype( theStereotypeName, "Package" );
			}
		}
	}

	private IRPPackage createReqtsPackageWithDependencyTo(
			IRPPackage theUseCasePkg, 
			String theName,
			IRPPackage theReqtsPackageOwner ){

		IRPPackage theReqtsPkg;
		theReqtsPkg = theReqtsPackageOwner.addNestedPackage( theName );
		theReqtsPkg.changeTo( StereotypeAndPropertySettings.getRequirementPackageStereotype( theReqtsPackageOwner ) );	

		StereotypeAndPropertySettings.setSavedInSeparateDirectoryIfAppropriateFor( theReqtsPkg );

		theUseCasePkg.addDependencyTo( theReqtsPkg );

		return theReqtsPkg;
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