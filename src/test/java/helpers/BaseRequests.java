package helpers;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import pojo.DataError;
import pojo.DataPost;

import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


public class BaseRequests {

    /**
     * Токен авторизации для доступа к API.
     */
    public static final String TOKEN = ParametersProvider.getProperty("token");

    /**
     * Путь для взаимодействия с постами.
     */
    public static final String POSTS_PATH = ParametersProvider.getProperty("posts_path");

    public static RequestSpecification requestSpec(String authToken) {
        return new RequestSpecBuilder()
                .setBaseUri(ParametersProvider.getProperty("apiUrl"))
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Basic " + authToken)
                .build();
    }

    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ParametersProvider.getProperty("apiUrl"))
                .setContentType(ContentType.JSON)
                .build();
    }

    /**
     * Создает заголовок авторизации в формате Basic Auth.
     *
     * @param username Имя пользователя
     * @param password Пароль пользователя
     * @return строка заголовка с кодировкой Base64, готовая к использованию в HTTP-запросе.
     */
    public static String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Создает приватный пост для тестирования и сохраняет его ID.
     */
    public static Integer createPrivatePost(String token) {
        DataPost requestBody = createPostBody("Приватный пост", "Это приватный пост для тестирования.", "private");
        DataPost response = createPost(requestBody, token);
        return response.getId();
    }

    /**
     * Создает новый пост
     *
     * @param requestBody Данные тела поста для создания.
     * @param token       Токен авторизации.
     * @return Созданный пост формата DataPost
     */
    public static DataPost createPost(DataPost requestBody, String token) {
        return given()
                .spec(BaseRequests.requestSpec(token))
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().as(DataPost.class);
    }

    /**
     * Создает тестовую сущность.
     *
     * @param titleText   Текст заголовка поста.
     * @param contentText Текст содержимого поста.
     * @param statusText  Статус поста.
     * @return Объект DataPost с тестовыми данными.
     */
    public static DataPost createPostBody(String titleText, String contentText, String statusText) {
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
     * @param id          ID поста.
     * @param titleText   Текст заголовка поста.
     * @param contentText Текст содержимого поста.
     * @param statusText  Статус поста.
     * @return Объект DataPost с заполненными полями.
     */
    public static DataPost createPostBodyWithId(Integer id, String titleText, String contentText, String statusText) {
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
     * Получает ошибку при обращении к посту без авторизации.
     *
     * @param postId ID поста.
     * @return Объект DataError с информацией об ошибке.
     */
    public static DataError getPostByIdWithoutAuth(Integer postId) {
        return given()
                .spec(BaseRequests.requestSpec())
                .when()
                .get(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(401)
                .body("data.status", equalTo(401))
                .extract().as(DataError.class);
    }

    /**
     * Получает ошибку при обращении к посту с некорректным токеном.
     *
     * @param postId ID поста.
     * @param token  Некорректный токен.
     * @return Объект DataError с информацией об ошибке.
     */
    public static DataError getPostByIdWithInvalidAuth(Integer postId, String token) {
        return given()
                .spec(BaseRequests.requestSpec(token))
                .when()
                .get(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(403)
                .body("data.status", equalTo(403))
                .extract().as(DataError.class);
    }

    /**
     * Выполняет успешное добавление поста.
     *
     * @param requestBody Данные нового поста в формате DataPost.
     * @param token       Токен авторизации.
     * @return Возвращает объект DataPost с информацией о созданном посте.
     */
    public static DataPost addPostSuccessRequest(DataPost requestBody, String token) {
        return given()
                .spec(BaseRequests.requestSpec(token))
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .as(DataPost.class);
    }

    /**
     * Выполняет попытку создать пост с некорректными данными.
     *
     * @param requestBody Данные ошибки в формате DataPost, содержащие некорректные значения.
     * @return Объект DataError с информацией об ошибке.
     */
    public static DataError addPostInvalidRequest(DataPost requestBody) {
        return given()
                .spec(BaseRequests.requestSpec(TOKEN))
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(400)
                .extract()
                .as(DataError.class);
    }

    /**
     * Выполняет попытку создать пост без авторизации.
     *
     * @param requestBody Данные ошибки в формате DataPost.
     * @return Объект DataError с информацией об ошибке.
     */
    public static DataError addPostWithoutAuth(DataPost requestBody) {
        return given()
                .spec(BaseRequests.requestSpec())
                .body(requestBody)
                .when()
                .post(POSTS_PATH)
                .then()
                .statusCode(401)
                .extract()
                .as(DataError.class);
    }


    /**
     * Выполняет удаление поста с авторизацией и проверяет, что статус изменился на 'trash'.
     *
     * @param postId ID поста, который нужно удалить.
     */
    public static void deleteCorrectPost(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(TOKEN))
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
    public static void deleteAlreadyDeletedPost(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec(TOKEN))
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
    public static void deleteCorrectPostWithoutAuth(Integer postId) {
        given()
                .spec(BaseRequests.requestSpec())
                .when()
                .delete(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(401)
                .body("code", equalTo("rest_cannot_delete"))
                .body("message", equalTo("Извините, вам не разрешено удалять эту запись."))
                .body("data.status", equalTo(401))
                .extract().as(DataError.class);
    }


    /**
     * Успешно обновляет пост по заданным данным.
     *
     * @param requestBody Данные для обновления поста в формате DataPost.
     * @param postId      ID поста, который необходимо обновить.
     * @return Объект обновленного DataPost.
     */
    public static DataPost updatePostWithAuthSuccess(DataPost requestBody, Integer postId) {
        return given()
                .spec(BaseRequests.requestSpec(TOKEN))
                .body(requestBody)
                .when()
                .put(POSTS_PATH + "/" + postId)
                .then()
                .statusCode(200)
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
    public static void updateInvalidPost(DataPost requestBody, Integer updateId) {
        given()
                .spec(BaseRequests.requestSpec(TOKEN))
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
    public static void updatePostWithoutAuth(DataPost requestBody, Integer postId) {
        given()
                .spec(BaseRequests.requestSpec())
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
}