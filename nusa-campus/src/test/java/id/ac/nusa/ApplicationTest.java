package id.ac.nusa;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.sql.SQLException;
import net.sf.jasperreports.engine.*;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

class ApplicationTest {
    @TempDir Path dir;
    StudentRepository repo;
    @BeforeEach void setup() throws Exception { repo=new StudentRepository(dir.resolve("test.db")); }
    Student student(String nim,String name) { return new Student(nim,name,"Teknik Informatika",2026,"student@example.com"); }
    @Test void crudPersistsAndRejectsDuplicates() throws Exception {
        repo.add(student("2026001001","Alya Putri"));
        assertThrows(SQLException.class,()->repo.add(student("2026001001","Duplikat")));
        repo.update(student("2026001001","Alya Lestari"));
        StudentRepository reopened=new StudentRepository(dir.resolve("test.db"));
        assertEquals("Alya Lestari",reopened.list("").get(0).name);
        reopened.delete("2026001001"); assertTrue(repo.list("").isEmpty());
        assertThrows(SQLException.class,()->repo.update(student("2026001001","Tidak ada")));
        assertThrows(SQLException.class,()->repo.delete("2026001001"));
    }
    @Test void searchEscapesWildcardsAndHandlesQuotes() throws Exception {
        repo.add(student("2026001001","D'Angelo")); repo.add(student("2026001002","Alya"));
        assertEquals(1,repo.list("D'Angelo").size()); assertTrue(repo.list("%_'").isEmpty());
        assertTrue(repo.list("' OR 1=1 --").isEmpty()); assertEquals(2,repo.list("informatika").size());
    }
    @Test void validationAndDemoSafety() throws Exception {
        assertThrows(IllegalArgumentException.class,()->student("12abc","Alya"));
        assertThrows(IllegalArgumentException.class,()->new Student("2026001001","Alya","TI",2026,"bad-email"));
        repo.seedDemo(); assertEquals(8,repo.list("").size());
        assertThrows(IllegalStateException.class,repo::seedDemo); assertEquals(8,repo.list("").size());
    }
    @Test void reportUsesInclusiveRangeAndRejectsEmptyOrReversedRange() throws Exception {
        repo.seedDemo(); ReportService service=new ReportService(repo);
        JasperPrint print=service.create("2026001002","2026001004");
        Path pdf=dir.resolve("range.pdf"); service.export(print,pdf);
        PdfReader reader=new PdfReader(pdf.toString());
        try { String text=new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("2026001002")); assertTrue(text.contains("Bagas Saputra")); assertTrue(text.contains("Citra Lestari"));
            assertFalse(text.contains("Aditya Pratama")); assertFalse(text.contains("Dimas Wijaya")); assertTrue(text.contains("Total: 3 mahasiswa"));
        } finally { reader.close(); }
        assertThrows(IllegalArgumentException.class,()->service.create("2026001004","2026001002"));
        assertThrows(IllegalArgumentException.class,()->service.create("2026002000","2026003000"));
        assertThrows(IllegalArgumentException.class,()->service.create("' OR 1=1--","9999999999"));
        assertEquals(1,service.create("2026001002","2026001002").getPages().size());
    }
    @Test void pdfHasCorrectCurrentAndTotalPages() throws Exception {
        for(int i=1;i<=80;i++) repo.add(student(String.format("202600%04d",i),"Mahasiswa nomor "+i));
        ReportService service=new ReportService(repo); JasperPrint print=service.create("0000000000","9999999999");
        assertTrue(print.getPages().size()>1); Path pdf=dir.resolve("multipage.pdf"); service.export(print,pdf);
        PdfReader reader=new PdfReader(pdf.toString());
        try { int pages=reader.getNumberOfPages(); assertEquals(print.getPages().size(),pages);
            for(int i=1;i<=pages;i++) { String text=new PdfTextExtractor(reader).getTextFromPage(i).replaceAll("\\s+"," "); assertTrue(text.replace(" ","").contains("Halaman"+i+"dari"+pages),text); assertTrue(text.contains("Laporan Data Mahasiswa")); }
        } finally { reader.close(); }
    }
}
