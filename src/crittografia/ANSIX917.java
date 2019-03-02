package crittografia;

import java.math.BigInteger;
import java.util.Collections;

/**
 * @author Giovanni Calà
**/

public class ANSIX917 {

    private static final String KEY1 = "galassia";
    private static final String KEY2 = "saggezza";
    
    TripleDES tripleDes;
    String dt, v0, r, v;
    
    public ANSIX917() {
        tripleDes = new TripleDES(KEY1, KEY2);
        v0 = String.format("%064d", new BigInteger(Long.toBinaryString((long)(Math.random()*((900000000)-500000000)+500000000))));
        v = convertiInTesto(v0);
    }
    
    public long getPseudoRandom() {
        
        dt = String.format("%064d", new BigInteger(Long.toBinaryString((long)System.currentTimeMillis())));
             
        System.out.println("V[i]: " + convertiInDecimale(v));
        
        r = tripleDes.Encrypt(convertiInTesto(XOR(convertiInBit(v), convertiInBit(tripleDes.Encrypt(convertiInTesto(dt))))));
        v = tripleDes.Encrypt(convertiInTesto(XOR(convertiInBit(r), convertiInBit(tripleDes.Encrypt(convertiInTesto(dt))))));
        
        System.out.println("R[i]:   " + convertiInDecimale(r)); 
        System.out.println("V[i+1]: " + convertiInDecimale(v));
                
        return Long.parseLong(convertiInDecimale(r));
    }
    
    private String XOR(String firstString, String secondString) {
        return tripleDes.des_cbc.des.XOR(firstString, secondString);
    }
    
    /* Conversione da stringa di testo in stringa di bit */
    private String convertiInBit(String text) {
        return tripleDes.des_cbc.des.convertiInBit(text);
    }
    
    /* Conversione da stringa di bit in stringa di testo */
    private String convertiInTesto(String text) {
        
        StringBuilder b = new StringBuilder();
        int len = text.length();
        int resto = len % 8;
        
        if(resto != 0)
            text += String.join("", Collections.nCopies(resto, "0"));
        
        int i = 0;
        len = text.length();

        while (i + 8 <= len) {
            char c = (char)Integer.parseInt(text.substring(i, i+8), 2);
            i+=8;
            b.append(c);
        }
        return b.toString();
    }
    
    /* Conversione da stringa di testo in stringa decimale */
    private String convertiInDecimale(String text) {
        //Considero la substring senza il primo bit altrimenti considero anche il segno '-'
        return String.valueOf(new BigInteger(convertiInBit(text).substring(1), 2).longValue()); 
    }
}
