package myGame.multiplayer;


import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;

public class GhostNPC extends GameObject {
	private int id;
	public static Vector3f location, location1;
	public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p) {
		super(GameObject.root(), s, t);
		this.id = id;
		setPosition(p);
		if(id==1) {
			location1=p;
		}else {
		location =p;
	}
	}
	public void setPosition(Vector3f m) { setLocalLocation(m); 
	location=m;}
	
	public void setSize(boolean big) {
		if(!big) { this.setLocalScale(new Matrix4f().scaling(0.5f));}
		else {
			this.setLocalScale(new Matrix4f().scaling(1.0f));}
		
		}
		}
