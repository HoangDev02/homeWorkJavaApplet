package com.oracle.javacard.sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.IntStream;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import com.oracle.javacard.ams.AMService;
import com.oracle.javacard.ams.AMServiceFactory;
import com.oracle.javacard.ams.AMSession;
import com.oracle.javacard.ams.config.AID;
import com.oracle.javacard.ams.config.CAPFile;
import com.oracle.javacard.ams.script.APDUScript;
import com.oracle.javacard.ams.script.ScriptFailedException;
import com.oracle.javacard.ams.script.Scriptable;

public class AMSMyApplet {

	static final String isdAID = "aid:A000000151000000";
	static final String sAID_CAP = "aid:a00000006203010C01";
	static final String sAID_AppletClass = "aid:a00000006203010C0101";
	static final String sAID_AppletInstance = "aid:a00000006203010C0101";
	static final CommandAPDU selectApplet = new CommandAPDU(0x00, 0xA4, 0x04, 0x00,
			AID.from(sAID_AppletInstance).toBytes(), 256);

	private static final byte INS_SET_TARGET_DATA = (byte) 0x30;
	private static final byte INS_COMPARE_DATA = (byte) 0x10;
	private static final byte INS_ENCRYPT_DATA = (byte) 0x20;

	
	public static void main(String[] args) {
		try {
			CAPFile appFile = CAPFile.from(getArg(args, "cap"));
			Properties props = new Properties();
			props.load(new FileInputStream(getArg(args, "props")));

			AMService ams = AMServiceFactory.getInstance("GP2.2");
			ams.setProperties(props);

			AMSession deploy = ams.openSession(isdAID).load(sAID_CAP, appFile.getBytes())
					.install(sAID_CAP, sAID_AppletClass, sAID_AppletInstance).close();
			AMSession undeploy = ams.openSession(isdAID).uninstall(sAID_AppletInstance).unload(sAID_CAP).close();

			Scanner scanner = new Scanner(System.in);

			String inputApplet;
			do {
				System.out.print("Enter Input Applet (must be 9 characters): ");
				inputApplet = scanner.nextLine();

				if (inputApplet.length() != 9) {
					System.out.println("Error: Input must be exactly 9 characters long.");
				}
			} while (inputApplet.length() != 9);

			byte[] inputBytes = inputApplet.getBytes();

			String inputData;
			do {
				System.out.print("Enter Input Client (must be 9 characters): ");
				inputData = scanner.nextLine();

				if (inputData.length() != 9) {
					System.out.println("Error: Input must be exactly 9 characters long.");
				}
			} while (inputData.length() != 9);

			byte[] dataBytes = inputData.getBytes();

			CommandAPDU setTargetDataAPDU = new CommandAPDU(0x80, INS_SET_TARGET_DATA, 0x00, 0x00, inputBytes);

			CommandAPDU compareDataAPDU = new CommandAPDU(0x80, INS_COMPARE_DATA, 0x00, 0x00, dataBytes);
			CommandAPDU encryptCommand = new CommandAPDU(0x80, INS_ENCRYPT_DATA, 0x00, 0x00, inputBytes);
		

			TestScript testScript = new TestScript().append(deploy).append(selectApplet).append(setTargetDataAPDU)
					.append(compareDataAPDU, new ResponseAPDU(new byte[] { 0x01, (byte) 0x90, 0x00 })) // Expected
					.append(encryptCommand).append(undeploy);

			CardTerminal terminal = getTerminal("socket", "127.0.0.1", "9025");
			Card card = terminal.connect("*");
			List<ResponseAPDU> responses = testScript.run(card.getBasicChannel());
			card.disconnect(true);

			System.out.println("Response count: " + responses.size());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | CardException | ScriptFailedException
				| IOException e) {
			e.printStackTrace();
		}
	}

	private static String getArg(String[] args, String argName) throws IllegalArgumentException {
		for (String param : args) {
			if (param.startsWith("-" + argName + "=")) {
				return param.substring(param.indexOf('=') + 1);
			}
		}
		throw new IllegalArgumentException("Argument " + argName + " is missing");
	}

	private static CardTerminal getTerminal(String... connectionParams)
			throws NoSuchAlgorithmException, NoSuchProviderException, CardException {
		TerminalFactory tf;
		String connectivityType = connectionParams[0];
		if (connectivityType.equals("socket")) {
			String ipaddr = connectionParams[1];
			String port = connectionParams[2];
			tf = TerminalFactory.getInstance("SocketCardTerminalFactoryType",
					List.of(new InetSocketAddress(ipaddr, Integer.parseInt(port))), "SocketCardTerminalProvider");
		} else {
			tf = TerminalFactory.getDefault();
		}
		return tf.terminals().list().get(0);
	}

	private static class TestScript extends APDUScript {
		private List<CommandAPDU> commands = new LinkedList<>();
		private List<ResponseAPDU> responses = new LinkedList<>();
		private int index = 0;

		public List<ResponseAPDU> run(CardChannel channel) throws ScriptFailedException {
			return super.run(channel, c -> lookupIndex(c), r -> !isExpected(r));
		}

		@Override
		public TestScript append(Scriptable<CardChannel, CommandAPDU, ResponseAPDU> other) {
			super.append(other);
			return this;
		}

		public TestScript append(CommandAPDU apdu, ResponseAPDU expected) {
			this.commands.add(apdu);
			this.responses.add(expected);
			super.append(apdu);
			return this;
		}

		public TestScript append(CommandAPDU apdu) {
			this.commands.add(apdu);
			this.responses.add(null);
			super.append(apdu);
			return this;
		}

		private CommandAPDU lookupIndex(CommandAPDU apdu) {
//	        print(apdu);
			this.index = IntStream.range(0, this.commands.size()).filter(i -> apdu.equals(this.commands.get(i))) // comparison
					.findFirst().orElse(-1);
			return apdu;
		}

		private static String bytesToHex(byte[] bytes) {
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02X", b));
			}
			return sb.toString();
		}

		private boolean isExpected(ResponseAPDU response) {
			logResponseData(response);
			return true;
		}

		private void logResponseData(ResponseAPDU response) {
			if (index >= 0) {
				switch (commands.get(index).getINS()) {
				case INS_COMPARE_DATA:
					System.out.println("INS_COMPARE_DATA: " + response.equals(responses.get(index)));
					break;
				case INS_ENCRYPT_DATA:
					System.out.println("INS_ENCRYPT_DATA: " + bytesToHex(response.getData()));
					break;
			
				}
			}
		}
	}

}
