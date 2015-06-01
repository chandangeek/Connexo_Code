package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (14:39)
 */
@Component(name = "com.energyict.mdc.device.data.search",
        service = SearchDevicesGogoCommand.class,
        property = {
                "osgi.command.scope=mdc.device.search",
                "osgi.command.function=lo",
                "osgi.command.function=listOptions",
                "osgi.command.function=search"},
        immediate = true)
@SuppressWarnings("unused")
public class SearchDevicesGogoCommand {

    private volatile SearchService searchService;
    private volatile TransactionService transactionService;

    public SearchDevicesGogoCommand() {
        super();
    }

    @Inject
    public SearchDevicesGogoCommand(SearchService searchService, TransactionService transactionService) {
        this();
        this.searchService = searchService;
        this.transactionService = transactionService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate() {
        System.out.println("Device meta search layer is open for business");
    }

    /**
     * Alias for listOptions.
     * @see #listOptions()
     */
    @SuppressWarnings("unused")
    public void lo() {
        this.listOptions();
    }

    /**
     * List all the search options.
     */
    @SuppressWarnings("unused")
    public void listOptions() {
        System.out.println("Usage mdc.device.search:search <condition>[ <condition]*");
        System.out.println("      where condition is: <key>=<value>");
        System.out.println("      and key is one of the following");
        System.out.println("         " + this.getDeviceSearchDomain().getProperties().stream().map(this::toString).collect(Collectors.joining("\n         ")));
        System.out.println("      and value is the String representation of the type of the key that was used");
        System.out.println("      Note that when the value type is String, wildcards * and ? are allowed");
    }

    private String toString(SearchableProperty property) {
        PropertySpec spec = property.getSpecification();
        if (spec.isReference()) {
            return spec.getName() + " reference to " + spec.getValueFactory().getValueType().getName();
        }
        else {
            return spec.getName() + " of type " + spec.getValueFactory().getValueType().getName();
        }
    }

    private SearchDomain getDeviceSearchDomain() {
        return this.searchService.getDomains()
                .stream()
                .filter(p -> p.supports(Device.class))
                .findAny()
                .orElseThrow(() -> new RuntimeException("SearchDomain for com.energyict.mdc.device.data.Device not found"));
    }

    @SuppressWarnings("unused")
    public void search(String... conditions) {
        SearchBuilder<Device> builder = this.searchService.search(Device.class);
        Stream
            .of(conditions)
            .map(this::toKeyValuePair)
            .forEach(p -> this.addCondition(builder, p.getFirst(), p.getLast()));
        this.transactionService.execute(
                VoidTransaction.of(() ->
                        System.out.println(
                                builder
                                        .complete()
                                        .stream()
                                        .map(this::toString)
                                        .collect(Collectors.joining("\n")))));
    }

    private SearchBuilder<Device> addCondition(SearchBuilder<Device> builder, String key, Object value) {
        if (this.isWildCard(value)) {
            return builder.where(key).like((String) value);
        }
        else {
            return builder.where(key).isEqualTo(value);
        }
    }

    private boolean isWildCard(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            return s.contains("*") || s.contains("?");
        }
        else {
            return false;
        }
    }

    private Pair<String, Object> toKeyValuePair(String condition) {
        String[] keyAndValue = condition.split("=");
        if (keyAndValue.length == 2) {
            return Pair.of(keyAndValue[0], keyAndValue[1]);
        }
        else {
            throw new IllegalArgumentException("All key value conditions must be written as: <key>=<value>");
        }
    }

    public void complexSearch(String mRID) {
        System.out.println(
                this.searchService
                        .search(Device.class)
                        .where("mRID").isEqualTo(mRID)
                        .and("statusName").in(
                            DefaultState.IN_STOCK.getKey(),
                            DefaultState.DECOMMISSIONED.getKey())
                        .and("deviceConfigId").isEqualTo(97L)
                        .complete()
                        .stream()
                        .map(this::toString)
                        .collect(Collectors.joining("\n")));
    }

    @SuppressWarnings("unused")
    public void complexSearch2(String mRID) {
        Condition condition =
                where("mRID").isEqualTo(mRID)
                        .and(where("statusName").isEqualTo(DefaultState.IN_STOCK.getKey()))
                        .and(where("deviceConfigId").isEqualTo(97L));
        Finder<Device> deviceFinder = this.searchService.search(Device.class, condition);
        deviceFinder
                .stream()
                .map(this::toString)
                .collect(Collectors.joining("\n"));
    }

    private String toString(Device device) {
        return device.getmRID() + " in state " + device.getState().getName();
    }

}