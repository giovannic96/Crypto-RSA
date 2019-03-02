package applicazione;

import crittografia.RSA;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class Client  {
    
    private final String notif = " *** ";

    private ObjectInputStream sInput;   //per leggere dalla socket
    private ObjectOutputStream sOutput; //per scrivere sulla socket
    private Socket socket;

    private final String SERVER; 
    private final int PORT; 	 
    private String username;	

    RSA rsa;
    
    KeyObject publicKeyOther;
    String sessionKey;
    boolean sessionEstablished = false;
    
    Client(String server, int port, String username) {
        this.SERVER = server;
        this.PORT = port;
        this.username = username;
        rsa = new RSA();
    }

    public static void main(String[] args) {

        /* Valori iniziali di default */
        int portNumber = 3434;
        String serverAddress = "localhost";
        String username = "MyName";
        Scanner scan = new Scanner(System.in);
        
        System.out.println("Inserisci il nome utente: ");
        do {
            username = scan.nextLine();
        } while(username.isEmpty() || username.contains(" "));
        
        Client client = new Client(serverAddress, portNumber, username);
        
        if(!client.start()) //se il Client non si riesce a connettere
            return;

        client.leggiInputInfinito(client);
    }
    
    private void leggiInputInfinito(Client client) {
       
        Scanner scan = new Scanner(System.in);
        
        /* Ciclo infinito per ottenere l'input dall'utente */
        while(true) {

            System.out.print("> ");
            String msg;
            do {
                msg = scan.nextLine();
            } while (msg.isEmpty());
            
            if(msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new TypeMessage(TypeMessage.LOGOUT, ""));
                break;
            }
            else if(msg.equalsIgnoreCase("SESSIONE")) {
                
                if(sessionEstablished) {
                    display("La sessione è già stata stabilita");
                    continue;
                }
                
                System.out.println("Con chi vuoi iniziare la sessione?");
                System.out.print("> ");
                String destinatario = scan.nextLine() + " ";
                
                if(destinatario.equals(this.getUsername() + " ") || destinatario.substring(0, destinatario.indexOf(" ")).equals(this.getUsername())) {
                    display("Mi dispiace, non puoi iniziare una sessione con te stesso.");
                    continue;
                }
                
                client.sendMessage(new TypeMessage(TypeMessage.SESSIONE, "@" + destinatario + " ", rsa.getPublicKey()));
            }
            else {
                if(msg.substring(0,1).equals("@")) { //se il messaggio ha un destinatario
                    
                    if(sessionEstablished) 
                        msg += " ";
                    else {
                        display("Mi dispiace, la sessione non è ancora stata stabilita.");
                        continue;
                    }
                } else msg = msg.replaceAll("\\s","");
                
                if(msg.substring(0,1).equals("@") && msg.substring(1, msg.indexOf(" ")).equals(this.getUsername())) {
                    display("Mi dispiace, non puoi inviare messaggi a te stesso.");
                    continue;
                }
                
                /* CRITTOGRAFIA RSA PER IL MESSAGGIO */
                String msgHeader = msg.substring(0, msg.indexOf(" ")+1); //il testo contenente: @ + nome del destinatario + spazio
                String msgBody = msg.substring(msg.indexOf(" ")+1); //perchè il testo da criptare è quello dopo lo spazio
                byte[] plainBytes = msgBody.getBytes(StandardCharsets.UTF_8);
                byte[] cipherBytes = rsa.encrypt(plainBytes, rsa.getPrivateKey());
                String encodedCipher = Base64.getEncoder().encodeToString(cipherBytes); //da byte a stringa di testo
                System.out.println("\nEncoded ciphertext: " + encodedCipher);
                client.sendMessage(new TypeMessage(TypeMessage.MESSAGE, msgHeader + encodedCipher));
            }  
        }
        scan.close(); 
        client.disconnect(); 	    
    }
    
    public boolean start() {
        
        /* Prova a connetterti con il server */
        try {
            socket = new Socket(SERVER, PORT);
        } 
        catch(IOException ec) {
            display("Errore durante la connessione al server:" + ec);
            return false;
        }

        String msg = "\nConnessione accettata " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        /* Creazione dei flussi di dati */
        try {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Eccezione nella creazione di nuovi flussi Input/Output: " + eIO);
            return false;
        }

        /* Creazione del Thread per ascoltare dal Server */
        new ListenFromServer().start();
        
        try {
            sOutput.writeObject(username); //invia l'username al Server
        }
        catch (IOException eIO) {
            display("Eccezione durante il login: " + eIO);
            disconnect();
            return false;
        }
        return true; //connessione al Server stabilita con successo
    }

    /* Per scrivere sulla Console */
    private void display(String msg) {
        System.out.println(msg);	
    }

    /* Per inviare un messaggio al Server */
    void sendMessage(TypeMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Eccezione durante l'invio di messaggi al Server: " + e);
        }
    }
    
    /* Disconnetti quando qualcosa va storto. Chiudi i flussi I/O e la socket */
    private void disconnect() {
        try { 
            if(sInput != null) sInput.close();
            if(sOutput != null) sOutput.close();
            if(socket != null) socket.close();
        }
        catch(IOException e) {
            display("Errore durante la disconnessione: " + e);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getSessionKey() {
        return this.sessionKey;
    }
    
    public void setSessionKey(String keyValue) {
        this.sessionKey = keyValue;
    }
    
    /* Thread che aspetta il messaggio dal Server */
    class ListenFromServer extends Thread {
        
        boolean publicKeyReceived = false;
        
        @Override
        public void run() {
            
            while(true) {

                try {
                    TypeMessage messaggio = (TypeMessage) sInput.readObject();
                    String msg = messaggio.getMessage();

                    switch (messaggio.getType()) {

                        case TypeMessage.MESSAGE:

                            /* DECRITTOGRAFIA RSA PER IL MESSAGGIO */
                            String msgHeader = msg.substring(0, msg.indexOf(" ")+1); //il testo contenente: @ + nome del mittente + spazio
                            String msgBody = msg.substring(msg.indexOf(" ")+1); //perchè il testo da decriptare è quello dopo lo spazio
                            byte[] decodedCipher = Base64.getDecoder().decode(msgBody);
                            System.out.println("\nDecoded ciphertext: " + Arrays.toString(decodedCipher));
                            byte[] decipherBytes = rsa.decrypt(decodedCipher, publicKeyOther);
                            display(msgHeader + new String(decipherBytes, StandardCharsets.UTF_8)); 
                            break;
                            
                        case TypeMessage.SESSIONE:
                            
                            if(!publicKeyReceived) {
                                
                                /* Ottengo la chiave pubblica del mittente */
                                publicKeyOther = new KeyObject(messaggio.getKeyObject().getValue(), messaggio.getKeyObject().getN());
                                display("Chiave pubblica ricevuta -> " + String.valueOf(publicKeyOther.getValue()) + ":" + publicKeyOther.getN());
                                sessionEstablished = true;
                                
                                /* Mando la mia chiave pubblica al mittente */
                                String destinatario = msg.substring(0, msg.indexOf(':'));
                                sendMessage(new TypeMessage(TypeMessage.SESSIONE, "@" + destinatario + " ", rsa.getPublicKey()));
                                publicKeyReceived = true;
                            }
                            break;
                            
                       default:
                            display(msg); //scrivi il messaggio
                            break;
                    }
                    System.out.print("> ");
                }
                catch(IOException e) {
                    display(notif + "Il Server ha chiuso la connessione: " + e + notif);
                    break;
                }
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}

