package com.bluesmoke.farm.correlator.builder;

import com.bluesmoke.farm.correlator.CorrelatorPool;
import com.bluesmoke.farm.correlator.builder.CorrelatorBuilderManager;
import com.bluesmoke.farm.service.feed.FeedService;
import com.bluesmoke.farm.util.RuntimeJavaFileCompiler;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class CorrelatorBuilderFromCodeFactory {
    public static void createCorrelatorBuilder(String classPath, String className, String correlatorBuilderCode, CorrelatorBuilderManager correlatorBuilderManager, FeedService feed, CorrelatorPool correlatorPool)
    {
        CorrelatorBuilder correlatorBuilder = null;
        try
        {
            RuntimeJavaFileCompiler.compile(classPath, className, correlatorBuilderCode);

            ClassLoader classLoader = feed.getClass().getClassLoader();

            Class correlatorClass = classLoader.loadClass(className);
            Constructor constructor = correlatorClass.getConstructor(String.class, CorrelatorBuilderManager.class, CorrelatorPool.class, FeedService.class);
            correlatorBuilder = (CorrelatorBuilder)constructor.newInstance(correlatorPool, feed, correlatorBuilderManager);

            correlatorBuilderManager.addBuilder(correlatorBuilder, 0.1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
