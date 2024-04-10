package myGame;


import myGame.multiplayer.ProtocolClient;
import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class TurnAction extends AbstractInputAction{
	private MyGame game;
	private GameObject avatar;
	private ProtocolClient protClient;

	 float yawAdjustmentSpeed = 0.1f;
     float yawAdjustment = 0.0f;
	
 	public TurnAction(MyGame g, ProtocolClient p)
 	{	game = g;
 		protClient = p;
 	}
 	
	@Override
	public void performAction(float time, Event e) {
			avatar = game.getAvatar();
	        String keyName = e.getComponent().getName();
			float keyValue = e.getValue();
	    	if(keyValue > -.9 && keyValue < .9)return; 

	        // This is a digital event (e.g., keyboard key or mouse button)
	        if ("D".equals(keyName) || keyValue > 0 &&!"A".equals(keyName)) {
	        	yawAdjustment += yawAdjustmentSpeed;
	            avatar.setWorldYaw(-1);
				try {
					protClient.sendMoveMessage(avatar.getWorldLocation());
				}catch(NullPointerException ex) {
				}

	        }
	        if ("A".equals(keyName)|| keyValue < 0 && !"D".equals(keyName)) {
	        	yawAdjustment -= yawAdjustmentSpeed;
	            avatar.setWorldYaw(1);

	        }
	}
	
}