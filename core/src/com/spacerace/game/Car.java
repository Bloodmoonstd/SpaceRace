package com.spacerace.game;

import com.badlogic.gdx.Gdx;
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

    public void createVehicle(World world)
    {
        Body chassis, leftWheel, rightWheel, leftAxle, rightAxle;
        PrismaticJoint leftSpring, rightSpring;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight()/2);

        chassis = world.createBody(bodyDef);

        PolygonShape polyBox = new PolygonShape();
        polyBox.setAsBox(124f, 60f);

        FixtureDef boxDef = new FixtureDef();
        boxDef.shape = polyBox;
        boxDef.density = 0.2f;
        boxDef.friction = 0f;
        boxDef.restitution = 0.2f;
        boxDef.filter.groupIndex = -1;
        chassis.createFixture(boxDef);

        polyBox.setAsBox(20f, 8f, new Vector2(-80, -20f), (float)Math.PI/3);
        chassis.createFixture(boxDef);

        polyBox.setAsBox(20f, 8f, new Vector2(80, -20f), (float)-Math.PI/3);
        chassis.createFixture(boxDef);

        boxDef.density = 1;

        polyBox.setAsBox(20f, 5f, new Vector2(0,0), (float)Math.PI/3);
        bodyDef.position.set(Gdx.graphics.getWidth()/2 -80f + 15f - (float)(60*Math.cos(Math.PI/3)),Gdx.graphics.getHeight()/2 -20f + 20f - (float)(60*Math.sin(Math.PI/3)));
        leftAxle = world.createBody(bodyDef);
        leftAxle.createFixture(boxDef);

        polyBox.setAsBox(20f, 5f, new Vector2(0,0), (float)-Math.PI/3);
        bodyDef.position.set(Gdx.graphics.getWidth()/2 + 80f -15f + (float)(60*Math.cos(-Math.PI/3)),Gdx.graphics.getHeight()/2 -20f  + 20f + (float)(60*Math.sin(-Math.PI/3)));
        rightAxle = world.createBody(bodyDef);
        rightAxle.createFixture(boxDef);

        //axle
        PrismaticJointDef prismaticJointDef;
        prismaticJointDef = new PrismaticJointDef();
        prismaticJointDef.lowerTranslation = -0.15f;
        prismaticJointDef.upperTranslation = 0.25f;
        prismaticJointDef.enableLimit = true;
        prismaticJointDef.enableMotor = true;
        prismaticJointDef.collideConnected = false;

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
        circleDef.friction = 1f;
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
        revoluteJointDef.maxMotorTorque = 10000000000f;
        revoluteJointDef.enableLimit = false;
        revoluteJointDef.collideConnected = false;

        revoluteJointDef.initialize(leftWheel, leftAxle, leftWheel.getWorldCenter());
        leftMotor = (RevoluteJoint)world.createJoint(revoluteJointDef);

        revoluteJointDef.initialize(rightWheel, rightAxle, rightWheel.getWorldCenter());
        rightMotor = (RevoluteJoint)world.createJoint(revoluteJointDef);
    }
}
