/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited

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

package sysmlhelperplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.telelogic.rhapsody.core.IRPProject;

public class FileHelper {

	public static File getFileWith(String theName, String inThePath){
		
		File theFileFound = null;
		
		File theDirectory = new File( inThePath );
		
	    File[] theCandidateFiles = theDirectory.listFiles();
	    
	    if( theCandidateFiles!=null ){
	        for (File theCandidateFile : theCandidateFiles){
	        	
	        	if (theCandidateFile.isFile() && theCandidateFile.getName().contains(theName)){
	        		Logger.writeLine("Found: " + theCandidateFile.getAbsolutePath());
	        		theFileFound = theCandidateFile;
	        		break;
	        	}		            
	        }		    		        
	    }
	    
	    if (theFileFound==null){
	    	Logger.writeLine("Error in getFileWith, no file found with name " + theName + " in the directory " + inThePath);
	    }
	    
	    return theFileFound;
	}

	public static void copyTheFile(IRPProject toTheProject, File theFile, String theNewName) {

		String rpyFolder = toTheProject.getName()+"_rpy";
		
		Logger.writeLine("Copying file called " + theFile.getName() + " to the " + rpyFolder + " folder");
		Path sourcePath      = theFile.toPath();
		Path destinationPath = Paths.get(toTheProject.getCurrentDirectory(), rpyFolder, theNewName);
 
		try {
		    Files.copy(sourcePath, destinationPath);
		    Logger.writeLine("Done!");
		} catch(FileAlreadyExistsException e) {
		    //destination file already exists
			Logger.writeLine("Already exists!");
		} catch (IOException e) {
		    //something else went wrong
		    e.printStackTrace();
		    Logger.writeLine("Exceptions!!!");
		}
	}
}