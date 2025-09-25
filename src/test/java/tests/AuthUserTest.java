package tests;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import java.io.IOException;
import java.util.Base64;

public class AuthUserTest extends BaseTest {
    private Integer postId;
    private String authHeader;

    @Test
    public void authWithToken() throws IOException {
        DataPost responsePost = getPostById(postId, authHeader);
        Assert.assertEquals(responsePost.getId(), postId);
        Assert.assertEquals(responsePost.getStatus(), "private");
    }

    @Test
    public void authWithoutToken() throws IOException {
        DataError responseError = getPostByIdWithoutAuth(postId);
        Assert.assertEquals(responseError.getCode(), "rest_forbidden");
        Assert.assertEquals(responseError.getMessage(), "Извините, вам не разрешено выполнять данное действие.");
        Assert.assertEquals(responseError.getData().getStatus(), 401);
    }

    @Test
    public void authWithInvalidToken() throws IOException {
        String authOtherHeader = createBasicAuthHeader(usernameAuthor, passwordAuthor);
        DataError responseError = getPostByIdWithInvalidAuth(postId, authOtherHeader);
        Assert.assertEquals(responseError.getCode(), "rest_forbidden");
        Assert.assertEquals(responseError.getMessage(), "Извините, вам не разрешено выполнять данное действие.");
        Assert.assertEquals(responseError.getData().getStatus(), 403);
    }

    @BeforeMethod
    public void authorization() throws IOException {
        authHeader = createBasicAuthHeader(usernameAdmin, passwordAdmin);
        createPrivatePost();
    }

    @AfterMethod
    public void deletePost() throws IOException {
        deletePostAfterCreation(postId);
    }

    /**
     * Создает заголовок авторизации в формате Basic Auth.
     *
     * @param username Имя пользователя
     * @param password Пароль пользователя
     * @return строка заголовка с кодировкой Base64, готовая к использованию в HTTP-запросе.
     */
    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Создает приватный пост для тестирования и сохраняет его ID.
     */
    private void createPrivatePost() throws IOException {
        DataPost requestBody = createPostBody("Приватный пост", "Это приватный пост для тестирования.", "private");
        DataPost response = createPost(requestBody, authHeader);
        postId = response.getId();
    }
}