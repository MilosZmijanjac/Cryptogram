package services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class PasswordUtil {
    public static String hashPasswordPlusSalt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String passwordAndSalt = password + salt;

        md.update(passwordAndSalt.getBytes());
        byte[] mdArray = md.digest();
        StringBuilder sb = new StringBuilder(mdArray.length * 2);
        for (byte b:mdArray){
            int v = b & 0xff;
            if (v < 16){
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));

        }
        return sb.toString();
    }
    public static String getSalt() {
        Random r = new SecureRandom();
        byte[] saltBytes = new byte[32];
        r.nextBytes(saltBytes);
        return PasswordUtil.Encode(saltBytes);
    }
    public static boolean compareSaltedHashWithUserEnteredPwd(String salt, String userEnteredPwd, String expectedHash) throws NoSuchAlgorithmException{
        return expectedHash.equals(hashPasswordPlusSalt(userEnteredPwd,salt));
    }
    public static String Encode(byte[] bytes){
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }
}
