package generalhelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.telelogic.rhapsody.core.*;
 
public class GeneralHelpers {
 
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theRhpApp.getSelectedGraphElements().toList();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			dumpGraphicalPropertiesFor(theGraphEl);
		}
	}
	
	// for test only
	public static void dumpGraphicalPropertiesFor(IRPGraphElement theGraphEl){
	 
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		Logger.writeLine("---------------------------");
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			
			Logger.writeLine(theGraphicalProperty.getKey() + "=" + theGraphicalProperty.getValue());
		}
		Logger.writeLine("---------------------------"); 
	}
	
	public static boolean isLegalName(String theName){
		
		String regEx = "^(([a-zA-Z_][a-zA-Z0-9_]*)|(operator.+))$";
		
		boolean isLegal = theName.matches( regEx );
		
		if (!isLegal){
			Logger.writeLine("Warning, detected that " + theName 
					+ " is not a legal name as it does not conform to the regex=" + regEx);
		}
		
		return isLegal;
	}
	
	public static String toLegalClassName(String theInput) {
		
		StringBuilder nameBuilder = new StringBuilder(theInput.length());    

		boolean capitalizeNextChar = true;

		int n = 1;
		final int max = 40;
		
		for (char c:theInput.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)){
				if (capitalizeNextChar) {
					nameBuilder.append(Character.toUpperCase(c));
				} else {
					if (n==1){
						nameBuilder.append(Character.toLowerCase(c));
					} else {
						nameBuilder.append(c);
					}

				}
				capitalizeNextChar = false;
			} else if (Character.isSpaceChar(c)){
				
				if (n<max){
					capitalizeNextChar = true;
					continue;
				} else {
					break;
				}
			}
			n++;
		}
		
		return nameBuilder.toString();
	}
	
	public static String toMethodName(String theInput) {
		
		StringBuilder nameBuilder = new StringBuilder(theInput.length());    

		boolean capitalizeNextChar = false;

		int n = 1;
		final int max = 40;
		
		for (char c:theInput.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)){
				if (capitalizeNextChar) {
					nameBuilder.append(Character.toUpperCase(c));
				} else {
					if (n==1){
						nameBuilder.append(Character.toLowerCase(c));
					} else {
						nameBuilder.append(c);
					}

				}
				capitalizeNextChar = false;
			} else if (Character.isSpaceChar(c)){
				
				if (n<max){
					capitalizeNextChar = true;
					continue;
				} else {
					break;
				}
			}
			n++;
		}
		
		return nameBuilder.toString();
	}

	public static String decapitalize(final String line){
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toLowerCase(line.charAt(0)) + line.substring(1);
		} else {
			theResult = line;
		}
		
		return theResult;	
	}
	
	public static String capitalize(final String line) {
		
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toUpperCase(line.charAt(0)) + line.substring(1);
		} else {
			Logger.writeLine("Error in capitalize");
			theResult = line;
		}
		
		return theResult;
	}
	
	public static String getActionTextFrom(
			IRPModelElement theEl) {
		
		String theSourceInfo = null;
		
		if (theEl instanceof IRPState){
			IRPState theState = (IRPState)theEl;
			String theStateType = theState.getStateType();
			
			if (theStateType.equals("Action")){
				theSourceInfo = theState.getEntryAction();
				
			} else if (theStateType.equals("AcceptEventAction")){ // receive event
				
				IRPAcceptEventAction theAcceptEventAction = (IRPAcceptEventAction)theEl;
				IRPEvent theEvent = theAcceptEventAction.getEvent();
				
				if (theEvent==null){
					Logger.writeLine("Event has no name so using Name");
					theSourceInfo = theState.getName();
				} else {
					theSourceInfo = theEvent.getName();
				}
				
			} else if (theStateType.equals("EventState")){ // send event
				
				IRPSendAction theSendAction = theState.getSendAction();
				
				if (theSendAction != null){
					IRPEvent theEvent = theSendAction.getEvent();
					
					if (theEvent != null){
						theSourceInfo = theEvent.getName();
					} else {
						Logger.writeLine("SendAction has no Event so using Name of action");
						theSourceInfo = theState.getName();
					}
				} else {
					Logger.writeLine("Error in deriveDownstreamRequirement, theSendAction is null");
				}	
				
			} else if (theStateType.equals("TimeEvent")){
				
				IRPAcceptTimeEvent theAcceptTimeEvent = (IRPAcceptTimeEvent)theEl;
				String theDuration = theAcceptTimeEvent.getDurationTime();
				
				if (theDuration.isEmpty()){
					theSourceInfo = theAcceptTimeEvent.getName();
				} else {
					theSourceInfo = theDuration;
				}
				
			} else {
				Logger.writeLine("Warning in getActionTextFrom, " + theStateType + " was not handled");
			}
			
		} else if (theEl instanceof IRPTransition){
			
			IRPTransition theTrans = (IRPTransition)theEl;
			IRPGuard theGuard = theTrans.getItsGuard();
			
			// check that transition has a guard before trying to use it
			if( theGuard != null ){
				theSourceInfo = ((IRPTransition) theEl).getItsGuard().getBody();
			} else {
				theSourceInfo = "TBD"; // no source info available
			}
			
		} else if (theEl instanceof IRPComment){
			
			theSourceInfo = theEl.getDescription();

		} else if (theEl instanceof IRPRequirement){
			
			IRPRequirement theReqt = (IRPRequirement)theEl;
			theSourceInfo = theReqt.getSpecification();

		} else if (theEl instanceof IRPConstraint){
			
			IRPConstraint theConstraint = (IRPConstraint)theEl;
			theSourceInfo = theConstraint.getSpecification();		

		} else {
			Logger.writeLine("Warning in getActionTextFrom, " + Logger.elementInfo(theEl) + " was not handled as of an unexpected type");
			theSourceInfo = ""; // default
		}
		
		if( theSourceInfo != null ){
			
			if( theSourceInfo.isEmpty() ){
				Logger.writeLine("Warning, " + Logger.elementInfo( theEl ) + " has no text");
			} else {
				theSourceInfo = decapitalize( theSourceInfo );
			}
		}
		
		return theSourceInfo;
	}
	
	public static IRPModelElement launchDialogToSelectElement(
			List<IRPModelElement> inList, 
			String messageToDisplay, 
			Boolean isFullPathRequested){
		
		IRPModelElement theEl = null;

		List<String> nameList = new ArrayList<String>();

		nameList.add("Nothing");

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

		if( selectedElementName != null && 
			!selectedElementName.equals("Nothing") ){

			int index = nameList.indexOf(selectedElementName);
			theEl = inList.get(index);
			Logger.writeLine(theEl, "was chosen");

		} else {
			Logger.writeLine("'Nothing' was chosen by user");
			theEl = null;
		}

		return theEl;
	}
	
	public static IRPStereotype applyExistingStereotype(
			String withTheName, 
			IRPModelElement toTheEl ){
		
		IRPStereotype theChosenStereotype = 
				getExistingStereotype( withTheName, toTheEl.getProject() );
		
		if( theChosenStereotype != null ){
			toTheEl.setStereotype( theChosenStereotype );
		} else {
			Logger.writeLine("Warning in applyExistingStereotype, unable to find stereotype <<" + 
					withTheName + ">> underneath " + Logger.elementInfo( toTheEl.getProject() ) );
		}
		
		return theChosenStereotype;
	}
	
	public static IRPStereotype getExistingStereotype(
			String withTheName,
			IRPModelElement underneathTheEl ){

		List<IRPModelElement> theStereotypeEls = 
				GeneralHelpers.findElementsWithMetaClassAndName(
						"Stereotype", withTheName, underneathTheEl );
		
		IRPStereotype theChosenStereotype = null;
		boolean isNewTermFound = false;
		
		for( IRPModelElement theStereotypeEl : theStereotypeEls ){
			
			IRPStereotype theStereotype = (IRPStereotype)theStereotypeEl;
			
			// Favour new term stereotypes
			if( theStereotype.getIsNewTerm()==1 ){
				isNewTermFound = true;
				theChosenStereotype = theStereotype;
				
				if( theStereotypeEls.size() > 1 ){
					
					Logger.writeLine("getExistingStereotype has chosen " + Logger.elementInfo( theStereotype ) + 
							" as it is a new term (there were x" + 
							theStereotypeEls.size() + " stereotypes with the same name)");
				}
			} else if( !isNewTermFound ){
				theChosenStereotype = theStereotype;
			}
		}
		
		if( theChosenStereotype == null ){
			Logger.writeLine("Warning: Unable to find a stereotype with name " + withTheName + " in getExistingStereotype");
		}
		
		return theChosenStereotype;
	}
	
	public static IRPStereotype getStereotypeIn(
			IRPProject theProject, 
			String basedOnTagName, 
			String ownedByPackageName ){
		
		IRPStereotype theStereotype = null;
		
		IRPModelElement thePkg = 
				theProject.findElementsByFullName( 
						ownedByPackageName, "Package" );
		
		if( thePkg == null ){
			
			Logger.writeLine("Error in getStereotypeIn for basedOnTagName=" + basedOnTagName + 
					", ownedByPackageName=" + ownedByPackageName + ", no " + ownedByPackageName + " was found" );
			
		} else {
			
			IRPTag theTag = thePkg.getTag( basedOnTagName );
			
			if( theTag == null ){
				
				Logger.writeLine("Warning in getStereotypeIn for basedOnTagName=" + basedOnTagName + 
					", ownedByPackageName=" + ownedByPackageName + ", no tag called " + basedOnTagName + " was found" );				
				
				theTag = (IRPTag) thePkg.addNewAggr( "Tag", basedOnTagName );
				theStereotype = selectAndPersistStereotype( theProject, thePkg, theTag );
				
			} else { // tag is not null
				
				String theValue = theTag.getValue();
				
				Logger.writeLine( "Read value of " + theValue + " from " + Logger.elementInfo( theTag ) );

				theStereotype = getExistingStereotype( theValue, theProject );
				
				if( theStereotype == null ){
					Logger.writeLine( "Error in getStereotypeForActionTracing, no Stereotyped called " + theValue + " was found" );

					theStereotype = selectAndPersistStereotype( theProject, thePkg, theTag );

				} else {				
					Logger.writeLine( "Using " + Logger.elementInfo( theStereotype ) + " for action tracing" );
				}
			}
		}
		
		return theStereotype;
	}
	
	private static IRPStereotype selectAndPersistStereotype(
			IRPProject inTheProject, 
			IRPModelElement theReqtsAnalysisPkg, 
			IRPTag theTag) {

		IRPStereotype theStereotype = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theStereotypes = inTheProject.getNestedElementsByMetaClass("Stereotype", 1).toList();

		if( theStereotypes.isEmpty() ){
			Logger.writeLine("Error in selectAndPersistStereotype, there are no stereotypes in project");
		} else {
			IRPModelElement theSelectedEl = 
					GeneralHelpers.launchDialogToSelectElement(
							theStereotypes, "Pick a stereotype for " + Logger.elementInfo( theTag ), true);

			if( theSelectedEl != null && theSelectedEl instanceof IRPStereotype ){
				
				theReqtsAnalysisPkg.setTagValue( theTag, theSelectedEl.getName() );
				theStereotype = (IRPStereotype)theSelectedEl;
			}
		}

		return theStereotype;
	}
	
	public static IRPStereotype getStereotypeCalled(
			String theName, 
			IRPModelElement onTheEl){
		
		int count = 0;
		IRPStereotype theFoundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();
		
		for (IRPStereotype theStereotype : theStereotypes) {
			if (theStereotype.getName().equals(theName)){
				
				theFoundStereotype = theStereotype;
				count++;
			}
		}
		
		if (count > 1){
			Logger.writeLine("Warning in getStereotypeCalled, found " + count 
					+ " elements that are called " + theName);
		}
		
		return theFoundStereotype;
	}
	
	public static Boolean hasStereotypeCalled(
			String theName, 
			IRPModelElement onTheEl){
		
		Boolean isFound = false;
		
		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();
		
		for (IRPStereotype theStereotype : theStereotypes) {
			if (theStereotype.getName().equals(theName)){
				isFound = true;
			}
		}
		
		return isFound;
	}
	
	public static IRPModelElement findElementWithMetaClassAndName(
			String theMetaClass, 
			String andName, 
			IRPModelElement underneathTheEl){
		
		IRPModelElement theElement = null;
		
		List <IRPModelElement> theMatches = findElementsWithMetaClassAndName(
				theMetaClass, andName, underneathTheEl);

		if (theMatches.size()==1){
		
			theElement = theMatches.get(0);
		
		} else if (theMatches.size()>1){
			
			Logger.writeLine("Warning in findElementWithMetaClassAndName(" + theMetaClass + "," + 
					andName + ","+Logger.elementInfo(underneathTheEl)+"), " + theMatches.size() + 
					" elements were found when I was expecting only one");
			
			theElement = theMatches.get(0);
		}
		
		return theElement;
	}

	public static IRPModelElement findNestedElementUnder( 
			IRPClassifier theElement,
			String withName,
			String andMetaClass,
			boolean isIncludeBases ){
		
		IRPModelElement theNestedElement = 
				theElement.findNestedElement( withName, andMetaClass );
		
		if( theNestedElement == null && isIncludeBases ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theBaseClassifiers = 
					theElement.getBaseClassifiers().toList();
		
			for( IRPModelElement theBaseClassifier : theBaseClassifiers ) {
				
				theNestedElement = findNestedElementUnder( 
						(IRPClassifier) theBaseClassifier, 
						withName, 
						andMetaClass, 
						isIncludeBases );
				
				if( theNestedElement != null ){
					break;
				}
			}
		}
		
		return theNestedElement;
	}
		
	public static List<IRPModelElement> findElementsWithMetaClassAndName(
			String theMetaClass, 
			String andName, 
			IRPModelElement underneathTheEl){
		
		List<IRPModelElement> theElements = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List <IRPModelElement> theCandidates = 
				underneathTheEl.getNestedElementsByMetaClass(theMetaClass, 1).toList();
		
		for (IRPModelElement theCandidate : theCandidates) {
			if (theCandidate.getName().equals( andName )){
				theElements.add( theCandidate );
			}
		}
				
		return theElements;
	}
	
	public static List<IRPModelElement> findElementsWithMetaClassAndStereotype(
			String theMetaClass, 
			String andStereotype, 
			IRPModelElement underneathTheEl,
			int recursive ){
		
		List <IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List <IRPModelElement> theCandidates = 
				underneathTheEl.getNestedElementsByMetaClass(theMetaClass, recursive).toList();

		for (IRPModelElement theCandidate : theCandidates) {
			
			if (hasStereotypeCalled(andStereotype, theCandidate)){
				theFilteredList.add(theCandidate);
			}
		}
		
		return theFilteredList;
	}
	
	public static List<IRPModelElement> findElementsWithMetaClassStereotypeAndName(
			String theMetaClass, 
			String andStereotype, 
			String andName, 
			IRPModelElement underneathTheEl){
		
		List <IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		List <IRPModelElement> theCandidates = 
				findElementsWithMetaClassAndName(
						theMetaClass, andName, underneathTheEl );

		for (IRPModelElement theCandidate : theCandidates) {
			
			if (hasStereotypeCalled(andStereotype, theCandidate)){
				theFilteredList.add(theCandidate);
			}
		}
		
		return theFilteredList;
	}
	
	public static String promptUserForTextEntry(
			String withTitle, 
			String andQuestion, 
			String andDefault, 
			int size){
		
		String theEntry = andDefault;
		
		JPanel panel = new JPanel();
		
		panel.add(new JLabel(andQuestion));
		
		JTextField theTextField = new JTextField(size);
		panel.add( theTextField );
		
		if (!andDefault.isEmpty())
			theTextField.setText(andDefault);
		
		int choice = JOptionPane.showConfirmDialog(null, panel, withTitle, JOptionPane.OK_CANCEL_OPTION);
		
		if( choice==JOptionPane.OK_OPTION ){
			String theTextEntered = theTextField.getText(); 
			
			if (!theTextEntered.isEmpty()){
				theEntry = theTextField.getText();
			} else {
				Logger.writeLine("No text was entered, using default response of '" + andDefault + "'");
			}
		}
		
		return theEntry;
	}
	
	public static IRPStereotype getStereotypeAppliedTo(
			IRPModelElement theElement, 
			String thatMatchesRegEx ){
		
		IRPStereotype foundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStereotype> theCandidateStereotypes = theElement.getStereotypes().toList();
		List<IRPStereotype> theMatchingStereotypes = new ArrayList<IRPStereotype>();
		
		for( IRPStereotype theCandidateStereotype : theCandidateStereotypes ){
			
			String theName = theCandidateStereotype.getName();
			
			if( theName.matches( thatMatchesRegEx ) ){

				theMatchingStereotypes.add( theCandidateStereotype );
			}		
		}
		
		int count = theMatchingStereotypes.size();
		
		if( count == 1 ){
			
			foundStereotype = theMatchingStereotypes.get( 0 );
			
		} else if( count > 1 ){
			
			Logger.writeLine(
					"Warning in getStereotypeAppliedTo, there are multiple stereotypes related to " + 
					Logger.elementInfo(theElement) + " size=" + theMatchingStereotypes.size() + 
					"matching regex=" + thatMatchesRegEx );
			
			foundStereotype = theMatchingStereotypes.get( 0 );
		} 
		
		return foundStereotype;
	}
	
	public static boolean askQuestion(
			String question){
		
		boolean isYes = false;
		
		int answer = javax.swing.JOptionPane.showConfirmDialog(
				null, 
				question, 
				null, 
				javax.swing.JOptionPane.YES_NO_OPTION);
		
		if (answer == javax.swing.JOptionPane.YES_OPTION){
			isYes = true;
		}
		
		return isYes;
	}
	
	public static List<IRPActivityDiagram> buildListOfActivityDiagramsFor(
			List<IRPModelElement> theSelectedEls) {
		
		List<IRPActivityDiagram> theADs = new ArrayList<IRPActivityDiagram>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			@SuppressWarnings("unchecked")
			List<IRPActivityDiagram> theCandidates = theSelectedEl.getNestedElementsByMetaClass("ActivityDiagramGE", 1).toList();
			
			for (IRPActivityDiagram theCandidate : theCandidates) {
				if (!theADs.contains(theCandidate)){
					
					if (theCandidate.isReadOnly()==0){
						theADs.add(theCandidate);			
						Logger.writeLine("Adding " + Logger.elementInfo(theCandidate.getOwner()) + " to the list");
					} else {
						Logger.writeLine("Skipping " + Logger.elementInfo(theCandidate.getOwner()) + " as it is read-only");
					}
				}
			}
		}
		return theADs;
	}
	
	public static Set<IRPModelElement> findModelElementsIn(
			List<IRPGraphElement> theGraphElementList, 
			String withMetaClass){
		
		Set<IRPModelElement> theFilteredSet = new HashSet<IRPModelElement>();
		
		for (IRPGraphElement theGraphEl : theGraphElementList) {
			
			IRPModelElement theEl = theGraphEl.getModelObject();

			if (theEl != null && theEl.getMetaClass().equals( withMetaClass )){
				theFilteredSet.add( theEl );
			}
		}
		
		return theFilteredSet;
	}
	
	public static List<IRPModelElement> findElementsIn(
			List<IRPModelElement> theModelElementList, 
			String withMetaClass){
		
		List<IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		for (IRPModelElement theEl : theModelElementList) {

			if (theEl.getMetaClass().equals( withMetaClass )){
				theFilteredList.add( theEl );
			}
		}
		
		return theFilteredList;
	}
	
	public static boolean doUnderlyingModelElementsIn(
			List<IRPGraphElement> theGraphElementList, 
            String haveTheMetaClass){
		
		boolean result = true;
		
		for (IRPGraphElement theGraphEl : theGraphElementList) {
			IRPModelElement theEl = theGraphEl.getModelObject();

			if (theEl != null && !theEl.getMetaClass().equals( haveTheMetaClass )){
				result = false;
			}
		}
		
		return result;
	}

	public static Set<IRPModelElement> findModelElementsNestedUnder(
			IRPModelElement rootEl, 
			String ofMetaClass, 
			String withStereotypeMatchingRegEx){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();
		
		Set<IRPModelElement> theFound = new LinkedHashSet<IRPModelElement>();
		
		for (IRPModelElement theEl : theCandidateEls) {
			
			IRPStereotype theStereotype = getStereotypeAppliedTo( theEl, withStereotypeMatchingRegEx );
			
			if( theStereotype != null ){
				// don't add if element is under the profile.
				if (!checkIsNestedUnderAProfile( theEl )){
					theFound.add( theEl );
				}
			}			
		}
		
		return theFound;
	}

	public static List<IRPModelElement> findModelElementsWithoutStereotypeNestedUnder(
			IRPModelElement rootEl, 
			String ofMetaClass, 
			String withStereotypeMatchingRegEx){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();
		List<IRPModelElement> theFound = new ArrayList<IRPModelElement>();
		
		for (IRPModelElement theEl : theCandidateEls) {
			
			IRPStereotype theStereotype = getStereotypeAppliedTo(theEl, withStereotypeMatchingRegEx);
			
			if (theStereotype==null){
				theFound.add(theEl);
			}			
		}
		
		return theFound;
	}
	
	public static void applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
			IRPModelElement theReqt, 
			IRPStereotype theStereotypeToApply ) {
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theReqt.getDependencies().toList();
		
		for (IRPDependency theDependency : theDependencies) {
						
			IRPStereotype theExistingGatewayStereotype = 
					GeneralHelpers.getStereotypeAppliedTo( theDependency, "from.*" );
			
			if (theExistingGatewayStereotype == null && 
					GeneralHelpers.hasStereotypeCalled("deriveReqt", theDependency)){
							
				Logger.writeLine("Applying " + Logger.elementInfo(theStereotypeToApply) + " to " + Logger.elementInfo(theDependency));
				theDependency.setStereotype(theStereotypeToApply);
				theDependency.changeTo("Derive Requirement");
			}
		}
	}
	
	public static boolean checkIsNestedUnderAProfile(
			IRPModelElement theElementToCheck){
		
		boolean isUnderAProfile = false;
		
		IRPModelElement theOwner = theElementToCheck.getOwner();
		
		if (theOwner!=null){
			
			if (theOwner instanceof IRPProfile){
				isUnderAProfile = true;
			} else {
				isUnderAProfile = checkIsNestedUnderAProfile( theOwner );
			}
		}
		
		return isUnderAProfile;
	}
	
	public static boolean isElementNameUnique(
			String theProposedName, 
			String ofMetaClass, 
			IRPModelElement underneathTheEl,
			int recursive){
				
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theExistingEls = 
				underneathTheEl.getNestedElementsByMetaClass(ofMetaClass, recursive).toList();
		
		for (IRPModelElement theExistingEl : theExistingEls) {
			
			if (theExistingEl.getName().equals(theProposedName)){
				count++;
				break;
			}
		}
		
		if (count > 1){
			Logger.writeLine("Warning in isElementNameUnique, there are " + count + " elements called " + 
					theProposedName + " of type " + ofMetaClass + " in the project. This may cause issues.");
		}
				
		boolean isUnique = (count == 0);

		return isUnique;
	}
	
	public static String determineUniqueNameBasedOn(
			String theProposedName,
			String ofMetaClass,
			IRPModelElement underElement){
		
		int count = 0;
		
		String theUniqueName = theProposedName;
		
		while( !isElementNameUnique(
				theUniqueName, ofMetaClass, underElement, 1 ) ){
			
			count++;
			theUniqueName = theProposedName + count;
		}
		
		return theUniqueName;
	}
	
	public static List<IRPModelElement> getNonActorOrTestingClassifiersConnectedTo( 
			IRPClassifier theClassifier,
			IRPClass inTheBuildingBlock ){
		
		@SuppressWarnings("unchecked")
		List<IRPClassifier> theBaseClassifiers = theClassifier.getBaseClassifiers().toList();
		
		List<IRPModelElement> theClassifiersConnectedTo = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theConnectors = inTheBuildingBlock.getLinks().toList();
		
		for( IRPModelElement theConnector : theConnectors ) {
			
			IRPLink theLink = (IRPLink) theConnector;
			
			IRPPort theFromPort = theLink.getFromPort();
			IRPPort theToPort = theLink.getToPort();
			
			if( theFromPort != null && theToPort != null ){
				
				IRPModelElement fromPortOwner = theFromPort.getOwner();
				IRPModelElement toPortOwner = theToPort.getOwner();
				
				if( fromPortOwner.equals( theClassifier ) || 
						theBaseClassifiers.contains( fromPortOwner ) ){
					
					if( toPortOwner instanceof IRPClass &&
						!GeneralHelpers.hasStereotypeCalled("TestDriver", toPortOwner) ){
						theClassifiersConnectedTo.add( toPortOwner );
					}
					
				} else if( toPortOwner.equals( theClassifier ) ||
						theBaseClassifiers.contains( toPortOwner )){
					
					if( fromPortOwner instanceof IRPClass &&
						!GeneralHelpers.hasStereotypeCalled("TestDriver", fromPortOwner) ){
						theClassifiersConnectedTo.add( fromPortOwner );
					}
				}
			}
		}

		return theClassifiersConnectedTo;
	}
	
	public static IRPPort getPortThatConnects(
			IRPClassifier theSourceClassifier,
			IRPClassifier withTheTargetClassifier, 
			IRPClass inTheBuildingBlock ) {
		
		IRPPort thePort = null;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theConnectors = inTheBuildingBlock.getLinks().toList();
		
		@SuppressWarnings("unchecked")
		List<IRPClassifier> theTargetClassifiers = 
				withTheTargetClassifier.getBaseClassifiers().toList();
		
		theTargetClassifiers.add( withTheTargetClassifier );
		
		@SuppressWarnings("unchecked")
		List<IRPClassifier> theSourceClassifiers = 
				theSourceClassifier.getBaseClassifiers().toList();
		
		theSourceClassifiers.add( theSourceClassifier );
		
		for( IRPModelElement theConnector : theConnectors ){
			
			IRPLink theLink = (IRPLink) theConnector;
			
			IRPPort theFromPort = theLink.getFromPort();
			IRPPort theToPort = theLink.getToPort();
			
			if( theFromPort != null && theToPort != null ){
				
				IRPModelElement fromPortOwner = theFromPort.getOwner();
				IRPModelElement toPortOwner = theToPort.getOwner();
				
				if( theTargetClassifiers.contains( fromPortOwner ) && 
					theSourceClassifiers.contains( toPortOwner ) ){
					
					Logger.writeLine( "Found " + Logger.elementInfo(theConnector) + " owned by " + Logger.elementInfo( inTheBuildingBlock ) + 
							"that goes from " + Logger.elementInfo(theFromPort) + " to " + Logger.elementInfo(theToPort));

					thePort = theToPort;
					
				} else if( theTargetClassifiers.contains( toPortOwner ) && 
						   theSourceClassifiers.contains( fromPortOwner ) ){
					
					Logger.writeLine( "Found " + Logger.elementInfo(theConnector) + " owned by " + Logger.elementInfo( inTheBuildingBlock ) + 
							"that goes from " + Logger.elementInfo(theFromPort) + " to " + Logger.elementInfo(theToPort));

					thePort = theFromPort;
				}
			}
		}
		
		Logger.writeLine("getPortThatConnects is returning " + Logger.elementInfo(thePort));
		
		return thePort;
	}
		
	public static String determineBestCheckOperationNameFor(
			IRPClassifier onTargetBlock,
			String theAttributeName){
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( "check" + GeneralHelpers.capitalize( theAttributeName ) ), 
				"Attribute", 
				onTargetBlock );
		
		return theProposedName;
	}
	
	public static IRPSysMLPort getExistingFlowPort( 
			IRPAttribute forTheAttribute ){
	
		IRPSysMLPort theExistingFlowPort = null;
		
		Set<IRPModelElement> theEls = 
				TraceabilityHelper.getElementsThatHaveStereotypedDependenciesFrom(
						forTheAttribute, "AutoRipple" );
		
		IRPModelElement theAttributeOwner = forTheAttribute.getOwner();
		
		for( IRPModelElement theEl : theEls ) {
			
			if( theEl instanceof IRPSysMLPort ){
				
				IRPModelElement theElementsOwner = theEl.getOwner();
				
				if( theElementsOwner.equals( theAttributeOwner )){
					theExistingFlowPort = (IRPSysMLPort)theEl;
					Logger.writeLine( theExistingFlowPort, "was found based on «AutoRipple» dependency" );					
				} else {
					Logger.writeLine( "Warning, in getExistingFlowPort() for " + 
							Logger.elementInfo( forTheAttribute ) + ":" + Logger.elementInfo( theEl ) + 
							"was found based on «AutoRipple» dependency" );	
					
					Logger.writeLine("However, it is incorrectly owned by " + Logger.elementInfo( theElementsOwner ) + 
							" hence relation needs to be deleted");
				}
			}
		}
		
		// still not found?
		if( theExistingFlowPort == null ){
			
			theExistingFlowPort = (IRPSysMLPort) forTheAttribute.getOwner().findNestedElement(
					forTheAttribute.getName(), "FlowPort" );
			
			if( theExistingFlowPort != null ){
				Logger.writeLine( theExistingFlowPort, "was found based on name matching" );
			} else {
				Logger.writeLine( "Unable to find an existing flow port related to " + Logger.elementInfo( forTheAttribute ) );
			}
		}
		
		return theExistingFlowPort;
	}
	
	public static IRPOperation getExistingCheckOp( 
			IRPAttribute forTheAttribute ){
	
		IRPOperation theExistingCheckOp = null;
		
		Set<IRPModelElement> theEls = 
				TraceabilityHelper.getElementsThatHaveStereotypedDependenciesFrom(
						forTheAttribute, "AutoRipple" );
		
		IRPModelElement theAttributeOwner = forTheAttribute.getOwner();
		
		for( IRPModelElement theEl : theEls ) {
			
			if( theEl instanceof IRPOperation && 
				theEl.getName().contains( "check" ) ){
				
				IRPModelElement theElementsOwner = theEl.getOwner();
				
				if( theElementsOwner.equals( theAttributeOwner )){
					theExistingCheckOp = (IRPOperation)theEl;
					Logger.writeLine( theExistingCheckOp, "was found based on «AutoRipple» dependency" );					
				} else {
					Logger.writeLine( "Warning, in getExistingCheckOp() for " + 
							Logger.elementInfo( forTheAttribute ) + ":" + Logger.elementInfo( theEl ) + 
							" was found based on «AutoRipple» dependency" );	
					
					Logger.writeLine("However, it is incorrectly owned by " + Logger.elementInfo( theElementsOwner ) + 
							" hence relation needs to be deleted");
				}
			}
		}
		
		// still not found?
		if( theExistingCheckOp == null ){
			
			String theExpectedName = determineBestCheckOperationNameFor(
					(IRPClassifier)theAttributeOwner, 
					forTheAttribute.getName() );
			
			theExistingCheckOp = 
					(IRPOperation) forTheAttribute.getOwner().findNestedElement(
							theExpectedName, "Operation" );
			
			if( theExistingCheckOp != null ){
				Logger.writeLine( theExistingCheckOp, "was found based on name matching" );
			} else {
				Logger.writeLine( "Unable to find an existing check operation called " + theExpectedName );
			}
		}
		
		return theExistingCheckOp;
	}
	
	public static void setStringTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			String theValue ){
		
		IRPTag theTag = theOwner.getTag( theTagName );
		
		if( theTag != null ){
			theOwner.setTagValue( theTag, theValue );
		} else {
			
			Logger.writeLine( "Error in GeneralHelpers.setStringTagValueOn for " + 
					Logger.elementInfo( theOwner) + ", unable to find tag called " + theTagName );
		}
	}
	
	public static IRPPackage getExistingOrCreateNewPackageWith( 
			String theName, 
			IRPModelElement underneathTheEl ){
		
		IRPModelElement thePackage = GeneralHelpers.findElementWithMetaClassAndName(
				"Package", theName, underneathTheEl );
		
		if( thePackage == null ){

			Logger.writeLine( "create a package called " + theName );
			thePackage = underneathTheEl.addNewAggr( "Package", theName );
		}
		
		return (IRPPackage) thePackage;
	}

	public static IRPModelElement getExistingOrCreateNewElementWith( 
			String theName, 
			String andMetaClass,
			IRPModelElement underneathTheEl ){
		
		IRPModelElement theElement =
				GeneralHelpers.findElementWithMetaClassAndName(
						andMetaClass, theName, underneathTheEl );
		
		try {
			if( theElement == null ){
				theElement = underneathTheEl.addNewAggr( andMetaClass, theName );
			}
			
		} catch (Exception e) {
			Logger.writeLine("Exception in getExistingOrCreateNewElementWith( theName " + theName + 
					", andMetaClass=" + andMetaClass + ", underneath=" + Logger.elementInfo(underneathTheEl));
		}
		
		return theElement;
	}
	
	public static List<IRPLink> getLinksBetween(
			IRPSysMLPort thePort,
			IRPInstance ownedByPart,
			IRPSysMLPort andThePort,
			IRPInstance whichIsOwnedByPart,
			IRPClassifier inBuildingBlock ){
		
		List<IRPLink> theLinksBetween = 
				new ArrayList<IRPLink>();
	
		@SuppressWarnings("unchecked")
		List<IRPLink> theExistingLinks = 
			inBuildingBlock.getLinks().toList();
		
		for( IRPLink theExistingLink : theExistingLinks ){
		
			IRPSysMLPort fromSysMLPort = theExistingLink.getFromSysMLPort();
			IRPModelElement fromSysMLElement = theExistingLink.getFromElement();
			
			IRPSysMLPort toSysMLPort = theExistingLink.getToSysMLPort();
			IRPModelElement toSysMLElement = theExistingLink.getToElement();
		
			if( fromSysMLPort != null && 
				fromSysMLElement != null && fromSysMLElement instanceof IRPInstance &&
				toSysMLPort != null &&
				toSysMLElement != null && toSysMLElement instanceof IRPInstance ){

				if( thePort.equals( fromSysMLPort ) && 
					ownedByPart.equals( fromSysMLElement ) &&
					andThePort.equals( toSysMLPort ) &&
					whichIsOwnedByPart.equals( toSysMLElement ) ){
					
					Logger.writeLine("Check for links between " + Logger.elementInfo(fromSysMLPort) + " and " + 
							Logger.elementInfo( toSysMLPort ) + " successfully found " + 
							Logger.elementInfo( theExistingLink ) );
					
					theLinksBetween.add( theExistingLink );

				} else if( thePort.equals( toSysMLPort ) && 
						   ownedByPart.equals( fromSysMLElement ) &&
						   andThePort.equals( fromSysMLPort ) &&
						   whichIsOwnedByPart.equals( toSysMLElement ) ){
					
					Logger.writeLine("Check for links between " + Logger.elementInfo(toSysMLPort) + " and " + 
							Logger.elementInfo( fromSysMLPort ) + " successfully found " + 
							Logger.elementInfo( theExistingLink ) );
					
					theLinksBetween.add( theExistingLink );

				} else {
//					Logger.writeLine("Check for links between " + Logger.elementInfo(toSysMLPort) + " and " + 
//							Logger.elementInfo( fromSysMLPort ) + " found no match to " + 
//							Logger.elementInfo( theExistingLink ) );
				}

			} else {
				// we're only interested in flow ports
			}
		}
		
		Logger.writeLine("getLinksBetween " + Logger.elementInfo( thePort ) + " and " +
				Logger.elementInfo( andThePort ) + " has found " + 
				theLinksBetween.size() + " matches");
		
		return theLinksBetween;
	}

	public static IRPLink addConnectorBetweenSysMLPortsIfOneDoesntExist(
			IRPSysMLPort theSrcPort,
			IRPInstance theSrcPart, 
			IRPSysMLPort theTgtPort,
			IRPInstance theTgtPart) {
		
		IRPLink theLink = null;
		
		IRPClass theAssemblyBlock = (IRPClass) theSrcPart.getOwner();
		
		Logger.writeLine( "addConnectorBetweenSysMLPortsIfOneDoesntExist has determined that " + 
				Logger.elementInfo( theAssemblyBlock ) + " is the assembly block" );
		
		List<IRPLink> theLinks = getLinksBetween(
				theSrcPort, 
				theSrcPart,
				theTgtPort, 
				theTgtPart,
				theAssemblyBlock );
		
		// only add if one does not already exist
		if( theLinks.size() == 0 ){

			Logger.writeLine( "Adding a new connector between " + Logger.elementInfo( theSrcPort ) + 
					" and " + Logger.elementInfo( theTgtPort ) + " as one does not exist" ); 
			
			IRPPackage thePkg = (IRPPackage) theAssemblyBlock.getOwner();

			theLink = thePkg.addLinkBetweenSYSMLPorts(
					theSrcPart, 
					theTgtPart, 
					null, 
					theSrcPort, 
					theTgtPort );

			theLink.changeTo("connector");
						
			String theUniqueName = GeneralHelpers.determineUniqueNameBasedOn(
					theSrcPart.getName() + "_" + theSrcPort.getName() + "__" + 
					theTgtPart.getName() + "_" + theTgtPort.getName(), 
					"Link", 
					theAssemblyBlock );
			
			theLink.setName( theUniqueName );		
			theLink.setOwner( theAssemblyBlock );
			
			Logger.writeLine("Added " + Logger.elementInfo( theLink ) + 
					" to " + Logger.elementInfo( theAssemblyBlock ));
		}
		
		return theLink;
	}
	
	public static Set<IRPModelElement> getSetOfElementsFromCombiningThe(
			List<IRPModelElement> theSelectedEls,
			List<IRPGraphElement> theSelectedGraphEls ){
		
		Set<IRPModelElement> theSetOfElements = 
				new HashSet<IRPModelElement>( theSelectedEls );

		for( IRPGraphElement theGraphEl : theSelectedGraphEls ){
			
			IRPModelElement theEl = theGraphEl.getModelObject();
			
			if( theEl != null ){
				theSetOfElements.add( theEl );
			}
		}

		return theSetOfElements;
	}
	
	public static void addGeneralization(
			IRPClassifier fromElement, 
			String toBlockWithName, 
			IRPPackage underneathTheRootPackage ){
		
		IRPModelElement theBlock = 
				underneathTheRootPackage.findNestedElementRecursive( 
						toBlockWithName, "Block" );
		
		if( theBlock != null ){
			fromElement.addGeneralization( (IRPClassifier) theBlock );
		} else {
			Logger.writeLine( "Error: Unable to find element with name " + toBlockWithName );
		}
	}
	
	public static IRPClass findOwningClassIfOneExistsFor( 
			IRPModelElement theModelEl ){
		
		IRPModelElement theOwner = theModelEl.getOwner();
		IRPClass theResult = null;
		
		if( ( theOwner != null ) &&
			!( theOwner instanceof IRPProject ) ){
			
			if( theOwner.getMetaClass().equals("Class") ){

				theResult = (IRPClass) theOwner;
			} else {
				theResult = findOwningClassIfOneExistsFor( theOwner );
			}
		}

		return theResult;
	}
}

/**
 * Copyright (C) 2016-2017  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #006 02-MAY-2016: Add FunctionalAnalysisPkg helper support (F.J.Chadburn)
    #007 05-MAY-2016: Move FileHelper into generalhelpers and remove duplicate class (F.J.Chadburn)
    #010 08-MAY-2016: Remove white-space from actor names (F.J.Chadburn)
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #035 15-JUN-2016: New panel to configure requirements package naming and gateway set-up (F.J.Chadburn)
    #041 29-JUN-2016: Derive downstream requirement menu added for reqts on diagrams (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
    #051 06-JUL-2016: Re-factored the GW panel to allow it to incrementally add to previous setup (F.J.Chadburn)
    #055 13-JUL-2016: Support requirement derivation from simplified AD elements (F.J.Chadburn)
    #065 17-JUL-2016: Changed simplified AD to use Name rather than Label for Event descriptions (F.J.Chadburn)
    #070 20-JUL-2016: Fix exception when populating events/ops from transitions with no guard (F.J.Chadburn)
	#071 25-JUL-2016: Fix exception to allow creating requirements from pre-condition statements (F.J.Chadburn) 
	#072 25-JUL-2016: Improved robustness when graphEls that don't have model elements are selected (F.J.Chadburn)
	#074 25-JUL-2016: Support creation of requirements from AcceptTimeEvents (F.J.Chadburn)
	#085 09-AUG-2016: Add helper to findElementsWithMetaClassStereotypeAndName (F.J.Chadburn)
	#089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
	#102 03-NOV-2016: Add right-click menu to auto update names of ADs from UC names (F.J.Chadburn)
	#113 13-NOV-2016: Stereotypes moved to GlobalPreferencesProfile to simplify/remove orphaned ownership issues (F.J.Chadburn)
    #125 25-NOV-2016: AutoRipple used in UpdateTracedAttributePanel to keep check and FlowPort name updated (F.J.Chadburn)
    #135 02-DEC-2016: Avoid port proliferation in inheritance tree for actors/system (F.J.Chadburn)
    #145 18-DEC-2016: Fix to remove warning with getWorkingPkgUnderDev unexpectedly finding 2 packages (F.J.Chadburn)
    #160 25-JAN-2017: Minor fixes to code found during development (F.J.Chadburn)
    #163 05-FEB-2017: Add new menus to Smart link: Start and Smart link: End (F.J.Chadburn)
    #171 08-MAR-2017: Added some dormant ops to GeneralHelpers to assist with 3rd party integration (F.J.Chadburn)
    #184 29-MAY-2017: Create a connector between pElapsedTime port when creating block hierarchy (F.J.Chadburn)
    #202 05-JUN-2017: Minor changes to logging in GeneralHelpers (F.J.Chadburn)
    #207 25-JUN-2017: Significant bolstering of Select Depends On/Dependent element(s) menus (F.J.Chadburn)
    #213 09-JUL-2017: Add dialogs to auto-connect «publish»/«subscribe» FlowPorts for white-box simulation (F.J.Chadburn)
    #216 09-JUL-2017: Added a new Add Block/Part command added to the Functional Analysis menus (F.J.Chadburn)
    #224 25-AUG-2017: Added new menu to roll up traceability to the transition and populate on STM (F.J.Chadburn)
    #227 06-SEP-2017: Increased robustness to stop smart link panel using non new term version of <<refine>> (F.J.Chadburn)

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

