import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;

public abstract class AudioObserver implements LineListener {

	public abstract void onAudioRecieved(byte[] data);

	public abstract void onStateChange(LineEvent event);
	
	public abstract void setDataLine(TargetDataLine dataLine);
	
	@Override
	public void update(LineEvent event) {
		// TODO Auto-generated method stub
		onStateChange(event);
	}
}
