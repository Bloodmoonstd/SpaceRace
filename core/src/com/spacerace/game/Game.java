package com.spacerace.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Game extends ApplicationAdapter implements GestureDetector.GestureListener{

    Texture textureSolid;
    PolygonSprite polySprite;
    PolygonSpriteBatch polyBatch;

	World world;
    OrthographicCamera camera;

    //Ground variables
    List<Float> verts;
    Boolean climbing;

    TextureRegion textureRegion;
    int start;
    int holder;
    float[] test = new float[512];

    Texture carImage;

    //test terrain poly
    float[][] polyVerts = new float[50][8];
    Body[] groundBody = new Body[50];
    BodyDef[] groundBodyDef = new BodyDef[50];
    Fixture[] groundFixture = new Fixture[50];
    FixtureDef[] groundFixtureDef = new FixtureDef[50];
    PolygonShape[] groundPoly = new PolygonShape[50];

    //car variables
    Body cart, wheel1Body, wheel2Body, axle1Body, axle2Body;
    PrismaticJoint spring1PrisJoint, spring2PrisJoint;
    RevoluteJoint motor1RevJoint, motor2RevJoint;

    //Debug renderer
    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;

	@Override
	public void create() {
        super.create();

        start = 0;
        holder = 0;

        camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0);

        Gdx.input.setInputProcessor(new GestureDetector(this));

        debugRenderer = new Box2DDebugRenderer();

        polyBatch = new PolygonSpriteBatch(); // To assign at the beginning

        polyBatch.setProjectionMatrix(camera.combined);

        // Creating the color filling (but textures would work the same way)
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0xFF33691E); // DE is red, AD is green and BE is blue.
        pix.fill();
        textureSolid = new Texture(pix);
        textureRegion = new TextureRegion(textureSolid);

		// Create a physics world, the heart of the simulation.  The Vecto passed in is gravity
		world = new World(new Vector2(0, -9.86f), true);

        verts = new ArrayList<Float>();
        float increment = 128.0f;

        climbing = true;

        createLevel(increment);

        test();

        createVehicle();

        //ground
        setGroundBody();


        debugMatrix = new Matrix4(camera.combined);


	}
    public static int randInt(int min, int max) {

        Random rand;
        rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

	@Override
	public void render()
    {
        if(camera.position.x  > start + 4096 + Gdx.graphics.getWidth()/2f)
        {
            start += 4096;
            test();
        }

        camera.update();

		// Advance the world, by the amount of time that has elapsed since thelast frame
		// Generally in a real game, dont do this in the render loop, as you aretying the physics
		// update rate to the frame rate, and vice versa
		world.step(Gdx.graphics.getDeltaTime(), 6, 2);


        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //carSprite.setPosition(carBody.getPosition().x, carBody.getPosition().y);
       // testSprite.setPosition(groundBody[0].getPosition().x, groundBody[0].getPosition().y);


       // spriteBatch.setProjectionMatrix(camera.combined);
        //spriteBatch.begin();
        //spriteBatch.draw(carSprite, carSprite.getX(), carSprite.getY());
        debugRenderer.render(world, debugMatrix);
        //spriteBatch.end();

        //polyBatch.setProjectionMatrix(camera.combined);
        //polyBatch.begin();
        //polySprite.draw(polyBatch);
        //polyBatch.end();

	}

    public void test()
    {
        Gdx.app.log("start x ", ": " + start);
        Gdx.app.log("camera x ", ": " + camera.position.x);
        for(int i = 0; i < 512; i++)
        {
            test[i] = verts.get(i+holder);
        }
        holder+=256;

        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        ShortArray triangleIndices = triangulator.computeTriangles(test);

        PolygonRegion polyReg = new PolygonRegion(textureRegion, test, triangleIndices.toArray());

        polySprite = new PolygonSprite(polyReg);

        int i = 0;
        for(int j = 0; j < 50; j++)
        {

                polyVerts[j][0] = test[i];
                polyVerts[j][1] = test[i + 1];

                polyVerts[j][2] = test[i + 6];
                polyVerts[j][3] = test[i + 7];

                polyVerts[j][4] = test[i + 4];
                polyVerts[j][5] = test[i + 5];

                polyVerts[j][6] = test[i + 2];
                polyVerts[j][7] = test[i + 3];

            i+=8;

        }
    }

    public void createLevel(float _increment)
    {
        for(int i = 0; i < 16384; i+=8)
        {
            verts.add(i * _increment / 8.0f); // i
            verts.add(0.0f); //i + 1

            verts.add(verts.get(i));
            if(verts.size() < 8)
                verts.add(200.0f);
            else
                verts.add(verts.get(i - 3));

            float randY = (float)randInt(0, 50);
            verts.add(verts.get(i) + _increment);
            if(climbing)
                verts.add(verts.get(i + 3) + randY);
            if(!climbing)
                verts.add(verts.get(i + 3) - randY);

            verts.add(verts.get(i) + _increment);
            verts.add(0.0f);

            if(verts.get(i + 3) > 700 )
                climbing = false;
            if(verts.get(i + 3) < 200)
                climbing = true;
        }
    }

    public void setGroundBody()
    {
        for(int i = 0; i < 50; i++)
        {
            groundBodyDef[i] = new BodyDef();
            groundBodyDef[i].type = BodyDef.BodyType.StaticBody;
            groundBodyDef[i].position.set(0, 0);

            groundBody[i] = world.createBody(groundBodyDef[i]);

            groundPoly[i] = new PolygonShape();
            groundPoly[i].set(polyVerts[i]);

            groundFixtureDef[i] = new FixtureDef();
            groundFixtureDef[i].shape = groundPoly[i];

            groundFixture[i] = groundBody[i].createFixture(groundFixtureDef[i]);
        }

    }

    public void createVehicle()
    {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight()/2);

        cart = world.createBody(bodyDef);

        PolygonShape polyBox = new PolygonShape();
        polyBox.setAsBox(150f, 30f);

        FixtureDef boxDef = new FixtureDef();
        boxDef.shape = polyBox;
        boxDef.density = 2;
        boxDef.friction = 0.5f;
        boxDef.restitution = 0.2f;
        boxDef.filter.groupIndex = -1;
        cart.createFixture(boxDef);

        polyBox.setAsBox(40f, 15f, new Vector2(-100, -30f), (float)Math.PI/3);
        cart.createFixture(boxDef);

        polyBox.setAsBox(40f, 15f, new Vector2(100, -30f), (float)-Math.PI/3);
        cart.createFixture(boxDef);

        boxDef.density = 1;

        //axle
        PrismaticJointDef prismaticJointDef;

        axle1Body = world.createBody(bodyDef);
        polyBox.setAsBox(40f, 10f, new Vector2(-100f - (float)(60*Math.cos(Math.PI/3)), -30f - (float)(60*Math.sin(Math.PI/3))), (float)Math.PI/3);
        axle1Body.createFixture(boxDef);

        prismaticJointDef = new PrismaticJointDef();
        prismaticJointDef.initialize(cart, axle1Body, axle1Body.getWorldCenter(), new Vector2((float)Math.cos(Math.PI/3), (float)Math.sin(Math.PI/3)));
        prismaticJointDef.lowerTranslation = -0.3f;
        prismaticJointDef.upperTranslation = 0.5f;
        prismaticJointDef.enableLimit = true;
        prismaticJointDef.enableMotor = true;

        spring1PrisJoint = (PrismaticJoint)world.createJoint(prismaticJointDef);

        axle2Body = world.createBody(bodyDef);
        polyBox.setAsBox(40f, 10f, new Vector2(100f + (float)(60*Math.cos(-Math.PI/3)), -30f + (float)(60*Math.sin(-Math.PI/3))), (float)-Math.PI/3);
        axle2Body.createFixture(boxDef);

        prismaticJointDef.initialize(cart, axle2Body, axle2Body.getWorldCenter(), new Vector2((float)-Math.cos(Math.PI/3), (float)Math.sin(Math.PI/3)));

        spring2PrisJoint = (PrismaticJoint)world.createJoint(prismaticJointDef);


        //wheels
        CircleShape circle = new CircleShape();
        circle.setPosition(new Vector2(0,0));

        circle.setRadius(50f);
        float test = axle1Body.getWorldCenter().x;
        Gdx.app.log("circle pos", " " + test);

        FixtureDef circleDef = new FixtureDef();
        circleDef.shape = circle;
        circleDef.density = 0.1f;
        circleDef.friction = 5f;
        circleDef.restitution = 0.2f;
        circleDef.filter.groupIndex = -1;

        for (int i = 0; i < 2; i++)
        {
            bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;

            if (i == 0)
                bodyDef.position.set((float)(axle1Body.getWorldCenter().x - 30*Math.cos(Math.PI/3)), (float)(axle1Body.getWorldCenter().y - 30*Math.sin(Math.PI/3)));
            else
                bodyDef.position.set((float)(axle2Body.getWorldCenter().x + 30*Math.cos(-Math.PI/3)), (float)(axle2Body.getWorldCenter().y + 30*Math.sin(-Math.PI/3)));

            bodyDef.allowSleep = false;

            if (i == 0)
                wheel1Body = world.createBody(bodyDef);
            else
                wheel2Body = world.createBody(bodyDef);

            (i == 0 ? wheel1Body : wheel2Body).createFixture(circleDef);
        }

        //add joints
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.enableMotor = true;

        revoluteJointDef.initialize(wheel1Body, axle1Body, wheel1Body.getWorldCenter());
        motor1RevJoint = (RevoluteJoint)world.createJoint(revoluteJointDef);

        revoluteJointDef.initialize(axle2Body, wheel2Body, wheel2Body.getWorldCenter());
        motor2RevJoint = (RevoluteJoint)world.createJoint(revoluteJointDef);


    }

    @Override
    public void resize(int _width, int _height)
    {
        camera.viewportWidth = _width;
        camera.viewportHeight = _height;
        camera.update();
    }
	@Override
	public void dispose() {
        //clean up
        carImage.dispose();
		world.dispose();
	}

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
// TODO Auto-generated method stub
        motor1RevJoint.setMotorSpeed((float)(150*Math.PI * 1));
        motor1RevJoint.setMaxMotorTorque(170f);

        motor2RevJoint.setMotorSpeed((float)(150*Math.PI * 1));
        motor2RevJoint.setMaxMotorTorque(170f);

        spring1PrisJoint.setMaxMotorForce((float)(30+Math.abs(800*Math.pow(spring1PrisJoint.getJointTranslation(), 200))));
        spring1PrisJoint.setMotorSpeed((spring1PrisJoint.getMotorSpeed() - 10*spring1PrisJoint.getJointTranslation())*40);

        spring2PrisJoint.setMaxMotorForce(30+Math.abs(800*(float)Math.pow(spring2PrisJoint.getJointTranslation(), 200)));
        spring2PrisJoint.setMotorSpeed((spring2PrisJoint.getMotorSpeed() - 10*spring2PrisJoint.getJointTranslation())*40);

        //cart.applyTorque(3f, false);
        return false;
    }
    @Override
    public boolean tap(float x, float y, int count, int button) {
// TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean longPress(float x, float y) {
// TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
// TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // TODO Auto-generated method stub
        camera.translate(-deltaX, 0);
        camera.update();
        return false;
    }
    @Override
    public boolean panStop(float f1, float f2, int x, int y) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean zoom(float initialDistance, float distance) {
// TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
                         Vector2 pointer1, Vector2 pointer2) {
// TODO Auto-generated method stub
        return false;
    }
}
