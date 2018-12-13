/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.security.thread.impl;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import static org.assertj.core.api.Assertions.assertThat;

public class ThreadPrincipalServiceTest {

	@Test
	public void test() throws InterruptedException {
		ThreadPrincipalService service = new ThreadPrincipalServiceImpl();
		Map<Thread, Principal> map = new ConcurrentHashMap<>();
		Principal p1 = () -> "P1";
		Principal p2 = () -> "P2";
		Thread thread1 = new Thread(() -> { service.set(p1); map.put(Thread.currentThread(), service.getPrincipal());});
		Thread thread2 = new Thread(() -> { service.set(p2); map.put(Thread.currentThread(), service.getPrincipal());});
		thread1.start();
		thread2.start();
		thread2.join();
		thread1.join();
		assertThat(map.get(thread1)).isEqualTo(p1);
		assertThat(map.get(thread2)).isEqualTo(p2);
		
	}
}
