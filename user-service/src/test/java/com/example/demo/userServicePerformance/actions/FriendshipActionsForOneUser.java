package com.example.demo.userServicePerformance.actions;

import io.gatling.javaapi.core.ChainBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

@Slf4j
public class FriendshipActionsForOneUser {

    public static ChainBuilder createFriendships() {

        return exec(session -> {

            List<String> userIds = session.getList("userIds");

            if (userIds == null || userIds.isEmpty() || userIds.getFirst() == null) {
                log.warn("CREATE FRIENDSHIPS | userIds is null or empty");
                return session;
            }

            return session.set("mainUserId", userIds.getFirst());
        })

                .foreach(
                        session -> session.<String>getList("userIds"),
                        "friendId"
                )
                .on(
                        doIf(
                                session ->
                                        session.getString("mainUserId") != null
                                                && session.getString("friendId") != null
                                                && !Objects.equals(
                                                session.getString("mainUserId"),
                                                session.getString("friendId")
                                        )
                        )
                                .then(
                                        http("create-friendship")
                                                .post("/api/v1/friendships")
                                                .body(StringBody("""
                                                        {
                                                          "userId":"#{mainUserId}",
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
                                                    session.getString("mainUserId"),
                                                    session.getString("friendId"),
                                                    responseTime
                                            );
                                        }
                                    }

                                    return session;
                                })
                );
    }

    public static ChainBuilder deleteFriendships(List<String> ids) {
        if (ids == null || ids.isEmpty() || ids.getFirst() == null) {
            log.warn("DELETE FRIENDSHIPS | userIds is null or empty");
            return exec(session -> session);
        }

        String mainUserId = ids.getFirst();
        return foreach(
                session -> ids,
                "friendId"
        )
                .on(
                        doIf(
                                session ->
                                        !Objects.equals(
                                                mainUserId,
                                                session.getString("friendId")
                                        )
                        )
                                .then(
                                        http("delete-friendship")
                                                .delete(session ->
                                                        "/api/v1/friendships/"
                                                                + mainUserId
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
                                                    mainUserId,
                                                    session.getString("friendId"),
                                                    responseTime
                                            );
                                        }
                                    }
                                    return session;
                                })
                );
    }
}