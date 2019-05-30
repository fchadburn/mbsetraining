package generalhelpers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.telelogic.rhapsody.core.*;

public class UserInterfaceHelpers {

	public static void setLookAndFeel(){
		
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

//			UIManager.setLookAndFeel(
//					"com.sun.java.swing.plaf.motif.MotifLookAndFeel");

//					"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			Logger.writeLine("Unhandled exception while trying to UIManager.setLookAndFeel");
			e.printStackTrace();
		}
	}
	
	public static void setLookAndFeel2(){
		
		JDialog.setDefaultLookAndFeelDecorated(true);
/*
		try {
			UIManager.setLookAndFeel(
					"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			Logger.writeLine("Unhandled exception while trying to UIManager.setLookAndFeel");
			e.printStackTrace();
		}*/
	}
	
	public static boolean askAQuestion(
			String question ){
		 
		JDialog.setDefaultLookAndFeelDecorated(true);
		 
		int answer = JOptionPane.showConfirmDialog(
				null, 
				question, 
				"Question?", 
				JOptionPane.YES_NO_OPTION);
		
		return (answer == JOptionPane.YES_OPTION);
	}
	
	public static void showWarningDialog(
			String theMsg ){
		
		Logger.writeLine("Warning: " + theMsg);
		
	    JDialog.setDefaultLookAndFeelDecorated( true );
	    
	    JOptionPane.showMessageDialog(
	    		null,  
	    		theMsg,
	    		"Warning",
	    		JOptionPane.WARNING_MESSAGE);	
	}
	
	public static void showInformationDialog(
			String theMsg ){
		
		Logger.writeLine("Information: " + theMsg);
		
	    JDialog.setDefaultLookAndFeelDecorated( true );
	    
	    JOptionPane.showMessageDialog(
	    		null,  
	    		theMsg,
	    		"Information",
	    		JOptionPane.INFORMATION_MESSAGE);	
	}
	
	public static IRPModelElement launchDialogToSelectElement(
			List<IRPModelElement> inList, 
			String messageToDisplay, 
			Boolean isFullPathRequested ){
		
		IRPModelElement theEl = null;
		
		List<String> nameList = new ArrayList<String>();
		
		for (int i = 0; i < inList.size(); i++) {
			if (isFullPathRequested){
				nameList.add(i, inList.get(i).getFullPathName());
			} else {
				nameList.add(i, inList.get(i).getName());
			}
		} 	
		
		Object[] options = nameList.toArray();
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		String selectedElementName = (String) JOptionPane.showInputDialog(
				null,
				messageToDisplay,
				"Input",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		
		if (selectedElementName != null){
			int index = nameList.indexOf(selectedElementName);
			theEl = inList.get(index);
		}
		
		return theEl;
	}
	
	public static boolean checkOKToRunAndWarnUserIfNot(){
		
		boolean isOK = true;
		 
		@SuppressWarnings("rawtypes")
		List theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();
		
		if( theAppIDs.size() > 1 ){
			
			UserInterfaceHelpers.showWarningDialog( "The SysMLHelper has detected that there are x" + 
					theAppIDs.size() + " Rhapsody clients running. \n" +
					"The helpers are disabled to avoid the danger of writing to the wrong model.\n" +
					"You need to close the clients you're not using and try running the command again.\n" +
					"If the commands are not working, then you may need to reload the plugin by exiting\n" +
					"and re-starting Rhapsody with the model you want it to work on.");

			isOK = false;
			
		} else {
			
			IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			IRPProject theRhpProject = theRhpApp.activeProject();
			
			if( theRhpProject.getName().equals("SysMLHelper")){
				
				UserInterfaceHelpers.showWarningDialog(
						"The SysMLHelper commands cannot be run in the SysMLHelper project.");
				isOK = false;
			}
		}
		
		return isOK;
	}
	
	public static String getAppIDIfSingleRhpRunningAndWarnUserIfNot(){
		
		String theAppID = null;
		 
		@SuppressWarnings({ "unchecked" })
		List<String> theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();
		
		if( theAppIDs.size() > 1 ){
			
			UserInterfaceHelpers.showWarningDialog( "The SysMLHelper has detected that there are x" + 
					theAppIDs.size() + " Rhapsody clients running. \n" +
					"The helpers are disabled to avoid the danger of writing to the wrong model.\n" +
					"You need to close the clients you're not using and try running the command again.\n" +
					"If the commands are not working, then you may need to reload the plugin by exiting\n" +
					"and re-starting Rhapsody with the model you want it to work on.");
			
		} else if( theAppIDs.size() == 1 ){
			
			theAppID = theAppIDs.get( 0 );
		}
		
		return theAppID;
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #003 09-APR-2016: Added double-click UC to open ACT (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #017 11-MAY-2016: Double-click now works with both nested and hyper-linked diagrams (F.J.Chadburn)
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #173 02-APR-2017: cleanUpAutoRippleDependencies now gives an information rather than warning dialog (F.J.Chadburn)
    #239 04-OCT-2017: Improve warning/behaviour if multiple Rhapsodys are open or user switches app (F.J.Chadburn)

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
