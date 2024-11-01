/** 
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 * 
 */

package com.example.myapplet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.security.AESKey;
import javacard.security.KeyBuilder;
import javacardx.crypto.Cipher;
import javacardx.framework.nio.Buffer;

/**
 * Applet class
 * 
 * @author <user>
 */

public class myapplet extends Applet {
	final static byte INS_SET_TARGET_DATA = (byte) 0x30;
	final static byte INS_COMPARE_DATA = (byte) 0x10;
	final static byte INS_ENCRYPT_DATA = (byte) 0x20;

	private byte[] targetData;
	private byte[] encryptedData;

	private AESKey aesKey;
	private Cipher aesCipher;



	// Độ dài khối AES (16 byte)
	private static final short AES_BLOCK_SIZE = 16;

	/**
	 * Installs this applet.
	 * 
	 * @param bArray  the array containing installation parameters
	 * @param bOffset the starting offset in bArray
	 * @param bLength the length in bytes of the parameter data in bArray
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new myapplet();
	}

	/**
	 * Only this class's install method should create the applet object.
	 */
	protected myapplet() {
		targetData = new byte[9];
		encryptedData = new byte[AES_BLOCK_SIZE];
		aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
		// Đặt khóa AES với 16 byte
		byte[] keyData = { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
				(byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
				(byte) 0x10 };
		aesKey.setKey(keyData, (short) 0);

		// start AES/ECB/noPadding
		aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);

		register();
	}

	/**
	 * Processes an incoming APDU.
	 * 
	 * @see APDU
	 * @param apdu the incoming APDU
	 */
	@Override
	public void process(APDU apdu) {
		byte[] buffer = apdu.getBuffer();

		if (selectingApplet()) {
			return;
		}

		byte ins = buffer[ISO7816.OFFSET_INS];
		switch (ins) {
		case INS_SET_TARGET_DATA:
			setTargetData(apdu, buffer);
			break;
		case INS_COMPARE_DATA:
			handleCompareData(apdu, buffer);
			break;
		case INS_ENCRYPT_DATA:
			encryptTargetData(apdu, buffer);
			break;

		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	private void setTargetData(APDU apdu, byte[] buffer) {
		short bytesRead = apdu.setIncomingAndReceive();
		if (bytesRead != 9) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, targetData, (short) 0, bytesRead);
	}

	private void handleCompareData(APDU apdu, byte[] buffer) {
		short bytesRead = apdu.setIncomingAndReceive();
		if (bytesRead != 9) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		boolean isMatch = Util.arrayCompare(buffer, ISO7816.OFFSET_CDATA, targetData, (short) 0, bytesRead) == 0;

		apdu.setOutgoing();
		apdu.setOutgoingLength((short) 1);
		buffer[0] = (byte) (isMatch ? 0x01 : 0x00);
		apdu.sendBytes((short) 0, (short) 1);
	}

	private void encryptTargetData(APDU apdu, byte[] buffer) {
		short bytesRead = apdu.setIncomingAndReceive();
		short paddedLength = (short) ((bytesRead + 15) / 16 * 16);
		byte paddingValue = (byte) (AES_BLOCK_SIZE - paddedLength);
		Util.arrayCopyNonAtomic(targetData, (short) 0, encryptedData, (short) 0, (short) targetData.length);

		for (short i = bytesRead; i < paddedLength; i++) {
			encryptedData[i] = paddingValue;
		}

		// Mã hóa dữ liệu đã padding
		aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
		aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, paddedLength, buffer, (short) 0);

		// Gửi dữ liệu mã hóa về cho client
		apdu.setOutgoing();
		apdu.setOutgoingLength(paddedLength);
		apdu.sendBytes((short) 0, paddedLength);
	}

}
