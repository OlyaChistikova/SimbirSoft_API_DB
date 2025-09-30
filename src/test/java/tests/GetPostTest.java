package tests;

import helpers.DataBaseHelper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static helpers.BaseRequests.*;

public class GetPostTest extends BaseTest {
    private Integer postId;
    private final DataBaseHelper repo = new DataBaseHelper();

    @AfterMethod
    public void deleteCreatedPost() {
        repo.deletePost(postId);
    }

    @Test
    public void getAllPostsDBTest() {
        DataPost requestPost = createPostBody("Созданная запись в бд", "Контент созданной записи", "publish");
        repo.addPost(requestPost, 1);
        postId = repo.getPostByTitle(requestPost.getTitle().getRaw()).getId();

        List<DataPost> listPosts = getResourceAsList(DataPost.class, POSTS_PATH, TOKEN);
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());

        List<DataPost> dbListPosts = repo.getAllPosts();
        List<Integer> idsDb = dbListPosts.stream().map(DataPost::getId).collect(Collectors.toList());

        Assert.assertTrue(ids.contains(postId));
        Assert.assertTrue(idsDb.containsAll(ids), "Не все ожидаемые IDs из API найдены в списке из базы данных");
    }

    @Test
    public void getPublicPostDBTest() {
        DataPost requestPost = createPostBody("Созданная запись в бд", "Контент созданной записи", "publish");
        repo.addPost(requestPost, 1);
        postId = repo.getPostByTitle(requestPost.getTitle().getRaw()).getId();

        DataPost responsePost = getItemById(DataPost.class, POSTS_PATH, postId, TOKEN);
        String contentResponse = responsePost.getContent().getRendered().replace("<p>", "").replace("</p>", "").trim();

        checkSuccessPostDb(postId, responsePost.getTitle().getRendered(), contentResponse, responsePost.getStatus(), repo);
    }

    @Test
    public void getPrivatePostDBInvalidAuthTest() {
        DataPost requestPost = createPostBody("Созданная приватная запись в бд", "Контент созданной записи", "private");
        repo.addPost(requestPost, 1);
        postId = repo.getPostByTitle(requestPost.getTitle().getRaw()).getId();

        String authOtherHeader = createBasicAuthHeader(usernameAuthor, passwordAuthor);
        DataError responseError = getPostByIdWithInvalidAuth(postId, authOtherHeader);

        Assert.assertEquals(responseError.getCode(), "rest_forbidden");
        Assert.assertEquals(responseError.getMessage(), "Извините, вам не разрешено выполнять данное действие.");
    }

    @Test
    public void getPrivatePostDBWithoutAuthTest() {
        DataPost requestPost = createPostBody("Созданная приватная запись в бд", "Контент созданной записи", "private");
        repo.addPost(requestPost, 1);
        postId = repo.getPostByTitle(requestPost.getTitle().getRaw()).getId();

        DataError responseError = getPostByIdWithoutAuth(postId);

        Assert.assertEquals(responseError.getCode(), "rest_forbidden");
        Assert.assertEquals(responseError.getMessage(), "Извините, вам не разрешено выполнять данное действие.");
    }
}