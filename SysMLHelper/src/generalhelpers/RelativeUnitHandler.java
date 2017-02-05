package generalhelpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import sysmlhelperplugin.SysMLHelperPlugin;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;

public class RelativeUnitHandler {

	public static void browseAndAddUnit(
			IRPProject inProject, 
			boolean relative ){

		JFileChooser theFileChooser = new JFileChooser( System.getProperty("user.dir") );
		theFileChooser.setFileFilter( new FileNameExtensionFilter( "Package", "sbs" ) );

		int choice = theFileChooser.showDialog( null, "Choose Unit (.sbs)" );

		if( choice==JFileChooser.CANCEL_OPTION ){
			Logger.writeLine("Operation cancelled by user when trying to choose Unit (.sbs)");

		} else if( choice==JFileChooser.APPROVE_OPTION ){

			File theFile = theFileChooser.getSelectedFile();

			Logger.writeLine("theFile.getAbsolutePath = " + theFile.getAbsolutePath() );
			Logger.writeLine("theFile.getName = " + theFile.getName() );
			Logger.writeLine("theFile.getParent = " + theFile.getParent() );
			Logger.writeLine("theFile.getPath = " + theFile.getPath() );
			Logger.writeLine("theFile.getParentFile().getName() = " + theFile.getParentFile().getName() );
			
			String theTargetPath;

			try {
				theTargetPath = theFile.getCanonicalPath();

				Logger.writeLine( "theTargetPath=" + theTargetPath );
				
				SysMLHelperPlugin.getRhapsodyApp().addToModelByReference( theTargetPath );

				if( relative ){

					String theFileNameIncludingExtension = theFile.getName();
					
					String theName = theFileNameIncludingExtension.substring(0, theFileNameIncludingExtension.length()-3);

					int trimSize = theName.length()+5;
					
					Path targetPath = Paths.get( theTargetPath.substring(0, theTargetPath.length()-trimSize) );
					Path targetRoot = targetPath.getRoot();
					
					Logger.writeLine( "targetRoot.toString()=" + targetRoot.toString() );

					Path sourcePath = Paths.get( 
							inProject.getCurrentDirectory().replaceAll(
									inProject.getName()+"$", "") );

					Path sourceRoot = sourcePath.getRoot();

					Logger.writeLine( "sourceRoot.toString()=" + sourceRoot.toString() );
					
					if( !targetRoot.equals( sourceRoot ) ){
						Logger.writeLine("Unable to set Unit called " + theName + " to relative, as the drive letters are different");
						Logger.writeLine("theTargetDir root =" + targetPath.getRoot());
						Logger.writeLine("theTargetDir=" + targetPath);
						Logger.writeLine("theSourceDir root =" + sourcePath.getRoot());
						Logger.writeLine("theSourceDir=" + sourcePath);
					} else {
						Path theRelativePath = sourcePath.relativize(targetPath);

						IRPModelElement theCandidate = inProject.findAllByName( theName, "Package" );

						if( theCandidate != null && theCandidate instanceof IRPPackage ){

							IRPPackage theAddedPackage = (IRPPackage)theCandidate;

							theAddedPackage.setUnitPath( "..\\..\\" + theRelativePath.toString() );

							Logger.writeLine( "Unit called " + theName + 
									".sbs was changed from absolute path='" + theTargetPath + 
									"' to relative path='" + theRelativePath + "'" );
						}
					}
				}
			} catch( IOException e ){
				Logger.writeLine( "Error, unhandled IOException in RelativeUnitHandler.browseAndAddUnit");
			}
		}
	}
}

/**
 * Copyright (C) 2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #162 05-FEB-2017: Add new menu to add a relative reference to an external unit (.sbs) (F.J.Chadburn)
    
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
