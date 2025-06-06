package it.xview.cp.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Cripto {
	public static void main(String[] args) {
//		System.out.println(Cripto.encript("Pasquino21","GM.consulenti@fbf-isola.it", "312"));
		System.out.println("-"+Cripto.decript("kQPNXHDZnhXW7QV+2d+QAQ==:ZvatTQPJUVTBzMdscGa/kA==","qwepoi123098asdlkj<>.,!", "12345678")+"-");
	}
	public static String encript(String sPassword, String sPhrase, String sSalt) {
		String retVal=null;
		try {
			SecretKeySpec key = Cripto.createSecretKey(sPhrase, sSalt);
			retVal = Cripto.encrypt(sPassword, key);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}
	public static String decript(String sCryptPassword, String sPhrase, String sSalt) {
		String retVal=null;
			SecretKeySpec key;
			try {
				key = Cripto.createSecretKey(sPhrase, sSalt);
				retVal = Cripto.decrypt(sCryptPassword, key);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return retVal;
	}
    private static SecretKeySpec createSecretKey(String sPhrase, String sSalt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // richiede le librerie di BC
		char[] phrase = sPhrase.toCharArray();
		byte[] salt = new String(sSalt).getBytes();
		int iterationCount = 40000;
		int keyLength = 128;
      	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      	PBEKeySpec keySpec = new PBEKeySpec(phrase, salt, iterationCount, keyLength);
      	SecretKey keyTmp = keyFactory.generateSecret(keySpec);
      	return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }
    private static String encrypt(String dataToEncrypt, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(dataToEncrypt.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }
    private static String decrypt(String string, SecretKeySpec key) throws GeneralSecurityException, IOException, BadPaddingException{
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }
    private static String base64Encode(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
                        //Base64.getEncoder().encodeToString(bytes);
    }
    private static byte[] base64Decode(String property) throws IOException {
        return DatatypeConverter.parseBase64Binary(property);
                        //Base64.getDecoder().decode(property);
    }

    
}
