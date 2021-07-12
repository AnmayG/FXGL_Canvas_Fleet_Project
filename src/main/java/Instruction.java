import javafx.geometry.Point2D;
import javafx.util.Duration;

public class Instruction {
    //It's 12PM and if it works it works
    private Point2D targetPoint = null;
    private double turnAmount = 0.0;
    private Duration waitAmount = Duration.seconds(0);
    private int type = 0;
    private boolean completed = false;
    private String name = "";
    private int target = -1;

    public Instruction(Point2D t, int target){
        this.targetPoint = t;
        this.type = 0;
        this.name = "Ship " + target + " moved to " + t.toString();
        this.target = target;
    }
    public Instruction(double t, double t2, int target){
        this.targetPoint = new Point2D(t, t2);
        this.type = 0;
        this.name = "Ship " + target + " moved to " + targetPoint.toString();
        this.target = target;
    }
    public Instruction(double t, int target){
        this.turnAmount = t;
        this.type = 1;
        this.name = "Ship " + target + " turned by " + turnAmount;
        this.target = target;
    }
    public Instruction(Duration t, int target){
        this.waitAmount = t;
        this.type = 2;
        this.name = "Ship " + target + " waited for " + t;
        this.target = target;
    }

    public void printInstruction(){
        System.out.print("[" + this.getTargetPoint() + " " + this.getWaitAmount() + " " +
                this.getTurnAmount() + " " + this.getTarget() + "]");
    }

    public void setTarget(int target){
        this.target = target;
    }

    public int getTarget() {
        return target;
    }

    public boolean isCompleted(){
        return this.completed;
    }

    public void complete(){
        System.out.println("Completed: " + name);
        this.completed =true;
    }

    public int getType(){
        return type;
    }

    public double getTurnAmount() {
        return turnAmount;
    }

    public Point2D getTargetPoint() {
        return targetPoint;
    }

    public Duration getWaitAmount(){
        return waitAmount;
    }
}
