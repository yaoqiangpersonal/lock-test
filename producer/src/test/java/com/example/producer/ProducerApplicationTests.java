package com.example.producer;

import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class ProducerApplicationTests {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RedisTemplate<Object,Object> redisTemplate;

	@Test
	void contextLoads() {
		redisTemplate.opsForValue().set("expect", 0);
		for(int i = 0; i < 100; i++){
			rabbitTemplate.convertAndSend( "hello.exchange","routing", i);
			redisTemplate.opsForList().rightPush("lock", i);
		}
	}

}
