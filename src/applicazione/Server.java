package applicazione;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    
    private static int uniqueId; //ID unico per ogni connessione
    private ArrayList<ClientThread> clientList; //lista per tenere traccia dei Client
    private final int PORT;
    private boolean isActive; //per controllare se il Server è attivo
    private String notifStr = " *** "; 

    public Server(int port) {
        this.PORT = port;
        clientList = new ArrayList<ClientThread>();
    }

    public static void main(String[] args) {
        
        int portNumber = 3434; //inizializza il Server su questa porta a meno che non viene specificata
        
        Server server = new Server(portNumber);
        server.start();
    }
    
    public void start() {
        
        isActive = true;
        
        /* Crea il ServerSocket e attendi le richieste di connessione...*/
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while(isActive) {
                
                display("Server in attesa sulla porta " + PORT + "...");
                Socket socket = serverSocket.accept(); //accetta la connessione se viene richiesta dal Client
                
                if(!isActive)
                    break;
                
                ClientThread t = new ClientThread(socket); //una volta connesso, al Client sarà dedicato un Thread apposito
                clientList.add(t); //aggiungi il Client alla lista
                t.start();
            }

            /* Chiusura del Server */
            try {
                serverSocket.close();
                
                for(int i = 0; i < clientList.size(); ++i) {
                    
                    ClientThread tc = clientList.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                    }
                }
            }
            catch(IOException e) {
                display("Eccezione durante la chiusura del Server e dei Client: " + e);
            }
        }
        catch (IOException e) {
            display("Eccezione sulla nuova ServerSocket: " + e + "\n");
        }
    }

    /* Per chiudere il Server */
    protected void stop() {
        
        isActive = false;
        try {
            new Socket("localhost", PORT);
        }
        catch(IOException e) {
        }
    }

    /* Per mostrare un messaggio nella Console */
    private void display(String msg) {
        System.out.println(msg);
    }

    /* Per trasmettere un messaggio */ 
    private synchronized boolean broadcast(String message) {
        
        /* Controlla se il messaggio è privato, cioè Client1 -> Client2 */
        String[] w = message.split(" ",3);
     
        boolean isPrivate = false;
        if(w[1].charAt(0)=='@') 
            isPrivate = true;

        if(isPrivate == true) { //invia il messaggio solo all'username specificato

            System.out.print(message + "\n");
            
            String tocheck = w[1].substring(1, w[1].length()); //contiene il nome del destinatario da verificare

            message = w[0] + " " + w[2]; //è del tipo: 'mittente: ciao'
            boolean found = false;
            
            /* Viene eseguito un ciclo al contrario per trovare l'username specificato */
            for(int y = clientList.size(); --y >= 0;) {
               
                ClientThread ct1 = clientList.get(y);
                String check = ct1.getUsername();
                
                if(check.equals(tocheck)) {
                    
                    /* Prova a scrivere un messaggio al Client, e se fallisce rimuovilo dalla lista */
                    if(!ct1.writeMsg(new TypeMessage(TypeMessage.MESSAGE, message))) {
                        clientList.remove(y);
                        display("Client disconnesso " + ct1.username + " rimosso dalla lista.");
                    }
                    
                    found = true; //username trovato e messaggio consegnato
                    break;
                }
            }
            
            if(found != true) { //utente specificato non trovato
                return false; 
            }
        }
        
        else { //se è un messaggio per tutti i Client (broadcast)
           
            String msg = message + "\n";
            System.out.print(msg);

            /* Viene effettuato un ciclo inverso nel caso in cui bisogna rimuovere un Client perchè si è disconnesso */
            for(int i = clientList.size(); --i >= 0;) {
                
                ClientThread ct = clientList.get(i);
                
                if(!ct.writeMsg(new TypeMessage(TypeMessage.INFO, msg))) {
                    clientList.remove(i);
                    display("Client disconnesso " + ct.username + " rimosso dalla lista.");
                }
            }
        }
        return true;
    }

    private synchronized boolean broadcastKey(String message, KeyObject key, int typeMsg) {
           
        /* Controlla se il messaggio è privato, cioè Client1 -> Client2 */
        String[] w = message.split(" ", 3);
     
        if(w[1].charAt(0)!='@') 
            return false;
        
        else {
            
            System.out.print(message + "\n");
            
            String tocheck = w[1].substring(1, w[1].length()); //contiene il nome del destinatario da verificare

            message = w[0] + " " + w[2]; //è del tipo: 'mittente: ciao'
            boolean found = false;
            
            /* Viene eseguito un ciclo al contrario per trovare l'username specificato */
            for(int y = clientList.size(); --y >= 0;) {
               
                ClientThread ct1 = clientList.get(y);
                String check = ct1.getUsername();
                
                if(check.equals(tocheck)) {
                    
                    /* Prova a scrivere un messaggio al Client, e se fallisce rimuovilo dalla lista */
                    if(!ct1.writeMsg(new TypeMessage(typeMsg, message, key))) {
                        clientList.remove(y);
                        display("Client disconnesso " + ct1.username + " rimosso dalla lista.");
                    }
                    found = true; //username trovato e messaggio consegnato
                    break;
                }
            }
            if(found != true) { //utente specificato non trovato
                return false; 
            }
        }
        return true;
    }
    
    /* Se il Client invia il messaggio di LOGOUT per uscire */
    synchronized void remove(int id) {

        String disconnectedClient = "";
        
        /* Cerca nella lista finchè non viene trovato l'ID */
        for(int i = 0; i < clientList.size(); ++i) {
            
            ClientThread ct = clientList.get(i);
          
            if(ct.id == id) { //rimuoviamolo 
                disconnectedClient = ct.getUsername();
                clientList.remove(i);
                break;
            }
        }
        broadcast(notifStr + disconnectedClient + " si è disconnesso" + notifStr);
    }

    /* Un'istanza di questo thread verrà eseguita per ogni Client */
    class ClientThread extends Thread {

        Socket socket; //socket da cui ricevere messaggi dal Client
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;

        int id; //ID unico per questa connessione
        String username; //username del Client
        
        TypeMessage messaggio; 
        
        ClientThread(Socket socket) {
            
            id = ++uniqueId;
            this.socket = socket;
            
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());

                username = (String) sInput.readObject(); //leggi l'username del Client
                broadcast(notifStr + username + " è ora connesso" + notifStr);
            }
            catch (IOException e) {
                display("Eccezione nella creazione di nuovi flussi I/O: " + e);
                return;
            }
            catch (ClassNotFoundException e) {
            }
        }

        /* Ciclo infinito per leggere ed inviare messaggi */
        public void run() {
            
            boolean isActive = true;
            
            while(isActive) {
                    
                /* Leggi una stringa (oggetto TypeMessage) */
                try {
                    messaggio = (TypeMessage) sInput.readObject();
                }
                catch (IOException e) {
                    //display(username + " Eccezione nella lettura dei flussi: " + e);
                    break;				
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                
                /* Ottieni il messaggio (stringa) dal messaggio di tipo TypeMessage ricevuto */
                String message = messaggio.getMessage();

                /* Diverse azioni in base al tipo di messaggio */
                switch(messaggio.getType()) {

                    case TypeMessage.MESSAGE:
                        
                        boolean confirmation = broadcast(username + ": " + message);
                        
                        if(confirmation == false) {
                            String msg = notifStr + "Mi dispiace, non esiste nessun utente con quel nome." + notifStr;
                            writeMsg(new TypeMessage(TypeMessage.INFO, msg));
                        }
                        break;
                        
                    case TypeMessage.SESSIONE:
                        
                        boolean confirmation2 = broadcastKey(username + ": " + message, messaggio.getKeyObject(), TypeMessage.SESSIONE);
                        
                        if(confirmation2 == false) {
                            String msg = notifStr + "Mi dispiace, non esiste nessun utente con quel nome." + notifStr;
                            writeMsg(new TypeMessage(TypeMessage.INFO, msg));
                        }
                        break;
                        
                    case TypeMessage.LOGOUT:
                        display(username + " disconnesso.");
                        isActive = false;
                        break;
                }
            }
            remove(id);
            close();
        }

        /* Chiudi tutto */
        private void close() {
            try {
                if(sOutput != null) sOutput.close();
                if(sInput != null) sInput.close();
                if(socket != null) socket.close();
            }
            catch(IOException e) {}
        }

        /* Scrivi un messaggio (stringa) al Client sul flusso di output */
        private boolean writeMsg(TypeMessage msg) {

            if(!socket.isConnected()) { //se il Client non è connesso
                close();
                return false;
            }
            
            try { //scrivi il messaggio
                sOutput.writeObject(msg);
            }
            catch(IOException e) { //non chiudere tutto, ma informa l'utente dell'errore
                display(notifStr + "Errore durante l'invio del messaggio a " + username + notifStr);
                display(e.toString());
            }
            return true;
        }
        
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}

