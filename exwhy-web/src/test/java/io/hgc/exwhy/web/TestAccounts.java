package io.hgc.exwhy.web;

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

    private static final Map<String, Consumer<WebClient>> accounts = Maps.newHashMap();

    static {{
        accounts.put("Testy McTest",
            new TwitterAccount(getRequired("USER_1_USERNAME"), getRequired("USER_1_PASSWORD")));
    }}

    public static Consumer<WebClient> get(String name) {
        return accounts.get(name);
    }

    private static String getRequired(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalStateException(String.format("Environment variable %s must be set", name));
        }
        return value;
    }

    private static class TwitterAccount implements Consumer<WebClient> {
        private final String username;
        private final String password;

        public TwitterAccount(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void accept(WebClient webClient) {
            try {
                HtmlPage page = webClient.getPage(UriBuilder.fromPath(server).path("/signin").build().toString());
                HtmlPage twitterPage = page.getHtmlElementById("signin-twitter").click();
                HtmlForm twitterSignInForm = twitterPage.getForms().get(0);
                twitterSignInForm.getInputByName("session[username_or_email]").setValueAttribute(username);
                twitterSignInForm.getInputByName("session[password]").setValueAttribute(password);
                twitterPage.getHtmlElementById("allow").click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
