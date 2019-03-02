package applicazione;

import java.io.Serializable;

/**
 * @author Giovanni Calà
**/

public class TypeMessage implements Serializable {

    static final int MESSAGE = 1, INFO = 2, LOGOUT = 3, SESSIONE = 4;
    
    private int type;
    private String message;
    private KeyObject keyObject;
    
    TypeMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    TypeMessage(int type, String message, KeyObject key) {
        this.type = type;
        this.message = message;
        this.keyObject = key;
    }
    
    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
    
    KeyObject getKeyObject() {
        return keyObject;
    }
}
