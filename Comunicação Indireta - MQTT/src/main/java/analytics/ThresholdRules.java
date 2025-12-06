package analytics;

public class ThresholdRules {
    // BPM
    public static final int MAX_BPM = 170;      // acima disso é alerta
    public static final int MIN_BPM = 40;       // abaixo disso é problema (só de referência)

    // Calorias
    public static final double CALORIES_GOAL = 300.0; // meta diária/sessão para alerta

    // Pace (min/km)
    public static final double MIN_PACE = 2.5;  // muito intenso (rápido) -> alerta (ex.: < 2.5 min/km)
    public static final double MAX_PACE = 8.0;  // muito lento -> alerta (opcional)

    // Buffer sizes (quantos últimos valores manter)
    public static final int HISTORY_SIZE = 12; // com intervalo 5s = ~60s de histórico
}