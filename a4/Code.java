package a4;

import javax.swing.JFrame;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_CW;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT32;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_LINE_SMOOTH;
import static com.jogamp.opengl.GL.GL_POLYGON_OFFSET_FILL;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE2;
import static com.jogamp.opengl.GL.GL_TEXTURE3;
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
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
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
    private int renderingProgramBlinnPhong, renderingProgramCubeMap, renderingProgramShadow, renderingProgramNoTex, renderingProgramCelShading, renderingProgramOutline, renderingProgramGlass;
    private int vao[] = new int[1];
    private int vbo[] = new int[70];
    private Camera cam;
    private InputManager inputManager;

    // variables for imported models and textures
    private int numObjVertices;
    private ImportedModel spiderModel, gnomeModel, televisionModel, benchModel, cabinetModel, nightstandModel, rock1Model, rock2Model, tree1Model, tree2Model;
    private Cube glassCube;
    private int skyboxTexture, groundPlaneTexture, spiderTexture, gnomeTexture, televisionTexture, benchTexture, cabinetTexture, nightstandTexture, baseTexture, rock1Texture, rock2Texture, tree1Texture, tree2Texture;
    private int groundPlaneNormalMap, spiderNormalMap, gnomeNormalMap, televisionNormalMap, benchNormalMap, cabinetNormalMap, nightstandNormalMap, baseNormalMap, rock1NormalMap, rock2NormalMap, tree1NormalMap, tree2NormalMap;

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

    // light/cam inital positions
    private Vector3f initialLightPos = new Vector3f(0f, 2.8f, -0.5f);
    private Vector3f initialCameraPos = new Vector3f(0, 0.5f, 3);


    // model inital position info
    private Vector3f initialSpiderPos = new Vector3f(0, 0.85f, 0.5f);
    private Quaternionf initialSpiderRotation = new Quaternionf().rotationY((float)Math.toRadians(-135));
    private Vector3f initialSpiderScale = new Vector3f(1, 1, 1);

    private Vector3f initialGnomePos = new Vector3f(-1.5f, 0.9f, 0);
    private Quaternionf initialGnomeRotation = new Quaternionf().rotationY((float)Math.toRadians(90));
    private Vector3f initialGnomeScale = new Vector3f(1, 1, 1);

    private Vector3f initialTelevisionPos = new Vector3f(1.5f, 1.44f, 0f);
    private Quaternionf initialTelevisionRotation = new Quaternionf().rotationY((float)Math.toRadians(-90));
    private Vector3f initialTelevisionScale = new Vector3f(1, 1, 1);

    private Vector3f initialBenchPos = new Vector3f(-1.5f, 0.25f, 0);
    private Quaternionf initialBenchRotation = new Quaternionf().rotationY((float)Math.toRadians(90));
    private Vector3f initialBenchScale = new Vector3f(1, 1, 1);
    
    private Vector3f initialCabinetPos = new Vector3f(1.5f, 0.25f, 0);
    private Quaternionf initialCabinetRotation = new Quaternionf().rotationY((float)Math.toRadians(-90));
    private Vector3f initialCabinetScale = new Vector3f(1, 1, 1);

    private Vector3f initialNightstandPos = new Vector3f(-1.5f, 0.25f, 0.9f);
    private Quaternionf initialNightstandRotation = new Quaternionf().rotationY((float)Math.toRadians(90));
    private Vector3f initialNightstandScale = new Vector3f(1, 1, 1);

    private Vector3f initialRock1Pos = new Vector3f(0.55f, 0.225f, -1.275f);
    private Quaternionf initialRock1Rotation = new Quaternionf().identity();
    private Vector3f initialRock1Scale = new Vector3f(1, 1, 1);

    private Vector3f initialRock2Pos = new Vector3f(0, 0.225f, 0.5f);
    private Quaternionf initialRock2Rotation = new Quaternionf().rotationY((float)Math.toRadians(45));
    private Vector3f initialRock2Scale = new Vector3f(1.5f, 1, 1.5f);

    private Vector3f initialTree1Pos = new Vector3f(-1f, 0f, -1.175f);
    private Quaternionf initialTree1Rotation = new Quaternionf().rotationY((float)Math.toRadians(180));
    private Vector3f initialTree1Scale = new Vector3f(0.9f, 1, 0.9f);

    private Vector3f initialTree2Pos = new Vector3f(1.25f, 0.225f, 1.25f);
    private Quaternionf initialTree2Rotation = new Quaternionf().identity();
    private Vector3f initialTree2Scale = new Vector3f(1.5f, 1.5f, 1.5f);

    private Vector3f initialGlassCubePos = new Vector3f(0, 1.5f, 0);
    private Quaternionf initialGlassCubeRotation = new Quaternionf().identity();
    private Vector3f initialGlassCubeScale = new Vector3f(1.9f, 1.5f, 1.9f);

    private Vector3f initialBasePos = new Vector3f(0, 0, 0);
    private Quaternionf initialBaseRotation = new Quaternionf().identity();
    private Vector3f initialBaseScale = new Vector3f(2, 0.25f, 2);

    private Vector3f initialGroundPos = new Vector3f(0, 0.251f, 0);
    private Quaternionf initialGroundRotation = new Quaternionf().identity();
    private Vector3f initialGroundScale = new Vector3f(1.9f, 1.9f, 1.9f);

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

    // swap between blinn-phong and cel shading
    private boolean celShading = true;
    private boolean renderToggleWasPressed = false;

    // cel shading options
    private int numShades = 3;

    // fog parameters
    private boolean fogEnabled = true;
    private float[] fogColor = new float[] { 0.5f, 0.5f, 0.6f }; // Bluish gray
    private float fogStart = 1f;
    private float fogEnd = 10f;
    private int fogEnabledLoc, fogColorLoc, fogStartLoc, fogEndLoc;
    private boolean fogToggleWasPressed = false;


    public Code() {
        // setup window
        setTitle("CSC155 - Assignment 4");
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

        lightVmat.identity().setLookAt(currentLightPos, origin, upVec);
        lightPmat.identity().setPerspective((float) Math.toRadians(90), aspect, 0.1f, 1000.0f);

        gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
        gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);

        gl.glDrawBuffer(GL_NONE);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(2.0f, 4.0f);

        renderShadows();

        gl.glDisable(GL_POLYGON_OFFSET_FILL);

        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

        gl.glDrawBuffer(GL_FRONT);

        if (celShading) {
            renderOutlines();
            renderCelShaded();
        } else {
            renderBlinnPhong();
        }

        drawLight();

        renderTransparent();
    }

    private void renderShadows() {
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
        mMat.translate(initialGroundPos);
        mMat.rotate(initialGroundRotation);
        mMat.scale(initialGroundScale);

        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_TRIANGLES, 0, 18);
        
        // draw gnome
        mMat.identity();
        mMat.translate(initialGnomePos);
        mMat.rotate(initialGnomeRotation);
        mMat.scale(initialGnomeScale);

        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glDrawArrays(GL_TRIANGLES, 0, gnomeModel.getNumVertices());

        // draw television
        mMat.identity();
        mMat.translate(initialTelevisionPos);
        mMat.rotate(initialTelevisionRotation);
        mMat.scale(initialTelevisionScale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, televisionModel.getNumVertices());

        // Draw bench
        mMat.identity();
        mMat.translate(initialBenchPos);
        mMat.rotate(initialBenchRotation);
        mMat.scale(initialBenchScale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, benchModel.getNumVertices());

        // Draw cabinet
        mMat.identity();
        mMat.translate(initialCabinetPos);
        mMat.rotate(initialCabinetRotation);
        mMat.scale(initialCabinetScale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[29]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, cabinetModel.getNumVertices());

        // Draw nightstand
        mMat.identity();
        mMat.translate(initialNightstandPos);
        mMat.rotate(initialNightstandRotation);
        mMat.scale(initialNightstandScale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[34]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, nightstandModel.getNumVertices());

        // Draw rock1
        mMat.identity();
        mMat.translate(initialRock1Pos);
        mMat.rotate(initialRock1Rotation);
        mMat.scale(initialRock1Scale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[42]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, rock1Model.getNumVertices());

        // Draw rock2
        mMat.identity();
        mMat.translate(initialRock2Pos);
        mMat.rotate(initialRock2Rotation);
        mMat.scale(initialRock2Scale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[47]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, rock2Model.getNumVertices());

        // Draw tree1
        mMat.identity();
        mMat.translate(initialTree1Pos);
        mMat.rotate(initialTree1Rotation);
        mMat.scale(initialTree1Scale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[52]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, tree1Model.getNumVertices());

        // Draw tree2
        mMat.identity();
        mMat.translate(initialTree2Pos);
        mMat.rotate(initialTree2Rotation);
        mMat.scale(initialTree2Scale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[57]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, tree2Model.getNumVertices());

        // Draw spider
        mMat.identity();
        mMat.translate(initialSpiderPos);
        mMat.rotate(initialSpiderRotation);
        mMat.scale(initialSpiderScale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);

        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, spiderModel.getNumVertices());

        // draw glass cube
        mMat.identity();
        mMat.translate(initialGlassCubePos);
        mMat.scale(initialGlassCubeScale);
        
        shadowMVP1.identity();
        shadowMVP1.mul(lightPmat);
        shadowMVP1.mul(lightVmat);
        shadowMVP1.mul(mMat);
        
        gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[39]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, 36);
        
    }

    private void renderBlinnPhong() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(renderingProgramBlinnPhong);

        mLoc = gl.glGetUniformLocation(renderingProgramBlinnPhong, "m_matrix");
        vLoc = gl.glGetUniformLocation(renderingProgramBlinnPhong, "v_matrix");
        pLoc = gl.glGetUniformLocation(renderingProgramBlinnPhong, "p_matrix");
        nLoc = gl.glGetUniformLocation(renderingProgramBlinnPhong, "norm_matrix");
        sLoc = gl.glGetUniformLocation(renderingProgramBlinnPhong, "shadowMVP");
        tfLoc = gl.glGetUniformLocation(renderingProgramBlinnPhong, "tileCount");
        
        installLights(renderingProgramBlinnPhong);
        setMaterialPlaster(renderingProgramBlinnPhong);
        setupFogUniforms(renderingProgramBlinnPhong);

        gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

        drawGround();
        drawGnome();
        drawTelevision();
        drawBench();
        drawCabinet();
        drawNightstand();
        drawRock1();
        drawRock2();
        drawTree1();
        drawTree2();
        drawSpider();

        gl.glProgramUniform1i(renderingProgramBlinnPhong, fogEnabledLoc, 0);
        drawBase();
    }

    private void renderOutlines() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(renderingProgramOutline);
        
        // Get uniform locations
        int mLoc = gl.glGetUniformLocation(renderingProgramOutline, "m_matrix");
        int vLoc = gl.glGetUniformLocation(renderingProgramOutline, "v_matrix");
        int pLoc = gl.glGetUniformLocation(renderingProgramOutline, "p_matrix");
        int scaleLoc = gl.glGetUniformLocation(renderingProgramOutline, "outlineScale");
        int colorLoc = gl.glGetUniformLocation(renderingProgramOutline, "outlineColor");
        
        // Set uniform values
        gl.glUniform1f(scaleLoc, 1.02f); // Scale the model by 2%
        gl.glUniform3f(colorLoc, 0.0f, 0.0f, 0.0f); // Black outline
        
        // Setup rendering state
        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_FRONT); // Cull front faces - important for outlines!
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);
        
        
        // Draw gnome outline
        mMat.identity();
        mMat.translate(initialGnomePos);
        mMat.rotate(initialGnomeRotation);
        mMat.scale(initialGnomeScale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, gnomeModel.getNumVertices());

        // Draw television outline
        mMat.identity();
        mMat.translate(initialTelevisionPos);
        mMat.rotate(initialTelevisionRotation);
        mMat.scale(initialTelevisionScale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, televisionModel.getNumVertices());

        // Draw bench outline
        mMat.identity();
        mMat.translate(initialBenchPos);
        mMat.rotate(initialBenchRotation);
        mMat.scale(initialBenchScale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, benchModel.getNumVertices());

        // Draw cabinet outline
        mMat.identity();
        mMat.translate(initialCabinetPos);
        mMat.rotate(initialCabinetRotation);
        mMat.scale(initialCabinetScale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[29]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, cabinetModel.getNumVertices());

        // Draw nightstand outline
        mMat.identity();
        mMat.translate(initialNightstandPos);
        mMat.rotate(initialNightstandRotation);
        mMat.scale(initialNightstandScale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[34]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, nightstandModel.getNumVertices());

        // Draw rock1 outline
        mMat.identity();
        mMat.translate(initialRock1Pos);
        mMat.rotate(initialRock1Rotation);
        mMat.scale(initialRock1Scale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[42]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, rock1Model.getNumVertices());

        // Draw rock2 outline
        mMat.identity();
        mMat.translate(initialRock2Pos);
        mMat.rotate(initialRock2Rotation);
        mMat.scale(initialRock2Scale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[47]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, rock2Model.getNumVertices());

        // Draw spider outline
        mMat.identity();
        mMat.translate(initialSpiderPos);
        mMat.rotate(initialSpiderRotation);
        mMat.scale(initialSpiderScale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, spiderModel.getNumVertices());

        // Draw tree1 outline
        mMat.identity();
        mMat.translate(initialTree1Pos);
        mMat.rotate(initialTree1Rotation);
        mMat.scale(initialTree1Scale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniform1f(scaleLoc, 1.005f);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[52]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, tree1Model.getNumVertices());

        // Draw tree2 outline
        mMat.identity();
        mMat.translate(initialTree2Pos);
        mMat.rotate(initialTree2Rotation);
        mMat.scale(initialTree2Scale);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[57]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, tree2Model.getNumVertices());
        
        // Reset to normal culling
        gl.glCullFace(GL_BACK);
    }

    private void renderCelShaded() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(renderingProgramCelShading);
    
        // Set up uniforms
        mLoc = gl.glGetUniformLocation(renderingProgramCelShading, "m_matrix");
        vLoc = gl.glGetUniformLocation(renderingProgramCelShading, "v_matrix");
        pLoc = gl.glGetUniformLocation(renderingProgramCelShading, "p_matrix");
        nLoc = gl.glGetUniformLocation(renderingProgramCelShading, "norm_matrix");
        sLoc = gl.glGetUniformLocation(renderingProgramCelShading, "shadowMVP");
        tfLoc = gl.glGetUniformLocation(renderingProgramCelShading, "tileCount");
        
        // Add cel shader specific uniform locations
        int numShadesLoc = gl.glGetUniformLocation(renderingProgramCelShading, "numShades");
        int specCutoffLoc = gl.glGetUniformLocation(renderingProgramCelShading, "specularCutoff");
        
        // Set cel shader values
        gl.glUniform1i(numShadesLoc, numShades); // 3 shades of lighting
        gl.glUniform1f(specCutoffLoc, 0.7f); // Specular highlight threshold
        
        installLights(renderingProgramCelShading);
        setMaterialPlaster(renderingProgramCelShading);
        setupFogUniforms(renderingProgramCelShading);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
    
        drawGround();
        drawGnome();
        drawTelevision();
        drawBench();
        drawCabinet();
        drawNightstand();
        drawRock1();
        drawRock2();
        drawTree1();
        drawTree2();
        drawSpider();

        // disable fog for base
        gl.glProgramUniform1i(renderingProgramCelShading, fogEnabledLoc, 0);
        drawBase();
    }

    private void renderTransparent() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
    
        // Enable blending for transparency
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        gl.glDepthMask(false);

        gl.glUseProgram(renderingProgramGlass);

        mLoc = gl.glGetUniformLocation(renderingProgramGlass, "m_matrix");
        vLoc = gl.glGetUniformLocation(renderingProgramGlass, "v_matrix");
        pLoc = gl.glGetUniformLocation(renderingProgramGlass, "p_matrix");
        nLoc = gl.glGetUniformLocation(renderingProgramGlass, "norm_matrix");
        sLoc = gl.glGetUniformLocation(renderingProgramGlass, "shadowMVP");

        // Set up the reflection/refraction uniforms
        int refractionIndexLoc = gl.glGetUniformLocation(renderingProgramGlass, "refractionIndex");
        int fresnelBiasLoc = gl.glGetUniformLocation(renderingProgramGlass, "fresnelBias");
        int fresnelScaleLoc = gl.glGetUniformLocation(renderingProgramGlass, "fresnelScale");
        int fresnelPowerLoc = gl.glGetUniformLocation(renderingProgramGlass, "fresnelPower");
        
        gl.glUniform1f(refractionIndexLoc, 1.52f);  // Glass refraction index
        gl.glUniform1f(fresnelBiasLoc, 0.1f);       // Fresnel bias
        gl.glUniform1f(fresnelScaleLoc, 1.0f);      // Fresnel scale
        gl.glUniform1f(fresnelPowerLoc, 2.0f);      // Fresnel power
        
        // Add lighting/material
        installLights(renderingProgramGlass);
        setMaterialGlass(renderingProgramGlass);
        
        // Draw the glass cube
        drawGlassCube();
        
        // Restore state
        gl.glDepthMask(true);
        gl.glDisable(GL_BLEND);
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

    private void drawGround() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        mMat.identity();
        mMat.translate(initialGroundPos);
        mMat.rotate(initialGroundRotation);
        mMat.scale(initialGroundScale);

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

        gl.glDisable(GL_CULL_FACE);
		gl.glDrawArrays(GL_TRIANGLES, 0, 18);
        gl.glEnable(GL_CULL_FACE);
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
        
        mMat.identity();
        mMat.translate(currentLightPos);
        mMat.scale(0.3f);
        
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glUniform3f(acLoc, 1.0f, 1.0f, 0.0f);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glDrawArrays(GL_TRIANGLES, 0, spiderModel.getNumVertices());
    }

    private void drawGnome() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialGnomePos);
        mMat.rotate(initialGnomeRotation);
        mMat.scale(initialGnomeScale);

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

    private void drawTelevision() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialTelevisionPos);
        mMat.rotate(initialTelevisionRotation);
        mMat.scale(initialTelevisionScale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, televisionTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, televisionNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, televisionModel.getNumVertices());
    }

    private void drawBench() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialBenchPos);
        mMat.rotate(initialBenchRotation);
        mMat.scale(initialBenchScale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[25]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[26]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[27]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[28]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, benchTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, benchNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, benchModel.getNumVertices());
    }

    private void drawCabinet() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialCabinetPos);
        mMat.rotate(initialCabinetRotation);
        mMat.scale(initialCabinetScale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[29]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[30]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[31]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[32]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[33]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, cabinetTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, cabinetNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, cabinetModel.getNumVertices());
    }

    private void drawNightstand() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialNightstandPos);
        mMat.rotate(initialNightstandRotation);
        mMat.scale(initialNightstandScale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[34]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[35]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[36]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[37]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[38]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, nightstandTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, nightstandNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, nightstandModel.getNumVertices());
    }

    private void drawGlassCube() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        mMat.identity();
        mMat.translate(initialGlassCubePos);
        mMat.rotate(initialGlassCubeRotation);
        mMat.scale(initialGlassCubeScale);
        
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
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[39]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[40]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[41]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
        
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, groundPlaneTexture);
        
        gl.glActiveTexture(GL_TEXTURE3);
        gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);
        
        gl.glDisable(GL_CULL_FACE);
        gl.glDrawArrays(GL_TRIANGLES, 0, 36);
        gl.glEnable(GL_CULL_FACE);

    }

    private void drawBase() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialBasePos);
        mMat.rotate(initialBaseRotation);
        mMat.scale(initialBaseScale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[39]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[40]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[41]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, baseTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, baseNormalMap);

        gl.glDisable(GL_CULL_FACE);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
        gl.glEnable(GL_CULL_FACE);
    }

    private void drawRock1() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialRock1Pos);
        mMat.rotate(initialRock1Rotation);
        mMat.scale(initialRock1Scale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[42]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[43]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[44]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[45]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[46]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, rock1Texture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, rock1NormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, rock1Model.getNumVertices());
    }

    private void drawRock2() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialRock2Pos);
        mMat.rotate(initialRock2Rotation);
        mMat.scale(initialRock2Scale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[47]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[48]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[49]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[50]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[51]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, rock2Texture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, rock2NormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, rock2Model.getNumVertices());
    }

    private void drawTree1() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialTree1Pos);
        mMat.rotate(initialTree1Rotation);
        mMat.scale(initialTree1Scale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[52]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[53]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[54]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[55]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[56]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, tree1Texture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, tree1NormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, tree1Model.getNumVertices());
    }

    private void drawTree2() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialTree2Pos);
        mMat.rotate(initialTree2Rotation);
        mMat.scale(initialTree2Scale);

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


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[57]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[58]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[59]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[60]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[61]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, tree2Texture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, tree2NormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, tree2Model.getNumVertices());
    }

    private void drawSpider() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        mMat.identity();
        mMat.translate(initialSpiderPos);
        mMat.rotate(initialSpiderRotation);
        mMat.scale(initialSpiderScale);

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
		gl.glBindTexture(GL_TEXTURE_2D, spiderTexture);

        gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, spiderNormalMap);

		gl.glDrawArrays(GL_TRIANGLES, 0, spiderModel.getNumVertices());
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // import models and create rendering program by compiling and linking shaders
        spiderModel = new ImportedModel("assets/models/Tarantula.obj");
        gnomeModel = new ImportedModel("assets/models/garden_gnome_1k.obj");
        televisionModel = new ImportedModel("assets/models/Television_01_1k.obj");
        benchModel = new ImportedModel("assets/models/painted_wooden_bench_1k.obj");
        cabinetModel = new ImportedModel("assets/models/painted_wooden_cabinet_1k.obj");
        nightstandModel = new ImportedModel("assets/models/painted_wooden_nightstand_1k.obj");
        rock1Model = new ImportedModel("assets/models/namaqualand_boulder_02_1k.obj");
        rock2Model = new ImportedModel("assets/models/namaqualand_boulder_05_1k.obj");
        tree1Model = new ImportedModel("assets/models/didelta_spinosa_1k.obj");
        tree2Model = new ImportedModel("assets/models/quiver_tree_02_1k.obj");
        

        renderingProgramBlinnPhong = Utils.createShaderProgram("assets/shaders/blinnphong.vert", "assets/shaders/blinnphong.frag");
        renderingProgramCubeMap = Utils.createShaderProgram("assets/shaders/cubemap.vert", "assets/shaders/cubemap.frag");
        renderingProgramShadow = Utils.createShaderProgram("assets/shaders/shadowmap.vert", "assets/shaders/shadowmap.frag");
        renderingProgramNoTex = Utils.createShaderProgram("assets/shaders/notex.vert", "assets/shaders/notex.frag");
        renderingProgramCelShading = Utils.createShaderProgram("assets/shaders/celshading.vert", "assets/shaders/celshading.frag");
        renderingProgramOutline = Utils.createShaderProgram("assets/shaders/outlines.vert", "assets/shaders/outlines.frag");
        renderingProgramGlass = Utils.createShaderProgram("assets/shaders/glass.vert", "assets/shaders/glass.frag");


        // set perspective matrix, only changes when screen is resized
        aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

        // Initialize light pos
        currentLightPos.set(initialLightPos);

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
        cam.lookAt(0, 1.5f, 0);
        
        // load all textures that will be used
        // ground/skybox
        groundPlaneTexture = Utils.loadTexture("assets/textures/gravelly_sand_diff_4k.jpg");
        groundPlaneNormalMap = Utils.loadTexture("assets/textures/gravelly_sand_nor_gl_4k.jpg");

        skyboxTexture = Utils.loadCubeMap("assets/textures/cubemaps/night-sky");
        gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

        // models
        spiderTexture =  Utils.loadTexture("assets/textures/Tarantula.png");
        spiderNormalMap = Utils.loadTexture("assets/textures/rosewood_veneer1_nor_gl_4k.jpg");

        gnomeTexture = Utils.loadTexture("assets/textures/garden_gnome_diff_1k.jpg");
        gnomeNormalMap = Utils.loadTexture("assets/textures/garden_gnome_nor_gl_1k.jpg");

        televisionTexture = Utils.loadTexture("assets/textures/Television_01_diff_1k.jpg");
        televisionNormalMap = Utils.loadTexture("assets/textures/Television_01_nor_gl_1k.jpg");

        benchTexture = Utils.loadTexture("assets/textures/painted_wooden_bench_diff_1k.jpg");
        benchNormalMap = Utils.loadTexture("assets/textures/painted_wooden_bench_nor_gl_1k.jpg");

        cabinetTexture = Utils.loadTexture("assets/textures/painted_wooden_cabinet_diff_1k.jpg");
        cabinetNormalMap = Utils.loadTexture("assets/textures/painted_wooden_cabinet_nor_gl_1k.jpg");

        nightstandTexture = Utils.loadTexture("assets/textures/painted_wooden_nightstand_diff_1k.jpg");
        nightstandNormalMap = Utils.loadTexture("assets/textures/painted_wooden_nightstand_nor_gl_1k.jpg");

        baseTexture = Utils.loadTexture("assets/textures/rosewood_veneer1_diff_4k.jpg");
        baseNormalMap = Utils.loadTexture("assets/textures/rosewood_veneer1_nor_gl_4k.jpg");
        
        rock1Texture = Utils.loadTexture("assets/textures/namaqualand_boulder_02_diff_1k.jpg");
        rock1NormalMap = Utils.loadTexture("assets/textures/namaqualand_boulder_02_nor_gl_1k.jpg");

        rock2Texture = Utils.loadTexture("assets/textures/namaqualand_boulder_05_diff_1k.jpg");
        rock2NormalMap = Utils.loadTexture("assets/textures/namaqualand_boulder_05_nor_gl_1k.jpg");

        tree1Texture = Utils.loadTexture("assets/textures/didelta_spinosa_diff_1k.jpg");
        tree1NormalMap = Utils.loadTexture("assets/textures/didelta_spinosa_nor_gl_1k.jpg");

        tree2Texture = Utils.loadTexture("assets/textures/quiver_tree_02_diff_1k.jpg");
        tree2NormalMap = Utils.loadTexture("assets/textures/quiver_tree_02_nor_gl_1k.jpg");

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

        // load in spider model info
        numObjVertices = spiderModel.getNumVertices();
        vertices = spiderModel.getVertices();
        texCoords = spiderModel.getTexCoords();
        normals = spiderModel.getNormals();
        tangents = spiderModel.getTangents();
        bitangents = spiderModel.getBitangents();

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

        // load in television model info
        numObjVertices = televisionModel.getNumVertices();
        vertices = televisionModel.getVertices();
        texCoords = televisionModel.getTexCoords();
        normals = televisionModel.getNormals();
        tangents = televisionModel.getTangents();
        bitangents = televisionModel.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

         // load in bench model info
        numObjVertices = benchModel.getNumVertices();
        vertices = benchModel.getVertices();
        texCoords = benchModel.getTexCoords();
        normals = benchModel.getNormals();
        tangents = benchModel.getTangents();
        bitangents = benchModel.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[25]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[26]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[27]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[28]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // load in cabinet model info
        numObjVertices = cabinetModel.getNumVertices();
        vertices = cabinetModel.getVertices();
        texCoords = cabinetModel.getTexCoords();
        normals = cabinetModel.getNormals();
        tangents = cabinetModel.getTangents();
        bitangents = cabinetModel.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[29]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[30]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[31]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[32]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[33]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // load in nightstand model info
        numObjVertices = nightstandModel.getNumVertices();
        vertices = nightstandModel.getVertices();
        texCoords = nightstandModel.getTexCoords();
        normals = nightstandModel.getNormals();
        tangents = nightstandModel.getTangents();
        bitangents = nightstandModel.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[34]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[35]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[36]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[37]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[38]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // setup glass cube
        glassCube = new Cube();

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[39]);
        vertBuf = Buffers.newDirectFloatBuffer(glassCube.getVertices());
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[40]);
        texBuf = Buffers.newDirectFloatBuffer(glassCube.getTexCoords());
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[41]);
        normBuf = Buffers.newDirectFloatBuffer(glassCube.getNormals());
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        // load in rock1 model info
        numObjVertices = rock1Model.getNumVertices();
        vertices = rock1Model.getVertices();
        texCoords = rock1Model.getTexCoords();
        normals = rock1Model.getNormals();
        tangents = rock1Model.getTangents();
        bitangents = rock1Model.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[42]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[43]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[44]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[45]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[46]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // load in rock2 model info
        numObjVertices = rock2Model.getNumVertices();
        vertices = rock2Model.getVertices();
        texCoords = rock2Model.getTexCoords();
        normals = rock2Model.getNormals();
        tangents = rock2Model.getTangents();
        bitangents = rock2Model.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[47]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[48]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[49]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[50]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[51]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

         // load in tree1 model info
        numObjVertices = tree1Model.getNumVertices();
        vertices = tree1Model.getVertices();
        texCoords = tree1Model.getTexCoords();
        normals = tree1Model.getNormals();
        tangents = tree1Model.getTangents();
        bitangents = tree1Model.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[52]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[53]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[54]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[55]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[56]);
        bitanBuf = Buffers.newDirectFloatBuffer(bitanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, bitanBuf.limit() * 4, bitanBuf, GL_STATIC_DRAW);

        // load in tree2 model info
        numObjVertices = tree2Model.getNumVertices();
        vertices = tree2Model.getVertices();
        texCoords = tree2Model.getTexCoords();
        normals = tree2Model.getNormals();
        tangents = tree2Model.getTangents();
        bitangents = tree2Model.getBitangents();

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
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[57]);
        vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[58]);
        texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[59]);
        normBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, normBuf.limit() * 4, normBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[60]);
        tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[61]);
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

    private void setupFogUniforms(int renderingProgram) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
    
        fogEnabledLoc = gl.glGetUniformLocation(renderingProgram, "fogEnabled");
        fogColorLoc = gl.glGetUniformLocation(renderingProgram, "fogColor");
        fogStartLoc = gl.glGetUniformLocation(renderingProgram, "fogStart");
        fogEndLoc = gl.glGetUniformLocation(renderingProgram, "fogEnd");
        
        gl.glProgramUniform1i(renderingProgram, fogEnabledLoc, fogEnabled ? 1 : 0);
        gl.glProgramUniform3fv(renderingProgram, fogColorLoc, 1, fogColor, 0);
        gl.glProgramUniform1f(renderingProgram, fogStartLoc, fogStart);
        gl.glProgramUniform1f(renderingProgram, fogEndLoc, fogEnd);
    }

    private void installLights(int renderingProgram) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
		
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);

        if (renderLight) {
            gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
            gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
        } else {
            float[] zeroLight = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
            gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, zeroLight, 0);
            gl.glProgramUniform4fv(renderingProgram, specLoc, 1, zeroLight, 0);
        }

		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
    }

    private void setMaterialPlaster(int renderingProgram) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        float[] matAmb = Utils.plasterAmbient();
        float[] matDif = Utils.plasterDiffuse();
        float[] matSpe = Utils.plasterSpecular();
        float matShi = Utils.plasterShininess();

        mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
    }

    private void setMaterialGlass(int renderingProgram) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        
        float[] matAmb = new float[] { 0.1f, 0.1f, 0.2f, 1.0f };   // Slightly blue tint
        float[] matDif = new float[] { 0.2f, 0.2f, 0.3f, 1.0f };   // Low diffuse for glass
        float[] matSpe = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };   // High specular
        float matShi = 150.0f;                                     // High shininess
        float matAlpha = 0.4f;                                     // Transparency level
        
        // Get uniform locations
        mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
        mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
        mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
        mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
        int malphaLoc = gl.glGetUniformLocation(renderingProgram, "material.alpha");
        
        // Set uniform values
        gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
        gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
        gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
        gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
        gl.glProgramUniform1f(renderingProgram, malphaLoc, matAlpha);
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

        boolean renderTogglePressed = inputManager.isKeyPressed(KeyEvent.VK_B);
        if (renderTogglePressed && !renderToggleWasPressed) {
            celShading = !celShading;   
        }
        renderToggleWasPressed = renderTogglePressed;

        if (inputManager.isKeyPressed(KeyEvent.VK_I)) currentLightPos.z -= lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_K)) currentLightPos.z += lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_J)) currentLightPos.x -= lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_L)) currentLightPos.x += lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_U)) currentLightPos.y += lightSpeed * time;
        if (inputManager.isKeyPressed(KeyEvent.VK_O)) currentLightPos.y -= lightSpeed * time;

        boolean fogTogglePressed = inputManager.isKeyPressed(KeyEvent.VK_F);
        if (fogTogglePressed && !fogToggleWasPressed) {
            fogEnabled = !fogEnabled;   
        }
        fogToggleWasPressed = fogTogglePressed;
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