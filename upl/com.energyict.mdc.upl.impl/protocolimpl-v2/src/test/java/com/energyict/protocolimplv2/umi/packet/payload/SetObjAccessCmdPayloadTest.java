package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.Role;
import com.energyict.protocolimplv2.umi.types.UmiCode;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SetObjAccessCmdPayloadTest {
    @Test
    public void createSetObjAccessCmdPayload() {
        UmiCode code = new UmiCode("umi.0.1.2.3");
        List<Role> roles = new ArrayList<Role>(){{add(Role.GUEST); add(Role.MANUFACTURER);}};
        SetObjAccessCmdPayload payload = new SetObjAccessCmdPayload(code, roles);
        assertEquals(code, payload.getUmiCode());
        assertEquals(roles, payload.getRoles());
        assertEquals(65, payload.getRolesBitfield());
    }

    @Test
    public void recreateSetObjAccessCmdPayloadFromRaw() {
        UmiCode code = new UmiCode("umi.0.1.2.3");
        List<Role> roles = new ArrayList<Role>(){{add(Role.GUEST); add(Role.MANUFACTURER);}};
        SetObjAccessCmdPayload payload = new SetObjAccessCmdPayload(code, roles);
        SetObjAccessCmdPayload payload1 = new SetObjAccessCmdPayload(payload.getRaw());

        assertEquals(payload.getUmiCode(), payload1.getUmiCode());
        assertEquals(payload.getRoles(), payload1.getRoles());
        assertEquals(payload.getRolesBitfield(), payload1.getRolesBitfield());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        SetObjAccessCmdPayload payload = new SetObjAccessCmdPayload(new byte[0]);
    }
} 
