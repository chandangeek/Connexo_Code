package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.configuration.DefaultRawConfiguration;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.configuration.RawConfigurationConverter;
import com.atos.worldline.jss.configuration.RawFunctionTimeout;
import com.atos.worldline.jss.configuration.RawHsm;
import com.atos.worldline.jss.configuration.RawLabel;
import com.atos.worldline.jss.configuration.RawLabelMapping;
import com.atos.worldline.jss.configuration.RawRoutingEngineRule;
import com.atos.worldline.jss.internal.runtime.HSMState;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class HsmJssConfigLoader {

    public RawConfiguration load(File f){
        GsonBuilder gsbuilder = new GsonBuilder();
        gsbuilder.registerTypeAdapter(DefaultRawConfiguration.class, (InstanceCreator<DefaultRawConfiguration>) (a) -> DefaultRawConfiguration.builder().build());
        gsbuilder.registerTypeAdapter(RawHsm.class, (InstanceCreator<RawHsm>) (a) -> RawHsm.builder().build());
        gsbuilder.registerTypeAdapter(RawLabel.class, (InstanceCreator<RawLabel>) (a) -> RawLabel.builder().build());
        gsbuilder.registerTypeAdapter(RawLabelMapping.class, (InstanceCreator<RawLabelMapping>) (a) ->  RawLabelMapping.builder().build());
        gsbuilder.registerTypeAdapter(RawFunctionTimeout.class, (InstanceCreator<RawFunctionTimeout>) (a) ->  RawFunctionTimeout.builder().build());
        gsbuilder.registerTypeAdapter(RawRoutingEngineRule.class, (InstanceCreator<RawRoutingEngineRule>) (a) -> RawRoutingEngineRule.builder().build());
        gsbuilder.registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer());
        gsbuilder.registerTypeAdapter(HSMState.class, new HsmStateDeserializer());
        gsbuilder.setPrettyPrinting().disableHtmlEscaping();
        Gson gs = gsbuilder.create();
        try {
            return gs.fromJson(new FileReader(f), DefaultRawConfiguration.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RawConfiguration load(InputStream is){
        return new RawConfigurationConverter().loadFromInputStream(is);
    }

    final class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {
        ImmutableListDeserializer() {
        }

        public ImmutableList<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            TypeToken<ImmutableList<?>> immutableListToken = (TypeToken<ImmutableList<?>>) TypeToken.of(type);
            TypeToken<? super ImmutableList<?>> listToken = immutableListToken.getSupertype(List.class);
            List<?> list = context.deserialize(json, listToken.getType());
            return ImmutableList.copyOf(list);
        }
    }

    final class HsmStateDeserializer implements JsonDeserializer<HSMState> {
        HsmStateDeserializer() {
        }

        @Override
        public HSMState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            JsonElement stateValue = obj.get("stateValue");
            if (stateValue == null) {
                return null;
            }
            return HSMState.getHSMStateById(stateValue.getAsInt());
        }
    }

}
