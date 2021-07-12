import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.geometry.Point2D;
import javafx.util.Duration;


import java.util.ArrayList;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFactory implements EntityFactory {
    @Spawns("Background")
    public Entity spawnBackground(SpawnData data){
        return entityBuilder(data)
                .type(Game.Type.BACKGROUND)
                .with(new BackgroundComponent())
                .build();
    }

    @Spawns("Player")
    public Entity spawnPlayer(SpawnData data){
        String s = data.get("type");
        s = s.substring(s.indexOf(":")+1);
        return entityBuilder(data)
                .type(Game.Type.PLAYER)
                .viewWithBBox(s + ".png")
                .collidable()
                .zIndex(s.equals("Plane")?1003:1000)
                .with(new BoatComponent(s))
                .with(new PlayerComponent(new ArrayList<>(), true))
                .buildAndAttach();
    }

    @Spawns("Turret")
    public Entity spawnTurret(SpawnData data, Entity p){
        return entityBuilder(data)
                .type(Game.Type.TURRET)
                .viewWithBBox("Turret.png")
                .zIndex(1002)
                .with(new TurretComponent(new Point2D(-48, -10), -35, p))
                .build();
    }

    @Spawns("Bullet")
    public Entity spawnBullet(SpawnData data, Entity a) {
        var expireClean = new ExpireCleanComponent(Duration.seconds(Math.random()*2));
        expireClean.pause();

        return entityBuilder(data)
                .type(Game.Type.BULLET)
                .viewWithBBox("Bullet.png")
                .opacity(20)
                .zIndex(1001)
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 200))
                .with(new BulletComponent(a))
                .with(expireClean)
                .build();
    }

    @Spawns("Gunboat")
    public Entity spawnGunboat(SpawnData data){
        return entityBuilder(data)
                .type(Game.Type.GUNBOAT)
                .viewWithBBox("Gunboat.png")
                .zIndex(1000)
                .with(new CollidableComponent(true))
                .with(new BoatComponent("Gunboat"))
                .with(new AIComponent(1, data.get("group"), data.get("id"), data.get("instructions"), data.get("side")))
                .buildAndAttach();
    }

    @Spawns("Destroyer")
    public Entity spawnDestroyer(SpawnData data){
        return entityBuilder(data)
                .type(Game.Type.DESTROYER)
                .viewWithBBox("Destroyer.png")
                .with(new CollidableComponent(true))
                .zIndex(1000)
                .with(new BoatComponent("Destroyer"))
                .with(new AIComponent(2, data.get("group"), data.get("id"), data.get("instructions"), data.get("side")))
                .buildAndAttach();
    }

    @Spawns("Carrier")
    public Entity spawnCarrier(SpawnData data){
        return entityBuilder(data)
                .type(Game.Type.CARRIER)
                .viewWithBBox("Carrier.png")
                .with(new CollidableComponent(true))
                .zIndex(1000)
                .with(new BoatComponent("Carrier"))
                .with(new AIComponent(3, data.get("group"), data.get("id"), data.get("instructions"), data.get("side")))
                .buildAndAttach();
    }

    @Spawns("Plane")
    public Entity spawnPlane(SpawnData data){
        return entityBuilder(data)
                .type(Game.Type.PLANE)
                .viewWithBBox("Plane.png")
                .with(new CollidableComponent(true))
                .zIndex(1003)
                .with(new BoatComponent("Plane"))
                .with(new AIComponent(4, data.get("group"), data.get("id"), data.get("instructions"), data.get("side"), data.get("attachment")))
                .buildAndAttach();
    }

    @Spawns("Explosion")
    public Entity spawnExplosion(SpawnData data){
        return entityBuilder()
                .at(data.getX() - 40, data.getY() - 40)
                .type(Game.Type.EXPLOSION)
                .zIndex(2000)
                .view(texture("explosion.png", 80 * 48, 80).toAnimatedTexture(48, Duration.seconds(0.75)).play())
                .build();
    }
}
