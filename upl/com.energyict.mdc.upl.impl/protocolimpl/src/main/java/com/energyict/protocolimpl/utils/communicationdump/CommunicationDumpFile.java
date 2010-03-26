package com.energyict.protocolimpl.utils.communicationdump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommunicationDumpFile {

	private List<CommunicationDumpEntry> entries = new ArrayList<CommunicationDumpEntry>();

	public CommunicationDumpFile(String fileName) {
		this(new File(fileName));
	}

	public CommunicationDumpFile(File file) {
		readEntriesFromFile(file);
	}

	private void readEntriesFromFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			int sequenceNumber = 0;
			while (reader.ready()) {
				String entryLine = reader.readLine();
				if (entryLine != null) {
					CommunicationDumpEntry entry = CommunicationDumpEntry.getEntryFromString(entryLine, sequenceNumber++);
					if (entry != null) {
						entries.add(entry);
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<CommunicationDumpEntry> getEntries() {
		return entries;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		List entryList = getEntries();
		for (Iterator iterator = entryList.iterator(); iterator.hasNext();) {
			CommunicationDumpEntry entry = (CommunicationDumpEntry) iterator.next();
			if (entry != null) {
				sb.append(entry);
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public List<CommunicationDumpEntry> getRxEntries() {
		List<CommunicationDumpEntry> rxEntries = new ArrayList<CommunicationDumpEntry>();
		for (CommunicationDumpEntry cde : entries) {
			if (cde.isRx()) {
				rxEntries.add(cde);
			}
		}
		return rxEntries;
	}

	public List<CommunicationDumpEntry> getTxEntries() {
		List<CommunicationDumpEntry> txEntries = new ArrayList<CommunicationDumpEntry>();
		for (CommunicationDumpEntry cde : entries) {
			if (cde.isTx()) {
				txEntries.add(cde);
			}
		}
		return txEntries;
	}

}
