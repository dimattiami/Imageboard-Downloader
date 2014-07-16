package dimattia.imageboard.downloader;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * Does the downloading part of the application
 * 
 * @author Mike
 *
 */
public class ThreadedDownloader {
	private String saveLocationStr;
	private String URL;
	private Vector<String> imageLocations;
	private int amountOfThreads = 1;
	private Thread[] downloadThreads;
	private boolean requestStop = false;
	private boolean isDoneDownloading = false;
	private int downloadCount = 0;
	private String status;

	/**
	 * Gets the list of images to be downloaded in Vector format and starts the
	 * downloading thread(s)
	 * 
	 * @param URL
	 *            The URL of the thread on imageboard
	 * @param saveLoc
	 *            Destination for files to be saved
	 * @param threadAmt
	 *            Allows for multithreading
	 * @param getWebm
	 *            Determines whether or not webm files should be downloaded
	 */

	public ThreadedDownloader(String URL, String saveLoc, int threadAmt,
			boolean getWebm) {

		amountOfThreads = threadAmt;
		this.URL = URL;
		saveLocationStr = saveLoc;
		/*
		 * System.out.println("URL:" + this.URL + "\nThread amt: " +
		 * amountOfThreads + "\nSave location: " + saveLocationStr +
		 * "\nGet webms: " + getWebm);
		 */
		try {
			imageLocations = ImageFinder.getImageLinks(URL, getWebm);

			new File(saveLocationStr + "\\/").mkdir(); // creates the path if it
														// doesn't
														// exist
		} catch (FileNotFoundException e) { // Notify user that thread 404ed
			JOptionPane.showMessageDialog(null,
					"Thread 404; unable to download.", "404",
					JOptionPane.WARNING_MESSAGE);
		} catch (IOException e) { // Other error catching
			JOptionPane.showMessageDialog(null,
					"An error has occurred:\n" + e.getMessage(), "Error",
					JOptionPane.WARNING_MESSAGE);
		}

		DownloadThread dT = new DownloadThread();

		if (requestStop == true) // Make sure the downloading thread(s) are not
									// in request-to-stop mode
			requestStop = false;

		downloadThreads = new Thread[amountOfThreads];

		for (Thread t : downloadThreads) {
			t = new Thread(dT);
			t.start(); // Start the threads
		}
	}

	/**
	 * Set the requestStop flag to true, allowing running threads to halt
	 * execution and updates the status
	 */
	public void stopDownload() {
		status = "Status: download stopped @ " + downloadCount + "/"
				+ imageLocations.size() + " files";
		requestStop = true;
	}

	/**
	 * @return True if all images are finished downloading
	 */
	public boolean isDoneDownloading() {
		return isDoneDownloading;
	}

	/**
	 * @return Status of downloading
	 */
	public String getDownloadStatus() {
		return status;
	}

	/**
	 * @return % downloaded, for the progress bar
	 */
	public int getPercentDone() {
		if (downloadCount == 0)
			return 0;
		return (int) (((double) downloadCount / imageLocations.size()) * 100);
	}

	/**
	 * The runnable object that does the downloading
	 * 
	 * @author Mike
	 *
	 */
	class DownloadThread implements Runnable {
		private Iterator<String> it = imageLocations.iterator(); // Iterator for
																	// the
																	// vector
																	// holding
																	// image
																	// locations
		private boolean threadNotFoundMsgDisplayed = false; // If message has
															// already been
															// displayed that
															// the thread was
															// not found

		public void run() {
			isDoneDownloading = false; // Just incase...

			final String urlBeginning = "http://i.4cdn.org/"; // Beginning of
																// the image URL

			while (it.hasNext()) {
				if (requestStop)
					break;
				status = "Status: downloading " + downloadCount + "/"
						+ imageLocations.size();
				String curURL = it.next(); // Board & file name
				String fileName = curURL.split("/")[1]; // File name only
				FileOutputStream out = null;
				try {
					URL pictureURL = new URL(urlBeginning + curURL); // Connects
																		// to
																		// URL
					// Check if file already exists, if does, skip downloading

					if (new File(saveLocationStr + "\\" + fileName).exists()) {
						// System.out.println("Exists..");
						downloadCount++;
						continue;
					}

					BufferedInputStream ins = new BufferedInputStream(
							pictureURL.openStream()); // Creates reader for
														// image data

					out = new FileOutputStream(saveLocationStr + "\\"
							+ fileName); // Creates file writer
					int next;

					while ((next = ins.read()) != -1) {
						out.write(next); // Write to file
					}
					downloadCount++; // Increment amount of images downloaded
				} catch (FileNotFoundException e) { // Glitch when thread is
													// still active, but images
													// are 404ed
					if (threadNotFoundMsgDisplayed == false) {
						threadNotFoundMsgDisplayed = true;
						stopDownload();
						JOptionPane.showMessageDialog(null,
								"Thread found, but images not.\n"
										+ "Try a different thread.");
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (out != null)
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
			if (!it.hasNext()) {
				isDoneDownloading = true;
			}
		}

	}

	/**
	 * Allows user to specify amount of threads to use
	 * 
	 * @param amt
	 *            amount of threads to use
	 */
	public void setAmountOfThreads(int amt) {
		amountOfThreads = amt;
	}
}
