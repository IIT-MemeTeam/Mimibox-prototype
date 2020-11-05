import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioListener {

	private Mixer.Info selectedInputInfo;
	private Mixer.Info selectedOutputInfo;
	private AudioFormat selectedFormat;
	private boolean listeningThreadRunning = true;
	private TargetDataLine microphone = null;
	private SourceDataLine speakers = null;
	private int listenLength;

	public AudioListener() {
		System.out.println("AudioListener starting...");
		selectOptions();

		Listen();

		System.out.println();
		System.out.println("AudioListener stopping...");
	}

	private void selectOptions() {
		Scanner scanner = new Scanner(System.in);
		// clearConsole();
		System.out.println();
		selectMicrophone(scanner);
		Utils.clearConsole();
		selectSpeakers(scanner);
		Utils.clearConsole();
		selectFormat(scanner);
		Utils.clearConsole();
		selectTimespan(scanner);
	}

	private void selectTimespan(Scanner scanner) {
		System.out.println();
		System.out.println("How many seconds would you like to record for?");
		this.listenLength = Integer.parseInt(scanner.nextLine());

	}

	private void selectMicrophone(Scanner scanner) {
		int selectedIndex = -1;
		Mixer.Info[] inputs = getAllLinesOfType(TargetDataLine.class);

		for (int i = 0; i < inputs.length; i++) {
			System.out.println("[" + i + "]\tLine Name: " + inputs[i].getName());
			System.out.println("\tLine Description: " + inputs[i].getDescription());
		}

		System.out.println("Select an input source: ");
		selectedIndex = Integer.parseInt(scanner.nextLine());
		while (selectedIndex < 0 && selectedIndex > inputs.length) {
			System.out.println("Please select a valid output:");
			selectedIndex = Integer.parseInt(scanner.nextLine());
		}

		System.out.println("Selected " + inputs[selectedIndex].getName());
		System.out.println();
		this.selectedInputInfo = inputs[selectedIndex];
	}

	private void selectSpeakers(Scanner scanner) {
		int selectedIndex = -1;
		Mixer.Info[] outputs = getAllLinesOfType(SourceDataLine.class);

		for (int i = 0; i < outputs.length; i++) {
			System.out.println("[" + i + "]\tLine Name: " + outputs[i].getName());
			System.out.println("\tLine Description: " + outputs[i].getDescription());
		}

		System.out.println("Select an output target: ");
		selectedIndex = Integer.parseInt(scanner.nextLine());
		while (selectedIndex < 0 && selectedIndex > outputs.length) {
			System.out.println("Please select a valid output:");
			selectedIndex = Integer.parseInt(scanner.nextLine());
		}

		System.out.println("Selected " + outputs[selectedIndex].getName());
		System.out.println();
		this.selectedOutputInfo = outputs[selectedIndex];
	}

	private <T> Mixer.Info[] getAllLinesOfType(T audioType) {
		ArrayList<Mixer.Info> results = new ArrayList<Mixer.Info>();
		Mixer.Info[] allMixers = AudioSystem.getMixerInfo();

		for (Mixer.Info info : allMixers) {

			Line.Info[] lineInfos = null;

			if (audioType.equals(SourceDataLine.class)) {
				lineInfos = AudioSystem.getMixer(info).getSourceLineInfo();
			} else {
				lineInfos = AudioSystem.getMixer(info).getTargetLineInfo();
			}
			if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(audioType)) {// Only prints out
				results.add(info);
			}
		}
		Mixer.Info[] resultsArr = new Mixer.Info[results.size()];
		return results.toArray(resultsArr);
	}

	private void selectFormat(Scanner scanner) {
		int selectedIndex = -1;
		Mixer micMixer = AudioSystem.getMixer(this.selectedInputInfo);

		printSupportedFormats(micMixer);

		System.out.println("Select a format: ");
		selectedIndex = Integer.parseInt(scanner.nextLine());
		if (selectedIndex != -1) {
			//Mixer.Info selectedMixerInfo = AudioSystem.getMixerInfo()[selectedIndex];
			AudioFormat selectedFormat = ((DataLine.Info) micMixer.getTargetLineInfo()[0]).getFormats()[selectedIndex];
			System.out.println("Selected " + selectedFormat.toString());
			System.out.println();
			this.selectedFormat = selectedFormat;
		}
	}

	private void printSupportedFormats(Mixer micMixer) {
		Line.Info[] targetLineInfo = micMixer.getTargetLineInfo();
		int i = 0;
		for (Line.Info info : targetLineInfo) {
			if (info instanceof DataLine.Info) {
				DataLine.Info dataLineInfo = (DataLine.Info) info;

				AudioFormat[] formats = dataLineInfo.getFormats();
				for (final AudioFormat format : formats) {
					System.out.println("[" + i + "]\t" + format.toString());
					i++;
				}
			}
		}
	}

	private void Listen() {
		AudioFormat format = this.selectedFormat;

		try {
			// Speaker
			Mixer speakerMixer = AudioSystem.getMixer(this.selectedOutputInfo);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			speakers = (SourceDataLine) speakerMixer.getLine(info);
			speakers.open();

			// Microphone
			Mixer micMixer = AudioSystem.getMixer(this.selectedInputInfo);
			info = new DataLine.Info(TargetDataLine.class, format);
			microphone = (TargetDataLine) micMixer.getLine(info);
			microphone.open();

			Thread monitorThread = new Thread() {
				@Override
				public void run() {
					microphone.start();
					speakers.start();

					byte[] data = new byte[microphone.getBufferSize() / 5];
					int readBytes;

					while (listeningThreadRunning) {
						readBytes = microphone.read(data, 0, data.length);
						speakers.write(data, 0, readBytes);
						//printMicOutput(data);
					}
				}
			};

			System.out.println("Start LIVE Monitor for " + this.listenLength + " seconds");
			monitorThread.start();

			TimeUnit.SECONDS.sleep(listenLength);
			listeningThreadRunning = false;
			microphone.stop();
			microphone.close();
			System.out.println("End LIVE Monitor");
			
		} catch (Exception e) {
			System.out.println("Error occured while listening:");
			e.printStackTrace();
		} finally {
			if (speakers != null) {
				speakers.drain();
				speakers.close();
				microphone.close();
			}
		}
	}

	private void printMicOutput(byte[] data) {
		// clearConsole();
		// System.out.println("Listening...\n\n");

		int j = 0;
		while (j < data.length) {
			System.out.print(data[j] + " ");
			j++;
		}
		// System.out.println();
	}
}
