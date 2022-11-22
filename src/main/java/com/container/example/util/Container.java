package com.container.example.util;

import com.container.example.annotation.Autowired;
import com.container.example.annotation.Component;
import com.container.example.annotation.PostConstruct;
import com.container.example.annotation.Qualifier;
import com.container.example.exception.CustomException;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Container {
    private static final String PACKAGE_NAME = "com.container.example";
    private static final Container container = new Container();
    private static final List<Class<?>> classes = getClasses();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Container getContainer() {
        return container;
    }

    public Object getInstance(Class<?> interfaceClazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new CustomException("Injection failed, "
                    + "missing @Component annotation on the class "
                    + clazz.getName());
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : declaredConstructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                clazzImplementationInstance = resolveConstructorAndGetInstance(constructor);
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = resolveConstructorAndGetInstance(declaredConstructors[0]);
        }
        resolvePostConstruct(clazzImplementationInstance, clazz);
        instances.put(clazz, clazzImplementationInstance);
        return clazzImplementationInstance;
    }

    private void resolvePostConstruct(Object clazzImplementationInstance, Class<?> clazz) throws IllegalAccessException, InvocationTargetException {
        Method postConstruct = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PostConstruct.class))
                .findFirst().orElse(null);
        if (postConstruct != null) {
            postConstruct.setAccessible(true);
            postConstruct.invoke(clazzImplementationInstance);
        }
    }

    private Object resolveConstructorAndGetInstance(Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object clazzImplementationInstance;
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (Class<?> param : parameterTypes) {
            Class<?> implementation = findImplementation(param);
            getInstance(implementation);
        }
        Map<Integer, String> fieldImplementationName = resolveAnnotation(constructor);

        Object[] args = Arrays.stream(parameterTypes)
                .filter(i -> instances.entrySet()
                        .stream()
                        .anyMatch(v -> v.getKey().getSimpleName().contains(i.getSimpleName())))
                .map(this::findImplementation)
                .map(instances::get)
                .toArray();
        if (!fieldImplementationName.isEmpty()) {
            for (Map.Entry<Integer, String> entry : fieldImplementationName.entrySet()) {
                Integer indexOfInstance = entry.getKey();
                String v = entry.getValue();
                Class<?> implementationByName = findImplementationByName(v, parameterTypes[indexOfInstance]);
                Object instance = getInstance(implementationByName);
                args[indexOfInstance] = instance;
            }
        }
        clazzImplementationInstance = constructor.newInstance(args);
        return clazzImplementationInstance;
    }

    private Map<Integer, String> resolveAnnotation(Constructor<?> constructor) {
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Map<Integer, String> fieldImplementationName = new HashMap<>();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                Annotation annotation = parameterAnnotations[i][j];
                if (annotation instanceof Qualifier) {
                    Qualifier qualifier = (Qualifier) annotation;
                    fieldImplementationName.put(i, qualifier.value());
                }
            }
        }
        return fieldImplementationName;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        try {
            if (interfaceClazz.isInterface()) {
                List<Class<?>> implementations = classes.stream()
                        .filter(c -> Arrays.asList(c.getInterfaces()).contains(interfaceClazz))
                        .collect(Collectors.toList());
                List<Class<?>> qualifierClasses = implementations.stream()
                        .filter(i -> i.isAnnotationPresent(Qualifier.class))
                        .collect(Collectors.toList());
                if (!qualifierClasses.isEmpty()) {
                    return classes.stream()
                            .filter(c -> c.getSimpleName().equalsIgnoreCase(qualifierClasses.stream()
                                    .map(m -> m.getAnnotation(Qualifier.class).value())
                                    .collect(Collectors.joining())))
                            .findFirst()
                            .orElseThrow(() -> new CustomException("Can't find implementation"));
                }
                return implementations.get(0);
            }
        } catch (Exception ex) {
            throw new CustomException("Can't find implementation", ex);
        }
        return interfaceClazz;
    }

    private Class<?> findImplementationByName(String name, Class<?> interfaceClazz) {
        try {
            if (interfaceClazz.isInterface()) {
                return classes.stream()
                        .filter(c -> Arrays.asList(c.getInterfaces()).contains(interfaceClazz)
                                && c.getSimpleName().equalsIgnoreCase(name))
                        .findFirst()
                        .orElseGet(() -> null);
            }
            return interfaceClazz;
        } catch (Exception ex) {
            throw new CustomException("Can't find implementation by name " + name, ex);
        }
    }

    private static List<Class<?>> getClasses() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new RuntimeException("Class loader is null");
        }
        String path = PACKAGE_NAME.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            ArrayList<Class<?>> classes = new ArrayList<>();
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, PACKAGE_NAME));
            }
            return classes;
        } catch (Exception ex) {
            throw new CustomException("Something went wrong while scanning classes", ex);
        }
    }

    private static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().contains(".")) {
                        throw new CustomException("File name shouldn't consist point.");
                    }
                    classes.addAll(findClasses(file, packageName + "."
                            + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        return classes;
    }
}
