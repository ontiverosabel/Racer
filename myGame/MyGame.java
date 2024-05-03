package myGame;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import myGame.multiplayer.GhostManager;
import myGame.multiplayer.GhostNPC;
import myGame.multiplayer.ProtocolClient;
import tage.Camera;
import tage.CameraOrbit3D;
import tage.Engine;
import tage.GameObject;
import tage.Light;
import tage.ObjShape;
import tage.TextureImage;
import tage.VariableFrameRateGame;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import tage.networking.IGameConnection.ProtocolType;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;
import tage.shapes.Cube;
import tage.shapes.ImportedModel;
import tage.shapes.RoomBox;
import tage.shapes.Sphere;
import tage.shapes.TerrainPlane;
import tage.shapes.AnimatedShape;



public class MyGame extends VariableFrameRateGame
{
	
	//NPC
	private ObjShape npcShape;
	private TextureImage npcTex;
	
	public ObjShape getNPCshape() { return npcShape;}
	public TextureImage getNPCtexture() { return npcTex;}
	
	
	
	//Sound
	private IAudioManager audioMgr;
	private Sound thumpSound;
	
	
	private int fluffyClouds, lakeislands; //skyboxes	
	private static InputManager im;
	private CameraOrbit3D orbitController;
	
	//Physics
	private PhysicsEngine physicsEngine;
	private PhysicsObject caps1P, caps2P, planeP;
	private boolean running = false;
	private float vals[] = new float[16];
	
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

	private GameObject dol, terr, cube, brick;
	private ObjShape terrS, ghostS, cubeS, brickS;
	private AnimatedShape dolS;
	private TextureImage doltx, grass, heightmap, ghostT, cubetx, bricktx;
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
	{	dolS = new AnimatedShape("car.rkm", "car.rks");
		dolS.loadAnimation("DRIVE", "car.rka");
		ghostS = new Sphere();
		terrS = new TerrainPlane(1000); //1000x1000
		brickS = new RoomBox();
		cubeS = new Cube();
		npcShape = new ImportedModel("placeholder_guy.obj");


	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("car_tex.png");
		heightmap = new TextureImage("tempHeightMap.jpg");
		grass = new TextureImage("grass.jpg");
		ghostT = new TextureImage("Dolphin_HighPolyUV_wireframe.png");
		cubetx = new TextureImage("wood.jpg");
		bricktx = new TextureImage("brick.jpg");
		npcTex = new TextureImage("placeholder_uv.png");


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
    {    Matrix4f initialTranslation, initialScale;

        // build dolphin in the center of the window
        dol = new GameObject(GameObject.root(), dolS, doltx);
        initialTranslation = (new Matrix4f()).translation(50,0,50);
        initialScale = (new Matrix4f()).scaling(0.25f);
        dol.setLocalTranslation(initialTranslation);
        dol.setLocalScale(initialScale);
        //PHYSICS TESTING
        dol.getRenderStates().setModelOrientationCorrection(
                (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(270.0f)));
                
        
		cube = new GameObject(GameObject.root(), cubeS, cubetx);
        cube.setLocalTranslation((new Matrix4f()).translation(-1, 2, 2));
        initialScale = (new Matrix4f()).scaling(2.0f, 0.5f, 1.0f);
        cube.setLocalScale(initialScale);      //  cube.getRenderStates().setModelOrientationCorrection((new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f)));
        // --------------- adding a ground plane ------------
       // plane = new GameObject(GameObject.root(), planeS, ghostT);
        //plane.setLocalTranslation((new Matrix4f()).translation(0, -2.75f, 0));
        //plane.setLocalScale((new Matrix4f()).scaling(8f));

        
        
        
        brick = new GameObject(GameObject.root(), brickS, bricktx);
        initialTranslation = (new Matrix4f()).translation(0f,1f,0f);
        brick.setLocalTranslation(initialTranslation);
        initialScale = (new Matrix4f()).scaling(20.0f, 10.0f, 20.0f);
        brick.setLocalScale(initialScale);
       
 
        // build terrain object
        terr = new GameObject(GameObject.root(), terrS, grass);
        initialTranslation = (new Matrix4f()).translation(0f,-1f,0f);
        terr.setLocalTranslation(initialTranslation);
        initialScale = (new Matrix4f()).scaling(100.0f, 1.0f, 100.0f);
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
	public void loadSounds() {
		AudioResource resource1;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("assets/sounds/thump.wav", AudioResourceType.AUDIO_SAMPLE);
		thumpSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		thumpSound.initialize(audioMgr);
		thumpSound.setMaxDistance(10.0f);
		thumpSound.setMinDistance(0.5f);
		thumpSound.setRollOff(5.0f);
		
		
	}
	
	
	
	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		Camera c = (engine.getRenderSystem())
				.getViewport("MAIN").getCamera();
		String gp = im.getFirstGamepadName();
		orbitController = new CameraOrbit3D(c, dol, gp, engine);
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

		//play animation
		dolS.playAnimation("DRIVE", 0.5f, AnimatedShape.EndType.LOOP, 0);
	
	
	//phys	
		float[] gravity = {0f,-10f, 0f};
		physicsEngine = (engine.getSceneGraph().getPhysicsEngine());
		physicsEngine.setGravity(gravity);
		
		float mass = 10.0f;
		float up[] = {0,5,0};
		float size[] = {8,2,3};
		float radius = 0.75f;
		float height = 2.0f;
		double[] tempTransform;	
		
		Matrix4f translation = new Matrix4f(cube.getLocalTranslation());
		/*tempTransform = toDoubleArray(translation.get(vals));
		caps1P = engine.getSceneGraph().addPhysicsCapsuleX(mass, tempTransform, radius, height);
		caps1P.setBounciness(0.2F);
		dol.setPhysicsObject(caps1P);
		*/
		translation = new Matrix4f(cube.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		caps2P = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);


		// Increase friction
		float newFriction = 1.0f; // Adjust the friction value as needed
		caps2P.setFriction(newFriction);

		// Lower bounciness
		float newBounciness = 1f; // Adjust the bounciness value as needed
		caps2P.setBounciness(newBounciness);

		// Apply damping
		float linearDamping = 0.5f; // Adjust linear damping as needed
		float angularDamping = 1f; // Adjust angular damping as needed
		caps2P.setDamping(linearDamping, angularDamping);

		//caps2P.setBounciness(10f);
		cube.setPhysicsObject(caps2P);
		
		translation = new Matrix4f(terr.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(
		tempTransform, up, 0.0f);
		planeP.setBounciness(1f);
		terr.setPhysicsObject(planeP);
		
		engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();
		thumpSound.setLocation(cube.getWorldLocation());
		setEarParameters();	
	
	}
	public void setEarParameters() {
		Camera camera = engine.getRenderSystem().getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(dol.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
		
	}
	//phys functions
	private float[] toFloatArray(double[] arr) {
		if(arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for(int i = 0; i < n; i++) {
			ret[i] = (float)arr[i];
		}
		return ret;
	}
	
	private double[] toDoubleArray(float[] arr) {
		if(arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for(int i = 0; i < n; i++) {
			ret[i] = (double)arr[i];
		}
		return ret;	
	}
	boolean gravityDown =true;
	float[] up = {0f,10f, 0f};
	float[] down = {0f,-10f, 0f};
	private void checkForCollisions() {
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
		dynamicsWorld =
		((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i=0; i<manifoldCount; i++)
		{ manifold = dispatcher.getManifoldByIndexInternal(i);
		object1 =
		(com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
		object2 =
		(com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
		JBulletPhysicsObject obj1 =
		JBulletPhysicsObject.getJBulletPhysicsObject(object1);
		JBulletPhysicsObject obj2 =
		JBulletPhysicsObject.getJBulletPhysicsObject(object2);
		for (int j = 0; j < manifold.getNumContacts(); j++)
		{ contactPoint = manifold.getContactPoint(j);
		if (contactPoint.getDistance() < 0.0f)
		{ System.out.println("---- hit between " + obj1 + " and " + obj2);
		/*if(gravityDown) {
			physicsEngine.setGravity(up);
			gravityDown=false;
			System.out.print(gravityDown + " should be false");
		}else {
			physicsEngine.setGravity(down);
			gravityDown=true;
			System.out.print(gravityDown + " should be true");
			}*/
		break;
		} } } 
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
		//phys
		Matrix4f currentTranslation, currentRotation;
		double elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		//phys
		if(running) {
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsedTime);
			for(GameObject go:engine.getSceneGraph().getGameObjects()) {
				if(go.getPhysicsObject() != null) {
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30());
					mat2.set(3,1,mat.m31());
					mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);
					
					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
					int cords = (int)go.getWorldLocation().y();
					if(checkCollision(dol, cube)) {
				        Matrix4f initialTranslation = (new Matrix4f()).translation(-15,0,-20);
				        dol.setLocalTranslation(initialTranslation);
						//System.out.println(checkCollision(dol, cube));

					}
					if(cords == 0 && gravityDown) {
						physicsEngine.setGravity(up);
						gravityDown=false;
						thumpSound.play(thumpSound.getVolume(), false);
					}else if(cords==4 && !gravityDown) {
						physicsEngine.setGravity(down);
						gravityDown=true;
					}
				}
			}
		}
		orbitController.updateCameraPosition();
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		if (!paused) elapsTime += (currFrameTime - lastFrameTime) / 1000.0;
		im.update((float)elapsTime);

		// build and set HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "coords = " + dol.getLocalTranslation();
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);
		
		// update altitude of dolphin based on height map
		Vector3f loc = dol.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		dol.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));
		thumpSound.setLocation(cube.getWorldLocation());
		setEarParameters();
		

		//applyJumpForce(1533);

		//update animation
		dolS.updateAnimation();

	//
		
		processNetworking((float)elapsedTime);
		if(checkCollision(dol, GhostNPC.location,2) || checkCollision(dol, GhostNPC.location1, 4)) {
	        Matrix4f initialTranslation = (new Matrix4f()).translation(-13,0,-10);
	        dol.setLocalTranslation(initialTranslation);	
	        running=true;
	        }
	}

	protected void processNetworking(float elapsTime) {
		//Process packets recieved by the client from the server
		if(protClient != null) protClient.processPackets();
	}
	
	
	@Override
	public void keyPressed(KeyEvent e)
	{	

		switch (e.getKeyCode())
		{	case KeyEvent.VK_SPACE:
			if(!running) {
			running=true;
			}
			
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
	
	
    public static boolean checkCollision(GameObject obj1, GameObject obj2) {
    	//(!MyGame.isRiding()) return false;
        // Get the world positions of the objects
        Vector3f pos1 = obj1.getWorldLocation();
        Vector3f pos2 = obj2.getWorldLocation();

        // Set a threshold distance for collision
        float collisionThreshold = 2f; // Adjust this value based on your scene scale

        // Calculate the distance between the objects
        float distance = pos1.distance(pos2);

        // Check if the distance is less than the collision threshold
        boolean collision = distance < collisionThreshold;
        //System.out.println(distance);
        // Print the collision status


        return collision;
    }
    public static boolean checkCollision(GameObject obj1, Vector3f obj2, float thresh) {
    	//(!MyGame.isRiding()) return false;
        // Get the world positions of the objects
        Vector3f pos1 = obj1.getWorldLocation();
        Vector3f pos2 = obj2;

        // Set a threshold distance for collision
        float collisionThreshold = thresh; // Adjust this value based on your scene scale

        // Calculate the distance between the objects
        float distance = pos1.distance(pos2);

        // Check if the distance is less than the collision threshold
        boolean collision = distance < collisionThreshold;
        //System.out.println(distance);
        // Print the collision status


        return collision;
    }
	
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	   public void applyJumpForce(float jumpForceMagnitude) {
	        // Check if the physics object exists
	        if (cube.getPhysicsObject() != null) {
	            // Apply an upward force to simulate jumping
	            cube.getPhysicsObject().applyForce(0, jumpForceMagnitude, 0, 0, jumpForceMagnitude, 0); // Applying force in the Y direction
	            System.out.print("jumping");
	        }
	    }
	   
	
}