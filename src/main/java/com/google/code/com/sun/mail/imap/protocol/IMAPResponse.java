/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.google.code.com.sun.mail.imap.protocol;

import java.io.IOException;
import java.util.Vector;

import com.google.code.com.sun.mail.iap.Protocol;
import com.google.code.com.sun.mail.iap.ProtocolException;
import com.google.code.com.sun.mail.iap.Response;
import com.google.code.com.sun.mail.util.ASCIIUtility;

/**
 * This class represents a response obtained from the input stream
 * of an IMAP server.
 *
 * @author  John Mani
 */

public class IMAPResponse extends Response {
    private String key;
    private int number;

    public IMAPResponse(Protocol c) throws IOException, ProtocolException {
	super(c);

	// continue parsing if this is an untagged response
	if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
	    key = readAtom();

	    // Is this response of the form "* <number> <command>"
	    try {
		number = Integer.parseInt(key);
		key = readAtom();
	    } catch (NumberFormatException ne) { }
	}
    }

    /**
     * Copy constructor.
     */
    public IMAPResponse(IMAPResponse r) {
	super((Response)r);
	key = r.key;
	number = r.number;
    }

    /**
     * Read a list of space-separated "flag_extension" sequences and 
     * return the list as a array of Strings. An empty list is returned
     * as null.  This is an IMAP-ism, and perhaps this method should 
     * moved into the IMAP layer.
     */
    public String[] readSimpleList() {
	skipSpaces();

	if (buffer[index] != '(') // not what we expected
	    return null;
	index++; // skip '('

	Vector v = new Vector();
	int start;
	for (start = index; buffer[index] != ')'; index++) {
	    if (buffer[index] == ' ') { // got one item
		v.addElement(ASCIIUtility.toString(buffer, start, index));
		start = index+1; // index gets incremented at the top
	    }
	}
	if (index > start) // get the last item
	    v.addElement(ASCIIUtility.toString(buffer, start, index));
	index++; // skip ')'
	
	int size = v.size();
	if (size > 0) {
	    String[] s = new String[size];
	    v.copyInto(s);
	    return s;
	} else  // empty list
	    return null;
    }
    
    public String[] readAtomStringList() {
	skipSpaces();

	if (buffer[index] != '(') // not what we expected
	    return null;
	index++; // skip '('

	Vector v = new Vector();
        String as;
	do {
            as = readAtomString();
            if(as.length() > 0){
                v.addElement(as);                
            }
	} while (buffer[index++] != ')');

	int size = v.size();
	if (size > 0) {
	    String[] s = new String[size];
	    v.copyInto(s);
	    return s;
	} else  // empty list
	    return null;    
    }

    public String getKey() {
	return key;
    }

    public boolean keyEquals(String k) {
	if (key != null && key.equalsIgnoreCase(k))
	    return true;
	else
	    return false;
    }

    public int getNumber() {
	return number;
    }

    public static IMAPResponse readResponse(Protocol p) 
			throws IOException, ProtocolException {
	IMAPResponse r = new IMAPResponse(p);
	if (r.keyEquals("FETCH"))
	    r = new FetchResponse(r);
	return r;
    }
}