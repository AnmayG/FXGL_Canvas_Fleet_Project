import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

public class BulletComponent extends Component{
    private final Entity attachment;

    public BulletComponent(Entity a){this.attachment = a;}

    public Entity getAttachment() {
        return attachment;
    }

    @Override
    public void onAdded() {
        entity.getComponent(ExpireCleanComponent.class).resume();
    }
}
