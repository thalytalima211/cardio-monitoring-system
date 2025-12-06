package analytics;

import org.eclipse.paho.client.mqttv3.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * Recebe mensagens dos sensores, mantém históricos simples, calcula médias e publica
 * analytics e alertas quando limites são ultrapassados.
 */
public class AnalyticsCallback implements MqttCallback {
    private final MqttAsyncClient client;

    // históricos como filas simples (FIFO)
    private final Deque<Integer> bpmHistory = new ArrayDeque<>();
    private final Deque<Double> caloriesHistory = new ArrayDeque<>();
    private final Deque<Double> paceHistory = new ArrayDeque<>();

    public AnalyticsCallback(MqttAsyncClient client) {
        this.client = client;
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("[Analytics] Conexão perdida: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        // simples visualização
        // System.out.println("[Analytics] msgReceived -> " + topic + " : " + payload);

        try {
            if (topic.equals("cardio/sensor/bpm")) {
                Integer bpm = parseIntFromJson(payload, "bpm");
                if (bpm != null) {
                    pushBpm(bpm);
                    double avg = averageBpm();
                    publishAnalytics("cardio/analytics/bpm/average", "{\"average_bpm\":" + String.format("%.2f", avg) + "}");
                    checkBpmAlert(bpm, avg, payload);
                }
            } else if (topic.equals("cardio/sensor/calories")) {
                Double cals = parseDoubleFromJson(payload, "calories");
                if (cals != null) {
                    pushCalories(cals);
                    double latest = cals;
                    double avg = averageCalories();
                    publishAnalytics("cardio/analytics/calories/latest", "{\"latest_calories\":" + String.format("%.2f", latest) + "}");
                    publishAnalytics("cardio/analytics/calories/average", "{\"average_calories\":" + String.format("%.2f", avg) + "}");
                    checkCaloriesAlert(latest, payload);
                }
            } else if (topic.equals("cardio/sensor/pace_speed")) {
                Double pace = parseDoubleFromJson(payload, "pace_min_km"); // campo do sensor
                Double speed = parseDoubleFromJson(payload, "speed_kmh");
                if (pace != null) {
                    pushPace(pace);
                    double avg = averagePace();
                    publishAnalytics("cardio/analytics/pace/average", "{\"average_pace_min_km\":" + String.format("%.2f", avg) + "}");
                    checkPaceAlert(pace, speed, payload);
                }
            } else {
                // topicos inesperados
                System.out.println("[Analytics] Tópico desconhecido: " + topic + " -> " + payload);
            }
        } catch (Exception ex) {
            System.err.println("[Analytics] Erro processando mensagem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // não precisamos de ação aqui agora
    }

    // ------- históricos e utilitários -------

    private void pushBpm(int bpm) {
        if (bpmHistory.size() >= ThresholdRules.HISTORY_SIZE) bpmHistory.removeFirst();
        bpmHistory.addLast(bpm);
        System.out.println("[Analytics] BPM recebido: " + bpm + " | média=" + String.format("%.2f", averageBpm()));
    }

    private void pushCalories(double c) {
        if (caloriesHistory.size() >= ThresholdRules.HISTORY_SIZE) caloriesHistory.removeFirst();
        caloriesHistory.addLast(c);
        System.out.println("[Analytics] Calorias acumuladas: " + String.format("%.2f", c));
    }

    private void pushPace(double p) {
        if (paceHistory.size() >= ThresholdRules.HISTORY_SIZE) paceHistory.removeFirst();
        paceHistory.addLast(p);
        System.out.println("[Analytics] Pace recebido: " + String.format("%.2f", p) + " min/km");
    }

    private double averageBpm() {
        if (bpmHistory.isEmpty()) return 0;
        return bpmHistory.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private double averageCalories() {
        if (caloriesHistory.isEmpty()) return 0;
        return caloriesHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double averagePace() {
        if (paceHistory.isEmpty()) return 0;
        return paceHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // ------- detecção de anomalias / geração de alertas -------

    private void checkBpmAlert(int bpm, double avg, String rawPayload) {
        if (bpm >= ThresholdRules.MAX_BPM) {
            String msg = "{\"sensor\":\"bpm\",\"bpm\":" + bpm + ",\"avg\":" + String.format("%.2f", avg) + ",\"raw\":" + quoteJson(rawPayload) + "}";
            publishAlert("cardio/alert/bpm", msg);
            System.out.println("[Analytics] ALERTA BPM -> " + msg);
        }
        if (bpm <= ThresholdRules.MIN_BPM) {
            String msg = "{\"sensor\":\"bpm\",\"bpm\":" + bpm + ",\"note\":\"too_low\",\"raw\":" + quoteJson(rawPayload) + "}";
            publishAlert("cardio/alert/bpm", msg);
            System.out.println("[Analytics] ALERTA BPM baixo -> " + msg);
        }
    }

    private void checkCaloriesAlert(double latestCalories, String rawPayload) {
        if (latestCalories >= ThresholdRules.CALORIES_GOAL) {
            String msg = "{\"sensor\":\"calories\",\"calories\":" + String.format("%.2f", latestCalories) + ",\"note\":\"goal_reached\",\"raw\":" + quoteJson(rawPayload) + "}";
            publishAlert("cardio/alert/calories", msg);
            System.out.println("[Analytics] ALERTA CALORIAS -> " + msg);
        }
    }

    private void checkPaceAlert(double pace, Double speed, String rawPayload) {
        // pace é min/km. Muito pequeno significa corrida muito rápida (intenso),
        // muito grande significa desaceleração/andar.
        if (pace <= ThresholdRules.MIN_PACE) {
            String msg = "{\"sensor\":\"pace\",\"pace_min_km\":" + String.format("%.2f", pace) + ",\"note\":\"too_intense\",\"raw\":" + quoteJson(rawPayload) + "}";
            publishAlert("cardio/alert/pace", msg);
            System.out.println("[Analytics] ALERTA PACE (intenso) -> " + msg);
        } else if (pace >= ThresholdRules.MAX_PACE) {
            String msg = "{\"sensor\":\"pace\",\"pace_min_km\":" + String.format("%.2f", pace) + ",\"note\":\"too_slow\",\"raw\":" + quoteJson(rawPayload) + "}";
            publishAlert("cardio/alert/pace", msg);
            System.out.println("[Analytics] ALERTA PACE (lento) -> " + msg);
        }
        // também pode detectar velocidade incoerente (opcional)
        if (speed != null && (speed < 0.5 || speed > 40.0)) {
            String msg = "{\"sensor\":\"speed\",\"speed_kmh\":" + String.format("%.2f", speed) + ",\"note\":\"speed_out_of_range\",\"raw\":" + quoteJson(rawPayload) + "}";
            publishAlert("cardio/alert/speed", msg);
            System.out.println("[Analytics] ALERTA SPEED -> " + msg);
        }
    }

    // ------- publicar analytics e alertas -------

    private void publishAnalytics(String topic, String payload) {
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(1);
            client.publish(topic, msg);
        } catch (Exception e) {
            System.err.println("[Analytics] Erro publicando analytics: " + e.getMessage());
        }
    }

    private void publishAlert(String topic, String payload) {
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(1);
            client.publish(topic, msg);
        } catch (Exception e) {
            System.err.println("[Analytics] Erro publicando alerta: " + e.getMessage());
        }
    }

    // ------- parsing simples (não usa libs externas) -------

    private Integer parseIntFromJson(String json, String key) {
        Double d = parseDoubleFromJson(json, key);
        return d == null ? null : d.intValue();
    }

    private Double parseDoubleFromJson(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx == -1) return null;
            int colon = json.indexOf(":", idx + search.length());
            if (colon == -1) return null;
            int start = colon + 1;
            // achar fim (vírgula ou })
            int endComma = json.indexOf(",", start);
            int endBrace = json.indexOf("}", start);
            int end = endComma;
            if (end == -1 || (endBrace != -1 && endBrace < end)) end = endBrace;
            if (end == -1) end = json.length();
            String rawVal = json.substring(start, end).trim();
            // remover aspas se houver
            if (rawVal.startsWith("\"") && rawVal.endsWith("\"")) rawVal = rawVal.substring(1, rawVal.length() - 1);
            return Double.parseDouble(rawVal);
        } catch (Exception ex) {
            return null;
        }
    }

    private String quoteJson(String raw) {
        // simplifica colocando raw entre aspas e escapando aspas internas
        String escaped = raw.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
