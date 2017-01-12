package net.soundvibe.reacto.server.handlers;

import io.vertx.core.logging.*;
import net.soundvibe.reacto.client.errors.CommandNotFound;
import net.soundvibe.reacto.internal.InternalEvent;
import net.soundvibe.reacto.mappers.Mappers;
import net.soundvibe.reacto.metric.CommandHandlerMetric;
import net.soundvibe.reacto.server.CommandRegistry;
import net.soundvibe.reacto.types.*;
import rx.*;
import rx.schedulers.Schedulers;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.*;

/**
 * @author OZY on 2016.02.09.
 */
public final class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private final CommandRegistry commands;

    private static final Scheduler SINGLE_THREAD = Schedulers.from(Executors.newSingleThreadExecutor());

    public CommandHandler(CommandRegistry commands) {
        this.commands = commands;
    }

    public void handle(final byte[] bytes,
                       Consumer<byte[]> sender,
                       Consumer<Subscription> unSubscriber,
                       Runnable closeHandler
                       ) {
        try {
            final Command receivedCommand = Mappers.fromBytesToCommand(bytes);
            final CommandDescriptor descriptor = CommandDescriptor.fromCommand(receivedCommand);
            final Optional<Function<Command, Observable<Event>>> commandFunc = commands.findCommand(descriptor);
            final CommandHandlerMetric metric = CommandHandlerMetric.of(receivedCommand);
            commandFunc
                    .map(cmdFunc -> cmdFunc.apply(receivedCommand)
                            .doOnEach(notification -> publishMetrics(notification, receivedCommand, metric))
                            .subscribeOn(SINGLE_THREAD)
                            .observeOn(SINGLE_THREAD)
                            .subscribe(
                                    event -> sender.accept(toBytes(InternalEvent.onNext(event))),
                                    throwable -> {
                                        sender.accept(toBytes(InternalEvent.onError(throwable)));
                                        closeHandler.run();
                                    },
                                    () -> {
                                        sender.accept(toBytes(InternalEvent.onCompleted()));
                                        closeHandler.run();
                                    }))
                    .ifPresent(unSubscriber);

            if (!commandFunc.isPresent()) {
                sender.accept(toBytes(InternalEvent.onError(new CommandNotFound(receivedCommand.name))));
                closeHandler.run();
            }
        } catch (Throwable e) {
            sender.accept(toBytes(InternalEvent.onError(e)));
            closeHandler.run();
        }
    }

    private void publishMetrics(Notification<? super Event> notification, Command receivedCommand, CommandHandlerMetric metric) {
        log.debug("Command "+ receivedCommand + " executed and received notification: " + notification);
        switch (notification.getKind()) {
            case OnNext:
                metric.onNext();
                break;
            case OnError:
                metric.onError(notification.getThrowable());
                break;
            case OnCompleted:
                metric.onCompleted();
                break;
        }
    }

    private byte[] toBytes(InternalEvent internalEvent) {
        return Mappers.internalEventToBytes(internalEvent);
    }


}
