package io.hgc.exwhy.web;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CustomisationBean implements EmbeddedServletContainerCustomizer {
    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (System.getenv("PORT") != null) {
            container.setPort(Integer.parseInt(System.getenv("PORT")));
        }
    }
}
