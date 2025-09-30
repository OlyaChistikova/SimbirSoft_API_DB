package helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public final class ParametersProvider {
    /**
     * Список параметров
     */
    private final ArrayList<Properties> propertiesList = new ArrayList<>();

    /**
     * Получить название конфигурационного файла из system properties.
     *
     * @return список название конфигурационных файлов
     */
    private static ArrayList<String> getConfigFileNames() {
        ArrayList<String> configFileNames = new ArrayList<>();
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("config.location")) {
                String[] fileNames = System.getProperties().getProperty(key).split(";");
                configFileNames.addAll(Arrays.asList(fileNames));
            }
        }
        return configFileNames;
    }

    /**
     * Конструктор, который загружает свойства из конфигурационных файлов.
     * Исключения внутри преобразуются в RuntimeException.
     */
    private ParametersProvider() {
        try {
            loadProperties("src/test/resources/env_local.xml");
            ArrayList<String> configFileNames = getConfigFileNames();
            for (String fileName : configFileNames) {
                loadProperties(fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке конфигурационных файлов", e);
        }
    }

    /**
     * Загружает свойства из файла и добавляет их в список.
     * Внутри ловим IOException и выбрасываем RuntimeException.
     *
     * @param fileName имя файла конфигурации
     */
    private void loadProperties(String fileName) {
        try {
            Properties properties = new Properties();
            properties.loadFromXML(Files.newInputStream(Paths.get(fileName)));
            propertiesList.add(properties);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла: " + fileName, e);
        }
    }

    /**
     * Instance holder для singleton.
     */
    private static ParametersProvider instance;

    /**
     * Получить singleton-экземпляр. Внутри исключения преобразуются в RuntimeException.
     *
     * @return instance
     */
    private static ParametersProvider getInstance() {
        if (instance == null) {
            instance = new ParametersProvider();
        }
        return instance;
    }

    /**
     * Получить значение свойства по ключу.
     * Внутри исключения преобразуются в RuntimeException.
     *
     * @param key ключ свойства
     * @return значение свойства или пустая строка, если не найдено
     */
    public static String getProperty(final String key) {
        try {
            for (Properties properties : getInstance().propertiesList) {
                String result = properties.getProperty(key, null);
                if (result != null) {
                    return result;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении свойства: " + key, e);
        }
        return "";
    }
}