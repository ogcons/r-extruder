package hr.ogcs.rextruderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RExtruderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RExtruderServiceApplication.class, args);
	}

}
