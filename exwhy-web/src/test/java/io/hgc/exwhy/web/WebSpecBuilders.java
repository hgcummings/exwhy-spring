package io.hgc.exwhy.web;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class WebSpecBuilders {
    public static class WebSpecArrangeBuilder {
        private static MockMvc mockMvc;

        public WebSpecActBuilder when() {
            return new WebSpecActBuilder(() -> {
                if (mockMvc == null) {
                    mockMvc = createMockMvc();
                }
                return mockMvc;
            });
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
        private Supplier<MockMvc> mockMvcSupplier;
        private WebClient webClient;

        public WebSpecActBuilder(Supplier<MockMvc> mockMvcSupplier)
        {
            this.mockMvcSupplier = mockMvcSupplier;
        }

        public WebRequestBuilder iVisit(String path) {
            return new WebRequestBuilder(path);
        }

        public class WebRequestBuilder {
            private UriBuilder uriBuilder;

            public WebRequestBuilder(String path) {
                this.uriBuilder = UriBuilder
                    .fromPath("http://localhost/exwhy")
                    .path(path);
            }

            public WebRequestBuilder withParameter(String name, Object value) {
                uriBuilder.queryParam(name, value);
                return this;
            }

            public WebSpecAssertBuilder then() {
                return new WebSpecAssertBuilder(() -> {
                    webClient = new WebClient();
                    webClient.setWebConnection(new MockMvcWebConnection(mockMvcSupplier.get()));
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
