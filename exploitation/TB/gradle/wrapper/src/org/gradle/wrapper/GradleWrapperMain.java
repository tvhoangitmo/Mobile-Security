package org.gradle.wrapper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Minimal Gradle wrapper implementation.
 * Downloads the Gradle distribution from gradle-wrapper.properties (distributionUrl),
 * unzips it into ~/.gradle/wrapper/dists, then runs the embedded gradle executable.
 *
 * This is NOT the official Gradle wrapper implementation, but is sufficient for Android Studio / CI usage.
 */
public class GradleWrapperMain {

    public static void main(String[] args) throws Exception {
        File jarFile = jarLocation();
        File wrapperDir = jarFile.getParentFile();
        File propFile = new File(wrapperDir, "gradle-wrapper.properties");
        if (!propFile.exists()) {
            throw new FileNotFoundException("Missing " + propFile.getAbsolutePath());
        }

        Properties p = new Properties();
        try (InputStream in = new FileInputStream(propFile)) {
            p.load(in);
        }

        String distUrl = p.getProperty("distributionUrl");
        if (distUrl == null || distUrl.trim().isEmpty()) {
            throw new IllegalStateException("distributionUrl not set in gradle-wrapper.properties");
        }
        distUrl = distUrl.replace("\\:", ":");

        File gradleUserHome = resolveGradleUserHome();
        File distsBase = new File(gradleUserHome, "wrapper/dists");

        String fileName = distUrl.substring(distUrl.lastIndexOf('/') + 1);
        String distName = fileName.replace(".zip", "");
        String hash = sha256Hex(distUrl).substring(0, 16);

        File distDir = new File(new File(distsBase, distName), hash);
        File marker = new File(distDir, ".installed");

        if (!marker.exists()) {
            installDistribution(distUrl, distDir);
            marker.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(marker)) {
                fw.write("ok");
            }
        }

        File gradleHome = findGradleHome(distDir);
        File gradleExe = gradleExecutable(gradleHome);

        List<String> cmd = new ArrayList<>();
        cmd.add(gradleExe.getAbsolutePath());
        cmd.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.inheritIO();
        Process pr = pb.start();
        int code = pr.waitFor();
        System.exit(code);
    }

    private static File jarLocation() throws Exception {
        URL u = GradleWrapperMain.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(u.toURI());
    }

    private static File resolveGradleUserHome() {
        String env = System.getenv("GRADLE_USER_HOME");
        if (env != null && !env.trim().isEmpty()) {
            return new File(env);
        }
        String prop = System.getProperty("gradle.user.home");
        if (prop != null && !prop.trim().isEmpty()) {
            return new File(prop);
        }
        return new File(System.getProperty("user.home"), ".gradle");
    }

    private static void installDistribution(String distUrl, File distDir) throws Exception {
        distDir.mkdirs();
        File tmpZip = File.createTempFile("gradle-dist", ".zip");
        try {
            download(new URL(distUrl), tmpZip);
            unzip(tmpZip, distDir);
        } finally {
            tmpZip.delete();
        }
    }

    private static void download(URL url, File out) throws Exception {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setInstanceFollowRedirects(true);
        c.setConnectTimeout(30000);
        c.setReadTimeout(30000);
        try (InputStream in = c.getInputStream();
             OutputStream os = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                os.write(buf, 0, r);
            }
        }
    }

    private static void unzip(File zip, File dest) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                File out = new File(dest, e.getName());
                if (e.isDirectory()) {
                    out.mkdirs();
                } else {
                    out.getParentFile().mkdirs();
                    try (OutputStream os = new FileOutputStream(out)) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = zis.read(buf)) != -1) {
                            os.write(buf, 0, r);
                        }
                    }
                    // Make executables runnable on unix
                    if (out.getName().equals("gradle") || out.getName().endsWith(".sh")) {
                        out.setExecutable(true, false);
                    }
                }
            }
        }
    }

    private static File findGradleHome(File distDir) {
        // Most zips extract a single folder like gradle-8.7/...
        File[] kids = distDir.listFiles(File::isDirectory);
        if (kids != null) {
            for (File k : kids) {
                File bin = new File(k, "bin");
                if (bin.exists()) return k;
            }
        }
        // Fallback: if extracted directly
        return distDir;
    }

    private static File gradleExecutable(File gradleHome) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        File bin = new File(gradleHome, "bin");
        if (os.contains("win")) {
            return new File(bin, "gradle.bat");
        }
        return new File(bin, "gradle");
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
