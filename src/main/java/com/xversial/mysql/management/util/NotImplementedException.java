/*
 Copyright (C) 2007-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.xversial.mysql.management.util;

public final class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(Object arg0) {
        super("args: " + arg0);
    }

    public NotImplementedException(Object[] arg0) {
        this(new ListToString().toString(arg0));
    }

    public NotImplementedException(Object arg0, Object arg1) {
        this(new Object[] { arg0, arg1 });
    }

    public NotImplementedException(Object arg0, Object arg1, Object arg2) {
        this(new Object[] { arg0, arg1, arg2 });
    }

    public NotImplementedException(Object arg0, Object arg1, Object arg2,
            Object arg3) {
        this(new Object[] { arg0, arg1, arg2, arg3 });
    }

    public NotImplementedException(Object arg0, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        this(new Object[] { arg0, arg1, arg2, arg3, arg4 });
    }
}
