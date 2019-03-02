package crittografia;

import java.util.ArrayList;

/**
 * @author Giovanni Calà
**/

public class DES_CBC {

    DES des;
    
    private String[] Pi; //plaintext parziali
    private String[] Ci; //ciphertext parziali
    private String IV;
    private final String DEFAULT_IV_VALUE = "invector";
       
    public DES_CBC() {
        des = new DES();
        IV = DEFAULT_IV_VALUE; //scelto casualmente da me
    }
    
    public String StartEncrypt(String plaintext, String key) {
        
        IV = DEFAULT_IV_VALUE;
        String ciphertext = "";
        System.out.println("\nPLAINTEXT: " + plaintext);            

        Pi = getListPi(plaintext);
        Ci = new String[Pi.length]; 
        
        for (int i = 0; i < Pi.length; i++) {
            
            System.out.println("\n\n/*********** CRITTOGRAFIA **********/");
            System.out.println("\nPLAINTEXT:       " + Pi[i]);            
            Ci[i] = des.encrypt(des.XOR(des.convertiInBit(IV), des.convertiInBit(Pi[i])), des.convertiInBit(key)); //Applico l'Encryption DES 
            System.out.println("\nCRITTOGRAFATO:   " + Ci[i]);

            IV = Ci[i]; //l'IV diventa il Ci
            ciphertext += Ci[i]; //aggiorno il ciphertext
        }
        return ciphertext;
    }
    
    public String StartDecrypt(String ciphertext, String key) {
                
        String plain = "";
        String out = "";
        IV = DEFAULT_IV_VALUE;
        Ci = getListPi(ciphertext);
        Pi = new String[Ci.length]; 

        for (int i = 0; i < Ci.length; i++) {
           
            System.out.println("\n\n/********* DECRITTOGRAFIA **********/");
            System.out.println("\nCIPHERTEXT:      " + Ci[i]);
            
            out = des.decrypt(des.convertiInBit(Ci[i]), des.convertiInBit(key)); //Applico la Decryption DES 
            Pi[i] = des.XOR(des.convertiInBit(IV), des.convertiInBit(out)); 
            System.out.println("\nDECRITTOGRAFATO: " + des.convertiInTesto(Pi[i]));
            
            IV = Ci[i]; //l'IV diventa il Ci
            plain += des.convertiInTesto(Pi[i]); //aggiorno il plaintext
        }
        return plain;
    }
    
    private String[] getListPi(String p) {
        
        ArrayList<String> array = new ArrayList<>();
        
        while(p.length() != 0) { 
            array.add(p.substring(0, 8)); //aggiungo 8 caratteri
            p = p.substring(8, p.length()); //in questo modo taglio i primi 8 caratteri
        }
        return array.toArray(new String[array.size()]); //crea una String[] e aggiungi ad essa i contenuti dell'ArrayList
    }
    
}
