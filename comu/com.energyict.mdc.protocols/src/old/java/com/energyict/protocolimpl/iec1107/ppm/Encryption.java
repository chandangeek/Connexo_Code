/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.protocolimpl.base.Encryptor;

public class Encryption implements Encryptor {

	private static char[] C245HGJ = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public Encryption() {
	}

	private int caiMethod(String ca27, String ca28, char[] ca29) {
		char[] cfG7821 = new char[9];
		char[] k8057hwf = new char[9];
		char[] li6348 = new char[17];
		char cfg873;
		char cfg837;

		for (int i = 0; i < 8; i++) {
			cfG7821[i] = ca27.charAt(i);
		}
		cfG7821[8] = '\0';
		for (int i = 0; i < 16; i += 2) {
			cfg873 = caiMethodB(ca28.charAt(i));
			cfg837 = caiMethodB(ca28.charAt(i + 1));
			int iVal = ((cfg873) * 16) + (cfg837);
			k8057hwf[i / 2] = (char) iVal;
		}
		k8057hwf[8] = '\0';
		for (int i = 0; i < 8; i++) {
			li6348[i] += k8057hwf[i] ^ cfG7821[i];
		}
		li6348[8] = '\0';
		for (int i = 0; i < 8; i++) {
			int c396uia;
			c396uia = li6348[i] + li6348[i + 7];
			c396uia = c396uia % 256;
			li6348[i + 8] = (char) c396uia;
			ca29[i * 2] = C245HGJ[c396uia / 16];
			ca29[(i * 2) + 1] = C245HGJ[c396uia % 16];
		}
		return 1;
	}

	private char caiMethodB(char c786fim) {
		String c612ops = String.valueOf(c786fim);
		c612ops = c612ops.toUpperCase();
		c786fim = c612ops.charAt(0);
		int c871ilg = 0;
		boolean f613idv = false;
		while (!f613idv) {
			if (C245HGJ[c871ilg] == c786fim) {
				f613idv = true;
			} else {
				c871ilg++;
			}
		}
		return (char) c871ilg;
	}

	public String encrypt(String passWord, String key) {
		char[] encryptedPassword = new char[16];
		caiMethod(passWord, key, encryptedPassword);
		return new String(encryptedPassword);
	}

}