package dataProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import soot.jimple.infoflow.android.TestApps.*;

public class FeatureExtractor {
	private static String FILE_NAME;
	private static String ANDROID_PLATFORM_FOLDER;
	static int apps = 0;

	public static void main(String[] args) {
	//	double start = System.currentTimeMillis();
		FILE_NAME = args[0];
		ANDROID_PLATFORM_FOLDER = args[1];
		read(FILE_NAME);

/*		double time = System.currentTimeMillis() - start;
		System.out.println("Analysed " + apps + " apps.");
		System.out.println("Total analysis time: " + time * 0.001 + " seconds.");
		System.out.println("Average analysis time per apk file: " + time * 0.001 / apps + " seconds.");
	*/	
		

	}

	private static void read(String filename) {
		
		
		
		File dataFile = new File(filename);
		if (!dataFile.isDirectory()){
			String[] config = { filename, ANDROID_PLATFORM_FOLDER };
			try {
				Test.main(config);
				System.out.println("Analysed "+filename);
				return;
			} catch (IOException | InterruptedException|RuntimeException e) {
				e.printStackTrace();
			}
			
		}
		String[] files = dataFile.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}

		});
		for(String i : files){System.out.println(i);}
		apps += files.length;
		// Arrays.sort(files);
		for (String s : files) {
			if (s.endsWith(".apk")) {
				String[] config = { filename+"/"+s, ANDROID_PLATFORM_FOLDER };
				try {
					Test.main(config);
					System.out.println(filename);
				} catch (IOException | InterruptedException|RuntimeException e) {
					e.printStackTrace();
				}
			}
			
			else if(new File(filename+"/"+s).isDirectory()){
			read(filename+"/"+s);}

		}

	}
}
