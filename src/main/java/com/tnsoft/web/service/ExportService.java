package com.tnsoft.web.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public interface ExportService {

    void exportToPDF(int expressId, Date start, Date end, int flag, HttpServletRequest request, HttpServletResponse response);

    void exportToXLS(int expressId, Date start, Date end, int flag, HttpServletRequest request, HttpServletResponse response);

    void exportCalibrationToPDF(String tagNo, HttpServletRequest request, HttpServletResponse response);
}
