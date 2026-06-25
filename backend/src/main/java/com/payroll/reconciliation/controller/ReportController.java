package com.payroll.reconciliation.controller;

import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for generating and exporting payroll reports.
 * Supports monthly, quarterly, yearly, and employee balance reports in Excel and PDF formats.
 *
 * @author Payroll Reconciliation Team
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    /**
     * Constructs the controller with the required report service.
     *
     * @param reportService the service handling report generation
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generates a monthly report for the specified year and month.
     *
     * @param year   the report year
     * @param month  the report month (1-12)
     * @param format the export format ("xlsx" or "pdf"), defaults to "xlsx"
     * @return the report file as a downloadable response
     */
    @GetMapping("/monthly")
    public ResponseEntity<byte[]> monthly(@RequestParam int year, @RequestParam int month, @RequestParam(defaultValue = "xlsx") String format) {
        return export(reportService.monthly(year, month), "monthly-report-" + year + "-" + month, format);
    }

    /**
     * Generates a quarterly report for the specified year and quarter.
     *
     * @param year    the report year
     * @param quarter the report quarter (1-4)
     * @param format  the export format ("xlsx" or "pdf"), defaults to "xlsx"
     * @return the report file as a downloadable response
     */
    @GetMapping("/quarterly")
    public ResponseEntity<byte[]> quarterly(@RequestParam int year, @RequestParam int quarter, @RequestParam(defaultValue = "xlsx") String format) {
        return export(reportService.quarterly(year, quarter), "quarterly-report-" + year + "-q" + quarter, format);
    }

    /**
     * Generates a yearly report for the specified year.
     *
     * @param year   the report year
     * @param format the export format ("xlsx" or "pdf"), defaults to "xlsx"
     * @return the report file as a downloadable response
     */
    @GetMapping("/yearly")
    public ResponseEntity<byte[]> yearly(@RequestParam int year, @RequestParam(defaultValue = "xlsx") String format) {
        return export(reportService.yearly(year), "yearly-report-" + year, format);
    }

    /**
     * Generates an employee balance report for the specified year.
     *
     * @param year   the report year
     * @param format the export format ("xlsx" or "pdf"), defaults to "pdf"
     * @return the report file as a downloadable response
     */
    @GetMapping("/employee-balance")
    public ResponseEntity<byte[]> employeeBalance(@RequestParam int year, @RequestParam(defaultValue = "pdf") String format) {
        return export(reportService.yearly(year), "employee-balance-report-" + year, format);
    }

    /**
     * Exports report data as either PDF or Excel file with proper content headers.
     *
     * @param rows   the monthly entry data to export
     * @param name   the base filename for the download
     * @param format the desired format ("pdf" or "xlsx")
     * @return the file as a downloadable response entity
     */
    private ResponseEntity<byte[]> export(List<MonthlyEntry> rows, String name, String format) {
        boolean pdf = "pdf".equalsIgnoreCase(format);
        byte[] body = pdf ? reportService.pdf(rows, name) : reportService.excel(rows, name);
        MediaType mediaType = pdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String extension = pdf ? ".pdf" : ".xlsx";
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + extension + "\"")
                .body(body);
    }
}
