package io.hgc.exwhy.web.spec;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Maps;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class TestAccounts {
    private static final String server = System.getProperty("test.server");

    private static final Map<String, Account> accounts = Maps.newHashMap();

    static {{
        accounts.put("Testy McTest",
            new TwitterAccount(System.getenv("USER_1_USERNAME"), System.getenv("USER_1_PASSWORD")));
    }}

    public static Consumer<WebClient> get(String name) {
        return ((webClient) -> {
            Account account = accounts.get(name);
            if (account == null || !account.isSpecified()) {
                throw new IllegalStateException(String.format("Account %s has not been specified", name));
            }
            account.signIn(webClient);
        });
    }

    private static interface Account {
        boolean isSpecified();
        void signIn(WebClient webClient);
    }

    private static class TwitterAccount implements Account {
        private final String username;
        private final String password;

        public TwitterAccount(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public boolean isSpecified() {
            return  (username != null && password != null);
        }

        @Override
        public void signIn(WebClient webClient) {
            try {
                HtmlPage signInPage = webClient.getPage(UriBuilder.fromPath(server).path("/signin").build().toString());
                HtmlPage twitterPage = signInPage.getHtmlElementById("signin-twitter").click();
                HtmlForm twitterSignInForm = twitterPage.getForms().get(0);
                twitterSignInForm.getInputByName("session[username_or_email]").setValueAttribute(username);
                twitterSignInForm.getInputByName("session[password]").setValueAttribute(password);
                while (twitterPage.getElementById("allow") != null) {
                    // Unlike with Chrome, Twitter seems to direct HtmlUnit to a second authorization page rather
                    // than immediately back to the application. Clicking 'allow' again has the desired effect.
                    twitterPage = twitterPage.getHtmlElementById("allow").click();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
