package tests;

import helpers.BaseRequests;

import helpers.DataBaseHelper;
import org.testng.Assert;
import pojo.DataError;
import pojo.DataPost;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BaseTest {
    /**
     * Экземпляр помощника для взаимодействия с базой данных.
     */
    private final DataBaseHelper dbHelper = new DataBaseHelper();

    /**
     * Токен авторизации для доступа к API.
     */
    protected static final String token = "T2x5YS5DaGlzdGlrb3ZhOjEyMy1UZXN0LkNoaXN0aWtvdmE=";

    /**
     * Путь базового url.
     */
    protected static final String URL = "http://localhost:8000/";

    /**
     * Путь для взаимодействия с постами.
     */
    public static final String POSTS_PATH = "index.php?rest_route=/wp/v2/posts";

    /**
     * Данные для авторизации
     */
    protected static final String usernameAdmin = "Olya.Chistikova";
    protected static final String passwordAdmin = "123-Test.Chistikova";
    protected static final String usernameAuthor = "Test_User";
    protected static final String passwordAuthor = "test_Password-123";

    /**
     * Создает тестовую сущность.
     *
     * @param titleText   Текст заголовка поста.
     * @param contentText Текст содержимого поста.
     * @param statusText  Статус поста.
     * @return Объект DataPost с тестовыми данными.
     */
    public DataPost createPostBody(String titleText, String contentText, String statusText) {
        return DataPost.builder()
                .title(DataPost.Title.builder()
                        .raw(titleText)
                        .rendered(titleText)
                        .build())
                .content(DataPost.Content.builder()
                        .raw(contentText)
                        .rendered(contentText)
                        .build())
                .status(statusText)
                .build();
    }

    /**
     * Создает объект DataPost с заданным ID и тестовыми данными.
     *
     * @param id           ID поста.
     * @param titleText    Текст заголовка поста.
     * @param contentText  Текст содержимого поста.
     * @param statusText   Статус поста.
     * @return Объект DataPost с заполненными полями.
     */
    public DataPost createPostBodyWithId(Integer id, String titleText, String contentText, String statusText) {
        return DataPost.builder()
                .id(id)
                .title(DataPost.Title.builder()
                        .raw(titleText)
                        .rendered(titleText)
                        .build())
                .content(DataPost.Content.builder()
                        .raw(contentText)
                        .rendered(contentText)
                        .build())
                .status(statusText)
                .build();
    }

    /**
     * Создает новый пост
     *
     * @param requestBody Данные тела поста для создания.
     * @param token       Токен авторизации.
     * @return Созданный пост формата DataPost
     */
    public DataPost createPost(DataPost requestBody, String token) {
        return given()
                .spec(BaseRequests.requestSpec(URL, token))
                .body(requestBody)
                .log().body()
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().as(DataPost.class);
    }

    /**
     * Получение списка всех публичных постов без авторизации
     *
     * @return Список объектов DataPost.
     */
    public List<DataPost> getAllPosts() {
        return given()
                .spec(BaseRequests.requestSpec(URL))
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
     * @param token  Токен авторизации.
     * @return Объект DataPost с данными поста.
     */
    public DataPost getPostById(Integer postId, String token) {
        return given()
                .spec(BaseRequests.requestSpec(URL, token))
                .when()
                .get(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .log().all()
                .extract().as(DataPost.class);
    }

    /**
     * Получает ошибку при обращении к посту без авторизации.
     *
     * @param postId ID поста.
     * @return Объект DataError с информацией об ошибке.
     */
    public DataError getPostByIdWithoutAuth(Integer postId) {
        return given()
                .spec(BaseRequests.requestSpec(URL))
                .when()
                .get(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(401)
                .log().all()
                .extract().as(DataError.class);
    }

    /**
     * Получает ошибку при обращении к посту с некорректным токеном.
     *
     * @param postId ID поста.
     * @param token  Некорректный токен.
     * @return Объект DataError с информацией об ошибке.
     */
    public DataError getPostByIdWithInvalidAuth(Integer postId, String token) {
        return given()
                .spec(BaseRequests.requestSpec(URL, token))
                .when()
                .get(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(403)
                .log().all()
                .extract().as(DataError.class);
    }

    /**
     * Удаление поста с заданным id
     *
     * @param postId id поста, который необходимо удалить
     */
    public void deletePostAfterCreation(Integer postId) {
        String token = "T2x5YS5DaGlzdGlrb3ZhOjEyMy1UZXN0LkNoaXN0aWtvdmE=";
        given()
                .spec(BaseRequests.requestSpec(URL, token))
                .when()
                .delete(POSTS_PATH + "/" + postId)
                .then()
                .log().all()
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
    public void checkSuccessPostDb(Integer post_id, String title, String content, String status){
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
    public void checkErrorDb(Integer post_id){
        Assert.assertFalse(dbHelper.isPostExist(post_id), "Пост найден в базе");
    }

    /**
     * Проверяет, что пост существует в базе и его статус соответствует ожидаемому.
     *
     * @param post_id ID поста.
     * @param status  Ожидаемый статус.
     */
    public void checkDeleteDb(Integer post_id, String status){
        Assert.assertTrue(dbHelper.isPostExist(post_id), "Пост найден в базе");
        Assert.assertEquals(dbHelper.getPostStatus(post_id), status, "Статус поста в базе не совпадает");
    }
}
