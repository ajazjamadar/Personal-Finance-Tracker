package com.qburst.training.personalfinancetracker.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ExportDocumentBuilder {

    private ExportDocumentBuilder() {
    }

    public static byte[] toCsv(List<String> headers, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(",", headers.stream().map(ExportDocumentBuilder::escapeCsv).toList()));
        builder.append("\n");

        for (List<String> row : rows) {
            builder.append(String.join(",", row.stream().map(ExportDocumentBuilder::escapeCsv).toList()));
            builder.append("\n");
        }

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toSimplePdf(String title, List<String> lines) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setLeading(16f);
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.newLineAtOffset(50, 790);
                content.showText(sanitizePdfText(title));

                content.newLine();
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 10);

                int writtenLines = 0;
                for (String line : lines) {
                    if (writtenLines >= 42) {
                        break;
                    }
                    content.showText(sanitizePdfText(line));
                    content.newLine();
                    writtenLines++;
                }
                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate PDF export", exception);
        }
    }

    private static String escapeCsv(String value) {
        String safe = value == null ? "" : value;
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static String sanitizePdfText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}