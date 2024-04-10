package myGame;

import org.joml.Vector3f;

import myGame.multiplayer.ProtocolClient;
import net.java.games.input.Event;
import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class FwdAction extends AbstractInputAction{
	private Camera cam = (MyGame.getEngine().getRenderSystem().getViewport("MAIN").getCamera());
	private GameObject avatar;
	MyGame game;
	private ProtocolClient protClient;
	
	public FwdAction(MyGame g, ProtocolClient p) {
		game = g;
		protClient = p;
	}
	@Override
	public void performAction(float time, Event e)
	{ 
		avatar = game.getAvatar();
		float keyValue = e.getValue();
    	if(keyValue > -.9 && keyValue < .9)return; 

    	
        String keyName = e.getComponent().getName();
		Vector3f loc, fwd, newLocation;
	    if ("W".equals(keyName) || keyValue < 0 &&!"S".equals(keyName)) {
	    		// deadzone)
	    	
			fwd = avatar.getWorldForwardVector();
			loc = avatar.getWorldLocation();
			newLocation = loc.add(fwd.mul(.02f));
			//if(checkDist(newLocation, 5)) {
			avatar.setLocalLocation(newLocation);
			try {
				protClient.sendMoveMessage(avatar.getWorldLocation());
			}catch(NullPointerException ex) {
			}			
			
			//checkCollisionWithAny(obj);
			//}
	    } else if ("S".equals(keyName) || keyValue > 0 && !"W".equals(keyName)) {
			fwd = avatar.getWorldForwardVector();
			loc = avatar.getWorldLocation();
			newLocation = loc.sub(fwd.mul(.02f));
			//if(checkDist(newLocation, 5)) {
				avatar.setLocalLocation(newLocation);
				try {
					protClient.sendMoveMessage(avatar.getWorldLocation());
				}catch(NullPointerException ex) {
				}
				//checkCollisionWithAny(obj);
				//}		
			}
	    Vector3f loc1 = avatar.getWorldLocation();
		Vector3f fwd1 = avatar.getWorldForwardVector();
		Vector3f up = avatar.getWorldUpVector();
		Vector3f right = avatar.getWorldRightVector();
}
	/*
	public boolean checkDist(Vector3f objectLocation, float distanceThreshold) {
	    Vector3f cameraToObj = new Vector3f(objectLocation).sub(cam.getLocation());

	    // Check if the object is in front of the camera
	    if (cameraToObj.dot(cam.getN()) > 1 ) {
	        // Check if the object is within the specified distance
	        float distance = cameraToObj.length();
	        if (distance <= distanceThreshold) {
	            // Object is within the specified distance and in front of the camera
	            // Trigger your message or perform the desired action here
	        	MyGame.setHUD("");
	            return true;
	        }
	    }

	    // Object is either behind the camera or outside the specified distance
	    // Optionally, you can have an else block for additional actions
    	MyGame.setHUD("Dolphin can't move because it is out of camera distance threshold.");
	    return false;
	}


    public static boolean checkCollisionWithAny(GameObject obj) {
        for (GameObject obj2 : MyGame.sites()) {
            if (checkCollision(obj, obj2)) {
            	MyGame.sites().remove(obj2);
            	System.out.print("collision detected");
            	MyGame.counter++;
            	MyGame.rc.addTarget(obj2);
                return true; // Return true if any collision is detected
            }
        }
        return false; // Return false if no collisions are detected
    }

    
    public static boolean checkCollision(GameObject obj1, GameObject obj2) {
    	//(!MyGame.isRiding()) return false;
        // Get the world positions of the objects
        Vector3f pos1 = obj1.getWorldLocation();
        Vector3f pos2 = obj2.getWorldLocation();

        // Set a threshold distance for collision
        float collisionThreshold = 1.0f; // Adjust this value based on your scene scale

        // Calculate the distance between the objects
        float distance = pos1.distance(pos2);

        // Check if the distance is less than the collision threshold
        boolean collision = distance < collisionThreshold;

        // Print the collision status


        return collision;
    }*/
}