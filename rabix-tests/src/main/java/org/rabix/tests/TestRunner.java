package org.rabix.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.common.helper.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunner {
  private static String testDirPath;
  private static String cmd_prefix;
  private static String resultPath = "./rabix-backend-local/target/result.yaml";
  private static String workingdir = "./rabix-backend-local/target/";
  private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

  public static void main(String[] commandLineArguments) {
    try {
      testDirPath = getConfig("testDirPath");
      cmd_prefix = getConfig("cmd_prefix");
    } catch (IOException e) {
      e.printStackTrace();
    }
    startTestExecution();
  }

  private static void startTestExecution() {
    boolean success = true;
    boolean testPassed = false;
    File dir = new File(testDirPath);
    File[] directoryListing = dir.listFiles();
    ArrayList<Object> failedTests = new ArrayList<Object>();

    if (!dir.isDirectory()) {
      logger.error("Problem with test directory path: Test directory path is not valid directory path.");
    } else {
      if (directoryListing == null) {
        logger.error("Problem with provided test directory: Test directory is empty.");
      } else {
        logger.info("Extracting jar file");
        executeCommand("sudo tar -zxvf " + System.getProperty("user.dir")
            + "/rabix-backend-local/target/rabix-backend-local-0.0.1-SNAPSHOT-id3.tar.gz");
        executeCommand("cp -a " + System.getProperty("user.dir") + "/rabix-tests/testbacklog .");

        for (File child : directoryListing) {
          if (!child.toString().endsWith(".test.yaml"))
            continue;
          try {
            String currentTest = readFile(child.getAbsolutePath(), Charset.defaultCharset());
            Map<String, Object> inputSuite = JSONHelper.readMap(JSONHelper.transformToJSON(currentTest));
            Iterator entries = inputSuite.entrySet().iterator();
            while (entries.hasNext()) {
              Entry thisEntry = (Entry) entries.next();
              Object test_name = thisEntry.getKey();
              Object test = thisEntry.getValue();

              logger.info("Running test: " + test_name + " with given parameters:");
              Map<String, Map<String, LinkedHashMap>> mapTest = (Map<String, Map<String, LinkedHashMap>>) test;
              logger.info("  app: " + mapTest.get("app"));
              logger.info("  inputs: " + mapTest.get("inputs"));
              logger.info("  expected: " + mapTest.get("expected"));
              String cmd = cmd_prefix + " " + mapTest.get("app") + " " + mapTest.get("inputs") + " > result.yaml";
              logger.info("->Running cmd: " + cmd + "\n");
              executeCommand(cmd);

              File resultFile = new File(resultPath);

              String resultText = readFile(resultFile.getAbsolutePath(), Charset.defaultCharset());
              Map<String, Object> resultData = JSONHelper.readMap(JSONHelper.transformToJSON(resultText));
              logger.info("\nGenerated result file:");
              logger.info(resultText);
              testPassed = validateTestCase(mapTest, resultData);
              logger.info("Test result: ");
              if (testPassed) {
                logger.info(test_name + " PASSED");

              } else {
                logger.info(test_name + " FAILED");
                failedTests.add(test_name);
                success = false;
              }

            }

          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        if (success) {

          logger.info("Test suite passed successfully.");

        } else {
          logger.info("Test suite failed.");
          logger.info("Failed test number: " + failedTests.size());
          logger.info("Failed tests:");
          for (Object test : failedTests) {
            logger.info(test.toString());
          }

        }
      }
    }

  }

  private static boolean validateTestCase(Map<String, Map<String, LinkedHashMap>> mapTest,
      Map<String, Object> resultData) {
    String resultFileName;
    int resultFileSize;
    String resultFileClass;
    Map<String, Object> resultValues = ((Map<String, Object>) resultData.get("outfile"));
    resultFileName = resultValues.get("path").toString();
    resultFileName = resultFileName.split("/")[resultFileName.split("/").length - 1];
    resultFileSize = (int) resultValues.get("size");
    resultFileClass = resultValues.get("class").toString();
    logger.info("Test validation:");
    logger.info("result file name: " + resultFileName + ", expected file name: "
        + mapTest.get("expected").get("outfile").get("name"));
    logger.info("result file size: " + resultFileSize + ", expected file size: "
        + mapTest.get("expected").get("outfile").get("size"));
    logger.info("result file class: " + resultFileClass + ", expected file class: "
        + mapTest.get("expected").get("outfile").get("class"));

    boolean fileNamesEqual = resultFileName.equals(mapTest.get("expected").get("outfile").get("name"));
    boolean fileSizesEqual = resultFileSize == (int) mapTest.get("expected").get("outfile").get("size");
    boolean fileClassesEqual = resultFileClass.equals(mapTest.get("expected").get("outfile").get("class"));

    if (!fileNamesEqual) {
      logger.info("result and expected file name are not equal!");
    } else {
      if (!fileSizesEqual) {
        logger.info("result and expected file size are not equal!");
      } else {
        if (!fileClassesEqual) {
          logger.info("result and expected file class are not equal!");
        } else {
          return true;
        }
      }
    }

    return false;
  }

  public static ArrayList<String> command(final String cmdline, final String directory) {
    try {
      Process process = new ProcessBuilder(new String[] { "bash", "-c", cmdline }).redirectErrorStream(true)
          .directory(new File(directory)).start();

      ArrayList<String> output = new ArrayList<String>();
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      while ((line = br.readLine()) != null)
        output.add(line);

      if (0 != process.waitFor()) {
        return null;
      }

      return output;

    } catch (Exception e) {
      return null;
    }
  }

  static void executeCommand(String cmdline) {
    ArrayList<String> output = command(cmdline, workingdir);
    if (null == output)
      logger.info("COMMAND FAILED: " + cmdline + "\n");
    else
      for (String line : output) {
        logger.info(line);
      }
  }

  /**
   * Reads content from a file
   */
  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  private static String getConfig(String key) throws IOException {
    File configFile = new File(System.getProperty("user.dir") + "/rabix-tests/config/core.properties");
    String config = readFile(configFile.getAbsolutePath(), Charset.defaultCharset());
    String[] splitedRows = config.split("\n");

    Map<String, String> cmap = new HashMap<String, String>();

    for (String row : splitedRows) {
      String[] elems = row.split("=");
      cmap.put(elems[0], elems[1]);
    }
    return cmap.get(key);
  }

}
