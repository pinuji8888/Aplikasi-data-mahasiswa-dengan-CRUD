package id.ac.nusa;

/** Model immutable: aturan data berlaku untuk UI maupun penyimpanan. */
public final class Student {
    public final String nim, name, program, email;
    public final int year;

    public Student(String nim, String name, String program, int year, String email) {
        this.nim = nim.trim(); this.name = name.trim(); this.program = program.trim();
        this.year = year; this.email = email.trim();
        validateNim(this.nim);
        if (this.name.length() < 2 || this.name.length() > 80) throw new IllegalArgumentException("Nama harus 2–80 karakter.");
        if (this.program.isEmpty() || this.program.length() > 60) throw new IllegalArgumentException("Program studi wajib diisi, maksimal 60 karakter.");
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Angkatan harus antara 2000–2100.");
        if (this.email.length() > 100 || !this.email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Format email tidak valid.");
    }

    public static void validateNim(String nim) {
        if (!nim.matches("[0-9]{10}")) throw new IllegalArgumentException("NIM harus tepat 10 digit angka, contoh 2026001001.");
    }
}
