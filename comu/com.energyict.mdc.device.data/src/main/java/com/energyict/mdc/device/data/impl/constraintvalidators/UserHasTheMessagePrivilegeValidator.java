package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.DeviceMessageImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 11/4/14
 * Time: 2:48 PM
 */
public class UserHasTheMessagePrivilegeValidator implements ConstraintValidator<UserHasTheMessagePrivilege, DeviceMessageImpl> {

    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public UserHasTheMessagePrivilegeValidator(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public void initialize(UserHasTheMessagePrivilege userHasTheMessagePrivilege) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        if (threadPrincipalService != null) {
            User currentUser = (User) threadPrincipalService.getPrincipal();
            if (currentUser != null) {
                Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = deviceMessage.getDevice().getDeviceConfiguration().getDeviceMessageEnablements().stream().filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessage.getDeviceMessageId())).findFirst();
                if (deviceMessageEnablementOptional.isPresent()) {
                    DeviceMessageEnablement deviceMessageEnablement = deviceMessageEnablementOptional.get();
                    if (!deviceMessageEnablement.getUserActions().stream().anyMatch(deviceMessageUserAction -> currentUser.hasPrivilege("MDC", deviceMessageUserAction.getPrivilege()))) {
                        context.disableDefaultConstraintViolation();
                        context.
                                buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_USER_NOT_ALLOWED + "}").
                                addPropertyNode(DeviceMessageImpl.Fields.USER.fieldName()).
                                addPropertyNode("device").
                                addConstraintViolation();
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
