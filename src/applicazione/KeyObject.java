package applicazione;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Giovanni Calà
**/

public class KeyObject implements Serializable {
    
    private BigInteger value;
    private BigInteger n;
    
    public KeyObject(BigInteger value, BigInteger n) {
        this.value = value;
        this.n = n;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getN() {
        return n;
    }
    
    public void setValue(BigInteger value) {
        this.value = value;
    }
    
    public void setN(BigInteger n) {
        this.n = n;
    }
}
