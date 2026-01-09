package com.hospital.scheduling.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.queue.created}")
    private String createdQueue;
    
    @Value("${rabbitmq.queue.updated}")
    private String updatedQueue;
    
    @Value("${rabbitmq.routing-key.created}")
    private String createdRoutingKey;
    
    @Value("${rabbitmq.routing-key.updated}")
    private String updatedRoutingKey;
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }
    
    @Bean
    public Queue createdQueue() {
        return new Queue(createdQueue, true);
    }
    
    @Bean
    public Queue updatedQueue() {
        return new Queue(updatedQueue, true);
    }
    
    @Bean
    public Binding createdBinding() {
        return BindingBuilder
                .bind(createdQueue())
                .to(exchange())
                .with(createdRoutingKey);
    }
    
    @Bean
    public Binding updatedBinding() {
        return BindingBuilder
                .bind(updatedQueue())
                .to(exchange())
                .with(updatedRoutingKey);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
