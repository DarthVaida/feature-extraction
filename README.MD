README FILE FOR FEATURE-EXTRACTION - ALEX VAIDA SEPTEMBER 2016

Prerequisites:
Android platforms 17-24, depending on the app you are analysing. I had to install all of them for the evaluation. You can only install the version you need. I personally downloaded these through Android Studio from here https://developer.android.com/studio/index.html#downloads

Step 1:

Create a new folder. Clone each of these projects inside the folder:
Jasmin: https://github.com/Sable/jasmin
Heros: https://github.com/Sable/heros
soot-infoflow: https://github.com/secure-software-engineering/soot-infoflow
My fork of Soot: https://github.com/alexv123/soot.git
My fork of FlowDroid: https://github.com/alexv123/soot-infoflow-android.git

Step 2:
Copy the feature-extraction folder (the code of my project) in the same folder as above.

git clone https://github.com/alexv123/feature-extraction.git

You should now have the following folder structure:
-> parent folder:
	-> heros-develop
	-> jasmin-develop
	-> soot-develop
	-> soot-infoflow-develop
	-> soot-infoflow-android-develop
	-> feature-extraction

Step 3:
From soot-infoflow-android-develop copy the following files to the feature-extraction folder:
	AndroidCallbacks.txt
	EasyTaintWrapperSource.txt
	SourcesAndSinks.txt
Import all these projects into your IDE (I have used Eclipse).

Step 4: 
Run the FeatureExtractor.main method with the following 2 arguments:
	1 - The complete filepath of the .apk file to analyze. Ex: "/home/alex/software/DroidBench/apk/Button2.apk"
	2 - The path of your Android SDK platform folder: "/home/hexd123/Android/Sdk/platforms"

This will create a new folder for each .apk and a new file for each path, containing its intermediate representation.
I have developed the code that performs the feature extraction on top of an on the shelf analysis in soot-infoflow-android-develop/src/soot.jimple.infoflow.android/TestApps/Test.java. The code in the MyResultsAvailableHandler private class belongs to me, while the rest is provided by the FlowDroid API. This Class is called from FeatureExtractor.main.  


Step 5:
Run DataProcessor.main with the following 2 arguments:
	1 - The file path of the query. You can choose one of the files extracted in Step 4. Ex: /home/alex/workspace/feature-extraction/data/Button2/Button2-features-path3.txt
	2 - The folder where the paths are situated. This was created in Step 4. Ex: /home/alex/workspace/feature-extraction/data/Button2



