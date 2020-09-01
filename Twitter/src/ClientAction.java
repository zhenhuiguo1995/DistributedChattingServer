public enum ClientAction {
    REGISTER,
    LOGIN,
    LOGOUT,
    CREATE_GROUP,
    JOIN_GROUP,
    POST,
    PULL // pulls all posts within a group, is triggered when a user joins a new group
}
