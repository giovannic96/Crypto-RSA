package crittografia;

/**
 * @author Giovanni Calà
**/

public class TripleDES {

    DES_CBC des_cbc;
    String key1, key2;
    
    public TripleDES(String key1, String key2) {
        des_cbc = new DES_CBC();
        this.key1 = key1;
        this.key2 = key2;
    }
    
    public String Encrypt(String plaintext) {
        
        return des_cbc.StartEncrypt(des_cbc.StartDecrypt(des_cbc.StartEncrypt(plaintext, key1), key2), key1);
    }
    
    public String Decrypt(String ciphertext) {
        
        return des_cbc.StartDecrypt(des_cbc.StartEncrypt(des_cbc.StartDecrypt(ciphertext, key1), key2), key1);
    }
}


