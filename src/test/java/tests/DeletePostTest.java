package tests;

import helpers.BaseRequests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DeletePostTest extends BaseTest {
    private Integer postId;

    @Test
    public void deleteCorrectPostWithAuthTest() {
        deleteCorrectPost(postId);

        List<DataPost> listPosts = getAllPosts();
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        Assert.assertFalse(ids.contains(postId));

        checkDeleteDb(postId, "trash");
    }

    @Test
    public void deleteRemovedPostWithAuthTest() {
        deleteCorrectPost(postId);
        deleteAlreadyDeletedPost(postId);

        List<DataPost> listPosts = getAllPosts();
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        Assert.assertFalse(ids.contains(postId));

        checkDeleteDb(postId, "trash");
    }

    @Test
    public void deleteCorrectPostWithoutAuthTest() {
        deleteCorrectPostWithoutAuth(postId);

        List<DataPost> listPosts = getAllPosts();
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        Assert.assertTrue(ids.contains(postId));

        checkDeleteDb(postId, "publish");

        deletePostAfterCreation(postId);
    }

    /**
     * Выполняет удаление поста с авторизацией и проверяет, что статус изменился на 'trash'.
     *
     * @param postId ID поста, который нужно удалить.
     */
    public void deleteCorrectPost(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(URL, token))
                .when()
                .delete(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("status", equalTo("trash"))
                .extract().as(DataPost.class);
    }

    /**
     * Выполняет попытку удалить уже удаленный пост, ожидая ответ с кодом 410.
     *
     * @param postId ID поста, который уже был удален.
     */
    public void deleteAlreadyDeletedPost(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(URL, token))
                .when()
                .delete(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(410)
                .body("code", equalTo("rest_already_trashed"))
                .body("message", equalTo("Запись уже была удалена."))
                .body("data.status", equalTo(410))
                .extract().as(DataError.class);
    }

    /**
     * Выполняет попытку удалить пост без авторизации и проверяет ответ с кодом 401.
     *
     * @param postId ID поста, который нужно удалить без авторизации.
     */
    public void deleteCorrectPostWithoutAuth(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(URL))
                .when()
                .delete(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(401)
                .body("code", equalTo("rest_cannot_delete"))
                .body("message", equalTo("Извините, вам не разрешено удалять эту запись."))
                .body("data.status", equalTo(401))
                .extract().as(DataError.class);
    }

    @BeforeMethod
    public void createPostForDelete() {
        DataPost requestBody = createPostBody("Удаляемый пост", "Привет! Этот пост должен быть удален.", "publish");
        postId = createPost(requestBody, token).getId();
    }
}
