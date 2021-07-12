import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameController;

public class Game extends GameApplication{
    private Entity player;
    public final int screenWidth = 1000; //1000
    public final int screenHeight = 600; //600
    public final int bgWidth = BackgroundComponent.backgroundWidth;
    public final int bgHeight = BackgroundComponent.backgroundHeight;
    private static ArrayList<SpawnData> allBoats = new ArrayList<>();
    private static int numGroups = 1;
    private static final ArrayList<ArrayList<Entity>> groups = new ArrayList<>();
    private static final boolean playerSpectator = false;
    public static boolean isPlayerSpectator() {
        return playerSpectator;
    }

    public static void receiveInfo(ArrayList<ShipInfo> information, int groupNum,
                                   ArrayList<ArrayList<Instruction>> instructions){
        allBoats = new ArrayList<>();
        numGroups = groupNum;
        for (int i = 0; i < information.size(); i++) {
            ShipInfo ship = information.get(i);
            for (Instruction inst:instructions.get(i)) {
                if(i>=14)inst.printInstruction();
            }
            allBoats.add(new SpawnData(ship.getPosition().getX(), ship.getPosition().getY())
                    .put("group", ship.getGroupNum()).put("side", ship.isSide())
                    .put("attachment", ship.getAttachment()).put("type", ship.getShipName()).put("id", i)
                    .put("instructions", instructions.get(i)));
        }
    }

    //My old buddy randInt
    public static int randInt(int min, int max){
        return (int)(Math.random()*(max + 1 - min)) + min;
    }

    public static double angleToPolarRadians(double angle){
        //what the heckity heck is the rotation system anyways
        //the same number can mean two different angles and its stupidly difficult to actually put into the polar system
        //please, just make your program follow polar coordinates for once in your life
        //it's literally what you're looking for and allows you to do actual math
        //we use cartesian coordinates anyway, don't make it so that using Math.abs actually has to make sense
        //and the Math library uses radians anyways, so just base rotations on the polar system
        //on another note i'm really starting to like these types of conditionals. so smooth, so easy to write.
        double output = angle<0?Math.abs(angle%360):360-Math.abs(angle%360);
        return output*Math.PI/180.0;
    }

    public enum Type {
        PLAYER, BULLET, BACKGROUND, TURRET, EXPLOSION, GUNBOAT, DESTROYER, CARRIER, PLANE
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("test");
        settings.setVersion("1.0");
        settings.setWidth(screenWidth);
        settings.setHeight(screenHeight);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
        settings.setDeveloperMenuEnabled(true);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory(){
            @NotNull
            @Override
            public FXGLMenu newMainMenu(){
                return new MainMenu();
            }
        });
    }

    public void initBoats(){
        for (int i = 0; i <= numGroups; i++) groups.add(new ArrayList<>());
        ArrayList<Integer> planeSave = new ArrayList<>();
        ArrayList<Entity> tracker = new ArrayList<>();
        for (int i = 0; i < allBoats.size(); i++) {
            SpawnData data = allBoats.get(i);
            //System.out.println(data.getData());
            SpawnData newData = new SpawnData(data.getX(), data.getY()).put("group", groups.get(data.get("group")))
                    .put("side", data.get("side")).put("id", data.get("id")).put("instructions", data.get("instructions"));
            if(data.get("type").toString().contains("Player")){
                player = spawn("Player", newData.put("type", data.get("type")).put("id", data.get("id")));
                tracker.add(player);
            }else if(data.get("type").toString().equals("Plane")){
                planeSave.add(i);
                tracker.add(null);
            }else{
                tracker.add(spawn(data.get("type").toString(), newData));
            }
        }
        for (Integer i:planeSave) {
            SpawnData data = allBoats.get(i);
            spawn("Plane", new SpawnData(data.getX(), data.getY()).put("group", groups.get(data.get("group")))
                    .put("side", data.get("side")).put("attachment", tracker.get(data.get("attachment"))).put("id", data.get("id"))
                    .put("instructions", data.get("instructions")));
        }
    }

    private static double volume = 0.1;
    private static double volume2 = 0.5;

    @Override
    protected void onPreInit(){
        getSettings().setGlobalSoundVolume(volume);
    }

    public static void getVolume(double i, double i2){
        volume = i;
        volume2 = i2;
    }

    @Override
    protected void initGame() {
        getSettings().setGlobalSoundVolume(volume);
        getSettings().setGlobalMusicVolume(volume2);
        //Music is Jimi Hendrix's Machine Gun Guitar Background Track
        //Source: https://www.youtube.com/watch?v=DAIi092yTkU
        loopBGM("bgm.mp3");
        getGameWorld().addEntityFactory(new GameFactory());
        getGameScene().setBackgroundColor(Color.color(0, 0.467, 0.745, 1.0));
        initBoats();

        spawn("Background");
        int dist = 200;
        getGameScene().getViewport().setBounds(-dist, -dist, bgWidth + dist, bgHeight + dist);
        getGameScene().getViewport().bindToEntity(player, getAppWidth()/2.0, getAppHeight()/2.0);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Up1") {
            @Override
            protected void onAction() {
                double theta = angleToPolarRadians(player.getRotation());
                double speed = player.getComponent(BoatComponent.class).getSpeed();
                player.translate(speed*Math.cos(theta), -1*speed*Math.sin(theta));
                //player.translateY(-1*player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Up2") {
            @Override
            protected void onAction() {
                double theta = angleToPolarRadians(player.getRotation());
                double speed = player.getComponent(BoatComponent.class).getSpeed();
                player.translate(speed*Math.cos(theta), -1*speed*Math.sin(theta));
                //player.translateY(-1*player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("Up3") {
            @Override
            protected void onAction() {
                double theta = angleToPolarRadians(player.getRotation());
                double speed = player.getComponent(BoatComponent.class).getSpeed();
                player.translate(speed*Math.cos(theta), -1*speed*Math.sin(theta));
                //player.translateY(-1*player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.KP_UP);

        getInput().addAction(new UserAction("Down1") {
            @Override
            protected void onAction() {
                double theta = angleToPolarRadians(player.getRotation());
                double speed = -1*player.getComponent(BoatComponent.class).getSpeed();
                player.translate(speed*Math.cos(theta), -1*speed*Math.sin(theta));
                //player.translateY(player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Down2") {
            @Override
            protected void onAction() {
                double theta = angleToPolarRadians(player.getRotation());
                double speed = -1*player.getComponent(BoatComponent.class).getSpeed();
                player.translate(speed*Math.cos(theta), -1*speed*Math.sin(theta));
                //player.translateY(player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.DOWN);

        getInput().addAction(new UserAction("Down3") {
            @Override
            protected void onAction() {
                double theta = angleToPolarRadians(player.getRotation());
                double speed = -1*player.getComponent(BoatComponent.class).getSpeed();
                player.translate(speed*Math.cos(theta), -1*speed*Math.sin(theta));
                //player.translateY(player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.KP_DOWN);

        getInput().addAction(new UserAction("Left1") {
            @Override
            protected void onAction() {
                player.rotateBy(-1*player.getComponent(BoatComponent.class).getTurningSpeed());
                //player.translateX(-1*player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Left2") {
            @Override
            protected void onAction() {
                player.rotateBy(-1*player.getComponent(BoatComponent.class).getTurningSpeed());
                //player.translateX(-1*player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("Left3") {
            @Override
            protected void onAction() {
                player.rotateBy(-1*player.getComponent(BoatComponent.class).getTurningSpeed());
                //player.translateX(-1*player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.KP_LEFT);

        getInput().addAction(new UserAction("Right1") {
            @Override
            protected void onAction() {
                player.rotateBy(player.getComponent(BoatComponent.class).getTurningSpeed());
                //player.translateX(player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Right2") {
            @Override
            protected void onAction() {
                player.rotateBy(player.getComponent(BoatComponent.class).getTurningSpeed());
                //player.translateX(player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("Right3") {
            @Override
            protected void onAction() {
                player.rotateBy(player.getComponent(BoatComponent.class).getTurningSpeed());
                //player.translateX(player.getComponent(PlayerComponent.class).getSpeed());
            }
        }, KeyCode.KP_RIGHT);

        getInput().addAction(new UserAction("Shoot Mouse") {
            @Override
            protected void onAction(){
                player.getComponent(BoatComponent.class).shoot();
            }
        }, MouseButton.PRIMARY);

        getInput().addAction(new UserAction("Shoot Keypad") {
            @Override
            protected void onAction(){
                player.getComponent(BoatComponent.class).shoot();
            }
        }, KeyCode.INSERT);
    }

    public static boolean getSide(Entity ent){
        Entity e = getAttachment(ent);
        if(e.getComponentOptional(AIComponent.class).equals(Optional.empty())){
            return true;
        }else{
            return e.getComponent(AIComponent.class).getSide();
        }
    }

    public static Entity getAttachment(Entity ent){
        Entity e = ent;
        if(ent.getType().equals(Type.BULLET)) e = ent.getComponent(BulletComponent.class).getAttachment();
        if(ent.getType().equals(Type.TURRET)) e = ent.getComponent(TurretComponent.class).getAttachment();
        return e;
    }

    @Override
    protected void initPhysics() {
        PhysicsWorld physics = getPhysicsWorld();
        CollisionHandler turretCollision = new CollisionHandler(Type.BULLET, Type.TURRET) {
            @Override
            protected void onCollisionBegin(Entity a, Entity b) {
                super.onCollisionBegin(a, b);
                if(getAttachment(a)!=getAttachment(b)){
                    a.removeFromWorld();
                }
            }
        };
        physics.addCollisionHandler(turretCollision);
        CollisionHandler enemyCollision = new CollisionHandler(Type.BULLET, Type.GUNBOAT) {
            @Override
            protected void onCollisionBegin(Entity a, Entity b) {
                super.onCollisionBegin(a, b);
                if(!b.getComponentOptional(BoatComponent.class).equals(Optional.empty())){
                    if(b.getComponent(BoatComponent.class).isSpectator()) return;
                }
                if(getSide(getAttachment(a))!=getSide(getAttachment(b))){
                    spawn("Explosion", new SpawnData(a.getCenter()));
                    if(!getAttachment(b).getComponentOptional(PlayerComponent.class).equals(Optional.empty())){
                        if(!b.getComponent(BoatComponent.class).isSpectator()) {
                            getGameScene().getViewport().shakeRotational(1);
                        }
                    }
                    a.removeFromWorld();
                    if(!b.getComponentOptional(BoatComponent.class).equals(Optional.empty())){
                        getAttachment(b).getComponent(BoatComponent.class).takeDamage();
                    }
                }
            }
        };
        physics.addCollisionHandler(enemyCollision);
        physics.addCollisionHandler(enemyCollision.copyFor(Type.BULLET, Type.DESTROYER));
        physics.addCollisionHandler(enemyCollision.copyFor(Type.BULLET, Type.CARRIER));
        physics.addCollisionHandler(enemyCollision.copyFor(Type.BULLET, Type.PLANE));
        physics.addCollisionHandler(enemyCollision.copyFor(Type.BULLET, Type.PLAYER));
        CollisionHandler bulletCollision = new CollisionHandler(Type.BULLET, Type.BULLET) {
            @Override
            protected void onCollisionBegin(Entity a, Entity b) {
                super.onCollisionBegin(a, b);
                spawn("Explosion", new SpawnData(a.getCenter()));
                a.removeFromWorld();
                b.removeFromWorld();
            }
        };
        physics.addCollisionHandler(bulletCollision);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars){
        vars.put("boats", "Battle Forces");
        vars.put("entities", FXGL.getGameWorld().getEntities());
    }

    @Override
    protected void initUI(){
        Rectangle background = new Rectangle(200, getAppHeight());
        background.setFill(Paint.valueOf("0x00000044"));
        Text allyNum = getUIFactoryService().newText("", Color.WHITE, 20);
        allyNum.setTranslateX(10);
        allyNum.setTranslateY(30);
        allyNum.textProperty().bind(getsp("boats"));
        getGameScene().addUINodes(background, allyNum);
    }

    public static ArrayList<Entity> getBoats(){
        ArrayList<Entity> output = new ArrayList<>();
        for (Entity e:FXGL.getGameWorld().getEntities()) {
            if(!e.getComponentOptional(BoatComponent.class).equals(Optional.empty()))output.add(e);
        }
        return output;
    }

    @Override
    protected void onUpdate(double tpf){
        ArrayList<Entity> boats = getBoats();
        ArrayList<Integer> counter = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        ArrayList<Integer> counter2 = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        for (Entity e:boats) {
            if(getSide(e)){
                if(!e.getComponent(BoatComponent.class).isSpectator()) {
                    counter.set(e.getComponent(BoatComponent.class).getType() - 1,
                            counter.get(e.getComponent(BoatComponent.class).getType() - 1) + 1);
                }
            }else{
                if(!e.getComponent(BoatComponent.class).isSpectator()) {
                    counter2.set(e.getComponent(BoatComponent.class).getType() - 1,
                            counter2.get(e.getComponent(BoatComponent.class).getType() - 1) + 1);
                }
            }
        }
        set("boats", "Allied Forces:\n    Gunboats: " + counter.get(0) + "\n    Destroyers: " + counter.get(1)
                + "\n    Carriers: " + counter.get(2)+ "\n    Planes: " + counter.get(3) + "\n\n" + "Enemy Forces:\n" +
                "    Gunboats: " + counter2.get(0) + "\n    Destroyers: " + counter2.get(1)
                + "\n    Carriers: " + counter2.get(2)+ "\n    Planes: " + counter2.get(3));
        set("entities", boats);
        counter2.removeIf(integer -> integer==0);
        if(counter2.size()==0){
            //this is probably where all my work went.
            //I feel like I would be meh at creative writing.
            String[] congrats = new String[]{
                    "\"Leadership is a two street, loyalty up and loyalty down.\nRespect for one's superiors; care for one's crew.\" - Rear Admiral Grace Hopper",
                    "\"We have met the enemy and they are ours.\" - Commodore Oliver Hazard Perry",
                    "\"Praise the Lord and pass the ammunition!\" - Lieutenant Howell Maurice Forgy",
                    "\"If you know your enemy and you know yourself, \nyou need not fear the result of a hundred battles.\" - Sun Tzu",
                    "\"It doesn't take a hero to order men into battle.\nIt takes a hero to be one of those men who goes into battle.\" - General Norman Schwarzkopf",
                    "\"I want you to try things that no one has ever tried because they're absolutely stupid.\" - Orson Scott Card",
                    "\"I have not yet begun to fight!\" - Captain John Paul Jones",
                    "\"D*mn the torpedoes, Full speed ahead!\" - Admiral David Glasgow Farragut"
            };
            getDialogService().showConfirmationBox(congrats[Game.randInt(0, congrats.length-1)]+"\nCongratulations on your victory! Play again?", answer -> {
                if(answer){
                    getGameController().gotoMainMenu();
                }else{
                    getGameController().exit();
                }
            });
        }
        //I'm very sleepy and this seems smart for now
        counter.removeIf(integer -> integer==0);
        if(counter.size()==0){
            player.getComponent(PlayerComponent.class).die(true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
