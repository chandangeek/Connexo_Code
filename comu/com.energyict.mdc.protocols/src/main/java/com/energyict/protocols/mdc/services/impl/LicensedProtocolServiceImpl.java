package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.ProtocolFamily;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;

import com.energyict.license.FamilyRule;
import com.energyict.license.LicensedProtocolRule;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:52
 */
@Component(name = "com.energyict.mdc.service.licensedprotocols", service = LicensedProtocolService.class, immediate = true)
public class LicensedProtocolServiceImpl implements LicensedProtocolService {

    /**
     * Every protocol, except the ones from the TEST family
     */
    private static final List<LicensedProtocol> ALL_PROTOCOLS;

    static {
        ALL_PROTOCOLS = Arrays.stream(LicensedProtocolRule.values())
                .filter(licensedProtocolRule -> !licensedProtocolRule.getFamilies().contains(FamilyRule.TEST))
                .collect(Collectors.toList());
    }

    @Override
    public List<LicensedProtocol> getAllLicensedProtocols(License license) {
        List<LicensedProtocol> allLicensedProtocols = new ArrayList<>();
        MdcProtocolLicense mdcProtocolLicense = new MdcProtocolLicense(license);
        for (LicensedProtocol licensedProtocolRule : ALL_PROTOCOLS) {
            if (mdcProtocolLicense.hasProtocol(licensedProtocolRule.getClassName())) {
                allLicensedProtocols.add(licensedProtocolRule);
            }
        }
        return allLicensedProtocols;
    }

    @Override
    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        for (LicensedProtocol licensedProtocolRule : ALL_PROTOCOLS) {
            if (licensedProtocolRule.getClassName().equals(deviceProtocolPluggableClass.getJavaClassName())) {
                return licensedProtocolRule;
            }
        }
        return null;
    }

    @Override
    public boolean isValidJavaClassName(String javaClassName, License license) {
        MdcProtocolLicense mdcProtocolLicense = new MdcProtocolLicense(license);
        LicensedProtocol licensedProtocol = mdcProtocolLicense.getLicensedProtocol(javaClassName);
        return licensedProtocol != null && licensedProtocol.getCode() != 0;
    }

    /**
     * Models the behavior of a component that will check if the protocol
     * with a certain class name is covered by this license or not.
     */
    private interface ProtocolCheck {

        /**
         * Checks if the protocol class with the specified name
         * is covered by the license.
         *
         * @param className The name of the protocol class
         * @return A flag that indicates of the protocol class is covered by the license
         */
        boolean isCovered(String className);

    }

    private class MdcProtocolLicense {

        // Key for licensed protocols
        static final String PROTOCOL_FAMILIES = "protocolFamilies";
        static final String PROTOCOLS = "protocols";

        //Key for allowing all protocols
        static final String ALL = "all";

        private final License jupiterLicense;
        private final Properties licenseProperties;
        private final boolean allProtocols;
        private final Set<Integer> protocolFamilies;
        private final Set<Integer> protocols;

        private MdcProtocolLicense(License license) {
            this.jupiterLicense = license;
            this.licenseProperties = this.jupiterLicense.getLicensedValues();
            this.allProtocols = Checks.is(this.licenseProperties.getProperty(PROTOCOLS)).equalTo(ALL);
            this.protocols = initializeProtocols();
            this.protocolFamilies = initProtocolFamilies();
        }

        boolean hasProtocol(String name) {
            ProtocolCheckComposite checker =
                    new ProtocolCheckComposite(
                            new AllProtocolsAreCoveredChecker(),
                            new FamilyChecker(),
                            new ProtocolClassChecker());
            return checker.isCovered(name);
        }

        private Set<Integer> getProtocolFamilies(String className) {
            LicensedProtocol licensedProtocol = getLicensedProtocol(className);
            if (licensedProtocol == null) {
                return new HashSet<>(0);
            } else {
                Set<Integer> families = new HashSet<>();
                for (ProtocolFamily family : licensedProtocol.getFamilies()) {
                    families.add(family.getCode());
                }
                return families;
            }
        }

        private LicensedProtocol getLicensedProtocol(String className) {
            return LicensedProtocolRule.fromClassName(className);
        }

        private Set<Integer> initProtocolFamilies() {
            String protocolFamiliesPropertyValue = this.licenseProperties.getProperty(PROTOCOL_FAMILIES);
            if (protocolFamiliesPropertyValue == null) {
                return new HashSet<>(0);
            } else {
                return this.parseIntegers(protocolFamiliesPropertyValue);
            }
        }

        private Set<Integer> initializeProtocols() {
            String protocolsPropertyValue = this.licenseProperties.getProperty(PROTOCOLS);
            if (protocolsPropertyValue == null) {
                return new HashSet<>(0);
            } else if (ALL.equals(protocolsPropertyValue)) {
                return new HashSet<>(0);
            } else {
                return this.parseIntegers(protocolsPropertyValue);
            }
        }

        private Set<Integer> parseIntegers(String propertyValue) {
            Set<Integer> parsed = new HashSet<>();
            StringTokenizer tokenizer = new StringTokenizer(propertyValue, ",");
            while (tokenizer.hasMoreTokens()) {
                String value = tokenizer.nextToken();
                try {
                    parsed.add(Integer.parseInt(value.trim()));
                } catch (NumberFormatException x) {
                /* Not expected but our IT department must have typed in a non integer
                 * let's NOT warn them about that as it is too late anyway ;-)
                 */
                }
            }
            return parsed;
        }

        private class ProtocolCheckComposite implements ProtocolCheck {

            private List<ProtocolCheck> checkers = new ArrayList<>();

            private ProtocolCheckComposite(ProtocolCheck... checkers) {
                super();
                this.checkers = Arrays.asList(checkers);
            }

            @Override
            public boolean isCovered(String className) {
                for (ProtocolCheck protocolCheck : this.checkers) {
                    if (protocolCheck.isCovered(className)) {
                        return true;
                    }
                }
                return false;
            }
        }

        private class AllProtocolsAreCoveredChecker implements ProtocolCheck {
            @Override
            public boolean isCovered(String className) {
                return allProtocols;
            }
        }

        private class FamilyChecker implements ProtocolCheck {
            @Override
            public boolean isCovered(String className) {
                Set<Integer> protocolFamilies = getProtocolFamilies(className);
                protocolFamilies.retainAll(protocolFamilies);
                return !protocolFamilies.isEmpty();
            }
        }

        private class ProtocolClassChecker implements ProtocolCheck {
            @Override
            public boolean isCovered(String className) {
                LicensedProtocol licensedProtocol = getLicensedProtocol(className);
                return licensedProtocol != null && protocols.contains(licensedProtocol.getCode());
            }
        }

    }
}
