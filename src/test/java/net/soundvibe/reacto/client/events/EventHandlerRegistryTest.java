package net.soundvibe.reacto.client.events;

import net.soundvibe.reacto.discovery.types.*;
import net.soundvibe.reacto.server.ServiceOptions;
import net.soundvibe.reacto.types.*;
import org.junit.Test;
import rx.Observable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author OZY on 2017.01.19.
 */
public class EventHandlerRegistryTest {

    @Test
    public void shouldBeEmpty() throws Exception {
        final EventHandlerRegistry sut = EventHandlerRegistry.empty();
        ServiceOptions serviceOptions = new ServiceOptions("foo", "/", "1", false, 80);
        assertEquals(0L, sut.find(ServiceRecord.createWebSocketEndpoint(serviceOptions, Collections.emptyList()))
                .count());
    }

    @Test
    public void shouldNotCacheTheFunctionWhenDifferentRecords() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        EventHandlerFactory f = serviceRecord -> getEventHandler(serviceRecord,
                Observable.just(Event.create("one")), counter.incrementAndGet());

        final EventHandlerRegistry sut = EventHandlerRegistry.Builder.create()
                .register(ServiceType.WEBSOCKET, f)
                .build();

        ServiceRecord record = ServiceRecord.createWebSocketEndpoint(
                new ServiceOptions("service", "/", "1", false, 8080),
                Collections.emptyList());
        final List<EventHandler> handlers = sut.find(record)
                .collect(Collectors.toList());

        assertEquals(1, handlers.size());
        final EventHandler actual = handlers.get(0);
        assertEquals(1, actual.hashCode());

        ServiceRecord newRecord = ServiceRecord.createWebSocketEndpoint(
                new ServiceOptions("service", "/root", "1", false, 8080),
                Collections.emptyList());
        final List<EventHandler> newHandlers = sut.find(newRecord)
                .filter(eventHandler -> eventHandler.hashCode() > 1)
                .collect(Collectors.toList());
        assertEquals(1, newHandlers.size());
        final EventHandler actual2 = newHandlers.get(0);
        assertEquals(2, actual2.hashCode());
    }


    @Test
    public void shouldCacheTheFunction() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        EventHandlerFactory f = serviceRecord -> getEventHandler(serviceRecord,
                Observable.just(Event.create("one")), counter.incrementAndGet());

        final EventHandlerRegistry sut = EventHandlerRegistry.Builder.create()
                .register(ServiceType.WEBSOCKET, f)
                .build();

        ServiceRecord record = ServiceRecord.createWebSocketEndpoint(
                new ServiceOptions("service", "/", "1", false, 8080),
                Collections.emptyList());
        final List<EventHandler> handlers = sut.find(record)
                .collect(Collectors.toList());

        assertEquals(1, handlers.size());
        final EventHandler actual = handlers.get(0);
        assertEquals(1, actual.hashCode());

        final List<EventHandler> newHandlers = sut.find(record)
                .collect(Collectors.toList());
        assertEquals(1, newHandlers.size());
        final EventHandler actual2 = newHandlers.get(0);
        assertEquals(1, actual2.hashCode());
    }


    private EventHandler getEventHandler(ServiceRecord serviceRecord, Observable<Event> observable, int hashCode) {
        return new EventHandler() {
            @Override
            public Observable<Event> observe(Command command) {
                return observable;
            }

            @Override
            public ServiceRecord serviceRecord() {
                return serviceRecord;
            }

            @Override
            public int hashCode() {
                return hashCode;
            }

            @Override
            public boolean equals(Object obj) {
                return super.equals(obj);
            }
        };
    }
}