package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"rawtypes"})
public abstract class FieldConverterProvider {
    private static final Map<Class<?>, FieldConverter<?>> converters = new HashMap<>();

    static {
        String packageName = FieldConverterProvider.class.getPackage().getName();
        Set<Class<? extends FieldConverter>> typesAnnotatedWith = new Reflections(packageName).getSubTypesOf(FieldConverter.class);
        addConverters(typesAnnotatedWith);
    }

    static FieldConverter<?> getFieldConverter(Field field) {
        // before try to get a specific converter (for Enums)
        Class<?> fieldType = field.getType();
        FieldConverter<?> fieldConverter = converters.get(fieldType);

        // if a specific converter was not found, we retrieve the generic Enum converter
        if (fieldConverter == null && fieldType.isEnum()) {
            fieldConverter = converters.get(Enum.class);
        }

        return fieldConverter;
    }

    static <E> Map<Annotation, Field> getFieldMap(Class<E> type, boolean isExport) {
        Map<Annotation, Field> fieldMap = new HashMap<>();

        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                field.setAccessible(true);
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

                if (!(annotation.onlyExport() && !isExport)) {
                    fieldMap.put(annotation, field);
                }
            } else if (field.isAnnotationPresent(ExcelObject.class)) {
                field.setAccessible(true);
                ExcelObject annotation = field.getAnnotation(ExcelObject.class);
                fieldMap.put(annotation, field);
            }
        }

        if (fieldMap.isEmpty()) {
            throw new IllegalArgumentException(String.format("Model '%s' has not been configured? check if you inserted @%s annotations", type.getSimpleName(), ExcelColumn.class.getSimpleName()));
        }

        return fieldMap;
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
            TypeConverter annotation = getAnnotations(clazz);

            if (annotation == null) {
                throw new IllegalStateException(String.format("Expected @%s annotation on %s class!", TypeConverter.class.getSimpleName(), clazz.getSimpleName()));
            }

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

    private static TypeConverter getAnnotations(Class<?> classType) {
        while (!classType.getName().equals(Object.class.getName())) {
            if (classType.isAnnotationPresent(TypeConverter.class)) {
                return classType.getAnnotation(TypeConverter.class);
            }
            classType = classType.getSuperclass();
        }
        return null;
    }
}