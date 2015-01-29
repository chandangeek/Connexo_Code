package com.elster.jupiter.orm.impl;

import java.util.HashMap;
import java.util.Map;

import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.pubsub.Publisher;

public interface TableCache<T> {
	T get(KeyValue key);
	void put(KeyValue key , T value);
	void remove(T entity);
	void renew();
	 
	class NoCache<T> implements TableCache<T> {
	
		@Override
		public T get(KeyValue key) {
			return null;
		}

		@Override
		public void put(KeyValue key, T value) {
		}

		@Override
		public void remove(T entity) {
		}
		
		@Override
		public void renew() {
		}
	
	}
	
	class TupleCache<T> implements TableCache<T> {
		
		private final TableImpl<T> table;
		private Map<KeyValue, T> cache = new HashMap<>();
		
		public TupleCache(TableImpl<T> table) {
			this.table = table;
		}
		
		private void cacheChange() {
			Publisher publisher = table.getDataModel().getOrmService().getPublisher();
			publisher.publish(new InvalidateCacheRequest(table.getComponentName(), table.getName()));
		}
		
		synchronized public void renew() {
			this.cache = new HashMap<>(); 
		}
				
		private KeyValue getKey(T entity) {
			return table.getPrimaryKey(entity);
		}
			
		@Override
        public synchronized T get(KeyValue key) {
			return cache.get(key);
		}

		@Override
        public synchronized void put(KeyValue key, T value) {
			cache.put(key,value);			
		}

		@Override
        public synchronized void remove(T entity) {
			if (cache != null) {
				cache.remove(getKey(entity));
			}
			cacheChange();
		}  		
	}
	
}

