package utils;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by HL on 4/18/17.
 */
public class CheckSumHandler {

	private String checkSumFileName;
    private HashMap<String, String> oldMap;
    private HashMap<String, String> newMap;
    private HashSet<String> dangerousClass;

    public CheckSumHandler(String checkSumFileName) {
    	this.checkSumFileName = checkSumFileName;
    	this.oldMap = new HashMap<>();
    	this.newMap = new HashMap<>();
    	this.dangerousClass = new HashSet<>();
    }
    
    public void doChecksum(String directoryName) throws IOException {
        readChecksums();
        updateChecksums(directoryName);
        writeChecksums();
    }

    private void readChecksums() throws IOException {
        //Read file and generate HashMap
        File checksumFile = new File(checkSumFileName);
        if(!checksumFile.exists()){
            checksumFile.createNewFile();
        }
        String targetFileStr = Files.readAllLines(checksumFile.toPath()).toString();

        targetFileStr = targetFileStr.replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[","").replaceAll("\\]","");
        String[] lines = targetFileStr.split(", ");
        String[] amap;
        for(String line: lines) {
            amap = line.split("=");
            if(amap.length > 1)
                oldMap.put(amap[0], amap[1]);
        }
    }

    private void updateChecksums(String directoryName) throws IOException {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] allFiles = directory.listFiles();
        for (File file: allFiles) {
            if (file.isFile()) {
                String fileName = file.getName();
                if(fileName.endsWith(".class")){

                    byte[] bytecode = Files.readAllBytes(file.toPath());

                    MessageDigest md = null;
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }

                    md.update(bytecode);
                    byte[] mdbytes = md.digest();

                    //convert the byte to hex format
                    StringBuffer sb = new StringBuffer();
                    for(byte b: mdbytes){
                        sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                    }

                    // Store every new checksum into
                    newMap.put(fileName, sb.toString());

                    //Then check dangerous class
                    if(!(oldMap.containsKey(fileName) && oldMap.get(fileName).equals(sb.toString()))){
                        // When not contains this class or checksum changed, include the class
                        int index = fileName.indexOf("$");
                        String className = fileName.substring(0, index == -1? fileName.indexOf(".") : index );
                        dangerousClass.add(className);
                    }
                }
            } else if (file.isDirectory()) {
                updateChecksums(file.getAbsolutePath());
            }
        }
    }

    private void writeChecksums() throws IOException {
        File file = new File(checkSumFileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(newMap.toString());
        bw.close();
    }

    public List<String> getDangerousClasses(){
        return new ArrayList<>(dangerousClass);
    }

}
