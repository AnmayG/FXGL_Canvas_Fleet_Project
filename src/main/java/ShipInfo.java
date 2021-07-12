import javafx.geometry.Point2D;

public class ShipInfo {
    private String shipName = null;
    private Point2D position = null;
    private int index;
    private int groupNum;
    private boolean side;
    private int attachment;
    public ShipInfo(String s, Point2D s2, int index, int groupNum, boolean side, int attachment){
        this.position = s2;
        this.shipName = s;
        this.index = index;
        this.groupNum = groupNum;
        this.side = side;
        this.attachment = attachment;
    }
    public void setPlayer() {
        this.shipName="Player:"+this.shipName;
    }
    public Point2D getPosition() {
        return position;
    }
    public String getShipName() {
        return shipName;
    }
    public int getIndex() {
        return index;
    }
    public boolean isSide() {
        return side;
    }
    public int getAttachment() {
        return attachment;
    }
    public int getGroupNum() {
        return groupNum;
    }
    public void setShipName(String s){this.shipName=s;}
    public void setPosition(Point2D s){this.position =s;}
    public void setAttachment(int attachment) {
        this.attachment = attachment;
    }
    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }
    public void setSide(boolean side) {
        this.side = side;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
