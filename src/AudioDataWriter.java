
public class AudioDataWriter extends AudioObserver {
	private String filePath;
	
	public AudioDataWriter(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public void onAudioRecieved(byte[] data) {
		System.out.println(Thread.currentThread().getId());
//		int i = 0;
//		while (i < data.length) {
//			System.out.print(data[i] + " ");
//			i++;
//		}
//		System.out.println();
	}
}
