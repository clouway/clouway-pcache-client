package com.clouway.api.pcache.extensions.redis.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for threads with customized names. The implemention is inspired
 * by Executors' DefaultThreadFactory ({@link Executors#defaultThreadFactory()}).
 *
 * @author @author Martin Grotzke
 */
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    /**
     * Constructor accepting the prefix of the threads that will be created by this {@link ThreadFactory}
     *
     * @param namePrefix
     *            Prefix for names of threads
     */
    public NamedThreadFactory(final String namePrefix) {
        this.namePrefix = namePrefix + "-thread-";
        final SecurityManager s = System.getSecurityManager();
        group = (s != null)? s.getThreadGroup() :
                             Thread.currentThread().getThreadGroup();
    }

    /**
     * Returns a new thread using a name as specified by this factory {@inheritDoc}
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement());
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}