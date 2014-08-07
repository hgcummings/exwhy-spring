package io.hgc.exwhy.web;

public class Setup {
    public static SetupBuilder given() {
        return new SetupBuilder();
    }

    public static class SetupBuilder {
        public WebSpecBuilders.WebSpecArrangeBuilder theApplicationIsRunning() {
            return new WebSpecBuilders.WebSpecArrangeBuilder();
        }
    }


}
