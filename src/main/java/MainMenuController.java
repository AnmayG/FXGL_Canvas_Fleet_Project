import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainMenuController{
    //The main allure of this code is the ability to create custom main menu screens in screen builder
    //Most of the special features can be done just by giving something a special id, but it'll run normally here too.
    //For example, if I name something "animateButton1" and a pane "animate1", the pane will be animated to go left to right.
    @FXML
    Pane aPane, menuPane, menuPane2, gunboat1, destroyer1, carrier1, plane1;
    @FXML
    Button startButton, animateButton1, exitButton, helpButton;
    @FXML
    ToggleButton b3, b4;
    @FXML
    Label l1, l2, sl1, sl2, label1234, ship1, ship2, ship3, ship4;
    @FXML
    Slider slider1, slider2;
    @FXML
    Canvas canvas1;
    @FXML
    ImageView img1;
    @FXML
    ColorPicker colorpick1;
    @FXML
    ListView<String> list1;
    @FXML
    HBox hbox1, shiphbox1, shiphbox2;
    @FXML
    Rectangle placeholder1;

    private Point2D prevMouseCoords = new Point2D(-1, -1);
    private int clickedThing = -1;
    private GraphicsContext gc;
    private final ArrayList<ShipInfo> shipInfos = new ArrayList<>();
    private int groupId = 1;
    private final ArrayList<ShipGroup> groups = new ArrayList<>();
    private final int[] shipCount = new int[]{4, 3, 1, 4};
    private final int[][] grid = new int[10][8];
    private int shipId = 0;

    //My old buddy randInt
    public static int randInt(int min, int max){
        return (int)(Math.random()*(max + 1 - min)) + min;
    }

    /*TODO:
        Make the thing described in the ipad notes
    */

    @FXML
    void initialize(){
        list1.getItems().add("Info List");
        //This runs before the MainMenu class runs, so do what you want here.
        gc = canvas1.getGraphicsContext2D();
        ToggleGroup toggleGroup = new ToggleGroup();
        b3.setToggleGroup(toggleGroup);
        b4.setToggleGroup(toggleGroup);
        aPane.setBackground(new Background(new BackgroundImage(new Image("assets/textures/battleship2.png"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        if(!menuPane.isVisible()) shipSelectScreen();
    }

    public void shipSelectScreen(){
        if(b3.getToggleGroup().getSelectedToggle()!=null)b3.getToggleGroup().getSelectedToggle().setSelected(false);
        aPane.setBackground(Background.EMPTY);
        aPane.setBackground(Background.EMPTY);
        colorpick1.setValue(Color.BLUE);
        placeholder1.setVisible(false);
        double canvasWidth = canvas1.getWidth();
        double canvasHeight = canvas1.getHeight();
        gc.setLineWidth(3);
        gc.strokeLine(0, 0, canvasWidth, 0);
        gc.strokeLine(canvasWidth, 0, canvasWidth, canvasHeight);
        gc.strokeLine(canvasWidth, canvasHeight, 0, canvasHeight);
        gc.strokeLine(0, canvasHeight, 0, 0);
        gc.setLineWidth(1);
        for (int i = 1; i <= 8; i++) {
            //horizontal lines
            gc.strokeText(i+"", i==1?15:4, i*canvasHeight/8.0-35);
            gc.strokeLine(0, i*canvasHeight/8.0, canvasWidth, i*canvasHeight/8.0);
        }
        for (int i = 1; i <= 10; i++) {
            //vertical lines
            gc.strokeText(String.valueOf((char)(i+64)), i*canvasWidth/10.0-45, 15);
            gc.strokeLine(i*canvasWidth/10.0, 0, i*canvasWidth/10.0, canvasHeight);
        }

        //Create the enemy side
        //public ShipGroup(int id, Color associatedColor, Canvas c, ColorPicker cp, Label l){
        Color c = new Color(0.729, 0, 0, 1);
        ShipGroup group = new ShipGroup(0, c, canvas1,null, null);
        groups.add(group);
        for (int i = 1; i <= 3; i++) {
            drawShipImage(1, new Point2D(i, 0), c, 10, false);
            drawShipImage(1, new Point2D(9-i, 0), c, 10, false);
            if(i==2){
                drawShipImage(1, new Point2D(i, 1), c, 10, false);
                drawShipImage(1, new Point2D(9-i, 1), c, 10, false);
                drawShipImage(2, new Point2D(i+3, 0), c, 10, false);
                drawShipImage(2, new Point2D(6-i, 0), c, 10, false);
            }else{
                drawShipImage(2, new Point2D(i+2, 1), c, 10, false);
                drawShipImage(2, new Point2D(7-i, 1), c, 10, false);
            }
        }
        Line l = new Line(250 + canvas1.getLayoutX(), 102 + canvas1.getLayoutY(),
                250 + canvas1.getLayoutX(), 390 + canvas1.getLayoutY());
        l.setStroke(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5));
        l.setStrokeWidth(10);
        ArrayList<Line> lines = new ArrayList<>(Collections.singletonList(l));
        menuPane2.getChildren().add(l);
        group.addInstruction(new Instruction(new Point2D(250, 400), -1), lines);
    }

    @FXML
    private void startBClick(){
        if(carrierId==-1){
            for (int i = 0; i < shipInfos.size(); i++) {
                if(shipInfos.get(i).getShipName().equals("Plane") && i!=groups.get(0).getShips().size()){
                    label1234.setText("In order to deploy planes on the battlefield, you must have a carrier.");
                    return;
                }
            }
        }
        if(shipInfos.size()<=0){
            label1234.setText("You must deploy at least one ship.");
            return;
        }
        System.out.println("Game Started");
        ArrayList<ArrayList<Instruction>> correctInstructions = new ArrayList<>();
        for (int i = 0; i < shipInfos.size(); i++) {
            correctInstructions.add(new ArrayList<>());
        }
        int offset = 0;
        for (ShipGroup group:groups) {
            //Each instruction needs to be ordered such that each ship gets their own list.
            //So correctInstructions is just a very long thing that has an instruction for each ship.
            for (ShipInfo ship:group.getShips()) {
                ship.setPosition(new Point2D(ship.getPosition().getX()-5, ship.getPosition().getY()-5)
                        .multiply(10));
            }
            for (Instruction instruction:group.getInstructions()) {
                //each instruction is not personalized right now, which means that they're just generic move to points
                //this personalizes it
                //instruction.printInstruction();
                Point2D centralShip = null;
                if(instruction.getType()==0) {
                    Point2D fixedTargetPoint = instruction.getTargetPoint().multiply(10);
                    centralShip = group.getShips().get(0).getPosition();
                    double dist = fixedTargetPoint.distance(group.getShips().get(0).getPosition());
                    //find the closest ship to the target and use that as a basis
                    for (ShipInfo ship : group.getShips()) {
                        double temp = fixedTargetPoint.distance(ship.getPosition());
                        if (temp < dist) {
                            dist = temp;
                            centralShip = ship.getPosition();
                        }
                    }
                }
                for (int i = 0; i < group.getShips().size(); i++) {
                    ShipInfo ship = group.getShips().get(i);
                    if(centralShip == null){
                        correctInstructions.get(i).add(instruction);
                        continue;
                    }
                    Point2D newTargetPoint = new Point2D(instruction.getTargetPoint().getX(), instruction.getTargetPoint().getY());
                    newTargetPoint = newTargetPoint.multiply(10);
                    newTargetPoint = new Point2D(newTargetPoint.getX() + ship.getPosition().getX() - centralShip.getX(),
                            newTargetPoint.getY() + ship.getPosition().getY() - centralShip.getY());
                    Instruction newInst = new Instruction(newTargetPoint, ship.getIndex());
                    correctInstructions.get(i + offset).add(newInst);
                }
            }
            offset+=group.getShips().size();
        }
        shipInfos.get(shipInfos.indexOf(groups.get(1).getShips().get(0))).setPlayer();
        Game.receiveInfo(shipInfos, groups.size(), correctInstructions);
    }

    @FXML
    private void b1Click(){
        menuPane.setVisible(false);
        menuPane2.setVisible(true);
        Game.getVolume(slider1.getValue()/100.0, slider2.getValue()/100.0);
        shipSelectScreen();
    }

    private int stage = 0;
    @FXML
    private void getHelp(){
        placeholder1.setVisible(true);
        stage++;
        if(stage==1){
            clearList();
            helpButton.setText("Next Step");
            placeholder1.setVisible(true);
            placeholder1.setStroke(Color.CYAN);
            label1234.setText("First off, please direct your attention to the beautiful display on your left. " +
                    "Through our advanced technology and a few hours of me working on my creative writing skills, " +
                    "we have developed an overhead view of troop deployment.");
        }else if(stage==2){
            label1234.setText("We've gone to the trouble of color-coding these things, so just look at the ships" +
                    " highlighted in red. These are the bad guys. They don't like you and you shouldn't like them." +
                    " Their highlight is what makes them one group.");
        }else if(stage==3){
            label1234.setText("That means that they'll all follow the red-highlighted instructions. The red line on the " +
                    "board right now is an example of an instruction. This means that all of the red ships" +
                    " are going to be going forward like that red line.");
        }else if(stage==4){
            placeholder1.setStroke(Color.BLACK);
            placeholder1.setVisible(false);
            shiphbox1.setStyle("-fx-border-color: cyan");
            shiphbox2.setStyle("-fx-border-color: cyan");
            label1234.setText("They're all just going to be charging forward at you, but you're not helpless " +
                    "either. You can place down ships as well. On the right, you can see the ships you have " +
                    "as well as how many you can place down.");
        }else if(stage==5){
            shiphbox1.setStyle("-fx-border-color: black");
            shiphbox2.setStyle("-fx-border-color: black");
            label1234.setText("When you click on the ship, it should show the information in the list view below. " +
                    "Try placing a ship down and see what happens!");
        }else if(stage==6){
            hbox1.setStyle("-fx-border-color: cyan");
            label1234.setText("As you can see, we've just placed down a ship! Its highlight is blue, which means that " +
                    "it's part of the blue group! You can see the groups that you've created at the bottom of the screen.");
        }else if(stage==7){
            hbox1.setStyle("-fx-border-color: black;");
            b3.setStyle("-fx-border-color: cyan");
            label1234.setText("We can also order the blue group around through instructions. Press the create order button" +
                    " to create an instruction.");
        }else if(stage==8){
            label1234.setText("You can decide the type of order that you want the ship to follow on the list view on your " +
                    "right. A move command allows your ship to move to an area, a wait command for your ships to hold " +
                    "a position, and a turn command to turn -90 degrees.");
        }else if(stage==9){
            label1234.setText("Just place a move command from your ship to somewhere else. Please" +
                    " note that the beginning coordinate doesn't really matter. That's just a guide for you to follow" +
                    " and strategize with. The captains will just go to the end point of the line.");
        }else if(stage==10){
            label1234.setText("Both the turn and wait commands only require 1 click. Try them out!");
        }else if(stage==11){
            b3.setStyle("-fx-border-color: transparent");
            label1234.setText("Now that we have some orders for our first group, let's create a second one! Press the " +
                    "\"Create Group\" button at the bottom of the screen!");
        }else if(stage==12){
            label1234.setText("As you can see, the color of the second group is different from the first one. Change the" +
                    " color of the color picker below this to add orders and ships to that second group, then do the" +
                    " same thing that you did before with the first group.");
        }else if(stage==13){
            b4.setStyle("-fx-border-color: cyan");
            label1234.setText("Now let's try to remove items. Press the remove item button and try clicking on a line" +
                    " or ship on the board.");
        }else if(stage==14){
            b4.setStyle("-fx-border-color: cyan");
            label1234.setText("Unless you clicked on an enemy ship, the line should disappear. If you want to delete" +
                    " a group, just press the \"x\" button by the group that you want to delete.");
        }else if(stage==15){
            clearList();
            helpButton.setText("End Tutorial");
            label1234.setText("Advanced Tips: You'll be controlling the ship in Group 1, so you won't be able to " +
                    "have any other ships in that group. You can only win when you defeat all enemy ships, and " +
                    "you'll lose when your ship sinks. Good luck Admiral!");
        }else if(stage==16){
            clearList();
            helpButton.setText("Tactics Help");
            label1234.setText("Welcome to the planning board, Admiral! The Penguins have officially declared war, " +
                    "and it's our duty to keep them back. Here, you can organize and deploy our ships. Fight on, " +
                    "Admiral, and put a new page into the book of war.");
        }
    }

    @FXML
    private void b3Click(){
        clickedThing = b3.isSelected()?1:-1;
        prevMouseCoords = new Point2D(-1, -1);
        list1.getItems().clear();
        list1.getItems().addAll("Order Type: ", "Move", "Wait 5 seconds", "Turn");
    }

    @FXML
    private void b4Click(){
        clickedThing = b4.isSelected()?0:-1;
        prevMouseCoords = new Point2D(-1, -1);
        clearList();
    }

    @FXML
    private void shipClick(MouseEvent event){
        if(b3.getToggleGroup().getSelectedToggle()!=null)b3.getToggleGroup().getSelectedToggle().setSelected(false);
        int mouseX = (int)event.getSceneX()-550;
        int mouseY = (int)event.getSceneY()-92;
        Point2D square = new Point2D((int)(mouseX/205.0),(int)(mouseY/95.0));
        clickedThing = (int)(square.getX() + square.getY()*2) + 2;
        switch (clickedThing-1){
            case 1->{
                list1.getItems().clear();
                list1.getItems().addAll("Gunboat Info:", "Speed: 20m/sec", "Length: 10m",
                        "Damage: 1 shell/sec", "HP: 3");
            }
            case 2->{
                list1.getItems().clear();
                list1.getItems().addAll("Destroyer Info:", "Speed: 20m/sec", "Length: 30m",
                        "Damage: 3 shell/sec", "HP: 6");
            }
            case 3->{
                list1.getItems().clear();
                list1.getItems().addAll("Carrier Info:", "Speed: 10m/sec", "Length: 45m",
                        "Damage: 5 shell/sec", "HP: 9");
            }
            case 4->{
                list1.getItems().clear();
                list1.getItems().addAll("Plane Info:", "Speed: 50m/sec", "Length: 10m",
                        "Damage: 1 shell/sec", "HP: 1");
            }
        }
        gunboat1.setStyle("-fx-border-color: black");
        destroyer1.setStyle("-fx-border-color: black");
        carrier1.setStyle("-fx-border-color: black");
        plane1.setStyle("-fx-border-color: black");
        Pane p = (Pane) event.getSource();
        p.setStyle("-fx-border-color: aqua");
    }

    private final ArrayList<ArrayList<ArrayList<Line>>> disConnectedLines = new ArrayList<>();
    private final ArrayList<Color> disconnectedLineColors = new ArrayList<>();

    private int carrierId = -1;
    @FXML
    private void canvas1Click(MouseEvent event){
        int mouseX = (int)event.getX();
        int mouseY = (int)event.getY();
        Point2D square = new Point2D((int) (mouseX/(canvas1.getWidth()/10.0)), (int)(mouseY/(canvas1.getHeight()/8.0)));
        Point2D squarePos = new Point2D(square.getX()*canvas1.getWidth()/10  + canvas1.getLayoutX(),
                square.getY()*canvas1.getHeight()/8 + canvas1.getLayoutY());
        ShipGroup group = null;
        for (ShipGroup g:groups) {
            if(g.getAssociatedColor().equals(colorpick1.getValue())){
                group = g;
                break;
            }
        }
        switch (clickedThing) {
            case 1:
                //adds an instruction to the list
                switch (list1.getSelectionModel().getSelectedIndex() - 1) {
                    case (1) -> {
                        Line l1 = createLine(squarePos, new int[]{15, 12, 15, 40});
                        Line l2 = createLine(squarePos, new int[]{35, 12, 35, 40});
                        EventHandler<MouseEvent> z = mouseEvent -> {
                            if (clickedThing == 0) {
                                menuPane2.getChildren().remove(l2);
                                menuPane2.getChildren().remove(l1);
                                removeDisconnectedLine(new Line[]{l1, l2});
                            }
                        };
                        l1.setOnMouseClicked(z);
                        l2.setOnMouseClicked(z);
                        addDisconnectedLine(new ArrayList<>(Arrays.asList(l1, l2)), group,
                                new Instruction(Duration.seconds(5.0), -1));
                        menuPane2.getChildren().addAll(l1, l2);
                        prevMouseCoords = new Point2D(-1, -1);
                        return;
                    }
                    case (2) -> {
                        Line l1 = createLine(squarePos, new int[]{15, 15, 40, 15});
                        Line l2 = createLine(squarePos, new int[]{40, 25, 40, 25});
                        Line l3 = createLine(squarePos, new int[]{15, 35, 40, 35});
                        Line l4 = createLine(squarePos, new int[]{10, 15, 20, 10});
                        Line l5 = createLine(squarePos, new int[]{10, 15, 20, 20});
                        EventHandler<MouseEvent> z = mouseEvent -> {
                            if (clickedThing == 0) {
                                menuPane2.getChildren().remove(l5);
                                menuPane2.getChildren().remove(l4);
                                menuPane2.getChildren().remove(l3);
                                menuPane2.getChildren().remove(l2);
                                menuPane2.getChildren().remove(l1);
                                removeDisconnectedLine(new Line[]{l1, l2, l3, l4, l5});
                            }
                        };
                        l1.setOnMouseClicked(z);
                        l2.setOnMouseClicked(z);
                        l3.setOnMouseClicked(z);
                        l4.setOnMouseClicked(z);
                        l5.setOnMouseClicked(z);
                        addDisconnectedLine(new ArrayList<>(Arrays.asList(l1, l2, l3, l4, l5)), group,
                                new Instruction(-90, -1));
                        menuPane2.getChildren().addAll(l1, l2, l3, l4, l5);
                        prevMouseCoords = new Point2D(-1, -1);
                        return;
                    }
                    default -> {
                        if (!prevMouseCoords.equals(new Point2D(-1, -1))) {
                            Line l = new Line(prevMouseCoords.getX() + canvas1.getLayoutX(), prevMouseCoords.getY() + canvas1.getLayoutY(),
                                    mouseX + canvas1.getLayoutX(), mouseY + canvas1.getLayoutY());
                            l.setStroke(new Color(colorpick1.getValue().getRed(), colorpick1.getValue().getGreen(), colorpick1.getValue().getBlue(), 0.5));
                            l.setStrokeWidth(10);
                            l.setOnMouseClicked(mouseEvent -> {
                                if (clickedThing == 0) {
                                    menuPane2.getChildren().remove(l);
                                    removeDisconnectedLine(new Line[]{l});
                                }
                            });
                            addDisconnectedLine(new ArrayList<>(Collections.singletonList(l)), group,
                                    new Instruction(mouseX, mouseY, -1));
                            menuPane2.getChildren().add(l);
                            prevMouseCoords = new Point2D(-1, -1);
                            return;
                        }
                    }
                }
                break;
            case 0:
                clearList();
                int index = grid[(int) square.getX()][(int) square.getY()];
                if(index != 0 && index <= 4){
                    gc.setFill(Color.WHITE);
                    gc.fillRect(squarePos.getX()-canvas1.getLayoutX()+1, squarePos.getY()-canvas1.getLayoutY()+1, 48, 48);
                    gc.setFill(Color.BLACK);
                    shipCount[index - 1]++;
                    for (ShipGroup g:groups) {
                        shipInfos.remove(g.removeShip(new Point2D(square.getX() * (canvas1.getWidth() / 10.0) + 5,
                                        square.getY() * (canvas1.getHeight() / 8.0) + 5)));
                    }
                    if(index==4)carrierId=-1;
                    updateShipLabel(square, index);
                }
                prevMouseCoords = new Point2D(-1, -1);
                return;
            case 2:
                clearList();
                if(grid[(int) square.getX()][(int) square.getY()]==0 && shipCount[0] > 0) {
                    if(checkFull(colorpick1.getValue())){
                        gunboat1.setStyle("-fx-border-color: black");
                        prevMouseCoords = new Point2D(-1, -1);
                        return;
                    }
                    shipCount[0]--;
                    ship1.setText("x" + shipCount[0]);
                    drawShipImage(1, square, colorpick1.getValue(), 0, true);
                    clickedThing = -1;
                }
                gunboat1.setStyle("-fx-border-color: black");
                prevMouseCoords = new Point2D(-1, -1);
                return;
            case 3:
                clearList();
                if(grid[(int) square.getX()][(int) square.getY()]==0 && shipCount[1] > 0) {
                    if(checkFull(colorpick1.getValue())){
                        destroyer1.setStyle("-fx-border-color: black");
                        prevMouseCoords = new Point2D(-1, -1);
                        return;
                    }
                    shipCount[1]--;
                    ship2.setText("x" + shipCount[1]);
                    drawShipImage(2, square, colorpick1.getValue(), 0, true);
                    clickedThing = -1;
                }
                destroyer1.setStyle("-fx-border-color: black");
                prevMouseCoords = new Point2D(-1, -1);
                return;
            case 4:
                clearList();
                if(grid[(int) square.getX()][(int) square.getY()]==0 && shipCount[2] > 0) {
                    if(checkFull(colorpick1.getValue())){
                        carrier1.setStyle("-fx-border-color: black");
                        prevMouseCoords = new Point2D(-1, -1);
                        return;
                    }
                    shipCount[2]--;
                    ship3.setText("x" + shipCount[2]);
                    carrierId = shipId;
                    drawShipImage(3, square, colorpick1.getValue(), 0, true);
                    clickedThing = -1;
                }
                carrier1.setStyle("-fx-border-color: black");
                prevMouseCoords = new Point2D(-1, -1);
                return;
            case 5:
                clearList();
                if(grid[(int) square.getX()][(int) square.getY()]==0 && shipCount[3] > 0) {
                    if(checkFull(colorpick1.getValue())){
                        plane1.setStyle("-fx-border-color: black");
                        prevMouseCoords = new Point2D(-1, -1);
                        return;
                    }
                    shipCount[3]--;
                    ship4.setText("x" + shipCount[3]);
                    drawShipImage(4, square, colorpick1.getValue(), 0, true);
                    clickedThing = -1;
                }
                plane1.setStyle("-fx-border-color: black");
                prevMouseCoords = new Point2D(-1, -1);
                return;
        }
        prevMouseCoords = new Point2D(mouseX, mouseY);
    }

    private void clearList(){
        if(!b3.isSelected()) {
            list1.getItems().clear();
            list1.getItems().add("Info List");
        }
    }

    private void updateShipLabel(Point2D square, int index) {
        Label l = switch (index){
            case 1 -> ship1;
            case 2 -> ship2;
            case 3 -> ship3;
            case 4 -> ship4;
            default -> null;
        };
        assert l != null;
        l.setText("x" + shipCount[index-1]);
        grid[(int) square.getX()][(int) square.getY()] = 0;
    }

    private final ArrayList<ArrayList<Instruction>> disconnectedInstructions = new ArrayList<>();
    public void addDisconnectedLine(ArrayList<Line> lineArray, ShipGroup group, Instruction i){
        if(group==null){
            int disconnectedLineIndex = disConnectedLines.size();
            if(!disconnectedLineColors.contains(colorpick1.getValue())){
                disConnectedLines.add(new ArrayList<>());
                disconnectedInstructions.add(new ArrayList<>());
                disconnectedLineColors.add(colorpick1.getValue());
            }else{
                disconnectedLineIndex = disconnectedLineColors.indexOf(colorpick1.getValue());
            }
            disconnectedInstructions.get(disconnectedLineIndex).add(i);
            disConnectedLines.get(disconnectedLineIndex).add(lineArray);
        }else{
            group.addInstruction(i, lineArray);
        }
    }

    public void removeDisconnectedLine(Line[] lines){
        for (int i = 0; i<disConnectedLines.size();i++) {
            ArrayList<ArrayList<Line>> disConnectedLineArray = disConnectedLines.get(i);
            for (int j = 0; j < disConnectedLineArray.size(); j++) {
                for (Line l:lines) {
                    if (disConnectedLineArray.get(j).contains(l)) {
                        disconnectedInstructions.remove(j);
                        disConnectedLineArray.remove(j);
                        break;
                    }
                }
            }
            if(disConnectedLineArray.size()==0){
                int disConnectedLineIndex = disConnectedLines.indexOf(disConnectedLineArray);
                disconnectedInstructions.remove(disConnectedLineIndex);
                disConnectedLines.remove(disConnectedLineIndex);
                disconnectedLineColors.remove(disConnectedLineIndex);
            }
        }
    }

    private boolean checkFull(Color c){
        for (ShipGroup g:groups) {
            if(g.getAssociatedColor().equals(c)) {
                if (g.getId() == 1 && g.getShips().size() >= 1) {
                    label1234.setText("You will be piloting the ship in Group 1, so you are only allowed to have 1 ship " +
                            "in that group. Although you can add instructions to that ship, they won't have any effect " +
                            "and as such it's not really advisable.");
                    return true;
                }
            }
        }
        return false;
    }

    private void drawShipImage(int shipNum, Point2D square, Color c, int bias, boolean side){
        Point2D squarePos = new Point2D(square.getX()*canvas1.getWidth()/10  + canvas1.getLayoutX(),
                square.getY()*canvas1.getHeight()/8 + canvas1.getLayoutY());
        grid[(int) square.getX()][(int) square.getY()] = shipNum + bias;
        gc.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5));
        gc.fillRect(squarePos.getX()-canvas1.getLayoutX()+2, squarePos.getY()-canvas1.getLayoutY()+2, 46, 46);
        gc.setFill(Color.BLACK);
        gc.drawImage(new Image("assets/textures/ShipIcon" + shipNum + ".png"), square.getX() * (canvas1.getWidth() / 10.0) + 5,
                square.getY() * (canvas1.getHeight() / 8.0) + 5, 40, 40);
        String name = switch(shipNum){
            case 1->"Gunboat";
            case 2->"Destroyer";
            case 3->"Carrier";
            case 4->"Plane";
            default -> throw new IllegalStateException("Unexpected value: " + shipNum);
        };
        ShipInfo ship = new ShipInfo(name, new Point2D(square.getX() * (canvas1.getWidth() / 10.0) + 5,
                square.getY() * (canvas1.getHeight() / 8.0) + 5),
                shipId, groupId, side, shipNum==4?carrierId:-1);
        shipId++;
        for (ShipGroup g:groups) {
            if(g.getAssociatedColor().equals(c)){
                ship.setGroupNum(g.getId());
                shipInfos.add(ship);
                g.addShip(ship);
                return;
            }
        }
        stupidColor = c;
        colorpick1.setValue(c);
        addGroup();
        groups.get(groups.size()-1).setAssociatedColor(c);
        groups.get(groups.size()-1).addShip(ship);
        shipInfos.add(ship);
    }

    //I think there should be some type of variable that's not really global but I can still access it outside of functions
    //It would make me feel better about having 50 different instance fields that I use maybe twice.
    private Color stupidColor = null;

    @FXML
    private void addGroup(){
        StackPane p = new StackPane();
        p.setStyle("-fx-border-color: black");
        p.setPrefWidth(200.0);
        VBox vbox = new VBox();
        Button b = new Button("x");
        b.setLayoutX(177);
        b.setStyle("-fx-background-color: transparent");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setLayoutY(30);
        colorPicker.setLayoutX(5);
        Color[] colors = new Color[]{Color.CYAN, Color.TEAL, Color.BLUE, Color.NAVY, Color.MAGENTA, Color.PURPLE,
                Color.RED, Color.MAROON, Color.YELLOW, Color.RED, Color.GREEN, Color.LIME};
        if(stupidColor==null) {
            ArrayList<Color> usedColors = new ArrayList<>();
            for (ShipGroup g : groups) {
                usedColors.add(g.getAssociatedColor());
            }
            colorPicker.setValue(colors[randInt(0, colors.length-1)]);
            while(usedColors.contains(colorPicker.getValue())) colorPicker.setValue(colors[randInt(0, colors.length-1)]);
            colorpick1.setValue(colorPicker.getValue());
        }else{
            colorPicker.setValue(stupidColor);
            stupidColor = null;
        }
        Label l = new Label("Group " + groupId);
        l.setFont(new Font(15));
        l.setLayoutY(5);
        l.setLayoutX(5);
        Label l2 = new Label("");
        l.setFont(new Font(15));
        l.setLayoutX(5);
        l2.setWrapText(true);
        ShipGroup g = new ShipGroup(groupId, colorPicker.getValue(), canvas1, colorPicker, l2);
        groups.add(g);
        b.setOnMouseClicked(mouseEvent -> {
            for (ShipInfo s:g.getShips()) {
                Point2D square = new Point2D((int) (s.getPosition().getX()/(canvas1.getWidth()/10.0)),
                (int)(s.getPosition().getY()/(canvas1.getHeight()/8.0)));
                Point2D squarePos = new Point2D(square.getX()*canvas1.getWidth()/10  + canvas1.getLayoutX(),
                        square.getY()*canvas1.getHeight()/8 + canvas1.getLayoutY());
                int index = grid[(int) square.getX()][(int) square.getY()];
                gc.setFill(Color.WHITE);
                gc.fillRect(squarePos.getX()-canvas1.getLayoutX()+1, squarePos.getY()-canvas1.getLayoutY()+1, 48, 48);
                gc.setFill(Color.BLACK);
                shipCount[index - 1]++;
                updateShipLabel(square, index);
            }
            for (ArrayList<Line> lines:g.getInstructionLines()) {
                for (Line line:lines) {
                    menuPane2.getChildren().remove(line);
                }
            }
            groups.remove(g);
            hbox1.getChildren().remove(p);
        });
        HBox hbox2 = new HBox();
        Region r = new Region();
        //https://stackoverflow.com/questions/19325351/how-to-set-hgrow-property-dynamically
        HBox.setHgrow(r, Priority.ALWAYS);
        b.setAlignment(Pos.CENTER_RIGHT);
        hbox2.getChildren().addAll(l, r, b);
        vbox.getChildren().addAll(hbox2, colorPicker, l2);
        p.getChildren().add(vbox);
        hbox1.getChildren().add(p);
        int index = -1;
        for (int i = 0; i < disconnectedLineColors.size(); i++) {
            if(disconnectedLineColors.get(i).equals(colorPicker.getValue())){
                index = i;
                break;
            }
        }
        if(index!=-1){
            for (int i = 0; i<disconnectedInstructions.get(index).size(); i++) {
                g.addInstruction(disconnectedInstructions.get(index).get(i), disConnectedLines.get(index).get(i));
            }
        }
        if(groupId==1){
            ArrayList<Color> usedColors = new ArrayList<>();
            for (ShipGroup g2 : groups) {
                usedColors.add(g2.getAssociatedColor());
            }
            colorpick1.setValue(colors[randInt(0, colors.length-1)]);
            while(usedColors.contains(colorpick1.getValue())) colorpick1.setValue(colors[randInt(0, colors.length-1)]);
        }
        groupId++;
    }

    public Line createLine(Point2D squarePos, int[] offset){
        Line l5 = new Line(squarePos.getX() + offset[0],
                squarePos.getY() + offset[1],
                squarePos.getX() + offset[2],
                squarePos.getY() + offset[3]);
        l5.setStroke(new Color(colorpick1.getValue().getRed(), colorpick1.getValue().getGreen(), colorpick1.getValue().getBlue(), 0.5));
        l5.setStrokeWidth(10);
        return l5;
    }

    //I feel like I've been creating a lot more classes for a lot more stupid reasons now that I can make them easily.
    private static class ShipGroup{
        private int id;
        private final ArrayList<Instruction> instructions = new ArrayList<>();
        private final ArrayList<ShipInfo> ships = new ArrayList<>();
        private Color associatedColor;
        private final Canvas canvas1;
        private final ColorPicker cp;
        private Label label = null;
        private final ArrayList<ArrayList<Line>> instructionLines = new ArrayList<>();
        public ShipGroup(int id, Color associatedColor, Canvas c, ColorPicker cp, Label l){
            this.id = id;
            this.associatedColor = associatedColor;
            this.canvas1 = c;
            this.cp = cp;
            if(cp!=null) cp.setOnAction(event -> setAssociatedColor(cp.getValue()));
            if(l!=null)this.label = l;
        }

        public void addShip(ShipInfo s){
            ships.add(s);
            updateCounter();
        }
        public ShipInfo removeShip(Point2D s){
            ShipInfo output = null;
            for (int i = 0; i < ships.size(); i++) {
                if(ships.get(i).getPosition().equals(s)){
                    output = ships.get(i);
                    ships.remove(i);
                    break;
                }
            }
            updateCounter();
            return output;
        }
        public void updateCounter(){
            int[] shipCounter = new int[]{0,0,0,0};
            for (ShipInfo si:ships) {
                switch (si.getShipName()){
                    case "Gunboat" -> shipCounter[0]++;
                    case "Destroyer" -> shipCounter[1]++;
                    case "Carrier" -> shipCounter[2]++;
                    case "Plane" -> shipCounter[3]++;
                }
            }
            if(label==null)return;
            label.setText((shipCounter[0]!=0?(shipCounter[0] + " gunboats "):"") +
                    (shipCounter[1]!=0?(shipCounter[1] + " destroyers "):"") +
                    (shipCounter[2]!=0?(shipCounter[2] + " carriers "):"") +
                    (shipCounter[3]!=0?(shipCounter[3] + " planes"):""));
        }
        public void addInstruction(Instruction i, ArrayList<Line> lines){
            instructions.add(i);
            instructionLines.add(lines);
        }
        public ArrayList<Instruction> getInstructions(){
            return this.instructions;
        }
        public ArrayList<ShipInfo> getShips() {
            return ships;
        }
        public ArrayList<ArrayList<Line>> getInstructionLines() {
            return instructionLines;
        }
        public void setAssociatedColor(Color associatedColor) {
            this.associatedColor = associatedColor;
            if(cp==null) return;
            cp.setValue(associatedColor);
            for (ShipInfo s:ships) {
                Point2D square = new Point2D((int) (s.getPosition().getX()/(canvas1.getWidth()/10.0)),
                        (int)(s.getPosition().getY()/(canvas1.getHeight()/8.0)));
                Point2D squarePos = new Point2D(square.getX()*canvas1.getWidth()/10  + canvas1.getLayoutX(),
                        square.getY()*canvas1.getHeight()/8 + canvas1.getLayoutY());
                GraphicsContext gc = canvas1.getGraphicsContext2D();
                gc.setFill(Color.WHITE);
                gc.fillRect(squarePos.getX()-canvas1.getLayoutX()+1, squarePos.getY()-canvas1.getLayoutY()+1, 48, 48);
                gc.setFill(new Color(associatedColor.getRed(), associatedColor.getGreen(),
                        associatedColor.getBlue(), 0.5));
                gc.fillRect(squarePos.getX()-canvas1.getLayoutX()+2, squarePos.getY()-canvas1.getLayoutY()+2, 46, 46);
                gc.setFill(Color.BLACK);
                int shipNum = switch (s.getShipName()){
                    case("Gunboat")->1;
                    case("Destroyer")->2;
                    case("Carrier")->3;
                    case("Plane")->4;
                    //IntelliJ autofilled this and I think it's reasonable
                    default -> throw new IllegalStateException("Unexpected value: " + s.getShipName());
                };
                gc.drawImage(new Image("assets/textures/ShipIcon" + shipNum + ".png"), square.getX() * (canvas1.getWidth() / 10.0) + 5,
                        square.getY() * (canvas1.getHeight() / 8.0) + 5, 40, 40);
            }
            for (ArrayList<Line> lines:instructionLines) {
                for (Line l:lines){
                    l.setStroke(new Color(associatedColor.getRed(), associatedColor.getGreen(),
                            associatedColor.getBlue(), 0.5));
                }
            }
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public Color getAssociatedColor() {
            return associatedColor;
        }
    }
}
