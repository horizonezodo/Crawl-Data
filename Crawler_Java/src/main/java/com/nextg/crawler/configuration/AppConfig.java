package com.nextg.crawler.configuration;


import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    public static String scrapyWorkDir;

    public static String pythonEnviromentFolder;

    public static String uploadsFolderWorkDir;

    public static  String resultFolderWorkDir;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
            scrapyWorkDir = properties.getProperty("crawler.scrapy.workDir");
            pythonEnviromentFolder = properties.getProperty("crawler.environment.folder");
            uploadsFolderWorkDir = scrapyWorkDir + "/uploads";
            resultFolderWorkDir = scrapyWorkDir + "/result/";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
