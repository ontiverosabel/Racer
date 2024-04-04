package myGame;


import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class TurnAction extends AbstractInputAction{
	private GameObject obj;
	 float yawAdjustmentSpeed = 0.1f;
     float yawAdjustment = 0.0f;
	
	public TurnAction(GameObject obj) {
		this.obj = obj;
	}
	@Override
	public void performAction(float time, Event e) {
	        String keyName = e.getComponent().getName();
			float keyValue = e.getValue();
	    	if(keyValue > -.9 && keyValue < .9)return; 

	        // This is a digital event (e.g., keyboard key or mouse button)
	        if ("D".equals(keyName) || keyValue > 0 &&!"A".equals(keyName)) {
	        	yawAdjustment += yawAdjustmentSpeed;
	            obj.setWorldYaw(-1);

	        }
	        if ("A".equals(keyName)|| keyValue < 0 && !"D".equals(keyName)) {
	        	yawAdjustment -= yawAdjustmentSpeed;
	            obj.setWorldYaw(1);

	        }
	}
	
}