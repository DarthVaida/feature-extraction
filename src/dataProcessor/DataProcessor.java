package dataProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DataProcessor {
	private static ArrayList<HashMap<String, Integer>> labels;
	public final static String SPLITEXPRESSION = "!-!";
	public static ArrayList<String> fileList;

	public static void main(String[] args) {
		fileList = new ArrayList<String>();
		String queryPath = args[0];
		String apkDirectory = args[1];
		labels = constructLabelsList(queryPath, apkDirectory);
		printLabelsToFile();
		System.gc();
		printDataToFile(queryPath, apkDirectory);
		// Measure similarity
		cosine(apkDirectory);
	}

	/**
	 * Constructs the complete list of unique feature names
	 * 
	 * @param featureSet
	 *            : the directory containing the feature files
	 * @return a list of all the feature Labels
	 */
	public static ArrayList<HashMap<String, Integer>> constructLabelsList(String queryPath, String apkDirectory) {
		ArrayList<HashMap<String, Integer>> labelsMap = new ArrayList<HashMap<String, Integer>>(1);
		for (int i = 0; i < 6; i++) {
			labelsMap.add(new HashMap<String, Integer>());
		}
		// Read the query file
		try {
			Path path = Paths.get(queryPath, "");
			List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
			System.err.println("Found " + lines.size() + " features for: " + queryPath);
			int pathLength;
			String currentFeature;
			// Go through each feature
			for (int i = 0; i < lines.size(); i++) {

				pathLength = Integer.parseInt(lines.get(i).split(SPLITEXPRESSION)[0]);
				currentFeature = lines.get(i).split(SPLITEXPRESSION)[2];
				// Add it to the map (unless it is already present)
				try {
					if (labelsMap.get(pathLength) != null) {
						labelsMap.get(pathLength).put(currentFeature, 1);
					}

				} catch (IndexOutOfBoundsException e) {
					labelsMap.add(pathLength, new HashMap<String, Integer>());
					labelsMap.get(pathLength).put(currentFeature, 1);
				}

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Read the files under analysis
		File featureSet = new File(apkDirectory);
		if (featureSet.isDirectory()) {
			String[] files = featureSet.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					String x = queryPath.substring(queryPath.lastIndexOf('/') + 1);
					return (name.endsWith(".txt") && !name.equals(queryPath.substring(queryPath.lastIndexOf('/') + 1)));
				}

			});

			Arrays.sort(files);

			// Go through each file
			for (String s : files) {
				try {
					Path path = Paths.get(apkDirectory, s);
					List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
					System.err.println("Found " + lines.size() + " features for: " + s);
					int index;
					String currentFeature;
					// Go through each feature
					for (int i = 0; i < lines.size(); i++) {

						index = Integer.parseInt(lines.get(i).split(SPLITEXPRESSION)[0]);
						currentFeature = lines.get(i).split(SPLITEXPRESSION)[2];
						// Add it to the map (unless it is already present)
						try {
							if (labelsMap.get(index) != null) {
								labelsMap.get(index).put(currentFeature, 1);
							}

						} catch (IndexOutOfBoundsException e) {
							labelsMap.add(index, new HashMap<String, Integer>());
							labelsMap.get(index).put(currentFeature, 1);
						}

					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		int c = 0;
		for (HashMap<String, Integer> it : labelsMap) {
			c += it.size();
		}
		// System.err.println("Found a total of " + totalFeatures + "
		// features");
		System.err.println("Out of which " + c + " are unique");
		return labelsMap;
	}

	private static ArrayList<String> toArrayList(ArrayList<HashMap<String, Integer>> labelsMap) {
		ArrayList<String> labelsList = new ArrayList<String>();
		for (int i = 0; i < labelsMap.size(); i++) {
			for (String label : labelsMap.get(i).keySet()) {
				labelsList.add(label);
			}
		}
		return labelsList;

	}

	private static void printDataToFile(String queryPath, String apkDirectory) {

		try {
			PrintWriter pw = new PrintWriter("output/data.txt");
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			File dataFile = new File("output/data.txt");
			File intermediateLabelsFile = new File(
					"output/intermediate-labels.txt");
			dataFile.createNewFile();
			FileWriter fileWriter = new FileWriter(dataFile, true);

			// Vectorize the query
			ArrayList<HashMap<String, Integer>> queryMap = constructFeatureMapFromFilePath(Paths.get(queryPath, ""));
			scaleWeights(queryMap);
			vectorize(fileWriter, queryMap, intermediateLabelsFile, "Query");

			File featureSet = new File(apkDirectory);
			if (featureSet.isDirectory()) {
				String[] files = featureSet.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return (name.endsWith(".txt")
								&& !name.equals(queryPath.substring(queryPath.lastIndexOf('/') + 1)));
					}

				});
				fileList.add(queryPath.substring(queryPath.lastIndexOf('/') + 1));
				Arrays.sort(files);

				// Go through each file
				for (String s : files) {

					// Construct features Map to represent one .apk app
					ArrayList<HashMap<String, Integer>> featuresMap = constructFeatureMapFromFilePath(
							Paths.get(apkDirectory, s));
					// Vectorize the path
					scaleWeights(featuresMap);
					vectorize(fileWriter, featuresMap, intermediateLabelsFile, s);
					fileList.add(s);

				}
				System.err.println("");
				fileWriter.close();

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void scaleWeights(ArrayList<HashMap<String, Integer>> map) {
		try {
			if (map.get(4).get("<sanitization>") == 0) {
				return;
			}
		} catch (NullPointerException e) {

		}

		int weightSum = 0;
		Iterator<HashMap<String, Integer>> it = map.iterator();
		while (it.hasNext()) {
			HashMap<String, Integer> e = it.next();
			Iterator<Entry<String, Integer>> i = e.entrySet().iterator();
			while (i.hasNext()) {
				Entry<String, Integer> pair = (Entry<String, Integer>) i.next();
				weightSum += pair.getValue();
			}
		}

		Iterator<Entry<String, Integer>> ix = map.get(4).entrySet().iterator();

		for (;;) {
			Entry<String, Integer> e = ix.next();
			String key = e.getKey();
			Integer value = e.getValue();
			map.get(4).put(key, value * weightSum);
			if (!ix.hasNext())
				break;
		}

	}

	private static void vectorize(FileWriter fileWriter, ArrayList<HashMap<String, Integer>> featuresMap,
			File intermediateLabelsFile, String s) {
		try {
			String dataLine = "";

			// Read each feature from the feature labels file and
			// check
			// if the current map contains that feature

			BufferedReader br = new BufferedReader(new FileReader(intermediateLabelsFile));
			String line, currentFeature;
			int pathLength;
			int c = 0;
			while ((line = br.readLine()) != null) {
				pathLength = Integer.parseInt(line.split(SPLITEXPRESSION)[0]);
				currentFeature = line.split(SPLITEXPRESSION)[1];
				// Feature present -> Write 1
				try {
					if (featuresMap.get(pathLength) != null
							&& featuresMap.get(pathLength).get(currentFeature) != null) {
						if (dataLine.equals("")) {
							dataLine += "" + featuresMap.get(pathLength).get(currentFeature);
							c++;
						} else {
							dataLine += " " + featuresMap.get(pathLength).get(currentFeature);
							c++;
						}

					}
					// Feature not present -> Write 0
					else {
						if (dataLine.equals("")) {
							dataLine += "0";
							c++;
						} else {
							dataLine += " 0";
							c++;
						}

					}
				} catch (IndexOutOfBoundsException ex) {
					// Feature not present -> Write 0
					if (dataLine.equals("")) {
						dataLine += "0";
						c++;
					} else {
						dataLine += " 0";
						c++;
					}

				}

			}
			fileWriter.write(dataLine + "\n");
			System.err.println("Written " + c + " features for: " + s);
			// System.out.println(dataLine);
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void printLabelsToFile() {
		printIntermediateLabelsToFile();
		printLabelsToFinalFile();

	}

	private static void printIntermediateLabelsToFile() {
		// Clear the content of the file first
		try {
			PrintWriter pw = new PrintWriter(
					"output/intermediate-labels.txt");
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		File labelsFile = new File("output/intermediate-labels.txt");
		FileWriter fileWriter;
		int c = 0;
		try {
			fileWriter = new FileWriter(labelsFile, true);
			for (int i = 0; i < labels.size(); i++) {
				for (String label : labels.get(i).keySet()) {
					fileWriter.write(i + SPLITEXPRESSION + label + "\n");
					c++;
				}
			}
			System.err.println("Written " + c + " intermediate features to file");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void printLabelsToFinalFile() {
		try {
			PrintWriter pw = new PrintWriter("output/labels.txt");
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		File labelsFile = new File("output/labels.txt");
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(labelsFile, true);
			int c = 0;
			for (int i = 0; i < labels.size(); i++) {
				for (String label : labels.get(i).keySet()) {
					fileWriter.write(label + "\n");
					c++;
				}
			}
			System.err.println("Written " + c + " features to labels file");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static ArrayList<HashMap<String, Integer>> constructFeatureMapFromFilePath(Path filePath) {
		ArrayList<HashMap<String, Integer>> map = new ArrayList<HashMap<String, Integer>>();
		for (int i = 0; i < 6; i++) {
			map.add(new HashMap<String, Integer>());
		}
		try {

			List<String> lines = Files.readAllLines(filePath, Charset.forName("UTF-8"));
			int pathLength, featureValue;
			String currentFeature;
			// Go through each feature
			for (int i = 0; i < lines.size(); i++) {

				pathLength = Integer.parseInt(lines.get(i).split(SPLITEXPRESSION)[0]);
				featureValue = Integer.parseInt(lines.get(i).split(SPLITEXPRESSION)[1]);
				currentFeature = lines.get(i).split(SPLITEXPRESSION)[2];
				// Add it to the map (unless it is already present)
				try {
					if (map.get(pathLength) != null) {
						map.get(pathLength).put(currentFeature, featureValue);
					}

				} catch (IndexOutOfBoundsException e) {
					map.add(pathLength, new HashMap<String, Integer>());
					map.get(pathLength).put(currentFeature, featureValue);
				}

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return map;
	}

	public static String printFeatures(ArrayList<HashMap<String, Integer>> features) {
		Iterator<HashMap<String, Integer>> it = features.iterator();
		if (!it.hasNext())
			return "[]";

		StringBuilder sb = new StringBuilder();
		for (;;) {
			HashMap<String, Integer> e = it.next();
			sb.append("\nPaths of depth " + (int) (features.indexOf(e) + 1) + " :\n");
			sb.append(hashMapToString(e));
			if (!it.hasNext())
				return sb.toString();

		}
	}

	// Helper method. Redefines the toString for a HashMap
	public static String hashMapToString(HashMap<String, Integer> map) {
		Iterator<Entry<String, Integer>> i = map.entrySet().iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();

		for (;;) {
			Entry<String, Integer> e = i.next();
			String key = e.getKey();
			sb.append(key);
			// Remove the new line for vectorization
			sb.append("\n");
			// sb.append(value);
			if (!i.hasNext())
				return sb.toString();

		}
	}

	private static String sortedHashMapToString(HashMap<Double, String> map) {

		List<Double> keys = new ArrayList<Double>(map.keySet());
		Collections.sort(keys);
		Collections.reverse(keys);
		Iterator<Double> i = keys.iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append("Path similarity measures:\n");

		for (;;) {
			Double key = i.next();
			if (key == 1 || key == 0) {
				sb.append(key + "            - " + map.get(key));
			} else {
				sb.append(key + " - " + map.get(key));
			}

			if (key > 0.5) {
				sb.append(" - Vulnerable" + "\n");
			} else {
				sb.append(" - Benign" + "\n");
			}

			if (!i.hasNext())
				return sb.toString();

		}
	}

	private static String sortedHashMapKeysToString(HashMap<Double, String> map) {

		List<Double> keys = new ArrayList<Double>(map.keySet());
		Collections.sort(keys);
		Collections.reverse(keys);
		Iterator<Double> i = keys.iterator();
		if (!i.hasNext())
			return "";

		StringBuilder sb = new StringBuilder();

		for (;;) {
			Double key = i.next();
			sb.append(key+"\n");

			if (!i.hasNext()) {
				return sb.toString();
			}
		}

	}

	private static void cosine(String apkDirectory) {

		HashMap<Double, String> scores = new HashMap<Double, String>();
		try {
			String line;
			Process p = Runtime.getRuntime().exec("python src/tfidf.py");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				if (line.startsWith("Path similarity:")) {
					String[] simValuesStr = line.substring(line.indexOf("1")).split("::");
					for (int i = 1; i < fileList.size(); i++) {
						scores.put(Double.valueOf(simValuesStr[i]), fileList.get(i));
					}

				}
			}
			in.close();
		} catch (IOException e) {

		}

		System.out.println("");
		System.out.println(sortedHashMapToString(scores));
	/*	File evaluation = new File("/home/hexd123/workspace/feature-extraction/output/ev1.txt");
		try {
			FileWriter fileWriter = new FileWriter(evaluation, true);
			fileWriter.write("APK: " + apkDirectory);
			fileWriter.write(sortedHashMapKeysToString(scores));
	    	fileWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
