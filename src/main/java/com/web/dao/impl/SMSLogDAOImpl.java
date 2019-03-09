package com.web.dao.impl;

import com.tnsoft.hibernate.model.SMSLog;
import com.tnsoft.web.dao.SMSLogDAO;
import org.springframework.stereotype.Repository;

@Repository("smsLogDAO")
public class SMSLogDAOImpl extends BaseDAOImpl<SMSLog> implements SMSLogDAO {
}
