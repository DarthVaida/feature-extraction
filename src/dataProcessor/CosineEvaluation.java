package dataProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Arrays;

public class CosineEvaluation {
	private final static String DIRECTORY_NAME = "/home/hexd123/workspace/feature-extraction/data/";

	public static void main(String[] args) {
		double start = System.currentTimeMillis();
		double apps =0;
		PrintWriter pw;
		try {
			pw = new PrintWriter("/home/hexd123/workspace/feature-extraction/output/ev1.txt");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		File dataFile = new File(DIRECTORY_NAME);
		if (dataFile.isDirectory()) {
			String[] directories = dataFile.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}

			});
			Arrays.sort(directories);
			for (String s : directories) {
				apps++;
				System.out.println(s);
				 //String query = getQuery(s);
				String query = "/home/hexd123/workspace/feature-extraction/data/Callbacks_Button2-release/Callbacks_Button2-release-features-path1.txt";
				String[] config = { query, DIRECTORY_NAME + "/" + s };
				DataProcessor.main(config);
			}
		} else {
			System.err.println("Filename is not a directory");
		}
		
		double time = System.currentTimeMillis() - start;
		System.out.println("Analyzed: " + apps + " apps.");
		System.out.println("Total analysis time: " + time * 0.001 + " seconds.");
		System.out.println("Average analysis time per apk file: " + time * 0.001 / apps + " seconds.");
		
		
		
	}

	private static String getQuery(String s) {
		File dataFile = new File(DIRECTORY_NAME + s);
		if (dataFile.isDirectory()) {
			String[] files = dataFile.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return (name.endsWith(".txt"));
				}

			});
			return DIRECTORY_NAME + s + "/" + files[0];
		} else {
			return null;
		}
	}

}
