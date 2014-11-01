package io.hgc.exwhy.web.spec;

public class Setup {
    public static SetupBuilder given() {
        return new SetupBuilder();
    }

    public static class SetupBuilder {
        public WebArrangeBuilder theApplicationIsRunning() {
            return new WebArrangeBuilder();
        }
    }


}
