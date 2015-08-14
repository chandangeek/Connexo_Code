package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * A {@link PersistentDomainExtension} for the {@link TestDomain}
 * that will be used in the test classes of this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (10:23)
 */
public class DomainExtensionForTestingPurposes implements PersistentDomainExtension<TestDomain> {

    public enum FieldNames {
        DOMAIN("testDomain", "testDomain"),
        BILLING_CYCLE("billingCycle", "bill_cycle"),
        CONTRACT_NUMBER("contractNumber", "contract_nr");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<TestDomain> testDomain = Reference.empty();
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "CannotBeNull")
    private BigDecimal billingCycle;
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "FieldTooLong")
    private String contractNumber;

    // For persistence framework
    public DomainExtensionForTestingPurposes() {
        super();
    }

    // For testing purposes
    public DomainExtensionForTestingPurposes(TestDomain testDomain) {
        this();
        this.testDomain.set(testDomain);
    }

    // For testing purposes
    public DomainExtensionForTestingPurposes(TestDomain testDomain, RegisteredCustomPropertySet registeredCustomPropertySet) {
        this(testDomain);
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public BigDecimal getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BigDecimal billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    @Override
    public void copyFrom(TestDomain domainInstance, CustomPropertySetValues propertyValues) {
        this.testDomain.set(domainInstance);
        this.setBillingCycle((BigDecimal) propertyValues.getProperty(FieldNames.BILLING_CYCLE.javaName()));
        this.setContractNumber((String) propertyValues.getProperty(FieldNames.CONTRACT_NUMBER.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(FieldNames.BILLING_CYCLE.javaName(), this.getBillingCycle());
        propertySetValues.setProperty(FieldNames.CONTRACT_NUMBER.javaName(), this.getContractNumber());
    }

}