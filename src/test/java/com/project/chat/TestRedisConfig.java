package com.project.chat;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TestRedisConfig implements BeforeAllCallback {

    private static final String REDIS_IMAGE = "redis:latest";
    private static final int PORT = 6379;
    private GenericContainer redis;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        redis = new GenericContainer(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(PORT);

        redis.start();

        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(redis.getMappedPort(PORT)));
    }
}
