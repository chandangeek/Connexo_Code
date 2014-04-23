package com.elster.jupiter.issue.rest.response;

import javax.ws.rs.WebApplicationException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ResponseHelper extends javax.ws.rs.core.Response {

    public static ResponseBuilder ok(Object entity) {
        ResponseBuilder b = ok();
        b.entity(new SingleResponse<Object>(entity));
        return b;
    }

    public static <T> ResponseBuilder ok(List<? extends Object> data, Class<T> entityWrapper) {
        ResponseBuilder b = ok();
        b.entity(new ListResponse<T>(data, entityWrapper));
        return b;
    }

    public static <T> ResponseBuilder ok(List<? extends Object> data, Class<T> entityWrapper, long start, long limit) {
        ResponseBuilder b = ok();
        b.entity(new ListResponse<T>(data, entityWrapper, start, limit));
        return b;
    }

    private static class SingleResponse<T>{
        private T data;

        public T getData() {
            return data;
        }

        private SingleResponse(T data) {
            this.data = data;
        }
    }

    private static class ListResponse<T> {
        private static final Logger LOG = Logger.getLogger(ListResponse.class.getName());

        private List<T> data = new ArrayList<>();
        private long total;

        private ListResponse(List<? extends Object> data, Class<T> entityWrapper) {
            validateEntityWrapper(entityWrapper);
            if (data != null) {
                initDataField(data, entityWrapper);
                total = data.size();
            }
        }

        private ListResponse(List<? extends Object> data, Class<T> entityWrapper, long start, long limit) {
            validateEntityWrapper(entityWrapper);
            if (data != null) {
                initDataField(data, entityWrapper);
                total = start + data.size();
                if (data.size() == limit) {
                    total++;
                }
            }
        }

        private void validateEntityWrapper(Class<T> entityWrapper) {
            if (entityWrapper == null) {
                throw new IllegalArgumentException("EntityWrapper class can't be null");
            }
        }

        private final void initDataField(List<? extends Object> sourceData, Class<T> entityWrapperClass) {
            Constructor<?> constructor = null;
            for (Object entity : sourceData) {
                try {
                    if (constructor == null) {
                        constructor = getWrapperConstructor(entityWrapperClass, entity.getClass());
                    }
                    data.add(entityWrapperClass.cast(constructor.newInstance(entity)));
                } catch (ReflectiveOperationException e) {
                    LOG.log(Level.SEVERE, "Unable to create list wrapper for '" + entityWrapperClass.getName() + "' class", e);
                    throw new WebApplicationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }

        private Constructor<?> getWrapperConstructor(Class<T> wrapperClass, Class<?> entityClass) throws ReflectiveOperationException {
            Constructor<?>[] availableConstructors = wrapperClass.getConstructors();

            for (Constructor<?> constructor : availableConstructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 1 || !parameterTypes[0].isAssignableFrom(entityClass)) {
                    continue;
                }
                return constructor;
            }
            throw new ReflectiveOperationException("Unable to find correct constructor for " + wrapperClass.getName());
        }

        public List<T> getData() {
            return data;
        }

        public long getTotal() {
            return total;
        }
    }
}
