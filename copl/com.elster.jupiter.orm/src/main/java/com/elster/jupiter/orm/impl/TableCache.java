package com.elster.jupiter.orm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.conditions.Condition;

public interface TableCache<T> {
	T get(KeyValue key);
	void put(KeyValue key , T value);
	void cache(T value);
	void remove(T entity);
	void renew();
	void start();
	 
	class NoCache<T> implements TableCache<T> {
	
		@Override
		public T get(KeyValue key) {
			return null;
		}

		@Override
		public void put(KeyValue key, T value) {
		}

		@Override
		public void cache(T value) {
		}

		@Override
		public void remove(T entity) {
		}
		
		@Override
		public void renew() {
		}
	
		@Override
		public void start() {
		}
	}
	
	class TupleCache<T> implements TableCache<T> {
		
		private final TableImpl<T> table;
		private Map<KeyValue, T> cache;
		
		public TupleCache(TableImpl<T> table) {
			this.table = table;
		}
		
		private void cacheChange() {
			Publisher publisher = table.getDataModel().getOrmService().getPublisher();
			publisher.publish(new InvalidateCacheRequest(table.getComponentName(), table.getName()));
		}
		
		synchronized public void renew() {
			this.cache = null; 
		}
		
		private void initCache() {
			cache = new HashMap<>();
			for (T each : table.getQuery().select(Condition.TRUE)) {
				cache.put(getKey(each), each);
			}
		}
		
		private KeyValue getKey(T entity) {
			return table.getPrimaryKey(entity);
		}
		
	
		@Override
        public synchronized T get(KeyValue key) {
			start();
			return cache.get(key);
		}

		@Override
        public synchronized void put(KeyValue key, T value) {
			start();
			cache.put(key,value);
			cacheChange();
		}

		@Override
		public void cache(T value) {
			put(getKey(value),value);
		}

		@Override
        public synchronized void remove(T entity) {
			if (cache != null) {
				cache.remove(getKey(entity));
			}
			cacheChange();
		}  
		
		@Override
		public synchronized void start() {
			if (cache == null) {
				initCache();
			}
		}
	}
	
}

