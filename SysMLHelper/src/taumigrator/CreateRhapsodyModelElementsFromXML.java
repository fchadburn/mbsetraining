package taumigrator;

import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.io.File;
import java.util.List;

import javax.swing.UIManager;

import com.telelogic.rhapsody.core.*;

public class CreateRhapsodyModelElementsFromXML {

	static IRPApplication m_RhpApp;

	IRPProject m_RhpPrj;
	IRPModelElement m_SelectedEl = null;

	static CreateRhapsodyModelElementsFromXML m_App;

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		m_App = new CreateRhapsodyModelElementsFromXML( theRhpApp );
		m_App.go();
	}

	public CreateRhapsodyModelElementsFromXML( 
			IRPApplication app ) {

		m_RhpApp = app;
		m_RhpPrj = m_RhpApp.activeProject();
		m_SelectedEl = m_RhpApp.getSelectedElement();
	}

	public void go(){

		try {
			setLookAndFeel();

			if( UserInterfaceHelpers.checkOKToRunAndWarnUserIfNot() ){

				Logger.info("CreateRhapsodyModelElementsFromXML.go was invoked");

				String thePath = "C:\\Users\\frase\\Documents\\XXX\\";

				RhpEl parentNode = new RhpElProject( m_RhpPrj.getName(), "Project", "xxxxx" );

				ModelBuilder theElementStructure = new ModelBuilder();

				theElementStructure.parseXmlFile( thePath + "File1.u2", parentNode );

				performXMLImportFrom( parentNode );

			}		

		} catch (Exception e) {
			Logger.info( "Exception in Go, e=" + e.getMessage() );
		}		
	}

	private void performXMLImportFrom(
			//			String filename,
			RhpEl parentNode ){

		Logger.info( "Importing from: " );//+ filename );

		//		ElementStructure theElementStructure = new ElementStructure();

		//		theElementStructure.parseXmlFile( filename, parentNode );

		@SuppressWarnings("unused")
		int nodeCount  = parentNode.getNodeCount();

		//		List<String> theWarnings = 
		//				parentNode.getWarnings();

		List<String> theInfos = 
				parentNode.getInfos();

		Logger.info("+=================================================");

		Logger.info("The tree contains " + theInfos.size() + " elements:");
		for (String theInfo : theInfos) {
			Logger.info( theInfo );
		}
		Logger.info("... end of tree (" + theInfos.size() + ")");
		Logger.info("+=================================================");

		// pass parent node so that element can search full tree
		parentNode.createNodeElementsAndChildrenForJustEvents(
				m_SelectedEl,
				parentNode );

		// pass parent node so that element can search full tree
		parentNode.createNodeElementsAndChildren(
				m_SelectedEl,
				parentNode );

		// pass parent node so that element can search full tree
		parentNode.createRelationshipsAndChildren(
				parentNode );

		parentNode.addMergeNodes(parentNode);
		parentNode.reflowControlNodesFromReceiveEvents(parentNode);
		parentNode.addElseTransitionsIfNeeded(parentNode);


		Logger.info( "Saving" );
		m_RhpApp.saveAll();

		Logger.info( "Import Complete");
	}


	@SuppressWarnings("unused")
	private boolean isValidFile(String path){
		File f = new File(path);
		if (!f.exists())
			return false;

		if (!f.getAbsolutePath().endsWith(".u2"))
			return false;

		return true;
	}

	public static void setLookAndFeel(){
		try{
			UIManager.setLookAndFeel(
					"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

		} catch (Exception e){
			Logger.info("Unhandled exception invoking UIManager.setLookAndFeel");
		}
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