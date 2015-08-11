package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.PersistenceAware;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Provides an implementation for the {@link RegisteredCustomPropertySet} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:25)
 */
public class RegisteredCustomPropertySetImpl implements RegisteredCustomPropertySet, PersistenceAware {

    enum FieldNames {
        LOGICAL_ID("logicalId");

        private final String name;
        FieldNames(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

    }
    private final DataModel dataModel;
    private final CustomPropertySetServiceImpl customPropertySetService;

    @Inject
    public RegisteredCustomPropertySetImpl(DataModel dataModel, CustomPropertySetServiceImpl customPropertySetService) {
        super();
        this.dataModel = dataModel;
        this.customPropertySetService = customPropertySetService;
    }

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String logicalId;
    private long viewPrivilegesBits;
    private EnumSet<ViewPrivilege> viewPrivileges;
    private long editPrivilegesBits;
    private EnumSet<EditPrivilege> editPrivileges;
    private Optional<CustomPropertySet> customPropertySet;

    public RegisteredCustomPropertySetImpl initialize(CustomPropertySet customPropertySet, Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges) {
        this.logicalId = customPropertySet.getId();
        this.customPropertySet = Optional.of(customPropertySet);
        this.addAllViewPrivileges(viewPrivileges);
        this.addAllEditPrivileges(editPrivileges);
        return this;
    }

    @Override
    public void postLoad() {
        this.customPropertySet = this.customPropertySetService.findActiveCustomPropertySet(this.logicalId);
        this.postLoadPrivileges();
    }

    private void postLoadPrivileges() {
        this.postLoadViewPrivileges();
        this.postLoadEditPrivileges();
    }

    private void postLoadViewPrivileges() {
        int mask = 1;
        for (ViewPrivilege privilege : ViewPrivilege.values()) {
            if ((this.viewPrivilegesBits & mask) != 0) {
                // The bit corresponding to the current privilege is set so add it to the set.
                this.viewPrivileges.add(privilege);
            }
            mask = mask * 2;
        }
    }

    private void postLoadEditPrivileges() {
        int mask = 1;
        for (EditPrivilege privilege : EditPrivilege.values()) {
            if ((this.editPrivilegesBits & mask) != 0) {
                // The bit corresponding to the current privilege is set so add it to the set.
                this.editPrivileges.add(privilege);
            }
            mask = mask * 2;
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public CustomPropertySet getCustomPropertySet() {
        /* this method is intended to be used by client code
         * and RegisteredCustomPropertySet that are not active
         * are never returned to the client side,
         * therefore it is safe to use the get method
         * as there will always be a value present when this method is called. */
        return this.customPropertySet.get();
    }

    @Override
    public Set<ViewPrivilege> getViewPrivileges() {
        return EnumSet.copyOf(this.viewPrivileges);
    }

    private void clearViewPrivileges() {
        this.viewPrivilegesBits = 0;
        this.viewPrivileges = EnumSet.noneOf(ViewPrivilege.class);
    }

    private void add(ViewPrivilege privilege) {
        this.viewPrivilegesBits |= (1L << privilege.ordinal());
        this.viewPrivileges.add(privilege);
    }

    private void addAllViewPrivileges(Set<ViewPrivilege> viewPrivileges) {
        viewPrivileges.forEach(this::add);
    }

    @Override
    public Set<EditPrivilege> getEditPrivileges() {
        return EnumSet.copyOf(this.editPrivileges);
    }

    private void clearEditPrivileges() {
        this.editPrivilegesBits = 0;
        this.editPrivileges = EnumSet.noneOf(EditPrivilege.class);
    }

    private void add(EditPrivilege privilege) {
        this.editPrivilegesBits |= (1L << privilege.ordinal());
        this.editPrivileges.add(privilege);
    }

    private void addAllEditPrivileges(Set<EditPrivilege> editPrivileges) {
        editPrivileges.forEach(this::add);
    }

    @Override
    public void updatePrivileges(Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges) {
        this.clearViewPrivileges();
        this.addAllViewPrivileges(viewPrivileges);
        this.clearEditPrivileges();
        this.addAllEditPrivileges(editPrivileges);
    }

    boolean isActive() {
        return this.customPropertySet.isPresent();
    }

    void save() {
        Save.CREATE.validate(this.dataModel, this);
        Save.CREATE.save(this.dataModel, this);
    }

    void delete() {
        this.dataModel.remove(this);
    }

}