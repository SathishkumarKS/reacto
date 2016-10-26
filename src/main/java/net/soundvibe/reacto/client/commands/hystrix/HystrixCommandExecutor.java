package net.soundvibe.reacto.client.commands.hystrix;

import com.netflix.hystrix.HystrixCommandProperties;
import net.soundvibe.reacto.client.commands.CommandExecutor;
import net.soundvibe.reacto.client.errors.CannotConnectToWebSocket;
import net.soundvibe.reacto.client.events.EventHandlers;
import net.soundvibe.reacto.types.*;
import rx.Observable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author OZY on 2015.11.13.
 */
public class HystrixCommandExecutor implements CommandExecutor {

    private final Supplier<Optional<EventHandlers>> eventHandlers;
    private final HystrixCommandProperties.Setter hystrixConfig;

    public HystrixCommandExecutor(Supplier<Optional<EventHandlers>> eventHandlers, HystrixCommandProperties.Setter hystrixConfig) {
        this.eventHandlers = eventHandlers;
        this.hystrixConfig = hystrixConfig;
    }


    @Override
    public Observable<Event> execute(Command command) {
        return eventHandlers.get()
                .map(handlers -> handlers.fallbackNodeClient.isPresent() ?
                        new HystrixObservableCommandWrapper(cmd -> handlers.mainNodeClient.toObservable(command),
                                cmd -> handlers.fallbackNodeClient.get().toObservable(command),
                                command, hystrixConfig).toObservable() :
                        new HystrixObservableCommandWrapper(cmd -> handlers.mainNodeClient.toObservable(command),
                                command, hystrixConfig).toObservable())
                .orElse(Observable.error(new CannotConnectToWebSocket("Cannot connect to ws of command: " + command)));
    }
}
