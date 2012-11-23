/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.webapi.beans;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierPPN {

    private String ppn;

    public IdentifierPPN(String ppn) {
        if (!isValid(ppn)) {
            throw new IllegalArgumentException("Given string is not a valid PPN identifier.");
        }
        this.ppn = ppn;
    }

    public static boolean isValid(String identifier) {
        Boolean result;
        int flags = Pattern.CASE_INSENSITIVE;
        Pattern pattern;
        Matcher matcher;

        if ((identifier == null) || (identifier.length() == 0)) {
            result = false;
        } else {
            pattern = Pattern.compile("^[0-9]{8}[0-9LXYZ]{1}$", flags);
            matcher = pattern.matcher(identifier);
            result = matcher.matches();
        }

        return result;
    }

    public String toString() {
        return ppn;
    }


}
