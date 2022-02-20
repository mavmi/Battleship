package com.company;

import java.util.Scanner;
import java.util.stream.IntStream;

class BadInputException extends Exception {
    BadInputException(String msg){
        super(msg);
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
}

class Ship {
    final private boolean isVertical;
    final private int length;
    final private Point begin;
    final private Point end;

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

        length = ((begin.line == end.line) ?
                (end.column - begin.column) : (end.line - begin.line)) + 1;
    }

    boolean isVertical(){
        return isVertical;
    }

    int getLength(){
        return length;
    }

    Point getBegin(){
        return new Point(begin.line, begin.column);
    }

    Point getEnd(){
        return new Point(end.line, end.column);
    }
}

class Battleship {
    private final int WIDTH = 10;
    private int HIT_POINTS = 0;
    private char[][] FIELD;

    public Battleship(){
        intiField();
        printField();
        setShips();
        startGame();
    }

    private void startGame(){
        System.out.println("The game starts!");

        takeAShot();
    }

    private void takeAShot(){
        System.out.println("Take a shot!");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                Point point = parsePoint(scanner.nextLine());
                if (FIELD[point.line][point.column] == Cells.SHIP){
                    FIELD[point.line][point.column] = Cells.HIT;
                    HIT_POINTS--;
                    System.out.println("You hit a ship!");
                } else {
                    FIELD[point.line][point.column] = Cells.MISS;
                    System.out.println("You missed!");
                }
                printField();
                break;
            } catch (BadInputException e) {
                printErrorMsg(e.getMessage());
            }
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
        HIT_POINTS = IntStream.of(lengths).sum();

        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < 5;){
            System.out.print("Enter the coordinates of the ");
            System.out.print(ships[i]);
            System.out.print(" (");
            System.out.print(lengths[i]);
            System.out.println(" cells):");

            try{
                Ship ship = parseShip(scanner.nextLine());
                if (ship.getLength() != lengths[i]){
                    throw new BadInputException("Wrong length of the " + ships[i] + "!");
                }
                checkPosition(ship);
                addShip(ship);
                printField();
                i++;
            } catch (Exception e){
                printErrorMsg(e.getMessage());
            }
        }
    }

    private void intiField(){
        FIELD = new char[WIDTH][WIDTH];

        int line, column;
        for (line = 0; line < WIDTH; line++){
            for (column = 0; column < WIDTH; column++){
                FIELD[line][column] = Cells.FOG;
            }
        }
    }

    private void printField(){
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
                System.out.print(FIELD[line][column]);
            }
            System.out.print('\n');
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
	    Battleship battleship = new Battleship();
    }
}
