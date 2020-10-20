// Sistermas Distribuidos 
// Giuseppe Carmigniani
// Proyecto - primer parcial

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

import java.io.*;
import java.net.*;
import java.util.Iterator;

// Esta clase utilizara el Servidor que recibira los numeros
public class reportero {

    // este puerto se utilizara para la comunicacion
    public static int puerto = 9999;

    public static void main(String[] args) {

        awaitSensorData();
    }

    public static void awaitSensorData() {

        try {
            // Se crea un ServerSocket para esperar los mensajes
            ServerSocket ss = new ServerSocket(puerto);
            System.out.println("Esperando a recibir mensaje en el puerto: 9999\n");
            Socket s = ss.accept();

            // Se crea un data inputStream que espera el numero de parte del cliente
            DataInputStream dis = new DataInputStream(s.getInputStream());

            String encodedJSON = (String) dis.readUTF();
           
            System.out.println(encodedJSON);
            System.out.println("Mensaje JSON recibido, procediendo al unmarshalling\n");

            readSensorData(encodedJSON);
            // Se cierra el puerto del servidor
            ss.close();

            System.out.println("Fin de la ejecucion, cerrando el servidor");
            awaitSensorData();

        } catch (Exception e) {

            System.out.println("Hubo un error al crear el servidor");
            System.out.println(e);

        }

    }

    public static void readSensorData(String encodedJSON) {

        JSONParser parser = new JSONParser();
        String agentId;
        JSONObject sensorData;
        String time;

        try {

            sensorData = (JSONObject) parser.parse(encodedJSON);
            agentId = sensorData.get("agente").toString();
            time = sensorData.get("fechahoraUTC").toString();

            JSONArray lecturas = (JSONArray) sensorData.get("lecturas");

            String nombreSensor;
            double lecturaValor;
           

            for (int i = 0; i < lecturas.size(); i++) {
                JSONObject itemObj = (JSONObject) lecturas.get(i);
                nombreSensor = (String) itemObj.get("sensor");
                lecturaValor =((Number) itemObj.get("lectura")).doubleValue();

                updateSensorLog(agentId, nombreSensor, lecturaValor, time);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public static void updateSensorLog(String agentId, String sensorId, double sensorValue, String time) {

        JSONObject sensorLog;

        String filename = agentId + "-" + sensorId + ".json";

        File f = new File("./registros/" + filename);
        if (f.exists() && !f.isDirectory()) {

            System.out.println("Se econtro el registro del sensor: " + sensorId + " del agente: " + agentId);
            System.out.println("Ingresando el nuevo valor de lectura: " + sensorValue);

            sensorLog = readJSON("./registros/" + agentId + "-" + sensorId + ".json");

            double logMin = (double) sensorLog.get("lectura_min");
            double logMax = (double) sensorLog.get("lectura_max");

            if (sensorValue < logMin) {
                logMin = sensorValue;
            }
            if (sensorValue > logMax) {
                logMax = sensorValue;
            }

           

            

            JSONArray sensorLogArray = (JSONArray) sensorLog.get("lecturas");

            JSONObject lectureObj = new JSONObject();
            lectureObj.put("fechahoraUTC", time);
            lectureObj.put("lectura", sensorValue);

            sensorLogArray.add(lectureObj);
            

            double logMed = 0;

            for (int i = 0; i < sensorLogArray.size(); i++) {
                System.out.println(((JSONObject)sensorLogArray.get(i)).get("lectura"));
                logMed +=((Number) ((JSONObject)sensorLogArray.get(i)).get("lectura")).doubleValue();
            }

            logMed = logMed / sensorLogArray.size();

            sensorLog.put("lectura_max", logMax);
            sensorLog.put("lectura_min", logMin);
            sensorLog.put("lectura_med", logMed);

            sensorLog.put("lecturas", sensorLogArray);

        } else {

            System.out.println("No existe el registro del sensor: " + sensorId + " del agente: " + agentId);

            // create file

            sensorLog = new JSONObject();

            sensorLog.put("agente", agentId);
            sensorLog.put("sensor", sensorId);
            sensorLog.put("lectura_min", sensorValue);
            sensorLog.put("lectura_max", sensorValue);
            sensorLog.put("lectura_med", sensorValue);

            JSONArray sensorLogArray = new JSONArray();

            JSONObject lectureObj = new JSONObject();
            lectureObj.put("fechahoraUTC", time);
            lectureObj.put("lectura", sensorValue);

            sensorLogArray.add(lectureObj);
            sensorLog.put("lecturas", sensorLogArray);

    
        }

        try (FileWriter file = new FileWriter("registros/" + filename)) {

            file.write(sensorLog.toJSONString());
            file.flush();

            System.out.println("--- Se ha actualizado el registro exitosamente");
            System.out.println(
                "Cambios: Agente: " + agentId + 
                ", Sensor: " + sensorId + 
                ", Minima: "+sensorLog.get("lectura_min")+
                ", Maxima: "+sensorLog.get("lectura_max")+
                ", Media: "+sensorLog.get("lectura_med")
                );

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    public static JSONObject readJSON(String url) {
        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(url)) {
            // Read JSON file
            Object obj = jsonParser.parse(reader);

            // JSONArray JSONsensor = (JSONArray) obj;
            JSONObject response = (JSONObject) obj;
            // System.out.println(response);
            return response;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONObject response = new JSONObject();
        return response;

    }

}