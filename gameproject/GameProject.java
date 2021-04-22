package gameproject;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class GameProject {
    public static StartFrame startFrame;
    
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            Font TEXT=Font.createFont(Font.TRUETYPE_FONT,new File(((new Integer(0)).getClass().getResource("/resources/Font.ttf")).toURI()));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(TEXT);
        }
        catch(Exception e){}
        GameProject.startFrame=new StartFrame();
        GameProject.startFrame.setVisible(true);
    }
}

interface GC {
    public static int WIDTH=10;
    public static int PWIDTH=80;
    public static int PHEIGHT=60;
}

enum Direction {UP,DOWN,RIGHT,LEFT}

class Fighter {
    private Image ship=(new ImageIcon(getClass().getResource("/resources/Fighter.png"))).getImage();
    public final ArrayList<Missile> Mi=new ArrayList<>(0);
    public static final int width=GC.WIDTH*10;
    public static final int height=GC.WIDTH*17;
    private int x=((GC.WIDTH*GC.PWIDTH)-width)/2;
    private int y=(GC.WIDTH*GC.PHEIGHT)-(height+GC.WIDTH/2);
    private boolean dead=false;
    
    public void draw(Graphics g){
        for(int i=0;i<Mi.size();i++)
            Mi.get(i).draw(g);
        if(ship!=null)
            g.drawImage(ship,x,y,null);
    }
    
    public void move(Direction dir){
        int nx,ny;
        switch(dir){
            case UP:
                ny=y-(GC.WIDTH/2);
                if(ny>GC.WIDTH/2)
                    y=ny;
                break;
            case DOWN:
                ny=y+(GC.WIDTH/2);
                if(ny<(GC.WIDTH*GC.PHEIGHT)-(height+GC.WIDTH/2))
                    y=ny;
                break;
            case RIGHT:
                nx=x+(GC.WIDTH/2);
                if(nx<(GC.WIDTH*GC.PWIDTH)-(width+GC.WIDTH/2))
                    x=nx;
                break;
            case LEFT:
                nx=x-(GC.WIDTH/2);
                if(nx>GC.WIDTH/2)
                    x=nx;
                break;
        }
    }
    
    public Point[] getCollisionPoints(){
        Point[] ps=new Point[3];
        ps[0]=new Point(this.x+Fighter.width/2,this.y+GC.WIDTH*2);
        ps[1]=new Point(this.x+GC.WIDTH,this.y+GC.WIDTH*11);
        ps[2]=new Point(this.x+Fighter.width-GC.WIDTH,this.y+GC.WIDTH*11);
        return ps;
    }
    
    public void fire(GamePanel pnl){
        Missile current=new Missile(this.x+(Fighter.width-Missile.width)/2,this.y+55,pnl);
        Mi.add(current);
    }
    
    public boolean hasCollisionWith(Point p){
        boolean check1=(p.x>=this.x+GC.WIDTH*4&&p.x<=this.x+GC.WIDTH*6)&&(p.y>=this.y+GC.WIDTH&&p.y<=this.y+Fighter.height);
        boolean check2=(p.x>=this.x+GC.WIDTH&&p.x<=this.x+Fighter.width-GC.WIDTH)&&(p.y>=this.y+GC.WIDTH*10&&p.y<=this.y+Fighter.height);
        return (check1||check2);
    }
    
    public synchronized void die(GamePanel pnl){
        if(StartFrame.gameFrame!=null){
            this.ship=null;
            pnl.destroyed.add(new Explosion(this.x,this.y,pnl));
            pnl.banner=new Banner(pnl,6);
            pnl.repaint();
            pnl.hold();
            String player=JOptionPane.showInputDialog(pnl,"Type your name","Leaderboards",JOptionPane.QUESTION_MESSAGE);
            if(player!=null&&(!player.isEmpty())){
                Saver sv=new Saver();
                sv.writeRecord(new Record(player,pnl.getScore(),pnl.getLevel()));
            }
            Grenade.hitFlag=false;
            StartFrame.gameFrame.setVisible(false);
            StartFrame.gameFrame=null;
            System.gc();
            GameProject.startFrame.setVisible(true);
        }
    }
}

class Invader {
    private final Image enemy=(new ImageIcon(getClass().getResource("/resources/Invader.png"))).getImage();
    public final ArrayList<Grenade> Gr=new ArrayList<>(0);
    public static final int width=GC.WIDTH*10;
    public static final int height=GC.WIDTH*11;
    private int x;
    private int y;
    
    public Invader(int x,int y){
        this.x=x;
        this.y=y;
    }
    
    public int getX(){return this.x;}
    public int getY(){return this.y;}
    
    public void draw(Graphics g){
        for(int i=0;i<Gr.size();i++)
            Gr.get(i).draw(g);
        g.drawImage(enemy,x,y,null);
    }
    
    public void move(Direction dir){
        switch(dir){
            case UP:y-=(GC.WIDTH/2); break;
            case DOWN:y+=(GC.WIDTH/2); break;
            case RIGHT:x+=(GC.WIDTH/2); break;
            case LEFT:x-=(GC.WIDTH/2); break;
        }
    }
    
    public boolean hasCollisionWith(Point[] ps){
        for(int i=0;i<ps.length;i++){
            boolean check1=(ps[i].x>=this.x&&ps[i].x<=this.x+Invader.width)&&(ps[i].y>=this.y+GC.WIDTH*2&&ps[i].y<=this.y+GC.WIDTH*4);
            boolean check2=(ps[i].x>=this.x+GC.WIDTH*2&&ps[i].x<=this.x+GC.WIDTH*8)&&(ps[i].y>=this.y+GC.WIDTH*2&&ps[i].y<=this.y+Invader.height-GC.WIDTH*2);
            if(check1||check2)
                return true;
        }
        return false;
    }
    
    public boolean hasCollisionWith(Point p){
        boolean check1=(p.x>=this.x&&p.x<=this.x+Invader.width)&&(p.y>=this.y+GC.WIDTH*2&&p.y<=this.y+GC.WIDTH*4);
        boolean check2=(p.x>=this.x+GC.WIDTH*2&&p.x<=this.x+GC.WIDTH*8)&&(p.y>=this.y+GC.WIDTH*2&&p.y<=this.y+Invader.height-GC.WIDTH*2);
        return (check1||check2);
    }
    
    public void drop(GamePanel pnl){
        Grenade current=new Grenade(this.x+(Invader.width-Grenade.width)/2,this.y+Invader.height-Grenade.width,pnl,this);
        Gr.add(current);
    }
}

class Fleet {
    public final ArrayList<Invader> fleet=new ArrayList<>(14);
    
    public Fleet(){
        this.fill();
    }
    
    public void fill(){
        for(int i=0;i<2;i++)
            for(int j=0;j<7;j++)
                fleet.add(new Invader((int)((j+1)*GC.WIDTH*1.3)+(j*Invader.width),((i+1)*(GC.WIDTH/2))+(i*Invader.height)-2*(GC.WIDTH/2+Invader.height)));
    }
    
    public void draw(Graphics g){
        for(int i=0;i<fleet.size();i++)
            fleet.get(i).draw(g);
    }
    
    public boolean hasCollisionWith(Point[] ps){
        for(int i=0;i<fleet.size();i++)
            if(fleet.get(i).hasCollisionWith(ps))
                return true;
        return false;
    }
    
    public int hasCollisionWith(Point p){
        for(int i=0;i<fleet.size();i++)
            if(fleet.get(i).hasCollisionWith(p))
                return i;
        return -1;
    }
}

class Monster {
    private final Image monster=(new ImageIcon(getClass().getResource("/resources/Monster.png"))).getImage();
    public Rocket Ro;
    public static final int width=GC.WIDTH*30;
    public static final int height=GC.WIDTH*30;
    public final GamePanel pnl;
    private int x=(GC.WIDTH*GC.PWIDTH-Monster.width)/2;
    private int y=-Monster.height;
    private Direction dir=Direction.DOWN;
    private Timer MonsterMotion;
    private Timer RocketLauncher;
    private int monsterMotionCount=0;
    private int health=20;
    
    public Monster(GamePanel pnl){
        this.pnl=pnl;
        this.init();
    }
    
    private void init(){
        RocketLauncher=new Timer(1000,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                Ro=new Rocket(x+(Monster.width-Rocket.width)/2,y+Monster.width-Rocket.width,pnl);
            }
        });
        MonsterMotion=new Timer(10,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                move();
                pnl.repaint();
                monsterMotionCount++;
                if(monsterMotionCount==62){
                    dir=Direction.RIGHT;
                    RocketLauncher.start();
                }
                boolean check1=pnl.Fi.hasCollisionWith(new Point(x,y));
                boolean check2=pnl.Fi.hasCollisionWith(new Point(x,y+Monster.height));
                boolean check3=pnl.Fi.hasCollisionWith(new Point(x+Monster.width,y));
                boolean check4=pnl.Fi.hasCollisionWith(new Point(x+Monster.width,y+Monster.height));
                boolean check5=pnl.Fi.hasCollisionWith(new Point(x,y+Monster.height/2));
                boolean check6=pnl.Fi.hasCollisionWith(new Point(x+Monster.width,y+Monster.height/2));
                boolean check7=pnl.Fi.hasCollisionWith(new Point(x+GC.WIDTH*10,y+Monster.height));
                boolean check8=pnl.Fi.hasCollisionWith(new Point(x+GC.WIDTH*20,y+Monster.height));
                if(check1||check2||check3||check4||check5||check6||check7||check8){
                    RocketLauncher.stop();
                    pnl.Fi.die(pnl);
                }
            }
        });
        MonsterMotion.start();
    }
    
    public void draw(Graphics g){
        if(Ro!=null)
            Ro.draw(g);
        g.drawImage(monster,x,y,null);
        g.setColor(Color.RED);
        for(int i=0;i<health;i++){
            int w=GC.WIDTH*2*health;
            g.fillRect((GC.WIDTH*GC.PWIDTH-w)/2+GC.WIDTH*2*i,5,GC.WIDTH*2,GC.WIDTH/2);
        }
    }
    
    public void move(){
        switch(dir){
            case UP: y-=GC.WIDTH/2; break;
            case DOWN: y+=GC.WIDTH/2; break;
            case RIGHT:
                if(x+GC.WIDTH/2+Monster.width>=((GC.WIDTH*GC.PWIDTH)-GC.WIDTH/2))
                    dir=Direction.LEFT;
                else
                    x+=GC.WIDTH/2;
                break;
            case LEFT:
                if(x-GC.WIDTH/2<=GC.WIDTH/2)
                    dir=Direction.RIGHT;
                else
                    x-=GC.WIDTH/2;
                break;
        }
    }
    
    public void hold(){
        RocketLauncher.stop();
        MonsterMotion.stop();
    }
    
    public void destroy(){
        health--;
        if(health==0){
            if(Ro!=null)
                pnl.droppedRo=Ro;
            pnl.Mo=null;
            this.hold();
            pnl.destroyed.add(new Explosion(x+(Monster.width-GC.WIDTH*10)/2,y+(Monster.height-GC.WIDTH*10)/2,pnl));
            pnl.banner=new Banner(pnl,7);
            pnl.repaint();
            pnl.setFocusable(false);
            pnl.incScore(100000);
            pnl.setStats();
            String player=JOptionPane.showInputDialog(pnl,"Type your name","Leaderboards",JOptionPane.QUESTION_MESSAGE);
            if(player!=null&&player!=""){
                Saver sv=new Saver();
                sv.writeRecord(new Record(player,pnl.getScore(),pnl.getLevel()));
            }
            Grenade.hitFlag=false;
            StartFrame.gameFrame.setVisible(false);
            StartFrame.gameFrame=null;
            System.gc();
            GameProject.startFrame.setVisible(true);
        }
        else{
            pnl.destroyed.add(new Explosion(x+(Monster.width-GC.WIDTH*10)/2,y+(Monster.height-GC.WIDTH*10)/2,pnl));
            pnl.repaint();
            pnl.incScore(5000);
            pnl.setStats();
        }
    }
    
    public boolean hasCollisionWith(Point p){
        boolean check=(p.x>=this.x&&p.x<=this.x+Monster.width)&&(p.y<=this.y+Monster.height-GC.WIDTH&&p.y>=this.y);
        return check;
    }
}

class Missile implements ActionListener {
    private final Image missile=(new ImageIcon(getClass().getResource("/resources/Missile.png"))).getImage();
    public static final int width=GC.WIDTH*3;
    private final GamePanel pnl;
    private int x;
    private int y;
    private final Timer MissileMotion;
    
    public Missile(int x,int y,GamePanel pnl){
        this.x=x;
        this.y=y;
        this.pnl=pnl;
        MissileMotion=new Timer(5,this);
        MissileMotion.start();
    }
    
    public void draw(Graphics g){
        g.drawImage(missile,x,y,null);
    }
    
    public void move(){
        y-=GC.WIDTH/2;
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        pnl.repaint();
        move();
        int index=pnl.Fl.hasCollisionWith(new Point(x+Missile.width/2,y+GC.WIDTH));
        if(index!=-1){
            pnl.destroy(index);
            pnl.Fi.Mi.remove(this);
            MissileMotion.stop();
        }
        else if(pnl.Mo!=null){
            boolean check=pnl.Mo.hasCollisionWith(new Point(x+Missile.width/2,y+GC.WIDTH));
            if(check){
                pnl.Mo.destroy();
                pnl.Fi.Mi.remove(this);
                MissileMotion.stop();
            }
        }
        else if(y<-55){
            pnl.Fi.Mi.remove(this);
            MissileMotion.stop();
            pnl.incScore(-250);
            pnl.setStats();
        }
    }
}

class Grenade implements ActionListener {
    private final Image grenade=(new ImageIcon(getClass().getResource("/resources/Grenade.png"))).getImage();
    public static final int width=GC.WIDTH*3;
    private final GamePanel pnl;
    private final Invader dropper;
    private int x;
    private int y;
    private final Timer GrenadeMotion;
    public static boolean hitFlag=false;
    
    public Grenade(int x,int y,GamePanel pnl,Invader dropper){
        this.x=x;
        this.y=y;
        this.pnl=pnl;
        this.dropper=dropper;
        GrenadeMotion=new Timer(5,this);
        GrenadeMotion.start();
    }
    
    public void draw(Graphics g){
        g.drawImage(grenade,x,y,null);
    }
    
    public void move(){
        y+=GC.WIDTH/2;
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        pnl.repaint();
        this.move();
        if((pnl.Fi.hasCollisionWith(new Point(this.x+Grenade.width/2,this.y+Grenade.width/2)))&&(!hitFlag)){
            Grenade.hitFlag=true;
            dropper.Gr.remove(this);
            pnl.dropped.remove(this);
            GrenadeMotion.stop();
            pnl.Fi.die(pnl);
        }
        else if(this.y>GC.WIDTH*GC.PHEIGHT){
            dropper.Gr.remove(this);
            pnl.dropped.remove(this);
            GrenadeMotion.stop();
        }
    }
}

class Rocket implements ActionListener {
    private final Image rocket=(new ImageIcon(getClass().getResource("/resources/Rocket.png"))).getImage();
    private final Image sideLeft=(new ImageIcon(getClass().getResource("/resources/SideL.png"))).getImage();
    private final Image sideRight=(new ImageIcon(getClass().getResource("/resources/SideR.png"))).getImage();
    public static final int width=GC.WIDTH*5;
    private int x1,x2,x3;
    private int y1,y2,y3;
    private final GamePanel pnl;
    private final Timer RocketMotion;
    
    public Rocket(int x,int y,GamePanel pnl){
        this.x2=x;
        this.y2=y;
        this.pnl=pnl;
        this.x1=this.x2-Rocket.width-GC.WIDTH;
        this.y1=y;
        this.x3=this.x2+Rocket.width+GC.WIDTH;
        this.y3=y;
        RocketMotion=new Timer(5,this);
        RocketMotion.start();
    }
    
    public void draw(Graphics g){
        g.drawImage(sideLeft,x1,y1,null);
        g.drawImage(rocket,x2,y2,null);
        g.drawImage(sideRight,x3,y3,null);
    }
    
    public void move(){
        x1-=GC.WIDTH/2;
        y1+=GC.WIDTH/2;
        y2+=GC.WIDTH/2;
        x3+=GC.WIDTH/2;
        y3+=GC.WIDTH/2;
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        pnl.repaint();
        this.move();
        boolean check1=pnl.Fi.hasCollisionWith(new Point(x1+Rocket.width/2,y1+Rocket.width/2));
        boolean check2=pnl.Fi.hasCollisionWith(new Point(x2+Rocket.width/2,y2+Rocket.width/2));
        boolean check3=pnl.Fi.hasCollisionWith(new Point(x3+Rocket.width/2,y3+Rocket.width/2));
        if(check1||check2||check3){
            pnl.Mo.Ro=null;
            RocketMotion.stop();
            pnl.Mo.hold();
            pnl.Fi.die(pnl);
        }
        if(y2>=GC.WIDTH*GC.PHEIGHT)
            RocketMotion.stop();
    }
}

class Explosion {
    private final Image explosion=(new ImageIcon(getClass().getResource("/resources/FighterExplosion.gif"))).getImage();
    private final int x;
    private final int y;
    private final Timer ExplosionEnder;
    private int explosionEnderCount=0;
    private final GamePanel pnl;
    
    public Explosion(int x, int y,GamePanel p){
        this.x=x;
        this.y=y;
        this.pnl=p;
        ExplosionEnder=new Timer(100,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                explosionEnderCount++;
                if(explosionEnderCount==2){
                    pnl.destroyed.remove(0);
                    pnl.repaint();
                    ExplosionEnder.stop();
                }
            }
        });
        ExplosionEnder.start();
    }
    
    public void draw(Graphics g){
        g.drawImage(explosion,x,y,pnl);
    }
}

class Banner {
    private final Image level1=(new ImageIcon(getClass().getResource("/resources/Level1.png"))).getImage();
    private final Image level2=(new ImageIcon(getClass().getResource("/resources/Level2.png"))).getImage();
    private final Image level3=(new ImageIcon(getClass().getResource("/resources/Level3.png"))).getImage();
    private final Image level4=(new ImageIcon(getClass().getResource("/resources/Level4.png"))).getImage();
    private final Image level5=(new ImageIcon(getClass().getResource("/resources/Level5.png"))).getImage();
    private final Image failed=(new ImageIcon(getClass().getResource("/resources/Failed.png"))).getImage();
    private final Image accomplished=(new ImageIcon(getClass().getResource("/resources/Accomplished.png"))).getImage();
    private final Image choice;
    private final GamePanel pnl;
    private final Timer BannerFader;
    private int bannerFaderCount=0;
    
    public Banner(GamePanel p,int c){
        this.pnl=p;
        switch(c){
            case 1: this.choice=level1; break;
            case 2: this.choice=level2; break;
            case 3: this.choice=level3; break;
            case 4: this.choice=level4; break;
            case 5: this.choice=level5; break;
            case 6: this.choice=failed; break;
            default: this.choice=accomplished; break;
        }
        BannerFader=new Timer(500,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                bannerFaderCount++;
                if(bannerFaderCount==2){
                    pnl.banner=null;
                    BannerFader.stop();
                    pnl.repaint();
                }
            }
        });
        BannerFader.start();
    }
    
    public void draw(Graphics g){
        g.drawImage(choice,0,0,null);
    }
}

class Record {
    private final String playername;
    private final int playerscore;
    private final int playerlevel;
    
    public Record(String pn,int ps,int pl){
        this.playername=pn;
        this.playerscore=ps;
        this.playerlevel=pl;
    }
    
    @Override
    public String toString(){
        return "     "+this.playername+"\t\t"+this.playerscore+"\t"+this.playerlevel;
    }
}

class Saver {
    private File leaderboards;
    private FileWriter fw;
    private PrintWriter pw;
    private FileReader fr;
    private BufferedReader br;
    
    public Saver(){
        try{
            leaderboards=new File(getClass().getResource("/resources/Leaderboards.txt").toURI());
            fw=new FileWriter(leaderboards,true);
            pw=new PrintWriter(fw,true);
            fr=new FileReader(leaderboards);
            br=new BufferedReader(fr);
        }
        catch(Exception e){}
    }
    
    public void writeRecord(Record re){
        pw.println(re);
        try{
            fw.close();
            pw.close();
        }
        catch(Exception e){}
    }
    
    public String readRecords(){
        String rs="";
        try{
            String r=br.readLine();
            while(r!=null){
                rs+="\n"+r;
                r=br.readLine();
            }
        }
        catch(Exception e){}
        try{
            fr.close();
            br.close();
        }
        catch(Exception e){}
        return rs;
    }
}

class GamePanel extends JPanel implements ActionListener {
    private final Image background=(new ImageIcon(getClass().getResource("/resources/Background.jpg"))).getImage();
    public final JLabel lblScore=new JLabel("Score: 0");
    public final JLabel lblLevel=new JLabel("Level: 1");
    public final ArrayList<Explosion> destroyed=new ArrayList<>(0);
    public final ArrayList<Grenade> dropped=new ArrayList<>(0);
    public Rocket droppedRo=null;
    public Banner banner=null;
    public final Fighter Fi=new Fighter();
    public final Fleet Fl=new Fleet();
    public Monster Mo=null;
    private final Random rand=new Random();
    private int score=0;
    private int level=1;
    private Timer EntranceMotion;
    private Timer GrenadeDropper;
    private int entranceActionCount;
    @Override
    public void paint(Graphics g){
        super.paint(g);
        g.drawImage(background,0,0,null);
        Fl.draw(g);
        Fi.draw(g);
        for(int i=0;i<dropped.size();i++)
            dropped.get(i).draw(g);
        if(Mo!=null)
            Mo.draw(g);
        if(droppedRo!=null)
            droppedRo.draw(g);
        for(int i=0;i<destroyed.size();i++)
            destroyed.get(i).draw(g);
        if(banner!=null)
            banner.draw(g);
    }
    
    public void entrance(){
        banner=new Banner(this,level);
        this.repaint();
        entranceActionCount=0;
        EntranceMotion=new Timer(10,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                for(int i=0;i<Fl.fleet.size();i++)
                    Fl.fleet.get(i).move(Direction.DOWN);
                repaint();
                entranceActionCount++;
                if(entranceActionCount==46+4*(level-1))
                    EntranceMotion.stop();
            }
        });
        EntranceMotion.start();
        
        GrenadeDropper=new Timer(1000-20*(level-1),this);
        GrenadeDropper.start();
    }
    
    public void destroy(int index){
        int x=Fl.fleet.get(index).getX();
        int y=Fl.fleet.get(index).getY();
        if(!(Fl.fleet.get(index).Gr.isEmpty()))
            dropped.add(Fl.fleet.get(index).Gr.get(Fl.fleet.get(index).Gr.size()-1));
        Fl.fleet.remove(index);
        destroyed.add(new Explosion(x,y,this));
        this.repaint();
        score+=500*level;
        if(Fl.fleet.isEmpty()&&level<4){
            level++;
            GrenadeDropper.stop();
            Fl.fill();
            this.entrance();
            this.setStats();
        }
        else if(Fl.fleet.isEmpty()&&level==4){
            level++;
            GrenadeDropper.stop();
            this.setStats();
            this.challenge();
        }
        else
            this.setStats();
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        for(int i=0;i<2+level;i++)
            Fl.fleet.get(rand.nextInt(Fl.fleet.size())).drop(this);
    }
    
    public void hold(){
        GrenadeDropper.stop();
    }
    
    public void challenge(){
        banner=new Banner(this,level);
        Mo=new Monster(this);
    }
    
    public void incScore(int value){
        score+=value;
    }
    
    public void setStats(){
        lblScore.setText("Score: "+score);
        lblLevel.setText("Level: "+level);
    }
    
    public int getScore(){return this.score;}
    public int getLevel(){return this.level;}
}

class GameFrame extends JFrame {
    private final GamePanel pnlGame=new GamePanel();
    private final JPanel pnlScore=new JPanel();
    private Direction dir;
    private Timer FighterMotion;
    
    public GameFrame(){
        this.init();
    }
    
    private void init(){
        this.setTitle("Space Invaders 1.0");
        Dimension ss=Toolkit.getDefaultToolkit().getScreenSize();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        Container c=this.getContentPane();
        
        pnlGame.setPreferredSize(new Dimension(GC.WIDTH*GC.PWIDTH,GC.WIDTH*GC.PHEIGHT));
        c.add(pnlGame);
        pnlGame.lblScore.setPreferredSize(new Dimension(150,30));
        pnlGame.lblLevel.setPreferredSize(new Dimension(150,30));
        pnlGame.lblScore.setFont(new Font("Star Jedi",Font.PLAIN,15));
        pnlGame.lblLevel.setFont(new Font("Star Jedi",Font.PLAIN,15));
        pnlScore.add(pnlGame.lblScore);
        pnlScore.add(pnlGame.lblLevel);
        c.add(pnlScore,BorderLayout.SOUTH);
        this.pack();
        this.setLocation((int)((ss.getWidth()-this.getWidth())/2),(int)((ss.getHeight()-this.getHeight())/2));
        
        FighterMotion=new Timer(7,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                pnlGame.Fi.move(dir);
                pnlGame.repaint();
                if(pnlGame.Fl.hasCollisionWith(pnlGame.Fi.getCollisionPoints())){
                    FighterMotion.stop();
                    pnlGame.Fi.die(pnlGame);
                }
            }
        });
        
        pnlGame.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                int key=e.getKeyCode();
                switch(key){
                    case KeyEvent.VK_UP:
                        dir=Direction.UP;
                        FighterMotion.start();
                        break;
                    case KeyEvent.VK_DOWN:
                        dir=Direction.DOWN;
                        FighterMotion.start();
                        break;
                    case KeyEvent.VK_RIGHT:
                        dir=Direction.RIGHT;
                        FighterMotion.start();
                        break;
                    case KeyEvent.VK_LEFT:
                        dir=Direction.LEFT;
                        FighterMotion.start();
                        break;
                    case KeyEvent.VK_SPACE:
                        pnlGame.Fi.fire(pnlGame);
                        break;
                }
            }
            @Override
            public void keyReleased(KeyEvent e){
                int key=e.getKeyCode();
                if(key==KeyEvent.VK_UP||key==KeyEvent.VK_DOWN||key==KeyEvent.VK_RIGHT||key==KeyEvent.VK_LEFT)
                    FighterMotion.stop();
            }
        });
        pnlGame.setFocusable(true);
        pnlGame.entrance();
    }
}

class ScoreFrame extends JFrame {
    private final JTextArea txtScore=new JTextArea();
    
    public ScoreFrame(){
        this.init();
    }
    
    private void init(){
        this.setTitle("Leaderboards");
        Dimension ss=Toolkit.getDefaultToolkit().getScreenSize();
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setResizable(false);
        Container c=this.getContentPane();
        
        txtScore.setPreferredSize(new Dimension(400,400));
        txtScore.setBackground(new Color(240,240,240));
        txtScore.setEditable(false);
        txtScore.setFont(new Font("Arial",Font.PLAIN,15));
        txtScore.setText("\n     Name\t\tScore\tLevel\n");
        this.add(txtScore);
        this.pack();
        this.setLocation((ss.width-this.getWidth())/2,(ss.height-this.getHeight())/2);
        
        Saver sv=new Saver();
        txtScore.setText(txtScore.getText()+sv.readRecords());
    }
}

class StartPanel extends JPanel {
    private final Image background=(new ImageIcon(getClass().getResource("/resources/StartBackground.jpg"))).getImage();
    private final Image start=(new ImageIcon(getClass().getResource("/resources/StartButton.png"))).getImage();
    private final Image high=(new ImageIcon(getClass().getResource("/resources/ScoreButton.png"))).getImage();
    private final Image credit=(new ImageIcon(getClass().getResource("/resources/AboutButton.png"))).getImage();
    private final Image exit=(new ImageIcon(getClass().getResource("/resources/ExitButton.png"))).getImage();
    
    @Override
    public void paint(Graphics g){
        super.paint(g);
        g.drawImage(background,0,0,null);
        g.drawImage(start,275,170,null);
        g.drawImage(high,275,240,null);
        g.drawImage(credit,275,310,null);
        g.drawImage(exit,275,380,null);
    }
}

class StartFrame extends JFrame {
    private final StartPanel pnlStart=new StartPanel();
    public static GameFrame gameFrame;
    public static ScoreFrame scoreFrame;
    
    public StartFrame(){
        this.init();
    }
    
    private void init(){
        this.setTitle("Space Invaders 1.0");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        Container c=this.getContentPane();
        
        pnlStart.setPreferredSize(new Dimension(800,600));
        c.add(pnlStart);
        this.pack();
        Dimension ss=Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((ss.width-this.getWidth())/2,(ss.height-this.getHeight())/2);
        
        pnlStart.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                int x=e.getX();
                int y=e.getY();
                if((x>=280&&x<=520)&&(y>=175&&y<=215)){
                    StartFrame.gameFrame=new GameFrame();
                    setVisible(false);
                    StartFrame.gameFrame.setVisible(true);
                }
                else if((x>=280&&x<=520)&&(y>=245&&y<=285)){
                    StartFrame.scoreFrame=new ScoreFrame();
                    StartFrame.scoreFrame.setVisible(true);
                }
                else if((x>=280&&x<=520)&&(y>=315&&y<=355)){
                    JOptionPane.showConfirmDialog(null,"Developer: Omar Lashin\n\nSupervisors: Dr.Cherif Salama, Eng. Eslam Mounir","About The Game",
                            JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE);
                }
                else if((x>=280&&x<=520)&&(y>=385&&y<=425)){
                    if(JOptionPane.showConfirmDialog(null,"Are you sure?","Quit Game",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION)
                        System.exit(0);
                }
            }
        });
    }
}