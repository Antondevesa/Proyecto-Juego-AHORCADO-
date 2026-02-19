package com.liceolapaz.ahorcado.seguridad;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class CifradoAESConverter implements AttributeConverter<String, String> {
    private static final String CLAVE_SECRETA = "LiceoLaPazClave!";
    private static final String ALGORITMO = "AES";

    @Override
    public String convertToDatabaseColumn(String datoOriginal) {
        if (datoOriginal == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            SecretKeySpec secretKey = new SecretKeySpec(CLAVE_SECRETA.getBytes(), ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytesCifrados = cipher.doFinal(datoOriginal.getBytes());
            return Base64.getEncoder().encodeToString(bytesCifrados);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar con AES", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String datoCifrado) {
        if (datoCifrado == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            SecretKeySpec secretKey = new SecretKeySpec(CLAVE_SECRETA.getBytes(), ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytesDecodificados = Base64.getDecoder().decode(datoCifrado);
            return new String(cipher.doFinal(bytesDecodificados));
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar con AES", e);
        }
    }
}