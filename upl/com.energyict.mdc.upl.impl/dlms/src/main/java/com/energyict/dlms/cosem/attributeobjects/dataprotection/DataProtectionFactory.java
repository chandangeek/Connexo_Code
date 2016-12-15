package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.axrdencoding.*;

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
    public Structure createObjectDefinition(Unsigned16 classId, OctetString logicalName, Integer8 attributeIndex, Unsigned16 dataIndex, Structure restrictionElement) {
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
    public Structure createRestrictionElement(RestrictionType restrictionType, Structure restrictionValue) {
        return new Structure(restrictionType.getTypeEnum(), restrictionValue);
    }

    public NullData createNoRestrictionElement() {
        return new NullData();
    }

    /*
        restriction_by_date ::= structure
        {
            from_date: octet-string,
            to_date: octet-string
        }
     */
    public Structure createRestrictionByDate(OctetString fromDate, OctetString toDate) {
        return new Structure(fromDate, toDate);
    }
    /*
        restriction_by_entry ::= structure
        {
            from_entry: double-long-unsigned,
            to_entry: double-long-unsigned
        }
     */
    public Structure createRestrictionByEntry(Unsigned32 fromEntry, OctetString toEntry) {
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
    public Structure createKeyInfoElement(KeyInfoType keyInfoType, AbstractDataType keyInfoOptions) {
        return new Structure(keyInfoType.getTypeEnum(), keyInfoOptions);
    }
    /*
        agreed_key_info_options ::= structure
        {
            key_parameters: octet-string,
            key_ciphered_data: octet-string
        }
     */
    public Structure createAgreedKeyInfoOptions(OctetString keyParameters, OctetString keyCipheredData) {
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
    public Structure createWrappedKeyInfoOptions(OctetString keyCipheredData) {
        return new Structure(new TypeEnum(0), keyCipheredData);
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
    public Structure createProtectionParametersElement(ProtectionType protectionType, Structure protectionOptions) {
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
    public Structure createProtectionOptions(OctetString transactionId, OctetString originatorSystemTitle, OctetString recipientSystemTitle, OctetString otherInformation, Structure keyInfoElement) {
        return new Structure(transactionId, originatorSystemTitle, recipientSystemTitle, otherInformation, keyInfoElement);
    }

    public Structure createProtectedAttributesGetRequest(Array objectDefinitionList, Array protectionParameters) {
        return new Structure(objectDefinitionList, protectionParameters);
    }

    public Structure createProtectedAttributesSetRequest(Array objectDefinitionList, Array protectionParameters, OctetString protectedAttributes) {
        return new Structure(objectDefinitionList, protectionParameters, protectedAttributes);
    }

    public Structure createInvokeProtectedMethodRequest(Structure objectMethodDefinition, Array protectionParameters, OctetString protectedMethodInvocationParameters) {
        return new Structure(objectMethodDefinition, protectionParameters, protectedMethodInvocationParameters);
    }

    public Structure createObjectMethodDefinition(Unsigned16 classId, OctetString logicalName, Integer8 methodIndex) {
        return new Structure(classId, logicalName, methodIndex);
    }
}
