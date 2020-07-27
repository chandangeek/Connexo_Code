/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.CacheClearedEvent;
import com.elster.jupiter.orm.Finder;
import com.elster.jupiter.pubsub.Publisher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.List;
import java.util.concurrent.TimeUnit;

interface TableCache<T> {
	T get(KeyValue key);

	void put(KeyValue key, T value);

	void remove(T entity);

	void renew();

	boolean reloadCacheIfNeeded(Object object, Finder<T> finder, KeyValue keyValue);

	CacheStats getCacheStats();

	class NoCache<T> implements TableCache<T> {

		@Override
		public T get(KeyValue key) {
			return null;
		}

		@Override
		public void put(KeyValue key, T value) {
			// NoCache means no cache
		}

		@Override
		public void remove(T entity) {
			// NoCache means no cache
		}

		@Override
		public void renew() {
			// NoCache means no cache
		}

		@Override
		public CacheStats getCacheStats() {
			return null;
		}

		@Override
		public boolean reloadCacheIfNeeded(Object object, Finder<T> finder, KeyValue keyValue){
			return false;
		}

	}

	class TupleCache<T> implements TableCache<T> {

		private final TableImpl<T> table;
		private Cache<KeyValue, T> cache;

		TupleCache(TableImpl<T> table, long ttl, long maximumSize, boolean recordStats) {
			this.table = table;
			CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(maximumSize)
					.expireAfterWrite(ttl, TimeUnit.MILLISECONDS);
			if (recordStats) {
				cacheBuilder = cacheBuilder.recordStats();
			}
			this.cache = cacheBuilder.build();
		}

		public synchronized void renew() {
			this.cache.invalidateAll();
			cacheCleared();
		}

		private void cacheCleared() {
			Publisher publisher = table.getDataModel().getOrmService().getPublisher();
			publisher.publish(new CacheClearedEvent(table.getComponentName(), table.getName()));
		}

		private KeyValue getKey(T entity) {
			return table.getPrimaryKey(entity);
		}

		@Override
		public synchronized T get(KeyValue key) {
			return cache.getIfPresent(key);
		}

		@Override
		public synchronized void put(KeyValue key, T value) {
			cache.put(key, value);
		}

		@Override
		public synchronized void remove(T entity) {
			if (cache != null) {
				cache.invalidate(getKey(entity));
			}
		}

		@Override
		public CacheStats getCacheStats() {
			return cache.stats();
		}

		@Override
		public boolean reloadCacheIfNeeded(Object object, Finder<T> finder, KeyValue keyValue){
			return false;
		}
	}

	class WholeTableCache<T> implements TableCache<T> {
		private final TableImpl<T> table;
		private Cache<KeyValue, T> cache;

		WholeTableCache(TableImpl<T> table, long ttl ,boolean recordStats) {
			this.table = table;
			CacheBuilder cacheBuilder = CacheBuilder.newBuilder()
					.expireAfterWrite(ttl, TimeUnit.MILLISECONDS);
			if (recordStats) {
				cacheBuilder = cacheBuilder.recordStats();
			}
			this.cache = cacheBuilder.build();
		}

		public synchronized void renew() {
			this.cache.invalidateAll();
			cacheCleared();
		}

		private void cacheCleared() {
			Publisher publisher = table.getDataModel().getOrmService().getPublisher();
			publisher.publish(new CacheClearedEvent(table.getComponentName(), table.getName()));
		}

		private KeyValue getKey(T entity) {
			return table.getPrimaryKey(entity);
		}

		@Override
		public synchronized T get(KeyValue key) {
			return cache.getIfPresent(key);
		}

		@Override
		public synchronized void put(KeyValue key, T value) {
			cache.put(key, value);
		}

		@Override
		public synchronized void remove(T entity) {
			if (cache != null) {
				cache.invalidate(getKey(entity));
			}
		}

		@Override
		public CacheStats getCacheStats() {
			return cache.stats();
		}

		@Override
		public boolean reloadCacheIfNeeded(Object object, Finder<T> finder, KeyValue keyValue){
			if (object == null){
				cache.invalidateAll();
				List<T> objects = finder.find();
				for (T objectitem : objects) {
					cache.put(table.getPrimaryKey(objectitem), objectitem);
				}
				return true;
			}
			return false;
		}

	}
}

