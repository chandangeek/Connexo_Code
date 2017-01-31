package com.elster.jupiter.pki.impl;

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
                "osgi.command.function=keytypes"},
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
                .map(keytype -> Arrays.asList(keytype.getName(), keytype.getCryptographicType().name(), keytype.getAlgorithm()))
                .collect(toList());
        collect.add(0, Arrays.asList("name", "type", "algorithm"));
        MYSQL_PRINT.printTableWithHeader(collect);
    }
}
