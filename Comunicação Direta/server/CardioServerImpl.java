package server;

import alerts.IAlertServer;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

// Implementa os métodos da interface server.ICardioServer
public class CardioServerImpl implements ICardioServer {

    private static int lastBpm = -1;              // armazena último valor
    private static final int HIGH_THRESHOLD = 170; // valor considerado alto
    private static IAlertServer alertServer;       // referência remota

    @Override
    public void sendBPM(int bpm) throws RemoteException {
        System.out.println("[CardioServer] Recebido BPM: " + bpm);

        // Verifica BPM alto
        if (alertServer != null && bpm > HIGH_THRESHOLD) {
            alertServer.notifyHighBPM(bpm);
            System.out.println("[CardioServer] ALERTA disparado -> BPM muito alto!");
        }

        // Verifica aumento brusco
        if (alertServer != null && lastBpm != -1 && (bpm - lastBpm) > 10) {
            alertServer.notifySuddenIncrease(lastBpm, bpm);
            System.out.println("[CardioServer] ALERTA disparado -> Aumento brusco detectado!");
        }


        lastBpm = bpm;
    }

    public static void main(String[] args) {
        Registry alertRegistry = null;

        // 1. Conectar ao servidor de alertas
        try {
            // acesso ao servidor RMI na porta 9999
            alertRegistry = LocateRegistry.getRegistry(9999);
        } catch (RemoteException r) {
            r.printStackTrace();
            System.exit(0);
        }

        try {
            // procura o "Servidor_de_Alerta" no servidor
            alertServer = (IAlertServer) alertRegistry.lookup("Servidor_de_Alerta");
            System.out.println("Conectado ao Servidor de Alerta!");
        } catch (RemoteException | NotBoundException r) {
            r.printStackTrace();
        }

        // 2. Criar servidor principal RMI
        String nome = "Servidor_E-Health";
        Remote remote = new CardioServerImpl();

        // versão remota do servidor
        ICardioServer stub = null;

        try {
            // transforma o objeto local em objeto remoto
            stub = (ICardioServer) UnicastRemoteObject.exportObject(remote, 0);
            Registry registry = null;

            try {
                // cria um RMI na porta 8888
                registry = LocateRegistry.createRegistry(8888);
            } catch (RemoteException r) {
                registry = LocateRegistry.getRegistry(8888);
            }

            // registra a versão remota do servidor na porta
            registry.rebind(nome, stub);

            System.out.println("Servidor E-Heath pronto...");
        } catch (RemoteException rem) {
            rem.printStackTrace();
        }
    }
}
