package io.pedrini.expsplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ExpSplitApplication {

    static void main(String[] args) {
        SpringApplication.run(ExpSplitApplication.class, args);
    }

}
