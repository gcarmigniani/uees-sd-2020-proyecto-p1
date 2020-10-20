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
            System.out.println("Esperando a recibir mensaje en el puerto: 9999");
            Socket s = ss.accept();

            // Se crea un data inputStream que espera el numero de parte del cliente
            DataInputStream dis = new DataInputStream(s.getInputStream());

            String encodedJSON = (String) dis.readUTF();
            System.out.println(encodedJSON);

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

        try {
            sensorData = (JSONObject) parser.parse(encodedJSON);
            agentId = sensorData.get("agente").toString();
           
            System.out.println(sensorData.get("lecturas"));

            // loop de todos los sensores de lecturas

            // updateSensorLog(agentId, sensorId)




        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public static void updateSensorLog(String agentId, String sensorId){

        // Busca si existe JSON con el Id del Agente y Sensor
        // Si no existe crear uno

        // comparar el valor si es un nuevo minimo o maximo
        // agregar valor a la lista de valores
        // calcular el nuevo valor medio

        // guardar JSON

        // sensorData.put("agente",agentId)
        // sensorData.put("sensor",sensorId)
        // sensorData.put("lectura_min",)
        // sensorData.put("lectura_max","")
        // sensorData.put("lectura_med","")
        // sensorData.put("lecturas","")

        // una ves actualizado, mostrar el cambio en consola


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