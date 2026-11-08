package id.ac.nusa;

import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** DAO SQLite; setiap operasi menggunakan prepared statement dan koneksi singkat. */
public final class StudentRepository {
    private final String url;
    public StudentRepository(Path file) throws Exception {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Class.forName("org.sqlite.JDBC");
        url = "jdbc:sqlite:" + file.toAbsolutePath();
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS students (nim TEXT PRIMARY KEY CHECK(length(nim)=10 AND nim NOT GLOB '*[^0-9]*'), name TEXT NOT NULL, program TEXT NOT NULL, year INTEGER NOT NULL CHECK(year BETWEEN 2000 AND 2100), email TEXT NOT NULL)");
        }
    }
    public Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) { s.execute("PRAGMA busy_timeout=5000"); }
        return c;
    }
    public List<Student> list(String keyword) throws SQLException {
        List<Student> result = new ArrayList<>();
        String term = "%" + keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT * FROM students WHERE nim LIKE ? ESCAPE '\\' OR name LIKE ? ESCAPE '\\' OR program LIKE ? ESCAPE '\\' ORDER BY nim")) {
            for (int i=1;i<=3;i++) p.setString(i,term);
            try (ResultSet r = p.executeQuery()) {
                while(r.next()) result.add(new Student(r.getString("nim"), r.getString("name"), r.getString("program"), r.getInt("year"), r.getString("email")));
            }
        }
        return result;
    }
    public void add(Student s) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("INSERT INTO students(nim,name,program,year,email) VALUES(?,?,?,?,?)")) {
            p.setString(1,s.nim); p.setString(2,s.name); p.setString(3,s.program); p.setInt(4,s.year); p.setString(5,s.email); p.executeUpdate();
        }
    }
    public void update(Student s) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("UPDATE students SET name=?,program=?,year=?,email=? WHERE nim=?")) {
            p.setString(1,s.name); p.setString(2,s.program); p.setInt(3,s.year); p.setString(4,s.email); p.setString(5,s.nim);
            if (p.executeUpdate()!=1) throw new SQLException("Mahasiswa tidak ditemukan. Muat ulang data.");
        }
    }
    public void delete(String nim) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("DELETE FROM students WHERE nim=?")) {
            p.setString(1,nim); if(p.executeUpdate()!=1) throw new SQLException("Mahasiswa tidak ditemukan. Muat ulang data.");
        }
    }
    public void seedDemo() throws SQLException {
        if (!list("").isEmpty()) throw new IllegalStateException("Data contoh hanya bisa dimuat saat database kosong.");
        String[] names={"Aditya Pratama","Alya Putri","Bagas Saputra","Citra Lestari","Dimas Wijaya","Farah Anindya","Gilang Ramadhan","Intan Maharani"};
        try (Connection c=connect(); PreparedStatement p=c.prepareStatement("INSERT INTO students VALUES(?,?,?,?,?)")) {
            c.setAutoCommit(false);
            try {
                for(int i=0;i<names.length;i++) { p.setString(1,"202600100"+(i+1)); p.setString(2,names[i]); p.setString(3,i%2==0?"Teknik Informatika":"Sistem Informasi"); p.setInt(4,2026); p.setString(5,"demo"+(i+1)+"@example.com"); p.addBatch(); }
                p.executeBatch(); c.commit();
            } catch(SQLException e) { c.rollback(); throw e; }
        }
    }
}
