package crittografia;
 
import java.util.Scanner;
import java.math.BigInteger;
 
public class MillerRabin {
    
    private final Scanner scan;
    private long n, a, j, k, q, primaCondizione, secondaCondizione;
    private int iteration;
    boolean check;
    
    public MillerRabin() {
        scan = new Scanner(System.in);
        iteration = 10;
    }
    
    public void Start() {
        
        System.out.println("\nInserisci un numero: ");
        n = scan.nextLong();

        check = isPrime(n);
        
        if (check)
            System.out.println("\n" + n +" è probabilmente un numero primo");
        else
            System.out.println("\n" + n + " è sicuramente un composito");
    }
    
    public boolean isPrime(long n) {
        
        /* Casi base */
        if (n == 0 || n == 1)
            return false;
        if (n == 2)
            return true;
        if (n % 2 == 0)
            return false;
 
        /* Trovo k e q */
        k = 0;
        q = n-1;
        
        /* Condizione da verificare: (n-1) = 2^k * q */
        while (q % 2 == 0) { //finchè non viene trovato il numero dispari
            q /= 2;
            k++;
        }
        
        /* Controllo se viene verificata almeno una delle due condizioni */
        for (int i = 0; i < iteration; i++) {

            j = 0; //contatore per la seconda condizione
            a = (long)(Math.random() * ((n-1) - 1) + 1); // 1 <= a <= (n-1)
            
            primaCondizione = potenzaConModulo(a, q, n); // a^q (mod n) = 1
            secondaCondizione = primaCondizione;         // a^((2^j)*q) (mod n) = (n-1), per 0 <= j <= k-1 

            if(primaCondizione != 1) { //se non viene verificata la prima condizione -> verifico la seconda
                
                while (j != k-1 && secondaCondizione != n-1) {
                    
                    //faccio la seconda condizione al quadrato (come se incrementassi j di 1)
                    secondaCondizione = prodottoConModulo(secondaCondizione, secondaCondizione, n); 
                    j++;
                }
                if (secondaCondizione != n-1)
                    return false;
            }
        }
        return true;        
    }

    public long potenzaConModulo(long a, long q, long n) {
        
        /* Operazione da effettuare: (a^q) (mod n) */
        return BigInteger.valueOf(a).modPow(BigInteger.valueOf(q), BigInteger.valueOf(n)).longValue();
    }
    
    private long prodottoConModulo(long a, long a2, long n) {
        
        /* Operazione da effettuare: (a*a) (mod n) */
        return BigInteger.valueOf(a).multiply(BigInteger.valueOf(a2)).mod(BigInteger.valueOf(n)).longValue();
    }
}