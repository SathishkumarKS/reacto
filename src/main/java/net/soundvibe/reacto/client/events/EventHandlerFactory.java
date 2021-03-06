package net.soundvibe.reacto.client.events;

import net.soundvibe.reacto.discovery.types.ServiceRecord;

/**
 * @author OZY on 2017.02.20.
 */
@FunctionalInterface
public interface EventHandlerFactory {
    EventHandler create(ServiceRecord serviceRecord);
}
