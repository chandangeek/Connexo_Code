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
public class WebcatchDevice {

  /**serial number of the device. */
  private String serial;

  /**List of channels that the device supports */
  private List<WebcatchChannel> values;

  public WebcatchDevice(){

  }

  /**
   * Serial number of the slave device
   * @return serial
  **/
  public String getSerial() {
    return serial;
  }

  @XmlElement
  public void setSerial(String serial) {
    this.serial = serial;
  }

  @XmlElementWrapper(name="values")
  @XmlElement(name = "channel")
  public WebcatchDevice addValuesItem(WebcatchChannel valuesItem) {
    if (this.values == null) {
      this.values = new ArrayList<WebcatchChannel>();
    }
    this.values.add(valuesItem);
    return this;
  }

  /**
   * Interval values
   * @return channelValues
  **/
  public List<WebcatchChannel> getValues() {
    return values;
  }

  @XmlElementWrapper(name="values")
  @XmlElement(name = "channel")
  public void setValues(List<WebcatchChannel> values) {
    this.values = values;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebcatchDevice webcatchDevice = (WebcatchDevice) o;
    return Objects.equals(this.serial, webcatchDevice.serial) &&
        Objects.equals(this.values, webcatchDevice.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serial, values);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebcatchDevice {\n");

    sb.append("    serial: ").append(toIndentedString(serial)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
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

