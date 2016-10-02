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

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: WrappedException.java,v 1.4 2005/08/30 18:20:23 eherman Exp $
 */
public final class WrappedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param cause
     */
    public WrappedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param msg
     * @param cause
     */
    public WrappedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
