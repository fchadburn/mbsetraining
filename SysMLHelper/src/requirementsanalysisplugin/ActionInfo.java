package requirementsanalysisplugin;

import generalhelpers.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class ActionInfo {
  
	private String oldName;
	private String desiredName;
	private String chosenName;
	private String theType = "Unspecified";
	private String theComment = "";
	
	private boolean isRenameNeeded = false;
	private boolean isNameChosen = false;
	private boolean isTraceabilityFailure = false;
	private boolean isTraceabilityChecked = false;
	
	private IRPModelElement theElement = null;
	private String currentUnadornedName = "Unspecified";
	
	private final List<String> elementTypesNeedingTraceability = Arrays.asList("Action", "AcceptEventAction", "EventState", "TimeEvent", "Transition");
	
	public ActionInfo(IRPModelElement theElement) {
		super();
		this.theElement = theElement;
		this.oldName = theElement.getName();		
		 
		// this is the name without the (X) post-fix
		this.currentUnadornedName = this.oldName.replaceAll("\\(\\d+\\)$","").trim();
				
		if (this.oldName.contains("ROOT")){		
			
			this.isRenameNeeded = false;

		} else if (theElement instanceof IRPState || theElement instanceof IRPTransition){
			
			if (theElement instanceof IRPState){
				
				IRPState theState = (IRPState)theElement; 
				this.theType = theState.getStateType();
				this.isRenameNeeded = true;
				
				if (theType.equals("Action")){
					this.desiredName = theState.getEntryAction().trim().replaceAll("\\.", "_");
					
				} else if (theType.equals("AcceptEventAction") || 
							theType.equals("EventState") || // This is a SendEvent action
							theType.equals("TimeEvent") ||
							theType.equals("Block")){ // This is the interruptibleRegion action
					
					this.desiredName = theState.getDisplayName().trim().replaceAll("\\.", "_");
					
				} else {
					Logger.writeLine("Ignoring " + theType + " called " + this.oldName);
					this.desiredName = null;
				}
				
			} else if (theElement instanceof IRPTransition){
				
				this.theType = theElement.getMetaClass();
				this.desiredName = null; 
			}
			
			if (this.desiredName != null){
				
				if (desiredName.isEmpty()){
					Logger.writeLine("Info: Skipping " + Logger.elementInfo( theElement ) + " as the desired name is blank");
					this.isRenameNeeded = false;
					
				} else if (this.currentUnadornedName.equals(desiredName)){
					this.isRenameNeeded = false;

				} else {
					Logger.writeLine(theElement.getName() + " is incorrectly named, desired name is '"
				       + this.desiredName + "' so mark as such");
					this.isRenameNeeded = true;	
				}
			} else {
				this.isRenameNeeded = false;
			}
			
			// check for requirements
			List<IRPRequirement> theReqts = getRequirementsThatDependOn( theElement );
		
			if (theReqts.isEmpty()){
				
				if (elementTypesNeedingTraceability.contains(theType)){
					this.isTraceabilityFailure = true;
					Logger.writeLine( theElement.getName() + " has no traceability to a requirement" );
					
					if (theType.equals("Transition")){
						this.theComment = "No traceability (on Transition with Guard [])";
					} else {
						this.theComment = "No traceability";
					}
				} else {					
					this.isTraceabilityFailure = false;
					Logger.writeLine( theElement.getName() + " was not checked for traceability to a requirement" );
					this.theComment = "No traceability found but not a failure";
				}

			} else {
				this.isTraceabilityFailure = false;
			}
			
			this.isTraceabilityChecked = true;
			
		}
	}
	
	private List<IRPRequirement> getRequirementsThatDependOn(IRPModelElement theElement){
		
		List<IRPRequirement> theReqts = new ArrayList<IRPRequirement>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theDeps = theElement.getDependencies().toList();
		
		for (IRPDependency theDep : theDeps) {
			
			IRPModelElement theDependsOn = theDep.getDependsOn();
			
			if (theDependsOn instanceof IRPRequirement){
				
				IRPRequirement theReq = (IRPRequirement)theDependsOn;
				
				if (!theReqts.contains(theReq)){
					theReqts.add(theReq);
				} else {
					Logger.writeLine("Unexpected: " + Logger.elementInfo(theElement) + " has more than one dependency to " + Logger.elementInfo(theReq));
				}
			}
		}
		
		return theReqts;
	}
	
    public void setChosenNameToOldName(){
    	this.chosenName = this.oldName;
    	this.isRenameNeeded = false;
    	this.isNameChosen = true;
    }
    
    public void setChosenNameToDesiredName(){   	
    	this.chosenName = this.desiredName;
    	this.isRenameNeeded = true;
    	this.isNameChosen = true;

    }
    
    public IRPModelElement getTheElement(){
    	return this.theElement;
    }
	
	public void performRename(){
		
		if (this.isRenameNeeded){
			if (this.isNameChosen){
				this.theElement.setName( this.chosenName );
				Logger.writeLine(this.theElement, "was renamed, old name was " + this.oldName);
			} else {
				Logger.writeLine(this.theElement, "rename not possible as no name was chosen");
			}
		} else {
			//Logger.writeLine(this.theElement, "rename is not needed");
		}
	}
	
	public String getOldName() {
		return oldName;
	}

	public String getDesiredName() {
		return desiredName;
	}
	
	public String getChosenName() {
		return chosenName;
	}
	
	public void setChosenName(String chosenName) {
		this.chosenName = chosenName;
		this.isNameChosen = true;
	}
	
	public boolean isRenameNeeded() {
		return isRenameNeeded;
	}
	
	public String getSummary(){
		return "isNameChosen=" + this.isNameChosen + "/Old name=" + this.getOldName() + 
			   "/ChosenName=" + this.getChosenName() + "/" + this.isRenameNeeded + 
			   "/UnadornedName=" + this.currentUnadornedName + "/DesiredName=" + 
			   this.getDesiredName();
	}

	public boolean isNameChosen() {
		return isNameChosen;
	}

	public Boolean isTraceabilityFailure() {
		return isTraceabilityFailure;
	}

	public boolean isTraceabilityChecked() {
		return isTraceabilityChecked;
	}

	public void setTraceabilityChecked(boolean isTraceabilityChecked) {
		this.isTraceabilityChecked = isTraceabilityChecked;
	}

	public String getType() {
		return theType;
	}
	
	public String getComment() {
		return theComment;
	}
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    
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
