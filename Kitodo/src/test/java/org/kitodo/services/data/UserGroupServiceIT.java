/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.services.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;

/**
 * Tests for UserGroupService class.
 */
public class UserGroupServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, CustomResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabaseAndIndex() throws IOException, CustomResponseException {
        // MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        boolean condition = userGroup.getTitle().equals("Admin") && userGroup.getPermission().equals(1);
        assertTrue("User group was not found in database!", condition);
    }

    @Test
    public void shouldRemoveUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("To Remove");
        userGroupService.save(userGroup);
        UserGroup foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(4));
        assertEquals("Additional user group was not inserted in database!", "To Remove", foundUserGroup.getTitle());

        userGroupService.remove(foundUserGroup);
        foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(4));
        assertEquals("Additional user group was not removed from database!", null, foundUserGroup);

        userGroup = new UserGroup();
        userGroup.setTitle("To remove");
        userGroupService.save(userGroup);
        foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(5));
        assertEquals("Additional user group was not inserted in database!", "To remove", foundUserGroup.getTitle());

        userGroupService.remove(5);
        foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(5));
        assertEquals("Additional user group was not removed from database!", null, foundUserGroup);
    }

    @Test
    public void shouldGetPermissionAsString() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        String actual = userGroupService.getPermissionAsString(userGroup);
        assertEquals("Permission string doesn't match to given plain text!", "1", actual);

        userGroup = userGroupService.find(3);
        actual = userGroupService.getPermissionAsString(userGroup);
        assertEquals("Permission string doesn't match to given plain text!", "4", actual);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        int actual = userGroupService.getTasksSize(userGroup);
        assertEquals("Tasks size is not equal to given value!", 2, actual);
    }
}
