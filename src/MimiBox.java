import java.security.Permissions;
import java.util.Scanner;

import javax.sound.sampled.AudioPermission;

public class MimiBox {
	Scanner scanner;
	AudioManager audioManager;

	private boolean wasSecuritySuccessful;

	public MimiBox() {
		scanner = new Scanner(System.in);
		audioManager = new AudioManager();

		setupSecurity();
		showMainMenu();
	}

	private void showMainMenu() {
		String selection = "";
		while (true) {
			Utils.clearConsole();
			printHeader("Mimi-Box Proof of Concept");

			checkSecurity();

			System.out.println();
			System.out.println("(C)onfigure Audio");
			System.out.println("(S)tart Audio Listener");
			System.out.println("(Q)uit");
			System.out.println("Select an option:");

			selection = scanner.nextLine();

			switch (selection.charAt(0)) {
			case 'C':
			case 'c': // Configure Audio
				configureAudio();
				break;
			case 'S':
			case 's': // Start audio listener
				startListener();
				break;
			case 'Q':
			case 'q': // Quit
				quit();
				return;
			default:
				System.out.println("Invalid selection. Try again.");
				break;
			}
		}
	}

	private void configureAudio() {
		String selection = " ";

		while (selection.charAt(0) != 'b' && selection.charAt(0) != 'B') {

			Utils.clearConsole();
			printHeader("Configure Audio");
			checkSecurity();

			System.out.println("Current Configuration:");
			System.out.println("   Input:  " + this.audioManager.getCurrentInputName());
			System.out.println("   Output: " + this.audioManager.getCurrentOutputName());
			System.out.println("   Format: " + this.audioManager.getCurrentFormat());
			System.out.println();

			System.out.println();

			System.out.println("Config (I)nput");
			System.out.println("Config (O)utput");
			System.out.println("Config Audio (F)ormat");
			System.out.println("(B)ack");
			System.out.println("Select an option:");

			selection = scanner.nextLine();

			switch (selection.charAt(0)) {
			case 'I':
			case 'i': // Input
				configInput();
				break;
			case 'O':
			case 'o': // Output
				configOutput();
				break;
			case 'F':
			case 'f': // Format
				configFormat();
				break;
			case 'B':
			case 'b':
				break;
			default:
				System.out.println("Invalid selection. Try again.");
				break;
			}
		}
	}

	private void configInput() {
		int selectedIndex = -1;
		String[] inputs = this.audioManager.getAvailableInputs();

		for (int i = 0; i < inputs.length; i++) {
			String[] info = inputs[i].split(",");
			System.out.println("[" + i + "]\tLine Name: " + info[0]);
			System.out.println("\tLine Description: " + info[1]);
		}

		System.out.println("Select an input source or -1 to go back: ");
		selectedIndex = Integer.parseInt(scanner.nextLine());
		while (selectedIndex < -1 && selectedIndex > inputs.length) {
			System.out.println("Please select a valid output:");
			selectedIndex = Integer.parseInt(scanner.nextLine());

			if (selectedIndex == -1) {
				break;
			}
		}

		if (selectedIndex != -1) {
			String inputName = inputs[selectedIndex].split(",")[0];
			System.out.println("Selected " + inputName);
			System.out.println();
			try {
				this.audioManager.setInput(inputName);
			} catch (Exception e) {
				System.out.println("Error!\n " + e.getMessage());
			}
		}
	}

	private void configOutput() {
		int selectedIndex = -1;
		String[] outputs = this.audioManager.getAvailableOutputs();

		for (int i = 0; i < outputs.length; i++) {
			String[] info = outputs[i].split(",");
			System.out.println("[" + i + "]\tLine Name: " + info[0]);
			System.out.println("\tLine Description: " + info[1]);
		}

		System.out.println("Select an output target or -1 to go back: ");
		selectedIndex = Integer.parseInt(scanner.nextLine());
		while (selectedIndex < -1 && selectedIndex > outputs.length) {
			System.out.println("Please select a valid output:");
			selectedIndex = Integer.parseInt(scanner.nextLine());

			if (selectedIndex == -1) {
				break;
			}
		}
		if (selectedIndex != -1) {
			String outputName = outputs[selectedIndex].split(",")[0];
			System.out.println("Selected " + outputName);
			System.out.println();
			try {
				this.audioManager.setOutput(outputName);
			} catch (Exception e) {
				System.out.println("Error!\n " + e.getMessage());
			}
		}
	}

	private void configFormat() {
		int selectedIndex = -1;
		if (this.audioManager.hasConfigInput()) {
			String[] formats = this.audioManager.getAvailableFormats();

			for (int i = 0; i < formats.length; i++) {
				System.out.println("[" + i + "] " + formats[i].toString());
			}

			System.out.println("Select a format: ");
			selectedIndex = Integer.parseInt(scanner.nextLine());
			while (selectedIndex < -1 && selectedIndex > formats.length) {
				System.out.println("Please select a valid output:");
				selectedIndex = Integer.parseInt(scanner.nextLine());

				if (selectedIndex == -1) {
					break;
				}
			}
			if (selectedIndex != -1) {
				System.out.println("Selected " + formats[selectedIndex]);
				System.out.println();
				try {
					this.audioManager.setFormat(formats[selectedIndex]);
				} catch (Exception e) {
					System.out.println("Error!\n " + e.getMessage());
				}
			}
		} else {
			System.out.println("Select an input source before attempting to set the audio format");
		}
	}

	private void startListener() {
		System.out.println("Start listener");
		System.out.println("How long in seconds should it listen for?");
		int seconds = Integer.parseInt(scanner.nextLine());
		try {

			this.audioManager.startListening(seconds);
			this.audioManager.listenerThread.addAudioListener(new AudioDataWriter(""));
			
			Thread.sleep(5);
			while (this.audioManager.listenerThread.isRunning()) { // TODO: change scope
				Thread.sleep(10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupSecurity() {
		AudioPermission record = new AudioPermission("record");
		AudioPermission play = new AudioPermission("record");
		Permissions permissions = new Permissions();
		permissions.add(play);
		permissions.add(record);

		try {
			if (System.getSecurityManager() == null) {
				SecurityManager secMan = new SecurityManager();
				System.setSecurityManager(secMan);
				secMan.checkPermission(record);
				wasSecuritySuccessful = true;
			}
		} catch (Exception e) {
			this.wasSecuritySuccessful = false;
		}
	}

	private void checkSecurity() {
		if (!wasSecuritySuccessful) {
			System.err.println("Failed to set security policies. Microphone and/or speakers may not be accessable.");
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void printHeader(String title) {
		System.out.println("-================================-");
		System.out.println("  " + title);
		System.out.println("-================================-");
	}

	private void quit() {
		System.out.println("Exiting...");
		this.audioManager.stopListening();
		System.exit(0);
	}

	public static void main(String[] args) {
		MimiBox mimiBox = new MimiBox();
	}

}
