package reactive.fp.mappers;

import org.junit.Test;
import reactive.fp.client.commands.CommandNodes;
import reactive.fp.client.commands.DistributedCommandDef;
import reactive.fp.types.Event;
import reactive.fp.client.events.EventHandlers;
import rx.Observable;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Cipolinas on 2015.11.23.
 */
public class MappersTest {

    @Test
    public void shouldBeBothMainAndFallbackSet() throws Exception {
        final Optional<EventHandlers<String>> actual = Mappers.mapToEventHandlers(
                new DistributedCommandDef("foo", new CommandNodes("localhost", Optional.of("www.google.com"))),
                uri -> (commandName, arg) -> Observable.just(Event.onNext("foo " + arg)));

        assertTrue("Mapping should be successful",actual.isPresent());
        final EventHandlers<String> eventHandlers = actual.get();
        assertNotNull("Main Node should be set", eventHandlers.mainNodeClient);
        assertNotNull("Fallback Node should be set", eventHandlers.fallbackNodeClient.get());
    }

    @Test
    public void shouldBeOnlyMainSet() throws Exception {
        final Optional<EventHandlers<String>> actual = Mappers.mapToEventHandlers(
                new DistributedCommandDef("foo", new CommandNodes("localhost", Optional.empty())),
                uri -> (commandName, arg) -> Observable.just(Event.onNext("foo " + arg)));

        assertTrue("Mapping should be successful",actual.isPresent());
        final EventHandlers<String> eventHandlers = actual.get();
        assertNotNull("Main Node should be set", eventHandlers.mainNodeClient);
        assertFalse("Fallback Node should not be set", eventHandlers.fallbackNodeClient.isPresent());
    }
}
