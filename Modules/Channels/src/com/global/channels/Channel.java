package com.global.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Róbert Dóczi
 *         Date: 2014.12.02.
 */
@SuppressWarnings("unchecked")
public class Channel {

    public <Handler extends Consumer<Message>, Message> void add(Class<? extends Message> msgType, Handler handler) {
        getInstance(msgType).add(handler);
    }

    public <Message> void broadcast(Message msg) {
        getInstance(msg.getClass()).broadcast(msg);
    }

    private InternalChannel getInstance(Class<?> clazz) {
        InternalChannel instance = instances.get(clazz);
        if (instance == null) {
            instance = new InternalChannel();
            instances.put(clazz, instance);
        }
        return instance;
    }

    private Map<Class, InternalChannel<?>> instances = new HashMap<>();

    private class InternalChannel<Message> {

        private List<Consumer<Message>> handlers;

        private InternalChannel() {
            handlers = new ArrayList<>();
        }

        public <Handler extends Consumer<Message>> void add(Handler handler) {
            handlers.add(handler);
        }

        public void broadcast(Message msg) {
            for (Consumer<Message> handler : handlers) {
                handler.accept(msg);
            }
        }
    }
}