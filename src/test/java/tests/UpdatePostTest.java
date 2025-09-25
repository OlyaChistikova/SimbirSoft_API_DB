package tests;

import helpers.BaseRequests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pojo.DataError;
import pojo.DataPost;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpdatePostTest extends BaseTest {
    private Integer postId;

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
        return new Object[][] {
                { 999999 }, // Предположительно несуществующий очень большой ID
                { 0 }      // 0, как потенциально неправильный ID
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
        DataPost responseBeforeGetPost = getPostById(postId, token);
        Assert.assertEquals(responseBeforeGetPost.getStatus(), "publish");

        String titleResponse = responseBeforeGetPost.getTitle().getRendered();
        String contentResponse = responseBeforeGetPost.getContent().getRendered().replace("<p>", "").replace("</p>", "").trim();
        String statusResponse = responseBeforeGetPost.getStatus();

        DataPost requestUpdateBody = createPostBodyWithId(postId, "Обновленный тестовый пост без авторизации", "Это мой обновленный пост без авторизации.", "publish");
        updatePostWithoutAuth(requestUpdateBody, postId);

        DataPost responseAfterGetPost = getPostById(postId, token);
        Assert.assertEquals(responseBeforeGetPost.getId(), responseAfterGetPost.getId());
        Assert.assertEquals(titleResponse, responseAfterGetPost.getTitle().getRendered());
        Assert.assertEquals(contentResponse, responseAfterGetPost.getContent().getRendered().replace("<p>", "").replace("</p>", "").trim());
        Assert.assertEquals(statusResponse, responseAfterGetPost.getStatus());

        checkSuccessPostDb(postId,"Старый пост", "Привет! Это мой старый пост.", "publish");
    }

    /**
     * Успешно обновляет пост по заданным данным.
     *
     * @param requestBody Данные для обновления поста в формате DataPost.
     * @param postId      ID поста, который необходимо обновить.
     * @return Объект обновленного DataPost.
     */
    public DataPost updatePostWithAuthSuccess(DataPost requestBody, Integer postId) {
        return given()
                .spec(BaseRequests.requestSpec(URL, token))
                .body(requestBody)
                .log().body()
                .when()
                .put(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(200)
                .log().all()
                .body("id", equalTo(postId))
                .extract()
                .as(DataPost.class);
    }

    /**
     * Обрабатывает ошибочный случай обновления несуществующего или удаленного поста.
     *
     * @param requestBody Данные для обновления в формате DataError.
     * @param updateId    ID поста, который пытаются обновить.
     */
    public void updateInvalidPost(DataPost requestBody, Integer updateId) {
        given()
                .spec(BaseRequests.requestSpec(URL, token))
                .body(requestBody)
                .when()
                .put(POSTS_PATH + "/" + updateId)
                .then()
                .statusCode(404)
                .body("code", equalTo("rest_post_invalid_id"))
                .body("message", equalTo("Неверный ID записи."))
                .body("data.status", equalTo(404))
                .extract()
                .as(DataError.class);
    }

    /**
     * Обрабатывает ошибочный случай обновления поста без авторизации.
     *
     * @param requestBody Данные для обновления в формате DataError.
     * @param postId      ID поста, который пытаются обновить.
     */
    public void updatePostWithoutAuth(DataPost requestBody, Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(URL))
                .body(requestBody)
                .when()
                .put(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(401)
                .body("code", equalTo("rest_cannot_edit"))
                .body("message", equalTo("Извините, вам не разрешено редактировать эту запись."))
                .body("data.status", equalTo(401))
                .extract()
                .as(DataError.class);
    }

    @BeforeMethod
    public void createPostForUpdate() {
        DataPost requestBody = createPostBody("Старый пост", "Привет! Это мой старый пост.", "publish");
        postId = createPost(requestBody, token).getId();
    }

    @AfterMethod
    public void deleteCreatedPost() {
        deletePostAfterCreation(postId);
    }
}
