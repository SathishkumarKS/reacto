package net.soundvibe.reacto.mappers;

import com.google.protobuf.InvalidProtocolBufferException;
import net.soundvibe.reacto.errors.RuntimeProtocolBufferException;
import net.soundvibe.reacto.internal.*;
import net.soundvibe.reacto.internal.proto.Messages;
import net.soundvibe.reacto.types.*;

import java.io.*;
import java.util.Optional;

/**
 * @author Linas on 2015.10.25.
 */
public interface Mappers {

    static byte[] internalEventToBytes(InternalEvent internalEvent) {
        return MessageMappers.toProtoBufEvent(internalEvent).toByteArray();
    }

    static byte[] commandToBytes(Command command) {
        return MessageMappers.toProtoBufCommand(command).toByteArray();
    }

    static InternalEvent fromBytesToInternalEvent(byte[] bytes) {
        try {
            return MessageMappers.toInternalEvent(Messages.Event.parseFrom(bytes));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeProtocolBufferException("Cannot deserialize event from bytes: " + new String(bytes), e);
        }
    }

    static Event fromInternalEvent(InternalEvent internalEvent) {
        return Event.create(internalEvent.name, internalEvent.metaData, internalEvent.payload);
    }

    static Command fromBytesToCommand(byte[] bytes) {
        try {
            return MessageMappers.toCommand(Messages.Command.parseFrom(bytes));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeProtocolBufferException("Cannot deserialize command from bytes: " + new String(bytes), e);
        }
    }

    static Optional<byte[]> exceptionToBytes(Throwable throwable) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
            oos.writeObject(throwable);
            return Optional.of(byteArrayOutputStream.toByteArray());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    static Optional<Throwable> fromBytesToException(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return Optional.ofNullable(objectInputStream.readObject())
                    .map(o -> (Throwable) o);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }
}
