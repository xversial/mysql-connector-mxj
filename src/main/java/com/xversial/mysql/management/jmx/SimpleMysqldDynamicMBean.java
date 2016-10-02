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
package com.xversial.mysql.management.jmx;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import com.xversial.mysql.management.MysqldFactory;
import com.xversial.mysql.management.MysqldResourceI;
import com.xversial.mysql.management.util.DefaultsMap;
import com.xversial.mysql.management.util.Exceptions;
import com.xversial.mysql.management.util.Str;

/**
 * MySQL DynamicMBean
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: SimpleMysqldDynamicMBean.java,v 1.1 2005/02/16 21:46:11 eherman
 *          Exp $
 */
public class SimpleMysqldDynamicMBean implements DynamicMBean {

    public static final String AUTOSTART_ATTR = "autostart";

    public static final String START_METHOD = "startMysqld";

    public static final String STOP_METHOD = "stop";

    private static final String VERSION = "$Id: SimpleMysqldDynamicMBean.java,v 1.15 2005/08/31 01:21:16 eherman Exp $";

    private MysqldFactory mysqldFactory;

    private MysqldResourceI mysqldResource;

    private MBeanAttributeInfo[] attrInfos;

    private MBeanConstructorInfo[] consInfo;

    private List mBeanOperationInfoList;

    private MBeanOperationInfo startMysqldOp;

    private MBeanOperationInfo stopOp;

    private MBeanNotificationInfo[] notesInfos;

    private MBeanInfo mbeanInfo;

    private DefaultsMap attributes;

    private Str str;

    // private Object[] lastInvocation;

    /**
     * Directs output generated by MySQL to Standard Out and Standard Error.
     * This is the constructor included in the MBeanInfo.
     */
    public SimpleMysqldDynamicMBean() {
        this(new MysqldFactory.Default());
    }

    /**
     * This constructor is useful for tests which need to have "stub" or "mock"
     * implementations of the underlying MySQL resource. This constructor is not
     * included int the MBeanInfo.
     */
    protected SimpleMysqldDynamicMBean(MysqldFactory mysqldFactory) {
        this.mysqldFactory = mysqldFactory;
        this.str = new Str();
        this.notesInfos = new MBeanNotificationInfo[0];
        this.mBeanOperationInfoList = new ArrayList();
        this.startMysqldOp = newVoidMBeanOperation(START_METHOD, "Start MySQL");
        this.stopOp = newVoidMBeanOperation(STOP_METHOD, "Stop MySQL");
        getMBeanOperationInfoList().add(startMysqldOp);
        initAttributes();
        initConstructors();
    }

    protected List getMBeanOperationInfoList() {
        return mBeanOperationInfoList;
    }

    private void initConstructors() {
        Constructor[] constructors = this.getClass().getConstructors();
        Constructor con = null;
        for (int i = 0; con == null && i < constructors.length; i++) {
            if (constructors[i].getParameterTypes().length == 0)
                con = constructors[i];
        }
        consInfo = new MBeanConstructorInfo[1];
        consInfo[0] = new MBeanConstructorInfo("MySQL", con);
    }

    private void initAttributes() {
        attributes = new DefaultsMap();
        Map options = new TreeMap(mysqldFactory.newMysqldResource(null, null,
                null).getServerOptions());
        List attInfs = new ArrayList();
        makeAttribute(attInfs, AUTOSTART_ATTR, Boolean.FALSE.toString(), true);
        for (Iterator iter = options.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String attName = (String) entry.getKey();
            String attValue = (String) entry.getValue();
            makeAttribute(attInfs, attName, attValue, true);
        }
        makeAttribute(attInfs, versionAttributeName(), VERSION, false);
        this.attrInfos = (MBeanAttributeInfo[]) attInfs
                .toArray(new MBeanAttributeInfo[attInfs.size()]);
    }

    private void makeAttribute(List attInfs, String attName, String attValue,
            boolean isWritable) {
        Attribute attr = new Attribute(attName, attValue);
        String description = "";
        MBeanAttributeInfo atInfo = new MBeanAttributeInfo(attName,
                String.class.getName(), description, true, isWritable, false);

        attributes.put(attName, attr);
        attInfs.add(atInfo);
    }

    /**
     * @param attributeName
     *            represents a command line argument to a MySQL database.
     * @return a String with the value associated the argument
     */
    public synchronized Object getAttribute(String attributeName)
            throws AttributeNotFoundException {
        Attribute attribute = ((Attribute) attributes.get(attributeName));
        if (attribute == null) {
            throw new AttributeNotFoundException(attributeName);
        }
        return attribute.getValue();
    }

    /**
     * Given an array of command line option names, an "AttributeList" is
     * created containing those option names and the values for each. Called by
     * a JMX Agent
     */
    public synchronized AttributeList getAttributes(String[] attributeNames) {
        AttributeList list = new AttributeList(attributeNames.length);
        for (int i = 0; i < attributeNames.length; i++) {
            Attribute att = (Attribute) attributes.get(attributeNames[i]);
            list.add(att);
        }
        return list;
    }

    /**
     * Returns an MBeanInfo object knowing the class name, what attributes are
     * available, the constructor information, and either the start or stop
     * method information.
     */
    public synchronized MBeanInfo getMBeanInfo() {
        if (mbeanInfo == null) {
            mbeanInfo = new MBeanInfo(this.getClass().getName(), "MySQL MBean",
                    attrInfos, consInfo, opsInfo(), notesInfos);
        }
        return mbeanInfo;
    }

    private MBeanOperationInfo[] opsInfo() {
        int length = getMBeanOperationInfoList().size();
        MBeanOperationInfo[] array = new MBeanOperationInfo[length];
        return (MBeanOperationInfo[]) getMBeanOperationInfoList()
                .toArray(array);
    }

    /**
     * Allows the JMX Agent to pass along calls of "startMysqld" or
     * "startMysqld" with no args of any type
     */
    public synchronized Object invoke(String methodName, Object args[],
            String types[]) throws ReflectionException {
        // lastInvocation = new Object[] { methodName, args, types };

        clearMBeanInfo();

        if (methodName.equals(START_METHOD)) {
            if (mysqldResource == null || !mysqldResource.isRunning()) {
                newMysqldResource();
            }
            mysqldResource.start("MysqldMBean", attributesToOpionMap(), true);
            getMBeanOperationInfoList().remove(startMysqldOp);
            getMBeanOperationInfoList().add(stopOp);
            freezeAttributes();
            return null;
        }
        if (methodName.equals(STOP_METHOD)) {
            if (mysqldResource == null) {
                newMysqldResource();
            }
            mysqldResource.shutdown();
            getMBeanOperationInfoList().remove(stopOp);
            getMBeanOperationInfoList().add(startMysqldOp);
            initAttributes();
            return null;
        }

        String msg = methodName + " not implemented";
        throw new ReflectionException(new NoSuchMethodException(msg), msg);
    }

    private void newMysqldResource() {
        File baseDir = getFileAttribute(MysqldResourceI.BASEDIR);
        File dataDir = getFileAttribute(MysqldResourceI.DATADIR);
        String version = getStringAttribute(MysqldResourceI.MYSQLD_VERSION);
        mysqldResource = mysqldFactory.newMysqldResource(baseDir, dataDir,
                version);
    }

    protected void clearMBeanInfo() {
        this.mbeanInfo = null;
    }

    void freezeAttributes() {
        boolean isWritable = false;
        for (int i = 0; i < attrInfos.length; i++) {
            MBeanAttributeInfo info = attrInfos[i];
            attrInfos[i] = new MBeanAttributeInfo(info.getName(), info
                    .getType(), info.getDescription(), info.isReadable(),
                    isWritable, info.isIs());
        }
    }

    Map attributesToOpionMap() {
        Map options = new HashMap();
        for (Iterator iter = attributes.getChanged().values().iterator(); iter
                .hasNext();) {
            Attribute at = (Attribute) iter.next();
            options.put(at.getName(), at.getValue());
        }
        options.remove(AUTOSTART_ATTR);
        return options;
    }

    /**
     * Allows the JMX Agent to set a command line option to be used when MySQL
     * is launched.
     * 
     * @throws AttributeNotFoundException
     *             ReflectionException
     */
    public synchronized void setAttribute(Attribute attribute)
            throws AttributeNotFoundException {

        String name = attribute.getName();
        if (!attributes.containsKey(name)) {
            // new RuntimeException("attempting to set '" + name + "'")
            // .printStackTrace();
            throw new AttributeNotFoundException(name);
        }
        attributes.put(name, attribute);
    }

    /**
     * Allows the JMX Agent to set a number of command line options at once.
     */
    public synchronized AttributeList setAttributes(AttributeList attributes) {
        for (int i = 0; i < attributes.size(); i++) {
            final Attribute att = (Attribute) attributes.get(i);
            new Exceptions.VoidBlock() {
                public void inner() throws Exception {
                    setAttribute(att);
                }
            }.exec();
        }
        return attributes;
    }

    MysqldResourceI getMysqldResource() {
        if (mysqldResource == null) {
            newMysqldResource();
        }
        return mysqldResource;
    }

    String versionAttributeName() {
        return "version_of_" + str.shortClassName(getClass());
    }

    protected MBeanOperationInfo newVoidMBeanOperation(String method,
            String description) {
        return new MBeanOperationInfo(method, description,
                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION);
    }

    private File getFileAttribute(String key) {
        String fileName = getStringAttribute(key);
        return fileName == null ? null : new File(fileName);
    }

    private String getStringAttribute(final String key) {
        if (!attributes.containsKey(key)) {
            return null;
        }
        Exceptions.Block block = new Exceptions.Block() {
            protected Object inner() throws Exception {
                return getAttribute(key);
            }
        };
        return (String) block.exec();
    }
}