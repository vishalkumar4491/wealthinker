package in.wealthinker.wealthinker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class WealThinkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WealThinkerApplication.class, args);
	}

}
