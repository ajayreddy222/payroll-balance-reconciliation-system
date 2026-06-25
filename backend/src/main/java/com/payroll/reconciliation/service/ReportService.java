package com.payroll.reconciliation.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.payroll.reconciliation.entity.MonthlyEntry;
import com.payroll.reconciliation.repository.MonthlyEntryRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for generating payroll reconciliation reports.
 * Supports Excel (XLSX) and PDF export formats for monthly, quarterly, and yearly data.
 *
 * @author Payroll Reconciliation Team
 */
@Service
public class ReportService {
    private final MonthlyEntryRepository entries;

    /**
     * Constructs the service with the required monthly entry repository.
     *
     * @param entries the monthly entry repository
     */
    public ReportService(MonthlyEntryRepository entries) {
        this.entries = entries;
    }

    /**
     * Retrieves monthly entries for a specific month.
     *
     * @param year  the year
     * @param month the month (1-12)
     * @return list of monthly entries for the specified month
     */
    public List<MonthlyEntry> monthly(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        return entries.findByMonthBetweenOrderByMonthAsc(start, start.withDayOfMonth(start.lengthOfMonth()));
    }

    /**
     * Retrieves monthly entries for a specific quarter.
     *
     * @param year    the year
     * @param quarter the quarter (1-4)
     * @return list of monthly entries for the specified quarter
     */
    public List<MonthlyEntry> quarterly(int year, int quarter) {
        int startMonth = ((quarter - 1) * 3) + 1;
        LocalDate start = LocalDate.of(year, startMonth, 1);
        return entries.findByMonthBetweenOrderByMonthAsc(start, start.plusMonths(2).withDayOfMonth(start.plusMonths(2).lengthOfMonth()));
    }

    /**
     * Retrieves monthly entries for an entire year with project data.
     *
     * @param year the year
     * @return list of monthly entries for the specified year
     */
    public List<MonthlyEntry> yearly(int year) {
        return entries.findByYearWithProject(year);
    }

    /**
     * Generates an Excel (XLSX) report from the given monthly entries.
     *
     * @param rows  the monthly entry data to include in the report
     * @param title the worksheet title and report name
     * @return the Excel file as a byte array
     * @throws IllegalStateException if report generation fails
     */
    public byte[] excel(List<MonthlyEntry> rows, String title) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(title);
            Row header = sheet.createRow(0);
            String[] headers = {"Month", "Project", "Client", "Vendor", "Hours", "Rate", "Actual", "Paystub", "Direct Employer Payment", "Insurance", "Other Adjustment", "Balance", "Notes"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int r = 1;
            for (MonthlyEntry entry : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(entry.getMonth().toString());
                row.createCell(1).setCellValue(entry.getProject().getProjectName());
                row.createCell(2).setCellValue(entry.getProject().getClientName());
                row.createCell(3).setCellValue(entry.getProject().getVendorName());
                row.createCell(4).setCellValue(entry.getHoursWorked().doubleValue());
                row.createCell(5).setCellValue(entry.getEmployeeHourlyRate().doubleValue());
                row.createCell(6).setCellValue(entry.getActualEarnings().doubleValue());
                row.createCell(7).setCellValue(entry.getPaystubAmountReceived().doubleValue());
                row.createCell(8).setCellValue(entry.getDirectEmployerPayment().doubleValue());
                row.createCell(9).setCellValue(entry.getInsuranceDeduction().doubleValue());
                row.createCell(10).setCellValue(entry.getOtherAdjustment().doubleValue());
                row.createCell(11).setCellValue(entry.getMonthlyBalance().doubleValue());
                row.createCell(12).setCellValue(entry.getNotes() == null ? "" : entry.getNotes());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create Excel report", ex);
        }
    }

    /**
     * Generates a PDF report from the given monthly entries.
     *
     * @param rows  the monthly entry data to include in the report
     * @param title the report title displayed in the PDF
     * @return the PDF file as a byte array
     * @throws IllegalStateException if report generation fails
     */
    public byte[] pdf(List<MonthlyEntry> rows, String title) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(title));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            for (String h : List.of("Month", "Project", "Hours", "Actual", "Paystub", "Balance")) table.addCell(h);
            for (MonthlyEntry e : rows) {
                table.addCell(e.getMonth().toString());
                table.addCell(e.getProject().getProjectName());
                table.addCell(e.getHoursWorked().toPlainString());
                table.addCell(e.getActualEarnings().toPlainString());
                table.addCell(e.getPaystubAmountReceived().toPlainString());
                table.addCell(e.getMonthlyBalance().toPlainString());
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create PDF report", ex);
        }
    }
}
