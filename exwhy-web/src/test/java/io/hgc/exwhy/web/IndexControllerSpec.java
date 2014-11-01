package io.hgc.exwhy.web;

import io.hgc.jarspec.JarSpecJUnitRunner;
import io.hgc.jarspec.Specification;
import io.hgc.jarspec.SpecificationNode;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.hgc.exwhy.web.spec.Setup.given;

@RunWith(JarSpecJUnitRunner.class)
public class IndexControllerSpec implements Specification {
    @Override
    public SpecificationNode root() {
        return describe("Homepage", () -> by(
           it("Displays the site name",
               given().theApplicationIsRunning()
               .when().iVisit("/")
               .then().theElementWithId("heading").hasText("exwhy")),
           it("Allows the user to sign in",
               given().theApplicationIsRunning()
               .when().iVisit("/")
                .and().iSelect("Sign in")
               .then().iSeeAnElementWithText("Sign in with Twitter")),
           it("Displays a greeting to signed-in users",
               given().theApplicationIsRunning()
                .and().iAmSignedInAs("Testy McTest")
               .when().iVisit("/")
               .then().theElementWithId("greeting").hasText("Hello, Testy!"))
        ));
    }
}