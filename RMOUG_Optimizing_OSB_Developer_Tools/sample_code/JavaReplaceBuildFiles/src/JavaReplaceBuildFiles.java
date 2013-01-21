import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/*
Example of using this JAR in Ant with the local VM:

<!-- Note: You must have the JavaReplaceBuildFiles jar defined as a dependency in ivy.xml in order to automatically retrieve it in /lib/ -->
<target name="CmdLineReplaceBuildFiles">
	<java	fork="true"
			failonerror="true"
			maxmemory="256m"
			classname="JavaReplaceBuildFiles"
			classpath="lib/JavaReplaceBuildFiles-1.4.jar">
		<arg line="${replacetokens.sourcefolders}" />
		<arg line="${replacetokens.filetypes}" />
		<arg line="${replacetokens.outputfolder}" />
		<arg line="../../tokens.LOCAL.properties" />
	</java>
</target>
*/

public class JavaReplaceBuildFiles
{
	// Arg 0: Comma-delimited list of source folders to scan
	// Arg 1: Comma-delimited list of file types to scan
	// Arg 2: Target output folder (will be created if not already exists)
	// Arg 3: Path to properties file that contains the tokens to be found/replaced
	// Arg 4: Comma-delimited list of strings to be replaced with the outputfolder name instead
	//        (This helps catch any oversights whereby something is referencing an original (still-tokenized) file,
	//         instead of the output file that has had the tokens replaced.)
	public static void main(String[] args)
			throws Exception
	{
		try
		{
			// Read project properties file to get the configured tokens
			ArrayList<String[]> keys = getTokensFromPropertiesFile(args[3]);

			// For each folder that was passed as a comma-delimited list:
			String[] folders = args[0].split(",");
			for (String folderName : folders)
			{
				File folder = new File("../" + folderName);
				File[] listOfFiles = folder.listFiles();

				// Examine the list of files in that folder
				for (File file : listOfFiles)
				{
					if ((file.isFile()) && 
							(isFileExtensionOkay(file.getName(), args[1])))
					{
						String content = readFile("../" + folderName + "/" + file.getName());
						for (String[] pair : keys)
						{
							content = content.replaceAll(pair[0], pair[1]);
						}

						// v1.3 change - Replace path names so we always point to the auto-generated file
						// This code removed with v1.4 change
						//content = content.replaceAll("/proxy/", "/VarsReplacedDuringBuild/");
						//content = content.replaceAll("/business/", "/VarsReplacedDuringBuild/");
						// End v1.3 change

						// Loop through strings to be replaced with the output folder name instead
						// (For instance, a PS that references /business/TokenBS should reference /OutputFolder/TokenBS instead.)
						String[] sourceStrings = args[4].split(",");
						for (String sourceString : sourceStrings) {
							content = content.replaceAll(sourceString, args[2]);
						}

						// Create output directory if not yet exists
						File outputDir = new File("../" + args[2]);
						if (!outputDir.exists()) {
							outputDir.mkdir();
						}

						BufferedWriter outFile = new BufferedWriter(new FileWriter("../" + args[2] + "/" + file.getName()));
						outFile.write(content);
						outFile.close();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private static ArrayList<String[]> getTokensFromPropertiesFile(String fileName) throws FileNotFoundException, IOException
	{
		ArrayList<String[]> keys = new ArrayList<String[]>();

		Properties prop = new Properties();
		prop.load(new FileInputStream(fileName));

		for (Iterator localIterator = prop.keySet().iterator(); localIterator.hasNext(); ) { Object propKey = localIterator.next();

		String key = (String)propKey;
		if (key.startsWith("token."))
		{
			String[] keyValuePair = new String[2];
			keyValuePair[0] = key.substring(6);
			keyValuePair[1] = prop.getProperty(key);
			keys.add(keyValuePair);
		}
		}

		return keys;
	}

	private static String readFile(String path) throws IOException
	{
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());

			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}

	private static boolean isFileExtensionOkay(String fileName, String extensions)
	{
		String[] exts = extensions.split(",");
		for (String ext : exts)
		{
			if (fileName.toLowerCase().endsWith(ext.toLowerCase()))
				return true;
		}
		return false;
	}
}