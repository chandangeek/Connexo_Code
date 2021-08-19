package com.energyict.protocolimplv2.umi.security.scheme2;

public enum CVCTagEnum {

   // Certificate fields
   CV_CERTIFICATE         (0x7F21, true),
   CERTIFICATE_BODY       (0x7F4E, true),
   PROFILE_IDENTIFIER     (0x5F29),
   PUBLIC_KEY             (0x7F49, true),
   HOLDER_REFERENCE       (0x5F20),
   HOLDER_AUTH_TEMPLATE   (0x7F4C, true),
   HOLDER_AUTHORIZATION   (0x5F4c),
   EFFECTIVE_DATE         (0x5F25),
   EXPIRATION_DATE        (0x5F24),
   SIGNATURE              (0x5F37),

   OID                    (0x06),
   CA_REFERENCE           (0x42),
   SERIAL_NUMBER          (0x5A),
   REQ_AUTHENTICATION     (0x67,   true),
   SEQUENCE               (0x30),
   BIT_STRING(0x03),

   // Fields for Holder Authorization Template
   ROLE_AND_ACCESS_RIGHTS (0x53),

   UMI_EFFECTIVE_DATE     (0xDF25),
   UMI_EXPIRATION_DATE    (0xDF24),

   // Fields for Public Key
   MODULUS                (0x81),
   EXPONENT               (0x82),
   // Only for EC
   COEFFICIENT_A          (0x82),
   COEFFICIENT_B          (0x83),
   BASE_POINT_G           (0x84),
   BASE_POINT_R_ORDER     (0x85),
   PUBLIC_POINT_Y         (0x86),
   COFACTOR_F             (0x87);

   
   private int value;
   private boolean isSequence;

   CVCTagEnum(final int value) {
      this(value, false);
   }

   CVCTagEnum(final int value, final boolean isSequence) {
      this.value = value;
      this.isSequence = isSequence;
   }

   public int getValue(){
      return value;
   }

   public boolean isSequence() {
      return isSequence;
   }

}
