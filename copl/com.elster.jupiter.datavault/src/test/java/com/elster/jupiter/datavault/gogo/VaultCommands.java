package com.elster.jupiter.datavault.gogo;

import com.elster.jupiter.datavault.SecretService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Copyrights EnergyICT
 * Date: 17/06/2014
 * Time: 18:00
 */
@Component(name = "com.elster.jupiter.playground.vault",
        service = VaultCommands.class,
        property = {"osgi.command.scope=vault", "osgi.command.function=encrypt", "osgi.command.function=decrypt"},
        immediate = true)
public class VaultCommands {

    private volatile SecretService secretService;

    @Activate
    public void activate(BundleContext context) {
    }

    @Reference
    public void setSecretService(SecretService secretService) {
        this.secretService = secretService;
    }

    public String encrypt(String string) {
        return secretService.encrypt(string.getBytes());
    }

    public String decrypt(String string) {
        return new String(secretService.decrypt(string));
    }


}
