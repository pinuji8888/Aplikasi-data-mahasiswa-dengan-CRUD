package id.ac.nusa;

import net.sf.jasperreports.engine.*;
import java.nio.file.*;
import java.sql.Connection;
import java.io.InputStream;
import java.util.*;
import javax.imageio.ImageIO;

public final class ReportService {
    private final StudentRepository repository;
    public ReportService(StudentRepository repository) { this.repository=repository; }
    public JasperPrint create(String from, String to) throws Exception {
        from=from.trim(); to=to.trim();
        Student.validateNim(from); Student.validateNim(to);
        if(from.compareTo(to)>0) throw new IllegalArgumentException("NIM awal tidak boleh lebih besar dari NIM akhir.");
        Map<String,Object> parameters=new HashMap<>();
        parameters.put("NIM_FROM",from); parameters.put("NIM_TO",to);
        try(InputStream logo=getClass().getResourceAsStream("/images/logo.png");
            InputStream template=getClass().getResourceAsStream("/reports/students.jrxml"); Connection c=repository.connect()) {
            parameters.put("LOGO",ImageIO.read(logo));
            JasperReport report=JasperCompileManager.compileReport(template);
            JasperPrint print=JasperFillManager.fillReport(report,parameters,c);
            if(print.getPages().isEmpty()) throw new IllegalArgumentException("Tidak ada mahasiswa dalam rentang NIM tersebut.");
            return print;
        }
    }
    public void export(JasperPrint print, Path output) throws Exception {
        Files.createDirectories(output.toAbsolutePath().getParent());
        Path temp=Files.createTempFile(output.toAbsolutePath().getParent(),"report-",".pdf");
        try {
            JasperExportManager.exportReportToPdfFile(print,temp.toString());
            Files.move(temp,output,StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temp); }
    }
}
