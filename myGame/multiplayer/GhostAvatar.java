package myGame.multiplayer;

import java.util.UUID;

import org.joml.Vector3f;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;

public class GhostAvatar extends GameObject{
	private UUID uuid;
	public GhostAvatar(UUID id, ObjShape s, TextureImage t, Vector3f p)
	{ 
		super(GameObject.root(), s, t);
		uuid = id;
		setPosition(p);;
}
	public UUID getID() { return uuid; }
	public void setPosition(Vector3f m) { setLocalLocation(m); }
	public Vector3f getPosition() { return getWorldLocation(); }
}