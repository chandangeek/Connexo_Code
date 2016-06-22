package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroActionTranslationKey;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will link the Device to the Usage Point.
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#LINK_TO_USAGE_POINT}
 */
public class LinkToUsagePoint extends TranslatableServerMicroAction {

    private final MeteringService meteringService;

    public LinkToUsagePoint(Thesaurus thesaurus, MeteringService meteringService) {
        super(thesaurus);
        this.meteringService = meteringService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Arrays.asList(
                usagePointPropertySpec(propertySpecService),
                usagePointPropertySpecTemp(propertySpecService));
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.LINK_TO_USAGE_POINT;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        Optional<UsagePoint> usagePoint = getUsagePointValue2(properties);
        if (usagePoint.isPresent()) {
            try {
                device.activate(effectiveTimestamp, usagePoint.get());
            } catch (LocalizedException e) {
                throw new DeviceLifeCycleActionViolationException() {
                    @Override
                    public String getLocalizedMessage() {
                        return e.getLocalizedMessage();
                    }
                };
            }
        }
    }

    private PropertySpec usagePointPropertySpec(PropertySpecService service) {
        return service.referenceSpec(UsagePoint.class)//supported by com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType#USAGEPOINT
                .named(MicroActionTranslationKey.MICRO_ACTION_NAME_LINK_TO_USAGE_POINT)
                .describedAs(MicroActionTranslationKey.MICRO_ACTION_DESCRIPTION_LINK_TO_USAGE_POINT)
                .fromThesaurus(thesaurus)
                .finish();
    }

    private Optional<UsagePoint> getUsagePointValue2(List<ExecutableActionProperty> properties) {
        return properties.stream()
                .filter(executableActionProperty -> executableActionProperty.getPropertySpec()
                        .getName()
                        .equals(MicroActionTranslationKey.MICRO_ACTION_NAME_LINK_TO_USAGE_POINT.getKey()))
                .findFirst()
                .map(ExecutableActionProperty::getValue)
                .map(UsagePoint.class::cast);
    }

    /**
     * TO BE REMOVED START
     **/

    private final static TranslationKey TEMPORAL_KEY = new TranslationKey() {
        @Override
        public String getKey() {
            return "transition.microaction.name.LINK_TO_USAGE_POINT2";
        }

        @Override
        public String getDefaultFormat() {
            return "Link to usage point";
        }
    };

    private PropertySpec usagePointPropertySpecTemp(PropertySpecService service) {
        return service.stringSpec()
                .named(TEMPORAL_KEY)
                .describedAs(MicroActionTranslationKey.MICRO_ACTION_DESCRIPTION_LINK_TO_USAGE_POINT)
                .fromThesaurus(thesaurus)
                .addValues(meteringService.getUsagePoints(new UsagePointFilter()).stream().map(UsagePoint::getMRID).toArray(String[]::new))
                .markExhaustive()
                .finish();
    }

    private Optional<UsagePoint> getUsagePointValue(List<ExecutableActionProperty> properties) {
        return properties.stream()
                .filter(executableActionProperty -> executableActionProperty.getPropertySpec()
                        .getName()
                        .equals(TEMPORAL_KEY.getKey()))
                .findFirst()
                .map(ExecutableActionProperty::getValue)
                .map(String.class::cast)
                .flatMap(meteringService::findUsagePoint);
    }

    /** TO BE REMOVED END **/
}
