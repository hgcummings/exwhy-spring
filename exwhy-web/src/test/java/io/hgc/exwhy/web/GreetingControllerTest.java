package io.hgc.exwhy.web;

import org.junit.Test;

import static io.hgc.exwhy.web.Setup.given;

public class GreetingControllerTest {
    @Test
    public void greeting() {
        given().theApplicationIsRunning()
            .when().iVisit("/greeting").withParameter("name", "Harry")
            .then().iSeeAnElement("greeting").withText("Hello, Harry!")
        .run();
    }
}