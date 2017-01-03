package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocol.UnsupportedException;

import java.util.List;

/**
 * Created by cisac on 12/15/2016.
 */
public class DataProtectionFactory {

    /*
         object_definition ::= structure
        {
            class_id: long-unsigned,
            logical_name: octet-string,
            attribute_index: integer,
            data_index: long-unsigned,
            restriction: restriction_element
        }
     */
    public static Structure createObjectDefinition(Unsigned16 classId, OctetString logicalName, Integer8 attributeIndex, Unsigned16 dataIndex, Structure restrictionElement) {
        return new Structure(classId, logicalName, attributeIndex, dataIndex, restrictionElement);
    }

    /*
        restriction_element ::= structure
        {
            restriction_type: enum:
                (0) none,
                (1) restriction by date,
                (2) restriction by entry
            restriction_value: CHOICE
                {
                null-data, // no restrictions apply
                restriction_by_date,
                restriction_by_entry
                }
        }
     */
    public static Structure createRestrictionElement(RestrictionType restrictionType, Structure restrictionValue) {
        return new Structure(restrictionType.getTypeEnum(), restrictionValue);
    }

    public static NullData createNoRestrictionElement() {
        return new NullData();
    }

    /*
        restriction_by_date ::= structure
        {
            from_date: octet-string,
            to_date: octet-string
        }
     */
    public static Structure createRestrictionByDate(OctetString fromDate, OctetString toDate) {
        return new Structure(fromDate, toDate);
    }
    /*
        restriction_by_entry ::= structure
        {
            from_entry: double-long-unsigned,
            to_entry: double-long-unsigned
        }
     */
    public static Structure createRestrictionByEntry(Unsigned32 fromEntry, OctetString toEntry) {
        return new Structure(fromEntry, toEntry);
    }

    /*
        key_info_element ::= structure
        {
            key_info_type: enum:
                (0) identified_key,
                -- used with identified_key_info_options
                (1) wrapped_key,
                -- used with wrapped_key_info_options
                (2) agreed_key
                -- used with agreed_key_info_options
            key_info_options: CHOICE
                {
                identified_key_info_options,
                wrapped_key_info_options,
                agreed_key_info_options
                }
         }
     */
    public static Structure createKeyInfoElement(GeneralCipheringKeyType keyInfoType, AbstractDataType keyInfoOptions) {
        return new Structure(new TypeEnum(keyInfoType.getId()), keyInfoOptions);
    }
    /*
        agreed_key_info_options ::= structure
        {
            key_parameters: octet-string,
            key_ciphered_data: octet-string
        }
     */
    public static Structure createAgreedKeyInfoOptions(OctetString keyParameters, OctetString keyCipheredData) {
        return new Structure(keyParameters, keyCipheredData);
    }

    /*
        wrapped_key_info_options ::= structure
        {
            kek_id: enum:
                (0) master_key,
            key_ciphered_data: octet-string
        }
     */
    public static Structure createWrappedKeyInfoOptions(TypeEnum kekID, OctetString keyCipheredData) {
        return new Structure(kekID, keyCipheredData);
    }

    /*
    protection_parameters_element ::= structure
    {
        protection_type: enum:
            (0) authentication,
            (1) encryption,
            (2) authentication and encryption,
            (3) digital signature
        protection_options: structure
            {
            transaction_id: octet-string,
            originator_system_title: octet-string,
            recipient_system_title: octet-string,
            other_information: octet-string,
            key_info: key_info_element
            }
    }
     */
    public static Structure createProtectionParametersElement(ProtectionType protectionType, Structure protectionOptions) {
        return new Structure(protectionType.getTypeEnum(), protectionOptions);
    }

    /*
        protection_options: structure
        {
            transaction_id: octet-string,
            originator_system_title: octet-string,
            recipient_system_title: octet-string,
            other_information: octet-string,
            key_info: key_info_element
        }
     */
    public static Structure createProtectionOptions(OctetString transactionId, OctetString originatorSystemTitle, OctetString recipientSystemTitle, OctetString otherInformation, Structure keyInfoElement) {
        return new Structure(transactionId, originatorSystemTitle, recipientSystemTitle, otherInformation, keyInfoElement);
    }

    /*
        get_protected_attributes_request ::= structure
        {
            object_list: array object_definition,
            protection_parameters: array protection_parameters_element,
        }
     */
    public static Structure createProtectedAttributesGetRequest(Array objectDefinitionList, Array protectionParameters) {
        return new Structure(objectDefinitionList, protectionParameters);
    }

    /*
        set_protected_attributes_request ::= structure
        {
            object_list: array object_definition,
            protection_parameters: array protection_parameters_element,
            protected_attributes: octet-string
        }
     */
    public static Structure createProtectedAttributesSetRequest(Array objectDefinitionList, Array protectionParameters, OctetString protectedAttributes) {
        return new Structure(objectDefinitionList, protectionParameters, protectedAttributes);
    }

    /*
        invoke_protected_method_request ::= structure
        {
            object_method: object_method_definition,
            protection_parameters: array protection_parameters_element,
            protected_method_invocation_parameters: octet-string
        }
     */
    public static Structure createInvokeProtectedMethodRequest(Structure objectMethodDefinition, Array protectionParameters, OctetString protectedMethodInvocationParameters) {
        return new Structure(objectMethodDefinition, protectionParameters, protectedMethodInvocationParameters);
    }

    /*
        object_method_definition ::= structure
        {
            class_id: long-unsigned,
            logical_name: octet-string,
            method_index: integer,
        }
     */
    public static Structure createObjectMethodDefinition(Unsigned16 classId, OctetString logicalName, Integer8 methodIndex) {
        return new Structure(classId, logicalName, methodIndex);
    }


    /**
     *
     * @return an array that contains one element of type:
     * protection_parameters_element ::= structure
        {
            protection_type: enum:
                (0) authentication,
                (1) encryption,
                (2) authentication and encryption,
                (3) digital signature
            protection_options: structure
            {
                transaction_id: octet-string,
                originator_system_title: octet-string,
                recipient_system_title: octet-string,
                other_information: octet-string,
                key_info: key_info_element
            }
        }
     */
    public static Array createProtectionParametersArray(SecurityContext securityContext, List<ProtectionType> protectionLayers, GeneralCipheringKeyType generalCipheringKeyType) throws UnsupportedException {
        Array protectionParameters = new Array();
        for(ProtectionType protectionType: protectionLayers){
            Structure protectionOptions = getProtectionOptions(securityContext, protectionType, generalCipheringKeyType);
            Structure protectionParametersElement = createProtectionParametersElement(protectionType, protectionOptions);
            protectionParameters.addDataType(protectionParametersElement);
        }
        return protectionParameters;
    }

    /**
     *
     * @param securityContext
     * @param protectionType
     *@param generalCipheringKeyType  @return protection_options: structure
                {
                    transaction_id: octet-string,
                    originator_system_title: octet-string,
                    recipient_system_title: octet-string,
                    other_information: octet-string,
                    key_info: key_info_element
                }
     */
    public static Structure getProtectionOptions(SecurityContext securityContext, ProtectionType protectionType, GeneralCipheringKeyType generalCipheringKeyType) throws UnsupportedException {
        OctetString transactionID = OctetString.fromByteArray(securityContext.getTransactionId());
        OctetString originatorSystemTitle = OctetString.fromByteArray(securityContext.getSystemTitle());
        OctetString recipientSystemTitle = OctetString.fromByteArray(securityContext.getResponseSystemTitle());
        OctetString otherInformation = OctetString.fromByteArray(new byte[]{});//no other info
        Structure keyInfo = new Structure();
        if(protectionType.getId() != ProtectionType.DIGITAL_SIGNATURE.getId()){
            keyInfo = getKeyInfo(securityContext, generalCipheringKeyType);
        }

        Structure protectionOptions = DataProtectionFactory.createProtectionOptions(transactionID, originatorSystemTitle, recipientSystemTitle, otherInformation, keyInfo);

        return protectionOptions;
    }

    public static Structure getKeyInfo(SecurityContext securityContext, GeneralCipheringKeyType keyInfoType) throws UnsupportedException {
        OctetString keyCipheredData;
        switch (keyInfoType) {
            case AGREED_KEY:
                throw new UnsupportedException("Key agreement is not implemented for Data protection object");
//                OctetString keyParameters = OctetString.fromByteArray(new byte[]{(byte) GeneralCipheringKeyType.AgreedKeyTypes.ECC_CDH_1E1S.getId()});//TODO: fill proper value
//                keyCipheredData = OctetString.fromString("");//TODO: fill proper value
//                Structure agreedKeyInfoOptions = DataProtectionFactory.createAgreedKeyInfoOptions(keyParameters, keyCipheredData);//TODO: fill proper value
//                return DataProtectionFactory.createKeyInfoElement(GeneralCipheringKeyType.AGREED_KEY, agreedKeyInfoOptions);
            case IDENTIFIED_KEY:
                TypeEnum identifiedKeyOption = new TypeEnum(GeneralCipheringKeyType.IdentifiedKeyTypes.GLOBAL_UNICAST_ENCRYPTION_KEY.getId());//TODO: see if also broadcast will be used
                return DataProtectionFactory.createKeyInfoElement(GeneralCipheringKeyType.IDENTIFIED_KEY, identifiedKeyOption);
            case WRAPPED_KEY:
                TypeEnum kekID = new TypeEnum(GeneralCipheringKeyType.WrappedKeyTypes.MASTER_KEY.getId());
                OctetString wrappedKey = OctetString.fromByteArray(securityContext.getWrappedKey(false));
                Structure wrappedKeyInfoOptions = DataProtectionFactory.createWrappedKeyInfoOptions(kekID, wrappedKey);
                return DataProtectionFactory.createKeyInfoElement(GeneralCipheringKeyType.WRAPPED_KEY, wrappedKeyInfoOptions);
        }
        return null;
    }
}
