package tests;

import helpers.BaseRequests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CreatePostTest extends BaseTest {

    @Test
    public void createPostWithCorrectDataTest() {
        DataPost requestPost = createPostBody("Тестовый заголовок", "Привет! Это мой тестовый пост.", "publish");
        DataPost responsePost = addPostSuccessRequest(requestPost, token);

        Integer postId = responsePost.getId();
        Assert.assertEquals(requestPost.getTitle().getRaw(), responsePost.getTitle().getRaw());
        Assert.assertEquals(requestPost.getStatus(), responsePost.getStatus());

        List<DataPost> listPosts = getAllPosts();
        List<Integer> ids = listPosts.stream().map(DataPost::getId).collect(Collectors.toList());
        List<String> titles = listPosts.stream()
                .map(post -> post.getTitle().getRendered())
                .collect(Collectors.toList());
        Assert.assertTrue(ids.contains(postId));
        Assert.assertTrue(titles.contains(responsePost.getTitle().getRendered()));

        checkSuccessPostDb(postId, "Тестовый заголовок", "Привет! Это мой тестовый пост.", "publish");

        deletePostAfterCreation(postId);
        checkDeleteDb(postId, "trash");
    }

    @Test
    public void createPostWithMinimalDataTest() {
        DataPost requestPost = createPostBody("Тестовый заголовок",null, null);
        DataPost responsePost = addPostSuccessRequest(requestPost, token);

        Integer postId = responsePost.getId();
        Assert.assertEquals(requestPost.getTitle().getRendered(), responsePost.getTitle().getRendered());
        Assert.assertTrue(responsePost.getContent().getRendered().isEmpty());
        Assert.assertEquals(responsePost.getStatus(), "draft");

        DataPost postById = getPostById(postId, token);
        Assert.assertEquals(postById.getTitle().getRendered(), responsePost.getTitle().getRendered());

        checkSuccessPostDb(postId, "Тестовый заголовок", "", "draft");

        deletePostAfterCreation(postId);
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

        List<DataPost> listPosts = getAllPosts();
        List<String> titles = listPosts.stream()
                .map(post -> post.getTitle().getRendered())
                .collect(Collectors.toList());
        List<String> contents = listPosts.stream()
                .map(post -> post.getContent().getRendered())
                .collect(Collectors.toList());
        Assert.assertFalse(titles.contains(requestPost.getTitle().getRendered()));
        Assert.assertFalse(contents.contains(requestPost.getContent().getRendered()));
    }

    /**
     * Выполняет успешное добавление поста.
     *
     * @param requestBody Данные нового поста в формате DataPost.
     * @param token       Токен авторизации.
     * @return Возвращает объект DataPost с информацией о созданном посте.
     */
    public DataPost addPostSuccessRequest(DataPost requestBody, String token) {
        return given()
                .spec(BaseRequests.requestSpec(URL, token))
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .log().all()
                .extract()
                .as(DataPost.class);
    }

    /**
     * Выполняет попытку создать пост с некорректными данными.
     *
     * @param requestBody Данные ошибки в формате DataPost, содержащие некорректные значения.
     * @return Объект DataError с информацией об ошибке.
     */
    public DataError addPostInvalidRequest(DataPost requestBody) {
        return given()
                .spec(BaseRequests.requestSpec(URL, token))
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(400)
                .log().all()
                .extract()
                .as(DataError.class);
    }

    /**
     * Выполняет попытку создать пост без авторизации.
     *
     * @param requestBody Данные ошибки в формате DataPost.
     * @return Объект DataError с информацией об ошибке.
     */
    public DataError addPostWithoutAuth(DataPost requestBody) {
        return given()
                .spec(BaseRequests.requestSpec(URL))
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(401)
                .log().all()
                .extract()
                .as(DataError.class);
    }
}
