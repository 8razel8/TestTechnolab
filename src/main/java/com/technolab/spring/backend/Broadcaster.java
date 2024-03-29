package com.technolab.spring.backend;

import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Broadcaster {
    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<Consumer<CrudMessage>> listeners = new LinkedList<>();

    public static synchronized Registration register(
            Consumer<CrudMessage> listener) {
        listeners.add(listener);

        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(CrudMessage message) {
        for (Consumer<CrudMessage> listener : listeners) {
            executor.execute(() -> listener.accept(message));
        }
    }
}