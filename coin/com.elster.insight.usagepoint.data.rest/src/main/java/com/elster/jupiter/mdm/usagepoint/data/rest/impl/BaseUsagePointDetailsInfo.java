/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.time.Clock;
import java.util.Arrays;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "serviceCategory")
@JsonTypeIdResolver(BaseUsagePointDetailsInfo.UsagePointDetailsTypeResolver.class)
public abstract class BaseUsagePointDetailsInfo {
    public YesNoAnswer collar;

    public BaseUsagePointDetailsInfo() {
    }

    public BaseUsagePointDetailsInfo(UsagePointDetail detail) {
        this.collar = detail.isCollarInstalled();
    }

    public abstract ServiceKind getKind();

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
            try {
                return ((BaseUsagePointDetailsInfo)aClass.newInstance()).getKind().name();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("class " + aClass.getName() + " is not found");
            }
        }

        @Override
        public String idFromBaseType() {
            return ServiceKind.OTHER.name();
        }

        @Override
        public JavaType typeFromId(String s) {
            try {
                ServiceKind kind = Arrays.stream(ServiceKind.values()).filter(k -> k.name().equals(s)).findAny().orElse(ServiceKind.OTHER);
                Class<?> clazz;
                switch (kind) {
                    case ELECTRICITY:
                        clazz = Class.forName(ElectricityUsagePointDetailsInfo.class.getName());
                        break;
                    case GAS:
                        clazz = Class.forName(GasUsagePointDetailsInfo.class.getName());
                        break;
                    case WATER:
                        clazz = Class.forName(WaterUsagePointDetailsInfo.class.getName());
                        break;
                    case HEAT:
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
