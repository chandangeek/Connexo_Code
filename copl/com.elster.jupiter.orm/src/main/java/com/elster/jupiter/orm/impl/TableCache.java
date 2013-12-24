package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TableCache<T> {
	Optional<List<T>> find(DataMapperReader<T> reader);
	Optional<T> getOptional(DataMapperReader<T> reader,Object[] key);
	void put(DataMapperReader<T> reader, Object[] key , T value);
	void cache(DataMapperReader<T> reader,T value);
	void remove(T entity);
	void renew();
	 
	class NoCache<T> implements TableCache<T> {
		@Override
		public Optional<List<T>> find(DataMapperReader<T> reader) {
			return Optional.absent();
		}

		@Override
		public Optional<T> getOptional(DataMapperReader<T> reader,Object[] key) {
			return Optional.absent();
		}

		@Override
		public void put(DataMapperReader<T> reader,Object[] key, T value) {
		}

		@Override
		public void cache(DataMapperReader<T> reader, T value) {
		}

		@Override
		public void remove(T entity) {
		}
		
		@Override
		public void renew() {
		}
	
	}
	
	class TupleCache<T> implements TableCache<T> {
		
		private final TableImpl table;
		private Map<ArrayWrapper, T> cache;
		
		public TupleCache(TableImpl table) {
			this.table = table;
		}
		
		private void cacheChange() {
			Publisher publisher = table.getDataModel().getOrmService().getPublisher();
			publisher.publish(new InvalidateCacheRequest(table.getComponentName(), table.getName()));
		}
		
		synchronized public void renew() {
			this.cache = null; 
		}
		
		private void initCache(DataMapperReader<T> reader) {
			cache = new HashMap<>();
			for (T each : table.<T>getQuery().select(Condition.TRUE)) {
				cache.put(createKey(getKey(each)), each);
			}
		}
		
		private Object[] getKey(T entity) {
			return table.getPrimaryKey(entity);
		}
		
		@Override
        public synchronized Optional<List<T>> find(DataMapperReader<T> reader) {
			if (cache == null) {
				initCache(reader);
			} 
			List<T> list = new ArrayList<>(cache.values());
			return Optional.of(list);
		}

		@Override
        public synchronized Optional<T> getOptional(DataMapperReader<T> reader, Object[] key) {
			if (cache == null) {
				initCache(reader);
			} 
			return Optional.fromNullable(cache.get(createKey(key)));
		}

		@Override
        public synchronized void put(DataMapperReader<T> reader, Object[] key, T value) {
			if (cache == null) {
				initCache(reader);
			} 
			cache.put(createKey(key),value);
			cacheChange();
		}

		private ArrayWrapper createKey(Object[] in) {
		    return new ArrayWrapper(in);
		}

		@Override
		public void cache(DataMapperReader<T> reader, T value) {
			put(reader,getKey(value),value);
		}

		@Override
        public synchronized void remove(T entity) {
			cache.remove(createKey(getKey(entity)));
			cacheChange();
		}  
	}
	
	final class ArrayWrapper {
		
		private final Object[] key;
		
		ArrayWrapper(Object[] values) {
			this.key = values;
		}
		
		@Override
		public boolean equals(Object other) {
	        if (this == other) {
	            return true;
	        }
	        if (!(other instanceof ArrayWrapper)) {
	            return false;
	        }
			return Arrays.equals(key, ((ArrayWrapper) other).key);
		}
		
		@Override 
		public int hashCode() {
			return Arrays.hashCode(key);
		}
	}
}

