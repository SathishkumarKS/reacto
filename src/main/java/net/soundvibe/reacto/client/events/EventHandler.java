package net.soundvibe.reacto.client.events;

import net.soundvibe.reacto.types.*;
import rx.Observable;

/**
 * @author Cipolinas on 2015.11.16.
 */
public interface EventHandler extends Named {

    Observable<Event> toObservable(Command command);

}
