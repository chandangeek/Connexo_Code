package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class RegisteredCustomPropertySetImpl implements RegisteredCustomPropertySet, PersistenceAware {

    enum FieldNames {
        LOGICAL_ID("logicalId"),
        SYSTEM_DEFINED("systemDefined"),
        VIEW_PRIVILEGES("viewPrivilegesBits"),
        EDIT_PRIVILEGES("editPrivilegesBits");

        private final String name;
        FieldNames(String name) {
            this.name = name;
        }

        String javaName() {
            return name;
        }

    }

    private final DataModel dataModel;
    private final ThreadPrincipalService threadPrincipalService;
    private final ServerCustomPropertySetService customPropertySetService;

    @Inject
    RegisteredCustomPropertySetImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService, ServerCustomPropertySetService customPropertySetService) {
        super();
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
        this.customPropertySetService = customPropertySetService;
    }

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String logicalId;
    private boolean systemDefined;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE__NULL+"}")
    private long viewPrivilegesBits;
    private EnumSet<ViewPrivilege> viewPrivileges = EnumSet.noneOf(ViewPrivilege.class);
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE__NULL+"}")
    private long editPrivilegesBits;
    private EnumSet<EditPrivilege> editPrivileges = EnumSet.noneOf(EditPrivilege.class);
    private CustomPropertySet customPropertySet;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    public RegisteredCustomPropertySetImpl initialize(CustomPropertySet customPropertySet, boolean systemDefined, Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges) {
        this.logicalId = customPropertySet.getId();
        this.systemDefined = systemDefined;
        this.customPropertySet = customPropertySet;
        this.addAllViewPrivileges(viewPrivileges);
        this.addAllEditPrivileges(editPrivileges);
        return this;
    }

    @Override
    public void postLoad() {
        this.customPropertySet =
                this.customPropertySetService
                    .findRegisteredCustomPropertySet(this.logicalId)
                    .orElse(null);
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
        return this.customPropertySet;
    }

    String getLogicalId() {
        return logicalId;
    }

    @Override
    public String getCustomPropertySetId() {
        if (this.isActive()) {
            return this.getCustomPropertySet().getId();
        } else {
            return this.logicalId;
        }
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
        update();
    }

    @Override
    public boolean isEditableByCurrentUser() {
        return currentUserIsAllowedToEditCustomPropertySet();
    }

    @Override
    public boolean isViewableByCurrentUser() {
        return currentUserIsAllowedToViewCustomPropertySet();
    }

    private List<String> getCurrentUserPrivileges() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return new ArrayList<>();
        }
        return ((User) principal).getPrivileges(threadPrincipalService.getApplicationName()).stream().map(Privilege::getName).collect(Collectors.toList());
    }

    private boolean currentUserIsAllowedToViewCustomPropertySet() {
        return this.getViewPrivileges().isEmpty()
            || this.currentUserIsAllowedTo(
                    this.getViewPrivileges()
                        .stream()
                        .map(ViewPrivilege::getPrivilege)
                        .collect(Collectors.toSet()));
    }

    private boolean currentUserIsAllowedToEditCustomPropertySet() {
        return this.getEditPrivileges().isEmpty()
            || this.currentUserIsAllowedTo(
                    this.getEditPrivileges()
                        .stream()
                        .map(EditPrivilege::getPrivilege)
                        .collect(Collectors.toSet()));
    }

    private boolean currentUserIsAllowedTo(Set<String> privileges) {
        Set<String> currentUserPrivileges = new HashSet<>(this.getCurrentUserPrivileges());
        currentUserPrivileges.retainAll(privileges);
        return !currentUserPrivileges.isEmpty();
    }

    @Override
    public boolean isActive() {
        return this.customPropertySet != null;
    }

    void create() {
        Save.CREATE.validate(this.dataModel, this);
        Save.CREATE.save(this.dataModel, this);
    }

    private void update() {
        Save.UPDATE.save(this.dataModel, this);
    }

    void delete() {
        this.dataModel.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        RegisteredCustomPropertySetImpl that = (RegisteredCustomPropertySetImpl) o;
        return id == that.getId() && logicalId.equals(that.logicalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, logicalId);
    }

}
