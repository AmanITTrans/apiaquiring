package acquiring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class DeliveryExecutorConfig {

    @Bean(name = "deliveryPriceExecutor")
    public Executor deliveryPriceExecutor() {
        return Executors.newFixedThreadPool(5);
    }
}