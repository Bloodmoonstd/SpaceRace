package com.spacerace.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

/**
 * Created by Craig on 27/05/2016.
 */
public class Car {

    double DEGTORAD = 0.0174532925;

    public RevoluteJoint leftMotor, rightMotor;
    public Body chassis, leftWheel, rightWheel, leftAxle, rightAxle;
    PrismaticJoint leftSpring, rightSpring;

    public Sprite chassisSprite, wheelSprite;

    public void CreateVehicle(World world)
    {
        float chassisWidth = 96f;
        float chassisHeight = 60f;

        float hAxleWidth = 20f;
        float hAxleHeight = 8f;

        Vector2 leftAxleDistance = new Vector2(-20f, 0f);
        Vector2 rightAxleDistance = new Vector2(20f, 0f);

        float springWidth = hAxleWidth;
        float springHeight = hAxleHeight-2;

        Vector2 startPosition = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight()/2);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startPosition);
        bodyDef.allowSleep = false;
        chassis = world.createBody(bodyDef);

        PolygonShape polyBox = new PolygonShape();
        polyBox.setAsBox(chassisWidth, chassisHeight);

        FixtureDef boxDef = new FixtureDef();
        boxDef.shape = polyBox;
        boxDef.density = 0.5f;
        boxDef.friction = 0.5f;
        boxDef.restitution = 0.2f;
        boxDef.filter.groupIndex = -1;
        chassis.createFixture(boxDef);

        polyBox.setAsBox(hAxleWidth, hAxleHeight, leftAxleDistance, (float)Math.PI/3);
        chassis.createFixture(boxDef);

        polyBox.setAsBox(hAxleWidth, hAxleHeight, rightAxleDistance, (float)-Math.PI/3);
        chassis.createFixture(boxDef);

        boxDef.density = 1;

        polyBox.setAsBox(springWidth, springHeight, new Vector2(0,0), (float)Math.PI/3);
        bodyDef.position.set(startPosition.x + leftAxleDistance.x + 12f - (float)(60*Math.cos(Math.PI/3)), startPosition.y + 20f - (float)(60*Math.sin(Math.PI/3)));
        leftAxle = world.createBody(bodyDef);
        leftAxle.createFixture(boxDef);

        polyBox.setAsBox(springWidth, springHeight, new Vector2(0,0), (float)-Math.PI/3);
        bodyDef.position.set(startPosition.x + rightAxleDistance.x -12f + (float)(60*Math.cos(-Math.PI/3)),startPosition.y + 20f + (float)(60*Math.sin(-Math.PI/3)));
        rightAxle = world.createBody(bodyDef);
        rightAxle.createFixture(boxDef);

        //axle
        PrismaticJointDef prismaticJointDef;
        prismaticJointDef = new PrismaticJointDef();
        prismaticJointDef.lowerTranslation = -0.3f;
        prismaticJointDef.upperTranslation = 0.5f;
        prismaticJointDef.enableLimit = true;
        prismaticJointDef.enableMotor = true;
        prismaticJointDef.collideConnected = true;

        prismaticJointDef.initialize(chassis, leftAxle, leftAxle.getWorldCenter(), new Vector2((float)Math.cos(Math.PI/3), (float)Math.sin(Math.PI/3)));
        leftSpring = (PrismaticJoint)world.createJoint(prismaticJointDef);

        prismaticJointDef.initialize(chassis, rightAxle, rightAxle.getWorldCenter(), new Vector2((float)-Math.cos(Math.PI/3), (float)Math.sin(Math.PI/3)));
        rightSpring = (PrismaticJoint)world.createJoint(prismaticJointDef);

        //**************************************************WHEELS************************************************************************//
        CircleShape circle = new CircleShape();
        circle.setPosition(new Vector2(0,0));
        circle.setRadius(32f);

        FixtureDef circleDef = new FixtureDef();
        circleDef.shape = circle;
        circleDef.density = 0.1f;
        circleDef.friction = 15f;
        circleDef.restitution = 0.2f;
        circleDef.filter.groupIndex = -1;

        BodyDef leftWheelBodyDef = new BodyDef();
        leftWheelBodyDef.type = BodyDef.BodyType.DynamicBody;
        leftWheelBodyDef.position.set((float)(leftAxle.getWorldCenter().x - 30*Math.cos(Math.PI/3)), (float)(leftAxle.getWorldCenter().y - 30*Math.sin(Math.PI/3)));
        leftWheelBodyDef.allowSleep = false;

        leftWheel = world.createBody(leftWheelBodyDef);
        leftWheel.createFixture(circleDef);

        BodyDef rightWheelBodyDef = new BodyDef();
        rightWheelBodyDef.type = BodyDef.BodyType.DynamicBody;
        rightWheelBodyDef.position.set((float)(rightAxle.getWorldCenter().x + 30*Math.cos(-Math.PI/3)), (float)(rightAxle.getWorldCenter().y + 30*Math.sin(-Math.PI/3)));
        rightWheelBodyDef.allowSleep = false;

        rightWheel = world.createBody(rightWheelBodyDef);
        rightWheel.createFixture(circleDef);

        //add joints
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.enableMotor = true;
        revoluteJointDef.motorSpeed = 0f;
        revoluteJointDef.maxMotorTorque = 1000000f;
        revoluteJointDef.enableLimit = false;
        revoluteJointDef.collideConnected = false;

        revoluteJointDef.initialize(leftWheel, leftAxle, leftWheel.getWorldCenter());
        leftMotor = (RevoluteJoint)world.createJoint(revoluteJointDef);

        revoluteJointDef.initialize(rightWheel, rightAxle, rightWheel.getWorldCenter());
        rightMotor = (RevoluteJoint)world.createJoint(revoluteJointDef);
    }

    public void SetTextures(String _chassisTexture, String _wheelTexture)
    {
        Texture chassisTexture = new Texture(_chassisTexture);
        chassisSprite = new Sprite(chassisTexture);

        Texture wheelTexture = new Texture(_wheelTexture);
        wheelSprite = new Sprite(wheelTexture);


    }
}
