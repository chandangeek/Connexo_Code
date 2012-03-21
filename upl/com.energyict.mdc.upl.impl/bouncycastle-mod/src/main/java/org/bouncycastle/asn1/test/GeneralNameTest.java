package org.bouncycastle.asn1.test;

import org.bouncycastle.util.test.SimpleTest;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.asn1.x509.GeneralName;

public class GeneralNameTest
    extends SimpleTest
{
    private static final byte[] ipv4 = Hex.decode("87040a090800");
    private static final byte[] ipv4WithMask = Hex.decode("87080a090800ffffff00");

    private static final byte[] ipv6a = Hex.decode("871020010db885a308d313198a2e03707334");
    private static final byte[] ipv6b = Hex.decode("871020010db885a3000013198a2e03707334");
    private static final byte[] ipv6c = Hex.decode("871000000000000000000000000000000001");
    private static final byte[] ipv6d = Hex.decode("871020010db885a3000000008a2e03707334");
    private static final byte[] ipv6e = Hex.decode("871020010db885a3000000008a2e0a090800");
    private static final byte[] ipv6f = Hex.decode("872020010db885a3000000008a2e0a090800ffffffffffff00000000000000000000");
    private static final byte[] ipv6g = Hex.decode("872020010db885a3000000008a2e0a090800ffffffffffffffffffffffffffffffff");
    private static final byte[] ipv6h = Hex.decode("872020010db885a300000000000000000000ffffffffffff00000000000000000000");
    
    public String getName()
    {
        return "GeneralName";
    }

    public void performTest()
        throws Exception
    {
        GeneralName nm = new GeneralName(GeneralName.iPAddress, "10.9.8.0");
        if (!Arrays.areEqual(nm.getEncoded(), ipv4))
        {
            fail("ipv4 encoding failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "10.9.8.0/255.255.255.0");
        if (!Arrays.areEqual(nm.getEncoded(), ipv4WithMask))
        {
            fail("ipv4 with netmask 1 encoding failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "10.9.8.0/24");
        if (!Arrays.areEqual(nm.getEncoded(), ipv4WithMask))
        {
            fail("ipv4 with netmask 2 encoding failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3:08d3:1319:8a2e:0370:7334");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6a))
        {
            fail("ipv6 with netmask encoding failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::1319:8a2e:0370:7334");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6b))
        {
            fail("ipv6b encoding failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "::1");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6c))
        {
            fail("ipv6c failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:0370:7334");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6d))
        {
            fail("ipv6d failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:10.9.8.0");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6e))
        {
            fail("ipv6e failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:10.9.8.0/ffff:ffff:ffff::0000");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6f))
        {
            fail("ipv6f failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:10.9.8.0/128");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6g))
        {
            fail("ipv6g failed");
        }

        nm = new GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/48");
        if (!Arrays.areEqual(nm.getEncoded(), ipv6h))
        {
            fail("ipv6h failed");
        }
    }

    public static void main(
        String[] args)
    {
        runTest(new GeneralNameTest());
    }
}
