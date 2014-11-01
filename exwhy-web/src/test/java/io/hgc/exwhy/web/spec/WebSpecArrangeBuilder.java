package io.hgc.exwhy.web.spec;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.collect.Lists;
import io.hgc.exwhy.web.Application;
import io.hgc.exwhy.web.TestAccounts;
import io.hgc.exwhy.web.authentication.SecurityContext;
import io.hgc.exwhy.web.authentication.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.social.connect.UserProfileBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebConnection;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class WebSpecArrangeBuilder {
    private static MockMvc mockMvc;
    private static MockPropertySource mockPropertySource;
    private List<Consumer<WebClient>> arrangeSteps = Lists.newArrayList();

    public WebSpecRequestActBuilder when() {
        String server = System.getProperty("test.server");
        if (StringUtils.isBlank(server)) {
            return new WebSpecRequestActBuilder((webClient) -> {
                if (mockMvc == null) {
                    mockMvc = createMockMvc();
                }
                return new MockMvcWebConnection(mockMvc);
            });
        } else {
            return new WebSpecRequestActBuilder((webClient) -> {
                HttpWebConnection connection = new HttpWebConnection(webClient);

                for (Consumer<WebClient> arrangeStep : arrangeSteps) {
                    arrangeStep.accept(webClient);
                }

                return connection;
            });
        }
    }

    private static MockMvc createMockMvc() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(Application.class);
        mockPropertySource = new MockPropertySource();
        mockPropertySource.setProperty("TWITTER_CONSUMER_KEY", UUID.randomUUID().toString());
        mockPropertySource.setProperty("TWITTER_CONSUMER_SECRET", UUID.randomUUID().toString());
        context.getEnvironment().getPropertySources().addLast(mockPropertySource);
        MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath("/exwhy");
        context.setServletContext(servletContext);
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    public WebSpecArrangeBuilder iAmSignedInAs(String name) {
        String server = System.getProperty("test.server");
        if (StringUtils.isBlank(server)) {
            SecurityContext.setCurrentUser(
                new User("userId", new UserProfileBuilder().setName(name).build()));
        } else {
            arrangeSteps.add(TestAccounts.get(name));
        }
        return this;
    }

    public WebSpecArrangeBuilder and() {
        return this;
    }
}