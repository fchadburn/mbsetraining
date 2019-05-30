package taumigrator;

public class Settings {

	static double _xScaling = 0.55;
	static double _yScaling = 0.5;
	
	static public int scaleInX( 
			int original ){
		
		double result = (double)original * _xScaling;
		return (int) result;
		
	}
	
	static public int scaleInY( 
			int original ){
		
		double result = (double)original * _yScaling;
		return (int) result;
	}
}
