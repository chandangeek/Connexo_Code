package com.elster.jupiter.orm.proxy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.elster.jupiter.orm.DataMapper;

public class ProxyHandler<T> implements InvocationHandler {
	
	private final Object[] primaryKey;
	private final DataMapper<T> dataMapper;
	private T target;
	
	public ProxyHandler(Object[] primaryKey , DataMapper<T> dataMapper) {
		this.primaryKey = primaryKey;
		this.dataMapper = dataMapper;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (target == null) {
			target = getTarget();
		}
		return method.invoke(target,args);
	}
	
	private T getTarget() {
		return dataMapper.getExisting(primaryKey);
	}

}
