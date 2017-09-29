package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener, AnimEventListener {

    Spatial townModel, oto;
    BulletAppState phisics;
    RigidBodyControl rigidTown, rigidOto;
    CharacterControl player;
    Vector3f walkDirecction;
    Vector3f camDir;
    Vector3f camLeft;
    AnimControl playerAnimation;
    boolean up = false, down = false, left = false, right = false, click = false, rayo = false;
    Ray ray;
    AnimChannel walk;

    public static void main(String[] args) {
        Main app = new Main();

        app.setDisplayStatView(false);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //Inicializar
        phisics = new BulletAppState();
        walkDirecction = new Vector3f();
        camDir = new Vector3f();
        camLeft = new Vector3f();

        //Configuración del entorno
        flyCam.setMoveSpeed(100);
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        stateManager.attach(phisics);
        setUpLight();
        setUpKeys();

        //Cargar Modelo
        assetManager.registerLocator("town.zip", ZipLocator.class);
        townModel = assetManager.loadModel("main.scene");
        townModel.setLocalTranslation(-0.5f, -0.5f, -0.5f);
        townModel.scale(2);

        //Damos rigides al entorno
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(townModel);
        rigidTown = new RigidBodyControl(sceneShape, 0);
        townModel.addControl(rigidTown);

        //Caja
        Box caja = new Box(5, 5, 5);
        Geometry geom = new Geometry("caja", caja);
        geom.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        geom.getMaterial().setColor("Color", ColorRGBA.Blue);
        geom.setLocalTranslation(0, 20, -30);

        Sphere cielo = new Sphere(30, 30, 5);
        Geometry geom2 = new Geometry("cielo", cielo);
        geom2.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        geom2.getMaterial().setColor("Color", ColorRGBA.Red);
        geom2.setLocalTranslation(0, 0, 0);
        geom2.scale(-100);

        CollisionShape col = CollisionShapeFactory.createBoxShape(geom);
        RigidBodyControl rb = new RigidBodyControl(col, 1f);
        geom.addControl(rb);

        //Configuramos el player
        CollisionShape playerCollition = new CapsuleCollisionShape(0, 0, 0);
        player = new CharacterControl(playerCollition, 50f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));

        //Atar nodos y spatial al nodo raiz
        phisics.getPhysicsSpace().add(rigidTown);
        phisics.getPhysicsSpace().add(rb);
        phisics.getPhysicsSpace().add(player);
        rootNode.attachChild(townModel);
        rootNode.attachChild(geom);
        rootNode.attachChild(geom2);
        cargarOto();
        phisics.setDebugEnabled(true);
    }

    private void setUpLight() {
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalize());

        rootNode.addLight(al);
        rootNode.addLight(dl);
    }

    private void setUpKeys() {
        //flyCam.setEnabled(false);

        inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("rayo", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(this, "up");
        inputManager.addListener(this, "down");
        inputManager.addListener(this, "left");
        inputManager.addListener(this, "right");
        inputManager.addListener(this, "jump");
        inputManager.addListener(this, "click");
        inputManager.addListener(this, "rayo");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "up":
                up = isPressed;
                break;
            case "down":
                down = isPressed;
                break;
            case "left":
                left = isPressed;
                break;
            case "right":
                right = isPressed;
                break;
            case "jump":
                if (isPressed) {
                    player.jump();
                }
                break;
            case "click":
                click = isPressed;
                break;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).mult(0.6f);
        camLeft.set(cam.getLeft()).mult(0.5f);
        walkDirecction.set(0, 0, 0);

        if (left) {
            walkDirecction.addLocal(camLeft);
        }
        if (right) {
            walkDirecction.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirecction.addLocal(camDir);
            if (!walk.getAnimationName().equals("Walk")) {
                walk.setAnim("Walk", 0.50f);
                walk.setLoopMode(LoopMode.Loop);
            }
        }
        if (down) {
            walkDirecction.addLocal(camDir.negate());
        }

        //flyCam.setEnabled(click);

        player.setWalkDirection(walkDirecction);
        cam.setLocation(player.getPhysicsLocation());
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void cargarOto() {
        oto = assetManager.loadModel("Models/Oto/Oto.mesh.j3o");

        oto.setLocalTranslation(20, 10, 0f);
        oto.scale(1);

        //Damos rigides a oto
        CollisionShape mayaOto = CollisionShapeFactory.createDynamicMeshShape(oto);
        rigidOto = new RigidBodyControl(mayaOto, 1f);
        oto.addControl(rigidOto);
        oto.addControl(player);

        phisics.getPhysicsSpace().add(rigidOto);
        rootNode.attachChild(oto);

        playerAnimation = oto.getControl(AnimControl.class);
        playerAnimation.addListener(this);
        walk = playerAnimation.createChannel();
        walk.setAnim("stand");
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("Walk")) {
            channel.setAnim("stand", 0.50f);
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setSpeed(1f);
        }
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }
}
