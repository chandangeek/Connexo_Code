package com.elster.jupiter.orm.associations.impl;

import static com.elster.jupiter.util.conditions.Where.where;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalAspect;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;

public abstract class AbstractPersistentTemporalAspect<T extends Effectivity> implements TemporalAspect<T> {
	private final ForeignKeyConstraintImpl constraint;
	private final DataMapperImpl<T> dataMapper;
	private final Object owner;
	private final Condition baseCondition;
	
	
	public AbstractPersistentTemporalAspect(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner) {
		this.constraint = constraint;
		this.dataMapper = dataMapper;
		this.owner = owner;
		this.baseCondition = where(this.constraint.getFieldName()).isEqualTo(owner);
	}

	private List<T> postProcess (List<T> queryResult) {
		for (T each : queryResult) {
			DomainMapper.FIELDSTRICT.set(each, constraint.getFieldName(), owner);
		}
		return queryResult;
	}
	
	@Override
	public List<T> effective(Interval interval) {
		return postProcess(dataMapper.select(baseCondition.and(Where.where("interval").isEffective(interval)),"interval.start"));
	}

	List<T> allEffective(Date date) {
		return postProcess(dataMapper.select(baseCondition.and(Where.where("interval").isEffective(date)),"interval.start"));
	}
	
	@Override
	public List<T> all() {
		return postProcess(dataMapper.select(baseCondition, "interval.start"));
	}

	public DataMapperImpl<T> getDataMapper() {
		return dataMapper;
	}
	
	public boolean add(T element) {
		if (constraint.isComposition()) {
			try {
				dataMapper.getWriter().persist(element);
			} catch (SQLException ex) {
				throw new UnderlyingSQLFailedException(ex);
			}
		}
		return true;
	}
	
	public boolean remove(T element) {
		if (constraint.isComposition()) {
			try {
				dataMapper.getWriter().remove(element);
			} catch (SQLException ex) {
				throw new UnderlyingSQLFailedException(ex);
			}
		}
		return true;
	}

}
