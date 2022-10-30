package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
	public static void main(String[] args) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("testecho.bat");

		try {
			Process process = processBuilder.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

				String line;

				while ((line = reader.readLine()) != null) {
					System.out.println(process.isAlive());
					System.out.println(line);
					process.destroy();
				}

			}
			System.out.println(process.isAlive());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
}
