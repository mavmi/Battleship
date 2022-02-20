package com.company;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class BadInputException extends Exception {
    BadInputException(String msg){
        super(msg);
    }
}

class AlreadyDestroyedShip extends Exception {
    AlreadyDestroyedShip(){
        super("Ship is destroyed!");
    }
}

class Cells {
    final static public char EMPTY = ' ';
    final static public char FOG = '~';
    final static public char SHIP = 'O';
    final static public char HIT = 'X';
    final static public char MISS = 'M';
}

class Point {
    public int line;
    public int column;

    public Point(int line, int column){
        this.line = line;
        this.column = column;
    }

    public Point(Point another){
        this.line = another.line;
        this.column = another.column;
    }

    @Override
    public boolean equals(final Object another){
        if (this == another){
            return true;
        }
        if (this.getClass() != another.getClass()){
            return false;
        }
        Point another_point = (Point)another;
        return line == another_point.line && column == another_point.column;
    }

    @Override
    public int hashCode(){
        return line * 17 + column * 19 + 123;
    }
}

class Ship {
    final private boolean isVertical;
    final private int length;
    final private Point begin;
    final private Point end;

    private int hit_points;

    Ship(Point first, Point second) throws BadInputException{
        if (first.column == second.column){
            isVertical = false;
        } else if (first.line == second.line){
            isVertical = true;
        } else {
            throw new BadInputException("Wrong ship location!");
        }

        if (first.line < second.line || first.column < second.column){
            begin = first;
            end = second;
        } else {
            begin = second;
            end = first;
        }

        hit_points = length = ((begin.line == end.line) ?
                (end.column - begin.column) : (end.line - begin.line)) + 1;
    }

    void getDamage() throws AlreadyDestroyedShip {
        if (hit_points > 0) {
            hit_points--;
        }
        if (hit_points == 0){
            throw new AlreadyDestroyedShip();
        }
    }

    boolean isVertical(){
        return isVertical;
    }

    int getLength(){
        return length;
    }

    Point getBegin(){
        return new Point(begin);
    }

    Point getEnd(){
        return new Point(end);
    }
}

class Battleship {
    private final int WIDTH = 10;

    private Map<Point, Ship> POINT_TO_MAP;
    private int SHIPS_COUNT;
    private char[][] FIELD;

    public Battleship(){
        intiField();
        setShips();
    }

    public void printField(boolean fog_of_war){
        System.out.print(Cells.EMPTY);
        for (int line = 1; line <= WIDTH; line++){
            System.out.print(' ');
            System.out.print(line);
        }
        System.out.print('\n');

        for (int line = 0; line < WIDTH; line++){
            System.out.print(getLineChar(line));
            for (int column = 0; column < WIDTH; ++column){
                System.out.print(' ');
                if (fog_of_war && FIELD[line][column] == Cells.SHIP){
                    System.out.print(Cells.FOG);
                } else {
                    System.out.print(FIELD[line][column]);
                }
            }
            System.out.print('\n');
        }
    }

    public void takeAShot(){
        Scanner scanner = new Scanner(System.in);

        try {
            String msg;
            Point point = parsePoint(scanner.nextLine());
            if (FIELD[point.line][point.column] == Cells.MISS ||
                    FIELD[point.line][point.column] == Cells.HIT){
                msg = "Bad location!";
            } else if (FIELD[point.line][point.column] == Cells.SHIP){
                FIELD[point.line][point.column] = Cells.HIT;
                try {
                    POINT_TO_MAP.get(point).getDamage();
                    msg = "You hit a ship!";
                } catch (AlreadyDestroyedShip e){
                    SHIPS_COUNT--;
                    msg = "You sank a ship! Specify a new target:";
                }
            } else {
                FIELD[point.line][point.column] = Cells.MISS;
                msg = "You missed!";
            }
            printField(true);
            System.out.println(msg);
            if (SHIPS_COUNT == 0){
                System.out.println("You sank the last ship. You won. Congratulations!");
                System.exit(0);
            }
        } catch (BadInputException e) {
            printErrorMsg(e.getMessage());
        }
    }

    private void setShips(){
        int[] lengths = new int[]{5, 4, 3, 3, 2};
        String[] ships = new String[]{
                "Aircraft Carrier",
                "Battleship",
                "Submarine",
                "Cruiser",
                "Destroyer"
        };
        SHIPS_COUNT = lengths.length;

        printField(false);
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < 5;){
            System.out.print("Enter the coordinates of the ");
            System.out.print(ships[i]);
            System.out.print(" (");
            System.out.print(lengths[i]);
            System.out.println(" cells):");

            try {
                Ship ship = parseShip(scanner.nextLine());
                if (ship.getLength() != lengths[i]) {
                    throw new BadInputException("Wrong length of the " + ships[i] + "!");
                }
                checkPosition(ship);
                addShip(ship);
                printField(false);
                i++;
            } catch (Exception e) {
                printErrorMsg(e.getMessage());
            }
        }
    }

    private void intiField(){
        POINT_TO_MAP = new HashMap<>();
        FIELD = new char[WIDTH][WIDTH];

        int line, column;
        for (line = 0; line < WIDTH; line++){
            for (column = 0; column < WIDTH; column++){
                FIELD[line][column] = Cells.FOG;
            }
        }
    }

    private char getLineChar(int line){
        return (char)('A' + line);
    }

    private Point parsePoint(String input) throws BadInputException {
        String exc_msg = "You entered the wrong coordinates!";

        if (input.length() < 2){
            throw new BadInputException(exc_msg);
        }
        if (input.charAt(0) < 'A' || input.charAt(0) > 'A' + WIDTH){
            throw new BadInputException(exc_msg);
        }
        for (int i = 1; i < input.length(); i++){
            if (input.charAt(i) < '0' || input.charAt(i) > '9'){
                throw new BadInputException(exc_msg);
            }
        }

        int column = Integer.parseInt(input.substring(1)) - 1;
        if (column < 0 || column >= WIDTH){
            throw new BadInputException(exc_msg);
        }

        return new Point((int)(input.charAt(0) - 'A'), column);
    }

    private Ship parseShip(String input) throws BadInputException {
        String[] coords = input.split(" ");
        return new Ship(parsePoint(coords[0]), parsePoint(coords[1]));
    }

    private void checkPosition(Ship ship) throws BadInputException {
        Point begin = ship.getBegin();

        for (int I = -1; I < ship.getLength() + 1; I++){
            for (int j = -1; j <= 1; j++){
                int line = (ship.isVertical()) ? begin.line + j : begin.line + I;
                int column = (!ship.isVertical()) ? begin.column + j : begin.column + I;

                if (line < 0 || line >= WIDTH ||
                        column < 0 || column >= WIDTH){
                    continue;
                }

                if (FIELD[line][column] == Cells.SHIP){
                    throw new BadInputException("You placed it too close to another one.");
                }
            }
        }
    }

    private void addShip(Ship ship){
        Point begin = ship.getBegin();

        for (int i = 0; i < ship.getLength(); i++){
            FIELD[begin.line][begin.column] = Cells.SHIP;
            POINT_TO_MAP.put(new Point(begin), ship);
            if (ship.isVertical()){
                begin.column++;
            } else {
                begin.line++;
            }
        }
    }

    private void printErrorMsg(String msg){
        System.out.print("Error! ");
        System.out.print(msg);
        System.out.println(" Try again:");
    }
}

public class Main {
    public static void main(String[] args) {
        Battleship player_1;
        Battleship player_2;

        String name_1;
        String name_2;

        {
            name_1 = "Player 1";
            name_2 = "Player 2";
            String place_ships_msg = "place your ships on the game field";

            printPlayerMsg(name_1, place_ships_msg);
            player_1 = new Battleship();
            printPassMoveMsg();

            printPlayerMsg(name_2, place_ships_msg);
            player_2 = new Battleship();
            printPassMoveMsg();
        }

        {
            String player_name;
            Battleship player_field;
            Battleship enemy_field;
            boolean is_first = true;

            while (true){
                player_name = (is_first) ? name_1 : name_2;
                player_field = (is_first) ? player_1 : player_2;
                enemy_field = (is_first) ? player_2 : player_1;

                enemy_field.printField(true);
                System.out.println("---------------------");
                player_field.printField(false);

                printPlayerMsg(player_name, "it's your turn:");
                enemy_field.takeAShot();
                printPassMoveMsg();

                is_first = !is_first;
            }
        }
    }

    private static void printPlayerMsg(String player, String msg){
        System.out.print(player);
        System.out.print(", ");
        System.out.println(msg);
    }

    private static void printPassMoveMsg(){
        System.out.println("Press Enter and pass the move to another player");
        System.out.println("...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
