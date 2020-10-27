package com.energyict.protocolimplv2.eict.webcatch.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * ModelApiResponse to return result in JSON format.
 */
@XmlRootElement
public class ModelApiResponse   {

  /**HTTP status code. */
  private Integer code = null;

  /**HTTP status type */
  private String type = null;

  /**Human readeable message from the implementation  */
  private String message = null;

  /**
   * Get code
   * @return code
  **/
  public Integer getCode() {
    return code;
  }

  @XmlElement
  public void setCode(Integer code) {
    this.code = code;
  }

  /**
   * Get type
   * @return type
  **/
  public String getType() {
    return type;
  }

  @XmlElement
  public void setType(String type) {
    this.type = type;
  }


  /**
   * Get message
   * @return message
  **/
  public String getMessage() {
    return message;
  }

  @XmlElement
  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelApiResponse _apiResponse = (ModelApiResponse) o;
    return Objects.equals(this.code, _apiResponse.code) &&
        Objects.equals(this.type, _apiResponse.type) &&
        Objects.equals(this.message, _apiResponse.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, type, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelApiResponse {\n");

    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

