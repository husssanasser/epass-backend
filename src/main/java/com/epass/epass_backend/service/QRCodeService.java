package com.epass.epass_backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

@Service
public class QRCodeService {

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String generateQRCodeImage(String token) throws Exception {
        String frontendUrl = System.getenv().getOrDefault("FRONTEND_URL", "https://epass-frontend-wine.vercel.app");        String content = frontendUrl + "/verify?token=" + token;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                300,
                300
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}