package io.hgc.exwhy.web.spec;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebSpecPageActBuilder {
    private Supplier<HtmlPage> htmlPageSupplier;
    private Consumer<Throwable> cleanup;

    public WebSpecPageActBuilder(Supplier<HtmlPage> htmlPageSupplier, Consumer<Throwable> cleanup) {
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