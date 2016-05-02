package functionalanalysisplugin;

import com.telelogic.rhapsody.core.*;

public class GR_Node {
	
	IRPGraphNode node;
	int srcX,srcY;
	int width;
	int height;
	// Corners:
	int top_left_x,top_left_y;
	int	top_right_x,top_right_y;
	int bot_left_x,bot_left_y;
	int bot_right_x,bot_right_y; //Corners
	int midBotX;
	int midBotY;
	int midRightX;
	int midRightY;
	
	
	public GR_Node (IRPGraphNode g) {
		node = g;
		width = getIntProperty("Width");
		height = getIntProperty("Height");
		getCoOrds();;
		calculateMidPoints();
		
	}
	
	private void calculateMidPoints() {
		midBotX = bot_left_x + (width /2);
		midBotY = bot_left_y;
		
		midRightX = top_right_x;
		midRightY = top_right_y + (height/2);
		
	}

	private void getCoOrds() {
		/*
		 * Graphical Elements are polygons of the form: noOfPoints,x,y,x,y,x,y,x,y etc
		 * States are of the form: 4,tlx,tly,trx,try,brx,bry,blx,bry
		 */
		
		IRPGraphicalProperty gPrp = node.getGraphicalProperty("Polygon");
		String v = gPrp.getValue();
		String[] xy = v.split(",");
		top_left_x = Integer.parseInt(xy[1]);
		top_left_y = Integer.parseInt(xy[2]);
		
		top_right_x = Integer.parseInt(xy[3]);
		top_right_y = Integer.parseInt(xy[4]);
		
		bot_right_x = Integer.parseInt(xy[5]);
		bot_right_y = Integer.parseInt(xy[6]);
		
		bot_left_x = Integer.parseInt(xy[7]);
		bot_left_y = Integer.parseInt(xy[8]);	
		
		
	}
	
	private Integer getIntProperty(String name) {
		Integer i = null;
		IRPGraphicalProperty gPrp = node.getGraphicalProperty(name);
		if (gPrp != null) {
			String value = gPrp.getValue();
			i = Integer.parseInt(value);
		}
		return i;
	}
	private void report(String name, int v) {
		System.out.println(name + " : " + v);
	}
	
	public void debug() {
		report("Width: ", width);
		report("Height: ", height);
		System.out.println("Top Left Corner: " + top_left_x + "," + top_left_y);
		System.out.println("Top Right Corner: " + top_right_x + "," + top_right_y);
		System.out.println("Bottom Right Corner: " + bot_right_x + "," + bot_right_y);
		System.out.println("Bottom Left Corner: " + bot_left_x + "," + bot_left_y);
		
		
		
	}
	public void reportAllProperties() {
		for (Object o:node.getAllGraphicalProperties().toList()) {
			IRPGraphicalProperty gprp = (IRPGraphicalProperty) o;
			System.out.println(gprp.getKey() + "::" + gprp.getValue());
		}
	}
}
