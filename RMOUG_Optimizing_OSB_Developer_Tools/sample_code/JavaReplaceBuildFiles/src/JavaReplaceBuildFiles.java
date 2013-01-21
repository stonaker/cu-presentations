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

public class JavaReplaceBuildFiles
{
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
			content = content.replaceAll("/proxy/", "/VarsReplacedDuringBuild/");
			content = content.replaceAll("/business/", "/VarsReplacedDuringBuild/");
			// End v1.3 change

            // Create output directory if not yet exists
            //File outputDir = new File("../" + args[2]);
            File outputDir = new File("../VarsReplacedDuringBuild");
            if (!outputDir.exists()) {
            	outputDir.mkdir();
            }

            //BufferedWriter outFile = new BufferedWriter(new FileWriter("../" + args[2] + "/" + file.getName()));
            BufferedWriter outFile = new BufferedWriter(new FileWriter("../VarsReplacedDuringBuild/" + file.getName()));
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
    //prop.load(new FileInputStream("../../local.properties"));
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