package requirementsanalysisplugin;

public class RequirementsAnalysisSettings {
	
//	private static String m_PropertyPrefix = "SysMLHelper.RequirementsAnalysis.";
			
//	private static IRPStereotype getStereotypeToUse(
//			IRPModelElement basedOnContext,
//			String basedOnTagName ){
//		
//		IRPStereotype theStereotype = null;
//		
//		String theStereotypeName = 
//				basedOnContext.getPropertyValue( 
//						m_PropertyPrefix + basedOnTagName );
//		
//		if( theStereotypeName != null ){
//			
//			theStereotype = GeneralHelpers.getExistingStereotype( 
//					theStereotypeName, 
//					basedOnContext.getProject() );
//			
//			if( theStereotype == null ){
//				Logger.writeLine( "Error in getStereotypeForActionTracing, no Stereotyped called " + theStereotypeName + " was found" );
//
//				//theStereotype = selectAndPersistStereotype( basedOnContext.getProject(), thePkg, theTag );
//
//			} else {				
//				Logger.writeLine( "Using " + Logger.elementInfo( theStereotype ) + " for " + basedOnTagName );
//			}
//		} else { // theStereotypeName == null, legacy models
//			
//			IRPPackage theSettingsPkg = getRequirementsAnalysisPkgBasedOn( basedOnContext );
//
//			if( theSettingsPkg == null ){
//				
//				Logger.writeLine("Error in getStereotypeToUseForActions, unable to find actions");
//				
//			} else {
//				IRPTag theTag = theSettingsPkg.getTag( basedOnTagName );
//			
//				if( theTag == null ){
//					
//					Logger.writeLine("Warning in getStereotypeToUseForActions for getStereotypeInbasedOnTagName=" + basedOnTagName + 
//						", no tag called " + basedOnTagName + " was found" );				
//					
//					//theTag = (IRPTag) thePkg.addNewAggr( "Tag", basedOnTagName );
//					//theStereotype = selectAndPersistStereotype( theProject, thePkg, theTag );
//					
//				} else { // tag is not null
//					
//					String theValue = theTag.getValue();
//					
//					Logger.writeLine( "Read value of " + theValue + " from " + Logger.elementInfo( theTag ) );
//
//					theStereotype = GeneralHelpers.getExistingStereotype( 
//							theValue, basedOnContext.getProject() );
//					
//					if( theStereotype == null ){
//						Logger.writeLine( "Error in getStereotypeForActionTracing, no Stereotyped called " + theValue + " was found" );
//
//						//theStereotype = selectAndPersistStereotype( basedOnContext.getProject(), thePkg, theTag );
//
//					} else {				
//						Logger.writeLine( "Using " + Logger.elementInfo( theStereotype ) + " for action tracing" );
//					}
//				}
//			}
//		}
//
//		return theStereotype;
//	}
	
//	private static IRPPackage getRequirementsAnalysisPkgBasedOn(
//			IRPModelElement theSelectedEl ){
//
//		IRPPackage theSettingsPkg = null;
//
//		// try and find the RequirementsAnalysisPkg
//		IRPModelElement theReqtsAnalysisPkg = 
//				theSelectedEl.getProject().findElementsByFullName( 
//						"RequirementsAnalysisPkg", "Package" );
//
//		if( theReqtsAnalysisPkg != null && 
//				theReqtsAnalysisPkg instanceof IRPPackage ){
//
//			theSettingsPkg = (IRPPackage) theReqtsAnalysisPkg;
//		}
//
//		return theSettingsPkg;
//	}
	
//	private static IRPPackage getUseCaseSettingsPackageBasedOn(
//			IRPModelElement theContextEl ){
//		
//		IRPPackage theSettingsPkg = null;
//		
//		if( theContextEl instanceof IRPProject ){
//			
//			List<IRPModelElement> thePackageEls = 
//					GeneralHelpers.findElementsWithMetaClassAndStereotype(
//							"Package", 
//							StereotypeSettings.getUseCasePackageStereotype(), 
//							theContextEl.getProject(), 
//							1 );
//			
//			if( thePackageEls.isEmpty() ){
//				Logger.writeLine( "Warning in getUseCaseSettingsPackageBasedOn, unable to find use case settings package");
//				
//			} else if( thePackageEls.size()==1){
//			
//				theSettingsPkg = (IRPPackage) thePackageEls.get(0);
//				
//			} else {
//				Logger.writeLine( "Error in getUseCaseSettingsPackageBasedOn, unable to find use case settings package");
//
//				IRPModelElement theUserSelectedPkg = 
//						UserInterfaceHelpers.launchDialogToSelectElement(thePackageEls, "Choose which settings to use", true);
//				
//				if( theUserSelectedPkg != null ){
//					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
//				}
//			}
//			
//		} else if( theContextEl instanceof IRPPackage &&
//			GeneralHelpers.hasStereotypeCalled(
//					StereotypeSettings.getUseCasePackageStereotype(), 
//					theContextEl ) ){
//			
//			Logger.writeLine( "getUseCaseSettingsPackageBasedOn, is returning " + Logger.elementInfo( theContextEl ) );
//
//			theSettingsPkg = (IRPPackage) theContextEl;
//		
//		} else {
//
//			// recurse
//			theSettingsPkg = getUseCaseSettingsPackageBasedOn(
//					theContextEl.getOwner() );
//		}
//		
//		return theSettingsPkg;
//	}
	
//	public static String getCreateRequirementTextForPrefixing(
//			IRPModelElement basedOnContextEl ){
//		
//		String theText = "The feature shall ";
//		
//		final String tagName = "createRequirementTextForPrefixing";	
//		
//		String thePropertyValue = 
//				basedOnContextEl.getPropertyValue( 
//						m_PropertyPrefix + tagName );
//
//		if( thePropertyValue != null ){
//			theText = thePropertyValue;
//			
//		} else { // legacy support
//			IRPPackage theReqtsAnalysisPkg = 
//					getRequirementsAnalysisPkgBasedOn( basedOnContextEl );
//			
//			if( theReqtsAnalysisPkg == null ){
//				
//				Logger.writeLine( "Error in getCreateRequirementTextForPrefixing, " +
//						"no settings package based on " + 
//						Logger.elementInfo( basedOnContextEl ) + " was found");
//			} else {
//				
//				IRPTag theTag = theReqtsAnalysisPkg.getTag( tagName );
//				
//				if (theTag != null){
//					theText = theTag.getValue();
//					
//					//#005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
//					theText = theText.replaceAll("ProjectName", basedOnContextEl.getProject().getName());
//				} else {
//					Logger.writeLine("Warning in getCreateRequirementTextForPrefixing, no tag called " + tagName + " was found so creating one");	
//					IRPTag theNewTag = (IRPTag) theReqtsAnalysisPkg.addNewAggr("Tag", tagName);
//					theReqtsAnalysisPkg.setTagValue(theNewTag, theText);
//				}
//			}
//		}
//		
//		return theText;
//	}
}
