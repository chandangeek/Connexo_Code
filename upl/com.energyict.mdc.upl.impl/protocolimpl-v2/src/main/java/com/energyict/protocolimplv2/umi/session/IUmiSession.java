package com.energyict.protocolimplv2.umi.session;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.properties.UmiSessionProperties;
import com.energyict.protocolimplv2.umi.types.*;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.packet.payload.GetObjAccessRspPayload;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjPartRspPayload;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjRspPayload;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface IUmiSession {
    /**
     * Send a 'No Operation' packet.
     * @return
     */
    ResultCode noOperation();

    /**
     * Send an "Event Publish" command packet.
     * This notifies the remote device that events(s) have occurred.
     * @param events The bitfield of event(s) that caused the Event Publish.
     * @return
     */
    ResultCode eventPublish(int events) throws ProtocolException, Exception;

    /**
     * Send an "Event Subscribe" command packet.
     * This registers interest in certain events in the remote device.
     * @param events The bitfield of event(s) the the subscriber is interested in.
     * @param publishSecScheme The signature scheme desired on Event Publish commands.
     * @return
     */
    ResultCode eventSubscribe(int events, SecurityScheme publishSecScheme) throws IOException, GeneralSecurityException;

    /**
     * Send a 'Image Start' command packet.
     *
     * The Image Start command tells the Target that an
     * image is about to be transferred.  The command contains the length of the complete
     * image to be transferred.
     * @param totalLength The total length of the data to be sent, in bytes
     * @return
     */
    ResultCode imageStart(int totalLength) throws IOException, GeneralSecurityException;

    /**
     * Send a 'Image Data' command packet.
     *
     * The image is transferred from the source to the target using
     * a sequence of Image Data commands.
     *
     * The actions taken by the Target on reception of each data block are device
     * specific.
     * @param dataBlock raw data
     * @return
     */
    ResultCode imageData(byte[] dataBlock) throws ProtocolException, Exception;

    /**
     * Send a 'Image End' command packet.
     *
     * The Image End command marks the end of an image transfer.
     * Actions taken are device specific.
     * @param valid Whether the image just sent was valid or not TRUE=valid, FALSE=invalid
     * @return
     */
    ResultCode imageEnd(boolean valid) throws IOException, GeneralSecurityException;

    /**
     * Send a 'read object' packet.
     *
     * A read object command performs a read of an entire object.
     * For array objects, this reads the entire array. For structure objects,
     * all members are returned sequentially, with member 0 first. For
     * array-of-structure objects, all members of element 0 are returned first,
     * followed by all members of element 1, etc.
     * @param code the UMI code of the data object being requested
     * @return
     */
    Pair<ResultCode, ReadObjRspPayload> readObject(UmiCode code) throws IOException, GeneralSecurityException;

    /**
     * Send a 'read object part' packet.
     *
     * A Read Object Part command is used to read part of an object. This command can be used to:
     * .  read one or all members from a structure.
     * .  read one or all members from one or more consecutive array entries.
     * .  read all members from an entire array (ie, the same as Read Object).
     * This allows read access to arbitrarily large objects.
     * @param objectPart object which describes the UMI object part.
     * @return
     */
    Pair<ResultCode, ReadObjPartRspPayload> readObjectPart(UmiObjectPart objectPart) throws IOException, GeneralSecurityException;

    /**
     * Send a 'write object' packet.
     *
     * This performs a data object write using the value given. For array objects,
     * this writes the entire array.
     * @param code The UMI code of the data object being written
     * @param value The value of the data object being written.
     * @return
     */
    ResultCode writeObject(UmiCode code, byte[] value) throws IOException, GeneralSecurityException;

    /**
     * Send a 'write object part' packet.
     *
     * A 'Write Object Part' command is similar to a 'Write Object' command, but
     * this command has similar addressing capabilities to 'Read Object Part'.
     * Write Object Part allows access to arbitrarily large objects.
     * @param objectPart object which describes the UMI object part.
     * @param value The value of the data object being written.
     * @return
     */
    ResultCode writeObjectPart(UmiObjectPart objectPart, byte[] value) throws IOException, GeneralSecurityException;

    /**
     * Sends a Tunnel Data command packet.
     *
     * The Tunnel service allows a UMI Device to send a stream of unstructured
     * bytes to another UMI Device. This enables the devices to tunnel data
     * through a UMI product. This enables UMI products to easily support
     * end-to-end security links.
     * @param data
     * @return
     */
    Pair<ResultCode, IData> tunnelData(IData data);

    /**
     * Sends a Tunnel UMI command packet.
     *
     * Tunnel UMI enables one UMI packet to be nested within another UMI packet :
     * Command packet within another command packet.
     * Response packet within another response packet.
     * @param packet
     * @return
     */
    Pair<ResultCode, IData> tunnelUmi(IData packet);

    /**
     * Send a 'Get Object Access' command packet
     *
     * Get Object Access is used to get the read and write access roles for an
     * object.  Any Role other than guest is allowed to issue this command.
     * @param code The UMI code of the data object being requested
     * @return
     */
    Pair<ResultCode, GetObjAccessRspPayload> getObjectAccess(UmiCode code) throws IOException, GeneralSecurityException;

    /**
     * Send a 'set object read access' packet.
     *
     * Set Object Read Access is used to set the read access roles for an object.
     *
     * The Roles allowed to set the object read access can be determined using the
     * Get Object Access command.
     * @param code The UMI code of the data object
     * @param roles The roles to write.
     * @return
     */
    ResultCode setObjectReadAccess(UmiCode code, List<Role> roles) throws IOException, GeneralSecurityException;

    /**
     * Send a 'Set Object Write Access' packet
     *
     * Set Object Write Access is used to set the write access roles for an object.
     * The Roles allowed to set the object write access can be determined using
     * the Get Object Access command.
     * @param code The UMI code of the data object
     * @param roles The roles to write.
     * @return
     */
    ResultCode setObjectWriteAccess(UmiCode code, List<Role> roles) throws IOException, GeneralSecurityException;

    /**
     * Function for opening session if applicable
     * @return
     */
    ResultCode startSession() throws IOException, GeneralSecurityException;

    /**
     * Function for closing session if applicable
     * @return
     */
    ResultCode endSession() throws IOException, GeneralSecurityException;

    boolean isSessionEstablished();
}
