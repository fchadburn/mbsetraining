package requirementsanalysisplugin;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;
 
public class ActionList extends ArrayList<ActionInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ActionList(IRPActivityDiagram theAD){
		super();
				
		@SuppressWarnings("unchecked")
		List<IRPModelElement> candidateEls = theAD.getElementsInDiagram().toList();
		
		for (IRPModelElement theEl : candidateEls) {
			
			if (theEl instanceof IRPState){
				
				this.add(new ActionInfo(theEl));
				
			} else if (theEl instanceof IRPTransition){
				
				IRPTransition theTransition = (IRPTransition)theEl;
				
				IRPGuard theGuard = theTransition.getItsGuard();
				 
				// only check transitions that have guards, e.g. []
				if (theGuard != null){
					this.add(new ActionInfo(theEl));
				}
			}	
		}
	}

	public boolean isRenamingNeeded(){
		boolean isNeeded = false;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isRenameNeeded()){
				isNeeded = true;
				break;
			}
		}
		
		return isNeeded;
	}
	
	public int getNumberOfRenamesNeeded(){
		int count = 0;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isRenameNeeded()){
				count++;
			}
		}
		
		return count;
	}
	
	public int getNumberOfTraceabilityFailures(){
		int count = 0;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isTraceabilityFailure()){
				count++;
			}
		}
		
		return count;
	}
	
	public void chooseNames(){
	
		chooseNamesFirstPass();
		chooseNamesSecondPass();
		chooseNamesThirdPass();
	}
	
	private void chooseNamesFirstPass(){
		
		for (ActionInfo theInfo : this) {
			if (!theInfo.isRenameNeeded()){
				theInfo.setChosenNameToOldName();
			}
		}
	}
	
	private void chooseNamesSecondPass(){

		for (ActionInfo theInfo : this) {
			if (theInfo.isRenameNeeded()){
				if (isNameFree(theInfo.getDesiredName())){
					theInfo.setChosenNameToDesiredName();
				}
			}
		}
	}
	
	private void chooseNamesThirdPass(){

		for (ActionInfo theInfo : this) {
			if (!theInfo.isNameChosen()){
				
				int count = 0;
				String theNameToTry = theInfo.getDesiredName();
				
				while (!isNameFree( theNameToTry )){
					count++;
					theNameToTry = theInfo.getDesiredName()+ " (" + count + ")";
				}
				
				theInfo.setChosenName(theNameToTry);
			}
		}
	}
	
	public boolean isNameFree(String theName){
		
		boolean result = true;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isNameChosen() && 
				theInfo.getChosenName().equals(theName)){
				result = false;
			}
		}
		return result;
	}
	
	public void performRenames(){
		
		chooseNames();
		for (ActionInfo theInfo : this) {
			theInfo.performRename();
		}
			
	}
	
	public List<ActionInfo> getListOfActionsCheckedForTraceability(){
		
		List<ActionInfo> theActions = new ArrayList<ActionInfo>();
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isTraceabilityChecked()){
				theActions.add(theInfo);
			}
		}
		return theActions;
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
