import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.TargetDataLine;
import javax.sound.midi.SysexMessage;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.spi.AudioFileWriter;

import org.omg.CORBA.PUBLIC_MEMBER;

public class AudioDataWriter extends AudioObserver {
	private String filePath;
	private AudioInputStream audioInputStream;
	private ByteArrayOutputStream outputStream;
	private int bufferSize;
	private AudioInputStream inputStream;

	public AudioDataWriter(String filePath) {
		this.filePath = filePath;

	}

	public void setDataLine(TargetDataLine dataLine) {
		audioInputStream = new AudioInputStream(dataLine);
		outputStream = new ByteArrayOutputStream();
	}

	@Override
	public void onAudioRecieved(byte[] data) {
		//System.out.println(Thread.currentThread().getId());
		try {
			System.out.println(outputStream.size());
			//System.out.println(this.bufferSize);
			outputStream.write(data);

			this.bufferSize += data.length;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	@Override
	public void onStateChange(LineEvent event) {
		// TODO Auto-generated method stub
		System.out.println("State changed to " + event.getType());
		if (event.getType() == LineEvent.Type.STOP) {
			try {
				writeToFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (event.getType() == LineEvent.Type.START) {
			this.bufferSize = 0;

//			try {
//				AudioSystem.write(audioInputStream, AudioFileFormat.Type.AU, outputStream);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}
//		} else if (event.getType() == LineEvent.Type.START) {
//			try {
//				AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputStream);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	private void writeToFile() throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			System.out.println("Final size:");
			System.out.println(this.bufferSize);
			System.out.println(this.outputStream.size());
			
			byte[] abAudioData = this.outputStream.toByteArray();
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(abAudioData);
			AudioInputStream outputAIS = new AudioInputStream(byteArrayInputStream, audioInputStream.getFormat(), abAudioData.length);

			AudioSystem.write(outputAIS, AudioFileFormat.Type.WAVE, out);
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
