package hello;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Configuration
public class RedisConfiguration {
    private static final String SENTINELS = "hostname1:26379,hostname2:26379,hostname3:26379";

    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(final ResourceLoader resourceLoader) throws IOException, InterruptedException {
        final Resource trustStoreResource = resourceLoader.getResource("classpath:/tls/redis-truststore.p12");
        final char[] trustStorePassword = Optional.ofNullable("Redis#22")
                .map(String::toCharArray)
                .orElse(null);

        return LettuceClientConfiguration.builder()
                .useSsl()
                .disablePeerVerification()
                .and()
                .clientOptions(ClientOptions.builder()
                        .sslOptions(SslOptions.builder()
                                .truststore(SslOptions.Resource.from(trustStoreResource.getFile()), trustStorePassword)
                                .build())
                        .build())
                .build();
    }


    @Bean
    public RedisSentinelConfiguration sentinelConfiguration() {
        final Set<String> sentinels = Set.of(SENTINELS);

        RedisSentinelConfiguration config = new RedisSentinelConfiguration("mymaster", sentinels);
        config.setPassword("foobar".toCharArray());
        return config;
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(RedisSentinelConfiguration sentinelConfiguration, LettuceClientConfiguration lettuceClientConfiguration) {
        return new LettuceConnectionFactory(sentinelConfiguration, lettuceClientConfiguration);
    }
}
