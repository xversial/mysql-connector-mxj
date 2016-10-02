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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/* this needs a better name */
/**
 * Represents a command to be executed by the os, like a command line.
 * 
 * Extends <code>java.util.Thread</code> and thus: May execute within the
 * current thread by calling <code>run</code> directly. May be launched as a
 * separate thread by calling <code>start</code>.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: Shell.java,v 1.15 2005/12/01 21:45:31 eherman Exp $
 */
public interface Shell extends Runnable {

    void setEnvironment(String[] envp);

    void setWorkingDir(File workingDir);

    void addCompletionListener(Runnable listener);

    int returnCode();

    boolean hasReturned();

    void destroyProcess();

    String getName();

    boolean isAlive();

    boolean isDaemon();

    void setDaemon(boolean val);

    void join();

    void start();

    public static class Factory {
        public Shell newShell(String[] args, String name, PrintStream out,
                PrintStream err) {
            return new Default(args, name, out, err);
        }
    }

    /*
     * We can't extend Thread because join() is final ... why? For the very good
     * reason reason of wanting developers to use composition in stead of
     * inheritance.
     */
    public static final class Default implements Shell {
        private Thread me;

        private String[] args;

        private String[] envp;

        private File workingDir;

        private PrintStream out;

        private PrintStream err;

        private Integer returnCode;

        private Process p;

        private List listeners;

        private Exceptions exceptions;

        private RuntimeI runtime;

        public Default(String[] args, String name, PrintStream out,
                PrintStream err) {
            this.me = new Thread(this, name);
            /* Think of this just as if this were an extension of Thread */
            /*
             * To avoid mysterious NullPointerExceptions, don't try to .start()
             * this thread here in the constructor. The RULE is: we must not
             * allow any other thread to obtain a reference to a
             * partly-constructed object. Since we pass a 'this' pointer in, if
             * we were to start the other thread, that very RULE could be
             * violated.
             */
            this.args = args;
            this.out = out;
            this.err = err;
            this.envp = null;
            this.workingDir = null;
            this.returnCode = null;
            this.listeners = new ArrayList();
            this.exceptions = new Exceptions();
            this.runtime = new RuntimeI.Default();
        }

        public void setEnvironment(String[] envp) {
            this.envp = envp;
        }

        public void setWorkingDir(File workingDir) {
            this.workingDir = workingDir;
        }

        void setRuntime(RuntimeI runtime) {
            this.runtime = runtime;
        }

        public void run() {
            if (p != null) {
                throw new IllegalStateException("Process already running");
            }
            try {
                returnCode = null;
                p = runtime.exec(args, envp, workingDir);
                captureStdOutAndStdErr();
                returnCode = new Integer(p.waitFor());
            } catch (Exception e) {
                throw exceptions.toRuntime(e);
            } finally {
                // p.destroy();
                p = null;
                for (int i = 0; i < listeners.size(); i++) {
                    new Thread((Runnable) listeners.get(i)).start();
                }
                listeners.clear();
            }
        }

        private void captureStdOutAndStdErr() {
            InputStream pOut = p.getInputStream();
            InputStream pErr = p.getErrorStream();
            new StreamConnector(pOut, out, getName() + " std out").start();
            new StreamConnector(pErr, err, getName() + " std err").start();
        }

        public void addCompletionListener(Runnable listener) {
            if (listener == null) {
                throw new IllegalArgumentException("Listener is null");
            }
            listeners.add(listener);
        }

        public int returnCode() {
            if (!hasReturned()) {
                throw new RuntimeException("Process hasn't returned yet");
            }
            return returnCode.intValue();
        }

        public boolean hasReturned() {
            return returnCode != null;
        }

        public void destroyProcess() {
            if (p != null) {
                p.destroy();
            }
        }

        public String getName() {
            return me.getName();
        }

        public boolean isAlive() {
            return me.isAlive();
        }

        public boolean isDaemon() {
            return me.isDaemon();
        }

        public void setDaemon(boolean val) {
            me.setDaemon(val);
        }

        public void join() {
            new Exceptions.VoidBlock() {
                protected void inner() throws InterruptedException {
                    me.join();
                }
            }.exec();
        }

        public void start() {
            me.start();
        }
    }

    public static class Stub implements Shell {
        public void addCompletionListener(Runnable listener) {
            throw new NotImplementedException(listener);
        }

        public void destroyProcess() {
            throw new NotImplementedException();
        }

        public String getName() {
            throw new NotImplementedException();
        }

        public boolean hasReturned() {
            throw new NotImplementedException();
        }

        public boolean isAlive() {
            throw new NotImplementedException();
        }

        public boolean isDaemon() {
            throw new NotImplementedException();
        }

        public void join() {
            throw new NotImplementedException();
        }

        public int returnCode() {
            throw new NotImplementedException();
        }

        public void run() {
            throw new NotImplementedException();
        }

        public void setDaemon(boolean val) {
            throw new NotImplementedException(new Boolean(val));
        }

        public void setEnvironment(String[] envp) {
            throw new NotImplementedException(envp);
        }

        public void setWorkingDir(File workingDir) {
            throw new NotImplementedException(workingDir);
        }

        public void start() {
            throw new NotImplementedException();
        }
    }
}
