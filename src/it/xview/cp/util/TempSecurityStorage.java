package it.xview.cp.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
/*
 * Concurrent Host XXH_CRYPTO
 * P_ID
 * P_VALORE
 * P_AZIONE
 */
public class TempSecurityStorage {
	private final static String sSalt = "12345678";

	public static void main(String[] args) {
		TempSecurityStorage cp = new TempSecurityStorage();
		try {
			cp.run(args[0], args[1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void run(String string1, String string2) throws Exception {
		ConcurrentHostConnection chc = new ConcurrentHostConnection(string1,string2);
		HashMap<String,Object> param = chc.getConcurrentParameter();
		String sId = (String)param.get("P_ID");//chc.getSingleValue("select XXH_TEMP_SECURITY_STORAGE_S.nextval from dual" , null);
		String pwd = (String)param.get("P_VALORE");//"TEST";
		String azione =  (String)param.get("P_AZIONE");//E encrypt D decrypt
		if ("E".equals(azione)) {
			SecretKeySpec key = TempSecurityStorage.createSecretKey(chc.getConnection());
			pwd = TempSecurityStorage.encrypt(pwd, key);
			insertTempSecurityStorage(chc.getConnection(), Integer.parseInt(sId),pwd);
		}else if("D".equals(azione)) {
			SecretKeySpec key = TempSecurityStorage.createSecretKey(chc.getConnection());
			pwd = TempSecurityStorage.decrypt(pwd, key);
			insertTempSecurityStorage(chc.getConnection(), Integer.parseInt(sId),pwd);
		}else {
			insertTempSecurityStorage(chc.getConnection(), Integer.parseInt(sId),null);
		}
	}
	public void insertTempSecurityStorage(Connection conn, int pId, String pValore) throws SQLException {
		CallableStatement stmt;
		stmt = conn.prepareCall("call xxh_sts_security.INSERT_TEMP_SECURITY_STORAGE (?,?)");
		stmt.setInt(1, pId);
		stmt.setString(2, pValore);
        stmt.executeUpdate(); 
        stmt.close();
		
	}
    public static String encrypt(String dataToEncrypt, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(dataToEncrypt.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    private static String base64Encode(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
                        //Base64.getEncoder().encodeToString(bytes);
    }

    public static String decrypt(String string, SecretKeySpec key) throws GeneralSecurityException, IOException, BadPaddingException{
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return DatatypeConverter.parseBase64Binary(property);
                        //Base64.getDecoder().decode(property);
    }

    public static SecretKeySpec createSecretKey(Connection conn) throws NoSuchAlgorithmException, 
    InvalidKeySpecException {
		// The salt (probably) can be stored along with the encrypted data
		String sPassword = TempSecurityStorage.getPwd(conn);
		return TempSecurityStorage.createSecretKey(sPassword);
	}
    public static SecretKeySpec createSecretKey(String sPassword) throws NoSuchAlgorithmException, 
    InvalidKeySpecException {
		// The salt (probably) can be stored along with the encrypted data
		char[] password = sPassword.toCharArray();
		byte[] salt = new String(sSalt).getBytes();
		
		// Decreasing this speeds down startup time and can be useful during testing, but it also makes it easier for brute force attackers
		int iterationCount = 40000;
		// Other values give me java.security.InvalidKeyException: Illegal key size or default parameters
		int keyLength = 128;
		
		return TempSecurityStorage.createSecretKey(password,salt,iterationCount,keyLength);
	}
    public static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
      // richiede le librerie di BC
    	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    	PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
    	SecretKey keyTmp = keyFactory.generateSecret(keySpec);
    	return new SecretKeySpec(keyTmp.getEncoded(), "AES");
  }
    protected static String getPwd(Connection conn){
        String sql ="SELECT XXH_STS_SECURITY.DECRYPT FROM DUAL";
        String retVal="";
        ResultSet rs=null;
        PreparedStatement stmt = null;
        try{
          stmt = conn.prepareStatement(sql);
          rs=stmt.executeQuery();
          if(rs.next()) retVal = rs.getString(1);
        }catch(Exception e)
        {
          e.printStackTrace();
        }finally
          {
            try{
              rs.close();
              stmt.close();
            }catch(Exception e){}
          }
        return retVal;        
    }
}
