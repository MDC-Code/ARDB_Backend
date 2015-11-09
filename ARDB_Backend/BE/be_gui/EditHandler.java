package be_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.swing.Timer;

import utilities.FileEmailer;

public class EditHandler extends ServicePanelInd {

	private String editFilePath;
	private File currentEditFile;
	private long currentEditTimeStamp;
	private FileEmailer editEmailer;
	private SimpleDateFormat sdf;
	private EditListener el;

	public EditHandler() {
		super("Edits");
		sdf = new SimpleDateFormat("MM-dd-yy kk:mm");
		editEmailer = new FileEmailer();
		el = new EditListener();
		timer = new Timer(34564, el);

		editFilePath = "Z:\\AAPRINT-EDIT";
		currentEditFile = new File(editFilePath);
		currentEditTimeStamp = currentEditFile.lastModified();

		timer.start();
		lblStatus.setText("Running");
		lblLastUpdate.setText(sdf.format(new Date()));

	}

	private void parseEdits(File file) {
		ArrayList<Edit> all, credit, creditCard, changeOrder;
		all = new ArrayList<Edit>();
		credit = new ArrayList<Edit>();
		creditCard = new ArrayList<Edit>();
		changeOrder = new ArrayList<Edit>();

		try {
			Scanner scan = new Scanner(file);
			scan.useDelimiter(new String(new char[] { 12 }));
			while (scan.hasNext()) {
				Edit e = new Edit(scan.next());
				all.add(e);
				if (e.inCredit()) {
					credit.add(e);
				}
				if (e.isCreditCard()) {
					creditCard.add(e);
				}
				if (e.isChange()) {
					changeOrder.add(e);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Edit Init Read Fail");
		}

		// Create all files for Email
		int fileCount = 0;
		File allEdits, creditEdits, creditCardEdits, changeEdits;
		for (int i = 0; i < 4; i++) {
			switch (i) {
			case 0: // All
				allEdits = createEditFile(all, "all.rtf");
				break;
			case 1: // Credit
				if (!credit.isEmpty()) {
					creditEdits = createEditFile(credit, "credit.rtf");
					fileCount++;
				}
				break;
			case 2: // Credit Card
				if (!creditCard.isEmpty()) {
					creditCardEdits = createEditFile(creditCard, "credit card.rtf");
					fileCount++;
				}
				break;
			case 3: // Change Order
				if (!changeOrder.isEmpty()) {
					changeEdits = createEditFile(changeOrder, "change order.rtf");
					fileCount++;
				}
				break;
			}
		}

		// Email Files and Text
		String subject = "Edits Sent to Credit " + sdf.format(new Date());
		String recipient = "ar@mariettadrapery.com";
		String ccRecipient = "jedwards@mariettadrapery.com";
		String[][] attachments = new String[fileCount][2];

		// Body Constructor
		fileCount = 0;
		String body = "";
		body = body + "Edits Sent to Credit:" + "\n";
		if (credit.isEmpty()) {
			body = body + "none" + "\n";
		} else {
			for (Edit e : credit) {
				String eout = e.getCustomerID() + "     " + e.getBillOne() + "        " + e.getCutNumber();
				body = body + eout + "\n";
			}
			attachments[fileCount][0] = "credit.rtf";
			attachments[fileCount][1] = "credit.rtf";
			fileCount++;
		}
		body = body + "\n";
		body = body + "Credit Card:" + "\n";
		if (creditCard.isEmpty()) {
			body = body + "none" + "\n";
		} else {
			for (Edit e : creditCard) {
				String eout = e.getCustomerID() + "     " + e.getBillOne() + "        " + e.getCutNumber();
				body = body + eout + "\n";
			}
			attachments[fileCount][0] = "credit card.rtf";
			attachments[fileCount][1] = "credit card.rtf";
			fileCount++;
		}
		body = body + "\n";
		body = body + "Change Orders:" + "\n";
		if (changeOrder.isEmpty()) {
			body = body + "none" + "\n";
		} else {
			for (Edit e : changeOrder) {
				String eout = e.getCustomerID() + "     " + e.getBillOne() + "        " + e.getCutNumber();
				body = body + eout + "\n";
			}
			attachments[fileCount][0] = "change order.rtf";
			attachments[fileCount][1] = "change order.rtf";
			fileCount++;
		}

		editEmailer.sendEmailWithAttachment(recipient, ccRecipient, subject, body, attachments);
	}

	private File createEditFile(ArrayList<Edit> editList, String name) {
		if (!editList.isEmpty()) {
			File editFile = new File(name);
			try {
				PrintWriter pw = new PrintWriter(editFile);
				for (Edit e : editList) {
					pw.write(e.getEditText() + (char) 12);
				}
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("look in EDIT HANDLER");
			}
			return editFile;
		}
		return null;
	}

	private class EditListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			File tempFile = new File(editFilePath);
			long tempTimeStamp = tempFile.lastModified();
			if (tempTimeStamp != currentEditTimeStamp) {
				currentEditFile = new File(editFilePath);
				currentEditTimeStamp = currentEditFile.lastModified();

				parseEdits(tempFile);

			}

		}

	}

}
