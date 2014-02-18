package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Fakes {
    ;

    public static final class Key {

        private Object[] values;

        private Key(Object... values) {
            this.values = values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            return Arrays.equals(values, ((Key) o).values);

        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }
    }

    public static interface KeyGetter<T> {

        Key getKey(T entity);
    }

    public static class FakeFactory<T> implements DataMapper<T> {

        private final Map<Key, T> store = new HashMap<>();
        private final KeyGetter<T> keyGetter;

        public FakeFactory(KeyGetter<T> keyGetter) {
            this.keyGetter = keyGetter;
        }

        @Override
        public Optional<T> getEager(Object... object) {
            //TODO automatically generated method body, provide implementation.
            return null;
        }

        @Override
        public T lock(Object... values) {
            return null;
        }

        @Override
        public void persist(T object) {
            if (store.containsKey(getKey(object))) {
                throw new IllegalArgumentException();
            }
            store.put(getKey(object), object);
        }

        private Key getKey(T object) {
            return keyGetter.getKey(object);
        }

        @Override
        public void persist(List<T> objects) {
            for (T object : objects) {
                persist(object);
            }
        }


        @Override
        public void update(T object, String... fieldNames) {
            store.put(getKey(object), object);
        }

        @Override
        public void update(List<T> objects, String... fieldNames) {
            for (T object : objects) {
                update(object);
            }
        }

        @Override
        public void remove(T object) {
            store.remove(getKey(object));
        }

        @Override
        public void remove(List<T> objects) {
            for (T object : objects) {
                remove(object);
            }
        }

        @Override
        public QueryExecutor<T> with(DataMapper<?>... tupleHandlers) {
            return null;
        }

        @Override
        public List<T> find() {
            return FluentIterable.from(store.values()).toList();
        }

        @Override
        public List<T> find(String fieldName, Object value) {
            return FluentIterable.from(store.values()).filter(new FieldMatcher<T>(fieldName, value)).toList();
        }

        @Override
        public List<T> find(String fieldName, Object value, final String orderBy) {
            return FluentIterable.from(store.values()).filter(new FieldMatcher<T>(fieldName, value)).toSortedList(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    Comparable first = (Comparable) getFieldValue(o1, orderBy);
                    Comparable second = (Comparable) getFieldValue(o1, orderBy);
                    return first.compareTo(second);
                }
            });
        }

        @Override
        public List<T> find(String fieldName1, Object value1, String fieldName2, Object value2) {
            return FluentIterable.from(store.values())
                    .filter(new FieldMatcher<T>(fieldName1, value1))
                    .filter(new FieldMatcher<T>(fieldName2, value2))
                    .toList();
        }

        @Override
        public List<T> find(String fieldName1, Object value1, String fieldName2, Object value2, String orderBy) {
            return null;
        }

        @Override
        public List<T> find(String[] fieldNames, Object[] values) {
            return null;
        }

        @Override
        public List<T> find(String[] fieldNames, Object[] values, String order, String... orders) {
            return null;
        }

        @Override
        public List<T> find(Map<String, Object> valueMap) {
            return null;
        }

        @Override
        public List<T> find(Map<String, Object> valueMap, String order, String... orders) {
            return null;
        }

        @Override
        public Optional<T> getOptional(Object... values) {
            return Optional.fromNullable(store.get(new Key(values)));
        }

        @Override
        public List<JournalEntry<T>> getJournal(Object... values) {
            return null;
        }

        @Override
        public T getExisting(Object... values) {
            return null;
        }

        @Override
        public Optional<T> getUnique(String fieldName, Object value) {
            return null;
        }

        @Override
        public Optional<T> getUnique(String fieldName1, Object value1, String fieldName2, Object value2) {
            return null;
        }

        @Override
        public Optional<T> getUnique(String[] fieldNames, Object[] values) {
            return null;
        }

        private class FieldMatcher<T> implements Predicate<T> {

            private final String fieldName;
            private final Object value;

            public FieldMatcher(String fieldName, Object value) {
                this.fieldName = fieldName;
                this.value = value;
            }

            @Override
            public boolean apply(T input) {
                String fieldName1 = fieldName;
                Object fieldValue = getFieldValue(input, fieldName1);
                return Objects.equal(fieldValue, value);
            }

        }

		@Override
		public List<T> select(Condition condition, String order, String... orderBy) {
			return null;
		}

		@Override
		public Object getAttribute(Object target, String fieldName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<T> select(Condition condition, Order... orderings) {
			return null;
		}

		@Override
		public List<T> find(String fieldName, Object value, Order... orders) {
			return null;
		}

		@Override
		public List<T> find(String fieldName1, Object value1,String fieldName2, Object value2, Order... orders) {
			return null;
		}

		@Override
		public List<T> find(String[] fieldNames, Object[] values, Order... orders) {
			return null;
		}

		@Override
		public List<T> find(Map<String, Object> valueMap, Order... orders) {
			return null;
		}

    }

    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException();
        }
    }


    public static class MeterActivationValidationFactory extends FakeFactory<MeterActivationValidation> {

        public MeterActivationValidationFactory() {
            super(new KeyGetter<MeterActivationValidation>() {
                @Override
                public Key getKey(MeterActivationValidation entity) {
                    return new Key(entity.getId());
                }
            });
        }
    }

    public static class ChannelValidationFactory extends FakeFactory<ChannelValidation> {

        public ChannelValidationFactory() {
            super(new KeyGetter<ChannelValidation>() {
                @Override
                public Key getKey(ChannelValidation entity) {
                    return new Key(entity.getId());
                }
            });
        }
    }
}
