package com.example.demo.userServicePerformance.actions;

import io.gatling.javaapi.core.ChainBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

@Slf4j
public class FriendshipActionsForAllUsers {

    public static ChainBuilder createFriendships() {
        return foreach(
                session -> session.<String>getList("userIds"),
                "userId"
        )
                .on(
                        foreach(
                                session -> session.<String>getList("userIds"),
                                "friendId"
                        )
                                .on(
                                        doIf(
                                                session ->
                                                        session.getString("userId") != null
                                                                && session.getString("friendId") != null
                                                                && !Objects.equals(
                                                                session.getString("userId"),
                                                                session.getString("friendId")
                                                        )
                                        )
                                                .then(
                                                        http("create-friendship")
                                                                .post("/api/v1/friendships")
                                                                .body(StringBody("""
                                                                        {
                                                                          "userId":"#{userId}",
                                                                          "friendId":"#{friendId}"
                                                                        }
                                                                        """))
                                                                .check(status().is(201))
                                                                .check(responseTimeInMillis().saveAs("responseTime"))
                                                )
                                                .exec(session -> {

                                                    if (session.contains("responseTime")) {
                                                        int responseTime = session.getInt("responseTime");

                                                        if (responseTime > 50) {
                                                            log.warn(
                                                                    "SLOW REQUEST | create-friendship | userId={} | friendId={} | responseTime={} ms",
                                                                    session.getString("userId"),
                                                                    session.getString("friendId"),
                                                                    responseTime
                                                            );
                                                        }
                                                    }

                                                    return session;
                                                })
                                )
                );
    }

    public static ChainBuilder deleteFriendships(List<String> ids) {
        return foreach(
                session -> ids,
                "userId"
        )
                .on(
                        foreach(
                                session -> ids,
                                "friendId"
                        )
                                .on(
                                        doIf(
                                                session ->
                                                        !Objects.equals(
                                                                session.getString("userId"),
                                                                session.getString("friendId")
                                                        )
                                        )
                                                .then(
                                                        http("delete-friendship")
                                                                .delete(session ->
                                                                        "/api/v1/friendships/"
                                                                                + session.getString("userId")
                                                                                + "/friendship/"
                                                                                + session.getString("friendId")
                                                                )
                                                                .check(status().is(204))
                                                                .check(responseTimeInMillis().saveAs("responseTime"))
                                                )
                                                .exec(session -> {
                                                    if (session.contains("responseTime")) {
                                                        int responseTime = session.getInt("responseTime");

                                                        if (responseTime > 50) {
                                                            log.warn(
                                                                    "SLOW REQUEST | delete-friendship | userId={} | friendId={} | responseTime={} ms",
                                                                    session.getString("userId"),
                                                                    session.getString("friendId"),
                                                                    responseTime
                                                            );
                                                        }
                                                    }
                                                    return session;
                                                })
                                )
                );
    }
}