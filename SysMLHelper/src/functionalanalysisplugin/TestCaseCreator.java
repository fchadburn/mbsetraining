package functionalanalysisplugin;

import java.util.List;

import com.telelogic.rhapsody.core.*;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;

public class TestCaseCreator {

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

		if( theSelectedEl instanceof IRPSequenceDiagram ){
			createTestCaseFor( (IRPSequenceDiagram) theSelectedEl );			
		}
	}

	public static void createTestCaseFor( IRPSequenceDiagram theSD ){

		Logger.writeLine("createTestCaseFor invoked for " + Logger.elementInfo( theSD ) );

		IRPCollaboration theLogicalCollab = theSD.getLogicalCollaboration();

		@SuppressWarnings("unchecked")
		List<IRPMessage> theMessages = theLogicalCollab.getMessages().toList();

		IRPClass theBuildingBlock = 
				FunctionalAnalysisSettings.getBuildingBlock( theSD );

		if( theBuildingBlock != null ){

			IRPClass theTestBlock = 
					FunctionalAnalysisSettings.getTestBlock( theBuildingBlock );

			IRPOperation theTC = OperationCreator.createTestCaseFor( theTestBlock );

			String theCode = 
					"comment(\"\");\n" +
							"start_of_test();\n";

			List<IRPActor> theActors =
					FunctionalAnalysisSettings.getActors( theBuildingBlock );

			for (IRPMessage theMessage : theMessages) {

				IRPModelElement theSource = theMessage.getSource();
				IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

				if (theInterfaceItem instanceof IRPEvent) {
					Logger.writeLine(theMessage, " was found with source = " + Logger.elementInfo(theSource)
							+ ", and theInterfaceItem=" + Logger.elementInfo(theInterfaceItem));

					IRPEvent theEvent = (IRPEvent) theInterfaceItem;

					String theEventName = theEvent.getName().replaceFirst("req", "send_");

					for (IRPActor theActor : theActors) {

						IRPModelElement theSend = GeneralHelpers.findElementWithMetaClassAndName("Reception",
								theEventName, theActor);

						if (theSend != null) {
							Logger.writeLine("Voila, found " + Logger.elementInfo(theSend) + " owned by "
									+ Logger.elementInfo(theActor));

							IRPLink existingLinkConnectingBlockToActor = ActorMappingInfo
									.getExistingLinkBetweenBaseClassifiersOf(theTestBlock, theActor);

							if (existingLinkConnectingBlockToActor != null) {
								IRPPort theToPort = existingLinkConnectingBlockToActor.getToPort();

								theCode += "OPORT(" + theToPort.getName() + ")->GEN(";
								theCode += theSend.getName() + "(";
								//theCode += theMessage.
								theCode += "));\n";
								theCode += "sleep(4);\n";

							} else {
								Logger.writeLine("No connector found between " + Logger.elementInfo(theTestBlock) + " and "
										+ Logger.elementInfo(theActor));
							}

						} else {
							@SuppressWarnings("unchecked")
							List<IRPClassifier> theBaseClassifiers = theActor.getBaseClassifiers().toList();

							for (IRPClassifier theBaseClassifier : theBaseClassifiers) {

								IRPModelElement theSendAgain = GeneralHelpers.findElementWithMetaClassAndName(
										"Reception", theEventName, theBaseClassifier);

								if (theSendAgain != null) {
									Logger.writeLine("Voila, found " + Logger.elementInfo(theSendAgain)
											+ " owned by " + Logger.elementInfo(theBaseClassifier));

									IRPLink existingLinkConnectingBlockToActor = ActorMappingInfo
											.getExistingLinkBetweenBaseClassifiersOf(theTestBlock, theActor);

									if (existingLinkConnectingBlockToActor != null) {
										IRPPort theToPort = existingLinkConnectingBlockToActor.getToPort();

										theCode += "OPORT(" + theToPort.getName() + ")->GEN(";
										theCode += theSendAgain.getName() + "(";
										theCode += "));\n";
										theCode += "sleep(4);\n";

									} else {
										Logger.writeLine("No connector found between " + Logger.elementInfo(theTestBlock)
												+ " and " + Logger.elementInfo(theActor));
									}
								}

							}
						}
					}
				}
			}

			theCode += "end_of_test();\n";

			theTC.setBody(theCode);
			theTC.highLightElement();
		}
	}
}

/**
 * Copyright (C) 2017-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #230 20-SEP-2017: Initial alpha trial for create test case script from a sequence diagram (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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
