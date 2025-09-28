package helpers;

import pojo.DataPost;
import pojo.DataUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper {

    /**
     * Параметры подключения к базе данных берутся из файла настроек через класс ParametersProvider.
     */
    private static final String DB_URL = ParametersProvider.getProperty("urlDB");
    private static final String DB_USERNAME = ParametersProvider.getProperty("usernameDB");
    private static final String DB_PASSWORD = ParametersProvider.getProperty("passwordDB");

    /**
     * Внутренний метод для выполнения SQL-запросов на обновление (INSERT, UPDATE, DELETE).
     * Открывает соединение с базой данных, готовит и выполняет SQL-запрос, освобождая ресурсы после выполнения.
     *
     * @param sql    SQL-запрос на обновление
     * @param params Массив параметров для заполнения в запросе
     */
    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD); // Получаем соединение из пула
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Внутренний метод для выполнения SQL-запросов на выборку данных (SELECT).
     * Открывает соединение с базой данных, готовит и выполняет SQL-запрос, преобразуя результат в указанный тип данных.
     *
     * @param sql       SQL-запрос на выборку
     * @param rowMapper Интерфейс RowMapper для преобразования строки ResultSet в объект
     * @param params    Массив параметров для заполнения в запросе
     * @param <T>       Тип возвращаемых данных
     * @return Результат выполнения запроса
     */
    private <T> T executeQuery(String sql, RowMapper<T> rowMapper, Object... params) {
        T result = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                result = rowMapper.map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Интерфейс для преобразования строк ResultSet в объекты.
     *
     * @param <T> Тип объекта, в который преобразуются данные
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Метод для добавления нового поста в базу данных.
     *
     * @param post   Экземпляр класса DataPost, содержащий данные поста
     * @param author ID автора поста
     */
    public void addPost(DataPost post, int author) {
        String sql = "INSERT INTO wp_posts (post_author, post_date, post_date_gmt, post_content, post_title, post_excerpt, post_status, comment_status, ping_status, post_password, post_name, to_ping, pinged, post_modified, post_modified_gmt, post_content_filtered, post_parent, guid, menu_order, post_type, post_mime_type, comment_count) VALUES (?, NOW(), NOW(), ?, ?, '', ?, 'open', 'open', '', '', '', '', NOW(), NOW(), '', 0, '', 0, 'post', '', 0)";
        executeUpdate(sql, author, post.getContent().getRendered(), post.getTitle().getRaw(), post.getStatus());
    }

    /**
     * Метод для добавления нового пользователя в базу данных.
     *
     * @param user Экземпляр класса DataUser, содержащий данные пользователя
     */
    public void addUser(DataUser user) {
        String sqlUser = "INSERT INTO wp_users (user_login, user_pass, user_nicename, user_email, user_url, user_registered, user_activation_key, user_status, display_name) VALUES(?, ?, '', ?, '', NOW(), '', 0, ?)";
        executeUpdate(sqlUser, user.getUsername(), user.getPassword(), user.getEmail(), user.getUsername());
    }

    /**
     * Метод для получения всех постов из базы данных.
     *
     * @return Список объектов DataPost, содержащих все доступные посты
     */
    public List<DataPost> getAllPosts() {
        String sql = "SELECT ID, post_title, post_content, post_status FROM wp_posts";
        return executeQuery(sql, rs -> {
            List<DataPost> posts = new ArrayList<>();
            while (rs.next()) {
                DataPost.Title titleObj = new DataPost.Title(rs.getString("post_title"), "");
                DataPost.Content contentObj = new DataPost.Content(rs.getString("post_content"), "");
                posts.add(new DataPost(rs.getInt("ID"), titleObj, contentObj, rs.getString("post_status")));
            }
            return posts;
        });
    }

    /**
     * Метод для получения поста по его ID.
     *
     * @param id Уникальный идентификатор поста
     * @return Объект DataPost, соответствующий запрошенному посту, или null, если пост не найден
     */
    public DataPost getPostById(int id) {
        String sql = "SELECT ID, post_title, post_content, post_status FROM wp_posts WHERE ID = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) {
                DataPost.Title titleObj = new DataPost.Title(rs.getString("post_title"), "");
                DataPost.Content contentObj = new DataPost.Content(rs.getString("post_content"), "");
                return new DataPost(rs.getInt("ID"), titleObj, contentObj, rs.getString("post_status"));
            }
            return null;
        }, id);
    }

    /**
     * Метод для получения ID поста по его title.
     *
     * @param title Уникальный заголовок поста
     * @return Объект DataPost, соответствующий запрошенному посту, или null, если пост не найден
     */
    public DataPost getPostByTitle(String title) {
        String sql = "SELECT ID, post_title, post_content, post_status FROM wp_posts WHERE post_title = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) {
                DataPost.Title titleObj = new DataPost.Title(rs.getString("post_title"), "");
                DataPost.Content contentObj = new DataPost.Content(rs.getString("post_content"), "");
                return new DataPost(rs.getInt("ID"), titleObj, contentObj, rs.getString("post_status"));
            }
            return null;
        }, title);
    }

    /**
     * Метод для получения данных пользователя по его id.
     *
     * @param id Уникальный логин пользователя
     * @return Объект DataUser, соответствующий запрошенному посту, или null, если пользователь не найден
     */
    public DataUser getUserById(Integer id) {
        String sql = "SELECT ID, user_login, user_email, user_pass FROM wp_users WHERE ID = ?;";
        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return new DataUser(rs.getInt("ID"), rs.getString("user_login"), rs.getString("user_email"), rs.getString("user_pass"));
            }
            return null;
        }, id);
    }

    /**
     * Метод для получения данных пользователя по его username.
     *
     * @param username Уникальный логин пользователя
     * @return Объект DataUser, соответствующий запрошенному посту, или null, если пользователь не найден
     */
    public DataUser getUserByName(String username) {
        String sql = "SELECT ID, user_login, user_email, user_pass FROM wp_users WHERE user_login = ?;";
        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return new DataUser(rs.getInt("ID"), rs.getString("user_login"), rs.getString("user_email"), rs.getString("user_pass"));
            }
            return null;
        }, username);
    }

    /**
     * Метод для удаления поста по его ID.
     *
     * @param id Уникальный идентификатор поста для удаления
     */
    public void deletePost(Integer id) {
        String sql = "DELETE FROM wp_posts WHERE ID = ?";
        executeUpdate(sql, id);
    }

    /**
     * Метод для удаления пользователя по его ID.
     *
     * @param id Уникальный идентификатор пользователя для удаления
     */
    public void deleteUser(Integer id) {
        String sql = "DELETE FROM wp_users WHERE ID = ?";
        executeUpdate(sql, id);
    }
}