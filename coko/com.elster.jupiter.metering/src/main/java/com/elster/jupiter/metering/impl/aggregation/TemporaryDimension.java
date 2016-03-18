package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.units.Dimension;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by igh on 18/03/2016.
 */
public class TemporaryDimension {

    private int lengthDimension;
    private int massDimension;
    private int timeDimension;
    private int currentDimension;
    private int temperatureDimension;
    private int amountDimension;
    private int luminousIntensityDimension;

    public TemporaryDimension(Dimension dimension) {
        this.lengthDimension = dimension.getLengthDimension();
        this.massDimension = dimension.getMassDimension();
        this.timeDimension = dimension.getTimeDimension();
        this.currentDimension = dimension.getCurrentDimension();
        this.temperatureDimension = dimension.getTemperatureDimension();
        this.amountDimension = dimension.getAmountDimension();
        this.luminousIntensityDimension = dimension.getLuminousIntensityDimension();
    }

    public TemporaryDimension(int lengthD, int massD, int timeD, int currentD, int temperatureD,int amountD,int luminousIntensityD) {
        this.lengthDimension = lengthD;
        this.massDimension = massD;
        this.timeDimension = timeD;
        this.currentDimension = currentD;
        this.temperatureDimension = temperatureD;
        this.amountDimension = amountD;
        this.luminousIntensityDimension = luminousIntensityD;
    }

    public static TemporaryDimension of (Dimension dimension) {
        return new TemporaryDimension(dimension);
    }

    TemporaryDimension multiply(Dimension second) {
        return this.add(of(second));
    }

    TemporaryDimension divide(Dimension second) {
        return this.substract(of(second));
    }

    TemporaryDimension add(TemporaryDimension second) {
        return new TemporaryDimension(
                this.lengthDimension + second.getLengthDimension(),
                this.massDimension + second.getMassDimension(),
                this.timeDimension + second.getTimeDimension(),
                this.currentDimension + second.getCurrentDimension(),
                this.temperatureDimension + second.getTemperatureDimension(),
                this.amountDimension + second.getAmountDimension(),
                this.luminousIntensityDimension + second.getLuminousIntensityDimension());
    }

    TemporaryDimension substract(TemporaryDimension second) {
        return new TemporaryDimension(
                this.lengthDimension - second.getLengthDimension(),
                this.massDimension - second.getMassDimension(),
                this.timeDimension - second.getTimeDimension(),
                this.currentDimension - second.getCurrentDimension(),
                this.temperatureDimension - second.getTemperatureDimension(),
                this.amountDimension - second.getAmountDimension(),
                this.luminousIntensityDimension - second.getLuminousIntensityDimension());
    }

    public int getLengthDimension() {
        return lengthDimension;
    }

    public int getMassDimension() {
        return massDimension;
    }

    public int getTimeDimension() {
        return timeDimension;
    }

    public int getCurrentDimension() {
        return currentDimension;
    }

    public int getTemperatureDimension() {
        return temperatureDimension;
    }

    public int getAmountDimension() {
        return amountDimension;
    }

    public int getLuminousIntensityDimension() {
        return luminousIntensityDimension;
    }

    public Optional<Dimension> getDimension() {
        Dimension[] values = Dimension.values();
        return Stream.of(values)
                .filter(dim -> dim.getLengthDimension() == this.getLengthDimension())
                .filter(dim -> dim.getMassDimension() == this.getMassDimension())
                .filter(dim -> dim.getTimeDimension() == this.getTimeDimension())
                .filter(dim -> dim.getCurrentDimension() == this.getCurrentDimension())
                .filter(dim -> dim.getTemperatureDimension() == this.getTemperatureDimension())
                .filter(dim -> dim.getAmountDimension() == this.getAmountDimension())
                .filter(dim -> dim.getLuminousIntensityDimension() == this.getLuminousIntensityDimension())
                .findAny();
    }

    public boolean exists() {
        return getDimension().isPresent();
    }

    public boolean hasSameDimensions(TemporaryDimension other) {
        return
                lengthDimension == other.lengthDimension && massDimension ==  other.massDimension &&
                        timeDimension == other.timeDimension && currentDimension == other.currentDimension &&
                        temperatureDimension == other.temperatureDimension && amountDimension == other.amountDimension &&
                        luminousIntensityDimension == other.luminousIntensityDimension;
    }

    public boolean isDimensionless() {
        return hasSameDimensions(new TemporaryDimension(0, 0, 0, 0, 0, 0, 0));
    }

    public boolean isCurrency() {
        return hasSameDimensions(new TemporaryDimension(0, 0, 0, 0, 0, 0, 0));
    }
}
