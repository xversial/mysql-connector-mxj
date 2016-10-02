/*
 Copyright (C) 2004-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. All rights reserved.
 Use is subject to license terms.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2 as 
 published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.management.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * String utility methods.
 * 
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: Str.java,v 1.11 2005/08/31 01:21:16 eherman Exp $
 */
public final class Str {
    /* merge with other string utility */

    private String newLine;

    public Str() {
        this(System.getProperty("line.separator"));
    }

    public Str(String newLine) {
        this.newLine = newLine;
    }

    public boolean containsIgnoreCase(String searchIn, String searchFor) {

        return searchIn.toLowerCase().indexOf(searchFor.toLowerCase()) != -1;
    }

    public String newLine() {
        return newLine;
    }

    public String[] toStringArray(List strings) {
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    /**
     * convienence method:
     * 
     * @return shortClassName(obj.getClass());
     */
    public String shortClassName(Object obj) {
        return shortClassName(obj.getClass());
    }

    /**
     * returns the unquallified "short" name of a class (no package info)
     * returns "String" for java.lang.String.class returns "Bar" for
     * foo.Bar.class returns "Foo" for Foo.class (in the default package)
     */
    public String shortClassName(Class aClass) {
        String name = aClass.getName();
        int lastDot = name.lastIndexOf('.');
        return name.substring(lastDot + 1);
    }

    /**
     * wrapper method for Class.forName(string) which converts
     * ClassNotFoundException to RuntimeException
     */
    public Class classForName(final String className) {
        return (Class) new Exceptions.Block() {
            protected Object inner() throws ClassNotFoundException {
                return Class.forName(className);
            }
        }.exec();
    }

    /**
     * returns an array of strings as read via a StringReader
     */
    public String[] splitLines(String str) {
        List lines = new ArrayList();
        StringReader stringReader = new StringReader(str);
        final BufferedReader reader = new BufferedReader(stringReader);
        Exceptions.StringBlock block = new Exceptions.StringBlock() {
            protected String inner() throws IOException {
                return reader.readLine();
            }
        };
        while (true) {
            String line = block.exec();
            if (line == null) {
                break;
            }
            lines.add(line);
        }
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    public boolean parseDefaultTrue(Object obj) {
        return obj == null
                || !obj.toString().equalsIgnoreCase(Boolean.FALSE.toString());
    }

    public int parseInt(Object parseMe, int defaultVal, PrintStream err) {
        if (parseMe == null) {
            return defaultVal;
        }
        try {
            String parseString = parseMe.toString().trim();
            if ("".equals(parseString)) {
                return defaultVal;
            }
            return Integer.parseInt(parseString);
        } catch (Exception e) {
            e.printStackTrace(err);
        }
        return defaultVal;
    }
}
