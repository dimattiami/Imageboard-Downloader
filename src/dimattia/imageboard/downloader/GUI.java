package dimattia.imageboard.downloader;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

/**
 * Contains the GUI for the imageboard downloader
 * 
 * @author Mike
 *
 */
public class GUI extends JFrame {
	private JTextField txtSaveDestination;
	private JSpinner spinner;
	private JButton btnStop;
	private JButton btnDownload;
	private JProgressBar progressBar;
	private JLabel lblThreads;
	private JLabel lblThreadLink;
	private JLabel lblSaveDestination;
	private JTextField txtpnThreadUrl;
	private JCheckBox chckbxDownloadWebm;
	private ThreadedDownloader tD;
	private Timer t;
	private JLabel statusLabel;

	public static void main(String[] args) {
		new GUI();
	}

	public GUI() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Imageboard Downloader");
		btnDownload = new JButton("Download");
		btnDownload.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!btnDownload.isEnabled())
					return;
				btnDownload.setEnabled(false);
				if (tD != null)
					tD = null; // Safecheck to remove old instances of downloader
								// if exist
				tD = new ThreadedDownloader(txtpnThreadUrl.getText(),
						txtSaveDestination.getText(), (int) spinner.getValue(),
						chckbxDownloadWebm.isSelected());
				t.start();
				txtSaveDestination.setEnabled(false);
				txtpnThreadUrl.setEnabled(false);
				chckbxDownloadWebm.setEnabled(false);
				spinner.setEnabled(false);
				btnStop.setEnabled(true);
				statusLabel.setText("Status: Downloading");
			}
		});
		btnDownload.setBounds(381, 22, 137, 23);

		btnStop = new JButton("Stop Download");
		btnStop.setEnabled(false);
		btnStop.setBounds(381, 57, 137, 23);
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!btnStop.isEnabled())
					return;
				if (tD != null)
					tD.stopDownload();
				statusLabel.setText(tD.getDownloadStatus());
				resetGUI();
			}
		});

		chckbxDownloadWebm = new JCheckBox("Get .webm's", true);
		chckbxDownloadWebm.setBounds(261, 57, 114, 23);

		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		spinner.setBounds(218, 58, 37, 20);
		txtpnThreadUrl = new JTextField();
		txtpnThreadUrl.setBounds(10, 23, 361, 20);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 89, 508, 20);

		t = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tD != null) {
					progressBar.setValue(tD.getPercentDone());
					statusLabel.setText(tD.getDownloadStatus());
					if (tD.isDoneDownloading()) {
						resetGUI();
						statusLabel.setText("Finished downloading");
						JOptionPane.showMessageDialog(null, "Thread has finished downloading", "Downloading complete", JOptionPane.INFORMATION_MESSAGE);
						if (tD != null)
							tD = null;
						t.stop();
					}
				}
			}
		});

		txtSaveDestination = new JTextField();
		txtSaveDestination.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!txtSaveDestination.isEnabled())
					return;
				JFileChooser fC = new JFileChooser();
				fC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fC.showSaveDialog(null);
				try {
					txtSaveDestination.setText(fC.getSelectedFile()
							.getAbsolutePath());
				} catch (NullPointerException ex) {
				}
			}
		});
		txtSaveDestination.setBounds(10, 58, 143, 20);
		txtSaveDestination.setColumns(10);

		lblThreads = new JLabel("Threads: ");
		lblThreads.setBounds(163, 61, 54, 14);
		getContentPane().setLayout(null);
		getContentPane().add(progressBar);
		getContentPane().add(txtSaveDestination);
		getContentPane().add(chckbxDownloadWebm);
		getContentPane().add(spinner);
		getContentPane().add(lblThreads);
		getContentPane().add(btnStop);
		getContentPane().add(txtpnThreadUrl);
		getContentPane().add(btnDownload);

		lblThreadLink = new JLabel("Thread URL");
		lblThreadLink.setBounds(10, 9, 73, 14);
		getContentPane().add(lblThreadLink);

		lblSaveDestination = new JLabel("Save destination");
		lblSaveDestination.setBounds(10, 44, 143, 14);
		getContentPane().add(lblSaveDestination);

		statusLabel = new JLabel("Status: waiting for user input");
		statusLabel.setBounds(10, 112, 378, 14);
		getContentPane().add(statusLabel);

		setSize(540, 157);
		setResizable(false);
		setVisible(true);
	}

	/**
	 * Resets the GUI to starting state
	 */
	private void resetGUI() {
		if (tD != null)
			tD = null;
		progressBar.setValue(0);
		t.stop();
		btnDownload.setEnabled(true);
		txtSaveDestination.setEnabled(true);
		txtpnThreadUrl.setEnabled(true);
		chckbxDownloadWebm.setEnabled(true);
		spinner.setEnabled(true);
		btnStop.setEnabled(false);
	}
}
