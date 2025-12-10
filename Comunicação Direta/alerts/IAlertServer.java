package alerts;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Interface de serviços que serão chamados pelo CardioServer
public interface IAlertServer extends Remote {

    void notifyHighBPM(int bpm) throws RemoteException;

    void notifySuddenIncrease(int before, int now) throws RemoteException;
}

