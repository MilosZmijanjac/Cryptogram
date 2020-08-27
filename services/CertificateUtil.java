package services;

import models.MyCertificate;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class CertificateUtil {

    public static MyCertificate getCertificateDetails(String jksPath, String jksPassword) {

        MyCertificate certDetails = null;

        try {
            boolean isAliasWithPrivateKey = false;
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());
            Enumeration<String> es = keyStore.aliases();
            String alias = "";

            while (es.hasMoreElements()) {
                alias = (String) es.nextElement();
                if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                    break;
                }
            }

            if (isAliasWithPrivateKey) {
                KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                        new KeyStore.PasswordProtection(jksPassword.toCharArray()));

                PrivateKey myPrivateKey = pkEntry.getPrivateKey();
                Certificate[] chain = keyStore.getCertificateChain(alias);
                certDetails = new MyCertificate();
                certDetails.setPrivateKey(myPrivateKey);
                certDetails.setX509Certificate((X509Certificate) chain[0]);
                certDetails.setPublicKey(((X509Certificate) chain[0]).getPublicKey());
                certDetails.setRootPublicKey(((X509Certificate) chain[1]).getPublicKey());
                certDetails.setCrlPath("src/resources/MyCA/MyCA-crl.pem");
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return certDetails;
    }

    public static PublicKey getPublicKey(String jksPath,String jksPassword,String certificateAlias){
        PublicKey publicKey=null;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());
            Certificate cer=keyStore.getCertificate(certificateAlias);
            publicKey= cer.getPublicKey();
        }catch (Exception e){
            e.printStackTrace();
        }
        return publicKey;
    }
}
