package com.energyict.protocolimplv2.eict.webcatch.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maps the given JSON data to a object notation.
 */
@XmlRootElement
public class CatchallObject   {

  /**Version of the JSON implementation. */
  private String version;

  /**Serial number of the concentrator. */
  private String serial;

  /**Version of the firmware. */
  private String firmware;

  /**The name of the application.*/
  private String application;

  /**Configuration of the catchall. */
  private String configuration;

  /**Flow version of the catchall.  */
  private String flow;

  /** IP of the catchall. */
  private String ip;

  /**MAC address of the catchall */
  private String mac;

  /**UNIX timestamp of all data. */
  private String utcstamp;

  /**Devices connected to the concentrator. */
  private List<WebcatchDevice> webcatchDevices = new ArrayList<WebcatchDevice>();

  /**Default no argument constructor. */
  public CatchallObject(){

  }

  /**
   * Verison of the JSON format
   * @return version
  **/
  public String getVersion() {
    return version;
  }
  @XmlElement
  public void setVersion(String version) {
    this.version = version;
  }


  /**
   * Serial number of the catchall
   * @return serial
  **/
  public String getSerial() {
    return serial;
  }

  @XmlElement
  public void setSerial(String serial) {
    this.serial = serial;
  }

  /**
   * Firmware version of the catchall
   * @return firmware
  **/
  public String getFirmware() {
    return firmware;
  }

  @XmlElement
  public void setFirmware(String firmware) {
    this.firmware = firmware;
  }

  /**
   * Application version of the catchall
   * @return application
  **/



  public String getApplication() {
    return application;
  }

  @XmlElement
  public void setApplication(String application) {
    this.application = application;
  }

  /**
   * Configuration of the catchall
   * @return configuration
  **/

  public String getConfiguration() {
    return configuration;
  }

  @XmlElement
  public void setConfiguration(String _configuration) {
    this.configuration = _configuration;
  }

  /**
   * Flow version of the catchall
   * @return flow
  **/
  public String getFlow() {
    return flow;
  }

  @XmlElement
  public void setFlow(String flow) {
    this.flow = flow;
  }


  /**
   * IP of the catchall
   * @return ip
  **/

  public String getIp() {
    return ip;
  }

  @XmlElement
  public void setIp(String ip) {
    this.ip = ip;
  }


  /**
   * MAC address of the catchall
   * @return mac
  **/

  public String getMac() {
    return mac;
  }

  @XmlElement
  public void setMac(String mac) {
    this.mac = mac;
  }

  /**
   * UNIX timestamp of all data
   * @return utcstamp
  **/

  public String getUtcstamp() {
    return utcstamp;
  }

  @XmlElement
  public void setUtcstamp(String utcstamp) {
    this.utcstamp = utcstamp;
  }


//  @XmlElement(name = "webcatchDevices")
//  public CatchallObject addDevicesItem(WebcatchDevice devicesItem) {
//    this.webcatchDevices.add(devicesItem);
//    return this;
//  }

  /**
   * Array containing all slave device objects
   * @return webcatchDevices
  **/

  public List<WebcatchDevice> getDevices() {
    return webcatchDevices;
  }

  @XmlElementWrapper(name="webcatchDevices")
  @XmlElement(name="device")
  public void setDevices(List<WebcatchDevice> webcatchDevices) {
    this.webcatchDevices = webcatchDevices;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CatchallObject catchallObject = (CatchallObject) o;
    return Objects.equals(this.version, catchallObject.version) &&
        Objects.equals(this.serial, catchallObject.serial) &&
        Objects.equals(this.firmware, catchallObject.firmware) &&
        Objects.equals(this.application, catchallObject.application) &&
        Objects.equals(this.configuration, catchallObject.configuration) &&
        Objects.equals(this.flow, catchallObject.flow) &&
        Objects.equals(this.ip, catchallObject.ip) &&
        Objects.equals(this.mac, catchallObject.mac) &&
        Objects.equals(this.utcstamp, catchallObject.utcstamp) &&
        Objects.equals(this.webcatchDevices, catchallObject.webcatchDevices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, serial, firmware, application, configuration, flow, ip, mac, utcstamp, webcatchDevices);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CatchallObject {\n");

    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    serial: ").append(toIndentedString(serial)).append("\n");
    sb.append("    firmware: ").append(toIndentedString(firmware)).append("\n");
    sb.append("    application: ").append(toIndentedString(application)).append("\n");
    sb.append("    configuration: ").append(toIndentedString(configuration)).append("\n");
    sb.append("    flow: ").append(toIndentedString(flow)).append("\n");
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    mac: ").append(toIndentedString(mac)).append("\n");
    sb.append("    utcstamp: ").append(toIndentedString(utcstamp)).append("\n");
    sb.append("    webcatchDevices: ").append(toIndentedString(webcatchDevices)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

