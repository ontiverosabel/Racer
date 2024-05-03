package tage.nodeControllers;

import org.joml.Matrix4f;

import tage.Engine;
import tage.GameObject;
import tage.NodeController;
/**
* A Stretch Controller is a node controller that, when enabled, causes any object
* it is attached to to stretch in an out.
* @author Abel Ontiveros
*/
public class StretchController extends NodeController{
	private float scaleRate = .0003f;
	private float cycleTime = 2000.0f;
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	private Matrix4f curScale, newScale;
	private Engine engine;
	public StretchController(Engine e, float ctime)
	{ super();
	cycleTime = ctime;
	engine = e;
	newScale = new Matrix4f();
	}
	public void apply(GameObject go)
	{ float elapsedTime = super.getElapsedTime();
	totalTime += elapsedTime/1000.0f;
	if (totalTime > cycleTime)
	{ direction = -direction;
	totalTime = 0.0f;
	}
	curScale = go.getLocalScale();
	float scaleAmt = 1.0f + direction * scaleRate * elapsedTime;
	newScale.scaling(curScale.m00()*scaleAmt, curScale.m11(), curScale.m22());
	go.setLocalScale(newScale);
	} }