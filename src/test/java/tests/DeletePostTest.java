package tests;

import helpers.DataBaseHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static helpers.BaseRequests.*;

public class DeletePostTest extends BaseTest {
    private Integer postId;
    private final DataBaseHelper repo = new DataBaseHelper();

    @BeforeMethod
    public void createPostForDelete() {
        DataPost requestBody = createPostBody("Удаляемый пост", "Привет! Этот пост должен быть удален.", "publish");
        postId = createPost(requestBody, TOKEN).getId();
    }

    @Test
    public void deleteCorrectPostWithAuthTest() {
        deleteCorrectPost(postId);

        List<DataPost> listPosts = getResourceAsList(DataPost.class, POSTS_PATH, TOKEN);
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        Assert.assertFalse(ids.contains(postId));

        checkDeleteDb(postId, "trash");
    }

    @Test
    public void deleteRemovedPostWithAuthTest() {
        deleteCorrectPost(postId);
        deleteAlreadyDeletedPost(postId);

        List<DataPost> listPosts = getResourceAsList(DataPost.class, POSTS_PATH, TOKEN);
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        Assert.assertFalse(ids.contains(postId));

        checkDeleteDb(postId, "trash");
    }

    @Test
    public void deleteCorrectPostWithoutAuthTest() {
        deleteCorrectPostWithoutAuth(postId);

        List<DataPost> listPosts = getResourceAsList(DataPost.class, POSTS_PATH, TOKEN);
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        Assert.assertTrue(ids.contains(postId));

        checkDeleteDb(postId, "publish");

        deleteItemById(POSTS_PATH, postId, TOKEN);
    }
}