import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;

public abstract class AudioObserver implements LineListener {

	public abstract void onAudioRecieved(byte[] data);

	public abstract void setDataLine(TargetDataLine dataLine);
}
