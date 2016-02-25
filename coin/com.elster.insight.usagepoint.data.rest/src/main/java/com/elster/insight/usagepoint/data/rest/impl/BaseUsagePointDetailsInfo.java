package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.time.Clock;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "serviceCategory")
@JsonTypeIdResolver(BaseUsagePointDetailsInfo.UsagePointDetailsTypeResolver.class)
public abstract class BaseUsagePointDetailsInfo {
    public Boolean collar;

    public BaseUsagePointDetailsInfo() {
    }

    public BaseUsagePointDetailsInfo(UsagePointDetail detail) {
        this.collar = detail.getCollar().isPresent() ? detail.getCollar().get() : null;
    }

    public abstract UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock);

    public static class UsagePointDetailsTypeResolver implements TypeIdResolver {

        private JavaType javaType;

        @Override
        public void init(JavaType javaType) {
            this.javaType = javaType;
        }

        @Override
        public String idFromValue(Object o) {
            return idFromValueAndType(o, o.getClass());
        }

        @Override
        public String idFromValueAndType(Object o, Class<?> aClass) {
            if (aClass.equals(ElectricityUsagePointDetailsInfo.class)) {
                return "ELECTRICITY";
            } else if (aClass.equals(GasUsagePointDetailsInfo.class)) {
                return "GAS";
            } else if (aClass.equals(WaterUsagePointDetailsInfo.class)) {
                return "WATER";
            } else if (aClass.equals(HeatUsagePointDetailsInfo.class)) {
                return "HEAT";
            } else {
                throw new IllegalStateException("class " + aClass + " is not found");
            }
        }

        @Override
        public String idFromBaseType() {
            return "UNKONOWN";
        }

        @Override
        public JavaType typeFromId(String s) {
            try {
                Class<?> clazz;
                switch (s) {
                    case "ELECTRICITY":
                        clazz = Class.forName(ElectricityUsagePointDetailsInfo.class.getName());
                        break;
                    case "GAS":
                        clazz = Class.forName(GasUsagePointDetailsInfo.class.getName());
                        break;
                    case "WATER":
                        clazz = Class.forName(WaterUsagePointDetailsInfo.class.getName());
                        break;
                    case "HEAT":
                        clazz = Class.forName(HeatUsagePointDetailsInfo.class.getName());
                        break;
                    default:
                        clazz = Class.forName(DefaultUsagePointDetailsInfo.class.getName());
                }

                return TypeFactory.defaultInstance().constructSpecializedType(javaType, clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("class " + s + " is not found");
            }
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }
}
