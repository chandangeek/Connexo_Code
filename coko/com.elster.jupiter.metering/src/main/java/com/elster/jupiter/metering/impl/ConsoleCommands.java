package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component(name="com.elster.jupiter.metering.console", service = ConsoleCommands.class, property = {"osgi.command.scope=metering", "osgi.command.function=printDdl", "osgi.command.function=createGroup", "osgi.command.function=updateGroup"}, immediate = true)
public class ConsoleCommands {

    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    public void printDdl() {
        try {
            for (Table table : Bus.getOrmClient().getDataModel().getTables()) {
                for (String s : table.getDdl()) {
                    System.out.println(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createGroup(String name, long... ids) {
        final EnumeratedUsagePointGroup group = new EnumeratedUsagePointGroupImpl();
        final List<EnumeratedUsagePointGroup.Entry> entries = new ArrayList<>();
        group.setName(name);
        for (long id : ids) {
            Optional<UsagePoint> usagePoint = Bus.getOrmClient().getUsagePointFactory().get(id);
            if (usagePoint.isPresent()) {
                EnumeratedUsagePointGroup.Entry entry = group.add(usagePoint.get(), Interval.startAt(Bus.getClock().now()));
                entries.add(entry);
            }
        }
        Optional<User> user = Bus.getUserService().findUser("batch executor");
        threadPrincipalService.set(user.get());
        try {
            transactionService.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    group.save();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void updateGroup(final String name, final long... ids) {
        Optional<User> user = Bus.getUserService().findUser("batch executor");
        threadPrincipalService.set(user.get());
        try {
            transactionService.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    List<EnumeratedUsagePointGroup> found = Bus.getOrmClient().getEnumeratedUsagePointGroupFactory().find("name", name);
                    List<Long> usagePointIds = new ArrayList<>(ids.length);
                    for (long id : ids) {
                        usagePointIds.add(id);
                    }
                    if (found.size() == 1) {
                        EnumeratedUsagePointGroup group = found.get(0);
                        Date now = Bus.getClock().now();
                        DiffList<UsagePoint> diffs = ArrayDiffList.fromOriginal(group.getMembers(now));
                        for (UsagePoint usagePoint : group.getMembers(now)) {
                            if (!usagePointIds.contains(usagePoint.getId())) {
                                diffs.remove(usagePoint);
                            } else {
                                usagePointIds.remove(usagePoint.getId());
                            }
                        }
                        for (Long usagePointId : usagePointIds) {
                            Optional<UsagePoint> usagePoint = Bus.getOrmClient().getUsagePointFactory().get(usagePointId);
                            if (usagePoint.isPresent()) {
                                EnumeratedUsagePointGroup.Entry entry = group.add(usagePoint.get(), Interval.startAt(now));
                            }
                        }
                        for (UsagePoint usagePoint : diffs.getRemovals()) {
                            group.endMembership(usagePoint, now);
                        }
                        group.save();
                    } else if (found.isEmpty()) {
                        System.out.println("No such group.");
                    } else {
                        System.out.println("Multiple groups by that name");
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
