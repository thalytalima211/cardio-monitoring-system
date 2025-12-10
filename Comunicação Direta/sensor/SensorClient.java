package sensor;

import server.ICardioServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

// Classe que representa o sensor cardíaco (cliente)
public class SensorClient {

    private static final Random random = new Random();

    private static final int baseBpm = 80;        // valor médio
    private static final int noiseRange = 8;      // +- 8 bpm de oscilação
    private static final boolean simulatePeaks = true;

    public static void main(String[] args) {
        Registry registry = null;

        try {
            // acesso ao servidor RMI na porta 8888
            registry = LocateRegistry.getRegistry(8888);
        } catch (RemoteException r) {
            r.printStackTrace();
            System.exit(0);
        }

        try {
            // procura o "Servidor_E-Health" no servidor
            ICardioServer server = (ICardioServer) registry.lookup("Servidor_E-Health");
            System.out.println("Sensor conectado ao Servidor E-Heath!");

            // envia BPM a cada 1 segundo
            while (true) {
                int bpm = generateBpm();
                System.out.println("[SensorClient] Gerado e enviado BPM: " + bpm);
                server.sendBPM(bpm);

                Thread.sleep(1000);
            }

        } catch (RemoteException | NotBoundException | InterruptedException r) {
            r.printStackTrace();
        }
    }

    private static int generateBpm() {
        int noise = random.nextInt(noiseRange * 2) - noiseRange;
        int bpm = baseBpm + noise;

        // 1,5% de chance de pico
        if (simulatePeaks && random.nextDouble() < 0.015) {
            return 175 + random.nextInt(15); // entre 175 e 189
        }

        return Math.max(40, bpm); // não deixa cair abaixo de 40
    }
}
