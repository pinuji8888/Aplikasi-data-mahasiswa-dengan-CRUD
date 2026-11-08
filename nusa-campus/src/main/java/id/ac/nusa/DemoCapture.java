package id.ac.nusa;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.*;

/** Bukti yang dirender dari panel Swing asli setelah operasi pada database demo terpisah. */
public final class DemoCapture {
    public static void run(Path base) throws Exception {
        Path temporary=Files.createTempDirectory("nusa-demo-");
        try {
            StudentRepository repo=new StudentRepository(temporary.resolve("demo.db")); repo.seedDemo();
            Exception[] failure=new Exception[1];
            SwingUtilities.invokeAndWait(()->{
                try {
                    StudentPanel panel=new StudentPanel(repo,base); panel.setSize(1240,820); layout(panel);
                    panel.message("BACA BERHASIL · 8 mahasiswa contoh dimuat dari SQLite."); panel.capture(base.resolve("captures/01-read.png"));
                    panel.setForm(new Student("2026001009","Nadia Permata","Teknik Informatika",2026,"nadia@example.com")); panel.createFromForm(); layout(panel); panel.capture(base.resolve("captures/02-create.png"));
                    panel.select("2026001009"); panel.setForm(new Student("2026001009","Nadia Permata Sari","Sistem Informasi",2026,"nadia.sari@example.com")); panel.updateFromForm(); panel.select("2026001009"); layout(panel); panel.capture(base.resolve("captures/03-update.png"));
                    panel.deleteSelected(); layout(panel); panel.capture(base.resolve("captures/04-delete.png"));
                } catch(Exception e) { failure[0]=e; }
            });
            if(failure[0]!=null) throw failure[0];
            ReportService service=new ReportService(repo); JasperPrint report=service.create("2026001002","2026001007"); service.export(report,base.resolve("reports/contoh-laporan.pdf"));
            Image rendered=JasperPrintManager.printPageToImage(report,0,1.5f); BufferedImage png=new BufferedImage(rendered.getWidth(null),rendered.getHeight(null),BufferedImage.TYPE_INT_RGB);
            Graphics2D g=png.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0,0,png.getWidth(),png.getHeight()); g.drawImage(rendered,0,0,null); g.dispose(); ImageIO.write(png,"png",base.resolve("captures/05-report.png").toFile());
            System.out.println("CRUD demo berhasil. Lima PNG dan contoh PDF tersimpan. Database pengguna tidak diubah.");
        } finally { try(java.util.stream.Stream<Path> paths=Files.walk(temporary)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(Exception e){throw new RuntimeException(e);}}); } }
    }
    private static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) if(child instanceof Container) layout((Container)child); }
}
