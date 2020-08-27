package services;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import javax.crypto.*;

public class SymmetricAlgorithms {

    private SecretKey symmetricKey;
    private Cipher cipher;

    private static List<String> algorithms = List.of("AES","DES","RC4","RC2","Blowfish");

    public SymmetricAlgorithms(String symmetricAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (!algorithms.contains(symmetricAlgorithm))
            throw new NoSuchAlgorithmException("Specified symmetric algorithm " + symmetricAlgorithm + " not supported!!!");
        KeyGenerator keygen = KeyGenerator.getInstance(symmetricAlgorithm);
        this.symmetricKey = keygen.generateKey();
        this.cipher=Cipher.getInstance(symmetricAlgorithm);
    }

    public SecretKey getSymmetricKey() {
        return this.symmetricKey;
    }

    public void setSymmetricKey(SecretKey symmetricKey){
        this.symmetricKey=symmetricKey;
    }

    public String symmetricEncrypt(String input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.ENCRYPT_MODE, this.symmetricKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
    }

    public String symmetricDecrypt(String input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE,this.symmetricKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(input)));
    }

}