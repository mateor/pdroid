package java.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Provides privacy handling for {@link java.lang.ProcessManager}
 * @author Anonymous
 */
public class PrivacyProcessManager {
    
    /**
     * Verifies if the current process has privacy access permission
     * to the specified setting
     * @param setting name of the setting file (e.g., systemLogsSetting or 
     *          externalStorageSetting)
     * @return boolean true if permission is granted or false otherwise
     */
    public static boolean hasPrivacyPermission(String setting, int pid) {
        String packageName = null;
        String uid = null;
        boolean output = true;
        try {
            packageName = getPackageName();
            uid = getUid();
        } catch (Exception e) {
            System.err.println("PrivacyProcessManager: could not find package name or UID");
            e.printStackTrace();
        }
        try {
            // get setting value
            PrivacyFileReader freader = new PrivacyFileReader("/data/system/privacy/" + 
                    packageName + "/" + uid + "/" + setting);
            String line = freader.readLine().trim();
            int systemLogsSetting = Integer.parseInt(line);
            freader.close();
            // get the command line of starting process
            freader = new PrivacyFileReader("/proc/" + pid + "/cmdline");
            String proc = "";
            while (proc.isEmpty()) proc = freader.readLine().trim();
            freader.close();
            // check permission
            if (systemLogsSetting == 1 && proc.length() > 5 && proc.contains("logcat")) {
                output = false;
            }
        } catch (FileNotFoundException e) {
            // no setting for this application; do nothing
        } catch (Exception e) {
            System.err.println("PrivacyProcessManager: could not read privacy settings: " + setting);
            e.printStackTrace();
        }
        
        return output;
    }
    
    /**
     * Finds the package name corresponding to the current process
     * @return Current process' package name
     * @throws IOException, FileNotFoundException 
     */
    private static String getPackageName() throws IOException, FileNotFoundException {
        PrivacyFileReader freader = new PrivacyFileReader("/proc/self/cmdline");
        String packageName = freader.readLine().trim();
        freader.close();
        return packageName;
    }
    
    /**
     * Finds the UID corresponding to the current process
     * @return Current process' UID
     * @throws IOException, FileNotFoundException, NumberFormatException, Exception
     */
    private static String getUid() throws IOException, FileNotFoundException, 
            NumberFormatException, Exception {
        PrivacyFileReader freader = new PrivacyFileReader("/proc/self/cgroup");
        String uid = null;
        String line = "";
        while (!line.contains("/uid/")) line = freader.readLine();
        freader.close();
        if (line != null) {
            int index = line.indexOf("/uid/");
            index += "/uid/".length();
            // make sure the found UID is an int and convert it back to string
            uid = Integer.parseInt(line.substring(index).trim()) + "";
        }
        if (uid != null) return uid;
        else throw new Exception();
    }
    
    public static class PrivacyFileReader {
        
        private FileInputStream inputStream;
        
        private BufferedReader buffReader;
        
        public PrivacyFileReader(String path) throws FileNotFoundException {
            inputStream = new FileInputStream(new File(path));
            buffReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        
        public String readLine() throws IOException {
            return buffReader.readLine();
        }
        
        public void close() throws IOException {
            inputStream.close();            
        }
    }
}
