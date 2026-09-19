package com.example.demo.userServicePerformance;

import com.example.demo.userServicePerformance.actions.*;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class UserServiceSimulation extends Simulation {

    private final HttpProtocolBuilder httpMainProtocol =
            http.baseUrl("http://localhost:8080")
                    .contentTypeHeader("application/json");

    private final HttpProtocolBuilder httpReplicaProtocol =
            http.baseUrl("http://localhost:8081")
                    .contentTypeHeader("application/json");


    private final ScenarioBuilder createScenario =
            scenario("Create users and friendships")
                    .exec(UserActions.createUsers(20000))
                    .pause(Duration.ofSeconds(5))
                    .exec(FriendshipActionsForOneUser.createFriendships())
                    .pause(Duration.ofSeconds(5));

    private final ScenarioBuilder fetchMainScenario =
            scenario("Fetch friends - main")
                    .exec(FetchFriendsAction.fetchFriendsPaging(
                            "Fetch friends - main",
                            6))
                    .exec(FetchFriendshipsAction.fetchFriendshipsPaging(
                            "Fetch friendships - main",
                            20));

    private final ScenarioBuilder fetchReplicaScenario =
            scenario("Fetch friends - replica")
                    .exec(FetchFriendsAction.fetchFriendsPaging(
                            "Fetch friends - replica",
                            6))
                    .exec(FetchFriendshipsAction.fetchFriendshipsPaging(
                            "Fetch friendships - replica",
                            20));


    {
        setUp(
                createScenario
                        .injectOpen(
                                atOnceUsers(1)
                        )
                        .protocols(httpMainProtocol)
                        .andThen(
                                fetchMainScenario
                                        .injectOpen(
                                                atOnceUsers(1)
                                        )
                                        .protocols(httpMainProtocol),

                                fetchReplicaScenario
                                        .injectOpen(
                                                atOnceUsers(1)
                                        )
                                        .protocols(httpReplicaProtocol)
                        )
        );
    }


}