package alerts;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

// Implementa os métodos da interface alerts.IAlertServer
public class AlertServerImpl implements IAlertServer {

    @Override
    public void notifyHighBPM(int bpm) throws RemoteException {
        System.out.println("[AlertServer] BPM muito alto! BPM = " + bpm);
    }

    @Override
    public void notifySuddenIncrease(int before, int now) throws RemoteException {
        System.out.println("[AlertServer] Elevação brusca detectada! "
                + before + " → " + now);
    }

    public static void main(String[] args) {
        String nome = "Servidor_de_Alerta";
        Remote remote = new AlertServerImpl();

        // versão remota do servidor
        IAlertServer stub = null;

        try {
            // transforma o objeto local em objeto remoto
            stub = (IAlertServer) UnicastRemoteObject.exportObject(remote, 0);
            Registry registry = null;

            try {
                // cria um RMI na porta 9999
                registry = LocateRegistry.createRegistry(9999);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(9999);
            }

            // registra a versão remota do servidor na porta
            registry.rebind(nome, stub);

            System.out.println("Servidor de Alerta pronto...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

