package com.epass.epass_backend.service;

import com.epass.epass_backend.model.Permit;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class PdfService {

    public byte[] generatePermitPdf(Permit permit) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        // Title
        document.add(new Paragraph("E-PASS PERMIT")
                .setBold()
                .setFontSize(24)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        document.add(new Paragraph(" "));

        // Permit Details
        document.add(new Paragraph("Permit ID: " + permit.getId()));
        document.add(new Paragraph("Applicant Name: " + permit.getUser().getFullName()));
        document.add(new Paragraph("Email: " + permit.getUser().getEmail()));
        document.add(new Paragraph("Permit Type: " + permit.getPermitRequest().getPermitType()));
        document.add(new Paragraph("Purpose: " + permit.getPermitRequest().getPurpose()));
        document.add(new Paragraph("Destination: " + permit.getPermitRequest().getDestination()));
        document.add(new Paragraph("Start Date: " + permit.getPermitRequest().getStartDate()));
        document.add(new Paragraph("End Date: " + permit.getPermitRequest().getEndDate()));
        document.add(new Paragraph("Issue Date: " + permit.getIssueDate()));
        document.add(new Paragraph("Expiry Date: " + permit.getExpiryDate()));
        document.add(new Paragraph("Status: APPROVED").setBold());

        document.add(new Paragraph(" "));

        // QR Code
        if (permit.getQrCode() != null) {
            byte[] qrBytes = Base64.getDecoder().decode(permit.getQrCode());
            Image qrImage = new Image(ImageDataFactory.create(qrBytes));
            qrImage.setWidth(150);
            qrImage.setHeight(150);
            qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(qrImage);
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("This permit is digitally verified. Scan QR code to verify.")
                .setItalic()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        document.close();
        return outputStream.toByteArray();
    }
}