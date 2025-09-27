package tests;

import helpers.BaseRequests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import static helpers.BaseRequests.*;

public class AuthUserTest extends BaseTest {
    private Integer postId;

    @BeforeMethod
    public void authorization() {
        String authHeader = createBasicAuthHeader(usernameAdmin, passwordAdmin);
        postId = createPrivatePost(authHeader);
    }

    @AfterMethod
    public void deletePost() {
        deleteItemById(POSTS_PATH, postId, TOKEN);
    }

    @Test
    public void authWithToken() {
        DataPost responsePost = getItemById(DataPost.class, POSTS_PATH, postId, TOKEN);
        Assert.assertEquals(responsePost.getId(), postId);
        Assert.assertEquals(responsePost.getStatus(), "private");
    }

    @Test
    public void authWithoutToken() {
        DataError responseError = BaseRequests.getPostByIdWithoutAuth(postId);
        Assert.assertEquals(responseError.getCode(), "rest_forbidden");
        Assert.assertEquals(responseError.getMessage(), "Извините, вам не разрешено выполнять данное действие.");
        Assert.assertEquals(responseError.getData().getStatus(), 401);
    }

    @Test
    public void authWithInvalidToken() {
        String authOtherHeader = createBasicAuthHeader(usernameAuthor, passwordAuthor);
        DataError responseError = getPostByIdWithInvalidAuth(postId, authOtherHeader);
        Assert.assertEquals(responseError.getCode(), "rest_forbidden");
        Assert.assertEquals(responseError.getMessage(), "Извините, вам не разрешено выполнять данное действие.");
        Assert.assertEquals(responseError.getData().getStatus(), 403);
    }
}