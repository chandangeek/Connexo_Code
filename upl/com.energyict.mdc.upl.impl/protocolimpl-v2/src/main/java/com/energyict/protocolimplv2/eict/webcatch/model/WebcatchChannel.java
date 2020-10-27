package com.energyict.protocolimplv2.eict.webcatch.model;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Maps the given JSON data to a object notation.
 */
@XmlRootElement
public class WebcatchChannel   {

  /**Value of the channel */
  private BigDecimal value;

  public WebcatchChannel(String value){
    this.value=new BigDecimal(value);
  }

  /**
   * Actual value of the channel
   * @return value
  **/
  public BigDecimal getValue() {
    return value;
  }

  @XmlElement
  public void setValue(String value) {
    this.value = new BigDecimal(value);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebcatchChannel channel = (WebcatchChannel) o;
    return Objects.equals(this.value, channel.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Channel {\n");

    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

