package taumigrator;

public abstract class RhpElRelation extends RhpEl {

	public RhpElRelation(
			String theElementName, 
			String theElementType,
			String theElementGuid ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid );
	}

	public RhpElRelation(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theParent );
	}
}
