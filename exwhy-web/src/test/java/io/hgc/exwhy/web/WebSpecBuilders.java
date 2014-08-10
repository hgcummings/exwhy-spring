package io.hgc.exwhy.web;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebConnection;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class WebSpecBuilders {
    public static class WebSpecArrangeBuilder {
        private static MockMvc mockMvc;

        public WebSpecActBuilder when() {
            String server = System.getProperty("test.server");
            if (StringUtils.isBlank(server)) {
                return new WebSpecActBuilder((webClient) -> {
                    if (mockMvc == null) {
                        mockMvc = createMockMvc();
                    }
                    return new MockMvcWebConnection(mockMvc);
                });
            } else {
                return new WebSpecActBuilder(HttpWebConnection::new);
            }
        }

        private static MockMvc createMockMvc() {
            AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
            context.register(Application.class);
            MockServletContext servletContext = new MockServletContext();
            servletContext.setContextPath("/exwhy");
            context.setServletContext(servletContext);
            return MockMvcBuilders.webAppContextSetup(context).build();
        }
    }

    public static class WebSpecActBuilder {
        private Function<WebClient, WebConnection> createWebConnection;
        private WebClient webClient;

        public WebSpecActBuilder(Function<WebClient, WebConnection> createWebConnection)
        {
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

            public WebRequestBuilder withParameter(String name, Object value) {
                uriBuilder.queryParam(name, value);
                return this;
            }

            public WebSpecAssertBuilder then() {
                return new WebSpecAssertBuilder(() -> {
                    webClient = new WebClient();
                    webClient.setWebConnection(createWebConnection.apply(webClient));
                    try {
                        return webClient.getPage(uriBuilder.build().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, this::cleanup);
            }

            private void cleanup() {
                webClient.closeAllWindows();
            }
        }
    }

    public static class WebSpecAssertBuilder {
        private Supplier<HtmlPage> htmlPageSupplier;
        private Runnable cleanup;

        public WebSpecAssertBuilder(Supplier<HtmlPage> htmlPageSupplier, Runnable cleanup) {
            this.htmlPageSupplier = htmlPageSupplier;
            this.cleanup = cleanup;
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