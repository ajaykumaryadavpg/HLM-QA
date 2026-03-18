package com.tpg.utils;

import com.github.javafaker.Faker;
import com.github.javafaker.service.RandomService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CredentialsGenerator {
    public static Map<String, String> generateCredentials() {
        var faker = new Faker(new Locale("en-US"), new RandomService());
        var username = faker.name().firstName() + faker.number().digits(3);
        var password = "Ateam" + faker.bothify("????###");
        return Map.of("username", username, "password", password);
    }
}
