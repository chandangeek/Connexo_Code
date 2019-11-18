/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderXDlmsApdu.java $
 * Version:     
 * $Id: CoderXDlmsApdu.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 07:48:56
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.coding.AbstractCoder;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.action.CosemActionRequest;
import com.elster.dlms.cosem.application.services.action.CosemActionResponse;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceType;
import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.application.services.get.CosemGetRequest;
import com.elster.dlms.cosem.application.services.get.CosemGetResponse;
import com.elster.dlms.cosem.application.services.open.ConfirmedServiceError;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import com.elster.dlms.cosem.application.services.set.CosemSetRequest;
import com.elster.dlms.cosem.application.services.set.CosemSetResponse;
import com.elster.dlms.security.IDlmsSecurityProvider;
import com.elster.dlms.security.IDlmsSecurityProvider.DecodingResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class dispatches the xDLMS APDUs to their en-/decoders.
 *
 * @author osse
 */
public class CoderXDlmsApdu extends AbstractCoder<AbstractCosemServiceInvocation>
{
  private static final Logger LOGGER = Logger.getLogger(CoderXDlmsApdu.class.getName());
  //--- IDs ---
  public static final int ID_INITIATE_REQUEST = 0x01;
  public static final int ID_INITIATE_RESPONSE = 0x08;
  public static final int ID_CONFIRMED_SERVICE_ERROR = 0x0E;
  public static final int ID_GLO_INITIATE_REQUEST = 0x21;
  public static final int ID_GLO_INITIATE_RESPONSE = 0x28;
  public static final int ID_GLO_CONFIRMED_SERVICE_ERROR = 0x2E;
  public static final int ID_GET_REQUEST = 0xC0;
  public static final int ID_SET_REQUEST = 0xC1;
  public static final int ID_EVENT_NOTIFICATION_REQUEST = 0xC2;
  public static final int ID_ACTION_REQUEST = 0xC3;
  public static final int ID_GET_RESPONSE = 0xC4;
  public static final int ID_SET_RESPONSE = 0xC5;
  public static final int ID_ACTION_RESPONSE = 0xC7;
  public static final int ID_GLO_GET_REQUEST = 0xC8;
  public static final int ID_GLO_SET_REQUEST = 0xC9;
  public static final int ID_GLO_EVENT_NOTIFICATION_REQUEST = 0xCA;
  public static final int ID_GLO_ACTION_REQUEST = 0xCB;
  public static final int ID_GLO_GET_RESPONSE = 0xCC;
  public static final int ID_GLO_SET_RESPONSE = 0xCD;
  public static final int ID_GLO_ACTION_RESPONSE = 0xCF;
  public static final int ID_DED_GET_REQUEST = 0xD0;
  public static final int ID_DED_SET_REQUEST = 0xD1;
  public static final int ID_DED_EVENT_NOTIFICATION_REQUEST = 0xD2;
  public static final int ID_DED_ACTION_REQUEST = 0xD3;
  public static final int ID_DED_GET_RESPONSE = 0xD4;
  public static final int ID_DED_SET_RESPONSE = 0xD5;
  public static final int ID_DED_ACTION_RESPONSE = 0xD7;
  public static final int ID_EXCEPTION_RESPONSE = 0xD8;
  private final List<TagInfo> infos = new ArrayList<TagInfo>();
  private final Map<Class<?>, TagInfo> classMap = new HashMap<Class<?>, TagInfo>();
  private final Map<Integer, TagInfo> tagMap = new HashMap<Integer, TagInfo>();
  private final Map<Integer, TagInfo> tagDedMap = new HashMap<Integer, TagInfo>();
  private final Map<Integer, TagInfo> tagGloMap = new HashMap<Integer, TagInfo>();
  private IDlmsSecurityProvider securityProvider;

  public CoderXDlmsApdu()
  {
    super();

    infos.add(new TagInfo(ID_INITIATE_REQUEST, ID_GLO_INITIATE_REQUEST, -1,
                                                ServiceType.INITIATE, ServiceInvocationType.REQUEST,
                                                ProposedXDlmsContext.class, new CoderInitiateRequest(false)));
    infos.add(new TagInfo(ID_INITIATE_RESPONSE, ID_GLO_INITIATE_RESPONSE, -1,
                                                  ServiceType.INITIATE, ServiceInvocationType.RESPONSE,
                                                  NegotiatedXDlmsContext.class, new CoderInitiateResponse(
                                                  false)));
    infos.add(new TagInfo(ID_CONFIRMED_SERVICE_ERROR, ID_GLO_CONFIRMED_SERVICE_ERROR,
                                                 -1,
                                                 ServiceType.SERVICE_ERROR, ServiceInvocationType.RESPONSE,
                                                 ConfirmedServiceError.class, new CoderConfirmedServiceError(
            false)));
    infos.add(new TagInfo(ID_GET_REQUEST, ID_GLO_GET_REQUEST, ID_DED_GET_REQUEST,
                                           ServiceType.GET, ServiceInvocationType.REQUEST,
                                           CosemGetRequest.class, new CoderGetRequest()));
    infos.add(new TagInfo(ID_GET_RESPONSE, ID_GLO_GET_RESPONSE, ID_DED_GET_RESPONSE,
                                            ServiceType.GET, ServiceInvocationType.RESPONSE,
                                            CosemGetResponse.class, new CoderGetResponse()));
    infos.add(new TagInfo(ID_SET_REQUEST, ID_GLO_SET_REQUEST, ID_DED_SET_REQUEST,
                                           ServiceType.SET, ServiceInvocationType.REQUEST,
                                           CosemSetRequest.class, new CoderSetRequest()));
    infos.add(new TagInfo(ID_SET_RESPONSE, ID_GLO_SET_RESPONSE, ID_DED_SET_RESPONSE,
                                            ServiceType.SET, ServiceInvocationType.RESPONSE,
                                            CosemSetResponse.class, new CoderSetResponse()));
    infos.add(new TagInfo(ID_ACTION_REQUEST, ID_GLO_ACTION_REQUEST, ID_DED_ACTION_REQUEST,
                                              ServiceType.ACTION, ServiceInvocationType.REQUEST,
                                              CosemActionRequest.class, new CoderActionRequest()));
    infos.add(new TagInfo(ID_ACTION_RESPONSE, ID_GLO_ACTION_RESPONSE,
                                               ID_DED_ACTION_RESPONSE,
                                               ServiceType.ACTION, ServiceInvocationType.RESPONSE,
                                               CosemActionResponse.class,
                                               new CoderActionResponse()));


    // Build maps.
    for (TagInfo info : infos)
    {
      classMap.put(info.getServiceObjectClass(), info);

      if (info.getId() >= 0)
      {
        tagMap.put(info.getId(), info);
      }
      if (info.getGloId() >= 0)
      {
        tagGloMap.put(info.getGloId(), info);
      }
      if (info.getDedId() >= 0)
      {
        tagDedMap.put(info.getDedId(), info);
      }
    }
  }

  private TagInfo getInfoForClass(Class<?>clazz)
  {

    TagInfo result = null;

    while (result == null && clazz != null)
    {
      result = classMap.get(clazz);


      if (result == null)
      {
        clazz = clazz.getSuperclass();
      }
    }

    return result;
  }

  @Override
  public void encodeObject(final AbstractCosemServiceInvocation object, final OutputStream out) throws
          IOException
  {
    final TagInfo info = getInfoForClass(object.getClass());

    if (info == null)
    {
      throw new IllegalArgumentException("COSEM service class unknown. Class: " + object.getClass().getName());
    }

    final SecurityControlField securityControlField = object.getSecurityControlField();

    if (securityControlField == null || (!securityControlField.isAuthenticated() && !securityControlField.
            isEncrypted()))
    {
      //No security
      out.write(info.getId());
      encodeCheckedObject(info.getCoder(),object, out);   //the correct type of object is ensured by the "TagInfo"

    }
    else
    {
      final AXdrOutputStream axdrOut = new AXdrOutputStream(out);

      if (securityProvider == null)
      {
        throw new IllegalStateException("No security provider set");
      }
      final byte[] ciphertext = securityProvider.encode(encodeCheckedObjectToBytes(info.getCoder(),info.getId(),object), // the correct type of object is ensured by the "TagInfo"
                                                        securityControlField);

      if (SecurityControlField.CipheringMethod.DEDICATED_UNICAST == securityControlField.getCipheringMethod())
      {
        axdrOut.writeTag(info.getDedId());
      }
      else
      {
        axdrOut.writeTag(info.getGloId());
      }
      axdrOut.writeOctetStringVariableLength(ciphertext);
    }
  }
  
  
  /**
   * Encodes the object. <P>
   * It must be ensured that the type of the object matches the generic type in {@link AbstractAXdrCoder#encodeObject(java.lang.Object, java.io.OutputStream)}
   */
  @SuppressWarnings("unchecked")
  private <T extends AbstractCosemServiceInvocation> void encodeCheckedObject(final AbstractAXdrCoder<T> coder, final Object object, final OutputStream out) throws IOException
  {
    coder.encodeObject((T) object, out);
  }
  /**
   * Encodes the object to bytes. <P>
   * It must be ensured that the type of the object matches the generic type in {@link AbstractAXdrCoder#encodeObjectToBytes(int, java.lang.Object) )}
   */
  @SuppressWarnings("unchecked")
  private <T extends AbstractCosemServiceInvocation>  byte[] encodeCheckedObjectToBytes(AbstractAXdrCoder<T> coder,final int prefix, final Object object) throws IOException
  {
    return coder.encodeObjectToBytes(prefix,(T) object);
  }

  @Override
  public AbstractCosemServiceInvocation decodeObject(final InputStream in) throws IOException
  {
    final int tag = in.read();

    if (tag == -1)
    {
      throw new EOFException();
    }
    AbstractCosemServiceInvocation result = null;


    //--- without ciphering ---

    TagInfo info = tagMap.get(tag);
    if (info != null)
    {
      result = info.decodeObject(in);
    }


    //--- with global ciphering ---
    if (result == null)
    {
      info = tagGloMap.get(tag);
      if (info != null)
      {
        result = decodeCipherdObject(in, info, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);
      }
    }

    //--- with dedicated ciphering ---
    if (result == null)
    {
      info = tagDedMap.get(tag);
      if (info != null)
      {
        result = decodeCipherdObject(in, info, SecurityControlField.CipheringMethod.DEDICATED_UNICAST);
      }
    }

    if (result != null && in.available() > 0)
    {
      LOGGER.log(Level.INFO, "APDU not completely decoded. {0} Bytes left. Decoded result: {1}", new Object[]
              {
                in.available(), result
              });
      throw new IOException("APDU not completely decoded. " + in.available() + " Bytes left.");
    }

    if (result == null)
    {
      throw new IOException("Unknown APDU tag " + tag);
    }

    return result;

  }

  public boolean isDedicatedTag(final int tag)
  {
    return tagDedMap.containsKey(tag);
  }

  public boolean isGlobalTag(final int tag)
  {
    return tagGloMap.containsKey(tag);
  }

  public ServiceInvocationType getServiceInvocationType(final int tag)
  {
    TagInfo info = tagMap.get(tag);

    if (info == null)
    {
      info = tagDedMap.get(tag);
    }

    if (info == null)
    {
      info = tagGloMap.get(tag);
    }

    return info == null ? null : info.getSeviceInvocationType();
  }

  private AbstractCosemServiceInvocation decodeCipherdObject(InputStream in, TagInfo info,
                                                             SecurityControlField.CipheringMethod cipheringMethod)
          throws
          IOException
  {
    if (securityProvider == null)
    {
      throw new IOException("No security provider set");
    }

    final AXdrInputStream axdrIn = new AXdrInputStream(in);
    final byte[] cipherText = axdrIn.readOctetString();
    logCipherText(cipherText);

    final DecodingResult decodingResult =
            securityProvider.decode(cipherText, cipheringMethod);
    final byte[] plainText = decodingResult.getData();
    logPlainText(plainText);

    if (plainText.length == 0 || (info.getId() != (0xFF & plainText[0]))) //The tag of the plain text must be the "normal" tag
    {
      LOGGER.log(Level.INFO, "Cipher: wrong decoded tag. Plain text: {0}", CodingUtils.byteArrayToString(
              plainText));
      throw new IOException("Cipher: wrong decoded tag");
    }

    final AbstractCosemServiceInvocation object = decodeObject(new ByteArrayInputStream(plainText));
    object.setSecurityControlField(decodingResult.getSecurityControlField());
    return object;
  }

  private void logPlainText(final byte[] plainText)
  {
    if (LOGGER.isLoggable(Level.FINER))
    {
      LOGGER.log(Level.FINER, "Plain text: {0}", CodingUtils.byteArrayToString(plainText));
    }

  }

  private void logCipherText(final byte[] cipherText)
  {
    if (LOGGER.isLoggable(Level.FINEST))
    {
      LOGGER.log(Level.FINEST, "Cipher text: {0}", CodingUtils.byteArrayToString(cipherText));
    }
  }

  public void setSecurityProvider(IDlmsSecurityProvider securityProvider)
  {
    this.securityProvider = securityProvider;
  }

  private static class TagInfo 
  {
    private final int id;
    private final int gloId;
    private final int dedId;
    private final AbstractCosemServiceInvocation.ServiceType seviceType;
    private final AbstractCosemServiceInvocation.ServiceInvocationType seviceInvocationType;
    private final Class<? extends AbstractCosemServiceInvocation> serviceObjectClass;
    private final AbstractAXdrCoder<? extends AbstractCosemServiceInvocation> coder;

    public <T extends AbstractCosemServiceInvocation> TagInfo(final int id, final int gloId, final int dedId, final ServiceType seviceType,
                   final ServiceInvocationType serviceInvocationType, final Class<T> serviceObjectClass,
                   final AbstractAXdrCoder<T> coder)
    {
      this.id = id;
      this.gloId = gloId;
      this.dedId = dedId;
      this.seviceType = seviceType;
      this.seviceInvocationType = serviceInvocationType;
      this.serviceObjectClass = serviceObjectClass;
      this.coder = coder;
    }

    public AbstractAXdrCoder<? extends AbstractCosemServiceInvocation> getCoder()
    {
      return coder;
    }

    public AbstractCosemServiceInvocation decodeObject(final InputStream in) throws IOException
    {
      return coder.decodeObject(in);
    }

    public int getDedId()
    {
      return dedId;
    }

    public int getGloId()
    {
      return gloId;
    }

    public int getId()
    {
      return id;
    }

    public ServiceType getSeviceType()
    {
      return seviceType;
    }

    public ServiceInvocationType getSeviceInvocationType()
    {
      return seviceInvocationType;
    }

    public Class<?> getServiceObjectClass()
    {
      return serviceObjectClass;
    }

  }

}
