/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elster.ek2xx;

import java.io.IOException;

public class EK2xxAarq {

	private static final byte AARE_APPLICATION_CONTEXT_NAME = (byte) 0xA1;
	private static final byte AARE_RESULT = (byte) 0xA2;
	private static final byte AARE_RESULT_SOURCE_DIAGNOSTIC = (byte) 0xA3;
	private static final byte AARE_USER_INFORMATION = (byte) 0xBE;

	private static final byte AARE_TAG = 0x61;

	private static final byte ACSE_SERVICE_USER = (byte) 0xA1;
	private static final byte ACSE_SERVICE_PROVIDER = (byte) 0xA2;

	private static final byte DLMS_PDU_INITIATE_RESPONSE = (byte) 0x08;
	private static final byte DLMS_PDU_CONFIRMED_SERVICE_ERROR = (byte) 0x0E;

	private EK2xx ek2xx = null;

	private static final byte[] AARQ_LOW_LEVEL = { (byte) 0xE6, (byte) 0xE6, (byte) 0x00, (byte) 0x60, (byte) 0x35, (byte) 0xA1, (byte) 0x09, (byte) 0x06,
		(byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01,
		(byte) 0x02, //application context name
		(byte) 0x8A, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0x8B, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08,
		(byte) 0x02, (byte) 0x01 };

	private static final byte[] AARQ_LOW_LEVEL_2 = { (byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
		(byte) 0x06, // dlms version nr
		(byte) 0x5F, (byte) 0x04, (byte) 0x00, (byte) 0x18, (byte) 0x02, (byte) 0x20, (byte) 0xFF, (byte) 0xFF };

	private static final byte[] AARQ_LOWEST_LEVEL = { (byte) 0xE6, (byte) 0xE6, (byte) 0x00,
		(byte) 0x60,
		(byte) 0x1C, // bytes to follow
		(byte) 0xA1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x60, (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08, (byte) 0x01, (byte) 0x02,
		(byte) 0xBE, (byte) 0x0F, (byte) 0x04, (byte) 0x0D, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x04,
		(byte) 0x00, (byte) 0x18, (byte) 0x02, (byte) 0x20, (byte) 0xFF, (byte) 0xFF };

	/*
	 * Constructors
	 */

	public EK2xxAarq(EK2xx ek2xx) {
		this.ek2xx = ek2xx;
	}

	/*
	 * Private getters, setters and methods
	 */

	private String getPassword() {
		return getEk2xx().getPassword();
	}

	private EK2xx getEk2xx() {
		return this.ek2xx;
	}

	private byte[] getLowLevelSecurity() {
		return buildaarq(AARQ_LOW_LEVEL, AARQ_LOW_LEVEL_2);
	}

	private byte[] buildaarq(byte[] aarq1, byte[] aarq2) {
		byte[] aarq = null;
		int i, t = 0;

		// prepare aarq buffer and copy aarq1 to aarq buffer
		aarq = new byte[3 + aarq1.length + 1 + getPassword().length() + aarq2.length];
		for (i = 0; i < aarq1.length; i++) {
			aarq[t++] = aarq1[i];
		}

		// calling authentification
		aarq[t++] = (byte) 0xAC; // calling authentification tag
		aarq[t++] = (byte) (getPassword().length() + 2); // length to follow
		aarq[t++] = (byte) 0x80; // tag representation

		// copy password to aarq buffer
		aarq[t++] = (byte) getPassword().length();
		for (i = 0; i < getPassword().length(); i++) {
			aarq[t++] = (byte) getPassword().charAt(i);
		}

		//copy in aarq2 to aarq buffer
		for (i = 0; i < aarq2.length; i++) {
			aarq[t++] = aarq2[i];
		}

		aarq[4] = (byte) ((aarq.length & 0xFF) - 5); // Total length of frame - headerlength

		return aarq;
	}

	private void doRequestApplAssoc(byte[] aarq) throws IOException {
		byte[] responseData;
		responseData = getEk2xx().getDLMSConnection().sendRequest(aarq);
		CheckAARE(responseData);
	}

	/*
	 * Public methods
	 */

	public void requestApplAssoc(int iLevel) throws IOException {
		byte[] aarq;

		if (iLevel == 0) {
			aarq = AARQ_LOWEST_LEVEL;
		} else if (iLevel == 1) {
			aarq = getLowLevelSecurity();
		} else {
			aarq = getLowLevelSecurity();
		}

		doRequestApplAssoc(aarq);

	}

	private void CheckAARE(byte[] responseData) throws IOException {
		int i;
		String strResultSourceDiagnostics = "";

		i = 0;
		while (true) {
			if (responseData[i] == AARE_TAG) {
				i += 2; // skip tag & length
				while (true) {
					if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME) {
						i++; // skip tag
						i += responseData[i]; // skip length + data
					} else if (responseData[i] == AARE_RESULT) {
						i++; // skip tag
						if ((responseData[i] == 3) && (responseData[i + 1] == 2) && (responseData[i + 2] == 1) && (responseData[i + 3] == 0)) {
							// Result OK
							return;
						}
						i += responseData[i]; // skip length + data
					} else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC) {
						i++; // skip tag
						if (responseData[i] == 5) {// check length
							if (responseData[i + 1] == ACSE_SERVICE_USER) {
								if ((responseData[i + 2] == 3) && (responseData[i + 3] == 2) && (responseData[i + 4] == 1)) {
									if (responseData[i + 5] == 0x00) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
									} else if (responseData[i + 5] == 0x01) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
									} else if (responseData[i + 5] == 0x02) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
									} else if (responseData[i + 5] == 0x0B) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
									} else if (responseData[i + 5] == 0x0C) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
									} else if (responseData[i + 5] == 0x0D) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
									} else if (responseData[i + 5] == 0x0E) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
									} else {
										throw new IOException("Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
									}
								} else {
									throw new IOException(
									"Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
								}
							} else if (responseData[i + 1] == ACSE_SERVICE_PROVIDER) {
								if ((responseData[i + 2] == 3) && (responseData[i + 3] == 2) && (responseData[i + 4] == 1)) {
									if (responseData[i + 5] == 0x00) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER!";
									} else if (responseData[i + 5] == 0x01) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Reason Given!";
									} else if (responseData[i + 5] == 0x02) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
									} else {
										throw new IOException("Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
									}
								} else {
									throw new IOException(
									"Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
								}
							} else {
								throw new IOException("Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
							}
						} else {
							throw new IOException("Application Association Establishment Failed, result_source_diagnostic, wrong length");
						}
						i += responseData[i]; // skip length + data
					} else if (responseData[i] == AARE_USER_INFORMATION) {
						i++; // skip tag
						if (DLMS_PDU_INITIATE_RESPONSE == responseData[i + 3]) {
							// Do nothing
						} else if (DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i + 3]) {
							if (0x01 == responseData[i + 4]) {
								strResultSourceDiagnostics += ", InitiateError";
							} else if (0x02 == responseData[i + 4]) {
								strResultSourceDiagnostics += ", getStatus";
							} else if (0x03 == responseData[i + 4]) {
								strResultSourceDiagnostics += ", getNameList";
							} else if (0x13 == responseData[i + 4]) {
								strResultSourceDiagnostics += ", terminateUpload";
							} else {
								throw new IOException(
								"Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");
							}

							if (0x06 != responseData[i + 5]) {
								strResultSourceDiagnostics += ", No ServiceError tag";
							}

							if (0x00 == responseData[i + 6]) {
								strResultSourceDiagnostics += "";
							} else if (0x01 == responseData[i + 6]) {
								strResultSourceDiagnostics += ", DLMS version too low";
							} else if (0x02 == responseData[i + 6]) {
								strResultSourceDiagnostics += ", Incompatible conformance";
							} else if (0x03 == responseData[i + 6]) {
								strResultSourceDiagnostics = ", pdu size too short";
							} else if (0x04 == responseData[i + 6]) {
								strResultSourceDiagnostics = ", refused by the VDE handler";
							} else {
								throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
							}
						} else {
							throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
						}

						i += responseData[i]; // skip length + data
					} else {
						i++; // skip tag
						// Very tricky, suppose we receive a length > 128 because of corrupted data,
						// then if we keep byte, it is signed and we can enter a LOOP because length will
						// be subtracted from i!!!
						i += ((responseData[i]) & 0x000000FF); // skip length + data
					}

					if (i++ >= (responseData.length - 1)) {
						i = (responseData.length - 1);
						break;
					}
				}
			}

			if (i++ >= (responseData.length - 1)) {
				i = (responseData.length - 1);
				break;
			}
		}

		throw new IOException("Application Association Establishment Failed" + strResultSourceDiagnostics);

	}

}
