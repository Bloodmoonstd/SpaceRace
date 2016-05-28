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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
    OrthographicCamera guicam;

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


    Rectangle wleftBounds;
    Rectangle wrightBounds;

    Vector3 touchPoint = new Vector3();
	@Override
	public void create() {
        super.create();

        start = 0;
        holder = 0;
        speed = 1f;

        camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


        guicam = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        guicam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
        car.CreateVehicle(world);
        car.SetTextures("car.png", "wheel.png");

        verts = new ArrayList<Float>();
        float increment = 128.0f;

        climbing = true;

        createLevel(increment);

        test();

        //ground
        setGroundBody();

        debugMatrix = new Matrix4(camera.combined);

        wleftBounds = new Rectangle(0, 0, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight());
        wrightBounds = new Rectangle(Gdx.graphics.getWidth()/2, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}


	@Override
	public void render()
    {
        update();
        //if(camera.position.x  > start + 4096 + Gdx.graphics.getWidth()/2f)
       // {
           // start += 4096;
           // test();
        //}


        for (int i=0; i<5; i++){
            if (!Gdx.input.isTouched(i)) continue;
            guicam.unproject(touchPoint.set(Gdx.input.getX(i), Gdx.input.getY(i), 0));
            if (wleftBounds.contains(touchPoint.x, touchPoint.y)){

                car.leftMotor.setMotorSpeed(-400f);
                car.rightMotor.setMotorSpeed(-400f);
                //Move your player to the left!
            }else if (wrightBounds.contains(touchPoint.x, touchPoint.y)){
                car.leftMotor.setMotorSpeed(400f);
                car.rightMotor.setMotorSpeed(400f);
                //Move your player to the right!
            }
            else
            {
                car.leftMotor.setMotorSpeed(0f);
                car.rightMotor.setMotorSpeed(0f);
            }
        }

		// Advance the world, by the amount of time that has elapsed since thelast frame
		// Generally in a real game, dont do this in the render loop, as you aretying the physics
		// update rate to the frame rate, and vice versa
        world.step(Gdx.graphics.getDeltaTime()*2, 6, 2);


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
        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();

        car.wheelSprite.setPosition(car.leftWheel.getPosition().x - 32, car.leftWheel.getPosition().y - 32);
        spriteBatch.draw(car.wheelSprite, car.wheelSprite.getX(), car.wheelSprite.getY());
        car.wheelSprite.setPosition(car.rightWheel.getPosition().x - 32, car.rightWheel.getPosition().y - 32);
        spriteBatch.draw(car.wheelSprite, car.wheelSprite.getX(), car.wheelSprite.getY());

        car.chassisSprite.setPosition(car.chassis.getPosition().x - 124 - 28, car.chassis.getPosition().y - 60);
        spriteBatch.draw(car.chassisSprite, car.chassisSprite.getX(), car.chassisSprite.getY());

        spriteBatch.draw(car.wheelSprite, wleftBounds.x, wleftBounds.y);
        spriteBatch.draw(car.wheelSprite, wrightBounds.x, wrightBounds.y);

        debugRenderer.render(world, camera.combined);

        spriteBatch.end();

        polyBatch.setProjectionMatrix(camera.combined);
        polyBatch.begin();
        polySprite.draw(polyBatch);
        polyBatch.end();


	}

    public void update()
    {
        car.leftSpring.setMaxMotorForce(30+(float)Math.abs(800*Math.pow(car.leftSpring.getJointTranslation(), 2)));
        car.leftSpring.setMotorSpeed((car.leftSpring.getMotorSpeed() - 10*car.leftSpring.getJointTranslation())*0.4f);

        car.rightSpring.setMaxMotorForce(30+(float)Math.abs(800*Math.pow(car.rightSpring.getJointTranslation(), 2)));
        car.rightSpring.setMotorSpeed((car.rightSpring.getMotorSpeed() - 10*car.rightSpring.getJointTranslation())*0.4f);

        camera.position.set(car.chassis.getPosition().x, car.chassis.getPosition().y, 0);
        camera.update();
    }
    public static int randInt(int min, int max) {

        Random rand;
        rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public void test()
    {
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
            groundFixtureDef[i].friction = 1f;
            groundFixtureDef[i].density = 0f;

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
        //speed *= -1f;
        //car.leftMotor.setMotorSpeed(20f * speed);
        //car.rightMotor.setMotorSpeed(20f * speed);

        //car.chassis.applyTorque(5f*-speed, true);
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
        //camera.translate(-deltaX, 0);
        //camera.update();
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
