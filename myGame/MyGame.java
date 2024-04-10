package myGame;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import myGame.multiplayer.GhostManager;
import myGame.multiplayer.ProtocolClient;
import tage.Engine;
import tage.GameObject;
import tage.Light;
import tage.ObjShape;
import tage.TextureImage;
import tage.VariableFrameRateGame;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import tage.networking.IGameConnection.ProtocolType;
import tage.shapes.ImportedModel;
import tage.shapes.Sphere;
import tage.shapes.TerrainPlane;



public class MyGame extends VariableFrameRateGame
{
	private int fluffyClouds, lakeislands; //skyboxes	
	private static InputManager im;

	
	
	//Networking
	private GhostManager gm;
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	
	private static Engine engine;
	public static Engine getEngine() {return engine;}
	
	private boolean paused=false;
	private int counter=0;
	private double lastFrameTime, currFrameTime, elapsTime, prevTime;

	private GameObject dol, terr;
	private ObjShape dolS, terrS, ghostS;
	private TextureImage doltx, grass, heightmap, ghostT;
	private Light light1;

	public MyGame(String serverAddress, int serverPort, String protocol) { 
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if(protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		im = engine.getInputManager();
		
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	dolS = new ImportedModel("placeholder_guy.obj");
		ghostS = new Sphere();
		terrS = new TerrainPlane(1000); //1000x1000
	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("placeholder_uv.png");
		heightmap = new TextureImage("tempHeightMap.jpg");
		grass = new TextureImage("grass.jpg");
		ghostT = new TextureImage("Dolphin_HighPolyUV_wireframe.png");
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
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(0.25f);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);

		// build terrain object
		terr = new GameObject(GameObject.root(), terrS, grass);
		initialTranslation = (new Matrix4f()).translation(0f,-1f,0f);
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
		setupNetworking();


		// ------------- inputs section ------------------
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction yawAction = new TurnAction(this, protClient);
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

	
	private void setupNetworking()
	{ 
		System.out.print("SETTING UP");
		isClientConnected = false;
		try{
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
	} catch (UnknownHostException e) { e.printStackTrace();
	} catch (IOException e) { e.printStackTrace(); }
		if (protClient == null){ 
			System.out.println("missing protocol host"); }
		else{ // ask client protocol to send initial join message
	// to server, with a unique identifier for this client
	protClient.sendJoinMessage();
	System.out.print(isClientConnected);
	} }
	
	
	
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
		
		double elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		processNetworking((float)elapsedTime);
		
		
	}

	protected void processNetworking(float elapsTime) {
		//Process packets recieved by the client from the server
		if(protClient != null) protClient.processPackets();
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

	public GameObject getAvatar() { return dol; }
	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Vector3f getPlayerPosition() { return dol.getWorldLocation(); }
	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}
	
	
}