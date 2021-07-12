import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;

public class AIComponent extends Component {
    private final int type;
    private final ArrayList<Entity> group;
    private Entity carrier = null;
    private final boolean side;
    private int id = -1;
//    private ArrayList<Instruction> instructions = new ArrayList<>(Arrays.asList(
//            new Instruction(Duration.seconds(10), id), new Instruction(new Point2D(1000, 1000), id),
//            new Instruction(90.0, id), new Instruction(90.0, id), new Instruction(Duration.seconds(50), id),
//            new Instruction(0, 0, id)));
    private ArrayList<Instruction> instructions = new ArrayList<>();
    private final LocalTimer returnTimer = FXGL.newLocalTimer();
    private final LocalTimer waitTimer = FXGL.newLocalTimer();

    public AIComponent(int type, ArrayList<Entity> friends, int id, ArrayList<Instruction> i, boolean side){
        this.type = type;
        this.group = friends;
        this.side = side;
        instructions.addAll(i);
        setId(id);
    }

    public AIComponent(int type, ArrayList<Entity> friends, int id, ArrayList<Instruction> i, boolean side, Entity c){
        this.type = type;
        this.group = friends;
        setId(id);
        this.side = side;
        instructions.addAll(i);
        this.carrier = c;
    }

    public void setId(int identification){
        this.id = identification;
        for (Instruction i:instructions) {
            i.setTarget(this.id);
        }
    }

    public int getId() {
        return id;
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void followInstructions(){
        if(instructions.size()<=0)return;
        Instruction inst = instructions.get(0);
        if(inst.getType() == 0){
            Point2D temp = inst.getTargetPoint().subtract(entity.getCenter());
            double angle = Math.atan2(temp.getY(), temp.getX());
            if(angle<0) angle+=Math.PI*2;
            entity.setRotation(angle*180.0/Math.PI);
            double speed = entity.getComponent(BoatComponent.class).getSpeed();
            //entity.translate(-1*speed*Math.cos(angle), speed*Math.sin(angle));
            entity.translateTowards(inst.getTargetPoint(), speed);
            //if(!entity.getType().equals(Game.Type.PLANE))System.out.println(entity.getCenter() + " " + inst.getTargetPoint() + " " + angle);
            if(inst.getTargetPoint().distance(entity.getCenter()) < 100){
                inst.complete();
                instructions.remove(0);
            }
        }else if(inst.getType() == 1){
            if(type==4){
                inst.complete();
                instructions.remove(0);
                return;
            }
            entity.rotateBy(inst.getTurnAmount());
            inst.complete();
            instructions.remove(0);
        }else{
            if(type==4){
                inst.complete();
                instructions.remove(0);
                return;
            }
            if(waitTimer.elapsed(inst.getWaitAmount())){
                inst.complete();
                instructions.remove(0);
                waitTimer.capture();
            }
        }
    }

    public boolean getSide() {
        return side;
    }

    public int getType() {
        return type;
    }

    private boolean returningToCarrier = false;

    public boolean die(){
        BoatComponent b = entity.getComponent(BoatComponent.class);
        if(b.getHp()<=0){
            b.destroyTurrets();
            group.remove(entity);
            entity.removeFromWorld();
            return true;
        }
        return false;
    }

    private boolean boatAlive = true;
    @Override
    public void onUpdate(double tpf){
        if(die()) return;
        if(returnTimer.elapsed(Duration.seconds(10)) && type==4
                && (!carrier.getComponentOptional(BoatComponent.class).equals(Optional.empty()) && carrier!=null)
                || instructions.size()<=0){
            if(carrier==null){
                if(!returningToCarrier){
                    followInstructions();
                    return;
                }
            }
            if(entity.getPosition().distance(carrier.getCenter())<150){
                entity.getComponent(BoatComponent.class).shoot();
                return;
            }
            if(carrier.getComponentOptional(BoatComponent.class).equals(Optional.empty())){
                if(boatAlive){
                    returnTimer.capture();
                    boatAlive = false;
                }
                spawnExplosion();
                BoatComponent b = entity.getComponent(BoatComponent.class);
                b.destroyTurrets();
                group.remove(entity);
                entity.removeFromWorld();
                return;
            }
            returningToCarrier = true;
            Point2D target = carrier.getComponent(BoatComponent.class).getEntity().getCenter();
//            double angle2 = Math.atan2(target.getY(), target.getX());
//            if(angle2<0) angle2+=Math.PI*2;
//            entity.setRotation(-1*angle2*180.0/Math.PI);
            entity.rotateToVector(target);
            entity.translateTowards(target, entity.getComponent(BoatComponent.class).getSpeed());
            if(target.distance(entity.getCenter())<150) {
                returnTimer.capture();
                returningToCarrier = false;
            }
        }else{
            if(!returningToCarrier){
                followInstructions();
            }
        }
        entity.getComponent(BoatComponent.class).shoot();
    }

    private void spawnExplosion(){
        spawn("Explosion", new SpawnData(entity.getCenter().getX(), entity.getCenter().getY()));
    }
}
