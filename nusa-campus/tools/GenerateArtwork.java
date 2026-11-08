import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/** Ilustrasi orisinal berbasis Java2D, dapat dibuat ulang tanpa aset eksternal. */
public class GenerateArtwork {
    public static void main(String[] args) throws Exception {
        BufferedImage bg=new BufferedImage(1600,260,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=bg.createGraphics(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0,0,new Color(15,40,60),1600,260,new Color(18,104,111))); g.fillRect(0,0,1600,260);
        g.setColor(new Color(255,255,255,10));
        for(int x=0;x<1600;x+=42) for(int y=0;y<260;y+=42) g.fillOval(x,y,3,3);
        g.setColor(new Color(255,222,157)); g.fillOval(1330,30,90,90);
        g.setColor(new Color(32,127,132)); g.fill(new Ellipse2D.Double(960,170,750,200));
        g.setColor(new Color(26,91,110)); g.fillRoundRect(1040,100,390,140,12,12);
        g.setColor(new Color(194,219,216)); g.fillRect(1145,82,190,158);
        Path2D roof=new Path2D.Double(); roof.moveTo(1117,83);roof.lineTo(1240,26);roof.lineTo(1363,83);roof.closePath();g.setColor(new Color(235,233,207));g.fill(roof);
        g.setColor(new Color(45,113,127)); for(int x=1160;x<1320;x+=38)g.fillRect(x,105,18,114);
        g.setColor(new Color(241,190,116));for(int x=1060;x<1420;x+=36){if(x>1130&&x<1340)continue;g.fillRoundRect(x,128,16,24,3,3);g.fillRoundRect(x,174,16,24,3,3);}
        g.setColor(new Color(221,232,218)); g.fillRect(1128,231,224,9);g.fillRect(1110,242,260,9);
        for(int x:new int[]{1000,1458,1510}){g.setColor(new Color(17,58,69));g.fillRect(x,155,9,90);g.setColor(new Color(73,161,145));g.fillOval(x-25,108,60,74);}
        g.dispose(); ImageIO.write(bg,"png",new File("src/main/resources/images/campus.png"));
        BufferedImage logo=new BufferedImage(180,180,BufferedImage.TYPE_INT_ARGB);g=logo.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0,116,120));g.fillRoundRect(0,0,180,180,42,42);g.setColor(new Color(235,249,245));
        Path2D cap=new Path2D.Double();cap.moveTo(25,69);cap.lineTo(90,37);cap.lineTo(155,69);cap.lineTo(90,101);cap.closePath();g.fill(cap);
        g.setStroke(new BasicStroke(9,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine(49,91,49,117);g.draw(new Arc2D.Double(49,100,82,36,180,180,Arc2D.OPEN));g.drawLine(131,91,131,117);g.setColor(new Color(255,206,126));g.drawLine(153,75,153,117);g.dispose();ImageIO.write(logo,"png",new File("src/main/resources/images/logo.png"));
    }
}
