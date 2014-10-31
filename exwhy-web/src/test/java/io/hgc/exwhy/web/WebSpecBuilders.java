package io.hgc.exwhy.web;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Lists;
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

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebSpecBuilders {
    public static class WebSpecArrangeBuilder {
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

    public static class WebSpecRequestActBuilder {
        private Function<WebClient, WebConnection> createWebConnection;
        private WebClient webClient;

        public WebSpecRequestActBuilder(Function<WebClient, WebConnection> createWebConnection) {
            this.createWebConnection = createWebConnection;
        }

        public WebRequestBuilder iVisit(String path) {
            return new WebRequestBuilder(path);
        }

        public class WebRequestBuilder {
            private UriBuilder uriBuilder;

            public WebRequestBuilder(String path) {
                String rootUrl;
                String server = System.getProperty("test.server");
                if (StringUtils.isBlank(server)) {
                    rootUrl = "http://mockmvc/exwhy";
                } else {
                    rootUrl = server;
                }

                this.uriBuilder = UriBuilder
                    .fromPath(rootUrl)
                    .path(path);
            }

            public WebSpecPageActBuilder and() {
                return new WebSpecPageActBuilder(getHtmlPageSupplier(), this::cleanup);
            }

            public WebRequestBuilder withParameter(String name, Object value) {
                uriBuilder.queryParam(name, value);
                return this;
            }

            public WebSpecAssertBuilder then() {
                return new WebSpecAssertBuilder(getHtmlPageSupplier(), this::cleanup);
            }

            private Supplier<HtmlPage> getHtmlPageSupplier() {
                return () -> {
                    webClient = new WebClient();
                    webClient.setWebConnection(createWebConnection.apply(webClient));
                    try {
                        return webClient.getPage(uriBuilder.build().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            private void cleanup() {
                webClient.closeAllWindows();
            }
        }
    }

    public static class WebSpecPageActBuilder {
        private Supplier<HtmlPage> htmlPageSupplier;
        private Runnable cleanup;

        public WebSpecPageActBuilder(Supplier<HtmlPage> htmlPageSupplier, Runnable cleanup) {
            this.htmlPageSupplier = htmlPageSupplier;
            this.cleanup = cleanup;
        }

        public WebSpecPageActBuilder iSelect(String text) {
            Supplier<HtmlPage> oldSupplier = htmlPageSupplier;
            this.htmlPageSupplier = () -> {
                try {
                    return (HtmlPage) oldSupplier.get().getAnchorByText(text).click();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
            return this;
        }

        public WebSpecAssertBuilder then() {
            return new WebSpecAssertBuilder(htmlPageSupplier, cleanup);
        }
    }

    public static class WebSpecAssertBuilder {
        private Supplier<HtmlPage> htmlPageSupplier;
        private Runnable cleanup;

        public WebSpecAssertBuilder(Supplier<HtmlPage> htmlPageSupplier, Runnable cleanup) {
            this.htmlPageSupplier = htmlPageSupplier;
            this.cleanup = cleanup;
        }

        public WebSpecAssertBuilder iSeeAnElement() {
            return this;
        }

        public Runnable withText(String text) {
            return () -> {
                DomNode element = htmlPageSupplier.get().getFirstByXPath(String.format("//*[text()=\"%s\"]", text));
                assertNotNull(element);
                cleanup.run();
            };
        }

        public HtmlElementAssertionBuilder iSeeAnElement(String id) {
            return new HtmlElementAssertionBuilder(() ->
                htmlPageSupplier.get().getElementById(id));
        }

        public class HtmlElementAssertionBuilder {
            private Supplier<DomElement> elementSupplier;
            private List<Consumer<DomElement>> assertions;

            public HtmlElementAssertionBuilder(Supplier<DomElement> elementSupplier) {
                this.elementSupplier = elementSupplier;
                this.assertions = new ArrayList<>();
            }

            public HtmlElementAssertionBuilder withText(String expected) {
               assertions.add((domElement) -> {
                    assertEquals(expected, domElement.getTextContent());
                });
                return this;
            }

            public void run() {
                DomElement element = elementSupplier.get();
                assertions.forEach((assertion) -> assertion.accept(element));
                cleanup.run();
            }
        }
    }
}
