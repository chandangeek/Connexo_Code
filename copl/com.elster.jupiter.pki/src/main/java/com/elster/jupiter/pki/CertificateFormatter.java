package com.elster.jupiter.pki;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public interface CertificateFormatter {
    default String x500FormattedName(String x500Name ) throws InvalidNameException {

        Map<String, Integer> rdsOrder = new HashMap<>();

        rdsOrder.put("CN", 1);
        rdsOrder.put("OU", 2);
        rdsOrder.put("O", 3);
        rdsOrder.put("L", 4);
        rdsOrder.put("ST", 5);
        rdsOrder.put("C", 6);

        return new LdapName(x500Name)
                .getRdns()
                .stream()
                .sorted(Comparator.comparing(rdn -> rdsOrder.getOrDefault(rdn.getType(), 7)))
                .map(Rdn::toString)
                .reduce((a, b) -> a + ", " + b)
                .map(X500Principal::new)
                .map(p -> p.getName(X500Principal.RFC1779))
                .get();
    }
}
