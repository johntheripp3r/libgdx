/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.gdxBulletConstants;
import com.badlogic.gdx.physics.bullet.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.physics.bullet.btSoftBody;
import com.badlogic.gdx.physics.bullet.btSoftBodyHelpers;
import com.badlogic.gdx.physics.bullet.btSoftBodyRigidBodyCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btSoftBodyWorldInfo;
import com.badlogic.gdx.physics.bullet.btSoftRigidDynamicsWorld;

/** @author xoppa */
public class SoftBodyTest extends BaseBulletTest {
	btSoftBodyWorldInfo worldInfo;
	btSoftBody softBody;
	Mesh mesh;
	Matrix4 tmpM = new Matrix4();
	Color color = new Color(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
	
	@Override
	public BulletWorld createWorld () {
		btDefaultCollisionConfiguration collisionConfiguration = new btSoftBodyRigidBodyCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 broadphase = new btAxisSweep3(Vector3.tmp.set(-1000, -1000, -1000), Vector3.tmp2.set(1000, 1000, 1000), 1024);
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btSoftRigidDynamicsWorld dynamicsWorld = new btSoftRigidDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		
		worldInfo = new btSoftBodyWorldInfo();
		worldInfo.setM_broadphase(broadphase);
		worldInfo.setM_dispatcher(dispatcher);
		worldInfo.getM_sparsesdf().Initialize();
		
		return new BulletWorld(collisionConfiguration, dispatcher, broadphase, solver, dynamicsWorld);
	}
	
	@Override
	public void create () {
		super.create();
		
		world.add("ground", 0f, 0f, 0f)
		.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		float x0 = -2f, y0 = 6f, z0 = -2f;
		float x1 = 8f, y1 = 6f, z1 = 8f;
		Vector3 patch00 = new Vector3(x0, y0, z0);
		Vector3 patch10 = new Vector3(x1, y1, z0);
		Vector3 patch01 = new Vector3(x0, y0, z1);
		Vector3 patch11 = new Vector3(x1, y1, z1);
		softBody = btSoftBodyHelpers.CreatePatch(worldInfo, patch00, patch10, patch01, patch11, 15, 15, 15, false);
		softBody.takeOwnership();
		softBody.setTotalMass(100f);
		((btSoftRigidDynamicsWorld)(world.dynamicsWorld)).addSoftBody(softBody);
		
		int vertCount = softBody.getNodeCount();
		int faceCount = softBody.getFaceCount(); 
		mesh = new Mesh(false, vertCount, faceCount*3,  new VertexAttribute(Usage.Position, 3, "a_position"));
		mesh.getVerticesBuffer().position(0);
		mesh.getVerticesBuffer().limit(vertCount * mesh.getVertexSize() / 4);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(faceCount * 3);
		softBody.getVertices(mesh.getVerticesBuffer(), vertCount, mesh.getVertexSize(), 0);
		softBody.getIndices(mesh.getIndicesBuffer(), faceCount);
	}
	
	@Override
	public void dispose () {
		super.dispose();
		worldInfo.delete();
		worldInfo = null;
		mesh.dispose();
		mesh = null;
	}
	
	@Override
	public void render () {
		super.render();
		if (world.renderMeshes) {
			softBody.getVertices(mesh.getVerticesBuffer(), softBody.getNodeCount(), mesh.getVertexSize(), 0);
			softBody.getWorldTransform(tmpM);
			Gdx.gl10.glPushMatrix();
			Gdx.gl10.glMultMatrixf(tmpM.val, 0);
			Gdx.gl10.glColor4f(color.r, color.g, color.b, color.a);
			mesh.render(GL10.GL_TRIANGLES);
			Gdx.gl10.glPopMatrix();
		}
		
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}
