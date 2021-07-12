import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getInput;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;

public class TurretComponent extends Component {
    private final Point2D center;
    private final int xDistance;
    private final Entity attachment;
    private final LocalTimer weaponTimer = FXGL.newLocalTimer();
    private double offset = 0.0;

    public TurretComponent(Point2D dist, int xDist, Entity a){
        this.center = dist;
        this.xDistance = xDist;
        this.attachment = a;
    }

    public TurretComponent(Point2D dist, int xDist, Entity a, double offset){
        this.center = dist;
        this.xDistance = xDist;
        this.attachment = a;
        this.offset = offset;
    }

    public Entity getAttachment() {
        return attachment;
    }

    @Override
    public void onUpdate(double tpf){
        double newRot = Game.angleToPolarRadians(attachment.getRotation());
        entity.setPosition(attachment.getCenter().add(new Point2D(xDistance*Math.cos(newRot), -1*xDistance*Math.sin(newRot))
                .add(this.center).add(new Point2D(offset*Math.sin(newRot), offset*Math.cos(newRot)))));
        Point2D temp = calcDistance().subtract(entity.getCenter());
        temp = new Point2D(temp.getX(), -1*temp.getY());
        double angle = Math.atan2(temp.getY(), temp.getX());
        if(angle<0) angle+=Math.PI*2;
        //RotateToVector is grossly inaccurate when it comes to mouse positions for some reason
        double idkWhatThisIs = -23;
        entity.translate(entity.getHeight()*Math.cos(angle) + idkWhatThisIs, -1*entity.getHeight()*Math.sin(angle));
        //does this even do anything anymore?
        double angle2 = Math.atan2(temp.getY(), temp.getX());
        if(angle2<0) angle2+=Math.PI*2;
        entity.setRotation(-1*angle2*180.0/Math.PI);
    }

    public Point2D calcDistance(){
        //Point2D temp = new Point2D(10000, entity.getCenter().getY()).subtract(entity.getCenter());
        Point2D temp = new Point2D(0, 0);
        if(!temp.equals(new Point2D(0, 0))) return temp;
        if(!attachment.getComponentOptional(PlayerComponent.class).equals(Optional.empty())){
            return getInput().getMousePositionWorld();
        } else {
            GameWorld g = FXGL.getGameWorld();
            ArrayList<Entity> nearby = new ArrayList<>(g.getEntities());
            //longest conditional in the world
            nearby.removeIf(a->(a.getComponentOptional(BoatComponent.class).equals(Optional.empty()) || a==entity ||
                    a==attachment || Game.getSide(a)==Game.getSide(entity)));
            nearby.removeIf(a->(a.getComponent(BoatComponent.class).isSpectator()));
//            nearby.removeIf(a->(a.getType()==Game.Type.TURRET || a.getType()==Game.Type.BACKGROUND ||
//                    a.getType()==Game.Type.EXPLOSION || a==entity || a==attachment ||
//                    Game.getSide(a)==Game.getSide(entity)));
            ArrayList<Double> d = new ArrayList<>();
            for (Entity e:nearby) {
                //if(e.getType()!=Game.Type.TURRET && e.getType()!=Game.Type.BACKGROUND && Game.getSide(e)!=Game.getSide(entity)){
                    d.add(entity.getCenter().distance(e.getCenter()));
                //}
            }
            if(nearby.size() == 0) return new Point2D(entity.getCenter().getX(), 8000).subtract(entity.getCenter());
            return nearby.get(d.indexOf(Collections.min(d))).getCenter();
        }
    }

    public void shoot(){
        /*FIXME:
            Change the shoot position so that it actually shoots from where I want it to and make it so it doesn't shoot if hitting another turret
            Need to get all objects within line of fire, based on rotation and not other things.
            I have no idea why this isn't working, but it's actually close enough for most things and it isn't super noticeable
            I'm leaving it alone for now, but I believe the problem is because I'm rotating to the new place and then translating.
            However, I have no idea how to fix this. I can't calculate an angle from a position that I need the angle to calculate.
            So I'm just living with the pain. It's really not a big deal anyways.
         */
        Point2D shootPoint = calcDistance();
        if(weaponTimer.elapsed(Duration.seconds(1))) {
            double angle = Game.angleToPolarRadians(entity.getRotation());
            Point2D currentPosition = entity.getCenter().add(new Point2D(0, -7*Math.sin(angle))).add(new Point2D(30*Math.cos(angle), -1*30*Math.sin(angle)));
            Point2D vectorToMouse = shootPoint.subtract(currentPosition);
            if(shootPoint.distance(currentPosition)>1000)return;
            var bullet = spawnBullet(new SpawnData(currentPosition.getX(), currentPosition.getY()).put("direction", vectorToMouse), attachment);
            weaponTimer.capture();
        }
    }

    public Entity spawnBullet(SpawnData data, Entity a){
        //Having bullets that miss adds to the spice
        play("cannon.wav");
        var expireClean = new ExpireCleanComponent(Duration.seconds(Math.random() + 1));
        expireClean.pause();

        return entityBuilder(data)
                .type(Game.Type.BULLET)
                .viewWithBBox("Bullet.png")
                .opacity(1)
                .zIndex(1000)
                .with(expireClean)
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 400))
                .with(new BulletComponent(a))
                .buildAndAttach();
    }
}