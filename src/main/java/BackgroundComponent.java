import com.almasb.fxgl.entity.component.Component;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundComponent extends Component {
    public static int backgroundHeight = 4000;
    public static int backgroundWidth = 5000;
    private final Canvas canvas = new Canvas(backgroundWidth, backgroundHeight);

    @Override
    public void onAdded(){
        entity.getViewComponent().addChild(canvas);
        new GridRenderThread().start();
    }

    private class GridRenderThread extends Thread{
        AtomicBoolean renderFinished = new AtomicBoolean(false);
        GridRenderThread(){
            super("GridRenderThread");
            setDaemon(true);
        }
        @Override
        public void run(){
            while(true) {
                if (renderFinished.get()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                canvas.getGraphicsContext2D().clearRect(0, 0, backgroundWidth, backgroundHeight);
                GraphicsContext g = canvas.getGraphicsContext2D();
                g.setStroke(Color.color(0.138, 0.138, 0.375, 0.66).brighter().brighter());
                g.setLineWidth(7.5);
                g.strokeLine(0, 0, 0, backgroundHeight);
                g.strokeLine(0, 0, backgroundWidth, 0);
                g.strokeLine(backgroundWidth, 0, backgroundWidth, backgroundHeight);
                g.strokeLine(0, backgroundHeight, backgroundWidth, backgroundHeight);
                g.setStroke(Color.color(0, 0, 0.05, 0));
                g.setLineWidth(1);
                g.setStroke(Color.color(0.0, 0.0, 0.05, 1.0));
                String abcs = "ABCDEFGHIJ";
                g.setFont(Font.font("Arial", 100));
                for (int i = 1; i <= 10; i++) {
                    g.fillText(abcs.charAt(i-1)+"", (i-1)*backgroundWidth/10.0 + 10, 100);
                    g.strokeLine(i*backgroundWidth/10.0, 0, i*backgroundWidth/10.0, backgroundHeight);
                }
                for (int i = 1; i <= 8; i++) {
                    g.fillText(i + "", 10, i==1?200:(i-1)*backgroundHeight/8.0 + 100);
                    g.strokeLine(0, i*backgroundHeight/8.0, backgroundWidth, i*backgroundHeight/8.0);
                }
                renderFinished.set(true);
            }
        }
    }
}
