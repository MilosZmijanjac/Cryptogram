package models;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;

public class MyCertificate {
    private PrivateKey privateKey;
    private X509Certificate x509Certificate;
    private PublicKey publicKey;
    private PublicKey rootPublicKey;
    private String crlPath;

    public PublicKey getRootPublicKey() {
        return rootPublicKey;
    }

    public void setRootPublicKey(PublicKey rootPublicKey) {
        this.rootPublicKey = rootPublicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey){
        this.publicKey = publicKey;
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public String getCrlPath(){
        return crlPath;
    }

    public void setCrlPath(String path){
        crlPath=path;
    }

    public boolean  validateCertificate(){
        try {
            x509Certificate.verify(rootPublicKey);//provjera da je potpisan od strane ca tijela kojem se vjeruje
            x509Certificate.checkValidity();//provjera vremena vazenja
            //provjera CRL liste
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509CRL crl = (X509CRL) cf.generateCRL(new FileInputStream(new File(this.crlPath)));
            X509CRLEntry revokedCertificate=null;
            revokedCertificate = crl.getRevokedCertificate(x509Certificate.getSerialNumber());
            return revokedCertificate == null;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
