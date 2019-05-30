package taumigrator;

import generalhelpers.Logger;

import com.telelogic.rhapsody.core.IRPGraphEdge;

public abstract class RhpElGraphEdge extends RhpElRelation {

	protected IRPGraphEdge _graphEdge;
	protected String _segmentPoints;
//	protected int _xSrcPosition;
//	protected int _ySrcPosition;
//	protected int _xTrgPosition;
//	protected int _yTrgPosition;
	
	public IRPGraphEdge get_graphEl() {
		return _graphEdge;
	}

	public void appendSegmentPoints( 
			String theSegmentPoints ){
		
		_segmentPoints += " " + theSegmentPoints;
		Logger.info( "_segmentPoints = " + _segmentPoints );	
	}
	
	public RhpElGraphEdge(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theSegmentPoints ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid );
		
		_segmentPoints = theSegmentPoints;
		
//		extractSrcAndTrgPositions( theSegmentPoints );
	}

	public RhpElGraphEdge(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theSegmentPoints ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theParent );
		
		_segmentPoints = theSegmentPoints;

//		extractSrcAndTrgPositions( theSegmentPoints );
	}

	public int get_xSrcPosition() throws Exception {
		
		String points[] = _segmentPoints.split(" ");
				
		int xSrcPosition = Settings.scaleInX( Integer.parseInt( points[0] ) );
		
		Logger.info("get_xSrcPosition is returning " + xSrcPosition + 
				" based on segmentpoints = " + _segmentPoints );
		
		return xSrcPosition;
	}
	
	public int get_ySrcPosition() throws Exception {
		
		String points[] = _segmentPoints.split(" ");
				
		int ySrcPosition = Settings.scaleInY( Integer.parseInt( points[1] ) );
		
		Logger.info("get_xSrcPosition is returning " + ySrcPosition + 
				" based on segmentpoints = " + _segmentPoints );
		
		return ySrcPosition;
	}
	
	public int get_xDstPosition() throws Exception {
		
		String points[] = _segmentPoints.split(" ");
		
		int count = points.length;
		
		int xDstPosition = Settings.scaleInX( Integer.parseInt( points[ count-2 ] ) );
		
		Logger.info("get_xDstPosition is returning " + xDstPosition + 
				" based on segmentpoints = " + _segmentPoints );
		
		return xDstPosition;
	}
	
	public int get_yDstPosition() throws Exception {
		
		String points[] = _segmentPoints.split(" ");
		
		int count = points.length;
		
		int yDstPosition = Settings.scaleInY( Integer.parseInt( points[ count-1 ] ) );
		
		Logger.info("get_xDstPosition is returning " + yDstPosition + 
				" based on segmentpoints = " + _segmentPoints );
		
		return yDstPosition;
	}
}