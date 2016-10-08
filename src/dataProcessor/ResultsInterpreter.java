package dataProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import com.google.common.io.Files;


public class ResultsInterpreter {

	public static void main(String[] args) {
		precision();
		recall();
		
		

	}

	private static void precision() {
		File directory = new File("/home/hexd123/workspace/feature-extraction/data");
		double x =0;
		String[] subdirs = directory.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
			
		});
		for(String d : subdirs){
			System.out.println("/home/hexd123/workspace/feature-extraction/data/"+d);
			File f = new File("/home/hexd123/workspace/feature-extraction/data/"+d);
			File[] files = f.listFiles();
			x+= files.length;
		}
		System.out.println(x);
		System.out.println(108);
		System.out.println(108/x);
	}

	private static void recall() {
		String line;
		double tp = 0;
		double l = 0;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("/home/hexd123/workspace/feature-extraction/output/ev2.txt"));
			while ((line = br.readLine()) != null) {
				l++;
				if (Double.valueOf(line) > 0.7) {
					tp++;
				}

			}

			br.close();
			System.out.println(l);
			System.out.println(tp);
			System.out.println(tp/l);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}


