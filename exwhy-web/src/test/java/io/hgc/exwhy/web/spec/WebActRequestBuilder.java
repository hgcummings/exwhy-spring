package io.hgc.exwhy.web.spec;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public class WebActRequestBuilder {
    private Function<WebClient, WebConnection> createWebConnection;
    private WebClient webClient;

    WebActRequestBuilder(Function<WebClient, WebConnection> createWebConnection) {
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

        public WebActPageBuilder and() {
            return new WebActPageBuilder(getHtmlPageSupplier(), this::cleanup);
        }

        public WebRequestBuilder withParameter(String name, Object value) {
            uriBuilder.queryParam(name, value);
            return this;
        }

        public WebAssertBuilder then() {
            return new WebAssertBuilder(getHtmlPageSupplier(), this::cleanup);
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

        private void cleanup(Throwable error) {
            if (error != null) {
                Page page = webClient.getCurrentWindow().getEnclosedPage();
                System.out.println("Error on " + page.getUrl());
                if (page instanceof SgmlPage) {
                    System.out.println(((SgmlPage) page).asXml());
                }
            }

            webClient.closeAllWindows();

            if (error != null) {
                throw new RuntimeException(error);
            }
        }
    }
}