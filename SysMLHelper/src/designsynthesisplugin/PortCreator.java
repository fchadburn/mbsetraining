package designsynthesisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.telelogic.rhapsody.core.*;

public class PortCreator {
		
	public static List<IRPModelElement> getBlocksThatAreChildPartsOf( IRPClassifier theBlock ){
		
		List<IRPModelElement> theBlockList = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theParts = theBlock.getNestedElementsByMetaClass("Object", 1).toList();
		
		for (IRPModelElement theEl : theParts) {
					
			if (theEl instanceof IRPInstance){
				
				IRPInstance thePart = (IRPInstance) theEl;				
				IRPClassifier theOtherClass = thePart.getOtherClass();	
				
				if (theOtherClass != null){
					theBlockList.add( theOtherClass );
					//Logger.writeLine(theOtherClass, "was added to the list");
					theBlockList.addAll( getBlocksThatAreChildPartsOf( theOtherClass ) );
				}	
			}
		}
		
		return theBlockList;
	}
	
	private static IRPModelElement getPartOwnerOf(IRPClassifier theBlock){
		
		IRPModelElement theOwner = null;
		
		IRPPackage theDesignSynthesisPkg = (IRPPackage) theBlock.getProject().findNestedElement("DesignSynthesisPkg", "Package");
		
		if (theDesignSynthesisPkg != null){
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theParts = theDesignSynthesisPkg.getNestedElementsByMetaClass("Object", 1).toList();
			
			for (IRPModelElement theEl : theParts) {
				
				if (theEl instanceof IRPInstance){
					IRPInstance thePart = (IRPInstance) theEl;
					
					IRPClassifier theOtherClass = thePart.getOtherClass();
					
					if (theOtherClass != null){
						Logger.writeLine("Check if " + Logger.elementInfo(theOtherClass) + " is " + Logger.elementInfo(theBlock));
					}
					
					if (theOtherClass != null && theOtherClass.equals(theBlock)){

						theOwner = thePart.getOwner();
						Logger.writeLine("Found owner of part typed by " + Logger.elementInfo( theBlock ) + 
								" is " + Logger.elementInfo( theOwner ));
						
						if (GeneralHelpers.hasStereotypeCalled("LogicalSystem", theOwner)){
							Logger.writeLine(theOwner, "does has a LogicalSystem stereotype");
							break;
						} else {
							Logger.writeLine(theOwner, "does not have a LogicalSystem stereotype");
							theOwner = getPartOwnerOf( (IRPClassifier) theOwner );
						}
					}
				}
			}
		}

		return theOwner;
	}
	
	public static void createPublishFlowportsFor(
			List<IRPModelElement> theSelectedEls){
			
		for (IRPModelElement selectedEl : theSelectedEls) {
			
			if (selectedEl instanceof IRPAttribute){
				
				IRPAttribute theAttribute = (IRPAttribute)selectedEl;
				Logger.writeLine(theAttribute, "is being processed");
				
				createPublishFlowportFor(theAttribute);
			} else {
				Logger.writeLine("Doing nothing for " + Logger.elementInfo(selectedEl) 
						+ " as it is not an Atttribute");
			}
		}
	}
	
	public static IRPSysMLPort createPublishFlowportFor(IRPAttribute theAttribute){
		
		IRPSysMLPort thePort = null;
		
		// check if flow port already exists
		String theName = theAttribute.getName();
		IRPClassifier theOwner = (IRPClassifier) theAttribute.getOwner();
		
	    JDialog.setDefaultLookAndFeelDecorated(true);
	    /*
	    int response = JOptionPane.showConfirmDialog(null, 
	    		"Do you want to add subscribe ports to other Blocks?\n", "Confirm",
	        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	    */
	    int response = JOptionPane.NO_OPTION;
	    
	    if (response == JOptionPane.YES_OPTION) {
	    	IRPModelElement thePartOwner = getPartOwnerOf(theOwner);
	    	
	    	if (thePartOwner != null && thePartOwner instanceof IRPClassifier){
	    		
	    		IRPClassifier theClassifier = (IRPClassifier)thePartOwner;
	    		Logger.writeLine(thePartOwner, "was found to be the LogicalSystem");
	    		
	    		List<IRPModelElement> theCandidateList = getBlocksThatAreChildPartsOf( theClassifier );
	    		List<BlockSelectionInfo> theBlockSelectionList = new ArrayList<BlockSelectionInfo>();
	    				
	    		JPanel panel = new JPanel();
	    		
				panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.PAGE_AXIS));
				panel.add( new JLabel("Which Blocks do you want to add subscribers of \n" +
				    "the attribute called " + theAttribute.getName() + " to?") );	
				
				for (IRPModelElement theEl : theCandidateList) {
					
					JCheckBox theCheckBox = new JCheckBox( theEl.getName() );
					
					BlockSelectionInfo theSelectionInfo = new BlockSelectionInfo(
							theCheckBox, theEl);
					
					theBlockSelectionList.add( theSelectionInfo );
					panel.add( theCheckBox );
				}
				
				int choice = JOptionPane.showConfirmDialog(
						null, panel, "Create from an inherited behaviour?", JOptionPane.YES_NO_CANCEL_OPTION);

				if( choice==JOptionPane.YES_OPTION ){
					
					Logger.writeLine("YES was chosen");
					
					for (BlockSelectionInfo theBlockInfo : theBlockSelectionList) {
						
						if (theBlockInfo.isSelected()){
							
							IRPModelElement theElement = theBlockInfo.getModelElement();
							
							if (theElement != null){
								Logger.writeLine(theElement, "was selected");
							} else {
								Logger.writeLine("Error in XXX");
							}							
						}
					}
				}				
	    	}	    	
	    }
	    
		// check if port already exists
		thePort = (IRPSysMLPort) theOwner.findNestedElement(theName, "SysMLPort");
		
		if (thePort != null){
			Logger.writeLine(theAttribute, "already has a corresponding port of same name");
		} else {
			Logger.writeLine("Creating an 'Out' flowport for " + Logger.elementInfo(theAttribute));
			thePort = (IRPSysMLPort) theOwner.addNewAggr("FlowPort", theName);
		}
		
		if (thePort != null){
			thePort.setType(theAttribute.getType());
			thePort.setPortDirection("Out");
			
			IRPStereotype existingSubscribeStereotype = 
					GeneralHelpers.getStereotypeCalled("subscribe",theAttribute);
			
			if (existingSubscribeStereotype != null){
				thePort.removeStereotype(existingSubscribeStereotype);
			}
			
			Logger.writeLine("Applying publish stereotype to " + Logger.elementInfo(theAttribute));
			
			GeneralHelpers.applyExistingStereotype("publish", theAttribute);
			
		} else {
			Logger.writeLine("Error in createPublishFlowportFor, no port was created");
		}
		
		return thePort;
	}
	
	
	public static void createSubscribeFlowportsFor(
			List<IRPModelElement> theSelectedEls){
			
		for (IRPModelElement selectedEl : theSelectedEls) {
			
			if (selectedEl instanceof IRPAttribute){
				
				IRPAttribute theAttribute = (IRPAttribute)selectedEl;
				Logger.writeLine(theAttribute, "is being processed");
				
				createSubscribeFlowportFor(theAttribute);
			} else {
				Logger.writeLine("Doing nothing for " + Logger.elementInfo(selectedEl) 
						+ " as it is not an Atttribute");
			}
		}
	}
	
	public static IRPSysMLPort createSubscribeFlowportFor(IRPAttribute theAttribute){
		
		IRPSysMLPort thePort = null;
		
		// check if flow port already exists
		String theName = theAttribute.getName();
		IRPClassifier theOwner = (IRPClassifier) theAttribute.getOwner();
		
		// check if port already exists
		thePort = (IRPSysMLPort) theOwner.findNestedElement(theName, "SysMLPort");
		
		if (thePort != null){
			Logger.writeLine(theAttribute, "already has a corresponding port of same name");
		} else {
			Logger.writeLine("Creating an 'In' flowport for " + Logger.elementInfo(theAttribute));
			thePort = (IRPSysMLPort) theOwner.addNewAggr("FlowPort", theName);
		}
		
		if (thePort != null){
			thePort.setType(theAttribute.getType());
			thePort.setPortDirection("In");
			
			IRPStereotype existingStereotype = 
					GeneralHelpers.getStereotypeCalled("publish",theAttribute);
			
			if (existingStereotype != null){
				thePort.removeStereotype(existingStereotype);
			}
			
			Logger.writeLine("Applying subscribe stereotype to " + Logger.elementInfo(theAttribute));
			
			GeneralHelpers.applyExistingStereotype("subscribe", theAttribute);
			
		} else {
			Logger.writeLine("Error in createSubscribeFlowportFor, no port was created");
		}
		
		return thePort;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #095 23-AUG-2016: Turned off the "Do you want to add subscribe ports to other Blocks?" question (F.J.Chadburn)
    
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
