package myGame;

import tage.*;
import tage.input.InputManager;
import tage.shapes.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;



public class MyGame extends VariableFrameRateGame
{
	private int fluffyClouds, lakeislands; //skyboxes	
	private static InputManager im;

	
	private static Engine engine;
	public static Engine getEngine() {return engine;}
	
	private boolean paused=false;
	private int counter=0;
	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject dol, terr;
	private ObjShape dolS, terrS;
	private TextureImage doltx, grass, heightmap;
	private Light light1;

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		im = engine.getInputManager();

		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	dolS = new ImportedModel("placeholder_guy.obj");
		terrS = new TerrainPlane(1000); //1000x1000
	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("placeholder_uv.png");
		heightmap = new TextureImage("tempHeightMap.jpg");
		grass = new TextureImage("grass.jpg");
	}

	@Override
	public void loadSkyBoxes() {
		fluffyClouds = engine.getSceneGraph().loadCubeMap("fluffyClouds");
		lakeislands = engine.getSceneGraph().loadCubeMap("lakeislands");
		engine.getSceneGraph().setActiveSkyBoxTexture(fluffyClouds);
		engine.getSceneGraph().setSkyBoxEnabled(true);
		
	}
	
	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale;

		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS);
		initialTranslation = (new Matrix4f()).translation(0,5,0);
		initialScale = (new Matrix4f()).scaling(0.25f);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);

		// build terrain object
		terr = new GameObject(GameObject.root(), terrS, grass);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f, 1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(heightmap);

		// set tiling for terrain texture
		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);


		// ------------- inputs section ------------------
		FwdAction fwdAction = new FwdAction(dol);
		TurnAction yawAction = new TurnAction(dol);
		//PitchAction pitchAction = new PitchAction(dol);
		//RideAction ride = new RideAction(dol);
		//FeedAction feed = new FeedAction();
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, pitchAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, pitchAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,5));
	
	}

	@Override
	public void update()
	{	

		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		if (!paused) elapsTime += (currFrameTime - lastFrameTime) / 1000.0;
		im.update((float)elapsTime);

		// build and set HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "Keyboard hits = " + counterStr;
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);
		
		// update altitude of dolphin based on height map
		Vector3f loc = dol.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		dol.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));
		//positionCameraBehindAvatar();
		
		
		
	}

	@Override
	public void keyPressed(KeyEvent e)
	{	switch (e.getKeyCode())
		{	case KeyEvent.VK_C:
				counter++;
				break;
			case KeyEvent.VK_1:
				engine.getSceneGraph().setActiveSkyBoxTexture(fluffyClouds);
				engine.getSceneGraph().setSkyBoxEnabled(true);
				break;
			case KeyEvent.VK_2:
				engine.getSceneGraph().setActiveSkyBoxTexture(lakeislands);
				engine.getSceneGraph().setSkyBoxEnabled(true);
				break;
			case KeyEvent.VK_3:
				engine.getSceneGraph().setSkyBoxEnabled(false);
				break;
			case KeyEvent.VK_4:
				(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,0));
				break;
		}
		super.keyPressed(e);
	}
}