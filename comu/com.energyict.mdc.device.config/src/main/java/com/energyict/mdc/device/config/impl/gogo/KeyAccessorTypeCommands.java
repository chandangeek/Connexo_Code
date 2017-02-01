package com.energyict.mdc.device.config.impl.gogo;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 1/31/17.
 */
@Component(name = "KeyAccessorTypeCommands",
        service = KeyAccessorTypeCommands.class,
        property = {
                "osgi.command.scope=kat",
                "osgi.command.function=keyAccessorTypes",
                "osgi.command.function=createKeyAccessorType"
        },
        immediate = true)
public class KeyAccessorTypeCommands {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private DeviceConfigurationService deviceConfigurationService;
    private PkiService pkiService;
    private TransactionService transactionService;

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void keyAccessorTypes() {
        System.out.println("Usage: keyAccessorTypes <device type id>");
        System.out.println("Eg.  : keyAccessorTypes 153");
    }

    public void keyAccessorTypes(Long deviceTypeId) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> new RuntimeException("No such device type"));
        List<List<?>> kats = deviceType.getKeyAccessorTypes()
                .stream()
                .map(kat -> Arrays.asList(kat.getName(), kat.getKeyType().getName(), kat.getDuration(), kat.getKeyEncryptionMethod()))
                .collect(toList());
        kats.add(0, Arrays.asList("Name", "Key type", "Duration", "Encryption method"));
        MYSQL_PRINT.printTableWithHeader(kats);
    }

    public void createKeyAccessorType() {
        System.out.println("Usage: createKeyAccessorTypes <name> <device type id> <key type name> <encryption method> [duration in days]");
        System.out.println("Eg.  : createKeyAccessorTypes GUAK 153 AES128 SSM 365");
    }

    public void createKeyAccessorType(String name, long deviceTypeId, String keyTypeName, String keyEncryptionMethod, Integer ... duration) {
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new RuntimeException("No such device type"));
            KeyType keyType = pkiService.getKeyType(keyTypeName)
                    .orElseThrow(() -> new RuntimeException("No such key type"));
            KeyAccessorType.Builder builder = deviceType.addKeyAccessorType(name, keyType, keyEncryptionMethod)
                    .description("Created by gogo command");
            if (duration != null && duration.length >= 1) {
                builder.duration(TimeDuration.days(duration[0]));
            }
            builder.add();
            context.commit();
        }
    }
}
