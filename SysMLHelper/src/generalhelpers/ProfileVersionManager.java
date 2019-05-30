package generalhelpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class ProfileVersionManager {

	public static void main(String[] args) {
		
		ConfigurationSettings configSettings = new ConfigurationSettings(
				"OrchidDesign.properties", 
				"OrchideDesign_MessagesBundle" );
		
		checkAndSetProfileVersion( true, configSettings, false );
	}
	
	/**
	 * @param args
	 */
	private static Date getDate( 
			String fromString ){

		SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Date date = null;

		try {
			date = parser.parse( fromString );

		} catch( ParseException e ){
			Logger.writeLine( "Exception in getDate trying to parse date" );
		}

		return date;
	}


	public static boolean checkAndSetProfileVersion(
			boolean withUserMsg,
			ConfigurationSettings theConfigSettings,
			boolean isChangeToSysML ){

		List<?> theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();

		boolean isContinue = true;
		
		if( theAppIDs.size() == 1 ){

			IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			IRPProject theRhpPrj = theRhpApp.activeProject();

			if( isChangeToSysML ){
				
				// check that SysML profile is applied
				IRPStereotype theProjectNewTerm = theRhpPrj.getNewTermStereotype();
							
				if( theProjectNewTerm == null ){
					
					isContinue = UserInterfaceHelpers.askAQuestion(
							"The SysML profile is not applied on " +
							Logger.elementInfo( theRhpPrj ) + "\nDo you want to proceed?");
					
					if( isContinue ){
						
						if( theRhpPrj != null && theRhpPrj.isReadOnly()==1 ){
							Logger.writeLine( "Error, checkAndSetProfileVersion was unable to change project to SysML");
						} else {
							theRhpPrj.changeTo( "SysML" );
						}
					}
				}
			}

			
			if( isContinue ){
				IRPStereotype theStereotype = 
						GeneralHelpers.getExistingStereotype( 
								"ProfileInfo", 
								theRhpPrj );

				if( theStereotype != null ){

					performCheckBetweenProjectAndStereotype(
							withUserMsg, 
							theRhpPrj,
							theStereotype,
							theConfigSettings );
				}				
			}
			
		} else  {
			Logger.writeLine("Error, unable to perform check as " + theAppIDs.size() + " Rhapsody's were detected");
		}
		
		return isContinue;
	}
	
	private static void performCheckBetweenProjectAndStereotype(
			boolean withUserMsg, 
			IRPProject theRphPrj,
			IRPStereotype theStereotype,
			ConfigurationSettings theConfigSettings ) {
				
		IRPTag theProjectDateTag = theRphPrj.getTag( "ProfileDate" );
		IRPTag theProfileDateTag = theStereotype.getTag( "ProfileDate" );
		IRPTag theProjectVersionTag = theRphPrj.getTag( "ProfileVersion" );
		IRPTag theProfileVersionTag = theStereotype.getTag( "ProfileVersion" );
//		IRPTag theProjectPropertyTag = theRphPrj.getTag( "ProfileProperties" );
		IRPTag theProfilePropertyTag = theStereotype.getTag( "ProfileProperties" );
		
		boolean isProjectsProfileVersionSet = 
				( theProjectDateTag != null && 
				theProjectDateTag.getOwner().equals( theRphPrj ) ) &&
				( theProjectVersionTag != null &&
				theProjectVersionTag.getOwner().equals( theRphPrj ) );

		if( isProjectsProfileVersionSet ){
			Logger.writeLine( Logger.elementInfo( theProjectDateTag ) + 
					" owner is " + Logger.elementInfo( theProjectDateTag.getOwner() ) );

		} else {
			Logger.info( "performCheckBetweenProjectAndStereotype has detected that there is no project date tag");
		}

		boolean isProfileVersionAvailable = 
				theProfileDateTag != null &&
				theProfileVersionTag != null;

		if( isProfileVersionAvailable ){

			String theProfileDateValue = theProfileDateTag.getValue();
			String theProfileVersionValue = theProfileVersionTag.getValue();

			Logger.writeLine( "Current ProfileDate    = " + theProfileDateValue );
			Logger.writeLine( "Current ProfileVersion = " + theProfileVersionValue );

			Date theProfileDate = getDate( theProfileDateValue );

			if( theProfileDate == null ){

				Logger.writeLine( "Error, theProfileDate could not be parsed" );

			} else {

				if( isProjectsProfileVersionSet ){

					String theProjectsProfileDateValue = theProjectDateTag.getValue();
					String theProjectsProfileVersionValue = theProjectVersionTag.getValue();

					Logger.writeLine( "Project's required ProfileDate    = " + theProjectsProfileDateValue );
					Logger.writeLine( "Project's required ProfileVersion = " + theProjectsProfileVersionValue );

					Date theProjectsProfileDate = getDate( theProjectsProfileDateValue );

					if( theProjectsProfileDate == null ){

						Logger.writeLine( "Error, theProjectsProfileDate could not be parsed" );

					} else {

						if( theProjectsProfileDate.after( theProfileDate ) ){

							String theMsg = "An upgraded profile is needed. \n" +
									"The current profile version you have installed is " + theProfileVersionValue + 
									" (" + theProfileDateValue + ")\n" +
									"The project called " + theRphPrj.getName() + 
									" is suggesting it needs " + theProfileVersionValue + 
									" (" + theProjectsProfileDateValue + ") \n";

							if( withUserMsg ){
								UserInterfaceHelpers.showWarningDialog( theMsg );
							} else {
								Logger.writeLine( theMsg );
							}

						} else if( theProjectsProfileDate.equals( theProfileDate ) ){

							String theMsg = "No upgrade needed. The profile and project's profile dates are matching. \n" +
									"The current profile version you have installed is " + theProfileVersionValue + 
									" (" + theProfileDateValue + ") \n" +
									"The project called " + theRphPrj.getName() + 
									" is suggesting it needs " + theProfileVersionValue + 
									" (" + theProjectsProfileDateValue + ") \n";

							if( withUserMsg ){
								UserInterfaceHelpers.showInformationDialog( theMsg );
							} else {
								Logger.writeLine( theMsg );
							}

						} else if( theProjectsProfileDate.before( theProfileDate ) ){

							String theMsg = "No upgrade needed. Your profile is newer that project's required profile date. \n\n" +
									"The current profile version you have installed is " + theProfileVersionValue + 
									" (" + theProfileDateValue + ") \n" +
									"The project called " + theRphPrj.getName() + 
									" is suggesting it needs " + theProfileVersionValue + 
									" (" + theProjectsProfileDateValue + ") \n\n" + 
									"Do you want to set the project to require the more up-to-date profile \n" +
									"that you have installed? \n";;

							boolean isSetTags = true;
							
							if( isSetTags ){
								
								if( theRphPrj.isReadOnly()==1 ){
								
									Logger.writeLine("Error, unable to set tags as project is read-only");
									
								} else {

									Logger.writeLine("Setting " + Logger.elementInfo( theProfileDateTag ) + " on " + 
											Logger.elementInfo( theStereotype ) + " to " + theProfileDateValue );

									theRphPrj.setTagValue( theProfileDateTag, theProfileDateValue );

									Logger.writeLine("Setting " + Logger.elementInfo( theProfileVersionTag ) + " on " + 
											Logger.elementInfo( theStereotype ) + " to " + theProfileVersionValue );

									theRphPrj.setTagValue( theProfileVersionTag, theProfileVersionValue );
								}
							}				
						}
					}

				} else {

					String theMsg = "The project called " + theRphPrj.getName() + " does not have the ProfileDate and ProfileVersion tags set \n" +
							"Your installed profile version is " + theProfileVersionValue + 
							" (" + theProfileDateValue + ") \n\n" +
							"Do you want to set the project to require this version? \n";

					boolean isSetTags = true;

					if( withUserMsg ){								
						isSetTags = UserInterfaceHelpers.askAQuestion( theMsg );
					}

					if( isSetTags ){
						
						if( theRphPrj.isReadOnly()==1 ){
							
							Logger.writeLine("Error, unable to set tags as project is read-only");
							
						} else {
							Logger.writeLine("Setting " + Logger.elementInfo( theProfileDateTag ) + " on " + 
									Logger.elementInfo( theStereotype ) + " to " + theProfileDateValue );

							theRphPrj.setTagValue( theProfileDateTag, theProfileDateValue );

							Logger.writeLine("Setting " + Logger.elementInfo( theProfileVersionTag ) + " on " + 
									Logger.elementInfo( theStereotype ) + " to " + theProfileVersionValue );

							theRphPrj.setTagValue( theProfileVersionTag, theProfileVersionValue );
							
							if( theProfilePropertyTag != null ){
								String thePropertyConfigName = theProfilePropertyTag.getValue().trim();
								
								if( !thePropertyConfigName.isEmpty() ){
									
									// set the properties							    	
							    	theConfigSettings.setPropertiesValuesRequestedInConfigFile( 
							    			theRphPrj,
							    			thePropertyConfigName );
							    	
									theRphPrj.setTagValue( theProfilePropertyTag, thePropertyConfigName );
								}
							}
						}
					}
				}
			}

		} else {
			Logger.writeLine("Warning, could not find ProfileDate and/or ProfileVersion tag in profile");
		}
	}

}
