package com.tnsoft.hibernate;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.SequenceGenerator;

import java.io.Serializable;

public class SeqGenerator extends SequenceGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object obj) {
        Number id = (Number) session.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, session);
        return id != null && id.longValue() > 0L ? id : super.generate(session, obj);
    }
}
