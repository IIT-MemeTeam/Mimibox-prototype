import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFileFormat;

public class AudioDataWriter extends AudioObserver {
	private String filePath;
	private AudioInputStream audioInputStream;
	private ByteArrayOutputStream outputStream;

	public AudioDataWriter(String filePath) {
		this.filePath = filePath;

	}

	public void setDataLine(TargetDataLine dataLine) {
		audioInputStream = new AudioInputStream(dataLine);
		outputStream = new ByteArrayOutputStream();
	}

	@Override
	public void onAudioRecieved(byte[] data) {
		// System.out.println(Thread.currentThread().getId());
		try {
			Utils.clearConsole();
			System.out.println("Recieved " + outputStream.size() + " bytes");
			outputStream.write(data);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	@Override
	public void update(LineEvent event) {
		System.out.println("State changed to " + event.getType());
		if (event.getType() == LineEvent.Type.STOP) {
			try {
				writeToFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (event.getType() == LineEvent.Type.START) {
		}
	}

	private void writeToFile() throws IOException {
		FileOutputStream out = null;
		try {
			// TODO: Fix path
			Path path = Paths.get(filePath);
			
			System.out.println("Saving file to '" + path.toAbsolutePath() + "'");
			File file = new File(this.filePath);
			out = new FileOutputStream(file);

			byte[] abAudioData = this.outputStream.toByteArray();

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(abAudioData);
			AudioInputStream outputAIS = new AudioInputStream(byteArrayInputStream, audioInputStream.getFormat(),
					abAudioData.length);

			AudioSystem.write(outputAIS, AudioFileFormat.Type.WAVE, out);
			
			System.out.println("Saved file to '" + file.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}

		}

		System.out.println("Finished writing");
	}
}
