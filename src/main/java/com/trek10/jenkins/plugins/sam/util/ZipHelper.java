package com.trek10.jenkins.plugins.sam.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.utils.IOUtils;

import hudson.FilePath;

/**
 * @author Trek10, Inc.
 */
public class ZipHelper {

    public static byte[] MAGIC = { 'P', 'K', 0x3, 0x4 };

    /**
     * The method to test if a input stream is a zip archive.
     * 
     * @param in
     *            the input stream to test.
     * @return true if a input stream is a zip archive.
     */
    public static boolean isZipStream(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        boolean isZip = true;
        try {
            in.mark(MAGIC.length);
            for (int i = 0; i < MAGIC.length; i++) {
                if (MAGIC[i] != (byte) in.read()) {
                    isZip = false;
                    break;
                }
            }
            in.reset();
        } catch (IOException e) {
            isZip = false;
        }
        return isZip;
    }

    public static void zipDirectoryContents(FilePath directory, FilePath zipFile)
            throws IOException, InterruptedException {
        ZipOutputStream zipStream = new ZipOutputStream(zipFile.write());
        zipStream.setMethod(ZipEntry.DEFLATED);
        zipStream.setLevel(6);
        addDirectoryToZip(directory, zipStream, directory.getRemote());
        zipStream.close();
    }

    private static void addDirectoryToZip(FilePath directory, ZipOutputStream zipStream, String rootPath)
            throws IOException, InterruptedException {
        for (FilePath file : directory.list()) {
            if (file.isDirectory()) {
                addDirectoryToZip(file, zipStream, rootPath);
                return;
            }
            ZipEntry entry = new ZipEntry(file.getRemote().substring(rootPath.length() + 1));
            entry.setTime(file.lastModified());
            entry.setSize(file.length());
            zipStream.putNextEntry(entry);
            IOUtils.copy(new BufferedInputStream(file.read()), zipStream);
            zipStream.closeEntry();
        }
    }
}
