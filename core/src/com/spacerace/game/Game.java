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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Game extends ApplicationAdapter implements GestureDetector.GestureListener{

    PolygonSpriteBatch polyBatch;
    SpriteBatch spriteBatch;

    Texture textureSolid;
    PolygonSprite polySprite;

	World world;
    OrthographicCamera camera;

    //Ground variables
    List<Float> verts;
    Boolean climbing;

    TextureRegion textureRegion;
    int start;
    int holder;
    float[] test = new float[512];

    //test terrain poly
    float[][] polyVerts = new float[50][8];
    Body[] groundBody = new Body[50];
    BodyDef[] groundBodyDef = new BodyDef[50];
    Fixture[] groundFixture = new Fixture[50];
    FixtureDef[] groundFixtureDef = new FixtureDef[50];
    PolygonShape[] groundPoly = new PolygonShape[50];



    float speed;

    Car car;

    //Renderer
    Box2DDebugRenderer debugRenderer;
    ShapeRenderer shapeRenderer;
    Matrix4 debugMatrix;

	@Override
	public void create() {
        super.create();

        start = 0;
        holder = 0;
        speed = 1f;

        camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0);

        Gdx.input.setInputProcessor(new GestureDetector(this));

        //testing wheel sprite
        spriteBatch = new SpriteBatch();

        shapeRenderer = new ShapeRenderer();
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

        car = new Car();
        car.createVehicle(world);

        verts = new ArrayList<Float>();
        float increment = 128.0f;

        climbing = true;

        createLevel(increment);

        test();

        //ground
        setGroundBody();

        debugMatrix = new Matrix4(camera.combined);
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

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // *****************************check where the sprites are supposed to be*************************** //
        /*
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1);
        //shapeRenderer.circle(wheel1Body.getPosition().x, wheel1Body.getPosition().y, 32f);
        //shapeRenderer.rect(cart.getPosition().x - 124, cart.getPosition().y - 60, 248, 120);
        shapeRenderer.end();
        */


        //carSprite.setPosition(cart.getPosition().x - 124, cart.getPosition().y - 60);
        //carSprite.setRotation(cart.getTransform().getRotation()* (float)degreesToRadians);

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();
        //wheelSprite.setPosition(leftWheel.getPosition().x - 32, leftWheel.getPosition().y - 32);
        //spriteBatch.draw(wheelSprite, wheelSprite.getX(), wheelSprite.getY());
        //wheelSprite.setPosition(rightWheel.getPosition().x - 32, rightWheel.getPosition().y - 32);
        //spriteBatch.draw(wheelSprite, wheelSprite.getX(), wheelSprite.getY());

        //spriteBatch.draw(carSprite, carSprite.getX(), carSprite.getY());
        debugRenderer.render(world, debugMatrix);
        spriteBatch.end();

        polyBatch.setProjectionMatrix(camera.combined);
        polyBatch.begin();
        polySprite.draw(polyBatch);
        polyBatch.end();

	}

    public static int randInt(int min, int max) {

        Random rand;
        rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
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

            //float randY = (float)randInt(0, 50);
            float randY = 0;
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
            groundFixtureDef[i].friction = 0.5f;
            groundFixtureDef[i].density = 2f;

            groundFixture[i] = groundBody[i].createFixture(groundFixtureDef[i]);
        }

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
		world.dispose();
	}

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
// TODO Auto-generated method stub
        speed *= -1;
        car.leftMotor.setMotorSpeed(2f * speed);
        car.rightMotor.setMotorSpeed(2f * speed);
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
