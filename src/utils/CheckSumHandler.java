package utils;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CheckSumHandler {

    private String checkSumFileName;
    private boolean isTest;
    private HashMap<String, String> oldMap;
    private HashMap<String, String> newMap;
    private HashSet<String> dangerousClass;

    public CheckSumHandler(String checkSumFileName, boolean isTest) {
        this.checkSumFileName = "myChecksum/" + checkSumFileName;
        this.isTest = isTest;
        oldMap = new HashMap<>();
        newMap = new HashMap<>();
        dangerousClass = new HashSet<>();
    }

    public void doChecksum(String directoryName) throws IOException {
        String packageName = isTest ? PackageHandler.getTestPackageName() : PackageHandler.getClassPackageName();
        readChecksums();
        updateChecksums(directoryName, packageName);
        writeChecksums();
    }

    private void readChecksums() throws IOException {
        File checksumFile = new File(checkSumFileName);
        if(!checksumFile.exists()){
            checksumFile.createNewFile();
        }
        else {
            FileReader fr = new FileReader(checksumFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null){
                String[] kv = line.split("=");
                oldMap.put(kv[0], kv[1]);
            }
            br.close();
            fr.close();
        }
    }

    private void updateChecksums(String directoryName, String packageName) throws IOException {
        File directory = new File(directoryName);

        // Get all the files from a directory
        for (File file: directory.listFiles()) {
            if (file.isFile()) {
                String fileName = file.getName();
                if(fileName.endsWith(".class")){
                    byte[] bytecode = Files.readAllBytes(file.toPath());

                    MessageDigest md = null;
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e) {
                        System.exit(-1);
                    }

                    // Convert the byte to hex md5 format
                    md.update(bytecode);
                    byte[] mdbytes = md.digest();
                    StringBuilder sb = new StringBuilder();
                    for(byte b: mdbytes){
                        sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                    }
                    String hexChecksum = sb.toString();

                    // Store every new checksum into
                    newMap.put(fileName, hexChecksum);

                    // Then check dangerous class
                    if(!oldMap.containsKey(fileName) || !oldMap.get(fileName).equals(hexChecksum)){
                        // When not contains this class or checksum changed, include the class
                        int index = fileName.indexOf(".class");
                        String className = packageName + "." + fileName.substring(0, index);
                        LoggingHandler.debug("Found dangerous class (" + (isTest?"test":"class") + "): "+className);
                        dangerousClass.add(className);
                    }
                }
            }
            else if (file.isDirectory()) {
                String newPackageName = packageName + "." + file.getName();
                updateChecksums(file.getAbsolutePath(), newPackageName);
            }
        }
    }

    private void writeChecksums() throws IOException {
        FileWriter fw = new FileWriter(checkSumFileName);
        BufferedWriter bw = new BufferedWriter(fw);
        for(Map.Entry<String, String> entry: newMap.entrySet()){
            bw.write(entry.getKey()+"="+entry.getValue());
            bw.newLine();
        }
        bw.close();
        fw.close();
    }

    public List<String> getDangerousClasses(){
        return new ArrayList<>(dangerousClass);
    }

}
