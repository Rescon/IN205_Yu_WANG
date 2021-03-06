package ensta;

import ensta.Board.*;
import ensta.Ship.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Game {

    /* ***
     * Constante
     */
    private static final int size_grille = 10;
    private static String filename = "savegame.dat";

    /* ***
     * Attributs
     */
    private static Player player1;
    private static Player player2;
    private static ArrayList<Integer> coords_player1;
    private static ArrayList<Integer> coords_player2;
    private static boolean two_player;
    private static boolean player1_attaque;

    private final Scanner sin = new Scanner(System.in);

    /* ***
     * Constructeurs
     */
    public Game() {}

    public static void intervalle(){
        for(int i = 0;i < 30;++i){
            System.out.println();
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Game init() {
        if (!loadSave()) {
            Scanner sin = new Scanner(System.in);
            String[] validOrientations = {"Y", "N"}; // Yes and No
            boolean done = false;
            two_player = false;
            System.out.println("Voulez-vous entrer dans le mode deux joueurs? Veuillez entrer Y ou N：");
            do {
                try {
                    String in = sin.nextLine();
                    if (Arrays.asList(validOrientations).contains(in)) {
                        done = true;
                        if(in.equals("Y")){
                            two_player = true;
                        }
                    }
                } catch (Exception e) {
                    // nop
                }

                if (!done) {
                    System.err.println("Format incorrect! Veuillez entrer Y ou N：");
                }
            } while (!done);

            coords_player1 = new ArrayList<>();
            coords_player2 = new ArrayList<>();
            player1_attaque = true;

            if(two_player){
                // init attributes
                System.out.println("entre le nom de joueur 1:");
                String username1 = sin.nextLine();
                System.out.println("entre le nom de joueur 2:");
                String username2 = sin.nextLine();

                List<AbstractShip> player1Ships = createDefaultShips();
                List<AbstractShip> player2Ships = createDefaultShips();

                Board b1 = new Board(username1,size_grille);
                Board b2 = new Board(username2,size_grille);
                player1 = new Player(b1,b2,player1Ships);
                player2 = new Player(b2,b1,player2Ships);

                System.out.println("Maintenant, le joueur 1 place le navire:");
                b1.print();

                // place player ships
                try {
                    player1.putShips();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                intervalle();

                System.out.println("Maintenant, le joueur 2 place le navire:");
                b2.print();

                // place player ships
                try {
                    player2.putShips();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                // init attributes
                System.out.println("entre ton nom:");
                String username = sin.nextLine();
                System.out.println("Votre nom est : " + username);

                List<AbstractShip> player1Ships = createDefaultShips();
                List<AbstractShip> player2Ships = createDefaultShips();
                Board b1 = new Board(username,size_grille);
                Board b2 = new Board("AI board",size_grille);
                player1 = new Player(b1,b2,player1Ships);
                player2 = new AIPlayer(b2,b1,player2Ships);

                b1.print();

                // place player ships
                try {
                    player1.putShips();
                    player2.putShips();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else {
            System.out.println("Vous avez rechargé votre progression de jeu précédente.");
        }
        return this;
    }

    /* ***
     * Méthodes
     */
    public void run() {
        if(two_player){
            Board b1 = player1.board;
            Board b2 = player2.board;
            Hit hit;
            int[] coords = new int[2];

            // main loop
            boolean done = false;
            boolean strike;
            do {
                if(!done && player1_attaque){
                    System.out.println("Maintenant, Au tour du joueur 1 d'attaquer:");
                    sleep(3000);
                    intervalle();
                    sleep(3000);
                    System.out.println("Bonjour, joueur 1, c'est maintenant à vous d'attaquer:");
                    b1.print();
                    do{
                        hit = player1.sendHit(coords_player1);
                        coords[0] = coords_player1.get(coords_player1.size() - 1)%size_grille;
                        coords[1] = coords_player1.get(coords_player1.size() - 1)/size_grille;
                        strike = hit != Hit.MISS;
                        b1.setHit(strike, coords[1], coords[0]);
                        b1.print();

                        done = updateScore();
                        System.out.println(makeHitMessage(false /* outgoing hit */, coords, hit));

                        if (!done) {
                            save();
                        }
                    } while(strike && !done);
                    player1_attaque = false;
                    save();
                }

                if (!done && !player1_attaque) {
                    System.out.println("Maintenant, Au tour du joueur 2 d'attaquer:");
                    sleep(3000);
                    intervalle();
                    sleep(3000);
                    System.out.println("Bonjour, joueur 2, c'est maintenant à vous d'attaquer:");
                    b2.print();
                    do {
                        hit = player2.sendHit(coords_player2);
                        coords[0] = coords_player2.get(coords_player2.size() - 1)%size_grille;
                        coords[1] = coords_player2.get(coords_player2.size() - 1)/size_grille;
                        strike = hit != Hit.MISS;
                        b2.setHit(strike, coords[1], coords[0]);
                        b2.print();

                        done = updateScore();
                        System.out.println(makeHitMessage(false /* incoming hit */, coords, hit));

                        if (!done) {
                            save();
                        }
                    } while(strike && !done);
                    player1_attaque = true;
                    save();
                }
            } while (!done);
        }else{
            Board b1 = player1.board;
            Hit hit;
            int[] coords = new int[2];

            // main loop
            b1.print();
            boolean done;
            boolean strike;
            do {
                hit = player1.sendHit(coords_player1);
                coords[0] = coords_player1.get(coords_player1.size() - 1)%size_grille;
                coords[1] = coords_player1.get(coords_player1.size() - 1)/size_grille;
                strike = hit != Hit.MISS;
                b1.setHit(strike, coords[1], coords[0]);

                done = updateScore();
                b1.print();
                System.out.println(makeHitMessage(false /* outgoing hit */, coords, hit));

                if (!done && !strike) {
                    do {
                        hit = player2.sendHit(coords_player2);
                        coords[0] = coords_player2.get(coords_player2.size() - 1)%size_grille;
                        coords[1] = coords_player2.get(coords_player2.size() - 1)/size_grille;
                        strike = hit != Hit.MISS;
                        if (strike) {
                            b1.print();
                        }
                        System.out.println(makeHitMessage(true /* incoming hit */, coords, hit));
                        done = updateScore();

                        if (!done) {
                            save();
                        }
                    } while(strike && !done);
                }

            } while (!done);
        }

        File file = new File(filename);
        if(file.exists()){
            file.delete();
        }
        System.out.println(String.format("joueur %d gagne", player1.lose ? 2 : 1));
        sin.close();
    }

    private void save() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(filename));
            objectOutputStream.writeObject(player1);
            objectOutputStream.writeObject(player2);
            objectOutputStream.writeObject(coords_player1);
            objectOutputStream.writeObject(coords_player2);
            objectOutputStream.writeObject(two_player);
            objectOutputStream.writeObject(player1_attaque);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadSave() {
        File file = new File(filename);
        if (file.exists()) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filename));
                try {
                    player1 = (Player)objectInputStream.readObject();
                    player2 = (Player)objectInputStream.readObject();
                    coords_player1 = (ArrayList<Integer>)objectInputStream.readObject();
                    coords_player2 = (ArrayList<Integer>)objectInputStream.readObject();
                    two_player = (boolean)objectInputStream.readObject();
                    player1_attaque = (boolean)objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private boolean updateScore() {
        for (Player player : new Player[]{player1, player2}) {
            int destroyed = 0;
            for (AbstractShip ship : player.getShips()) {
                if (ship.isSunk()) {
                    destroyed++;
                }
            }

            player.destroyedCount = destroyed;
            player.lose = destroyed == player.getShips().length;
            if (player.lose) {
                return true;
            }
        }
        return false;
    }

    private String makeHitMessage(boolean incoming, int[] coords, Hit hit) {
        String msg;
        ColorUtil.Color color = ColorUtil.Color.RESET;
        switch (hit) {
            case MISS:
                msg = hit.toString();
                break;
            case STRIKE:
                msg = hit.toString();
                color = ColorUtil.Color.RED;
                break;
            default:
                msg = hit.toString() + " coulé";
                color = ColorUtil.Color.RED;
        }
        msg = String.format("%s Frappe en %c%d : %s", incoming ? "<=" : "=>",
                ((char) ('A' + coords[0])),
                (coords[1] + 1), msg);
        return ColorUtil.colorize(msg, color);
    }

    private static List<AbstractShip> createDefaultShips() {
        return Arrays.asList(new AbstractShip[]{new Destroyer(), new Submarine(), new Submarine(), new BattleShip(), new Carrier()});
    }

    public static void main(String args[]) {
        new Game().init().run();
    }
}
