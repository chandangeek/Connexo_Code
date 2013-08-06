package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.UsagePoint;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UsagePointInfos {
	public int total;
	public List<UsagePointInfo> usagePoints = new ArrayList<>();
	
	UsagePointInfos() {		
	}
	
	UsagePointInfos(UsagePoint usagePoint) {
		add(usagePoint);		
	}
	
	UsagePointInfos(Iterable<? extends UsagePoint> usagePoints) {
		addAll(usagePoints);
	}
	
	UsagePointInfo add(UsagePoint usagePoint) {
		UsagePointInfo result = new UsagePointInfo(usagePoint);
		usagePoints.add(result);
		total++;
		return result;
	}
	
	void addAll(Iterable<? extends UsagePoint> usagePoints) {
		for (UsagePoint each : usagePoints) {
			add(each);
		}
	}
	
	void addServiceLocationInfo() {
		for (UsagePointInfo each : usagePoints) {
			each.addServiceLocationInfo();
		}		
	}
	
}
