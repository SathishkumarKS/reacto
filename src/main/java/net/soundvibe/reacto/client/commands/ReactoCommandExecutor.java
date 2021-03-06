package net.soundvibe.reacto.client.commands;

import net.soundvibe.reacto.client.events.EventHandler;
import net.soundvibe.reacto.discovery.*;
import net.soundvibe.reacto.errors.CannotFindEventHandlers;
import net.soundvibe.reacto.types.*;
import rx.Observable;

import java.util.*;

/**
 * @author OZY on 2016.09.06.
 */
public final class ReactoCommandExecutor implements CommandExecutor {

    private final List<EventHandler> eventHandlers;
    private final LoadBalancer<EventHandler> loadBalancer;

    public static final CommandExecutorFactory FACTORY = ReactoCommandExecutor::new;

    public ReactoCommandExecutor(List<EventHandler> eventHandlers,
                                 LoadBalancer<EventHandler> loadBalancer) {
        Objects.requireNonNull(eventHandlers, "eventHandlers cannot be null");
        Objects.requireNonNull(loadBalancer, "loadBalancer cannot be null");
        this.eventHandlers = eventHandlers;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public Observable<Event> execute(Command command) {
        if (eventHandlers.isEmpty()) return Observable.error(new CannotFindEventHandlers("No event handlers found for command: " + command));
        return Observable.just(eventHandlers)
                .map(loadBalancer::balance)
                .concatMap(eventHandler -> eventHandler.observe(command)
                        .onBackpressureBuffer()
                        .onErrorResumeNext(error -> handleError(error, command, eventHandler)))
                ;
    }

    private Observable<Event> handleError(Throwable error, Command command, EventHandler eventHandler) {
        return Observable.just(eventHandler)
                .doOnNext(this::removeHandler)
                .flatMap(any -> eventHandlers.isEmpty() ?  Observable.error(error) : Observable.just(command))
                .flatMap(this::execute);
    }

    private synchronized void removeHandler(EventHandler eventHandler) {
        eventHandlers.remove(eventHandler);
    }
}
