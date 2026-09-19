package com.example.demo.userServicePerformance.actions;

import com.example.demo.userServicePerformance.SharedTestData;
import io.gatling.javaapi.core.ChainBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

@Slf4j
public class FetchFriendsAction {

    public static ChainBuilder fetchFriendsPaging(String name, int pageSize) {

        return exec(session -> {
            List<String> userIds = SharedTestData.USER_IDS.get();
            if (userIds == null || userIds.isEmpty() || userIds.get(0) == null) {
                log.warn("FETCH FRIENDS | userIds is null or empty");
                return session
                        .set("hasNextPage", false);
            }

            return session
                    .set("userIds", userIds)
                    .set("fetchUserId", userIds.get(0))
                    .set("token", "")
                    .set("hasNextPage", true);

        })
                .asLongAs(
                        session ->
                                session.getBoolean("hasNextPage")
                )
                .on(
                        exec(
                                http(name)
                                        .get("/api/v1/friendships/friends")
                                        .queryParam(
                                                "userId",
                                                session ->
                                                        session.getString("fetchUserId")
                                        )
                                        .queryParam(
                                                "token",
                                                session ->
                                                        session.getString("token")
                                        )
                                        .queryParam("size", pageSize)
                                        .check(status().is(200))
                                        .check(
                                                jsonPath("$.nextPage.token")
                                                        .optional()
                                                        .saveAs("nextToken")
                                        )
                                        .check(
                                                responseTimeInMillis()
                                                        .saveAs("responseTime")
                                        )
                        )
                                .exec(session -> {
                                    if (session.contains("responseTime")) {
                                        int responseTime =
                                                session.getInt("responseTime");

                                        if (responseTime > 110) {
                                            log.warn(
                                                    "SLOW REQUEST | {} | userId={} | token={} | responseTime={} ms",
                                                    name,
                                                    session.getString("fetchUserId"),
                                                    session.getString("token"),
                                                    responseTime
                                            );
                                        }
                                    }
                                    return session;
                                })
                                .exec(session -> {

                                    String nextToken =
                                            session.getString("nextToken");

                                    if (nextToken == null || nextToken.isEmpty()) {

                                        return session
                                                .set("hasNextPage", false);
                                    }

                                    return session
                                            .set("token", nextToken)
                                            .set("hasNextPage", true);
                                })
                );
    }
}