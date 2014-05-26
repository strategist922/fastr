/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.test.tools.cmpr;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.oracle.truffle.r.runtime.*;

/**
 * Compare the FastR versions of .R files in the standard packages against GnuR. Removes all
 * formatting to perform the check, replacing all whitespace (including newlines) with exactly one
 * space.
 *
 */
public class CompareLibR {

    private static class FileContent {
        String name;
        String content;
        String flattened;

        FileContent(String name, String content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) throws Exception {
        // Checkstyle: stop system print check
        String gnurHome = null;
        String lib = null;
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            switch (arg) {
                case "--gnurhome":
                    i++;
                    gnurHome = args[i];
                    break;
                case "--lib":
                    i++;
                    lib = args[i];
                    break;
                default:
                    usage();
            }
            i++;
        }

        if (gnurHome == null) {
            usage();
        }

        Map<String, FileContent> fastRFiles = getFastR(lib);
        Map<String, FileContent> gnuRFiles = getGnuR(gnurHome, lib, fastRFiles);
        flatten(gnuRFiles);
        flatten(fastRFiles);
        for (Map.Entry<String, FileContent> entry : fastRFiles.entrySet()) {
            FileContent fastR = entry.getValue();
            String fileName = entry.getKey();
            FileContent gnuR = gnuRFiles.get(fileName);
            if (gnuR == null) {
                System.out.println("FastR has file: " + fileName + " not found in GnuR");
            } else {
                if (!fastR.flattened.equals(gnuR.flattened)) {
                    System.out.println(fileName + " differs");
                } else {
                    System.out.println(fileName + " is identical (modulo formatting)");
                }
            }
        }
    }

    private static String flatten(String s) {
        String ss = s;
        ss = ss.replace('\n', ' ');
        ss = ss.replace('\t', ' ');
        int i = 0;
        int psx = 0;
        StringBuffer sb = new StringBuffer();
        int len = ss.length();
        while (i < len) {
            int sx = ss.indexOf("  ", psx);
            if (sx < 0) {
                break;
            }
            sb.append(ss.substring(psx, sx + 1));
            psx = sx + 2;
            while (psx < len && ss.charAt(psx) == ' ') {
                psx++;
            }
        }
        sb.append(ss.substring(psx));
        return sb.toString();
    }

    private static void flatten(Map<String, FileContent> map) {
        for (Map.Entry<String, FileContent> entry : map.entrySet()) {
            FileContent fc = entry.getValue();
            fc.flattened = flatten(fc.content);
        }
    }

    private static Map<String, FileContent> getGnuR(String gnurHome, String lib, Map<String, FileContent> filter) throws IOException {
        FileSystem fs = FileSystems.getDefault();
        Path baseR = fs.getPath(lib, "R");
        Path library = fs.getPath(gnurHome, "src", "library");
        baseR = library.resolve(baseR);
        Map<String, FileContent> result = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseR)) {
            for (Path entry : stream) {
                String entryName = entry.getFileName().toString();
                if (entryName.endsWith(".R") && (filter.get(entryName) != null)) {
                    File file = entry.toFile();
                    byte[] buf = new byte[(int) file.length()];
                    try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream(file))) {
                        bs.read(buf);
                        result.put(entryName, new FileContent(entryName, new String(buf)));
                    }
                }
            }
        }
        return result;
    }

    private static String toFirstUpper(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static Map<String, FileContent> getFastR(String lib) throws Exception {
        Class<?> klass = Class.forName("com.oracle.truffle.r.nodes.builtin." + lib + "." + toFirstUpper(lib) + "Package");
        InputStream is = ResourceHandlerFactory.getHandler().getResourceAsStream(klass, "R");
        Map<String, FileContent> result = new HashMap<>();
        if (is == null) {
            return result;
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.endsWith(".r") || line.endsWith(".R")) {
                    String fileName = line.trim();
                    final String rResource = "R/" + fileName;
                    String content = Utils.getResourceAsString(klass, rResource, true);
                    result.put(fileName, new FileContent(fileName, content));
                }
            }
        }
        return result;
    }

    private static void usage() {
        // Checkstyle: stop system print check
        System.err.println("usage: --gnurhome path --lib lib");
        System.exit(1);
    }

}