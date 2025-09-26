package helpers;

import java.sql.*;

public class DataBaseHelper {

    private Connection getConnection() throws SQLException {
        String url = ParametersProvider.getProperty("urlDB");
        String username = ParametersProvider.getProperty("usernameDB");
        String password = ParametersProvider.getProperty("passwordDB");
        return DriverManager.getConnection(url, username, password);
    }

    public boolean isPostExist(Integer postId) {
        String query = "SELECT COUNT(*) FROM wp_posts WHERE id = ?";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPostStatus(Integer postId) {
        String query = "SELECT post_status FROM wp_posts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("post_status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPostTitle(Integer postId) {
        String query = "SELECT post_title FROM wp_posts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("post_title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPostContent(Integer postId) {
        String query = "SELECT post_content FROM wp_posts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("post_content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}