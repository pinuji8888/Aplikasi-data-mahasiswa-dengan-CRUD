package id.ac.nusa;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;

public final class Main {
    public static void main(String[] args) throws Exception {
        FlatLightLaf.setup(); UIManager.put("Button.arc",10); UIManager.put("Component.arc",10); UIManager.put("TextComponent.arc",8);
        UIManager.put("defaultFont",new Font("SansSerif",Font.PLAIN,13));
        if(args.length>0 && args[0].equals("--demo-captures")) { DemoCapture.run(Paths.get(".")); return; }
        Path base=Paths.get(System.getProperty("nusa.home",".")).toAbsolutePath().normalize();
        StudentRepository repository=new StudentRepository(base.resolve("data/mahasiswa.db"));
        SwingUtilities.invokeLater(()->{
            try { JFrame frame=new JFrame("Nusa Campus · Data Mahasiswa"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setContentPane(new StudentPanel(repository,base)); frame.setSize(1240,820); frame.setMinimumSize(new Dimension(1120,780)); frame.setLocationRelativeTo(null); frame.setVisible(true); }
            catch(Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(null,e.getMessage(),"Gagal membuka aplikasi",JOptionPane.ERROR_MESSAGE); }
        });
    }
}
