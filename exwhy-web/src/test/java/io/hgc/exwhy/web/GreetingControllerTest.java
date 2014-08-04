package io.hgc.exwhy.web;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebConnection;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GreetingControllerTest {
    private WebClient webClient;

    @Before
    public void setup() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(Application.class);
        MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath("/exwhy");
        context.setServletContext(servletContext);
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        webClient = new WebClient();
        webClient.setWebConnection(new MockMvcWebConnection(mockMvc));
    }

    @After
    public void cleanup() {
        this.webClient.closeAllWindows();
    }

    @Test
    public void greeting() throws IOException {
        HtmlPage response = webClient.getPage("http://localhost/exwhy/greeting?name=Harry");

        assertEquals("Hello, Harry!", response.getElementById("greeting").getTextContent());
    }
}