import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class PlayerComponent extends Component {
    private double playerSpeed;
    private double speed;
    private final ArrayList<Entity> group;
    private final boolean side;

    public PlayerComponent(ArrayList<Entity> g, boolean s){
        this.group = g;
        this.side = s;
    }

    public ArrayList<Entity> getGroup() {
        return group;
    }

    public boolean isSide() {
        return side;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public void onAdded() {
        BoatComponent b = entity.getComponent(BoatComponent.class);
        this.playerSpeed = b.getSpeed();
    }

    public boolean die(boolean forceDead){
        BoatComponent b = entity.getComponent(BoatComponent.class);
        if(b.getHp()<=0 || forceDead){
            b.destroyTurrets();
            group.remove(entity);
            entity.removeFromWorld();
            String[] comebacks = new String[]{
                    "A navy without a commander can't survive.",
                    "The captain went down with his ship.",
                    "I guess war tactics aren't really your thing.",
                    "\"If you know your enemy and you know yourself, \nyou need not fear the result of a hundred battles.\" - Sun Tzu",
                    "All these guys did was go forward, and you still messed it up. How?",
                    "When the admiral dies, the battle is lost.",
                    "I hope you went out fighting.",
                    "These ships have really bad designs."
            };
            getDialogService().showConfirmationBox(comebacks[Game.randInt(0, comebacks.length-1)]+"\nPlay again?", answer -> {
                if(answer){
                    getGameController().gotoMainMenu();
                }else{
                    getGameController().exit();
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate(double tpf){
        if(die(false)){
            return;
        }
        speed = tpf*playerSpeed;
        //if (!entity.getPosition().equals(oldPos)) entity.rotateToVector(entity.getPosition().subtract(oldPos));
        //oldPos = entity.getPosition();
        var viewPort = new Rectangle2D(0, 0, BackgroundComponent.backgroundWidth, BackgroundComponent.backgroundHeight);
        if(getEntity().getX() < viewPort.getMinX()){
            getEntity().setX(viewPort.getMinX());
        } else if (getEntity().getRightX() > viewPort.getMaxX()){
            getEntity().setX(viewPort.getMaxX() - getEntity().getWidth());
        }
        if(getEntity().getY() < viewPort.getMinY()){
            getEntity().setY(viewPort.getMinY());
        }else if(getEntity().getBottomY() > viewPort.getMaxY()){
            getEntity().setY(viewPort.getMaxY() - getEntity().getHeight());
        }
    }
}
