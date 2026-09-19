package com.example.demo.userServicePerformance.actions;

import com.example.demo.userServicePerformance.SharedTestData;
import io.gatling.javaapi.core.ChainBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

@Slf4j
public class UserActions {

    private static final AtomicInteger USER_COUNTER = new AtomicInteger(0);

    private static final LocalDate DEFAULT_DATE =
            LocalDate.of(1, 1, 1);

    public static ChainBuilder createUsers(int count) {
        return repeat(count).on(
                        exec(session -> {
                            int index = USER_COUNTER.incrementAndGet();
                            String birthDate =
                                    DEFAULT_DATE
                                            .plusDays(index)
                                            .toString();

                            return session
                                    .set("name", "John" + index)
                                    .set("surname", "Doe" + index)
                                    .set("username",
                                            "john" + index + "_" + UUID.randomUUID())
                                    .set("birthDate", birthDate)
                                    .set("residence", "Bratislava" + index);

                        })
                                .exec(
                                        http("create-user")
                                                .post("/api/v1/users")
                                                .body(StringBody(session -> """
                                                        {
                                                          "name":"%s",
                                                          "surname":"%s",
                                                          "username":"%s",
                                                          "birthDate":"%s",
                                                          "residence":"%s"
                                                        }
                                                        """.formatted(
                                                        session.getString("name"),
                                                        session.getString("surname"),
                                                        session.getString("username"),
                                                        session.getString("birthDate"),
                                                        session.getString("residence")
                                                )))
                                                .check(status().is(201))
                                                .check(jsonPath("$.id").saveAs("createdUserId"))
                                                .check(responseTimeInMillis().saveAs("responseTime"))
                                )
                                .exec(session -> {

                                    ArrayList<String> userIds =
                                            session.contains("userIds")
                                                    ? new ArrayList<>(session.getList("userIds"))
                                                    : new ArrayList<>();
                                    userIds.add(session.getString("createdUserId"));

                                    if (session.contains("responseTime")) {
                                        int responseTime = session.getInt("responseTime");
                                        if (responseTime > 50) {
                                            log.warn(
                                                    "SLOW REQUEST | create-user | username={} | responseTime={} ms",
                                                    session.getString("username"),
                                                    responseTime
                                            );
                                        }
                                    }
                                    return session.set("userIds", userIds);
                                })
                )
                .exec(session -> {
                    SharedTestData.USER_IDS.set(
                            session.getList("userIds")
                    );
                    return session;
                });
    }

    public static ChainBuilder deleteUsers() {
        return foreach(
                session -> SharedTestData.USER_IDS.get(),
                "userId"
        )
                .on(
                        exec(
                                http("delete-user")
                                        .delete(session ->
                                                "/api/v1/users/"
                                                        + session.getString("userId"))
                                        .check(status().is(204))
                        )
                );
    }
}