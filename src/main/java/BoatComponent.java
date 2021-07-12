import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

import java.util.ArrayList;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class BoatComponent extends Component {
    private final ArrayList<Entity> turrets = new ArrayList<>();
    private int type;
    private int hp;
    private double turningSpeed;
    private double speed;
    private boolean spectator = Game.isPlayerSpectator();

    public BoatComponent(String t){
        switch (t){
            case("Gunboat")->{
                type=1;
                hp = 3;
            }
            case("Destroyer")->{
                type=2;
                hp = 6;
            }
            case("Carrier")->{
                type=3;
                hp = 9;
            }
            case("Plane")->{
                type=4;
                hp=1;
            }
        }
        double[] speed = getSpeed(type);
        this.speed = speed[0];
        this.turningSpeed = speed[1];
    }

    public int getType() {
        return type;
    }

    public double getTurningSpeed() {
        return turningSpeed;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public static double[] getSpeed(int type){
        double slowDown = 1/100.0;
        switch(type){
            case (1) -> {
                return new double[]{BackgroundComponent.backgroundWidth/10.0/5.0 * slowDown, 50 * slowDown};
            }
            case(2)->{
                return new double[]{BackgroundComponent.backgroundWidth/10.0/5.0 * slowDown, 25 * slowDown};
            }
            case(3)->{
                return new double[]{BackgroundComponent.backgroundWidth/10.0/10.0 * slowDown, 10 * slowDown};
            }
            case(4)->{
                return new double[]{BackgroundComponent.backgroundWidth/10.0/2.0 * slowDown, 100 * slowDown};
            }
        }
        return new double[]{1, 0.1};
    }

    public int getHp(){
        return hp;
    }

    public void takeDamage(){
        if(!spectator) hp-=1;
    }

    public void destroyTurrets(){
        for (Entity e:this.turrets) {
            e.removeFromWorld();
        }
    }

    @Override
    public void onAdded() {
        if(!entity.getType().equals(Game.Type.PLAYER)) {
            spectator = false;
        }
        if(entity.getType().equals(Game.Type.PLAYER) && spectator){
            this.speed = 15;
            this.turningSpeed = 5;
        }
        switch(this.type) {
            case(1)-> turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), -35, entity, 0));
            case(2)->{
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), 128, entity, 0));
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), 64, entity, 0));
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), -104, entity, 0));
            }
            case(3)->{
                //Just looking at this makes me realize how terrible my graphic design skills are
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), 190, entity, -47));
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), 93, entity, -47));
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), 0, entity, -47));
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), -99, entity, -47));
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), -190, entity, -47));
            }
            case(4)->{
                turrets.add(spawnTurret(new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()), 0, entity, 0));
            }
        }
    }

    public Entity spawnTurret(SpawnData data, int xdist, Entity p, double offset){
        //xdist is the radius of the circle and offset is how much I should offset the turret so it's not centered
        return entityBuilder(data)
                .type(Game.Type.TURRET)
                .viewWithBBox("Turret.png")
                .zIndex(1002)
                .collidable()
                .with(new TurretComponent(new Point2D(-12, -10), xdist, p, offset))
                .buildAndAttach();
    }

    public void shoot(){
        if(!spectator) {
            for (Entity t : turrets) {
                t.getComponent(TurretComponent.class).shoot();
            }
        }
    }
}
