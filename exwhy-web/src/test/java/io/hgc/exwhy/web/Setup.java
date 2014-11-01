package io.hgc.exwhy.web;

import io.hgc.exwhy.web.spec.WebSpecArrangeBuilder;

public class Setup {
    public static SetupBuilder given() {
        return new SetupBuilder();
    }

    public static class SetupBuilder {
        public WebSpecArrangeBuilder theApplicationIsRunning() {
            return new WebSpecArrangeBuilder();
        }
    }


}
