package io.hgc.exwhy.web.spec;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebSpecAssertBuilder {
    private Supplier<HtmlPage> htmlPageSupplier;
    private Consumer<Throwable> cleanup;

    public WebSpecAssertBuilder(Supplier<HtmlPage> htmlPageSupplier, Consumer<Throwable> cleanup) {
        this.htmlPageSupplier = htmlPageSupplier;
        this.cleanup = cleanup;
    }

    public WebSpecAssertBuilder iSeeAnElement() {
        return this;
    }

    public Runnable withText(String text) {
        return () -> {
            try {
                DomNode element = htmlPageSupplier.get().getFirstByXPath(String.format("//*[text()=\"%s\"]", text));
                assertNotNull(element);
                cleanup.accept(null);
            } catch (Throwable e) {
                cleanup.accept(e);
            }
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
            assertions.add((domElement) -> assertEquals(expected, domElement.getTextContent()));
            return this;
        }

        public void run() {
            try {
                DomElement element = elementSupplier.get();
                assertions.forEach((assertion) -> assertion.accept(element));
                cleanup.accept(null);
            } catch (Throwable e) {
                cleanup.accept(e);
            }
        }
    }
}