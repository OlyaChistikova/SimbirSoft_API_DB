package tests;

import helpers.DataBaseHelper;
import org.testng.Assert;
import org.testng.annotations.*;
import pojo.DataUser;
import pojo.User;

import java.util.List;
import java.util.stream.Collectors;

import static helpers.BaseRequests.createUserBody;

public class GetUserTest extends BaseTest {
    private Integer userId;
    private final DataBaseHelper repo = new DataBaseHelper();

    @BeforeMethod
    public void createUserDB() {
        DataUser requestPost = createUserBody(usernameTest, emailTest, passwordTest);
        repo.addUser(requestPost);
        userId = repo.getUserByName(requestPost.getUsername()).getId();
    }

    @Test
    public void getAllUsersDBTest() {
        List<User> listUsers = getResourceAsList(User.class, USERS_PATH, TOKEN);
        List<Integer> ids = listUsers.stream().map(User::getId).collect(Collectors.toList());

        Assert.assertTrue(listUsers.size() > 1);
        Assert.assertTrue(ids.contains(userId));
    }

    @Test
    public void getUserDBTest() {
        User responseUser = getItemById(User.class, USERS_PATH, userId, TOKEN);
        checkSuccessUserDb(userId, responseUser.getName());
    }

    @AfterMethod
    public void deleteUserDB() {
        repo.deleteUser(userId);
    }
}