package myGame;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.multiplayer.GhostManager;
import myGame.multiplayer.ProtocolClient;
import tage.Camera;
import tage.CameraOrbit3D;
import tage.Engine;
import tage.GameObject;
import tage.Light;
import tage.Light.LightType;
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
import tage.shapes.AnimatedShape;
import tage.shapes.Cube;
import tage.shapes.ImportedModel;
import tage.shapes.Plane;
import tage.shapes.RoomBox;
import tage.shapes.Sphere;
import tage.shapes.TerrainPlane;



public class MyGame extends VariableFrameRateGame
{
	
	public boolean blue,red,jumping, start= false;
	
	public ArrayList<Integer> cubeIDS = new ArrayList<Integer>();
	public ArrayList<Integer> jumpIDS = new ArrayList<Integer>();

	
	private static String hud1 = "";
	private static String bluedriver = "waiting...";
	private static String reddriver = "waiting...";
	private static String hud2 = "Blue Car: " + bluedriver + " Red Car: " + reddriver;
	
	
	//NPC
	private ObjShape blueNPCShape, redNPCShape;
	private TextureImage blueCartx, redCartx;
	
	public ObjShape getBlueNPCshape() { return blueNPCShape;}
	public TextureImage getBlueNPCtexture() { return blueCartx;}
	
	public ObjShape getRedNPCshape() { return redNPCShape;}
	public TextureImage getRedNPCtexture() { return redCartx;}
	
	//Sound
	private IAudioManager audioMgr;
	private Sound thumpSound, finishSound;
	

	private int fluffyClouds, lakeislands; //skyboxes	
	private static InputManager im;
	private CameraOrbit3D orbitController;
	
	//Physics
	private PhysicsEngine physicsEngine;
	private PhysicsObject avatarPhys, RedNPCPhys, finishlinePhys, BlueNPCPhys, CubePhys, PlanePhys, trampPhys;
	private boolean running = false;
	private float vals[] = new float[16];
	private int avatarUID, cubeUID, blueNPCUID, redNPCUID, wallsUID;
	
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
	
	private GameObject cube, jump;
	private GameObject dol, terr, finishline, avatar,bluecar,redcar, trophy;
	private ObjShape terrS, ghostS, cubeS, finishlineS, dolS, trophyS;
	private AnimatedShape redCarS, blueCarS;
	private TextureImage doltx, grass, heightmap, finishlinetx, ghostT, cubetx, bricktx, trampoline, trophytx;
	private Light light1, spotlight, positional;

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
	public void loadShapes() {
		System.out.print("ercfr");

		dolS = new ImportedModel("placeholder_guy.obj");
		ghostS = new Sphere();
		terrS = new TerrainPlane(1000); //1000x1000
		cubeS = new Cube();
		finishlineS = new Plane();
		trophyS = new ImportedModel("dolphinHighPoly.obj");
		
		
		blueNPCShape = new ImportedModel("car.obj");
		redNPCShape = new ImportedModel("car.obj");


		
		blueCarS = new AnimatedShape("car.rkm", "car.rks");
		blueCarS.loadAnimation("DRIVE", "car.rka");
		redCarS = new AnimatedShape("car.rkm", "car.rks");
		redCarS.loadAnimation("DRIVE", "car.rka");
	}

	@Override
	public void loadTextures()
	{	
		doltx = new TextureImage("placeholder_uv.png");
		heightmap = new TextureImage("tempHeightMap.jpg");
		grass = new TextureImage("grass.jpg");
		ghostT = new TextureImage("Dolphin_HighPolyUV_wireframe.png");
		cubetx = new TextureImage("wood.jpg");
		bricktx = new TextureImage("brick.jpg");
		blueCartx = new TextureImage("car_texB.png");
		redCartx = new TextureImage("car_tex.png");
		trampoline = new TextureImage("jump.jpg");
		finishlinetx = new TextureImage("finishline.jpg");
		trophytx = new TextureImage("Dolphin_HighPolyUV.png");

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
        initialTranslation = (new Matrix4f()).translation(10,2,-80);
        initialScale = (new Matrix4f()).scaling(0.25f);
        dol.setLocalTranslation(initialTranslation);
        dol.setLocalScale(initialScale);
        //PHYSICS TESTING
       dol.getRenderStates().setModelOrientationCorrection(
               (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90f)));
                        
        //build redcar and blueCar
        bluecar = new GameObject(GameObject.root(), blueCarS, blueCartx);
        initialTranslation = (new Matrix4f()).translation(10,2,-80);
        initialScale = (new Matrix4f()).scaling(0.25f);
        bluecar.setLocalTranslation(initialTranslation);
        bluecar.setLocalScale(initialScale);
        bluecar.getRenderStates().disableRendering();
        
        redcar = new GameObject(GameObject.root(), redCarS, redCartx);
        initialTranslation = (new Matrix4f()).translation(10,2,-80);
        initialScale = (new Matrix4f()).scaling(0.25f);
        redcar.setLocalTranslation(initialTranslation);
        redcar.setLocalScale(initialScale);
        redcar.getRenderStates().disableRendering();
        

        finishline = new GameObject(GameObject.root(), finishlineS, finishlinetx);
        finishline.setLocalTranslation((new Matrix4f()).translation(0, 1, 90));
         initialScale = (new Matrix4f()).scaling(50.0f, 1f, 1.5f);
        finishline.setLocalScale(initialScale);

		//trophy object
		trophy = new GameObject(GameObject.root(), trophyS, trophytx);
        
        // --------------- adding a ground plane ------------  
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
	
    private Random random = new Random();
    public void addCube(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        // Generate random position within the specified range
        float randomX = minX + random.nextFloat() * (maxX - minX);
        float randomY = minY + random.nextFloat() * (maxY - minY);
        float randomZ = minZ + random.nextFloat() * (maxZ - minZ);
		float mass = 100.0f;
		float size[] = {8,2,2};
        // Create cube GameObject and set its position
        cube = new GameObject(GameObject.root(), cubeS, cubetx);
        cube.setLocalTranslation((new Matrix4f()).translation(randomX, randomY, randomZ));
        Matrix4f initialScale = (new Matrix4f()).scaling(4.0f, 0.75f, 1.0f);
        cube.setLocalScale(initialScale);
		//Cube Phys
        Matrix4f translation = new Matrix4f(cube.getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		CubePhys = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
		// Increase friction
		float newFriction = 1.0f; // Adjust the friction value as needed
		CubePhys.setFriction(newFriction);
		// Lower bounciness
		float newBounciness = 0f; // Adjust the bounciness value as needed
		CubePhys.setBounciness(newBounciness);
		// Apply damping
		float linearDamping = 0.5f; // Adjust linear damping as needed
		 float angularDamping = 1f; // Adjust angular damping as needed
		CubePhys.setDamping(linearDamping, angularDamping);
		//caps2P.setBounciness(10f);
		cube.setPhysicsObject(CubePhys);
		cubeIDS.add(CubePhys.getUID());
		
    }

    public void spawnCubes(int numCubes, float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        for (int i = 0; i < numCubes; i++) {
            addCube(minX, maxX, minY, maxY, minZ, maxZ);
        }
    }
    public void addTramps(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        // Generate random position within the specified range
        float randomX = minX + random.nextFloat() * (maxX - minX);
        float randomY = minY + random.nextFloat() * (maxY - minY);
        float randomZ = minZ + random.nextFloat() * (maxZ - minZ);
		float mass = 100.0f;
		float size[] = {2,2,2};
        // Create tramp GameObject and set its position
        jump = new GameObject(GameObject.root(), cubeS, trampoline);
        jump.setLocalTranslation((new Matrix4f()).translation(randomX, randomY, randomZ));
        Matrix4f initialScale = (new Matrix4f()).scaling(2.0f, 0.75f, 1.0f);
        jump.setLocalScale(initialScale);

        
        Matrix4f translation = new Matrix4f(jump.getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		trampPhys = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
		float newFriction = 1.0f; // Adjust the friction value as needed
		trampPhys.setFriction(newFriction);
		float newBounciness = 0f; // Adjust the bounciness value as needed
		trampPhys.setBounciness(newBounciness);
		// Apply damping
		float linearDamping = 0.5f; // Adjust linear damping as needed
		 float angularDamping = 1f; // Adjust angular damping as needed
		 trampPhys.setDamping(linearDamping, angularDamping);
		//caps2P.setBounciness(10f);
		jump.setPhysicsObject(trampPhys);
		jumpIDS.add(trampPhys.getUID());
		
    }

    public void spawnTramps(int numTramps, float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        for (int i = 0; i < numTramps; i++) {
            addTramps(minX, maxX, minY, maxY, minZ, maxZ);
        }
    }
	@Override
	public void initializeLights()
	{	
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);

		//spotlight
		spotlight = new Light();
		spotlight.setType(LightType.SPOTLIGHT);
		spotlight.setLocation(new Vector3f(0f, 1f, 90f));
		(engine.getSceneGraph()).addLight(spotlight);

		//positional light
		positional = new Light();
		positional.setType(LightType.POSITIONAL);
		positional.setLocation(new Vector3f(1f, 0, 0));
		(engine.getSceneGraph()).addLight(positional);
	}

	@Override
	public void loadSounds() {
		AudioResource resource1, resource2;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("assets/sounds/thump.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("assets/sounds/finishlinesound.wav", AudioResourceType.AUDIO_SAMPLE);

		thumpSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		thumpSound.initialize(audioMgr);
		thumpSound.setMaxDistance(10.0f);
		thumpSound.setMinDistance(0.5f);
		thumpSound.setRollOff(5.0f);
		
		
		
		finishSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		finishSound.initialize(audioMgr);
		finishSound.setMaxDistance(10.0f);
		finishSound.setMinDistance(0.5f);
		finishSound.setRollOff(5.0f);
		
	}
	
	
	
	public void setupNPCPhys() {
		float mass = 0.0f;
		float radius = 2f;
		float height = 0.5f;
		
		//blue npc
		Matrix4f translation = new Matrix4f(protClient.getBlueNPC().getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		BlueNPCPhys = engine.getSceneGraph().addPhysicsCapsule(mass, tempTransform, radius, height);
		BlueNPCPhys.setBounciness(0.2F);
		protClient.getBlueNPC().setPhysicsObject(BlueNPCPhys);
		blueNPCUID = BlueNPCPhys.getUID();
		
		
		
		//red npc
		translation = new Matrix4f(protClient.getRedNPC().getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		RedNPCPhys = engine.getSceneGraph().addPhysicsCapsule(mass, tempTransform, radius, height);
		RedNPCPhys.setBounciness(0.2F);
		protClient.getRedNPC().setPhysicsObject(RedNPCPhys);
		redNPCUID = RedNPCPhys.getUID();
	}
	
	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		Camera c = (engine.getRenderSystem())
				.getViewport("MAIN").getCamera();
		String gp = im.getFirstGamepadName();
		avatar=dol;
		orbitController = new CameraOrbit3D(c, avatar, gp, engine);
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);
		setupNetworking();


		// ------------- inputs section ------------------
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction yawAction = new TurnAction(this, protClient);

		//gamepad controls
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._4, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._7, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._6, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._5, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		//keyboard controls
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, yawAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,5));
		//dolS.playAnimation("DRIVE", 0.5f, AnimatedShape.EndType.LOOP, 0);
	
	//phys	
		float[] gravity = {0f,-10f, 0f};
		physicsEngine = (engine.getSceneGraph().getPhysicsEngine());
		physicsEngine.setGravity(gravity);
		
		float mass = 1.0f;
		float radius = 0.75f;
		float height = 1f;
		double[] tempTransform;	
		float up[] = {0,1,0};
		
		
		//Avatar Phys
		Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		avatarPhys = engine.getSceneGraph().addPhysicsCapsule(mass, tempTransform, radius, height);
		//avatarPhys.setBounciness(0.2F);
		avatarPhys.setBounciness(0f);
		float linearDamping = 0.5f; // Adjust linear damping as needed
		float angularDamping = 1f; // Adjust angular damping as needed
		avatarPhys.setDamping(linearDamping, angularDamping);
		avatar.setPhysicsObject(avatarPhys);
		avatarUID = avatarPhys.getUID();
		
		
		
		//finish line phys
			mass=0;
			float size[] = {100,1,3};
	        translation = new Matrix4f(finishline.getLocalTranslation());
			tempTransform = toDoubleArray(translation.get(vals));
			finishlinePhys = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
			// Increase friction
			float newFriction = 1.0f; // Adjust the friction value as needed
			finishlinePhys.setFriction(newFriction);
			// Lower bounciness
			float newBounciness = 0f; // Adjust the bounciness value as needed
			finishlinePhys.setBounciness(newBounciness);
			// Apply damping
			 linearDamping = 0.5f; // Adjust linear damping as needed
			 angularDamping = 1f; // Adjust angular damping as needed
			 finishlinePhys.setDamping(linearDamping, angularDamping);
			//caps2P.setBounciness(10f);
			finishline.setPhysicsObject(finishlinePhys);		
		

		
		
		//PLANE PHYS
		translation = new Matrix4f(terr.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		PlanePhys = (engine.getSceneGraph()).addPhysicsStaticPlane(
		tempTransform, up, 0.0f);
		PlanePhys.setBounciness(1.0f);
		terr.setPhysicsObject(PlanePhys);
		
		engine.enableGraphicsWorldRender();
		engine.enablePhysicsWorldRender();
		finishSound.setLocation(finishline.getWorldLocation());
		setEarParameters();		
	}
	public void setEarParameters() {
		Camera camera = engine.getRenderSystem().getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
		
	}
	public void resetAvatar() {
		System.out.println("resetting avatars.");
		blue=false;
		red=false;
		bluedriver="waiting...";
		reddriver="waiting...";
		avatar.setPhysicsObject(null);
		avatar.getRenderStates().disableRendering();
		avatar=dol;
		orbitController.setAvatar(dol);
		setEarParameters();
        Matrix4f initialTranslation = (new Matrix4f()).translation(50,10,50);
        avatar.setLocalTranslation(initialTranslation);
		avatar.setPhysicsObject(avatarPhys);
		avatar.getRenderStates().enableRendering();
	}
	
	
	public void changeAvatar(GameObject newAvatar) {
		if(avatar == bluecar || avatar == redcar) {
			System.out.println("avatar already selected!");
		}else {
		if((newAvatar == bluecar && !blue) || (newAvatar == redcar && !red)) {
		avatar.setPhysicsObject(null);
		avatar.getRenderStates().disableRendering();
		avatar=newAvatar;
		orbitController.setAvatar(newAvatar);
		setEarParameters();
        Matrix4f initialTranslation = (new Matrix4f()).translation(10,2,-80);
        avatar.setLocalTranslation(initialTranslation);
		avatar.setPhysicsObject(avatarPhys);
		avatar.getRenderStates().enableRendering();
		}if(avatar == bluecar ) {
				blue=true;
				bluedriver="You";
				protClient.sendAvatarMsg("blue");
				blueCarS.playAnimation("DRIVE", 0.5f, AnimatedShape.EndType.LOOP, 0);
				System.out.println("setting blue to true");
		}else if(avatar == redcar){
				red=true;
				reddriver="You";
				protClient.sendAvatarMsg("red");
				redCarS.playAnimation("DRIVE", 0.5f, AnimatedShape.EndType.LOOP, 0);

		}
		
	}}
	
	public void changeGhost(UUID id, String color) {
		System.out.println("ID " + id + " color " + color);
		if(color.equalsIgnoreCase("blue")) { 
			if(!blue) {
			getGhostManager().changeShape(blueCarS);
			getGhostManager().changeTex(blueCartx);
			blue=true;
			bluedriver="Player 2";
		}
		}else if(color.equalsIgnoreCase("red")) {
			if(!red) {
				getGhostManager().changeShape(redCarS);
				getGhostManager().changeTex(redCartx);
				red=true;
				reddriver="Player 2";
			}
		}
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
		object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
		object2 =
		(com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
		JBulletPhysicsObject obj1 =
		JBulletPhysicsObject.getJBulletPhysicsObject(object1);
		JBulletPhysicsObject obj2 =
		JBulletPhysicsObject.getJBulletPhysicsObject(object2);
		for (int j = 0; j < manifold.getNumContacts(); j++)
		{ contactPoint = manifold.getContactPoint(j);
		if (contactPoint.getDistance() < 0.0f)
		{ //System.out.println("---- hit between " + obj1 + " and " + obj2);
			//System.out.println(avatar.getPhysicsObject().toString());
			if(obj1.getUID() == avatar.getPhysicsObject().getUID() || obj2.getUID() == avatar.getPhysicsObject().getUID()) {
		//	avJBPhys = obj1;
				if(obj2.getUID() == blueNPCUID) {
					changeAvatar(bluecar);
					
				}
				
				if(obj2.getUID() == redNPCUID) {
					changeAvatar(redcar);
				}
				
				if(cubeIDS.contains(obj2.getUID()) || cubeIDS.contains(obj1.getUID())) {
					System.out.print("cube collision");
					applyBackwardForce();
					    }
				if(jumpIDS.contains(obj2.getUID()) || jumpIDS.contains(obj1.getUID())) {
					System.out.print("jump collision");
					jumping=true;		
					applyJumpForce(10);
					//thumpSound.setLocation(cube.getWorldLocation());

					    }
				
				else if(obj2.getUID() == PlanePhys.getUID()) {
					    	jumping=false;
							//System.out.print("ground collision");

					    }
				
			}
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
		System.out.print("Setting up networking...");
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
	} }
	
	
	
	@Override
	public void update()
	{	
		//phys
		Matrix4f currentTranslation, currentRotation;
		double elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		//phys
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsedTime);
			for(GameObject go:engine.getSceneGraph().getGameObjects()) {
				if(go.getPhysicsObject() != null ) {
					if(go == avatar && !jumping) {
						Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
						double[] tempTransform = toDoubleArray(translation.get(vals));
						avatarPhys.setTransform(tempTransform);
					}else {
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30());
					mat2.set(3,1,mat.m31());
					mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);
					
					//mat.getRotation(aa);
					//mat3.rotation(aa);
					//go.setLocalRotation(mat3);
					//if(checkCollision(dol, cube)) {
					//if(collision) {
				     //   Matrix4f initialTranslation = (new Matrix4f()).translation(-15,0,-20);
				     //   avatar.setLocalTranslation(initialTranslation);
				     //   collision=false;
						//System.out.println(checkCollision(dol, cube));

					/*}
					if(cords == 0 && gravityDown) {
						physicsEngine.setGravity(up);
						gravityDown=false;
						thumpSound.play(thumpSound.getVolume(), false);
					}else if(cords==4 && !gravityDown) {
						physicsEngine.setGravity(down);
						gravityDown=true;
					}*/
				//}
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
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		hud2 = "Blue Car: " + bluedriver + " Red Car: " + reddriver;
		if((blue && red) && !start) {
			start=true;
			hud1 = "Objective: Reach the finish line.";
	        spawnCubes(15, -100, 30, 1, 1, -50, 50);
	        spawnTramps(6, -100, 100, 1, 1, -50, 50);
			finishSound.play(finishSound.getVolume(), true);

		}else {
			hud1 = "Objective: 2 players must choose a car color.";
		}
		if(!isClientConnected) {
			hud1 = "Objective: Single Player Mode.";
		}
		(engine.getHUDmanager()).setHUD1(hud1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(hud2, hud2Color, 500, 15);
		
		// update altitude of dolphin based on height map
		Vector3f loc = avatar.getWorldLocation();
		int cords = (int)avatar.getWorldLocation().y();
		float height = terr.getHeight(loc.x(), loc.z());
		if(!jumping) {
		avatar.setLocalLocation(new Vector3f(loc.x(), height+1, loc.z()));
		}
		finishSound.setLocation(finishline.getWorldLocation());
		setEarParameters();
		if(avatar==bluecar) {
			blueCarS.updateAnimation();
		}else if(avatar == redcar) {
			redCarS.updateAnimation();
		}

		//applyJumpForce(1533);

	//
		//dolS.updateAnimation();
		processNetworking((float)elapsedTime);
		/*if(checkCollision(avatar, GhostNPC.location,2) || checkCollision(avatar, GhostNPC.location1, 4)) {
	        Matrix4f initialTranslation = (new Matrix4f()).translation(-13,0,-10);
	        avatar.setLocalTranslation(initialTranslation);	
	        running=true;
	        }*/
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
				int x = (int) avatar.getWorldLocation().x();
				int y = (int) avatar.getWorldLocation().y();
				int z = (int) avatar.getWorldLocation().z();

				
				//System.out.println(avatar.getWorldTranslation());
				System.out.println("x: " + x + " y: " + y + " z: " + z);

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
			case KeyEvent.VK_5:
			spotlight.setLocation(new Vector3f(0f, 0f, 0f));
				break;
		}
		super.keyPressed(e);
	}
	public void setAvatar(GameObject newavatar) { avatar=newavatar;}
	public GameObject getAvatar() { return avatar; }
	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }
	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	
    
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}
	   public void applyJumpForce(float jumpForceMagnitude) {
	            // Apply an upward force to simulate jumping
	        	jumping=true;
	            float[] velocity = avatar.getPhysicsObject().getLinearVelocity();
	            velocity[1] = jumpForceMagnitude; // Set the upward velocity
	            avatar.getPhysicsObject().setLinearVelocity(velocity);	        }
	   
public void applyBackwardForce() {
	Vector3f fwd = avatar.getWorldForwardVector();
	Vector3f loc = avatar.getWorldLocation();
	Vector3f newLocation = loc.sub(fwd.mul(1.2f));
	
	avatar.setLocalLocation(newLocation);
	Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
	double[] tempTransform = toDoubleArray(translation.get(vals));
	avatar.getPhysicsObject().setTransform(tempTransform);	
}
}