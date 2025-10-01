package tests;

import helpers.DataBaseHelper;
import org.testng.Assert;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static helpers.BaseRequests.*;

public class CreatePostTest extends BaseTest {
    private final DataBaseHelper repo = new DataBaseHelper();

    @Test
    public void createPostWithCorrectDataTest() {
        DataPost requestPost = createPostBody("Тестовый заголовок", "Привет! Это мой тестовый пост.", "publish");
        DataPost responsePost = addPostSuccessRequest(requestPost, TOKEN);

        Integer postId = responsePost.getId();
        Assert.assertEquals(requestPost.getTitle().getRaw(), responsePost.getTitle().getRaw());
        Assert.assertEquals(requestPost.getStatus(), responsePost.getStatus());

        List<DataPost> listPosts = getResourceAsList(DataPost.class, POSTS_PATH, TOKEN);
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        List<String> titles = listPosts.stream()
                .map(post -> post.getTitle().getRendered())
                .collect(Collectors.toList());
        Assert.assertTrue(ids.contains(postId));
        Assert.assertTrue(titles.contains(responsePost.getTitle().getRendered()));

        checkSuccessPostDb(postId, requestPost.getTitle().getRaw(), requestPost.getContent().getRaw(), requestPost.getStatus(), repo);

        deleteItemById(POSTS_PATH, postId, TOKEN);
        checkDeleteDb(postId, "trash");
    }

    @Test
    public void createPostWithMinimalDataTest() {
        DataPost requestPost = createPostBody("Тестовый заголовок", null, null);
        DataPost responsePost = addPostSuccessRequest(requestPost, TOKEN);

        Integer postId = responsePost.getId();
        Assert.assertEquals(requestPost.getTitle().getRendered(), responsePost.getTitle().getRendered());
        Assert.assertTrue(responsePost.getContent().getRendered().isEmpty());
        Assert.assertEquals(responsePost.getStatus(), "draft");

        DataPost postById = getItemById(DataPost.class, POSTS_PATH, postId, TOKEN);
        Assert.assertEquals(postById.getTitle().getRendered(), responsePost.getTitle().getRendered());

        checkSuccessPostDb(postId, responsePost.getTitle().getRaw(), responsePost.getContent().getRaw(), responsePost.getStatus(), repo);

        deleteItemById(POSTS_PATH, postId, TOKEN);
        checkDeleteDb(postId, "trash");
    }

    @Test
    public void createPostWithInvalidDataTest() {
        DataPost requestPost = createPostBody(null, null, "publish");
        DataError responsePost = addPostInvalidRequest(requestPost);

        Assert.assertEquals(responsePost.getCode(), "empty_content");
        Assert.assertEquals(responsePost.getMessage(), "Содержимое, заголовок и отрывок пусты.");
        Assert.assertEquals(responsePost.getData().getStatus(), 400);
    }

    @Test
    public void createCorrectPostWithoutAuthTest() {
        DataPost requestPost = createPostBody("Тестовый заголовок без авторизации", "Привет! Это мой тестовый пост без авторизации.", "publish");
        DataError responsePost = addPostWithoutAuth(requestPost);

        Assert.assertEquals(responsePost.getCode(), "rest_cannot_create");
        Assert.assertEquals(responsePost.getMessage(), "Извините, вам не разрешено создавать записи от лица этого пользователя.");
        Assert.assertEquals(responsePost.getData().getStatus(), 401);

        List<DataPost> listPosts = getResourceAsList(DataPost.class, POSTS_PATH, TOKEN);
        List<String> titles = listPosts.stream()
                .map(post -> post.getTitle().getRendered())
                .collect(Collectors.toList());
        List<String> contents = listPosts.stream()
                .map(post -> post.getContent().getRendered())
                .collect(Collectors.toList());
        Assert.assertFalse(titles.contains(requestPost.getTitle().getRendered()));
        Assert.assertFalse(contents.contains(requestPost.getContent().getRendered()));
    }
}