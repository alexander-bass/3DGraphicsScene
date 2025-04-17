package a4;

import org.joml.Matrix4f;
import org.joml.Vector3f;

// Simple camera that can move around a scene using manipulation of u, v, and n vectors

public class Camera {
    private Vector3f u, v, n;
    private Vector3f location;
    
    private Matrix4f view, viewR, viewT;

    // variables for speed of movement/rotation
    private float moveSpeed, yawSens, pitchSens;

    // temps
    private Vector3f tempForward, tempUp, tempRight;

    // variables for scaling speed of movement with time
    private float speed, yawSpeed, pitchSpeed;

    public Camera() {
        location = new Vector3f();
        
        // point down neg z axis
        u = new Vector3f(1, 0, 0);
        v = new Vector3f(0, 1, 0);
        n = new Vector3f(0, 0, -1);

        view = new Matrix4f();
        viewR = new Matrix4f();
        viewT = new Matrix4f();

        tempForward = new Vector3f();
        tempUp = new Vector3f();
        tempRight = new Vector3f();

        moveSpeed = 2.0f; yawSens = 2.0f; pitchSens = 2.0f;
    }

    // build the view matrix by multiplying translation and rotation matrices
    public Matrix4f getViewMatrix() { 
        viewT.set(1.0f, 0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f, 0.0f,
		-location.x(), -location.y(), -location.z(), 1.0f);

		viewR.set(u.x(), v.x(), -n.x(), 0.0f,
		u.y(), v.y(), -n.y(), 0.0f,
		u.z(), v.z(), -n.z(), 0.0f,
		0.0f, 0.0f, 0.0f, 1.0f);

		view.identity();
		view.mul(viewR);
		view.mul(viewT);

		return(view);
    }

    // moves the camera along it's u, v, and n vectors depending on input from the user
    public void move(float forwardInput, float rightInput, float upInput, float deltaTime) {
        speed = moveSpeed * deltaTime;  // scale with deltaTime, time between displays

        tempForward.set(n).mul(forwardInput * speed);
        tempUp.set(v).mul(upInput * speed);
        tempRight.set(u).mul(rightInput * speed);

        location.add(tempForward)
            .add(tempUp)
            .add(tempRight);
    }

    // rotate the camera around its v or u axis, depending on input from the user
    public void rotate(float yawInput, float pitchInput, float deltaTime) {
        yawSpeed = yawSens * deltaTime; // scale with deltaTime
        pitchSpeed = pitchSens * deltaTime; // scale with deltaTime

        // rotate using euler angles
        // yaw first
        tempRight.set(u).rotateAxis(yawInput * yawSpeed, v.x, v.y, v.z);
        tempForward.set(n).rotateAxis(yawInput * yawSpeed, v.x, v.y, v.z);

        u.set(tempRight);
        n.set(tempForward);

        // then pitch
        tempUp.set(v).rotateAxis(pitchInput * pitchSpeed, u.x, u.y, u.z);
        tempForward.set(n).rotateAxis(pitchInput * pitchSpeed, u.x, u.y, u.z);

        v.set(tempUp);
        n.set(tempForward);
    }

    // change the camera's orientation by looking at a specific point
    public void lookAt(float x, float y, float z) {
		n.set((new Vector3f(x-location.x(), y-location.y(), z-location.z())).normalize());
		Vector3f copyN = new Vector3f(n);
		if ((n.equals(0,1,0)) || (n.equals(0,-1,0)))
			u = new Vector3f(1f,0f,0f);
		else
			u = (new Vector3f(copyN.cross(0f,1f,0f))).normalize();
		Vector3f copyU = new Vector3f(u);
		v = (new Vector3f(copyU.cross(n))).normalize();
	}
    
    // gets and sets
    public void setLocation(Vector3f newLocation) { location.set(newLocation); }
    public void setMoveSpeed(float speed ) { moveSpeed = speed; }
    public void setYawSens(float sens ) { yawSens = sens; }
    public void setPitchSens(float sens ) { pitchSens = sens; }
    public Vector3f getLocation() { return new Vector3f(location); }
    public float getMoveSpeed() { return moveSpeed; }
    public float getYawSens() { return yawSens; }
    public float getPitchSens() { return pitchSens; }

}
