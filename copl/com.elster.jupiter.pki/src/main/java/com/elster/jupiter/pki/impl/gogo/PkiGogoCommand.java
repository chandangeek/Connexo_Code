/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.util.gogo.MysqlPrint;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component(name = "com.elster.jupiter.pki.gogo.impl.PkiGogoCommand",
        service = PkiGogoCommand.class,
        property = {"osgi.command.scope=pki",
                "osgi.command.function=keytypes",
                "osgi.command.function=certificateStore"
        },
        immediate = true)
public class PkiGogoCommand {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private volatile PkiService pkiService;

    public PkiGogoCommand() {
    }

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    public void keytypes() {
        List<List<?>> collect = pkiService.findAllKeyTypes()
                .stream()
                .map(keytype -> Arrays.asList(keytype.getId(), keytype.getName(), keytype.getCryptographicType().name(), keytype.getKeyAlgorithm()))
                .collect(toList());
        collect.add(0, Arrays.asList("id", "name", "type", "algorithm"));
        MYSQL_PRINT.printTableWithHeader(collect);
    }

    public void certificateStore() {
        List<List<?>> certs = pkiService.findAllCertificates()
                .stream()
                .map(cert -> Arrays.asList(cert.getAlias(), cert.getCertificate().isPresent()))
                .collect(toList());
        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Alias", "Certificate"), certs);
    }

}
