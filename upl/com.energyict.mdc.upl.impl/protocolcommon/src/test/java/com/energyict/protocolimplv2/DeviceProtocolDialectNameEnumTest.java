package com.energyict.protocolimplv2;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.FieldLengthValidator;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Tests the {@link DeviceProtocolDialectNameEnum} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:06
 */
public class DeviceProtocolDialectNameEnumTest {

    /**
     * This test will purely test if all {@link DeviceProtocolDialectNameEnum} values have
     * a unique name field
     */
    @Test
    public void uniquenessTest() {
        int counter = 0;
        List<String> duplicates = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (DeviceProtocolDialectNameEnum deviceProtocolDialectNameEnum : DeviceProtocolDialectNameEnum.values()) {
            set.add(deviceProtocolDialectNameEnum.getName());
            counter++;
            if ((set.size() + duplicates.size()) != counter) {
                duplicates.add(deviceProtocolDialectNameEnum.getName());
            }
        }
        if (duplicates.size() > 0) {
            String newLine = "\r\n";
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(newLine);
            stringBuilder.append("**************************************************************");
            stringBuilder.append(newLine);
            stringBuilder.append("The DeviceProtocolDialectNameEnum contains duplicate entries: ");
            for (String duplicate : duplicates) {
                stringBuilder.append(newLine);
                stringBuilder.append(" - ").append(duplicate);
            }
            stringBuilder.append(newLine);
            stringBuilder.append("This should not happen as these names are used as RelationTypeNames!!");
            stringBuilder.append(newLine);
            stringBuilder.append("**************************************************************");
            fail(stringBuilder.toString());
        }
    }

    /**
     * This test will check if the names of the dialects match the length conventions for
     * relationTypeNames, eg. max 24 chars
     */
    @Test
    public void lengthTest() throws BusinessException {
        int maxLength = 24;
        for (DeviceProtocolDialectNameEnum deviceProtocolDialectNameEnum : DeviceProtocolDialectNameEnum.values()) {
            try {
                FieldLengthValidator.validateMaxLength(deviceProtocolDialectNameEnum.getName(), "name", maxLength, false);
            } catch (BusinessException e) {
                fail(deviceProtocolDialectNameEnum.getName() + " is to long, only " + maxLength + " chars are allowed.");
            }
        }
    }

    @Test
    public void validCharactersTest() {
        for (DeviceProtocolDialectNameEnum deviceProtocolDialectNameEnum : DeviceProtocolDialectNameEnum.values()) {
            for (int i = 0; i < deviceProtocolDialectNameEnum.getName().length(); i++) {
                if (getValidCharacters().indexOf(deviceProtocolDialectNameEnum.getName().charAt(i)) == -1) {
                    fail(deviceProtocolDialectNameEnum.getName() + " contains invalid characters. " +
                            "Only the following are allowed : " + getValidCharacters());
                }
            }
        }
    }

    protected String getValidCharacters() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
    }

}
