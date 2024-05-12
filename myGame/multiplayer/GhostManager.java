package myGame.multiplayer;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import myGame.MyGame;
import tage.ObjShape;
import tage.TextureImage;
import tage.VariableFrameRateGame;

public class GhostManager {
	private MyGame game;
	GhostAvatar newAvatar;
	private Vector<GhostAvatar> ghostAvs = new Vector<GhostAvatar>();
	public GhostManager(VariableFrameRateGame vfrg)
	{ game = (MyGame)vfrg;
	}
	
	
	public void createGhost(UUID id, Vector3f p) throws IOException
	{
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		newAvatar = new GhostAvatar(id, s, t, p);
		Matrix4f initialScale = (new Matrix4f()).scaling(0.25f);
		newAvatar.setLocalScale(initialScale);
		ghostAvs.add(newAvatar);
	}
	
	public void changeShape(ObjShape s) {
		newAvatar.setShape(s);
	}
	public void changeTex(TextureImage t) {
		newAvatar.setTextureImage(t);
	}
	
	
	public void removeGhostAvatar(UUID id)
	{ GhostAvatar ghostAv = findAvatar(id);
	if(ghostAv != null)
	{ game.getEngine().getSceneGraph().removeGameObject(ghostAv);
	ghostAvs.remove(ghostAv);
	}
	else
	{ System.out.println("unable to find ghost in list");
	} }
	
	
	public GhostAvatar findAvatar(UUID id)
	{ GhostAvatar ghostAvatar;
	Iterator<GhostAvatar> it = ghostAvs.iterator();
	while(it.hasNext())
	{ ghostAvatar = it.next();
	if(ghostAvatar.getID().compareTo(id) == 0)
	{ return ghostAvatar;
	} }
	return null;
	}
	
	
	public void updateGhostAvatar(UUID id, Vector3f position)
	{ GhostAvatar ghostAvatar = findAvatar(id);
	if (ghostAvatar != null) { ghostAvatar.setPosition(position); }
	else { System.out.println("unable to find ghost in list"); }
	} 


}
