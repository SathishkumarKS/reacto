package net.soundvibe.reacto.client.commands.hystrix;

import net.soundvibe.reacto.errors.*;
import net.soundvibe.reacto.types.*;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.Collections;

/**
 * @author Cipolinas on 2016.02.18.
 */
public class HystrixCommandExecutorTest {

    private final TestSubscriber<Event> testSubscriber = new TestSubscriber<>();

    @Test
    public void shouldGetErrorWhenEventHandlersAreEmpty() throws Exception {
        HystrixCommandExecutor sut = new HystrixCommandExecutor(Collections.emptyList(), HystrixCommandExecutor.defaultHystrixSetter);
        sut.execute(Command.create("foo"))
            .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertError(CannotFindEventHandlers.class);
    }
}
