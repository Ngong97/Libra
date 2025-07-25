package com.ngong.librasoftware.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.ReportData;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportService {

    private static final DatabaseService db = new DatabaseService();
    public static boolean generateReport(File destination,
                                         String school, String librarian,
                                         ReportData data) {
        try {
            String date = java.time.LocalDate.now().toString();

            String extension = getFileExtension(destination.getName()).toLowerCase();

            switch (extension) {
                case "pdf" -> generatePDF(destination, school, librarian, date, data);
                case "docx" -> generateDOCX(destination, school, librarian, date, data);
                default -> throw new IllegalArgumentException("Unsupported format: " + extension);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "";
    }


    private static void generatePDF(File destination, String school, String librarian, String date, ReportData data) throws Exception {
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, new FileOutputStream(destination));
        doc.open();

        Paragraph title = new Paragraph("📘 Library Report\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);


        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{70, 30});

        PdfPCell librarianCell = new PdfPCell(new Phrase("Librarian: " + (librarian.isEmpty() ? "N/A" : librarian)));
        PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + date));

        librarianCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        metaTable.addCell(librarianCell);
        metaTable.addCell(dateCell);

        doc.add(metaTable);
        doc.add(new Paragraph("School: " + (school.isEmpty() ? "N/A" : school)));

        doc.add(Chunk.NEWLINE);


        doc.add(new Paragraph("📊 Statistics:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        doc.add(new Paragraph("• Registered Books: " + db.getRegisteredBooksNo()));
        doc.add(new Paragraph("• Borrowed Books: " + db.countOverdueBooks("","","","",null,"","")));
        doc.add(new Paragraph("• Remaining Books: " + db.getRemainingBooksNo()));


        doc.add(Chunk.NEWLINE);

        Map<String, Integer> genderDist = getUnclearedGenderDistribution(null, null, null);

        doc.add(new Paragraph("👥 Gender Distribution (Uncleared Students):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        if (genderDist.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            doc.add(new Paragraph("• No uncleared students found for the selected filters.\n"));
        } else {
//            String gender="";
            for (Map.Entry<String, Integer> entry : genderDist.entrySet()) {
                String label = switch (entry.getKey()) {
                    case "Male" -> "• Male students: ";
                    case "Female" -> "• Female students: ";
                    case "Other" -> "• Students of other gender identities: ";
                    default -> "• " + entry.getKey() + ": ";
                };
                doc.add(new Paragraph(label + entry.getValue()));
            }
            doc.add(Chunk.NEWLINE);
        }


        doc.add(Chunk.NEWLINE);
        Map<String, Integer> classDistData = new DatabaseService().getActiveStudentsByClass();

        doc.add(new Paragraph("🏫 Class Distribution (Uncleared Students):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        if (classDistData.isEmpty()) {
            doc.add(new Paragraph("• No uncleared students found."));
        } else {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{60, 40});

            // Header row
            PdfPCell classHeader = new PdfPCell(new Phrase("Class"));
            PdfPCell countHeader = new PdfPCell(new Phrase("Number of Students"));
            classHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            countHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(classHeader);
            table.addCell(countHeader);

            // Data rows
            for (Map.Entry<String, Integer> entry : classDistData.entrySet()) {
                table.addCell(entry.getKey());
                table.addCell(String.valueOf(entry.getValue()));
            }

            doc.add(table);
        }
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Yours Sincerely, "));
        doc.add(new Paragraph(librarian));
        doc.add(new Paragraph("Signature:............."));

        doc.close();
    }
    private static void generateDOCX(File destination, String school, String librarian, String date, ReportData data) throws Exception {
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText("📘 Library Report");
        run.setBold(true);
        run.setFontSize(18);


//        XWPFTable metaTable = doc.createTable(1, 2);
//        metaTable.setWidth("100%");
        XWPFParagraph datePara = doc.createParagraph();
        datePara.createRun().setText("Date: " + date);

        XWPFParagraph librarianPara = doc.createParagraph();
        librarianPara.createRun().setText("Librarian: " + (librarian.isEmpty() ? "N/A" : librarian));


        XWPFParagraph schoolPara = doc.createParagraph();
        schoolPara.createRun().setText("School: " + (school.isEmpty() ? "N/A" : school));

        XWPFParagraph stats = doc.createParagraph();
        stats.setStyle("Heading2");
        stats.createRun().setText("📊 Statistics:");
        stats.createRun().addBreak();
        stats.createRun().setText("• Registered Books: " + data.getRegistered());
        stats.createRun().addBreak();
        stats.createRun().setText("• Borrowed Books: " + data.getBorrowed());
        stats.createRun().addBreak();
        stats.createRun().setText("• Remaining Books: " + data.getRemaining());
        stats.createRun().addBreak();
        stats.createRun().setText("• Not Returned: " + data.getNotReturned());
        stats.createRun().addBreak();
        stats.createRun().addBreak();



        Map<String, Integer> genderDist = getUnclearedGenderDistribution(null, null, null);

        XWPFParagraph genderHeader = doc.createParagraph();
        genderHeader.setStyle("Heading2");
        genderHeader.createRun().setText("👥 Gender Distribution (Uncleared Students):");

        if (genderDist.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            XWPFParagraph none = doc.createParagraph();
            none.createRun().setText("• No uncleared students found for the selected filters.");
        } else {
            for (Map.Entry<String, Integer> entry : genderDist.entrySet()) {
                String label = switch (entry.getKey()) {
                    case "Male" -> "• Male students who haven't returned books: ";
                    case "Female" -> "• Female students who haven't returned books: ";
                    case "Other" -> "• Students of other gender identities: ";
                    default -> "• " + entry.getKey() + ": ";
                };
                XWPFParagraph para = doc.createParagraph();
                para.createRun().setText(label + entry.getValue());
            }
        }


        Map<String, Integer> classDistData = new DatabaseService().getActiveStudentsByClass();

        XWPFParagraph classHeader = doc.createParagraph();
        classHeader.setStyle("Heading2");
        classHeader.createRun().setText("🏫 Class Distribution (Uncleared Students):");

        if (classDistData.isEmpty()) {
            XWPFParagraph none = doc.createParagraph();
            none.createRun().setText("• No uncleared students found.");
        } else {
            XWPFTable table = doc.createTable(classDistData.size() + 1, 2); // +1 for header row
            table.setWidth("100%");

            // Header row
            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("Class");
            header.getCell(1).setText("Number of Students");

            // Data rows
            int rowIndex = 1;
            for (Map.Entry<String, Integer> entry : classDistData.entrySet()) {
                XWPFTableRow row2 = table.getRow(rowIndex++);
                row2.getCell(0).setText(entry.getKey());
                row2.getCell(1).setText(String.valueOf(entry.getValue()));
            }
        }

        XWPFParagraph signing = doc.createParagraph();
        signing.createRun().addBreak();
        signing.createRun().addBreak();

        signing.createRun().setText("Yours Sincerely, ");
        signing.createRun().addBreak();
        signing.createRun().setText(librarian);
        signing.createRun().addBreak();
        signing.createRun().setText("Signature:.............");

        try (FileOutputStream out = new FileOutputStream(destination)) {
            doc.write(out);
        }
    }


    public static Map<String, Integer> getUnclearedGenderDistribution(String term, String studentClass, Month month) {
        Map<String, Integer> genderCounts = new LinkedHashMap<>();
        int maleCount = db.countUnclearedStudentsByGender("Male", term, studentClass, month);
        int femaleCount = db.countUnclearedStudentsByGender("Female", term, studentClass, month);
        int otherCount = db.countUnclearedStudentsByGender("Other", term, studentClass, month);

        genderCounts.put("Male", maleCount);
        genderCounts.put("Female", femaleCount);
        if (otherCount > 0) genderCounts.put("Other", otherCount); // Optional: only include if non-zero

        return genderCounts;
    }




    private static void insertImage(XWPFDocument doc, File imageFile) throws Exception {
        try (FileInputStream is = new FileInputStream(imageFile)) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.addPicture(is, org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG, imageFile.getName(), 400, 300);
        }
    }

}