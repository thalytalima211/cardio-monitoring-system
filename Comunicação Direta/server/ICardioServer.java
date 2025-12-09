package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Interface de serviços que serão oferecidos servidor cardio
public interface ICardioServer extends Remote {

    void sendBPM(int bpm) throws RemoteException;

}
