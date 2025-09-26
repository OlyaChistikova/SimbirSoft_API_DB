package tests;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static helpers.BaseRequests.*;

public class UpdatePostTest extends BaseTest {
    private Integer postId;

    @BeforeMethod
    public void createPostForUpdate() {
        DataPost requestBody = createPostBody("Старый пост", "Привет! Это мой старый пост.", "publish");
        postId = createPost(requestBody, TOKEN).getId();
    }

    @AfterMethod
    public void deleteCreatedPost() {
        deletePostAfterCreation(postId);
    }

    @Test
    public void updateCorrectPostWithAuthTest() {
        DataPost requestUpdateBody = createPostBodyWithId(postId, "Обновленный тестовый пост", "Это мой обновленный пост.", "publish");
        DataPost responsePost = updatePostWithAuthSuccess(requestUpdateBody, postId);

        Assert.assertEquals(responsePost.getTitle().getRendered(), requestUpdateBody.getTitle().getRendered());
        Assert.assertEquals(responsePost.getContent().getRaw(), requestUpdateBody.getContent().getRaw());
        Assert.assertEquals(responsePost.getStatus(), requestUpdateBody.getStatus());

        List<DataPost> listPosts = getAllPosts();
        String actualContentPost = listPosts.get(0).getContent().getRendered().replace("<p>", "").replace("</p>", "").trim();

        Assert.assertEquals(listPosts.get(0).getId(), postId);
        Assert.assertEquals(listPosts.get(0).getTitle().getRendered(), responsePost.getTitle().getRendered());
        Assert.assertEquals(actualContentPost, responsePost.getContent().getRaw());

        checkSuccessPostDb(postId, "Обновленный тестовый пост", "Это мой обновленный пост.", "publish");
    }

    @DataProvider(name = "updateIdProvider")
    public static Object[][] updateIdProvider() {
        return new Object[][]{
                {999999}, // Предположительно несуществующий очень большой ID
                {0}      // 0, как потенциально неправильный ID
        };
    }

    @Test(dataProvider = "updateIdProvider")
    public void updateExistingPostWithAuthTest(Integer updateId) {
        DataPost requestUpdateBody = createPostBodyWithId(updateId, "Новый обновленный тестовый пост", "Привет! Это мой обновленный пост.", "publish");
        updateInvalidPost(requestUpdateBody, updateId);

        List<DataPost> listPosts = getAllPosts();
        List<String> titles = listPosts.stream()
                .map(post -> post.getTitle().getRendered())
                .collect(Collectors.toList());
        Assert.assertNotEquals(listPosts.get(0).getId(), updateId);
        Assert.assertFalse(titles.get(0).contains(requestUpdateBody.getTitle().getRendered()));

        checkErrorDb(updateId);
    }

    @Test
    public void updateCorrectPostWithoutAuthTest() {
        DataPost responseBeforeGetPost = getPostById(postId);
        Assert.assertEquals(responseBeforeGetPost.getStatus(), "publish");

        String titleResponse = responseBeforeGetPost.getTitle().getRendered();
        String contentResponse = responseBeforeGetPost.getContent().getRendered().replace("<p>", "").replace("</p>", "").trim();
        String statusResponse = responseBeforeGetPost.getStatus();

        DataPost requestUpdateBody = createPostBodyWithId(postId, "Обновленный тестовый пост без авторизации", "Это мой обновленный пост без авторизации.", "publish");
        updatePostWithoutAuth(requestUpdateBody, postId);

        DataPost responseAfterGetPost = getPostById(postId);
        Assert.assertEquals(responseBeforeGetPost.getId(), responseAfterGetPost.getId());
        Assert.assertEquals(titleResponse, responseAfterGetPost.getTitle().getRendered());
        Assert.assertEquals(contentResponse, responseAfterGetPost.getContent().getRendered().replace("<p>", "").replace("</p>", "").trim());
        Assert.assertEquals(statusResponse, responseAfterGetPost.getStatus());

        checkSuccessPostDb(postId, "Старый пост", "Привет! Это мой старый пост.", "publish");
    }
}