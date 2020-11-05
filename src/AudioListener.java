import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;
import javax.naming.NameNotFoundException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioListener {

	private Mixer.Info currentInputInfo;
	private Mixer.Info currentOutputInfo;
	private AudioFormat currentAudioFormat;
	private boolean listeningThreadRunning = true;
	private TargetDataLine input = null;
	private SourceDataLine output = null;
	private int listenLength;

	public AudioListener() {
		setDefaultConfig();
	}

	private void setDefaultConfig() {
		Mixer.Info[] inputs = getAllLinesOfType(TargetDataLine.class);

		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i].getName().toLowerCase().contains("default")) {
				this.currentInputInfo = inputs[i];
			}
		}

		Mixer.Info[] outputs = getAllLinesOfType(SourceDataLine.class);

		for (int i = 0; i < inputs.length; i++) {
			if (outputs[i].getName().toLowerCase().contains("default")) {
				this.currentOutputInfo = outputs[i];
			}
		}

		if (hasConfigInput()) {
			AudioFormat[] formats = getAllAudioFormatTypes();
			if (formats.length >= 0) {
				this.currentAudioFormat = formats[0];
			}
		}
	}

//	private void selectTimespan(Scanner scanner) {
//		System.out.println();
//		System.out.println("How many seconds would you like to record for?");
//		this.listenLength = Integer.parseInt(scanner.nextLine());
//	}

	public boolean hasConfigInput() {
		return this.currentInputInfo != null;
	}

	public String getCurrentInputName() {
		if (this.currentInputInfo == null) {
			return "None Set";
		}

		return this.currentInputInfo.getName();
	}

	public String getCurrentOutputName() {
		if (this.currentOutputInfo == null) {
			return "None Set";
		}
		return this.currentOutputInfo.getName();
	}

	public String getCurrentFormat() {
		if (this.currentAudioFormat == null) {
			return "None Set";
		}
		return this.currentAudioFormat.toString();
	}

	public String[] getAvailableInputs() {
		Mixer.Info[] allInputInfos = getAllLinesOfType(TargetDataLine.class);

		String[] results = new String[allInputInfos.length];

		for (int i = 0; i < results.length; i++) {
			String name = allInputInfos[i].getName();
			String description = allInputInfos[i].getDescription();
			results[i] = name + "," + description;
		}
		return results;
	}

	public String[] getAvailableOutputs() {
		Mixer.Info[] allOutputInfos = getAllLinesOfType(SourceDataLine.class);

		String[] results = new String[allOutputInfos.length];

		for (int i = 0; i < results.length; i++) {
			String name = allOutputInfos[i].getName();
			String description = allOutputInfos[i].getDescription();
			results[i] = name + "," + description;
		}
		return results;
	}

	public void setInput(String name) throws NameNotFoundException {
		Mixer.Info[] inputInfos = getAllLinesOfType(TargetDataLine.class);
		boolean isFound = false;

		int i = 0;
		while (i < inputInfos.length && isFound == false) {
			if (inputInfos[i].getName().equals(name)) {
				this.currentInputInfo = inputInfos[i];
				isFound = true;
			}
			i++;
		}

		throw new NameNotFoundException("Input device '" + name + "' could not be found");
	}

	public void setOutput(String name) throws NameNotFoundException {
		Mixer.Info[] inputInfos = getAllLinesOfType(SourceDataLine.class);
		boolean isFound = false;

		int i = 0;
		while (i < inputInfos.length && isFound == false) {
			if (inputInfos[i].getName().equals(name)) {
				this.currentOutputInfo = inputInfos[i];
				isFound = true;
			}
			i++;
		}

		throw new NameNotFoundException("Output device '" + name + "' could not be found");
	}
	
	public void setFormat(String format) throws ConfigurationException, NameNotFoundException {
		if (!hasConfigInput()) {
			throw new ConfigurationException("Input source needs to be set first");
		} else {
			AudioFormat[] formats = getAllAudioFormatTypes();
			boolean isFound = false;
			
			int i = 0;
			while (i < formats.length && isFound == false) {
				if (formats[i].toString().equals(format)) {
					this.currentAudioFormat = formats[i];
					isFound = true;
				}
				i++;
			}
			
			throw new NameNotFoundException("Audio Format '" + format + "' could not be found");
		}
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

	public String[] getAvailableFormats() {
		AudioFormat[] formats = getAllAudioFormatTypes();
		String[] result = new String[formats.length];
		
		int i = 0;
		while (i < result.length) {
			result[i]= formats[i].toString(); 
			i++;
		}
		
		return result;
	}

	private AudioFormat[] getAllAudioFormatTypes() {
		ArrayList<AudioFormat> formats = new ArrayList<AudioFormat>();

		if (hasConfigInput()) {
			Mixer mixer = AudioSystem.getMixer(this.currentInputInfo);
			Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
			
			int i = 0;
			for (Line.Info info : targetLineInfo) {
				if (info instanceof DataLine.Info) {
					DataLine.Info dataLineInfo = (DataLine.Info) info;

					AudioFormat[] lineFormats = dataLineInfo.getFormats();
					for (final AudioFormat format : lineFormats) {
						formats.add(format);
						i++;
					}
				}
			}
		}
		AudioFormat[] result = new AudioFormat[formats.size()];
		return formats.toArray(result);
	}

	private void Listen() {
		AudioFormat format = this.currentAudioFormat;

		try {
			// Speaker
			Mixer speakerMixer = AudioSystem.getMixer(this.currentOutputInfo);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			output = (SourceDataLine) speakerMixer.getLine(info);
			output.open();

			// Microphone
			Mixer micMixer = AudioSystem.getMixer(this.currentInputInfo);
			info = new DataLine.Info(TargetDataLine.class, format);
			input = (TargetDataLine) micMixer.getLine(info);
			input.open();

			Thread monitorThread = new Thread() {
				@Override
				public void run() {
					input.start();
					output.start();

					byte[] data = new byte[input.getBufferSize() / 5];
					int readBytes;

					while (listeningThreadRunning) {
						readBytes = input.read(data, 0, data.length);
						output.write(data, 0, readBytes);
						// printMicOutput(data);
					}
				}
			};

			System.out.println("Start LIVE Monitor for " + this.listenLength + " seconds");
			monitorThread.start();

			TimeUnit.SECONDS.sleep(listenLength);
			listeningThreadRunning = false;
			input.stop();
			input.close();
			System.out.println("End LIVE Monitor");

		} catch (Exception e) {
			System.out.println("Error occured while listening:");
			e.printStackTrace();
		} finally {
			if (output != null) {
				output.drain();
				output.close();
				input.close();
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
