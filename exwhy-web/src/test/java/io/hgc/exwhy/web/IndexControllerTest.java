package io.hgc.exwhy.web;

import org.junit.Test;
import org.springframework.social.connect.UserProfileBuilder;

import static io.hgc.exwhy.web.Setup.given;

public class IndexControllerTest {
    @Test
    public void homepage() {
        given().theApplicationIsRunning()
            .when().iVisit("/")
            .then().iSeeAnElement("heading").withText("exwhy")
            .run();
    }

    @Test
    public void signIn() {
        given().theApplicationIsRunning()
            .when().iVisit("/")
            .and().iSelect("Sign in")
            .then().iSeeAnElement().withText("Sign in with Twitter")
            .run();
    }

    @Test
    public void greeting() {
        given().theApplicationIsRunning()
            .and().iAmSignedInAs("Testy McTest")
            .when().iVisit("/")
            .then().iSeeAnElement("greeting").withText("Hello, Testy!")
            .run();
    }
}
