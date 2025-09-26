package tests;

import helpers.BaseRequests;

import helpers.DataBaseHelper;
import helpers.ParametersProvider;
import org.testng.Assert;
import pojo.DataPost;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BaseTest {

    /**
     * Экземпляр помощника для взаимодействия с базой данных.
     */
    private final DataBaseHelper dbHelper = new DataBaseHelper();

    /**
     * Токен авторизации для доступа к API.
     */
    protected static final String TOKEN = BaseRequests.TOKEN;

    /**
     * Путь для взаимодействия с постами.
     */
    public static final String POSTS_PATH = BaseRequests.POSTS_PATH;

    /**
     * Данные для авторизации
     */
    protected static final String usernameAdmin = ParametersProvider.getProperty("usernameAdmin");
    protected static final String passwordAdmin = ParametersProvider.getProperty("passwordAdmin");
    protected static final String usernameAuthor = ParametersProvider.getProperty("usernameAuthor");
    protected static final String passwordAuthor = ParametersProvider.getProperty("passwordAuthor");

    /**
     * Получение списка всех публичных постов без авторизации
     *
     * @return Список объектов DataPost.
     */
    public List<DataPost> getAllPosts() {
        return given()
                .spec(BaseRequests.requestSpec())
                .when()
                .get(POSTS_PATH)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("", DataPost.class);
    }

    /**
     * Получает пост по ID с авторизацией.
     *
     * @param postId ID поста.
     * @return Объект DataPost с данными поста.
     */
    public DataPost getPostById(Integer postId) {
        return given()
                .spec(BaseRequests.requestSpec(TOKEN))
                .when()
                .get(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .extract().as(DataPost.class);
    }

    /**
     * Удаление поста с заданным id
     *
     * @param postId id поста, который необходимо удалить
     */
    public void deletePostAfterCreation(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(TOKEN))
                .when()
                .delete(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(200);
    }

    /**
     * Проверяет, что пост существует в базе и его параметры совпадают.
     *
     * @param post_id ID поста.
     * @param title   Ожидаемый заголовок.
     * @param content Ожидаемое содержание.
     * @param status  Ожидаемый статус.
     */
    public void checkSuccessPostDb(Integer post_id, String title, String content, String status) {
        Assert.assertTrue(dbHelper.isPostExist(post_id), "Пост не найден в базе");
        Assert.assertEquals(dbHelper.getPostTitle(post_id), title, "Заголовок поста в базе не совпадает");
        Assert.assertEquals(dbHelper.getPostContent(post_id), content, "Содержимое поста в базе не совпадает");
        Assert.assertEquals(dbHelper.getPostStatus(post_id), status, "Статус поста в базе не совпадает");
    }

    /**
     * Проверяет, что пост отсутствует в базе.
     *
     * @param post_id ID поста.
     */
    public void checkErrorDb(Integer post_id) {
        Assert.assertFalse(dbHelper.isPostExist(post_id), "Пост найден в базе");
    }

    /**
     * Проверяет, что пост существует в базе и его статус соответствует ожидаемому.
     *
     * @param post_id ID поста.
     * @param status  Ожидаемый статус.
     */
    public void checkDeleteDb(Integer post_id, String status) {
        Assert.assertTrue(dbHelper.isPostExist(post_id), "Пост найден в базе");
        Assert.assertEquals(dbHelper.getPostStatus(post_id), status, "Статус поста в базе не совпадает");
    }
}