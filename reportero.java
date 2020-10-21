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

        // Se llama a una funcion que esperara que el Verificador envio datos de los
        // sensores
        awaitSensorData();
    }

    // Esta funcion crea un Socket que espera al los datos enviados por el
    // Verificador
    public static void awaitSensorData() {

        try {

            // Se crea un ServerSocket para esperar los datos
            ServerSocket ss = new ServerSocket(puerto);
            System.out.println("Esperando a recibir datos en el puerto: 9999\n");
            Socket s = ss.accept();

            // Se crea un data inputStream que espera los datos
            DataInputStream dis = new DataInputStream(s.getInputStream());

            // Los datos son de tipo JSON en formato String UTF, por lo que se debe hacer el
            // unmarshalling
            String encodedJSON = (String) dis.readUTF();

            System.out.println(encodedJSON);
            System.out.println("Mensaje JSON recibido, procediendo al unmarshalling\n");

            // Se envia los datos recibidos para hacer el unmarshalling

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

    // Este funcion recibe el JSON en formato String, y lo parsea en un JSONObject
    public static void readSensorData(String encodedJSON) {

        // Se crea un parser para crear el objecto JSONObject
        JSONParser parser = new JSONParser();

        String agentId;
        JSONObject sensorData;
        String time;

        try {
            // sensorData el es Objeto JSON que tendra la informacion de nuestro sensor
            sensorData = (JSONObject) parser.parse(encodedJSON);
            agentId = sensorData.get("agente").toString();
            time = sensorData.get("fechahoraUTC").toString();
            // Las lecturas del sensor seran manejadas en un JSONArray
            JSONArray lecturas = (JSONArray) sensorData.get("lecturas");

            String nombreSensor;
            double lecturaValor;

            // Para actualizar cada registro de sensor de cada agente, se crea un loop que
            // recorrera todos los sensores de una lectura con su respectivo agente y
            // buscara si tal registro exista para lo cual llamara la funcion
            // updateSensorLog enviados los datos necesarios para crear o actualizar los
            // registros de casa sensor
            for (int i = 0; i < lecturas.size(); i++) {

                JSONObject itemObj = (JSONObject) lecturas.get(i);
                nombreSensor = (String) itemObj.get("sensor");
                lecturaValor = ((Number) itemObj.get("lectura")).doubleValue();

                updateSensorLog(agentId, nombreSensor, lecturaValor, time);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    // Esta funcion lee los regitros existentes, o los crea si no existen
    // Tambien actualiza los registros con las nuevas lectura, y calcula el max, min
    // y med de las lecturas
    public static void updateSensorLog(String agentId, String sensorId, double sensorValue, String time) {

        JSONObject sensorLog;

        // Se asigna el nombre de archivo con el nombre del agente y el nombre del
        // sensor
        String filename = agentId + "-" + sensorId + ".json";

        // Se busca si el archivo de registro existe en la carpeta de registros
        File f = new File("./registros/" + filename);
        if (f.exists() && !f.isDirectory()) {

            System.out.println("Se econtro el registro del sensor: " + sensorId + " del agente: " + agentId);
            System.out.println("Ingresando el nuevo valor de lectura: " + sensorValue);

            // Si el registro existe, se lo lee y se lo asigna a sensorLog
            sensorLog = readJSON("./registros/" + agentId + "-" + sensorId + ".json");

            // Para saber si la nueva lectura cambia el minimo o maximo del registro,
            // se lee el Maximo y el Minimo, y se lo compare con el valor de la lectura del
            // sensor
            double logMin = (double) sensorLog.get("lectura_min");
            double logMax = (double) sensorLog.get("lectura_max");

            if (sensorValue < logMin) {
                // si el valor es menor, se actualiza el minimo
                logMin = sensorValue;
            }
            if (sensorValue > logMax) {
                // si el valor es mayor, se actualiza el maximo
                logMax = sensorValue;
            }

            // Para encontrar la nueva media de las lecturas, es necesario tomar las
            // lecturas como un JSONArray, a la cual se le agrega un nuevo JSONObject con la
            // nueva lectura
            JSONArray sensorLogArray = (JSONArray) sensorLog.get("lecturas");

            JSONObject lectureObj = new JSONObject();
            lectureObj.put("fechahoraUTC", time);
            lectureObj.put("lectura", sensorValue);

            sensorLogArray.add(lectureObj);

            // Una vez agregado la nueva lectura, se itera en el array, sumando el total de
            // todos los valores de la lectura, y dividiendola por el numero total de
            // lecturas
            // para obtener el valor de la media de las lecturas

            double logMed = 0;

            for (int i = 0; i < sensorLogArray.size(); i++) {
                System.out.println(((JSONObject) sensorLogArray.get(i)).get("lectura"));
                logMed += ((Number) ((JSONObject) sensorLogArray.get(i)).get("lectura")).doubleValue();
            }

            logMed = logMed / sensorLogArray.size();

            // Finalmente se agregan los valores actualizaron al JSON

            sensorLog.put("lectura_max", logMax);
            sensorLog.put("lectura_min", logMin);
            sensorLog.put("lectura_med", logMed);
            sensorLog.put("lecturas", sensorLogArray);

        } else {

            // En caso de que el archivo de registro no existe, se creara el archivo
            // pertinente

            System.out.println("No existe el registro del sensor: " + sensorId + " del agente: " + agentId);

            // Primero se crea el archivo JSON, y se agregan los datos pertinentes al objeto

            sensorLog = new JSONObject();

            sensorLog.put("agente", agentId);
            sensorLog.put("sensor", sensorId);

            // Debido a que solo hay un valor de lectura al crear el archivo, este equivale
            // al minimo, maximo y media por lo que no hay que hacer otros calculos

            sensorLog.put("lectura_min", sensorValue);
            sensorLog.put("lectura_max", sensorValue);
            sensorLog.put("lectura_med", sensorValue);

            // Se crea un JSONArray para las lecturas y se agrega la lectura enviada

            JSONArray sensorLogArray = new JSONArray();

            JSONObject lectureObj = new JSONObject();
            lectureObj.put("fechahoraUTC", time);
            lectureObj.put("lectura", sensorValue);

            sensorLogArray.add(lectureObj);
            sensorLog.put("lecturas", sensorLogArray);

        }

        // Una vez creado o modificado el archivo de registro, se utiliza Filewriter
        // para guardarlo en el disco, utilizando el filename que equivale al nombre del
        // agente y el nombre del sensor pertintente

        try (FileWriter file = new FileWriter("registros/" + filename)) {

            file.write(sensorLog.toJSONString());
            file.flush();

            // Cuando se guarda el archivo, se muestra por consola una confirmacion, ademas
            // de los datos pertinentes como el agente, el sensor y los valores del maximo,
            // minimo y media actualizados

            System.out.println("--- Se ha actualizado el registro exitosamente");
            System.out.println("Cambios: Agente: " + agentId + ", Sensor: " + sensorId + ", Minima: "
                    + sensorLog.get("lectura_min") + ", Maxima: " + sensorLog.get("lectura_max") + ", Media: "
                    + sensorLog.get("lectura_med"));

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    // Esta funcion recibe la ubicacion del JSON y retorna un JSONObject
    public static JSONObject readJSON(String url) {

        // JSON parser para crear el JSON a partir del archivo leido
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(url)) {
            // Se lee el archivo JSON
            Object obj = jsonParser.parse(reader);

            // se crea un JSONObject con el resultado del parser
            JSONObject response = (JSONObject) obj;

            // Se retorna el JSONObject
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