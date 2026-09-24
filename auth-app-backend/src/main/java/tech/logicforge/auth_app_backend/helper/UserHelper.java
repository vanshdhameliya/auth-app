package tech.logicforge.auth_app_backend.helper;

import java.util.UUID;

public class UserHelper {

    public static UUID parseUUID(String uuid) {

        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }

        return UUID.fromString(uuid.trim());
    }
}
