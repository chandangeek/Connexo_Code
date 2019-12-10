/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemApplicationLayer.java $
 * Version:     
 * $Id: CosemApplicationLayer.java 6401 2013-04-03 14:52:46Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  11.05.2010 14:25:21
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.axdr.coding.AXdrCoderSequenceOfWrapper;
import com.elster.coding.CodingUtils;
import com.elster.dlms.apdu.coding.CoderAare;
import com.elster.dlms.apdu.coding.CoderAarq;
import com.elster.dlms.apdu.coding.CoderDlmsData;
import com.elster.dlms.apdu.coding.CoderGetDataResult;
import com.elster.dlms.apdu.coding.CoderReleaseRequest;
import com.elster.dlms.apdu.coding.CoderReleaseResponse;
import com.elster.dlms.apdu.coding.CoderXDlmsApdu;
import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.action.ActionResult;
import com.elster.dlms.cosem.application.services.action.CosemActionRequestNormal;
import com.elster.dlms.cosem.application.services.action.CosemActionRequestWithList;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseNormal;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseWithList;
import com.elster.dlms.cosem.application.services.common.AbstractCosemDataServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNext;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNormal;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestWithList;
import com.elster.dlms.cosem.application.services.get.CosemGetResponse;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseNormal;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseOneBlock;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseWithList;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import com.elster.dlms.cosem.application.services.open.ConfirmedServiceError;
import com.elster.dlms.cosem.application.services.open.DlmsConformance;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;
import com.elster.dlms.cosem.application.services.open.OpenRequest;
import com.elster.dlms.cosem.application.services.open.OpenResponse;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import com.elster.dlms.cosem.application.services.release.ReleaseRequest;
import com.elster.dlms.cosem.application.services.release.ReleaseResponse;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestNormal;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestWithList;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseNormal;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseWithList;
import com.elster.dlms.definitions.DlmsOids;
import com.elster.dlms.security.DlmsSecurityProviderGcm;
import com.elster.dlms.security.IDlmsSecurityProvider;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.CosemAttributeDescriptorWithData;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.CosemMethodDescriptorWithData;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.basic.ServiceClass;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.protocols.ILongOperationListener;
import com.elster.protocols.IProtocolStateObservable;
import com.elster.protocols.IProtocolStateObserver;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.ProtocolStateObservableSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the COSEM Application Layer.
 *
 * @author osse
 */
public class CosemApplicationLayer implements IProtocolStateObservable
{
  private static final Logger LOGGER = Logger.getLogger(CosemApplicationLayer.class.getName());
  private final IDlmsLlc logicalLink;
  private final CoderXDlmsApdu coderXDlmsApdu = new CoderXDlmsApdu();
  private final ProtocolStateObservableSupport protocolStateSupport = new ProtocolStateObservableSupport(this);
  private final int clientId;
  private int logicalDeviceId = -1;
  private int pduRxTimeout = 10000;
//  private boolean useRlrqRlre = false;
  private boolean cipheredContext = false;
  private ProposedXDlmsContext lastProposedXDlmsContext;
  private IDlmsSecurityProvider securityProvider = null;
  private SecurityControlField defaultSecurityControlField = null;
  private int invocation = 0;
  private int clientMaxReceivePduSize = 0;
  private static final EnumSet<DlmsConformance> DRIVER_CAPS = EnumSet.of(
          DlmsConformance.ACTION,
          DlmsConformance.SET,
          DlmsConformance.GET,
          DlmsConformance.BLOCK_TRANSFER_WITH_GET_OR_READ,
          DlmsConformance.SELECTIVE_ACCESS,
          DlmsConformance.MULTIPLE_REFERENCES);

  private synchronized ServiceInvocationId nextInvocationId()
  {
    invocation = (invocation + 1) & 0x0F;
    return new ServiceInvocationId(invocation, ServiceInvocationId.Priority.NORMAL,
                                   ServiceClass.CONFIRMED);
  }

  /**
   * Create the COSEM application layer.
   *
   *
   * @param logicalLink This LLC will be used to send and receive PDUs.
   *
   */
  public CosemApplicationLayer(final IDlmsLlc logicalLink)
  {
    this(logicalLink, 0x10);
  }

  /**
   * Create the COSEM application layer.
   *
   *
   * @param logicalLink This LLC will be used to send and receive PDUs.
   * @param clientId The client id (e.g. 0x10 for the public client)
   *
   */
  public CosemApplicationLayer(final IDlmsLlc logicalLink, final int clientId)
  {
    this.logicalLink = logicalLink;
    this.clientId = clientId;
  }

  private final IProtocolStateObserver logicalLinkObserver = new IProtocolStateObserver()
  {
    // @Override
    public void openStateChanged(final Object sender, final ProtocolState oldState,
                                 final ProtocolState newState)
    {
      //ignore (only connectionBroken is relevant)
    }

//      @Override
    public void connectionBroken(final Object sender, final Object orign, final Exception reason)
    {
      if (protocolStateSupport.getProtocolState() != ProtocolState.CLOSE)
      {
        protocolStateSupport.setState(ProtocolState.CLOSE, true);
        protocolStateSupport.notifyConnectionBroken(orign, reason);
      }
    }

  };

  /**
   * Opens an connection to an logical device.<P>
   * The LLC (provided in the constructor) must be in an state were an logical link
   * can be opened.
   *
   * @param logicalDeviceID The id of the logical device. (For example "1" for the management device)
   * @param openRequest The open request to use.
   * @throws IOException
   */
  public OpenResponse open(final int logicalDeviceID, final OpenRequest openRequest) throws IOException
  {
    synchronized (logicalLink)
    {
      setProtocolState(ProtocolState.OPENING);

      try
      {
        cipheredContext = false;
        this.logicalDeviceId = logicalDeviceID;
        logicalLink.openLogicalDeviceLink(clientId, logicalDeviceID, true);

        final OpenResponse openResponse = sendOpenRequest(openRequest); //  coderAare.decodeObject(dataUnitIn.getInputStream());
        lastProposedXDlmsContext = openRequest.getProposedXDlmsContext();

        checkOpenResponse(openResponse);

        logicalLink.addProtocolStateListener(logicalLinkObserver);

        setProtocolState(ProtocolState.OPEN);
        return openResponse;
      }
      catch (IOException ex)
      {
        setProtocolState(ProtocolState.CLOSE);
        protocolStateSupport.notifyObservers();

        if (logicalLink.isOpen())
        {
          logicalLink.closeLogicalDeviceLink(true);
        }

        throw ex;
      }
    }
  }

  private OpenResponse sendOpenRequest(final OpenRequest openRequest) throws IOException
  {
    //--- Write ---
    final CoderAarq coderAarq = new CoderAarq();
    logicalLink.writePdu(new ProtocolDataUnitOut(coderAarq.encodeObjectToBytes(openRequest)), true);

    //--- Read ---
    final ProtocolDataUnitIn dataUnitIn = logicalLink.readPdu(true, pduRxTimeout);

    final CoderAare coderAare = new CoderAare(true, true);
    final OpenResponse openResponse = coderAare.decodeObject(dataUnitIn.getInputStream());

    return openResponse;
  }

  /**
   * Open a high level security link to the device.<P>
   * This includes:
   * <ul>
   *  <li>Sending an open request with an encrypted user information field</li>
   *  <li>Receiving the open response and decrypting the user information field</li>
   *  <li>Calling method 1 (reply_to_HLS_authentication) of 0.0.40.0.0.255 (class id 15) </li>
   *  <li>Checking the reply. The reply must be the processed challenge. </li>
   * </ul>
   * <P>
   * If the application association (inclusive calling "reply_to_HLS_authentication") fails an
   * {@link ApplicationAssociationFailedException } will be thrown.
   *
   *
   * @param logicalDeviceID The logical device ID to connect to.
   * @param encryptionKey The encryption key to use.
   * @param authenticationKey The authentication key.
   * @return The received open response.
   * @throws IOException
   */
  public OpenResponse openHls(final int logicalDeviceID, final byte[] systemTitle, final byte[] encryptionKey,
                              final byte[] authenticationKey) throws IOException
  {

    synchronized (logicalLink)
    {
      setProtocolState(ProtocolState.OPENING);
      try
      {
        cipheredContext = true;
        this.logicalDeviceId = logicalDeviceID;
        logicalLink.openLogicalDeviceLink(clientId, logicalDeviceID, true);

        securityProvider = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey, systemTitle);
        coderXDlmsApdu.setSecurityProvider(securityProvider);

        ProposedXDlmsContext proposedXDlmsContext = new ProposedXDlmsContext();

        proposedXDlmsContext.setProposedDlmsConformance(DRIVER_CAPS);
        proposedXDlmsContext.setClientMaxReceivePduSize(15000);

        proposedXDlmsContext.setDedicatedKey(securityProvider.getDedicatedKey());
        proposedXDlmsContext.setResponseAllowed(true);
        proposedXDlmsContext.setClientMaxReceivePduSize(clientMaxReceivePduSize);

        proposedXDlmsContext.setSecurityControlField(
                new SecurityControlField(0, true, true, SecurityControlField.CipheringMethod.GLOBAL_UNICAST));

        final OpenRequest openRequest = new OpenRequest();
        openRequest.setAcseRequirements(OpenRequest.ASCE_AUTHENTICATION);
        openRequest.setApplicationContextName(DlmsOids.DLMS_UA_AC_LN_WC);
        openRequest.setCallingApTitle(systemTitle);
        openRequest.setCallingAuthenticationValue(securityProvider.buildCallingAuthenticationValue());

        openRequest.setSecurityMechanismName(
                DlmsOids.DLMS_UA_AMN_COSEM_HIGH_LEVEL_SECURITY_MECHANISM_NAME_USING_GMAC);

        openRequest.setProposedXDlmsContext(proposedXDlmsContext);

        openRequest.setUserInfo(coderXDlmsApdu.encodeObjectToBytes(proposedXDlmsContext));

        final OpenResponse openResponse = sendOpenRequest(openRequest);
        lastProposedXDlmsContext = openRequest.getProposedXDlmsContext();

        if (openResponse.getRespondingApTitle() != null)
        {
          securityProvider.setRespondingApTitle(openResponse.getRespondingApTitle());
          decryptUserInfo(openResponse);
        }
        checkOpenResponse(openResponse);


        final AuthenticationValue respondingAuthenticationValue = openResponse.
                getRespondingAuthenticationValue();

        if (respondingAuthenticationValue == null)
        {
          throw new ApplicationAssociationFailedException("No responding authentication value received",
                                                          openResponse);
        }
        final byte[] answerToServerChallenge =
                securityProvider.processServerChallenge(respondingAuthenticationValue);
        final ActionResponse actionResponse = executeAction(
                new CosemMethodDescriptor(new ObisCode(0, 0, 40, 0, 0, 255), 15, 1), new DlmsDataOctetString(
                answerToServerChallenge), null);


        if (actionResponse.getActionResult() != ActionResult.SUCCESS)
        {
          throw new ApplicationAssociationFailedException("Reply to HLS authentication failed. Action result: "
                                                          + actionResponse.getActionResult().getName(),
                                                          openResponse);
        }

        final GetDataResult dataResult = actionResponse.getGetDataResult();

        if (dataResult == null)
        {
          throw new ApplicationAssociationFailedException(
                  "Reply to HLS authentication failed. No \"Get Data Result\" received.", openResponse);
        }

        if (dataResult.getAccessResult() != DataAccessResult.SUCCESS)
        {
          throw new ApplicationAssociationFailedException("Reply to HLS authentication failed. \"Get Data Access Result\": "
                                                          + dataResult.getAccessResult().getName(),
                                                          openResponse);
        }

        final DlmsData respondToChallenge = dataResult.getData();

        if (respondToChallenge.getType() != DlmsData.DataType.OCTET_STRING)
        {
          throw new ApplicationAssociationFailedException("Reply to HLS authentication failed. Unexpected data type: "
                                                          + respondToChallenge.getType(), openResponse);
        }

        if (!securityProvider.
                checkServerReplyToChallenge(((DlmsDataOctetString)respondToChallenge).getValue()))
        {
          throw new ApplicationAssociationFailedException(
                  "Reply to HLS authentication failed. Processed challenge wrong!", openResponse);
        }

        defaultSecurityControlField =
                new SecurityControlField(SecurityControlField.SECURITY_SUITE_AES128_GCM,
                                         true, true, SecurityControlField.CipheringMethod.DEDICATED_UNICAST);

        setProtocolState(ProtocolState.OPEN);
        return openResponse;
      }
      catch (IOException ex)
      {
        setProtocolState(ProtocolState.CLOSE);
        protocolStateSupport.notifyObservers();

        if (logicalLink.isOpen())
        {
          logicalLink.closeLogicalDeviceLink(true);
        }

        throw ex;
      }

    }
  }

  private void decryptUserInfo(final OpenResponse openResponse) throws ApplicationAssociationFailedException
  {
    if (openResponse.getNegotiatedXDlmsContext() == null && openResponse.getxDlmsInitiateError() == null
        && openResponse.getUserInfo() != null)
    {
      final byte[] userInfo = openResponse.getUserInfo();

      if (userInfo.length > 0 && ((userInfo[0] & 0xFF) == 0x28 || (userInfo[0] & 0xFF) == 0x0E))
      {
        try
        {
          final AbstractCosemServiceInvocation serviceObject = coderXDlmsApdu.decodeObjectFromBytes(userInfo);

          switch (serviceObject.getServiceInvocation())
          {
            case INITIATE_RESPONSE:
              openResponse.setNegotiatedXDlmsContext((NegotiatedXDlmsContext)serviceObject);
              break;
            case SERVICE_ERROR:
              openResponse.setxDlmsInitiateError((ConfirmedServiceError)serviceObject);
              break;
          }
        }
        catch (IOException ex)
        {
          throw new ApplicationAssociationFailedException("Error decrypting user info: " + ex, ex,
                                                          openResponse);
        }
      }
    }

  }

  /**
   * Opens an connection to an logical device.
   * <P>
   * If the key is not {@code null} "low level security" is used, otherwise "lowest level security" is used.
   * For more specified open requests {@link #open(int, com.elster.dlms.cosem.application.services.open.OpenRequest)}
   * can be used.
   * <P>
   * The LLC (provided in the constructor) must be in an state there an logical link
   * can be opened.
   *
   * @param logicalDeviceID The id of the logical device. (For example "1" for the management device)
   * @param key The key for "low level security".
   * @throws IOException
   */
  public OpenResponse open(final int logicalDeviceID, final String key) throws IOException
  {
    final OpenRequest openRequest = new OpenRequest();

    openRequest.setApplicationContextName(DlmsOids.DLMS_UA_AC_LN);
    openRequest.getProposedXDlmsContext().setClientMaxReceivePduSize(clientMaxReceivePduSize);

    openRequest.getProposedXDlmsContext().setProposedDlmsConformance(DRIVER_CAPS);

    if (key != null)
    {
      openRequest.setCallingAuthenticationValue(AuthenticationValue.createCharstring(key));
      openRequest.setSecurityMechanismName(DlmsOids.DLMS_UA_AMN_COSEM_LOW_LEVEL_SECURITY_MECHANISM_NAME);
      openRequest.setAcseRequirements(OpenRequest.ASCE_AUTHENTICATION);
    }

    return open(logicalDeviceID, openRequest);
  }

  /**
   * Opens an connection to an logical device.<P>
   * The "lowest level security" is used.
   * For more specified open requests {@link #open(int, com.elster.dlms.cosem.application.services.open.OpenRequest)}
   * can be used.
   * <P>
   * The LLC (provided in the constructor) must be in an state there an logical link
   * can be opened.
   *
   * @param logicalDeviceID The id of the logical device. (For example "1" for the management device)
   * @throws IOException
   */
  public OpenResponse open(final int logicalDeviceID) throws IOException
  {
    final OpenRequest openRequest = new OpenRequest();

    openRequest.setApplicationContextName(DlmsOids.DLMS_UA_AC_LN);
    openRequest.getProposedXDlmsContext().setClientMaxReceivePduSize(clientMaxReceivePduSize);
    openRequest.getProposedXDlmsContext().setProposedDlmsConformance(DRIVER_CAPS);

    return open(logicalDeviceID, openRequest);
  }

  /**
   * Closes the application layer.
   * <P>
   * The logical link will also be closed.
   *
   * @throws IOException
   */
  public void close() throws IOException
  {
    setProtocolState(ProtocolState.CLOSING);
    try
    {
//      if (useRlrqRlre)
//      {
//        release(ReleaseRequest.Reason.NORMAL);
//      }

      synchronized (logicalLink)
      {
        logicalLink.removeProtocolStateListener(logicalLinkObserver);
        logicalLink.closeLogicalDeviceLink(true);
        setProtocolState(ProtocolState.CLOSE);
      }
    }
    catch (IOException ex)
    {
      setProtocolState(ProtocolState.CLOSE);
      throw ex;
    }
  }

  /**
   * Sends a release request message and return the result.<P>
   * <b>This method currently don't changes the status of this layer, even if the answer indicates the AA was ended.<b>
   * 
   * @param reason The reason to be sent.
   * @return The received reason.
   * @throws IOException 
   */
  public ReleaseResponse.Reason release(final ReleaseRequest.Reason reason) throws IOException
  {
    final ReleaseRequest releaseRequest = new ReleaseRequest();
    releaseRequest.setReason(reason);

    if (cipheredContext)
    {
      if (lastProposedXDlmsContext == null)
      {
        throw new IllegalStateException("Proposed xDLMS context not available (required for release service)");
      }

      if (!lastProposedXDlmsContext.getSecurityControlField().isEncrypted())
      {
        throw new IllegalStateException(
                "The security control field in the proposed xDLMS context must indicate that the PDU has to be ciphered");
      }
      releaseRequest.setUserInfo(coderXDlmsApdu.encodeObjectToBytes(lastProposedXDlmsContext));
    }


    //--- Write ---
    final CoderReleaseRequest coderRequest = new CoderReleaseRequest();
    logicalLink.writePdu(new ProtocolDataUnitOut(coderRequest.encodeObjectToBytes(releaseRequest)), true);

    //--- Read ---
    final ProtocolDataUnitIn dataUnitIn = logicalLink.readPdu(true, pduRxTimeout);

    final CoderReleaseResponse coderResponse = new CoderReleaseResponse();
    final ReleaseResponse response = coderResponse.decodeObject(dataUnitIn.getInputStream());

    return response.getReason();
  }

  public AbstractCosemServiceInvocation sendAndReceive(final AbstractCosemServiceInvocation serviceInvocation)
          throws
          IOException
  {
    try
    {

      if (LOGGER.isLoggable(Level.FINER))
      {
        LOGGER.log(Level.FINER, "Send: {0}", serviceInvocation.toString());
      }

      final byte[] pduOut = coderXDlmsApdu.encodeObjectToBytes(serviceInvocation);

      if (LOGGER.isLoggable(Level.FINEST))
      {
        LOGGER.log(Level.FINEST, "PDU out: {0}", CodingUtils.byteArrayToString(pduOut));
      }

      ProtocolDataUnitIn dataUnitIn;
      synchronized (logicalLink)
      {
        logicalLink.writePdu(new ProtocolDataUnitOut(pduOut), true);
        dataUnitIn = logicalLink.readPdu(true, pduRxTimeout);
      }


      final AbstractCosemServiceInvocation result = coderXDlmsApdu.decodeObject(dataUnitIn.getInputStream());

      if (LOGGER.isLoggable(Level.FINER))
      {
        LOGGER.log(Level.FINER, "Received: {0}", result.toString());
      }

      final SecurityControlField scfRq = serviceInvocation.getSecurityControlField();
      final SecurityControlField scfRs = result.getSecurityControlField();


      if (!saveEquals(scfRq, scfRs))
      {
        throw new CosemApplicationLayerException("Unexpected received security control field: " + result.
                getSecurityControlField()
                                                 + " --- Expected:"
                                                 + serviceInvocation.getSecurityControlField());
      }


      if (serviceInvocation instanceof AbstractCosemDataServiceInvocation
          && result instanceof AbstractCosemDataServiceInvocation)
      {
        final AbstractCosemDataServiceInvocation dataServiceInvocation =
                (AbstractCosemDataServiceInvocation)serviceInvocation;
        final AbstractCosemDataServiceInvocation dataServiceResult =
                (AbstractCosemDataServiceInvocation)result;

        if (!dataServiceInvocation.getInvocationId().equals(dataServiceResult.getInvocationId()))
        {
          throw new CosemApplicationLayerException("Unexpected invocation id. Expected:"
                                                   + dataServiceInvocation.getInvocationId().toInteger()
                                                   + "Actual: "
                                                   + dataServiceResult.getInvocationId().toInteger());
        }
      }
      return result;
    }
    catch (IOException exception)
    {
      LOGGER.log(Level.INFO, "Exception during sending and receiving PDUs. Request: " + serviceInvocation.
              toString(), exception);
      throw exception;
    }
  }

  /**
   * Invokes the DLMS COSEM ACTION service.
   *
   * @param methodDescriptor The method to invoke.
   * @param parameters The parameters for the method
   * @param securityControlField The security control field.
   * @return The Action response.
   * @throws IOException
   */
  public ActionResponse executeAction(final CosemMethodDescriptor methodDescriptor,
                                      final DlmsData parameters,
                                      final SecurityControlField securityControlField) throws
          IOException
  {
    final CosemActionRequestNormal actionRequestNormal = new CosemActionRequestNormal();

    actionRequestNormal.setMethodDescriptor(methodDescriptor);
    actionRequestNormal.setMethodInvocationParamers(parameters);
    actionRequestNormal.setInvocationId(nextInvocationId());

    if (securityControlField == null)
    {
      actionRequestNormal.setSecurityControlField(defaultSecurityControlField);
    }
    else
    {
      actionRequestNormal.setSecurityControlField(securityControlField);
    }
    final AbstractCosemServiceInvocation result = sendAndReceive(actionRequestNormal);

    if (result instanceof CosemActionResponseNormal)
    {
      return ((CosemActionResponseNormal)result).getActionResponseWithOptionalData();
    }
    else
    {
      throw new IOException("Unexpected action request response. Expected: CosemActionResponseNormal, Actual: "
                            + result.getClass().getSimpleName());
    }
  }

  public List<ActionResponse> executeActions(
          final List<CosemMethodDescriptorWithData> methodDescriptorsWithData) throws
          IOException
  {
    return executeActions(methodDescriptorsWithData, defaultSecurityControlField);
  }

  public List<ActionResponse> executeActions(
          final List<CosemMethodDescriptorWithData> methodDescriptorsWithData,
                                             final SecurityControlField securityControlField) throws
          IOException
  {

    final CosemActionRequestWithList actionRequestWithList = new CosemActionRequestWithList();

    actionRequestWithList.setInvocationId(nextInvocationId());


    for (CosemMethodDescriptorWithData methodDescriptorWithData : methodDescriptorsWithData)
    {
      actionRequestWithList.getMethodDescriptors().add(methodDescriptorWithData.getMethodDescriptor());
      actionRequestWithList.getMethodInvocationParamters().add(methodDescriptorWithData.getData());
    }

    if (securityControlField == null)
    {
      actionRequestWithList.setSecurityControlField(defaultSecurityControlField);
    }
    else
    {
      actionRequestWithList.setSecurityControlField(securityControlField);
    }


    final AbstractCosemServiceInvocation result = sendAndReceive(actionRequestWithList);

    if (result instanceof CosemActionResponseWithList)
    {
      return ((CosemActionResponseWithList)result).getActionResponsesWithOptionalData();
    }
    else
    {
      throw new IOException("Unexpected action request response. Expected: CosemActionResponseWithList, Received: "
                            + result.getClass().getSimpleName());
    }
  }

  /**
   * Executes the COSEM method and checks the result.<P>
   * 
   * If the action result or the data access result indicates and error a {@link CosemActionResponseException} will be thrown.<P>
   * 
   * The default security control field will be used.
   * 
   * @return The return parameters or null if no return parameters are available.
   */
  public DlmsData executeActionAndCheckResponse(final CosemMethodDescriptor methodDescriptor,
                                                final DlmsData parameters) throws
          IOException
  {
    return executeActionAndCheckResponse(methodDescriptor, parameters, defaultSecurityControlField);
  }

  /**
   * Executes the COSEM method and checks the result.<P>
   * 
   * If the action result or the data access result indicates and error a {@link CosemActionResponseException} will be thrown.
   * 
   * @return The return parameters or null if no return parameters are available.
   */
  public DlmsData executeActionAndCheckResponse(final CosemMethodDescriptor methodDescriptor,
                                                final DlmsData parameters,
                                                final SecurityControlField securityControlField) throws
          IOException
  {
    final ActionResponse response = executeAction(methodDescriptor, parameters, securityControlField);

    if (!response.getActionResult().equals(ActionResult.SUCCESS))
    {
      throw new CosemActionResponseException("Action response: " + response.getActionResult()
                                             + "Method descriptor: " + methodDescriptor.toString(), response);
    }

    if (response.getGetDataResult() != null)
    {
      if (!response.getGetDataResult().getAccessResult().equals(DataAccessResult.SUCCESS))
      {
        throw new CosemActionResponseException("Data access result in action response: " + response.
                getGetDataResult() + "Method descriptor: " + methodDescriptor.toString(), response);
      }
      return response.getGetDataResult().getData();
    }

    return null;
  }

  /**
   * Invokes the COSEM GET service.<P>
   * See GB ed.7 p.146 "9.3.6 The GET service" and GB ed.7 p.180 "9.4.6.3 Protocol for the GET service"<P>
   * The default security control field will be used.
   *
   *
   * @param attributeDescriptor The attribute descriptor describing the attribute
   * @return The result as {@link GetDataResult}. The {@link GetDataResult#getAccessResult()} must be checked after calling this method.
   * @throws IOException
   */
  public GetDataResult getAttribute(final CosemAttributeDescriptor attributeDescriptor) throws IOException
  {
    return getAttribute(attributeDescriptor, defaultSecurityControlField);
  }

  /**
   * Invokes the COSEM GET service.<P>
   * See GB ed.7 p.146 "9.3.6 The GET service" and GB ed.7 p.180 "9.4.6.3 Protocol for the GET service"
   *
   *
   * @param attributeDescriptor The attribute descriptor describing the attribute
   * @param securityControlField The security control field. If null the default security control field will be used.
   * @return The result as {@link GetDataResult}. The {@link GetDataResult#getAccessResult()} must be checked after calling this method.
   * @throws IOException
   */
  public GetDataResult getAttribute(final CosemAttributeDescriptor attributeDescriptor,
                                    SecurityControlField securityControlField) throws IOException
  {
    final CosemGetRequestNormal getRequestNormal = new CosemGetRequestNormal();

    getRequestNormal.setAttributeDescriptor(attributeDescriptor);
    getRequestNormal.setInvocationId(nextInvocationId());

    if (securityControlField == null)
    {
      securityControlField = defaultSecurityControlField;
    }

    getRequestNormal.setSecurityControlField(securityControlField);

    final AbstractCosemServiceInvocation result = sendAndReceive(getRequestNormal);

    if (result instanceof CosemGetResponse)
    {
      final CosemGetResponse getResponse = (CosemGetResponse)result;

      switch (getResponse.getResponseType())
      {
        case NORMAL:
          return ((CosemGetResponseNormal)getResponse).getGetDataResult();
        case LAST_BLOCK:
        case ONE_BLOCK:
          return readDataBlocks((CosemGetResponseOneBlock)getResponse, securityControlField);
        default:
          throw new UnsupportedOperationException("get response type not supported: " + getResponse.
                  getResponseType());
      }
    }
    else
    {
      throw new IOException("Unexpected PDU received. Excpected: GET_RESPONSE, Received:" + result.
              getServiceInvocation());
    }
  }

  public List<GetDataResult> getAttributes(final List<CosemAttributeDescriptor> attributeDescriptors) throws
          IOException
  {
    return getAttributes(attributeDescriptors, defaultSecurityControlField);
  }

  /**
   * Invokes the COSEM GET service.<P>
   * See GB ed.7 p.146 "9.3.6 The GET service" and GB ed.7 p.180 "9.4.6.3 Protocol for the GET service"
   *
   *
   * @param attributeDescriptors The attribute descriptor describing the attribute
   * @param securityControlField The security control field. If null the default security control field will be used.
   * @return The result as {@link GetDataResult}. The {@link GetDataResult#getAccessResult()} must be checked after calling this method.
   * @throws IOException
   */
  public List<GetDataResult> getAttributes(final List<CosemAttributeDescriptor> attributeDescriptors,
                                           SecurityControlField securityControlField) throws IOException
  {
    final CosemGetRequestWithList getRequestWithList = new CosemGetRequestWithList();

    getRequestWithList.getAttributeDescriptors().addAll(attributeDescriptors);
    getRequestWithList.setInvocationId(nextInvocationId());

    if (securityControlField == null)
    {
      securityControlField = defaultSecurityControlField;
    }

    getRequestWithList.setSecurityControlField(securityControlField);

    final AbstractCosemServiceInvocation result = sendAndReceive(getRequestWithList);

    if (result instanceof CosemGetResponse)
    {
      final CosemGetResponse getResponse = (CosemGetResponse)result;

      switch (getResponse.getResponseType())
      {
        case LAST_BLOCK:
        case ONE_BLOCK:
          return readDataBlocksWithList(attributeDescriptors.size(), (CosemGetResponseOneBlock)getResponse,
                                        securityControlField);
        case WITH_LIST:
          return ((CosemGetResponseWithList)getResponse).getGetDataResults();
        default:
          throw new UnsupportedOperationException("get response type not supported: " + getResponse.
                  getResponseType());
      }
    }
    else
    {
      throw new IOException("Unexpected PDU received. Excpected: GET_RESPONSE, Received:" + result.
              getServiceInvocation());
    }
  }

  /**
   * Invokes the COSEM GET service and check the data access result.<P>
   * See GB ed.7 p.146 "9.3.6 The GET service" and GB ed.7 p.180 "9.4.6.3 Protocol for the GET service"<P>
   * If the data access result indicates an error an {@link CosemDataAccessException} will be thrown.
   *
   * 
   * @param attributeDescriptor The attribute descriptor describing the attribute
   * @param securityControlField The security control field. If null the default security control field will be used.
   * @return The result as {@link GetDataResult}. The {@link GetDataResult#getAccessResult()} must be checked after calling this method.
   * @throws IOException
   */
  public DlmsData getAttributeAndCheckResult(final CosemAttributeDescriptor attributeDescriptor,
                                             SecurityControlField securityControlField) throws IOException
  {
    GetDataResult getDataResult = getAttribute(attributeDescriptor, securityControlField);
    checkGetDataResult(attributeDescriptor, getDataResult);
    return getDataResult.getData();
  }

  public static void checkGetDataResult(final CosemAttributeDescriptor attributeDescriptor,
                                        final GetDataResult getDataResult) throws CosemDataAccessException
  {
    if (!getDataResult.getAccessResult().equals(DataAccessResult.SUCCESS))
    {
      throw new CosemDataAccessException("Data access error:" + getDataResult.getAccessResult().getName()
                                         + ", Logical Name:"
                                         + attributeDescriptor.getInstanceId() + ", Attribute: "
                                         + attributeDescriptor.getAttributeId(),
                                         getDataResult.getAccessResult());
    }
  }

  /**
   * Invokes the COSEM GET service and check the data access result.<P>
   * See GB ed.7 p.146 "9.3.6 The GET service" and GB ed.7 p.180 "9.4.6.3 Protocol for the GET service"<P>
   * If the data access result indicates an error an {@link CosemDataAccessException} will be thrown.<P>
   * The default security control field will be used.
   *
   * 
   * @param attributeDescriptor The attribute descriptor describing the attribute
   * @return The result as {@link GetDataResult}. The {@link GetDataResult#getAccessResult()} must be checked after calling this method.
   * @throws IOException
   */
  public DlmsData getAttributeAndCheckResult(final CosemAttributeDescriptor attributeDescriptor) throws
          IOException
  {
    return getAttributeAndCheckResult(attributeDescriptor, null);
  }

  /**
   * Invokes the COSEM GET service.<P>
   * For details see {@link #getAttribute(com.elster.dlms.types.basic.CosemAttributeDescriptor)}
   * 
   * @param classId The class id
   * @param instanceId The instance id
   * @param attributeId The attribute id
   * @param accessSelectionParameters The access selection parameters (can be {@code null}).
   * @return The result as {@link GetDataResult}. The {@link GetDataResult#getAccessResult()} must be checked after calling this method.
   * @throws IOException
   */
  public GetDataResult getAttribute(final int classId, final ObisCode instanceId, final int attributeId,
                                    final AccessSelectionParameters accessSelectionParameters,
                                    final SecurityControlField securityControlField) throws IOException
  {
    final CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(instanceId, classId,
                                                                                      attributeId,
                                                                                      accessSelectionParameters);
    return getAttribute(attributeDescriptor, securityControlField);
  }

  private GetDataResult readDataBlocks(final CosemGetResponseOneBlock firstBlock,
                                       final SecurityControlField securityControlField) throws IOException
  {

    DataAccessResultAndRawData resultAndRawData = readRawDataBlocks(firstBlock, securityControlField);

    if (resultAndRawData.dataAccessResult != DataAccessResult.SUCCESS)
    {
      return new GetDataResult(resultAndRawData.dataAccessResult, null);
    }

    final CoderDlmsData coderDlmsData = new CoderDlmsData();
    final DlmsData receivedData = coderDlmsData.decodeObjectFromBytes(resultAndRawData.raw);
    return new GetDataResult(DataAccessResult.SUCCESS, receivedData);
  }

  private List<GetDataResult> readDataBlocksWithList(final int expectedCount,
                                                     final CosemGetResponseOneBlock firstBlock,
                                                     final SecurityControlField securityControlField) throws
          IOException
  {


    DataAccessResultAndRawData resultAndRawData = readRawDataBlocks(firstBlock, securityControlField);

    if (resultAndRawData.dataAccessResult != DataAccessResult.SUCCESS)
    {
      final List<GetDataResult> failureResult = new ArrayList<GetDataResult>(expectedCount);
      for (int i = 0; i < expectedCount; i++)
      {
        failureResult.add(new GetDataResult(resultAndRawData.dataAccessResult, null));
      }
      return failureResult;
    }

    final AXdrCoderSequenceOfWrapper<GetDataResult> coder =
            new AXdrCoderSequenceOfWrapper<GetDataResult>(new CoderGetDataResult());
    return coder.decodeObjectFromBytes(resultAndRawData.raw);
  }

  private DataAccessResultAndRawData readRawDataBlocks(final CosemGetResponseOneBlock firstBlock,
                                                       final SecurityControlField securityControlField) throws
          IOException
  {

    if (LOGGER.isLoggable(Level.FINE))
    {
      LOGGER.log(Level.FINE, "Start receiving get response with data blocks");
    }


    if (firstBlock.getDataAccessResult() != DataAccessResult.SUCCESS)
    {
      return new DataAccessResultAndRawData(firstBlock.getDataAccessResult(), null);
    }

    long blockNumber = firstBlock.getBlocknumber();
    final ServiceInvocationId serviceInvocationId = firstBlock.getInvocationId();

    if (blockNumber != 1) //The first block must have the block number 1. See GB ed.7 p.181 "9.4.6.3 Protocol for the GET service"
    {
      throw new IOException("GET: the first block must have the block number 1. Is:" + blockNumber);
    }

    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    buffer.write(firstBlock.getRawData());

    startLongOpertation(ILongOperationListener.Operation.READ);
    try
    {

      CosemGetResponseOneBlock currentBlock = firstBlock;
      while (!currentBlock.isLastBlock())
      {
        setLongOpertationProgress(ILongOperationListener.Operation.READ, buffer.size());

        final CosemGetRequestNext getRequestNext = new CosemGetRequestNext();
        getRequestNext.setBlockNo(blockNumber);
        getRequestNext.setInvocationId(serviceInvocationId);
        getRequestNext.setSecurityControlField(securityControlField);

        final AbstractCosemServiceInvocation result = sendAndReceive(getRequestNext);

        if (result instanceof CosemGetResponseOneBlock)
        {
          blockNumber++;
          currentBlock = (CosemGetResponseOneBlock)result;
          if (currentBlock.getBlocknumber() != blockNumber)
          {
            //Abort the transfer by sending a CosemGetRequestNext with an invalid block number. See GB ed.7 p.182 (Case d) "9.4.6.3 Protocol for the GET service"
            final CosemGetRequestNext getRequestNextAbort = new CosemGetRequestNext();
            getRequestNextAbort.setBlockNo(0);
            getRequestNextAbort.setInvocationId(serviceInvocationId);
            getRequestNextAbort.setSecurityControlField(securityControlField);
            sendAndReceive(getRequestNextAbort);
            throw new IOException("GET: unexpected block number. Expected" + blockNumber + " Is:"
                                  + currentBlock.getBlocknumber());
          }

          if (currentBlock.getDataAccessResult() != DataAccessResult.SUCCESS)
          {
            return new DataAccessResultAndRawData(firstBlock.getDataAccessResult(), null);
          }

          buffer.write(currentBlock.getRawData());
        }
        else
        {
          throw new IOException("Unexpected answer. Expected CosemGetResponseOneBlock. Received:" + result.
                  getClass().getSimpleName());
        }
      }

    }
    finally
    {
      endLongOpertation(ILongOperationListener.Operation.READ);
    }
    byte[] data = buffer.toByteArray();

    if (LOGGER.isLoggable(Level.FINEST))
    {
      LOGGER.log(Level.FINEST, "Data from block transfer: {0}", CodingUtils.byteArrayToString(data));
    }

    return new DataAccessResultAndRawData(DataAccessResult.SUCCESS, data);
  }

  private static class DataAccessResultAndRawData
  {
    final DataAccessResult dataAccessResult;
    final byte[] raw;

    public DataAccessResultAndRawData(final DataAccessResult dataAccessResult, final byte[] raw)
    {
      this.dataAccessResult = dataAccessResult;
      this.raw = raw;
    }

  }

  /**
   * Sets the attribute and checks the result.<P>
   * If the data access result is not {@link DataAccessResult#SUCCESS} a {@link CosemDataAccessException} will be thrown.<P>
   * Parameters are described in {@link #setAttribute(com.elster.dlms.types.basic.CosemAttributeDescriptor, com.elster.dlms.types.data.DlmsData, com.elster.dlms.cosem.application.services.common.SecurityControlField)  }
   * 
   */
  public void setAttributeAndCheckResult(final CosemAttributeDescriptor attributeDescriptor,
                                         final DlmsData data,
                                         final SecurityControlField securityControlField)
          throws IOException
  {
    final DataAccessResult dataAccessResult = setAttribute(attributeDescriptor, data, securityControlField);
    if (!dataAccessResult.equals(DataAccessResult.SUCCESS))
    {
      throw new CosemDataAccessException("Data access error:" + dataAccessResult.getName() + " Logical Name:"
                                         + attributeDescriptor.getInstanceId() + " Attribute: "
                                         + attributeDescriptor.getAttributeId(), dataAccessResult);
    }
  }

  /**
   * Sets the attribute and checks the result.<P>
   * If the data access result is not {@link DataAccessResult#SUCCESS} a {@link CosemDataAccessException} will be thrown.<P>
   * The default security control field will be used.
   * Parameters are described in {@link #setAttribute(com.elster.dlms.types.basic.CosemAttributeDescriptor, com.elster.dlms.types.data.DlmsData, com.elster.dlms.cosem.application.services.common.SecurityControlField)  }
   * 
   */
  public void setAttributeAndCheckResult(final CosemAttributeDescriptor attributeDescriptor,
                                         final DlmsData data)
          throws IOException
  {
    setAttributeAndCheckResult(attributeDescriptor, data, null);
  }

  /**
   * Set one attribute using the SET service.
   * 
   * 
   * @param attributeDescriptor The descriptor of the attribute to set.
   * @param data The data to set.
   * @param securityControlField The security control field. If {@code null} the default security control field will be used.
   * @return The data access result.
   * @throws IOException 
   */
  public DataAccessResult setAttribute(final CosemAttributeDescriptor attributeDescriptor, final DlmsData data,
                                       final SecurityControlField securityControlField)
          throws IOException
  {
    final CosemSetRequestNormal setRequestNormal = new CosemSetRequestNormal();

    setRequestNormal.setAttributeDescriptor(attributeDescriptor);
    setRequestNormal.setInvocationId(nextInvocationId());
    setRequestNormal.setData(data);

    if (securityControlField == null)
    {
      setRequestNormal.setSecurityControlField(defaultSecurityControlField);
    }
    else
    {
      setRequestNormal.setSecurityControlField(securityControlField);
    }

    final AbstractCosemServiceInvocation result = sendAndReceive(setRequestNormal);

    if (result instanceof CosemSetResponseNormal)
    {
      return ((CosemSetResponseNormal)result).getDataAccessResult();
    }
    else
    {
      throw new CosemApplicationLayerException("Unexpected answer. Expected:" + CosemSetResponseNormal.class.
              getSimpleName()
                                               + " Received:" + result.getClass().getSimpleName());
    }
  }

  public List<DataAccessResult> setAttributes(
          final List<CosemAttributeDescriptorWithData> attributeDescriptorsWithData)
          throws IOException
  {
    return setAttributes(attributeDescriptorsWithData, defaultSecurityControlField);
  }

  public List<DataAccessResult> setAttributes(
          final List<CosemAttributeDescriptorWithData> attributeDescriptorsWithData,
          final SecurityControlField securityControlField)
          throws IOException
  {
    if (attributeDescriptorsWithData.isEmpty())
    {
      throw new IllegalArgumentException("The attribute descriptor list with data must be empty");
    }

    final CosemSetRequestWithList setRequestWithList = new CosemSetRequestWithList();

    setRequestWithList.setInvocationId(nextInvocationId());

    for (CosemAttributeDescriptorWithData attributeDescriptorWithData : attributeDescriptorsWithData)
    {
      setRequestWithList.getAttributeDescriptors().add(attributeDescriptorWithData.
              getCosemAttributeDescriptor());
      setRequestWithList.getValues().add(attributeDescriptorWithData.getData());
    }

    if (securityControlField == null)
    {
      setRequestWithList.setSecurityControlField(defaultSecurityControlField);
    }
    else
    {
      setRequestWithList.setSecurityControlField(securityControlField);
    }

    final AbstractCosemServiceInvocation result = sendAndReceive(setRequestWithList);

    if (result instanceof CosemSetResponseWithList)
    {
      return ((CosemSetResponseWithList)result).getDataAccessResults();
    }
    else
    {
      throw new CosemApplicationLayerException("Unexpected answer. Expected:"
                                               + CosemSetResponseWithList.class.
              getSimpleName()
                                               + " Received:" + result.getClass().getSimpleName());
    }


  }

  /**
   * Set one attribute using the SET service.<P>
   * The default security control field will be used.
   * 
   * 
   * @param attributeDescriptor The descriptor of the attribute to set.
   * @param data The data to set.
   * @return The data access result.
   * @throws IOException 
   */
  public DataAccessResult setAttribute(final CosemAttributeDescriptor attributeDescriptor, final DlmsData data)
          throws IOException
  {
    return setAttribute(attributeDescriptor, data, null);

  }

  //@Override
  public ProtocolState getProtocolState()
  {
    return protocolStateSupport.getProtocolState();
  }

  private void setProtocolState(ProtocolState openState)
  {
    protocolStateSupport.setState(openState, true);
  }

  //@Override
  public boolean isOpen()
  {
    return protocolStateSupport.isOpen();
  }

  //@Override
  public void removeProtocolStateListener(final IProtocolStateObserver observer)
  {
    protocolStateSupport.removeProtocolStateListener(observer);
  }

  //@Override
  public void addProtocolStateListener(final IProtocolStateObserver observer)
  {
    protocolStateSupport.addProtocolStateListener(observer);
  }

  public int getClientId()
  {
    return clientId;
  }

  public boolean isCipheredContext()
  {
    return cipheredContext;
  }

  /**
   * Checks the specified open response and throws an {@link ApplicationAssociationFailedException} if
   * the open response indicates that the Application Association failed.<P>
   *
   *
   * @param openResponse The open response to check.
   */
  private void checkOpenResponse(final OpenResponse openResponse) throws ApplicationAssociationFailedException
  {
    if (openResponse.getResult() != 0)
    {
      throw new ApplicationAssociationFailedException("Application association failed. Result: "
                                                      + openResponse.getResult(), openResponse);
    }

    if (openResponse.getxDlmsInitiateError() != null)
    {
      throw new ApplicationAssociationFailedException("Application association failed. Confirmed Service error: "
                                                      + openResponse.getxDlmsInitiateError(), openResponse);
    }

    if (openResponse.getNegotiatedXDlmsContext() == null)
    {
      throw new ApplicationAssociationFailedException("No \"negotiated XDLMS context\" ", openResponse);
    }

    if (securityProvider != null)
    {
      if (openResponse.getRespondingApTitle() == null)
      {
        throw new ApplicationAssociationFailedException(
                "No responding AP title (system title) received (Required for High level security).",
                openResponse);
      }
    }

  }

  public int getPduRxTimeout()
  {
    return pduRxTimeout;
  }

  public void setPduRxTimeout(final int pduRxTimeout)
  {
    this.pduRxTimeout = pduRxTimeout;
  }

  /**
   * Indicates if {@link #close()} sends a RLRQ (Release request) telegram.
   * 
   * @return true if a RLRQ should be sent.
   */
  /*
   public boolean isUseRlrqRlre()
   {
   return useRlrqRlre;
   }
  
   public void setUseRlrqRlre(boolean useRlrqRlre)
   {
   this.useRlrqRlre = useRlrqRlre;
   }
   */
  /**
   * Sets the security provider for testing.
   */
  public void testSetSecurityProvider(final IDlmsSecurityProvider securityProvider)
  {
    this.securityProvider = securityProvider;
    coderXDlmsApdu.setSecurityProvider(securityProvider);
  }

  public int getLogcialDeviceId()
  {
    return logicalDeviceId;
  }

  private boolean saveEquals(final Object o1, final Object o2)
  {
    if (o1 == o2)
    {
      return true;
    }
    if ((o1 == null) || (o2 == null))
    {
      return false;
    }
    return o1.equals(o2);
  }

  volatile ILongOperationListener longOperationListener = null;

  public ILongOperationListener getLongOperationListener()
  {
    return longOperationListener;
  }

  public void setLongOperationListener(final ILongOperationListener longOperationListener)
  {
    this.longOperationListener = longOperationListener;
  }

  private void startLongOpertation(final ILongOperationListener.Operation operation)
  {
    final ILongOperationListener l = longOperationListener;
    if (l != null)
    {
      l.longOperationStart(this, operation);
    }
  }

  private void endLongOpertation(final ILongOperationListener.Operation operation)
  {
    final ILongOperationListener l = longOperationListener;
    if (l != null)
    {
      l.longOperationEnd(this, operation);
    }
  }

  private void setLongOpertationProgress(final ILongOperationListener.Operation operation,
                                         final long byteCount)
  {
    final ILongOperationListener l = longOperationListener;
    if (l != null)
    {
      l.longOperationBytesTransfered(this, operation, byteCount);
    }
  }

  public IDlmsLlc getLogicalLink()
  {
    return logicalLink;
  }

  public int getClientMaxReceivePduSize()
  {
    return clientMaxReceivePduSize;
  }

  public void setClientMaxReceivePduSize(int clientMaxReceivePduSize)
  {
    this.clientMaxReceivePduSize = clientMaxReceivePduSize;
  }

}
