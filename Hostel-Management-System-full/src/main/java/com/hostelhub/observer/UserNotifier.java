package com.hostelhub.observer;

import com.hostelhub.db.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserNotifier implements Observer {
    
    @Override
    public void update(String message, int targetUserId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String inferredType = "COMPLAINT";
            String lower = message != null ? message.toLowerCase() : "";
            if (lower.contains("emergency")) {
                inferredType = "EMERGENCY";
            } else if (lower.contains("visitor")) {
                inferredType = "VISITOR";
            } else if (lower.contains("resolved")) {
                inferredType = "RESOLVED";
            }

            String sql = "INSERT INTO Notifications (user_id, message, type) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, targetUserId);
                stmt.setString(2, message);
                stmt.setString(3, inferredType);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
