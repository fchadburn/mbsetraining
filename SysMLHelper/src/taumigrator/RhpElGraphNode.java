package taumigrator;

import com.telelogic.rhapsody.core.IRPGraphNode;

public abstract class RhpElGraphNode extends RhpElElement {

	protected IRPGraphNode _graphNode;
	protected int _xPosition;
	protected int _yPosition;
	protected int _nWidth;
	protected int _nHeight;
	
	public IRPGraphNode get_graphEl() {
		return _graphNode;
	}

	public RhpElGraphNode(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String thePosition,
			String theSize ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid );
		
		extractPosition( thePosition );
		extractSize( theSize );
	}

	public RhpElGraphNode(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String thePosition,
			String theSize ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theParent );
		
		extractPosition( thePosition );
		extractSize( theSize );
	}

	protected void extractSize(
			String theSize) throws Exception {
		
		String sizes[] = theSize.split(" ");
		
		if( sizes.length != 2 ){
			throw new Exception( "theSize string is not valid" );
		}
		
		_nWidth = Settings.scaleInX( Integer.parseInt( sizes[0] ) );
		_nHeight = Settings.scaleInY( Integer.parseInt( sizes[1] ) );
	}

	private void extractPosition(String thePosition) throws Exception {
		String positions[] = thePosition.split(" ");
		
		if( positions.length != 2 ){
			throw new Exception( "thePosition string is not valid" );
		}
		
		_xPosition = Settings.scaleInX( Integer.parseInt( positions[0] ) );
		_yPosition = Settings.scaleInY( Integer.parseInt( positions[1] ) );
	}
}