package crittografia;

import java.math.BigInteger;
import applicazione.KeyObject;

/**
 * @author Giovanni Calà
**/

public class RSA {

    private final MillerRabin mr;
    private final ANSIX917 generator;
    
    private BigInteger p, q, phi, n, d, e;
    private KeyObject privateKey, publicKey;

    public RSA() {
        mr = new MillerRabin();
        generator = new ANSIX917();
        getKeys();
    }
    
    private void getKeys() {
        
        p = findPrime();
        q = findPrime();
        n = p.multiply(q);
        phi = (p.subtract(BigInteger.valueOf(1))).multiply(q.subtract(BigInteger.valueOf(1)));
        e = new BigInteger("65537"); //valore usato in letteratura per una maggiore efficienza
        d = e.modInverse(phi);
        
        setPrivateKey();
        setPublicKey();
        System.out.println("\nChiave pubblica: " + e + ":" + n);
        System.out.println("Chiave privata: " + d + ":" + n);
    }
    
    private BigInteger findPrime() {
        
        boolean check = false;
        long number = 0;
        
        while(!check) {
            number = generator.getPseudoRandom();
            check = mr.isPrime(number);
        }
        return BigInteger.valueOf(number);
    }
    
    public byte[] encrypt(byte[] plaintext, KeyObject key) {      
         
        /* Ottengo la dimensione dei blocchi in base al valore di 'n' */
        int dimBlocco = (key.getN().bitLength()) / 8;  
        
        /* Applico il padding al testo */
        plaintext = padding(plaintext, dimBlocco);
        
        /* Creo una matrice per differenziare tutti i blocchi (righe della matrice) */
        byte[][] matrice = creaMatrice(plaintext, dimBlocco);
        
        /* Applico l'Encryption RSA per ogni blocco di testo (riga della matrice) */
        for (int i = 0; i < matrice.length; i++) 
            matrice[i] = (new BigInteger(1, matrice[i]).modPow(key.getValue(), key.getN())).toByteArray(); //C = M^e (mod n)
        
        /* Converto la matrice crittografata in vettore */
        byte[] encryptedText = convertiInVettore(matrice, dimBlocco+1); //il blocco cifrato ha un byte in più
        
        return encryptedText;
    }
    
    public byte[] decrypt(byte[] ciphertext, KeyObject key) {
 
        /* Ottengo la dimensione dei blocchi in base al valore di 'n' (+1 perchè il blocco cifrato ha un byte in più) */
        int dimBlocco = (key.getN().bitLength()) / 8 + 1;
        
        /* Creo una matrice per differenziare tutti i blocchi (righe della matrice) */
        byte[][] matrice = creaMatrice(ciphertext, dimBlocco);
        
        /* Applico la Decryption RSA per ogni blocco di testo (riga della matrice) */
        for (int i = 0; i < matrice.length; i++) 
            matrice[i] = (new BigInteger(1, matrice[i]).modPow(key.getValue(), key.getN())).toByteArray(); //M = C^d (mod n)
        
        /* Converto la matrice decrittografata in vettore */
        byte[] decryptedText = convertiInVettore(matrice, dimBlocco-1); //il blocco decifrato ha un byte in meno
        
        /* Rimuovo il padding dal testo */
        decryptedText = removePadding(decryptedText);
        
        return decryptedText;
    }
    
    private byte[] convertiInVettore(byte[][] matrice, int dimBlocco) {
       
        byte[] vettore = new byte[matrice.length * dimBlocco];                
        int numCol, colonneMancanti;
        
        for (int i = 0; i < matrice.length; i++) {
            
            numCol = matrice[i].length; //può capitare che il numero risultante dalla crittografia sia minore della dim del blocco
            colonneMancanti = dimBlocco - numCol;           

            for (int k = 0; k < colonneMancanti; k++) {
                vettore[k + i*dimBlocco] = (byte) 0; //inserisco 0 all'inizio della riga
            }
            for (int k = colonneMancanti; k < dimBlocco; k++) {
                vettore[k + i*dimBlocco] = matrice[i][k - colonneMancanti]; //continuo a riempire la riga del vettore a partire dall'inizio riga della matrice di partenza
            }  
            //Es. Matrice Partenza: [-11, 123, 4]
            //    Vettore Finale:   [0, -11, 123, 4]
        }
        return vettore;
    }
    
    private byte[][] creaMatrice(byte[] text, int dimBlocco) {
        
        int numBlocchi = text.length / dimBlocco;
        byte[][] m = new byte[numBlocchi][dimBlocco];
        
        for (int i = 0; i < numBlocchi; i++)
            for (int j = 0; j < dimBlocco; j++)
                m[i][j] = text[j + i*dimBlocco];

        return m;
    }
    
    private byte[] padding(byte[] text, int dimBlocco) {
        
        /* In accordo allo Standard PKCS#5 */
        
        int rimanenti = dimBlocco - (text.length % dimBlocco); 
        byte[] finalText = new byte[text.length + rimanenti];
        System.arraycopy(text, 0, finalText, 0, text.length); //copio il testo nel testo finale
        
        for (int i = text.length; i < finalText.length; i++) {
            finalText[i] = (byte)rimanenti; //aggiungo il byte tante volte quanto è il suo valore (es. 011 011 011)
        }
        return finalText;
    }
    
    private byte[] removePadding(byte[] text) {
        
        int lastByte = text[text.length - 1]; //ottengo il valore dell'ultimo byte del blocco (considerandolo come int)
        byte[] finalText = new byte[text.length - lastByte];
        System.arraycopy(text, 0, finalText, 0, finalText.length);
        
        return finalText;
    }
    
    public void setPrivateKey() {
        this.privateKey = new KeyObject(this.d, this.n);
    }
    
    public KeyObject getPrivateKey() {
        return this.privateKey;
    }
    
    public void setPublicKey() {
        this.publicKey = new KeyObject(this.e, this.n);
    }
    
    public KeyObject getPublicKey() {
        return this.publicKey;
    }
}
