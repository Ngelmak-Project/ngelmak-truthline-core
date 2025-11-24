package org.ngelmakproject.service.mapper;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.thymeleaf.util.StringUtils;

public class CsvToObjectMapper {
    public static <T> List<T> getRecords(Class<T> clazz, String path) {
        List<T> instances = new ArrayList<>();
        try (Reader redader = new FileReader(path);
                CSVParser parser = new CSVParser(redader,
                        CSVFormat.DEFAULT.withDelimiter(';')
                                .withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
            List<String> headerNames = parser.getHeaderNames();
            List<String> names = headerNames.stream().map(headerName -> {
                String[] parts = StringUtils.split(headerName, "_");
                String name = parts[0];
                for (int i = 1; i < parts.length; i++)
                    name += StringUtils.capitalize(parts[i]);
                return name;
            }).toList();
            instances = parser.getRecords().stream().map(record -> {
                try {
                    T object = clazz.getDeclaredConstructor().newInstance();
                    for (int i = 0; i < names.size(); i++) {
                        String name = names.get(i);
                        String headerName = headerNames.get(i);
                        try {
                            Field field = clazz.getDeclaredField(name);
                            field.setAccessible(true);
                            Object value = parseValue(field.getType(), record.get(headerName));
                            field.set(object, value);
                        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                                | IllegalAccessException e) {
                        }
                    }
                    return object;
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                }
                return null;
            }).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        instances = instances.stream().filter(instance -> instance != null).toList();
        return instances;
    }

    private static Object parseValue(Class<?> type, String value) {
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(value);
        else if (type == long.class || type == Long.class)
            return Long.parseLong(value);
        else if (type == double.class || type == Double.class)
            return Double.parseDouble(value);
        else if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(value);
        else
            return value;
    }
}
