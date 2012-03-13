package android.privacy;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;

public class PrivacyFileObserver extends FileObserver {
    
    public static final String TAG = "PrivacyFileObserver";
    
    public static final int PACKAGE_PATH_INDEX = 3;
    
    public String absolutePath;
    
    private PrivacySettingsManagerService pSetManServ;
    
    public HashMap<String, PrivacyFileObserver> children; 
    
    public PrivacyFileObserver(String path, PrivacySettingsManagerService pSetManServ) {
        super(path, FileObserver.ALL_EVENTS);
        this.absolutePath = path;
        this.pSetManServ = pSetManServ;
        
        this.children = new HashMap<String, PrivacyFileObserver>();
        File thisFile = new File(absolutePath);
        if (thisFile.isDirectory()) {
            File[] subfiles = thisFile.listFiles();
            for (File file : subfiles) {
                String observePath = file.getAbsolutePath();
                PrivacyFileObserver child = new PrivacyFileObserver(observePath, pSetManServ);
                children.put(observePath, child);
                // don't watch directories, only the settings files
                if (file.isFile()) child.startWatching();
            }
        }
        
    }

    @Override
    public void onEvent(int event, String path) {
        if ((FileObserver.ACCESS & event) != 0) { // data was read from a file
//            Log.d(TAG, "onEvent - file accessed: " + absolutePath);
            StringTokenizer tokenizer = new StringTokenizer(absolutePath, "/");
            for (int i = 0; i < PACKAGE_PATH_INDEX && tokenizer.hasMoreElements(); i++) {
                tokenizer.nextToken();
            }
            
            // get the package and UID of accessing application
            String packageName = tokenizer.nextToken();
//            int uid = 0;
//            try {
//                uid = Integer.parseInt(tokenizer.nextToken());
//            } catch (NumberFormatException e) {
//                Log.e(TAG, "onEvent - could not get the UID of accessing application", e);
//                // we still can continue, UID is optional here
//            }
            
            // read the setting
            PrivacySettings pSet = pSetManServ.getSettings(packageName);
            pSetManServ.notification(packageName, pSet.getSystemLogsSetting(), PrivacySettings.DATA_SYSTEM_LOGS, null);
        }
        
    }
    
    public void addObserver(String relativePath) {
        String observePath = absolutePath + "/" + relativePath;
        // remove existing observer(s) if any
        children.remove(observePath); // child observers should be destroyed at next GC
        // create new observer(s)
        PrivacyFileObserver child = new PrivacyFileObserver(observePath, pSetManServ);
        children.put(observePath, child);
    }

    @Override
    public void startWatching() {
//        Log.d("PrivacyFileObserver", "PrivacyFileObserver - observing directory: " + absolutePath);
        super.startWatching();
    }
    
//    public void verifyObserver() {
//        Log.d(TAG, "verifyObservers - observer path: " + absolutePath);
//        for (PrivacyFileObserver obs : children.values()) obs.verifyObserver();
//    }
    
}