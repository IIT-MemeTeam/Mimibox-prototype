import java.util.ArrayList;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioListener implements Runnable {
	private Thread worker;
	private SourceDataLine outputDataLine;
	private TargetDataLine inputDataLine;

	private AtomicBoolean running;
	private AtomicBoolean stopped;

	private boolean isSetup;
	
	private Long timerDelay;
	Timer timer;
	
	EventListener[] eventListeners;
	ArrayList<AudioObserver> audioListeners;

	public AudioListener(int seconds, Mixer.Info input, Mixer.Info output, AudioFormat audioFormat)
			throws LineUnavailableException {
		this.running = new AtomicBoolean(false);
		this.stopped = new AtomicBoolean(true);
		this.isSetup = false;
		audioListeners = new ArrayList<AudioObserver>();

		// Output
		Mixer speakerMixer = AudioSystem.getMixer(output);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		outputDataLine = (SourceDataLine) speakerMixer.getLine(info);

		// Input
		Mixer micMixer = AudioSystem.getMixer(input);
		info = new DataLine.Info(TargetDataLine.class, audioFormat);
		inputDataLine = (TargetDataLine) micMixer.getLine(info);
		
		this.timerDelay = TimeUnit.SECONDS.toMillis(seconds);
		this.timer = new Timer();
		this.isSetup = true;
	}
	
	public void addAudioListener(AudioObserver observer) {
		this.audioListeners.add(observer);
	}

	public boolean isRunning() {
		return this.running.get();
	}

	public boolean isStopped() {
		return this.stopped.get();
	}

	public void interrupt() {
		this.running.set(false);
		this.worker.interrupt();
	}

	public void start() {
		if (isSetup && !isRunning()) {
			worker = new Thread(this);
			worker.start();

			timer.schedule(onTimerFinished(), this.timerDelay);
		}
	}
	
	public TimerTask onTimerFinished() {
		return new TimerTask() {
	        public void run() {
	            stop();
	        }
	    };
	}

	public void stop() {
		this.running.set(false);
	}

	public void run() {
		System.out.println("Starting on thread " + Thread.currentThread().getId());
		this.running.set(true);
        this.stopped.set(false);
		try {
			this.inputDataLine.open();
			this.outputDataLine.open();

			this.inputDataLine.start();
			this.outputDataLine.start();

			byte[] data = new byte[this.inputDataLine.getBufferSize() / 5];
			int readBytes;

			while (this.isRunning()) {
				readBytes = this.inputDataLine.read(data, 0, data.length);
				this.outputDataLine.write(data, 0, readBytes);
				notifyAllObservers(data); // Should be on a different thread
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			this.inputDataLine.stop();
			this.inputDataLine.close();
			this.outputDataLine.drain();
			this.outputDataLine.close();
		}

		stop();
		
		this.stopped.set(true);
	}
	
	private void notifyAllObservers(byte[] data) {
		if (this.audioListeners != null) {
			for (AudioObserver observer : this.audioListeners) {
				observer.onAudioRecieved(data);
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
