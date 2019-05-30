package executablembse;

import generalhelpers.StereotypeAndPropertySettings;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class CreateActorPkg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		IRPProject theRhpPrj = theRhpApp.activeProject();
		@SuppressWarnings("unused")
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		IRPPackage theUseCasePkg = theRhpPrj.addPackage("UseCasePkg");
		
		@SuppressWarnings("unused")
		CreateActorPkg theCreateNewButEmpty =
				new CreateActorPkg(
						CreateActorPkgOption.CreateNewButEmpty,
						theRhpPrj,
						"NewButEmptyActorPkg",
						theUseCasePkg,
						null,
						null );
		
		CreateActorPkg theCreateNewFromDefault =
				new CreateActorPkg(
						CreateActorPkgOption.CreateNew,
						theRhpPrj,
						"NewFromDefaultActorPkg",
						theUseCasePkg,
						null,
						null );
		
		@SuppressWarnings("unused")
		CreateActorPkg theUseExisting =
				new CreateActorPkg(
						CreateActorPkgOption.UseExisting,
						theRhpPrj,
						null,
						theUseCasePkg,
						new ArrayList<>( theCreateNewFromDefault.getActorPkgs() ),
						null );
		
		@SuppressWarnings("unused")
		CreateActorPkg theInstantiateFromExisting =
				new CreateActorPkg(
						CreateActorPkgOption.InstantiateFromExisting,
						theRhpPrj,
						"Actors_MyFeaturePkg",
						theUseCasePkg,
						new ArrayList<>( theCreateNewFromDefault.getActorPkgs() ),
						"MyFeature" );
	}
	
	private List<IRPActor> _actors = new ArrayList<IRPActor>();
	private List<IRPPackage> _actorPkgs = new ArrayList<IRPPackage>();
	
	public enum CreateActorPkgOption {
		DoNothing,
	    CreateNew,
	    CreateNewButEmpty,
	    UseExisting,
	    InstantiateFromExisting,
	}

	public List<IRPPackage> getActorPkgs(){
		return _actorPkgs;
	}
	
	public List<IRPActor> getActors(){
		return _actors;
	}
	
	@SuppressWarnings("unchecked")
	CreateActorPkg(
			CreateActorPkgOption theCreateActorPkgOption,
			IRPPackage theActorPkgOwner,
			String theActorPkgName,
			IRPPackage theUseCasePkg,
			List<IRPPackage> theOptionalExistingActorPkgs,
			String thePrefixIfInstantiating ){
				
		if( theCreateActorPkgOption == CreateActorPkgOption.CreateNew ){
			
			IRPPackage theActorPkg = 
					createActorPackage( 
							theActorPkgOwner, 
							theActorPkgName, 
							theUseCasePkg );
			
			_actorPkgs.add( theActorPkg );
			
			_actors = addDefaultActorsForUseCaseDiagramTo( 
					theActorPkg,
					true );

		} else if( theCreateActorPkgOption == CreateActorPkgOption.CreateNewButEmpty ){

			IRPPackage theActorPkg = createActorPackage( 
					theActorPkgOwner, 
					theActorPkgName, 
					theUseCasePkg );

			_actorPkgs.add( theActorPkg );

		} else if( theCreateActorPkgOption == CreateActorPkgOption.UseExisting ){
			
			for( IRPPackage theExistingPkg : theOptionalExistingActorPkgs ){
				
				_actorPkgs.add( theExistingPkg );

				_actors.addAll(
						theExistingPkg.getNestedElementsByMetaClass(
						"Actor", 1).toList() );
					
				theUseCasePkg.addDependencyTo( theExistingPkg );
			}	
			
		} else if( theCreateActorPkgOption == CreateActorPkgOption.InstantiateFromExisting ){

			for( IRPModelElement theExistingPkg : theOptionalExistingActorPkgs ){

				IRPPackage theActorPkg = 
						createActorPackage( 
								theActorPkgOwner, 
								theActorPkgName, 
								theUseCasePkg );

				_actorPkgs.add( theActorPkg );

				List<IRPActor> theCandidateActors = 
						theExistingPkg.getNestedElementsByMetaClass(
								"Actor", 1).toList();

				for( IRPActor theCandidateActor : theCandidateActors ){
					theActorPkg.addActor( 
							theCandidateActor.getName() + "_" + 
							thePrefixIfInstantiating );
				}

				//theUseCasePkg.addDependencyTo( theExistingPkg );
			}		
		}
	}

	private IRPPackage createActorPackage(
			IRPPackage underThePackage,
			String withTheName,
			IRPPackage andDependencyTo ){
		
		IRPPackage theActorPkg = underThePackage.addNestedPackage( withTheName );
		theActorPkg.changeTo( StereotypeAndPropertySettings.getActorPackageStereotype( underThePackage ) );
		StereotypeAndPropertySettings.setSavedInSeparateDirectoryIfAppropriateFor( theActorPkg );
		andDependencyTo.addDependencyTo( theActorPkg );
		
		return theActorPkg;
	}
	
	private List<IRPActor> addDefaultActorsForUseCaseDiagramTo(
			IRPPackage thePackage,
			boolean isSkipIfExisting ){
	
		List<IRPActor> theActors = new ArrayList<>();
		
		List<String> theActorNames = 
				StereotypeAndPropertySettings.getDefaultActorsForUseCaseDiagram(
						thePackage );

		for( String theActorName : theActorNames ){
			
			if( isSkipIfExisting ){
				IRPModelElement theExistingActorEl =
						thePackage.getProject().findAllByName( 
								theActorName, "Actor" );
				
				if( theExistingActorEl == null ){
					IRPActor theActor = thePackage.addActor( theActorName );
					theActors.add( theActor );
				} else {
					theActors.add( (IRPActor) theExistingActorEl );
				}
			} else {
				IRPActor theActor = thePackage.addActor( theActorName );
				theActors.add( theActor );
			}
		}
					
		return theActors;
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