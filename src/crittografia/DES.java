package crittografia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Giovanni Calà
**/

public class DES {

    static final int[] IP = {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17,  9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };
    
    static final int[] IIP = {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41,  9, 49, 17, 57, 25
    };
    
    static final int[] LEFTPC1 = {
        57, 49, 41, 33, 25, 17, 9,
        1,  58, 50, 42, 34, 26, 18,
        10,  2, 59, 51, 43, 35, 27,
        19, 11,  3, 60, 52, 44, 36
    };
    
    static final int[] RIGHTPC1 = {
        63, 55, 47, 39, 31, 23, 15,
        7,  62, 54, 46, 38, 30, 22,
        14,  6, 61, 53, 45, 37, 29,
        21, 13,  5, 28, 20, 12, 4
    };
    
    static final int[] NUM_SHIFT = {
        1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
    };
    
    static final int[] PC2 = {
        14, 17, 11, 24,  1, 5, 
         3, 28, 15,  6, 21, 10, 
        23, 19, 12,  4, 26,  8, 
        16,  7, 27, 20, 13,  2,
        41, 52, 31, 37, 47, 55, 
        30, 40, 51, 45, 33, 48, 
        44, 49, 39, 56, 34, 53, 
        46, 42, 50, 36, 29, 32
    };
    
    static final int[] E = {
        32, 1,  2,  3,  4,   5,
        4,  5,  6,  7,  8,   9,
        8,  9,  10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32,  1
	};
    
    static final int[] S1 = {
        14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7,
         0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8,
         4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0,
        15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13
    };
    static final int[] S2 = {
        15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10,
         3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5,
         0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15,
        13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9
    };
    static final int[] S3 = {
        10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8,
        13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1,
        13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7,
         1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12
    };
        
    static final int[] S4 = {
        7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15,
       13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9,
       10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4,
        3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14
    };
    static final int[] S5 = {
        2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9,
       14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6,
        4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14,
       11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3
    };
    static final int[] S6 = {
        12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11,
        10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8,
         9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6,
         4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13
    };
    static final int[] S7 = {
        4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1,
       13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6,
        1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2,
        6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12
    };
    static final int[] S8 = {
        13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7,
         1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2,
         7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8,
         2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11
    };
        
    static final int[] P = {
        16,  7, 20, 21,
        29, 12, 28, 17,
        1, 15, 23, 26,
        5, 18, 31, 10,
        2,  8, 24, 14,
        32, 27,  3,  9,
        19, 13, 30,  6,
        22, 11,  4, 25
    };
    
    static final ArrayList<int[]> S_BOXES = new ArrayList<>(Arrays.asList(S1, S2, S3, S4, S5, S6, S7, S8));
    public String ciphertext;
    private String outputIP; //sarà l'output della permutazione iniziale
    private String leftPlaintext, rightPlaintext;
    private String C, D;
    private String outputPhases;
    
    public String encrypt(String plaintext, String key) {
        
        String mode = "encryption";
        return Start(plaintext, key, mode);
    }
    
    public String decrypt(String plaintext, String key) {
        
        String mode = "decryption";
        return Start(plaintext, key, mode);
    }
    
    public String Start(String plaintext, String key, String mode) {
        
        outputIP = Permutation(IP, plaintext);
        leftPlaintext = Dividi(outputIP, 0, outputIP.length()/2);
        rightPlaintext = Dividi(outputIP, outputIP.length()/2, outputIP.length());
        outputPhases = StartPhases(leftPlaintext, rightPlaintext, key, mode);
        ciphertext = Permutation(IIP, outputPhases);
        
        return convertiInTesto(ciphertext);
    }
    
    public String convertiInBit(String text) {
        
        char[] textChar = text.toCharArray();
        String pBit = "";
        for (int i = 0; i < textChar.length; i++) {
            pBit += String.format("%08d", Integer.parseInt(Integer.toBinaryString(textChar[i]))); //%08d mi serve per far sì che un carattere abbia 8 bit, non 7
        }

        return pBit;
    }
    
    public String convertiInTesto(String text) {
        
        StringBuilder b = new StringBuilder();
        int len = text.length();
        int i = 0;
        
        while (i + 8 <= len) {
            char c = (char)Integer.parseInt(text.substring(i, i+8), 2);
            i+=8;
            b.append(c);
        }
        return b.toString();
    }
    
    private String Permutation(int[] permutationType, String text) {
        
        char[] textArray = text.toCharArray();
        char[] outputArray = new char[permutationType.length];
        
        for (int i = 0; i < permutationType.length; i++) { //scorro la matrice di permutazione
            for (int j = 0; j < textArray.length; j++) { //scorro il testo in bit
                if (j == permutationType[i]-1) { //il '-1' mi serve perchè i valori della matrice di permutazione partono da 1, non da 0
                    outputArray[i] = textArray[j]; //mi prendo il bit che c'è alla j-esima posizione del testo e lo metto alla i-esima posizione dell'output
                }
            }
        }
        return String.valueOf(outputArray); 
    }

    private String generateSubkey(String Ci, String Di, int nShift) {
                                    
        /* Left Shift */
        C = LeftShift(Ci, nShift);
        D = LeftShift(Di, nShift);
        
        String unione = C + D;

        /* Permutated Choice 2 */
        return Permutation(PC2, unione);
    }

    private String LeftShift(String block, int numShift) {
        
        return block.replaceAll("^(.{" + numShift + "})(.*)", "$2$1"); //elimino i primi n caratteri e li metto alla fine
    }

    private String Dividi(String text, int inizio, int fine) {
        
        char[] textArray = text.toCharArray();
        String resultText = "";
        
        for (int i = inizio; i < fine; i++) {
            resultText += textArray[i];
        }
        return resultText;
    }
    
    private String Feistel(String block, String subkey) {
        
        /* Espansione E */
        String blockExpanded = Permutation(E, block);
        
        /* XOR con subkey */
        String xoredString = XOR(blockExpanded, subkey);
        
        /* S-Box */
        String[] inputBox = new String[8];
        String[] outputBox = new String[8];
        String outputFinale = "";
        
        for (int i = 0; i < S_BOXES.size(); i++) {
            
            inputBox[i] = "";
            outputBox[i] = "";
            
            for (int k = 0+(6*i); k < 6*(i+1); k++) { //in questo modo vado a passi di 6 in base all'indice dell S-Box
                inputBox[i] += xoredString.charAt(k);     
            }
            
            //mi gestisco l'inputBox parziale come una lista per semplicità
            List<Character> inputList = new ArrayList<Character>();
            for (char c : inputBox[i].toCharArray()) {
                inputList.add(c);
            }
            
            //ottengo i bit di riga e i bit di colonna
            String bitRiga = "" + inputList.get(0) + inputList.get(inputList.size()-1);
            String bitColonna = "" + inputList.get(1) + inputList.get(2) + inputList.get(3) + inputList.get(4);
            
            //li converto in valori decimali per andare a selezionare le corrispondenti riga e colonna, ottenendo un valore 
            int indiceRiga = Integer.parseInt(bitRiga, 2);
            int indiceColonna = Integer.parseInt(bitColonna, 2);
            int valoreTrovato = S_BOXES.get(i)[0+(16*indiceRiga) + indiceColonna];
            
            //converto l'outputBox parziale in valore binario a 4-bit
            outputBox[i] += String.format("%4s", Integer.toBinaryString(valoreTrovato)).replace(' ', '0');
            
            outputFinale += outputBox[i]; //aggiorno l'output finale
        }
        
        /* Permutazione P */
        return Permutation(P, outputFinale);        
    }
    
    private static boolean bitOf(char in) {
        return (in == '1');
    }

    private static char charOf(boolean in) {
        return (in) ? '1' : '0';
    }

    public String XOR(String firstString, String secondString) {
        
        StringBuilder xoredString = new StringBuilder();
        for (int i = 0; i < firstString.length(); i++) {
            xoredString.append(charOf(bitOf(firstString.charAt(i)) ^ bitOf(secondString.charAt(i))));
        }
        return xoredString.toString();
    }

    private String StartPhases(String leftPlaintext, String rightPlaintext, String key, String mode) {
        
        String Li, Ri;
        String[] subkey = new String[16];
        
        //Ottengo C0 e D0 applicando la PC1 -> la chiave passa da 64 bit a 56 bit
        C = Permutation(LEFTPC1, key);
        D = Permutation(RIGHTPC1, key);
        
        //Genero le subkey
        for (int i = 0; i < 16; i++) {
            subkey[i] = generateSubkey(C, D, NUM_SHIFT[i]); 
        }
        
        /* SVOLGIMENTO 16 FASI */
        if(mode.equals("encryption")) { 
            
            for (int i = 0; i < 16; i++) { 

                //Ottengo Li ed Ri
                Li = rightPlaintext;
                Ri = XOR(leftPlaintext, Feistel(rightPlaintext, subkey[i]));

                //Aggiorno L(i-1) e R(i-1)
                leftPlaintext = Li;
                rightPlaintext = Ri;
            }
        } else { //chiavi al contrario
            
            for (int i = 15; i >= 0; i--) { 
                       
                Li = rightPlaintext;
                Ri = XOR(leftPlaintext, Feistel(rightPlaintext, subkey[i]));

                leftPlaintext = Li;
                rightPlaintext = Ri;
            }
        }

        //32-bit Swap finale
        return rightPlaintext + leftPlaintext;
    }
}
