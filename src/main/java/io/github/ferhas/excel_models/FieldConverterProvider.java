package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"rawtypes"})
public abstract class FieldConverterProvider {
    static final Map<Class<?>, FieldConverter<?>> converters = new HashMap<>();

    static {
        Set<Class<? extends FieldConverter>> typesAnnotatedWith = new Reflections(FieldConverterProvider.class.getPackage().getName())
                .getSubTypesOf(FieldConverter.class);

        addConverters(typesAnnotatedWith);
    }

    // manually added to not scan all packages
    public static void registerAdditionalConverter(Class<? extends FieldConverter> converter) {
        addConverters(Set.of(converter));
    }

    // manually added to not scan all packages
    public static void registerAdditionalConverters(Set<Class<? extends FieldConverter>> additionalConverters) {
        addConverters(additionalConverters);
    }

    private static void addConverters(Set<Class<? extends FieldConverter>> typesAnnotatedWith) {
        typesAnnotatedWith.forEach(clazz -> {
            TypeConverter annotation = clazz.getAnnotation(TypeConverter.class);

            for (Class<?> type : annotation.forTypes()) {
                try {
                    Constructor<? extends FieldConverter> declaredConstructor = clazz.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    converters.put(type, declaredConstructor.newInstance());
                } catch (Exception e) {
                    String error = String.format("Failed to initialize converters mapping! Check classes annotated with %s", TypeConverter.class.getSimpleName());
                    throw new RuntimeException(error, e);
                }
            }
        });
    }
}