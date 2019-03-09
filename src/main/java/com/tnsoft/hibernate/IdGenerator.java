package com.tnsoft.hibernate;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

public class IdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object obj) {
        Serializable id = session.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, session);
        return id != null ? id : UUID.randomUUID().toString();
    }
}
