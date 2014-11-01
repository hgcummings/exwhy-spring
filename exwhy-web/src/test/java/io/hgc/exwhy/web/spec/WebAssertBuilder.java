package io.hgc.exwhy.web.spec;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Lists;
import io.hgc.jarspec.Test;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebAssertBuilder implements Test {
    private Supplier<HtmlPage> htmlPageSupplier;
    private Consumer<Throwable> cleanup;
    private ArrayList<Consumer<HtmlPage>> assertions;

    WebAssertBuilder(Supplier<HtmlPage> htmlPageSupplier, Consumer<Throwable> cleanup) {
        this.htmlPageSupplier = htmlPageSupplier;
        this.cleanup = cleanup;
        this.assertions = Lists.newArrayList();
    }

    public WebAssertBuilder iSeeAnElementWithText(String text) {
        assertions.add((htmlPage) -> {
            DomNode element = htmlPage.getFirstByXPath(String.format("//*[text()=\"%s\"]", text));
            assertNotNull(element);
        });
        return this;
    }

    public HtmlElementAssertionBuilder theElementWithId(String id) {
        return new HtmlElementAssertionBuilder(id);
    }

    public void run() {
        try {
            HtmlPage htmlPage = htmlPageSupplier.get();
            assertions.forEach((assertion) -> assertion.accept(htmlPage));
            cleanup.accept(null);
        } catch (Throwable e) {
            cleanup.accept(e);
        }
    }

    public class HtmlElementAssertionBuilder implements Test {
        private String elementId;

        public HtmlElementAssertionBuilder(String elementId) {
            this.elementId = elementId;
        }

        public HtmlElementAssertionBuilder hasText(String expected) {
            assertions.add((htmlPage) -> {
                assertEquals(expected, htmlPage.getElementById(elementId).getTextContent());
            });
            return this;
        }

        public WebAssertBuilder and() {
            return WebAssertBuilder.this;
        }

        @Override
        public void run() {
            WebAssertBuilder.this.run();
        }
    }
}