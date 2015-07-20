package com.elster.jupiter.cps;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

/**
 * Tests the equality aspects of the {@link CustomPropertySetValues} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (17:15)
 */
public class CustomPropertySetValuesEqualsTest extends EqualsContractTest {

    private static CustomPropertySetValues instanceA;

    @BeforeClass
    public static void initializeInstanceA() {
        instanceA = CustomPropertySetValues.empty();
        for (String each : keys()) {
            instanceA.setProperty(each, each.length());
        }
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        CustomPropertySetValues instanceEqualToA = CustomPropertySetValues.empty();
        for (String each : keys()) {
            instanceEqualToA.setProperty(each, each.length());
        }
        return instanceEqualToA;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        List<CustomPropertySetValues> instancesNotEqualToA = new ArrayList<>();
        CustomPropertySetValues allCaps = CustomPropertySetValues.empty();
        for (String each : keys()) {
            allCaps.setProperty(each.toUpperCase(), each.length());
        }
        instancesNotEqualToA.add(allCaps);
        CustomPropertySetValues reversed = CustomPropertySetValues.empty();
        for (String each : keys()) {
            reversed.setProperty(new StringBuilder(each).reverse().toString(), each.length());
        }
        instancesNotEqualToA.add(reversed);
        CustomPropertySetValues lessKeys = CustomPropertySetValues.empty();
        lessKeys.setProperty("one", 1);
        lessKeys.setProperty("two", 2);
        instancesNotEqualToA.add(lessKeys);
        CustomPropertySetValues moreKeys = CustomPropertySetValues.empty();
        for (String each : keys()) {
            moreKeys.setProperty(each, each.length());
        }
        moreKeys.setProperty("OneMoreForTheRoad", 17);
        instancesNotEqualToA.add(moreKeys);
        CustomPropertySetValues bigdecimalValues = CustomPropertySetValues.empty();
        for (String each : keys()) {
            bigdecimalValues.setProperty(each.toUpperCase(), BigDecimal.valueOf(each.length()));
        }
        instancesNotEqualToA.add(bigdecimalValues);
        instancesNotEqualToA.add(CustomPropertySetValues.empty());
        return instancesNotEqualToA;
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    private static List<String> keys() {
        return Arrays.asList("azerty", "verhko", "mgfo", "jhpujxlghl", "mgi", "mdghergsdmuj", "fvbopeuyaz");
    }

}