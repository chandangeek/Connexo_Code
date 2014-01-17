package com.energyict.mdc.common;

/**
 * Shadow for Phenomenon.
 */
public class PhenomenonShadow extends NamedObjectShadow {

    private Unit unit = Unit.getUndefined();
    private String description;
    private String measurementCode;
    private String ediCode;
    private int integratedPhenomenonId;

    /**
     * Provides a shadow for Phenomenon objects.
     */
    public PhenomenonShadow() {
    }

    /**
     * Creates a new shadow, initialized with the argument.
     *
     * @param phenomenon the Phenomenon.
     */
    public PhenomenonShadow(Phenomenon phenomenon) {
        super(phenomenon.getId(), phenomenon.getName());
        unit = phenomenon.getUnit();
        description = phenomenon.getDescription();
        measurementCode = phenomenon.getMeasurementCode();
        ediCode = phenomenon.getEdiCode();
//        integratedPhenomenonId = phenomenon.getIntegratedPhenomenonId();
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public java.lang.String getDescription() {
        return description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
        markDirty();
    }

    /**
     * Getter for property unit.
     *
     * @return Value of property unit.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Setter for property unit.
     *
     * @param unit New value of property unit.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
        markDirty();
    }

    /**
     * Getter for property measurementCode.
     *
     * @return Value of property measurementCode.
     */
    public java.lang.String getMeasurementCode() {
        return measurementCode;
    }

    /**
     * Setter for property measurementCode.
     *
     * @param measurementCode New value of property measurementCode.
     */
    public void setMeasurementCode(java.lang.String measurementCode) {
        this.measurementCode = measurementCode;
        markDirty();
    }

    /**
     * Getter for property ediCode.
     *
     * @return Value of property ediCode.
     */
    public java.lang.String getEdiCode() {
        return ediCode;
    }

    /**
     * Setter for property ediCode.
     *
     * @param ediCode New value of property ediCode.
     */
    public void setEdiCode(java.lang.String ediCode) {
        this.ediCode = ediCode;
        markDirty();
    }

    /**
     * Getter for property integratedPhenomenonId.
     *
     * @return Value of property integratedPhenomenonId.
     * @deprecated no longer used
     */
    public int getIntegratedPhenomenonId() {
        return integratedPhenomenonId;
    }

    /**
     * Setter for property integratedPhenomenonId.
     *
     * @param integratedPhenomenonId New value of property integratedPhenomenonId.
     * @deprecated no longer used
     */
    public void setIntegratedPhenomenonId(int integratedPhenomenonId) {
        this.integratedPhenomenonId = integratedPhenomenonId;
        markDirty();
    }

}
