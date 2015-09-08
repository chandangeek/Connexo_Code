package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.favorites.FavoritesService;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 19/01/2015
 * Time: 17:30
 */
@Component(name = "com.energyict.ddr.favorites.label", service = InstallService.class, immediate = true, property = {"name=FLI"})
public class FavoritesLabelInstaller implements InstallService {

    private final Logger logger = Logger.getLogger(FavoritesLabelInstaller.class.getName());
    private volatile FavoritesService favoritesService;

    @Reference
    public void setFavoritesService(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Override
    public void install() {
        createLabelCategories();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(NlsService.COMPONENTNAME, FavoritesService.COMPONENTNAME);
    }

    private void createLabelCategories() {
        try {
            favoritesService.createLabelCategory(DefaultTranslationKey.MDC_LABEL_CATEGORY_FAVORITES.getKey());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
