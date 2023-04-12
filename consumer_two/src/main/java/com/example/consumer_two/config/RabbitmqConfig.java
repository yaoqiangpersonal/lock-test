package com.example.consumer_two.config;

import java.time.LocalDateTime;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @description：TODO
 * @author     ：weiliang
 * @date       ：2023/04/11 23:03
 */
@Configuration
public class RabbitmqConfig {
	@Autowired
	private RedisTemplate<Object, Object> redis;

	@Autowired
	private RedissonClient redissonClient;

	@RabbitListener(
			bindings =
			@QueueBinding(
					value = @Queue("hello.java"),
					exchange = @Exchange("hello.exchange"),
					key = "routing")
			, admin = "amqpAdmin")
	public void receive(Integer message) {
		Integer e = (Integer) redis.opsForValue().get("expect");

		if (e == null) {
			System.out.println("expect is null");
			return;
		}
		int limit = 1000;
		int count = 0;
		while (true) {
			Integer l = (Integer) redis.opsForList().index("lock", 0);
			Object n = (Integer) redis.opsForList().index("lock", 1);
			if (l == null) {
				System.out.println("lock is null");
				return;
			}
			System.out.println("get lock,l: " + l + "  n: " + n + "   e: " + e + "  message: " + message);
			if (message.compareTo(l) == 0 && l.compareTo(e) == 0) {
				System.out.println(LocalDateTime.now() + "     " + message + " success");
				RLock lock = redissonClient.getLock("dLock");
				lock.lock();
				redis.opsForValue().set("expect", n);
				redis.opsForList().leftPop("lock");
				lock.unlock();
				break;
			}
			if (count > limit) {
				System.out.println("exceed limit");
				break;
			}
			count++;
			e = (Integer) redis.opsForValue().get("expect");

		}

	}
}
