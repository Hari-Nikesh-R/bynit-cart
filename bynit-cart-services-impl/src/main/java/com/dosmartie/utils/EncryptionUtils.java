package com.dosmartie.utils;

import com.dosmartie.helper.PropertiesCollector;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class EncryptionUtils {
    @Autowired
    private PropertiesCollector propertiesCollector;

    public boolean decryptAuthIdAndValidateRequest(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec("1234567812345678".getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec("1234567812345678".getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return validateRequest(new String(original));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private synchronized boolean validateRequest(String auth) {
        return propertiesCollector.getAuthId().equals(auth);
    }
}
