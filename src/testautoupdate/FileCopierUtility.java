/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testautoupdate;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author fakhruzzaman
 */
public class FileCopierUtility extends JFrame implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    private JProgressBar progressAll;
    private JProgressBar progressCurrent;
    private JTextArea txtDetails;
    private JButton btnCancel;
    private CopyTask task;
    private final List<FileProperties> listFp;

    public FileCopierUtility(List<FileProperties> listFp) {
        this.listFp = listFp;

        buildGUI();
    }

    private void buildGUI() {
        setTitle("Download CSS Update");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (task != null) {
                    task.cancel(true);
                }
                dispose();
                System.exit(0);
            }
        });

        JLabel lblProgressAll = new JLabel("Overall: ");
        JLabel lblProgressCurrent = new JLabel("Current File: ");
        progressAll = new JProgressBar(0, 100);
        progressAll.setStringPainted(true);
        progressCurrent = new JProgressBar(0, 100);
        progressCurrent.setStringPainted(true);
        txtDetails = new JTextArea(5, 50);
        txtDetails.setEditable(false);
        DefaultCaret caret = (DefaultCaret) txtDetails.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(txtDetails, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        btnCancel = new JButton("Cancel");
        btnCancel.setFocusPainted(false);
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(this);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel panProgressLabels = new JPanel(new BorderLayout(0, 5));
        JPanel panProgressBars = new JPanel(new BorderLayout(0, 5));

        panProgressLabels.add(lblProgressAll, BorderLayout.NORTH);
        panProgressLabels.add(lblProgressCurrent, BorderLayout.CENTER);
        panProgressBars.add(progressAll, BorderLayout.NORTH);
        panProgressBars.add(progressCurrent, BorderLayout.CENTER);

        JPanel panInput = new JPanel(new BorderLayout(0, 5));
        panInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Input"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel panProgress = new JPanel(new BorderLayout(0, 5));
        panProgress.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Progress"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel panDetails = new JPanel(new BorderLayout());
        panDetails.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Details"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel panControls = new JPanel(new BorderLayout());
        panControls.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        panProgress.add(panProgressLabels, BorderLayout.LINE_START);
        panProgress.add(panProgressBars, BorderLayout.CENTER);
        panDetails.add(scrollPane, BorderLayout.CENTER);
        panControls.add(btnCancel, BorderLayout.CENTER);

        JPanel panUpper = new JPanel(new BorderLayout());
        panUpper.add(panProgress, BorderLayout.SOUTH);

        contentPane.add(panUpper, BorderLayout.NORTH);
        contentPane.add(panDetails, BorderLayout.CENTER);
        contentPane.add(panControls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public void runDownload() {
        task = this.new CopyTask(listFp);
        task.addPropertyChangeListener(this);
        task.execute();
        btnCancel.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Cancel".equals(btnCancel.getText())) {
            task.cancel(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressAll.setValue(progress);
        }
    }

    class CopyTask extends SwingWorker<Void, Integer> {

        private URL source;
        private File target;
        private final List<FileProperties> listFp;
        private long totalBytes = 0L;
        private long copiedBytes = 0L;

        public CopyTask(List<FileProperties> listFp) {
            this.listFp = listFp;
            progressAll.setValue(0);
            progressCurrent.setValue(0);
        }

        @Override
        public Void doInBackground() throws Exception {
            txtDetails.append("Retrieving data from server ...\n");
            for (int i = 0; i < listFp.size(); i++) {
                FileProperties fp = listFp.get(i);
                source = new URL(fp.getSource());
                target = new File(fp.getTarget());

                getFileSize(source);
            }

            for (int i = 0; i < listFp.size(); i++) {
                FileProperties fp = listFp.get(i);
                source = new URL(fp.getSource());
                target = new File(fp.getTarget());

                copyFiles(source, target);
            }

            btnCancel.setEnabled(false);
            return null;
        }

        @Override
        public void process(List<Integer> chunks) {
            for (int i : chunks) {
                progressCurrent.setValue(i);
            }
        }

        @Override
        public void done() {
            setProgress(100);
        }

        private void getFileSize(URL url) {
            try {
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                totalBytes += conn.getContentLength();

                conn.disconnect();
            } catch (IOException e) {
                //return -1;
            }
        }

        private void copyFiles(URL sourceURL, File targetFile) throws IOException {
            URLConnection con = null;

            txtDetails.append("Copying " + sourceURL.toString() + " ... ");

            con = sourceURL.openConnection();
            BufferedOutputStream bos;
            try (BufferedInputStream bis = new BufferedInputStream(con.getInputStream())) {
                bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                long fileBytes = con.getContentLength();
                long soFar = 0L;
                int theByte;
                while ((theByte = bis.read()) != -1) {
                    bos.write(theByte);

                    setProgress((int) (copiedBytes++ * 100 / totalBytes));
                    publish((int) (soFar++ * 100 / fileBytes));
                }
            }
            
            bos.close();

            publish(100);

            String ext = getFileExtension(targetFile);
            txtDetails.append("Done!\n");

            if ("zip".equals(ext)) {
                unzip(targetFile);
            }
        }
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private void unzip(File file) {
        try {
            String fileName = file.getAbsolutePath();
            String zipFile = fileName;
            
            if (fileName.indexOf(".") > 0) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            
            txtDetails.append("unzip "+zipFile + " ...");
            UnZip unZip = new UnZip();
            unZip.unzip(zipFile, fileName);
            txtDetails.append("Done!\n");
            
            file.deleteOnExit();
        } catch (IOException ex) {
            Logger.getLogger(FileCopierUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
