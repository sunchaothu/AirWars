package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Random;

import element.EnemyPlane;
import element.OurPlane;
import element.Bullet;

public class GamePanel extends JPanel implements Runnable,KeyListener{
    // Screen Size
    private int screenWidth = 320;
    private int screenHeight = 480;
    // Backgroud
    private Image backgroudImage1 = null;
    private Image backgroudImage2 = null;
    private int backgroudPosY1 = 0;
    private int backgroudPosY2 = 0;

    // Player
    private int playerPosX1;
    private int playerPosY1;
    private OurPlane player1;

    private int playerPosX2;
    private int playerPosY2;
    private OurPlane player2;
    private int planStep = 10;
    // Enenmy
        // Enemy Plane 1, ordinary
    private int enemyCount1 = 4;
    private EnemyPlane enemyPlane1[] = null;
    private int flightID1 = 0;

    // Enemy Plane 2, more life point
    private int enemyCount2 = 4;
    private EnemyPlane enemyPlane2[] = null;
    private int flightID2 = 0;

    // Enemy Plane 3, with attack
    private EnemyPlane enemyPlane3[] = null;
    private int enemyCount3 = 4;
    private int flightID3 = 0;

    // Bullet
    private int bulletCount = 15;
    private Bullet bullet[] = null;
    private long shootTime = 0L;
    private int sendID = 0;

    // Bullet2
    private  int bulletCount2 = 15;
    private Bullet bullet2[] = null;
    private int sendID2 = 0;
    private final int bulletUpOffset = 10;
    private final int bulletLeftOffset = 10;


    // Thread
    private Thread thread = null;
    private boolean isRunning = false;
    private ConnectThread connectThread = null;

    // sever and socket
    //socket
    //socket
    private Socket socket;
    private BufferedReader is;
    private PrintWriter os;



    //  score
    private int score1 = 0;
    private int score2 = 0;
    private int HighScore = 0;


    // Game_State
    private enum GameState{GAME_START,GAME_END,GAME_PASUE};
    private GameState gameState = GameState.GAME_END;

    public GamePanel(String ip,int port,JFrame jFrame){
        setPreferredSize(new Dimension(screenWidth,screenHeight));
        setFocusable(true);
        addKeyListener(this);
        init();
        myEvent(jFrame);
        listenServer(ip,port);

        isRunning = true;
        thread = new Thread(this);
     // start game
        thread.start();
        setVisible(true);
    }

    public void init(){
        try{
            URL url = this.getClass().getResource("/image/bg01.jpg");
            backgroudImage1 = Toolkit.getDefaultToolkit().getImage(url);
            backgroudImage2 = Toolkit.getDefaultToolkit().getImage(url);
        }catch (Exception e){
            e.printStackTrace();
        }

        backgroudPosY1 = 0;
        backgroudPosY2 = -screenHeight;
        // player
        playerPosX1 = 180;
        playerPosY1 = 400;
        player1 = new OurPlane();
        player1.initLocation(playerPosX1,playerPosY1);

        player2 = new OurPlane();
        playerPosX2 = 120;
        playerPosY2 = 400;
        player2.initLocation(playerPosX2,playerPosY2);
        // enemy
        enemyPlane1 = new EnemyPlane[enemyCount1];
        for (int i = 0; i < enemyCount1; i++){
            enemyPlane1[i] = new EnemyPlane(1);
        }

        enemyPlane2 = new EnemyPlane[enemyCount2];
        for(int i = 0; i < enemyCount2; i++){
            enemyPlane2[i] = new EnemyPlane(2);
        }

        enemyPlane3 = new EnemyPlane[enemyCount3];
        for(int i = 0; i < enemyCount3; i++){
            enemyPlane3[i] = new EnemyPlane(3);
        }

        bullet = new Bullet[bulletCount];
        for(int i =0; i < bulletCount;i++){
            bullet[i] = new Bullet();
        }

        bullet2 = new Bullet[bulletCount2];
        for(int i =0; i < bulletCount2;i++){
            bullet2[i] = new Bullet();
        }
    }

    public void run(){
        while (isRunning){
            draw();
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }


    public void draw(){
        renderBg();
        updateBg();
    }

    private void renderBg(){
        repaint();
    }

    public void paint(Graphics g){

        if(gameState == GameState.GAME_START) {
            g.drawImage(backgroudImage1, 0, backgroudPosY1, screenWidth, screenHeight, this);
            g.drawImage(backgroudImage2, 0, backgroudPosY2, screenWidth, screenHeight, this);
            if (player1.lifePoint > 0) {
                player1.drawAlive(g, this);
            }
            if (player2.lifePoint > 0) {
                player2.drawAlive(g, this);
            }
            for (int i = 0; i < bulletCount; i++) {
                bullet[i].drawBullet(g, this);
            }

            for (int i = 0; i < bulletCount2; i++) {
                bullet2[i].drawBullet(g, this);
            }

            for (int i = 0; i < enemyCount1; i++) {
                enemyPlane1[i].drawPlane(g, this);

            }

            for (int i = 0; i < enemyCount2; i++) {
                enemyPlane2[i].drawPlane(g, this);

            }

            for (int i = 0; i < enemyCount3; i++) {
                enemyPlane3[i].drawPlane(g, this);
            }

            g.setFont(new Font("Times New Roman", 0, 25));
            g.setColor(Color.PINK);
            g.drawString("2UP", 10, 30);
            g.drawString(String.valueOf(score2), 10, 50);


            g.drawString("HIGHSCORE", 80, 30);
            g.drawString(String.valueOf(HighScore), 120, 50);

            g.drawString("1UP", 250, 30);
            g.drawString(String.valueOf(score1), 250, 50);
        }
    }

    public void updateBg(){
        backgroudPosY1 += 2;
        backgroudPosY2 += 2;

        if(backgroudPosY1 == screenHeight){
            backgroudPosY1 = -screenHeight;
        }

        if(backgroudPosY2 == screenHeight){
            backgroudPosY2 = -screenHeight;
        }

        for (int i=0; i < bulletCount;i++){
            bullet[i].updateBullet();
        }

        for (int i=0; i < bulletCount2;i++){
            bullet2[i].updateBullet();
        }


        // TODO: collision
//        collsion();
    }

    public void judgeEdge(EnemyPlane enemyPlane){
        if(enemyPlane.posY > screenHeight){
            enemyPlane.setDeath();
        }
    }
    public void updateEnemy(){
        for(int i = 0; i < enemyCount1;i++){
            enemyPlane1[i].updateLocation();
         //   System.out.print(enemyPlane1[i].posY);
            judgeEdge(enemyPlane1[i]);

        }

       // System.out.println(" ");
        for(int i = 0; i < enemyCount2;i++){
            enemyPlane2[i].updateLocation();
       //     System.out.print(enemyPlane2[i].posY);
            judgeEdge(enemyPlane2[i]);
        }

       // System.out.println(" ");
        for(int i = 0; i < enemyCount3;i++){
            enemyPlane3[i].updateLocation();
       //     System.out.print(enemyPlane3[i].posY);
            judgeEdge(enemyPlane3[i]);
        }
       // System.out.println(" ");
        int type = getRandom(0,30);
        int x = getRandom(0,260);
        int y = getRandom(0,30);

        if(type == 0 ){
            if(flightID1 < enemyCount1&&enemyPlane1[flightID1].isDeath()) {
                enemyPlane1[flightID1].setAlive();
                enemyPlane1[flightID1].initLocation(x,-y);
                flightID1++;
            }

            if(flightID1>=enemyCount1)
                flightID1 = 0;
        }
        if(type == 1 ){
            if(flightID2 < enemyCount2&&enemyPlane2[flightID2].isDeath()) {
                enemyPlane2[flightID2].setAlive();
                enemyPlane2[flightID2].initLocation(x,-y);
                flightID2++;
            }
            if(flightID2>=enemyCount2)
                flightID2 = 0;
        }

        if(type == 2 ){
            if(flightID3 < enemyCount3&&enemyPlane3[flightID3].isDeath()) {
                enemyPlane3[flightID3].setAlive();
                enemyPlane3[flightID3].initLocation(x,-y);
                flightID3++;
            }
            if(flightID3>=enemyCount3)
                flightID3 = 0;
        }

    }


    public int getRandom(int lower,int upper){
        return((Math.abs(new Random().nextInt()))%(upper-lower)+lower);
    }

    public void shoot2(){
        if(sendID2 < bulletCount2){
            bullet2[sendID2].setToDraw(true);
            int x = playerPosX2+ OurPlane.planeWidth/2-5;
            int y = playerPosY2-bulletUpOffset;
            bullet2[sendID2].initLocation(x,y);
            String msg = "Bullet2|"+sendID2+"|"+x+"|"+y;
            sendMessage(msg);
            sendID2++;
        }
        if(sendID2 == bulletCount2){
            sendID2 = 0;
        }
    }


    // Not use
    public void collsion(){
        for(int i = 0; i < bulletCount; i++){
            for(int j = 0;j < enemyCount1;j++){
                if(enemyPlane1[j].isAlive()&&bullet[i].posX>enemyPlane1[j].posX
                        && bullet[i].posX<enemyPlane1[j].posX+20
                        && bullet[i].posY>enemyPlane1[j].posY
                        && bullet[i].posY<enemyPlane1[j].posY+40
                        ){
                    enemyPlane1[j].lifePointMinus();
                    bullet[i].setToDraw(false);
                }
            }

            for(int j = 0;j < enemyCount2;j++){
                if(enemyPlane2[j].isAlive()&&bullet[i].posX>enemyPlane2[j].posX
                        && bullet[i].posX<enemyPlane2[j].posX+20
                        && bullet[i].posY>enemyPlane2[j].posY
                        && bullet[i].posY<enemyPlane2[j].posY+40){
                    enemyPlane2[j].lifePointMinus();
                    bullet[i].initLocation(-50,-50); // double kill
                    bullet[i].setToDraw(false);
                }
            }

            for(int j = 0;j < enemyCount1;j++){
                if(enemyPlane3[j].isAlive()&&bullet[i].posX>enemyPlane3[j].posX
                        && bullet[i].posX<enemyPlane3[j].posX+20
                        && bullet[i].posY>enemyPlane3[j].posY
                        && bullet[i].posY<enemyPlane3[j].posY+40
                        ){
                    enemyPlane3[j].lifePointMinus();
                    bullet[i].setToDraw(false);
                }
            }
        }


    }
    public void myEvent(JFrame j){
        j.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try{
                    if(os!=null){
                        os.println("Close");
                        os.close();
                    }
                    if(is!=null)
                        is.close();
                    socket.close();
                    System.exit(0);
                }catch (IOException excp){
                    excp.printStackTrace();
                }

            }
        });
    }

    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        switch (key){
            case KeyEvent.VK_W:{
                playerPosY2 -= planStep;
                break;
            }
            case KeyEvent.VK_S:{
                playerPosY2 += planStep;
                break;
            }
            case KeyEvent.VK_A:{
                playerPosX2 -= planStep;
                break;
            }
            case KeyEvent.VK_D:{
                playerPosX2 += planStep;
                break;
            }
            case KeyEvent.VK_K:{
                if(player2.isAlive())
                shoot2();
            }
            default:break;
        }
        if(playerPosX2>screenWidth - OurPlane.planeWidth)
            playerPosX2 = screenWidth - OurPlane.planeWidth;
        if(playerPosX2 < 0){
            playerPosX2 = 0;
        }

        if(playerPosY2 < 0){
            playerPosY2 = 0;
        }

        if(playerPosY2 > screenHeight - OurPlane.planeHeight){
            playerPosY2 =screenHeight - OurPlane.planeHeight ;
        }
        player2.updateLocation(playerPosX2,playerPosY2);
        String msg = "Player2|"+playerPosX2+"|"+playerPosY2;
        sendMessage(msg);
    }
    public void sendMessage(String msg){
        os.println(msg);
    }
    public void keyReleased(KeyEvent e){}

    public void keyTyped(KeyEvent e){

    }

    private void listenServer(String ip, int port){
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        System.out.println("Connect port "+ip+":"+port);
                        socket = new Socket(ip,port);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    System.out.println("Accept Connection!");
                    gameState = GameState.GAME_START;
                    connectThread = new ConnectThread();
                    connectThread.start();
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private class ConnectThread extends Thread{
        public void run(){
            try{
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                os = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
                while (true){
                    try{

                       String s1 = is.readLine();

                        if(s1 == null){
                            continue;
                        }
                        System.out.println(s1);
                        if(s1.startsWith("Close")){
                            socket.close();
                            is.close();
                            os.close();
                            // Todo:
                            System.exit(0); // ？
                        }
                        if(s1.startsWith("Player1")){
                            String ss[] = new String[3];
                            ss = s1.split("\\|");
                            int x = Integer.parseInt(ss[1]);
                            int y = Integer.parseInt(ss[2]);
                            player1.updateLocation(x,y);
                        }

                        if(s1.startsWith("Enemy1")){
                            String ss[] = new  String[5];
                            ss = s1.split("\\|");
                            int index = Integer.parseInt(ss[1]);
                            int isAliveOrHit = Integer.parseInt(ss[2]);
                            int x = Integer.parseInt(ss[3]);
                            int y = Integer.parseInt(ss[4]);
                            enemyPlane1[index].setProperty(isAliveOrHit,x,y);
                        }

                        if(s1.startsWith("Enemy2")){
                            String ss[] = new  String[5];
                            ss = s1.split("\\|");
                            int index = Integer.parseInt(ss[1]);
                            int isAliveOrHit = Integer.parseInt(ss[2]);
                            int x = Integer.parseInt(ss[3]);
                            int y = Integer.parseInt(ss[4]);
                            enemyPlane2[index].setProperty(isAliveOrHit,x,y);
                        }

                        if(s1.startsWith("Enemy3")){
                            String ss[] = new  String[5];
                            ss = s1.split("\\|");
                            int index = Integer.parseInt(ss[1]);
                            int isAliveOrHit = Integer.parseInt(ss[2]);
                            int x = Integer.parseInt(ss[3]);
                            int y = Integer.parseInt(ss[4]);
                            enemyPlane3[index].setProperty(isAliveOrHit,x,y);
                        }

                        if(s1.startsWith("Bullet1")){
                            String ss[] = new  String[4];
                            ss = s1.split("\\|");
                            int index = Integer.parseInt(ss[1]);
                            int x = Integer.parseInt(ss[2]);
                            int y = Integer.parseInt(ss[3]);

                            sendID = index;
                            bullet[index].setToDraw(true);
                            bullet[index].initLocation(x,y);
                        }

                        if(s1.startsWith("HitBullet1")){
                            String ss[] = new String[2];
                            ss = s1.split("\\|");
                            sendID = Integer.parseInt(ss[1]);
                            bullet[sendID].setToDraw(false);
                        }

                        if(s1.startsWith("Attacked")){
                            String ss[] = new String[3];
                            ss = s1.split("\\|");
                            int num = Integer.parseInt(ss[1]);
                            int life = Integer.parseInt(ss[2]);
                            if(num == 1){
                                player1.setlifePoint(life);
                            }else {
                                player2.setlifePoint(life);

                            }
                        }

                        if(s1.startsWith("Score")){
                            String ss[] = new String[3];
                            ss = s1.split("\\|");
                            score1 = Integer.parseInt(ss[1]);
                            score2 = Integer.parseInt(ss[2]);
                        }

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }



}
