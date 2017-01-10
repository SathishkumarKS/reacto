package net.soundvibe.reacto.server;

import io.vertx.core.json.*;
import io.vertx.servicediscovery.*;
import net.soundvibe.reacto.types.*;

import java.time.*;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author OZY on 2016.08.26.
 */
public interface ServiceRecords {

    String LAST_UPDATED = "updated";
    String COMMANDS = "commands";

    static boolean areEquals(Record existingRecord, Record newRecord) {
        return  Objects.equals(newRecord.getName(), existingRecord.getName()) &&
                Objects.equals(newRecord.getLocation(), existingRecord.getLocation()) &&
                Objects.equals(newRecord.getType(), existingRecord.getType());
    }

    static boolean isDown(Record existingRecord) {
        return existingRecord.getStatus() == Status.DOWN || !isUpdatedRecently(existingRecord);
    }

    static boolean isUpdatedRecently(Record record) {
        final Instant now = Instant.now();
        return Duration.between(record.getMetadata().getInstant(LAST_UPDATED, now),
                now)
                .toMinutes() < 4L;
    }

    JsonArray emptyJsonArray = new JsonArray();

    static boolean hasCommand(String commandName, Record record) {
        return record.getMetadata().getJsonArray(COMMANDS, emptyJsonArray)
                .stream()
                .flatMap(o -> o instanceof JsonObject ? Stream.of((JsonObject)o) : Stream.empty())
                .filter(descriptor -> descriptor.getString(CommandDescriptor.COMMAND, "").equals(commandName))
                .count() > 0L;
    }

    static boolean hasCommand(Command command, Record record) {
        return record.getMetadata().getJsonArray(COMMANDS, emptyJsonArray)
                .stream()
                .flatMap(o -> o instanceof JsonObject ? Stream.of((JsonObject)o) : Stream.empty())
                .filter(descriptor -> descriptor.getString(CommandDescriptor.COMMAND, "").equals(command.name) &&
                    descriptor.getString(CommandDescriptor.EVENT, "").equals(command.eventType()))
                .count() > 0L;
    }

    static boolean isService(String serviceName, Record record) {
        return serviceName.equals(record.getName());
    }

}
