package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.LabelCategory;

public enum TableSpecs {

    FAV_FAVORITEDEVICEGROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<FavoriteDeviceGroup> table = dataModel.addTable(name(), FavoriteDeviceGroup.class);
            table.map(FavoriteDeviceGroupImpl.class);

            Column userColumn = table.column("USERID").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column deviceGroupColumn = table.column("ENDDEVICEGROUP").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();

            table.primaryKey("FAV_PK_FAVDEVICEGROUP").on(userColumn, deviceGroupColumn).add();
            table.foreignKey("FAV_FK_FAVDEVICEGROUP_USER").on(userColumn).references("USR", "USR_USER").map("user").add();
            table.foreignKey("FAV_FK_FAVDEVICEGROUP_GROUP").on(deviceGroupColumn).references("MTG", "MTG_ED_GROUP").map("endDeviceGroup").onDelete(DeleteRule.CASCADE).add();
        }
    },

    FAV_LABELCATEGORY {
        @Override
        void addTo(DataModel dataModel) {
            Table<LabelCategory> table = dataModel.addTable(name(), LabelCategory.class);
            table.map(LabelCategoryImpl.class);
    
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("TRANSLATIONKEY").varChar().map("translationKey").add();
    
            table.primaryKey("FAV_PK_LABELCATEGORY").on(name).add();
        }
    },
    
    FAV_DEVICELABEL {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceLabel> table = dataModel.addTable(name(), DeviceLabel.class);
            table.map(DeviceLabelImpl.class);

            Column userColumn = table.column("USERID").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column deviceColumn = table.column("DEVICE").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column categoryColumn = table.column("LABELCATEGORY").varChar(Table.NAME_LENGTH).notNull().add();

            table.column("CREATETIME").number().notNull().map("creationDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("LABELCOMMENT").type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();

            table.primaryKey("FAV_PK_DEVICELABEL").on(userColumn, deviceColumn, categoryColumn).add();
            table.foreignKey("FAV_FK_DEVICELABEL_USER").on(userColumn).references("USR", "USR_USER").map("user").add();
            table.foreignKey("FAV_FK_DEVICELABEL_DEVICE").on(deviceColumn).references("DDC", "DDC_DEVICE").map("device").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FAV_FK_DEVICELABEL_CATEGORY").on(categoryColumn).references(TableSpecs.FAV_LABELCATEGORY.name()).map("labelCategory").add();
        }
    };

    abstract void addTo(DataModel dataModel);

}
