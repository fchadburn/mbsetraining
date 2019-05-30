package executablembse;

import java.util.List;

import com.telelogic.rhapsody.core.IRPActor;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPUseCase;
import com.telelogic.rhapsody.core.IRPUseCaseDiagram;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

import generalhelpers.Logger;
import generalhelpers.PopulatePkg;
import generalhelpers.StereotypeAndPropertySettings;

public class CreateUseCasesPackage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
	
	public CreateUseCasesPackage(
			String theUseCasesPackageName,
			IRPPackage theUseCasesOwnerPkg,
			CreateRequirementsPkg.CreateRequirementsPkgOption theReqtsPkgChoice,
			String theReqtsPkgOptionalName,
			IRPPackage theExistingReqtsPkgIfChosen,
			CreateActorPkg.CreateActorPkgOption theActorPkgChoice,
			String theActorsPkgNameOption,
			List<IRPPackage> theExistingActorsPkgOption,
			String theActorPkgPrefixOption ){
		
		IRPProject theProject = theUseCasesOwnerPkg.getProject();
		
		String theAdornedName = theUseCasesPackageName + "Pkg";
		
		Logger.writeLine( "The name is " + theAdornedName );
		
		IRPPackage theUseCasePkg = theUseCasesOwnerPkg.addNestedPackage( theAdornedName );
		
		theUseCasePkg.changeTo( 
				StereotypeAndPropertySettings.getUseCasePackageStereotype( 
						theUseCasesOwnerPkg ) );
		
		StereotypeAndPropertySettings.setSavedInSeparateDirectoryIfAppropriateFor( 
				theUseCasePkg );
		
		@SuppressWarnings("unused")
		CreateRequirementsPkg theCreateRequirementsPkg = new CreateRequirementsPkg( 
				theReqtsPkgChoice, 
				theUseCasePkg, 
				theReqtsPkgOptionalName, 
				theExistingReqtsPkgIfChosen );

		CreateActorPkg theActorPkg = new CreateActorPkg( 
				theActorPkgChoice,
				theProject,
				theActorsPkgNameOption,
				theUseCasePkg,
				theExistingActorsPkgOption,
				theActorPkgPrefixOption );
		
		List<IRPActor> theActors = theActorPkg.getActors();

		createUseCaseDiagram( theActors, theAdornedName, theUseCasePkg );
		
		PopulatePkg.deleteIfPresent( "Structure1", "StructureDiagram", theProject );
		PopulatePkg.deleteIfPresent( "Model1", "ObjectModelDiagram", theProject );
		PopulatePkg.deleteIfPresent( "Default", "Package", theProject );
		
		AutoPackageDiagram theAPD = new AutoPackageDiagram( theProject );
		theAPD.drawDiagram();
			    			
		theProject.save();
	}
	
	private void createUseCaseDiagram(
			List<IRPActor> theActors, 
			String theName,
			IRPPackage theUseCasePkg ) {
		
		IRPUseCaseDiagram theUCD = 
				theUseCasePkg.addUseCaseDiagram( "UCD - " + theName );
		
		IRPStereotype theStereotype =
				StereotypeAndPropertySettings.getStereotypeForUseCaseDiagram( 
						theUseCasePkg );
		
		if( theStereotype != null ){
			
			Logger.writeLine("Applying " + Logger.elementInfo( theStereotype ) + 
					" to " + Logger.elementInfo( theUCD ) );
			
			theUCD.setStereotype( theStereotype );
		}
		
		IRPUseCase theUC = theUseCasePkg.addUseCase( "UC01 - " );
					
		IRPCollection theCollection = 
				RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();
		
		for( IRPActor theActor : theActors ){
			
			theActor.addRelationTo(
					(IRPClassifier) theUC, 
					"", 
					"Association", 
					"1", 
					"", 
					"Association", 
					"1", 
					"" );
		}
		
		IRPGraphNode theNote =
				theUCD.addNewNodeByType( "Note", 21, 42, 156, 845 );
		
		String theUseCaseNoteText = 
				StereotypeAndPropertySettings.getUseCaseNoteText( theUCD );
		
		theNote.setGraphicalProperty(
				"Text",
				theUseCaseNoteText );
		
		int x0 = 420;
		int y0 = 270;
		int r = 190;

		int items = theActors.size();
				
		String theDefaultActorSize = theUCD.getPropertyValue("Format.Actor.DefaultSize");
		String[] theActorSplit = theDefaultActorSize.split(",");
		int actorWidth = Integer.parseInt( theActorSplit[2] );
		int actorHeight = Integer.parseInt( theActorSplit[3] );
		
		String theDefaultUseCaseSize = theUCD.getPropertyValue("Format.UseCase.DefaultSize");
		String[] theUseCaseSplit = theDefaultUseCaseSize.split(",");
		int useCaseWidth = Integer.parseInt( theUseCaseSplit[2] );
		int useCaseHeight = Integer.parseInt( theUseCaseSplit[3] );
		
	    IRPGraphNode theUCGraphNode = 
	    		theUCD.addNewNodeForElement( theUC, x0-(useCaseWidth/2), y0-(useCaseHeight/2), useCaseWidth, useCaseHeight );

	    theCollection.addGraphicalItem( theUCGraphNode );
	    
		for(int i = 0; i < items; i++) {

		    int x = (int) (x0 + r * Math.cos(2 * Math.PI * i / items));
		    int y = (int) (y0 + r * Math.sin(2 * Math.PI * i / items));   
		    
		    IRPGraphNode theActorGN = theUCD.addNewNodeForElement( 
		    		theActors.get(i), 
		    		x-(actorWidth/2), 
		    		y-(actorHeight/2), 
		    		actorWidth, 
		    		actorHeight );
		    
		    theCollection.addGraphicalItem( theActorGN );
		}
					
		theUCD.completeRelations(
				theCollection, 
				1);
				
		theUCD.highLightElement();
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