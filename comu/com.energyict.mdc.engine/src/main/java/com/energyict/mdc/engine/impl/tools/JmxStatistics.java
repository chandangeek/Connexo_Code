/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JmxStatistics implements CompositeData {

    private static final Logger LOGGER = Logger.getLogger(JmxStatistics.class.getName());

    private String name;
    private int count;
    private ValueHolder min;
    private ValueHolder max;
    private long avg;

    public JmxStatistics(String name, int count, long min, long max, long avg) {
        super();
        this.name = name;
        this.count = count;
        this.min = new CurrentValue(min);
        this.max = new CurrentValue(max);
        this.avg = avg;
    }

    public JmxStatistics(String name) {
        super();
        this.name = name;
        this.count = 0;
        this.min = new NoValue();
        this.max = new NoValue();
        this.avg = 0;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public long getMin() {
        return this.min.getValue();
    }

    public long getMax() {
        return this.max.getValue();
    }

    public long getAvg() {
        return avg;
    }

    public void update(long duration) {
        min = new CurrentValue(min.minimum(duration));
        max = new CurrentValue(max.maximum(duration));
        avg = (avg * count + duration) / (count + 1);
        count++;
    }

    public CompositeType getCompositeType() {
        return doGetCompositeType();
    }

    public static CompositeType doGetCompositeType() {
        try {
            return new CompositeType("JmxStatistics", "Stats about a task", new String[]{"name", "count", "min", "max", "avg"}, new String[]{"name", "count", "min", "max", "avg"},
                    new OpenType[]{SimpleType.STRING, SimpleType.INTEGER, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG});
        } catch (OpenDataException e) {
            LOGGER.severe("Caught exception while creating composite type " + e);
            LOGGER.log(Level.FINE, "Caught exception while creating composite type " + e, e);
        }
        return null;
    }


    public Object get(String key) {
        switch (key) {
            case "name": {
                return getName();
            }
            case "min": {
                return getMin();
            }
            case "max": {
                return getMax();
            }
            case "avg": {
                return getAvg();
            }
            case "count": {
                return getCount();
            }
            default: {
                return null;
            }
        }
    }

    public Object[] getAll(String[] keys) {
        Object[] result = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            result[i] = get(key);
        }
        return result;
    }

    public boolean containsKey(String key) {
        return Arrays.asList("name", "count", "min", "max", "avg").contains(key);
    }

    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    public Collection values() {
        List<Object> result = new ArrayList<>();
        result.add(getName());
        result.add(getCount());
        result.add(getMin());
        result.add(getMax());
        result.add(getAvg());
        return result;
    }

    public CompositeDataSupport getSupport() throws OpenDataException {
        return new CompositeDataSupport(getCompositeType(), new String[]{"name", "count", "min", "max", "avg"}, values().toArray());
    }

    private interface ValueHolder {
        public long minimum (long otherValue);
        public long maximum (long otherValue);
        public long getValue ();
    }

    private class CurrentValue implements ValueHolder {
        private long value;

        private CurrentValue (long value) {
            super();
            this.value = value;
        }

        @Override
        public long minimum (long otherValue) {
            return Math.min(this.value, otherValue);
        }

        @Override
        public long maximum (long otherValue) {
            return Math.max(this.value, otherValue);
        }

        @Override
        public long getValue () {
            return this.value;
        }
    }

    private class NoValue implements ValueHolder {
        @Override
        public long minimum (long otherValue) {
            return otherValue;
        }

        @Override
        public long maximum (long otherValue) {
            return otherValue;
        }

        @Override
        public long getValue () {
            return 0;
        }
    }
}
