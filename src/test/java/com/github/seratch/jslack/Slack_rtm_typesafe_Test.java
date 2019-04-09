package com.github.seratch.jslack;

import com.github.seratch.jslack.api.model.event.HelloEvent;
import com.github.seratch.jslack.api.model.event.UserTypingEvent;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.RTMEventHandler;
import com.github.seratch.jslack.api.rtm.RTMEventsDispatcher;
import com.github.seratch.jslack.api.rtm.RTMEventsDispatcherFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Slack_rtm_typesafe_Test {

    @Slf4j
    public static class HelloHandler extends RTMEventHandler<HelloEvent> {
        public final AtomicInteger counter = new AtomicInteger(0);
        @Override
        public void handle(HelloEvent event) {
            counter.incrementAndGet();
        }
    }

    public static class SubHelloHandler extends HelloHandler {
    }

    @Slf4j
    public static class UserTypingHandler extends RTMEventHandler<UserTypingEvent> {
        @Override
        public void handle(UserTypingEvent event) {
        }
    }

    @Test
    public void test() throws Exception {

        Slack slack = Slack.getInstance();

        String botToken = System.getenv(Constants.SLACK_BOT_USER_TEST_OAUTH_ACCESS_TOKEN);

        RTMEventsDispatcher dispatcher = RTMEventsDispatcherFactory.getInstance();
        HelloHandler hello = new HelloHandler();
        dispatcher.register(hello);

        try (RTMClient rtm = slack.rtmStart(botToken)) {
            rtm.addMessageHandler(dispatcher.toMessageHandler());

            rtm.connect();
            Thread.sleep(300L);
            assertThat(hello.counter.get(), is(1));

            rtm.reconnect();
            Thread.sleep(300L);
            assertThat(hello.counter.get(), is(2));

            dispatcher.deregister(hello);

            rtm.reconnect();
            Thread.sleep(300L);
            assertThat(hello.counter.get(), is(2)); // should not be incremented
        }
    }

    @Test
    public void test_with_subclass() throws Exception {

        Slack slack = Slack.getInstance();

        String botToken = System.getenv(Constants.SLACK_BOT_USER_TEST_OAUTH_ACCESS_TOKEN);

        RTMEventsDispatcher dispatcher = RTMEventsDispatcherFactory.getInstance();
        SubHelloHandler hello = new SubHelloHandler();
        dispatcher.register(hello);

        try (RTMClient rtm = slack.rtmStart(botToken)) {
            rtm.addMessageHandler(dispatcher.toMessageHandler());

            rtm.connect();
            Thread.sleep(300L);
            assertThat(hello.counter.get(), is(1));

            rtm.reconnect();
            Thread.sleep(300L);
            assertThat(hello.counter.get(), is(2));
        }
    }

    @Test
    public void userTyping() throws Exception {

        Slack slack = Slack.getInstance();

        String botToken = System.getenv(Constants.SLACK_BOT_USER_TEST_OAUTH_ACCESS_TOKEN);

        RTMEventsDispatcher dispatcher = RTMEventsDispatcherFactory.getInstance();
        dispatcher.register(new UserTypingHandler());

        try (RTMClient rtm = slack.rtmStart(botToken)) {
            rtm.addMessageHandler(dispatcher.toMessageHandler());
            rtm.connect();
        }
    }

}