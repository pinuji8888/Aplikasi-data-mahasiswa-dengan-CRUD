package id.ac.nusa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;

/** View dan controller Swing. Operasi laporan dijalankan di worker agar UI responsif. */
public final class StudentPanel extends JPanel {
    private final StudentRepository repository;
    private final JTextField nim=new JTextField(), name=new JTextField(), email=new JTextField(), search=new JTextField();
    private final JComboBox<String> program=new JComboBox<>(new String[]{"Teknik Informatika","Sistem Informasi","Manajemen","Akuntansi"});
    private final JSpinner year=new JSpinner(new SpinnerNumberModel(2026,2000,2100,1));
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"NIM","Nama mahasiswa","Program studi","Angkatan","Email"},0) {
        public boolean isCellEditable(int row,int col) { return false; }
    };
    private final JTable table=new JTable(model);
    private final JLabel status=new JLabel("Siap. Tambahkan mahasiswa pertama atau muat data contoh."), count=new JLabel();
    private final JButton add, update, delete;
    private String selectedNim;
    private final Path base;
    private static final Color INK=new Color(23,48,67), TEAL=new Color(0,116,120);

    public StudentPanel(StudentRepository repository, Path base) throws Exception {
        this.repository=repository; this.base=base;
        setLayout(new BorderLayout(0,20)); setBackground(new Color(238,244,247)); setBorder(new EmptyBorder(24,28,20,28));
        BufferedImage backdrop=ImageIO.read(getClass().getResourceAsStream("/images/campus.png"));
        JPanel banner=new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) { super.paintComponent(g); g.drawImage(backdrop,0,0,getWidth(),getHeight(),null); }
        };
        banner.setPreferredSize(new Dimension(1000,162)); banner.setBorder(new EmptyBorder(22,26,20,26));
        JPanel words=new JPanel(); words.setOpaque(false); words.setLayout(new BoxLayout(words,BoxLayout.Y_AXIS));
        words.add(label("NUSA / CAMPUS",13,new Color(108,224,210))); words.add(Box.createVerticalStrut(12));
        words.add(label("Ruang data mahasiswa",30,Color.WHITE)); words.add(Box.createVerticalStrut(8));
        words.add(label("Kelola identitas. Rapikan administrasi. Cetak dengan mudah.",14,new Color(211,230,239)));
        banner.add(words,BorderLayout.WEST); add(banner,BorderLayout.NORTH);

        JPanel body=new JPanel(new BorderLayout(20,0)); body.setOpaque(false); add(body,BorderLayout.CENTER);
        JPanel form=new JPanel(); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20,20,20,20)); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(285,480));
        form.add(label("01  /  IDENTITAS",12,TEAL)); form.add(Box.createVerticalStrut(12)); form.add(label("Detail mahasiswa",21,INK));
        form.add(Box.createVerticalStrut(12));
        field(form,"NIM · 10 digit",nim); field(form,"Nama lengkap",name); program.setEditable(true); field(form,"Program studi",program);
        year.setEditor(new JSpinner.NumberEditor(year,"#")); field(form,"Angkatan",year); field(form,"Email",email);
        add=button("+ Tambah mahasiswa",()->perform(()->createFromForm()),true);
        update=button("Simpan perubahan",()->perform(()->updateFromForm()),false);
        delete=button("Hapus mahasiswa",()->{
            if(selectedNim!=null && JOptionPane.showConfirmDialog(this,"Hapus mahasiswa "+selectedNim+"?","Konfirmasi hapus",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) perform(()->deleteSelected());
        },false);
        for(JButton b:new JButton[]{add,update,delete,button("Bersihkan form",this::clearForm,false)}) { b.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); b.setAlignmentX(LEFT_ALIGNMENT); form.add(b); form.add(Box.createVerticalStrut(7)); }
        form.add(Box.createVerticalGlue()); body.add(form,BorderLayout.WEST);

        JPanel records=new JPanel(new BorderLayout(0,14)); records.setBackground(Color.WHITE); records.setBorder(new EmptyBorder(20,20,20,20)); body.add(records,BorderLayout.CENTER);
        JPanel top=new JPanel(new BorderLayout(12,12)); top.setOpaque(false);
        JPanel titles=new JPanel(new GridLayout(2,1,0,6)); titles.setOpaque(false); titles.add(label("02  /  DIREKTORI MAHASISWA",12,TEAL)); count.setFont(new Font("SansSerif",Font.BOLD,21)); count.setForeground(INK); titles.add(count); top.add(titles,BorderLayout.NORTH);
        search.putClientProperty("JTextField.placeholderText","Cari NIM, nama, atau program studi..."); search.setPreferredSize(new Dimension(220,38)); top.add(search,BorderLayout.CENTER);
        top.add(button("Cetak laporan PDF",this::showReportDialog,true),BorderLayout.EAST); records.add(top,BorderLayout.NORTH);
        table.setRowHeight(34); table.setFont(new Font("SansSerif",Font.PLAIN,13)); table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
        table.setSelectionBackground(new Color(211,240,235)); table.setSelectionForeground(INK); table.setShowVerticalLines(false); table.setGridColor(new Color(237,241,244));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true); table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(110); table.getColumnModel().getColumn(1).setPreferredWidth(155); table.getColumnModel().getColumn(2).setPreferredWidth(150); table.getColumnModel().getColumn(4).setPreferredWidth(175);
        JScrollPane tableScroll=new JScrollPane(table); tableScroll.setColumnHeaderView(table.getTableHeader()); records.add(tableScroll,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); actions.setOpaque(false);
        actions.add(button("Muat ulang",()->perform(()->refresh()),false));
        actions.add(button("Isi data contoh",()->perform(()->{ repository.seedDemo(); refresh(); message("8 mahasiswa contoh berhasil ditambahkan."); }),false));
        actions.add(button("Simpan PNG",()->perform(()->{ Path p=base.resolve("captures/tampilan-"+System.currentTimeMillis()+".png"); capture(p); message("Tangkapan tersimpan: "+p); }),false));
        records.add(actions,BorderLayout.SOUTH);
        status.setForeground(TEAL); status.setBorder(new EmptyBorder(2,0,0,0)); add(status,BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(e->{ if(!e.getValueIsAdjusting()) loadSelection(); });
        Timer timer=new Timer(220,e->perform(()->refresh())); timer.setRepeats(false);
        search.getDocument().addDocumentListener(new DocumentListener(){ public void insertUpdate(DocumentEvent e){timer.restart();} public void removeUpdate(DocumentEvent e){timer.restart();} public void changedUpdate(DocumentEvent e){timer.restart();} });
        refresh(); clearForm();
    }
    private static JLabel label(String text,int size,Color color) { JLabel l=new JLabel(text); l.setFont(new Font("SansSerif",Font.BOLD,size)); l.setForeground(color); l.setAlignmentX(LEFT_ALIGNMENT); return l; }
    private static void field(JPanel panel,String title,JComponent input) {
        panel.add(Box.createVerticalStrut(10)); panel.add(label(title,12,INK)); panel.add(Box.createVerticalStrut(5));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); input.setAlignmentX(LEFT_ALIGNMENT); panel.add(input); panel.add(Box.createVerticalStrut(3));
    }
    private static JButton button(String text,Runnable action,boolean primary) {
        JButton b=new JButton(text); b.addActionListener(e->action.run()); b.setFocusPainted(false);
        if(primary){b.setBackground(TEAL); b.setForeground(Color.WHITE);} return b;
    }
    private interface Action { void run() throws Exception; }
    private void perform(Action a) { try { a.run(); } catch(Exception ex) { String msg=ex.getMessage(); if(msg!=null && msg.contains("UNIQUE")) msg="NIM sudah terdaftar. Gunakan NIM lain."; message("Gagal: "+msg); JOptionPane.showMessageDialog(this,msg,"Periksa kembali",JOptionPane.WARNING_MESSAGE); } }
    void message(String text) { status.setText(text); }
    void refresh() throws Exception {
        List<Student> students=repository.list(search.getText()); model.setRowCount(0);
        for(Student s:students) model.addRow(new Object[]{s.nim,s.name,s.program,s.year,s.email});
        count.setText(students.size()+" mahasiswa"+(search.getText().trim().isEmpty()?" terdaftar":" ditemukan")); clearForm();
    }
    private void loadSelection() {
        int view=table.getSelectedRow(); if(view<0) return; int r=table.convertRowIndexToModel(view);
        setForm(new Student(model.getValueAt(r,0).toString(),model.getValueAt(r,1).toString(),model.getValueAt(r,2).toString(),(Integer)model.getValueAt(r,3),model.getValueAt(r,4).toString()));
        selectedNim=nim.getText(); nim.setEditable(false); add.setEnabled(false); update.setEnabled(true); delete.setEnabled(true);
    }
    void setForm(Student s) { nim.setText(s.nim); name.setText(s.name); program.setSelectedItem(s.program); year.setValue(s.year); email.setText(s.email); }
    void select(String target) {
        for(int r=0;r<model.getRowCount();r++) if(target.equals(model.getValueAt(r,0))) {int v=table.convertRowIndexToView(r); table.setRowSelectionInterval(v,v); table.scrollRectToVisible(table.getCellRect(v,0,true)); return;}
        throw new IllegalArgumentException("NIM tidak tampil: "+target);
    }
    private Student readForm() throws Exception { year.commitEdit(); return new Student(nim.getText(),name.getText(),program.getSelectedItem()==null?"":program.getSelectedItem().toString(),(Integer)year.getValue(),email.getText()); }
    void createFromForm() throws Exception { Student s=readForm(); repository.add(s); refresh(); message("TAMBAH BERHASIL · "+s.nim+" / "+s.name); }
    void updateFromForm() throws Exception { if(selectedNim==null) throw new IllegalArgumentException("Pilih mahasiswa terlebih dahulu."); Student s=readForm(); repository.update(s); refresh(); message("UBAH BERHASIL · "+s.nim+" / "+s.name); }
    void deleteSelected() throws Exception { if(selectedNim==null) throw new IllegalArgumentException("Pilih mahasiswa terlebih dahulu."); String id=selectedNim; repository.delete(id); refresh(); message("HAPUS BERHASIL · Mahasiswa "+id+" telah dihapus."); }
    void clearForm() { selectedNim=null; table.clearSelection(); nim.setText(""); nim.setEditable(true); name.setText(""); email.setText(""); program.setSelectedIndex(0); year.setValue(2026); add.setEnabled(true); update.setEnabled(false); delete.setEnabled(false); }
    public void capture(Path path) throws Exception {
        Files.createDirectories(path.toAbsolutePath().getParent()); BufferedImage image=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics(); printAll(g); g.dispose(); ImageIO.write(image,"png",path.toFile());
    }
    private void showReportDialog() {
        JTextField from=new JTextField("0000000000"), to=new JTextField("9999999999");
        JPanel range=new JPanel(new GridLayout(0,1,0,8)); range.add(new JLabel("Rentang NIM inklusif · tepat 10 digit")); range.add(new JLabel("NIM awal")); range.add(from); range.add(new JLabel("NIM akhir")); range.add(to);
        if(JOptionPane.showConfirmDialog(this,range,"Cetak laporan mahasiswa",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION) return;
        try { Student.validateNim(from.getText().trim()); Student.validateNim(to.getText().trim()); if(from.getText().trim().compareTo(to.getText().trim())>0) throw new IllegalArgumentException("NIM awal lebih besar dari NIM akhir."); }
        catch(Exception e) { JOptionPane.showMessageDialog(this,e.getMessage()); return; }
        JFileChooser chooser=new JFileChooser(base.resolve("reports").toFile()); chooser.setSelectedFile(base.resolve("reports/laporan-mahasiswa.pdf").toFile());
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        Path chosen=chooser.getSelectedFile().toPath(); if(!chosen.toString().toLowerCase().endsWith(".pdf")) chosen=Paths.get(chosen+".pdf"); final Path output=chosen;
        if(Files.exists(output) && JOptionPane.showConfirmDialog(this,"Ganti file yang sudah ada?","Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        message("Menyusun laporan PDF...");
        new SwingWorker<Path,Void>() {
            protected Path doInBackground() throws Exception { ReportService service=new ReportService(repository); service.export(service.create(from.getText(),to.getText()),output); return output; }
            protected void done() { try { Path p=get(); message("LAPORAN BERHASIL · "+p.toAbsolutePath()); JOptionPane.showMessageDialog(StudentPanel.this,"PDF tersimpan di:\n"+p.toAbsolutePath()); if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) Desktop.getDesktop().open(p.toFile()); } catch(Exception e) { Throwable cause=e.getCause()==null?e:e.getCause(); message("Laporan: "+cause.getMessage()); JOptionPane.showMessageDialog(StudentPanel.this,cause.getMessage(),"Laporan",JOptionPane.WARNING_MESSAGE); } }
        }.execute();
    }
}
