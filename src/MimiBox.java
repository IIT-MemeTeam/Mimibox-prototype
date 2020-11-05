import java.security.Permissions;

import javax.sound.sampled.AudioPermission;

public class MimiBox {

	public MimiBox() {
		Utils.clearConsole();
		setupSecurity();
		
		System.out.println("Starting Mimi-box prototype...");
		System.out.println();
		
		AudioListener listener = new AudioListener();

		System.out.println("Exiting...");
	}
	
	private void setupSecurity() {
		AudioPermission record = new AudioPermission("record");
		AudioPermission play = new AudioPermission("record");
		Permissions permissions = new Permissions();
		permissions.add(play);
		permissions.add(record);
		
		if (System.getSecurityManager() == null) {
			System.out.println("Checking");
			SecurityManager secMan = new SecurityManager();
			secMan.checkPermission(record);
			System.setSecurityManager(secMan);
		}
	}

	public static void main(String[] args) {
		MimiBox mimiBox = new MimiBox();
	}

}
