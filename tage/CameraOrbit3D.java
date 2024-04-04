package tage;

import org.joml.Vector3f;

import net.java.games.input.Event;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
/**
* Class used to implement a 3rd-person orbit camera controller.
* Gives player the ability to orbit the camera by adjusting elevation angles,
* zooming in/out, and panning left/right. 
* 
* 
* @author Abel Ontiveros
*/

public class CameraOrbit3D {
	private Engine engine;
	private Camera camera; // the camera being controlled
	private GameObject avatar; // the target avatar the camera looks at
	private float cameraAzimuth; // rotation around target Y axis
	private float cameraElevation; // elevation of camera above target
	private float cameraRadius; // distance between camera and target
	public CameraOrbit3D(Camera cam, GameObject av,
	String gpName, Engine e)
	{ engine = e;
	camera = cam;
	avatar = av;
	cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
	cameraElevation = 20.0f; // elevation is in degrees
	cameraRadius = 2.0f; // distance from camera to avatar
	setupInputs();
	updateCameraPosition();
	}
	private void setupInputs()
	{ 
		OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
    OrbitRadiusAction radiusAction = new OrbitRadiusAction();
    OrbitElevationAction elevationAction = new OrbitElevationAction();

	InputManager im = engine.getInputManager();
	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, azmAction,
	InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.F, azmAction,
	InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	
	
	
    // Radius controls
    im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.T, radiusAction,
            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.G, radiusAction,
            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    
    // Elevation controls
    im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Q, elevationAction,
            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.E, elevationAction,
            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    
	}
	// Compute the camera’s azimuth, elevation, and distance, relative to
	// the target in spherical coordinates, then convert to world Cartesian
	// coordinates and set the camera position from that.
	public void updateCameraPosition()
	{ 
		Vector3f avatarRot = avatar.getWorldForwardVector();
		double avatarAngle = Math.toDegrees((double)
				
				
	avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
		
		float totalAz = cameraAzimuth - (float)avatarAngle;
		double theta = Math.toRadians(totalAz);
		double phi = Math.toRadians(cameraElevation);
		
		float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
		float y = cameraRadius * (float)(Math.sin(phi));
		float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
	camera.setLocation(new
	Vector3f(x,y,z).add(avatar.getWorldLocation()));
	camera.lookAt(avatar);

}
	/** Checks for inputs and pans camera left/right */

private class OrbitAzimuthAction extends AbstractInputAction
{ 
	
	public void performAction(float time, Event event)
{ float rotAmount;
if (event.getValue() < -0.2)
{ rotAmount=-0.2f; }
else
{ if (event.getValue() > 0.2)
{ rotAmount=0.2f; }
else
{ rotAmount=0.0f; }
}



String keyName = event.getComponent().getName();
if ("F".equals(keyName) || event.getValue() < 0 &&!"H".equals(keyName))
	{ rotAmount=-0.2f; }
else if("H".equals(keyName) || event.getValue() < 0 &&!"F".equals(keyName))
		{ rotAmount=0.2f; }

cameraAzimuth += rotAmount;
cameraAzimuth = cameraAzimuth % 360;
updateCameraPosition();
} 
}
/** Checks for inputs and zooms camera in/out */

private class OrbitRadiusAction extends AbstractInputAction {
    public void performAction(float time, Event event) {
    	String keyName = event.getComponent().getName();
        float radiusAmount =0; // Adjust the multiplier as needed
        if ("T".equals(keyName) || event.getValue() < 0 &&!"G".equals(keyName))
    	{ radiusAmount = -0.2f; }
        else if("G".equals(keyName) || event.getValue() < 0 &&!"T".equals(keyName))
    		{ radiusAmount = 0.2f; }
        cameraRadius += radiusAmount;
        // Add any necessary checks or limits on the camera radius
        updateCameraPosition();
    }
}
/** Checks for inputs and adjusts camera's elevation */

private class OrbitElevationAction  extends AbstractInputAction {
    public void performAction(float time, Event event) {
    	String keyName = event.getComponent().getName();
        float elevationAmount =0; // Adjust the multiplier as needed
        if ("Q".equals(keyName) || event.getValue() < 0 &&!"E".equals(keyName))
    	{ elevationAmount = -0.2f; }
        else if("E".equals(keyName) || event.getValue() < 0 &&!"Q".equals(keyName))
    		{ elevationAmount = 0.2f; }
        cameraElevation += elevationAmount;
        // Add any necessary checks or limits on the camera radius
        updateCameraPosition();
    }
}
}
