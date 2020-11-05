import java.io.IOException;

public class Utils {
	public static void clearConsole() {
		//System.out.println("\f");
		//System.out.println("\033\143");
//		System.out.println("\033[H\033[2J");
//		System.out.flush();
	    try {
	        if (System.getProperty("os.name").contains("Windows"))
	            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
	        else
	            Runtime.getRuntime().exec("clear");
	    } catch (IOException | InterruptedException ex) {}
	}
}
