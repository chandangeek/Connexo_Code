package com.energyict.mdc.channels.serial;

import com.energyict.cbo.TimeDuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * Groups relevant serial port configuration options
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/08/12
 * Time: 13:23
 */
@XmlRootElement
public class SerialPortConfiguration {

    public static final String BAUDRATE_NAME = "serialconfig_baudrate";
    public static final String NR_OF_DATA_BITS_NAME = "serialconfig_numberofdatabits";
    public static final String NR_OF_STOP_BITS_NAME = "serialconfig_numberofstopbits";
    public static final String PARITY_NAME = "serialconfig_parity";
    public static final String FLOW_CONTROL_NAME = "serialconfig_flowcontrol";

    public static final TimeDuration DEFAULT_SERIAL_PORT_OPEN_TIMEOUT = new TimeDuration(2);
    public static final TimeDuration DEFAULT_SERIAL_PORT_READ_TIMEOUT = new TimeDuration(10);

    private String comPortName;
    private BaudrateValue baudrate;
    private NrOfDataBits nrOfDataBits;
    private NrOfStopBits nrOfStopBits;
    private FlowControl flowControl;
    private Parities parity;

    private BigDecimal serialPortOpenTimeOut = new BigDecimal(DEFAULT_SERIAL_PORT_OPEN_TIMEOUT.getMilliSeconds());
    private BigDecimal serialPortReadTimeOut = new BigDecimal(DEFAULT_SERIAL_PORT_READ_TIMEOUT.getMilliSeconds());

    // For XML serialization only
    private SerialPortConfiguration() {
    }

    public SerialPortConfiguration(String comPortName, BaudrateValue baudrate, NrOfDataBits nrOfDataBits, NrOfStopBits nrOfStopBits, Parities parity, FlowControl flowControl) {
        this.comPortName = comPortName;
        this.baudrate = baudrate;
        this.nrOfDataBits = nrOfDataBits;
        this.nrOfStopBits = nrOfStopBits;
        this.flowControl = flowControl;
        this.parity = parity;
    }
    @XmlAttribute
    public String getComPortName() {
        return comPortName;
    }

    public void setComPortName(String comPortName) {
        this.comPortName = comPortName;
    }
    @XmlAttribute
    public BaudrateValue getBaudrate() {
        return baudrate;
    }
    @XmlAttribute
    public FlowControl getFlowControl() {
        return flowControl;
    }
    @XmlAttribute
    public NrOfDataBits getNrOfDataBits() {
        return nrOfDataBits;
    }
    @XmlAttribute
    public NrOfStopBits getNrOfStopBits() {
        return nrOfStopBits;
    }
    @XmlAttribute
    public Parities getParity() {
        return parity;
    }
    @XmlAttribute
    public BigDecimal getSerialPortOpenTimeOut() {
        return serialPortOpenTimeOut;
    }

    public void setSerialPortOpenTimeOut(BigDecimal serialPortOpenTimeOut) {
        this.serialPortOpenTimeOut = serialPortOpenTimeOut;
    }

    @XmlAttribute
    public BigDecimal getSerialPortReadTimeOut() {
        return serialPortReadTimeOut;
    }

    public void setSerialPortReadTimeOut(BigDecimal serialPortReadTimeOut) {
        this.serialPortReadTimeOut = serialPortReadTimeOut;
    }

    public void setBaudrate(BaudrateValue baudrate) {
        this.baudrate = baudrate;
    }

    public void setFlowControl(FlowControl flowControl) {
        this.flowControl = flowControl;
    }

    public void setNrOfDataBits(NrOfDataBits nrOfDataBits) {
        this.nrOfDataBits = nrOfDataBits;
    }

    public void setNrOfStopBits(NrOfStopBits nrOfStopBits) {
        this.nrOfStopBits = nrOfStopBits;
    }

    public void setParity(Parities parity) {
        this.parity = parity;
    }
}
