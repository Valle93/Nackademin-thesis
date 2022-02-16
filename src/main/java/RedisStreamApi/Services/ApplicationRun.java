package RedisStreamApi.Services;


import RedisStreamApi.Enteties.InitServer;
import RedisStreamApi.Enteties.Injectable;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ApplicationRun {

    public static Set<String> classNames = new HashSet<>();
    public static Map<Class<?>, Object> objectsMap = new HashMap<>();

    public static void run(Class<?> startUpClass){

        String rootDir = startUpClass.getProtectionDomain().getCodeSource().getLocation().getFile();

        File dir = new File(rootDir);

        if (dir.isDirectory()) {

            recurseFiles(dir, "");
        }

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        List<Class<?>> loadedClasses = classNames.stream()
                .map(className -> {
                    try {
                        return classLoader.loadClass(className);
                    } catch (ClassNotFoundException ignore) {
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        loadedClasses.removeIf(aClass -> !aClass.isAnnotationPresent(Injectable.class));

        AtomicBoolean haveWeCreatedSomething = new AtomicBoolean(true);

        do {

            haveWeCreatedSomething.set(false);

            loadedClasses.removeIf(aClass -> objectsMap.containsKey(aClass));

            loadedClasses.forEach(aClass -> {


                Constructor<?>[] constructors = aClass.getConstructors();

                ForLoopConstructors: for(Constructor<?> constructor : constructors){

                    if(constructor.getParameterCount() <= objectsMap.keySet().size()){

                        Class<?>[] parameterTypes = constructor.getParameterTypes();

                        Object[] parameters = Arrays.stream(parameterTypes)
                                .map(bClass -> objectsMap.get(bClass))
                                .toArray();

                        for (Object parameter : parameters)

                            if(parameter == null)
                                break ForLoopConstructors;

                        try {

                            Object o = constructor.newInstance(parameters);

                            objectsMap.put(aClass, o);

                            haveWeCreatedSomething.set(true);

                            break;

                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });

        }while(haveWeCreatedSomething.get());

        objectsMap.forEach((aClass, o) -> {

            Method[] methods = aClass.getMethods();

            for(Method method : methods){

                if(method.getDeclaredAnnotations().length > 0){

                    Annotation[] annotations = method.getDeclaredAnnotations();

                    for (Annotation annotation : annotations) {

                        if(annotation.equals(InitServer.class)){

                            method.setAccessible(true);

                            try {

                                method.invoke(o);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

    }

    private static void recurseFiles(File dir, String path){

        File[] files = dir.listFiles();

        for(File file: files){

            if (file.isDirectory()) {

                recurseFiles(file, path + file.getName() + ".");
            }
            else if(file.getName().endsWith(".class")){

                String className = path + file.getName().replace(".class", "");
                classNames.add(className);

            }
        }
    }

}
