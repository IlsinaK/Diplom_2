package model;

import java.util.UUID;

public class UserGenerator {
    public static UserDataLombok getRandomUser() {
        String email = "test-" + UUID.randomUUID() + "@yandex.ru";
        String password = "password";
        String name = "Username";
        return new UserDataLombok(email, password, name);
    }
}


