package idv.neo.utils;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Neo on 2017/5/19.
 */

public class FolderFileUtils {
    private static final String TAG = FolderFileUtils.class.getSimpleName();

    public static boolean checkSDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean checkTesseractOCRFolderExist() {
        return checkExternalFolderFileExist("tessdata");
    }

    public static boolean createTesseractOCRFolder() {//FIXME
        return createFolderFile("tessdata");
    }

    public static boolean isTesseractOCRFolder(File folderfile) {
        return checkFolderFileTheSameName(folderfile, "tessdata");
    }

    public static boolean checkExternalFolderFileExist(String folderfilename) {
        folderfilename = folderfilename.replace(Environment.getExternalStorageDirectory().toString() + File.separator, "");
        return new File(Environment.getExternalStorageDirectory().toString() + File.separator + folderfilename).exists();
    }

    public static boolean createFolderFile(String folderfilename) {
        folderfilename = folderfilename.replace(Environment.getExternalStorageDirectory() + File.separator.toString(), "");
        return new File(Environment.getExternalStorageDirectory().toString() + File.separator + folderfilename).mkdirs();
    }

    public static boolean checkFolderFileTheSameName(File folderfile, String targetname) {//FIXME
        return folderfile.getName().equals(targetname);
    }


    //http://stackoverflow.com/questions/4894885/how-to-check-file-extension-in-android
    //http://stackoverflow.com/questions/24988384/check-file-extension-in-java-android
    //http://commons.apache.org/proper/commons-io/javadocs/api-1.4/org/apache/commons/io/FilenameUtils.html#getExtension%28java.lang.String%29
    //https://commons.apache.org/proper/commons-io/javadocs/api-1.4/org/apache/commons/io/FilenameUtils.html
    //http://stackoverflow.com/questions/5603966/how-to-make-filefilter-in-java
    public static class TesseractOCRTrainedDataFilter implements FilenameFilter {
        //Add the file extensions you want to look for here:
        private final String[] extension = {"traineddata"};

        @Override
        public boolean accept(File pathname, String assetsname) {
            final String name = assetsname.toLowerCase();
            for (String anExt : extension) {
                if (name.endsWith(anExt)) {
// A file has been detected with that extension
                    return true;
                }
            }
            return false;
        }
    }

    public class FileExtensionFilter implements FilenameFilter {
        private Set<String> exts = new HashSet<String>();

        /**
         * @param extensions a list of allowed extensions, without the dot, e.g.
         *                   <code>"xml","html","rss"</code>
         */
        public FileExtensionFilter(String... extensions) {
            for (String ext : extensions) {
                exts.add("." + ext.toLowerCase().trim());
            }
        }

        @Override
        public boolean accept(File dir, String name) {
            final Iterator<String> extList = exts.iterator();
            while (extList.hasNext()) {
                if (name.toLowerCase().endsWith(extList.next())) {
                    return true;
                }
            }
            return false;
        }
    }

//    public class ExtensionAwareFilenameFilter implements FilenameFilter {
//
//        private final Set<String> extensions;
//
//        public ExtensionAwareFilenameFilter(String... extensions) {
//            this.extensions = extensions == null ?
//                    Collections.emptySet() :
//                    Arrays.stream(extensions)
//                            .map(e -> e.toLowerCase()).collect(Collectors.toSet());
//        }
//
//        @Override
//        public boolean accept(File dir, String name) {
//            return extensions.isEmpty() ||
//                    extensions.contains(getFileExtension(name));
//        }
//
//        private String getFileExtension(String filename) {
//            String ext = null;
//            int i = filename .lastIndexOf('.');
//            if(i != -1 && i < filename .length()) {
//                ext = filename.substring(i+1).toLowerCase();
//            }
//            return ext;
//        }
//    }

//    public class ExtensionFileFilter implements FileFilter {
//        String description;
//
//        String extensions[];
//
//        public ExtensionFileFilter(String description, String extension) {
//            this(description, new String[] { extension });
//        }
//
//        public ExtensionFileFilter(String description, String extensions[]) {
//            if (description == null) {
//                this.description = extensions[0];
//            } else {
//                this.description = description;
//            }
//            this.extensions = (String[]) extensions.clone();
//            toLower(this.extensions);
//        }
//
//        private void toLower(String array[]) {
//            for (int i = 0, n = array.length; i < n; i++) {
//                array[i] = array[i].toLowerCase();
//            }
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public boolean accept(File file) {
//            if (file.isDirectory()) {
//                return true;
//            } else {
//                String path = file.getAbsolutePath().toLowerCase();
//                for (int i = 0, n = extensions.length; i < n; i++) {
//                    String extension = extensions[i];
//                    if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//    }

    public static ArrayList<File> getDirectorys(File dir, ArrayList<File> dirList) {
        final File[] listFile = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    dirList.add(file);
                    getDirectorys(file, dirList);
                }
            }
        }
        return dirList;
    }

    public static ArrayList<File> getExternalStorageDirectorys(ArrayList<File> dirList) {
        return getDirectorys(Environment.getExternalStorageDirectory(), dirList);
    }

    /**
     * 獲取sd卡的路徑
     * Environment.getDataDirectory();// 得到data
     * Environment.getRootDirectory();// 得到system
     * Environment.getDownloadCacheDirectory();// 得到cache
     *
     * @return 路徑的字串
     */
    public static String getSDPath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public static String[] getAssetsFiles(AssetManager am) {
        try {
            return am.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        return null;
    }

    public static String[] getAssetsFilterFiles(AssetManager am, FilenameFilter filter) {
        final ArrayList<String> profiles = new ArrayList<>();
        if (filter == null) {
//            return new ArrayList<>(Arrays.asList(getAssetsFiles(am)));
            return getAssetsFiles(am);
        }

        try {
            String[] rawfiles = am.list("");
            if (rawfiles == null) {
                return null;
            }
            for (String s : rawfiles) {
                if ((filter == null) || filter.accept(null, s)) {
                    profiles.add(s);
                }
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }

        return profiles.toArray(new String[profiles.size()]);
    }

    public static void writeFile(InputStream in, OutputStream out) {
        try {
            final byte[] buffer = new byte[1024];
            int read;
            long total = 0;
            while ((read = in.read(buffer)) != -1) {
//            publishProgress("" + (int) ((total * 100) / lenghtOfFile));//FIXME  if want use Progress
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
        }
    }
}
