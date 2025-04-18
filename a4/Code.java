package a4;

import javax.swing.JFrame;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT32;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_LINE_SMOOTH;
import static com.jogamp.opengl.GL.GL_POLYGON_OFFSET_FILL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE2;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_CUBE_MAP;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_COMPARE_REF_TO_TEXTURE;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_FUNC;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_MODE;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_FRONT;

import java.nio.FloatBuffer;
import java.lang.Math;
import java.awt.event.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import a4.shapes.*;

import org.joml.*;

public class Code extends JFrame implements GLEventListener {
    // game initialization variables
    private GLCanvas myCanvas;
    private int renderingProgramDefault, renderingProgramCubeMap, renderingProgramShadow, renderingProgramNoTex;
    private int vao[] = new int[1];
    private int vbo[] = new int[30];
    private Camera cam;
    private InputManager inputManager;

    // variables for imported models and textures
    private int numObjVertices;
    private ImportedModel rubberDuckModel, gnomeModel;
    private int skyboxTexture, groundPlaneTexture, rubberDuckTexture, gnomeTexture;
    private int groundPlaneNormalMap, rubberDuckNormalMap, gnomeNormalMap;

    // display function variables
    private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
    private Matrix4f pMat = new Matrix4f();
    private Matrix4f vMat = new Matrix4f();
    private Matrix4f mMat = new Matrix4f();
    private Matrix4f invTrMat = new Matrix4f();
    private int mLoc, pLoc, tfLoc, acLoc, vLoc, nLoc;
    private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
    private float aspect;
    private long prevDisplayTime, currDisplayTime;
    private float deltaTime;
    private boolean renderAxes = false;
    private boolean spaceIsPressed;
    private boolean spaceWasPressed = false;

    // camera control input variables
    private float yaw, pitch, forward, up, right;

    // rotate facade
    private Quaternionf rotateQuat = new Quaternionf();

    // inital positions
    private Vector3f initialLightPos = new Vector3f(0f, 0f, 3.0f);
    private Vector3f initialDuckPos = new Vector3f(0, 0, 1);
    private Vector3f initialCameraPos = new Vector3f(0, 0, 2);
    private Vector3f initialGnomePos = new Vector3f(1, 0, 1);
    private Quaternionf duckRotation = new Quaternionf();
    
    // for the light to point at
    private Vector3f origin = new Vector3f(0, 0, 0);
    private Vector3f upVec = new Vector3f(0, 1, 0);

    // Light control variables
    private float lightSpeed = 1f;
    private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];
    private boolean lightToggleWasPressed = false;
    private boolean renderLight = true;

    // shadow stuff
    private int scSizeX, scSizeY;
    private int[] shadowTex = new int[1];
    private int[] shadowBuffer = new int[1];
    private Matrix4f lightVmat = new Matrix4f();
    private Matrix4f lightPmat = new Matrix4f();
    private Matrix4f shadowMVP1 = new Matrix4f();
    private Matrix4f shadowMVP2 = new Matrix4f();
    private Matrix4f b = new Matrix4f();
    private int sLoc;

    // white light properties
    private float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 0.6f };
	private float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };


    public Code() {
        // setup window
        setTitle("CSC155 - Assignment 3");
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // setup GLCanvas
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        inputManager = new InputManager(this);
        myCanvas.addKeyListener(inputManager);

        this.add(myCanvas);
        this.setVisible(true);

        prevDisplayTime = System.nanoTime();
        Animator animtr = new Animator(myCanvas);
        animtr.start();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // get time since last display
        currDisplayTime = System.nanoTime();
        deltaTime = (currDisplayTime - prevDisplayTime) / 1e9f;
        prevDisplayTime = currDisplayTime;
        
        // call input handler every frame for smooth movement
        handleInput(deltaTime);
        
        // render all of the objects
        renderScene();
    }

    public void renderScene() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        gl.glClear(GL_COLOR_BUFFER_BIT);
        gl.glClearColor(0, 0.2f, 0.2f, 1);
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        vMat = cam.getViewMatrix();

        drawSkybox();
        drawWorldAxes();
        drawLight();

        lightVmat.identity().setLookAt(currentLightPos, origin, upVec);
        lightPmat.identity().setPerspective((float) Math.toRadians(90), aspect, 0.1f, 1000.0f);

        gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
        gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);

        gl.glDrawBuffer(GL_NONE);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(2.0f, 4.0f);

        passOne();

        gl.glDisable(GL_POLYGON_OFFSET_FILL);

        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

        gl.glDrawBuffer(GL_FRONT);

        passTwo();

    }

    private void passOne() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(renderingProgramShadow);

        sLoc = gl.glGetUniformLocation(renderingProgramShadow, "shadowMVP");

        gl.glClear(GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        // draw ground plane
        mMat.identity();
        mMat.rotate(rotateQuat.rotationX((float) Math.toRadians(90)));
        mMat.scale(3f);

        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_TRIANGLES, 0, 18);

        // draw duck
        mMat.identity();
        mMat.translate(initialDuckPos);
        mMat.rotate(duckRotation);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);
        
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_TRIANGLES, 0, rubberDuckModel.getNumVertices());
        
        
        // draw gnome
        mMat.identity();
        mMat.translate(initialGnomePos);

        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_TRIANGLES, 0, gnomeModel.getNumVertices());
        
    }

    private void passTwo() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(renderingProgramDefault);

        mLoc = gl.glGetUniformLocation(renderingProgramDefault, "m_matrix");
        vLoc = gl.glGetUniformLocation(renderingProgramDefault, "v_matrix");
        pLoc = gl.glGetUniformLocation(renderingProgramDefault, "p_matrix");
        nLoc = gl.glGetUniformLocation(renderingProgramDefault, "norm_matrix");
        sLoc = gl.glGetUniformLocation(renderingProgramDefault, "shadowMVP");
        tfLoc = gl.glGetUniformLocation(renderingProgramDefault, "tileCount");
        
        installLights();

        gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

        drawEnvironment();
        drawRubberDuck();
        drawGnome();
    }

    private void drawSkybox() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // draw skybox cubemap
        gl.glUseProgram(renderingProgramCubeMap);

		vLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "v_matrix");
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));

		pLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "p_matrix");
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

    }

    private void drawWorldAxes() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        if (!renderAxes) return;

        gl.glUseProgram(renderingProgramNoTex);

        mLoc = gl.glGetUniformLocation(renderingProgramNoTex, "m_matrix");
        vLoc = gl.glGetUniformLocation(renderingProgramNoTex, "v_matrix");
        pLoc = gl.glGetUniformLocation(renderingProgramNoTex, "p_matrix");
        acLoc = gl.glGetUniformLocation(renderingProgramNoTex, "axisColor");

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glEnable(GL_LINE_SMOOTH);
        gl.glLineWidth(3);

        mMat.identity();
        mMat.scale(5);

        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glUniform3f(acLoc, 255, 0, 0);   // specify solid color

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_LINES, 0, 2);

        gl.glUniform3f(acLoc, 0, 255, 0);   // specify solid color

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_LINES, 0, 2);

        gl.glUniform3f(acLoc, 0, 0, 255);   // specify solid color

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_LINES, 0, 2);
    }

    private void drawEnvironment() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        setMaterialPlaster();

        mMat.identity();
        mMat.rotate(rotateQuat.rotationX((float) Math.toRadians(90)));
        mMat.scale(3f);

        mMat.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        shadowMVP2.identity();
        shadowMVP2.mul(b);
        shadowMVP2.mul(lightPmat);
        shadowMVP2.mul(lightVmat);
        shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
        gl.glUniform1i(tfLoc, 1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, groundPlaneTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, groundPlaneNormalMap);
        /*
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        */

		gl.glDrawArrays(GL_TRIANGLES, 0, 18);

        gl.glDisableVertexAttribArray(3);
        gl.glDisableVertexAttribArray(4);
    }
    
    private void drawRubberDuck() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        setMaterialPlaster();
        
        mMat.identity();
        mMat.translate(initialDuckPos);
        mMat.rotate(duckRotation);

        mMat.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        shadowMVP2.identity();
        shadowMVP2.mul(b);
        shadowMVP2.mul(lightPmat);
        shadowMVP2.mul(lightVmat);
        shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
        gl.glUniform1i(tfLoc, 1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, rubberDuckTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, rubberDuckNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, rubberDuckModel.getNumVertices());
    }

    private void drawLight() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        if (!renderLight) return;

        gl.glUseProgram(renderingProgramNoTex);

        mLoc = gl.glGetUniformLocation(renderingProgramNoTex, "m_matrix");
        vLoc = gl.glGetUniformLocation(renderingProgramNoTex, "v_matrix");
        pLoc = gl.glGetUniformLocation(renderingProgramNoTex, "p_matrix");
        acLoc = gl.glGetUniformLocation(renderingProgramNoTex, "axisColor");

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        
        // Draw a small duck at light position
        mMat.identity();
        mMat.translate(currentLightPos);
        mMat.scale(0.3f); // Small size for the light indicator
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glUniform3f(acLoc, 1.0f, 1.0f, 0.0f); // Yellow color
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, rubberDuckModel.getNumVertices());
    }

    private void drawGnome() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        setMaterialPlaster();
        
        mMat.identity();
        mMat.translate(initialGnomePos);

        mMat.invert(invTrMat);
        invTrMat.transpose(invTrMat);

        shadowMVP2.identity();
        shadowMVP2.mul(b);
        shadowMVP2.mul(lightPmat);
        shadowMVP2.mul(lightVmat);
        shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
        gl.glUniform1i(tfLoc, 1);


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, gnomeTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, gnomeNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, gnomeModel.getNumVertices());
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // import models and create rendering program by compiling and linking shaders
        rubberDuckModel = new ImportedModel("assets/models/rubber_duck_toy_1k.obj");
        gnomeModel = new ImportedModel("assets/models/garden_gnome_1k.obj");

        renderingProgramDefault = Utils.createShaderProgram("assets/shaders/default.vert", "assets/shaders/default.frag");
        renderingProgramCubeMap = Utils.createShaderProgram("assets/shaders/cubemap.vert", "assets/shaders/cubemap.frag");
        renderingProgramShadow = Utils.createShaderProgram("assets/shaders/shadowmap.vert", "assets/shaders/shadowmap.frag");
        renderingProgramNoTex = Utils.createShaderProgram("assets/shaders/notex.vert", "assets/shaders/notex.frag");

        // set perspective matrix, only changes when screen is resized
        aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

        // setup the vertex and texture information for all models in the scene
        setupVertices();
        setupShadowBuffers();

        b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);

        // create a camera and define it's initial location and orientation
        cam = new Camera();
        cam.setLocation(initialCameraPos);
        cam.lookAt(0, 0, 0);
        
        // load all textures that will be used
        rubberDuckTexture =  Utils.loadTexture("assets/textures/rubber_duck_toy_diff_1k.jpg");
        rubberDuckNormalMap = Utils.loadTexture("assets/textures/rubber_duck_toy_nor_gl_1k.jpg");

        gnomeTexture = Utils.loadTexture("assets/textures/garden_gnome_diff_1k.jpg");
        gnomeNormalMap = Utils.loadTexture("assets/textures/garden_gnome_nor_gl_1k.jpg");

        groundPlaneTexture = Utils.loadTexture("assets/textures/granite_wall_diff_4k.jpg");
        groundPlaneNormalMap = Utils.loadTexture("assets/textures/granite_wall_nor_gl_4k.jpg");

        skyboxTexture = Utils.loadCubeMap("assets/textures/cubemaps/storm");
        gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

        // Initialize duck position
        duckRotation.identity().rotateY((float) Math.toRadians(90));

        // Initialize light pos
        currentLightPos.set(initialLightPos);
    }

    private void setupVertices() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // varibles for all models within the scene that are not imported
        FloatBuffer vertBuf, texBuf, normBuf, tanBuf, bitanBuf;
        Vector3f[] vertices, normals, tangents, bitangents;
        Vector2f[] texCoords;
        float[] pvalues, tvalues, nvalues, tanvalues, bitanvalues;

        Vector3f origin = new Vector3f(0, 0, 0);
        Line worldXAxis = new Line(origin, new Vector3f(1, 0, 0));
        Line worldYAxis = new Line(origin, new Vector3f(0, 1, 0));
        Line worldZAxis = new Line(origin, new Vector3f(0, 0, 1));
        Plane groundPlane = new Plane();
        Cube skyBox = new Cube();

        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);

        // setup skybox
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        vertBuf = Buffers.newDirectFloatBuffer(skyBox.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        // setup world axes x, y, and z
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        vertBuf = Buffers.newDirectFloatBuffer(worldXAxis.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        vertBuf = Buffers.newDirectFloatBuffer(worldYAxis.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
        vertBuf = Buffers.newDirectFloatBuffer(worldZAxis.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        //setup ground plane
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        vertBuf = Buffers.newDirectFloatBuffer(groundPlane.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
        texBuf = Buffers.newDirectFloatBuffer(groundPlane.getTexCoords());
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        normBuf = Buffers.newDirectFloatBuffer(groundPlane.getNormals());
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
        tanBuf = Buffers.newDirectFloatBuffer(groundPlane.getTangents());
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
        bitanBuf = Buffers.newDirectFloatBuffer(groundPlane.getBitangents());
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // load in rubber duck model info
        numObjVertices = rubberDuckModel.getNumVertices();
        vertices = rubberDuckModel.getVertices();
        texCoords = rubberDuckModel.getTexCoords();
        normals = rubberDuckModel.getNormals();
        tangents = rubberDuckModel.getTangents();
        bitangents = rubberDuckModel.getBitangents();

        pvalues = new float[numObjVertices*3];
        tvalues = new float[numObjVertices*2];
        nvalues = new float[numObjVertices*3];
        tanvalues = new float[numObjVertices*3];
        bitanvalues = new float[numObjVertices*3];

        for (int i = 0; i < numObjVertices; i++) {
            pvalues[i*3] = (float) (vertices[i]).x();
            pvalues[i*3+1] = (float) (vertices[i]).y();
            pvalues[i*3+2] = (float) (vertices[i]).z();

            tvalues[i*2] = (float) (texCoords[i]).x();
            tvalues[i*2+1] = (float) (texCoords[i]).y();

            nvalues[i*3] = (float) (normals[i]).x();
            nvalues[i*3+1] = (float) (normals[i]).y();
            nvalues[i*3+2] = (float) (normals[i]).z();

            tanvalues[i*3] = (float) (tangents[i]).x();
            tanvalues[i*3+1] = (float) (tangents[i]).y();
            tanvalues[i*3+2] = (float) (tangents[i]).z();

            bitanvalues[i*3] = (float) (bitangents[i]).x();
            bitanvalues[i*3+1] = (float) (bitangents[i]).y();
            bitanvalues[i*3+2] = (float) (bitangents[i]).z();
        }

        // setup model vert tex & norms
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // load in gnome model info
        numObjVertices = gnomeModel.getNumVertices();
        vertices = gnomeModel.getVertices();
        texCoords = gnomeModel.getTexCoords();
        normals = gnomeModel.getNormals();
        tangents = gnomeModel.getTangents();
        bitangents = gnomeModel.getBitangents();

        pvalues = new float[numObjVertices*3];
        tvalues = new float[numObjVertices*2];
        nvalues = new float[numObjVertices*3];
        tanvalues = new float[numObjVertices*3];
        bitanvalues = new float[numObjVertices*3];

        for (int i = 0; i < numObjVertices; i++) {
            pvalues[i*3] = (float) (vertices[i]).x();
            pvalues[i*3+1] = (float) (vertices[i]).y();
            pvalues[i*3+2] = (float) (vertices[i]).z();

            tvalues[i*2] = (float) (texCoords[i]).x();
            tvalues[i*2+1] = (float) (texCoords[i]).y();

            nvalues[i*3] = (float) (normals[i]).x();
            nvalues[i*3+1] = (float) (normals[i]).y();
            nvalues[i*3+2] = (float) (normals[i]).z();

            tanvalues[i*3] = (float) (tangents[i]).x();
            tanvalues[i*3+1] = (float) (tangents[i]).y();
            tanvalues[i*3+2] = (float) (tangents[i]).z();

            bitanvalues[i*3] = (float) (bitangents[i]).x();
            bitanvalues[i*3+1] = (float) (bitangents[i]).y();
            bitanvalues[i*3+2] = (float) (bitangents[i]).z();
        }

        // setup model vert tex & norms
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);
    }

    private void setupShadowBuffers() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();

        gl.glGenFramebuffers(1, shadowBuffer, 0);

        gl.glGenTextures(1, shadowTex, 0);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    private void installLights() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
		
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgramDefault, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgramDefault, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgramDefault, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgramDefault, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgramDefault, "light.position");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgramDefault, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgramDefault, ambLoc, 1, lightAmbient, 0);

        if (renderLight) {
            gl.glProgramUniform4fv(renderingProgramDefault, diffLoc, 1, lightDiffuse, 0);
            gl.glProgramUniform4fv(renderingProgramDefault, specLoc, 1, lightSpecular, 0);
        } else {
            float[] zeroLight = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
            gl.glProgramUniform4fv(renderingProgramDefault, diffLoc, 1, zeroLight, 0);
            gl.glProgramUniform4fv(renderingProgramDefault, specLoc, 1, zeroLight, 0);
        }

		gl.glProgramUniform3fv(renderingProgramDefault, posLoc, 1, lightPos, 0);
    }

    private void setMaterialPlaster() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        float[] matAmb = Utils.plasterAmbient();
        float[] matDif = Utils.plasterDiffuse();
        float[] matSpe = Utils.plasterSpecular();
        float matShi = Utils.plasterShininess();

        mambLoc = gl.glGetUniformLocation(renderingProgramDefault, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgramDefault, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgramDefault, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgramDefault, "material.shininess");

        gl.glProgramUniform4fv(renderingProgramDefault, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgramDefault, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgramDefault, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgramDefault, mshiLoc, matShi);
    }

    private void handleInput(float time) {
        // handle different user input to move the camera around the scene
        // rotation direction determined by pos/neg yaw and pitch
        yaw = 0; pitch = 0;
        if (inputManager.isKeyPressed(KeyEvent.VK_LEFT)) yaw += 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_RIGHT)) yaw -= 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_UP)) pitch += 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_DOWN)) pitch -= 1;
        cam.rotate(yaw, pitch, time);

        // movement direction determined by pos/neg forward, right, and up
        forward = 0; right = 0; up = 0;
        if (inputManager.isKeyPressed(KeyEvent.VK_W)) forward += 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_S)) forward -= 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_D)) right += 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_A)) right -= 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_Q)) up += 1;
        if (inputManager.isKeyPressed(KeyEvent.VK_E)) up -= 1;
        cam.move(forward, right, up, time);

        // allow user to toggle world axes
        // only toggles when state of the key changes, to prevent rapid toggling from holding down space
        spaceIsPressed = inputManager.isKeyPressed(KeyEvent.VK_SPACE);
        if (spaceIsPressed && !spaceWasPressed) renderAxes = !renderAxes;
        spaceWasPressed = spaceIsPressed;

        boolean lightTogglePressed = inputManager.isKeyPressed(KeyEvent.VK_P);
        if (lightTogglePressed && !lightToggleWasPressed) {
            renderLight = !renderLight;   
        }
        lightToggleWasPressed = lightTogglePressed;

        if (inputManager.isKeyPressed(KeyEvent.VK_I)) currentLightPos.z -= lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_K)) currentLightPos.z += lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_J)) currentLightPos.x -= lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_L)) currentLightPos.x += lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_U)) currentLightPos.y += lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_O)) currentLightPos.y -= lightSpeed * time;
    }

    public static void main(String[] args) { new Code(); }
    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
        // remake the perspective matrix when screen is resized, as asapect ratio may have changed
        aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
    
        setupShadowBuffers();
    }
    @Override
    public void dispose(GLAutoDrawable arg0) {}

}