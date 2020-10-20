
// Sistermas Distribuidos 
// Giuseppe Carmigniani
// Proyecto - primer parcial

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

// Esta clase utilizara el cliente para enviar los numeros al servidor
public class verificador {

    // este puerto se utilizara para la comunicacion
    public static int puerto = 9999;

    public static void main(String[] args) {

        checkSensorDataFolder();

    }

    public static void checkSensorDataFolder() {

        JSONObject history = readJSON("./lecturas/history.json");

        File folder = new File("./lecturas");
        File[] listOfFiles = folder.listFiles();

        System.out.println("\n Archivos en la carpeta lectura: \n");

        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {

                System.out.println("\n- " + listOfFiles[i].getName());

                String extension = listOfFiles[i].getName().substring(listOfFiles[i].getName().length() - 5);

                if (!extension.equalsIgnoreCase(".json")) {
                    System.out.println(
                            "--- El archivo " + listOfFiles[i].getName() + " no es JSON, moviendolo a carpeta 'otros'");

                    try {

                        Path temp = Files.move(Paths.get(listOfFiles[i].getAbsolutePath()),
                                Paths.get("./otros/" + listOfFiles[i].getName()));

                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                } else if (listOfFiles[i].getName().substring(0, 2).equalsIgnoreCase("ag")) {

                    System.out.println("--- El archivo es un reporte de sensor, Buscando en historial...");

                    if (checkIfSensorDataExists(listOfFiles[i].getName())) {

                        System.out.println("----- El archivo ya ha sido previamente leido, se lo borrara.");

                        try {

                            Path temp = Files.move(Paths.get(listOfFiles[i].getAbsolutePath()),
                                    Paths.get("./borrados/" + listOfFiles[i].getName()));

                            System.out.println("------- El archivo ha sido borrado correctamente");

                        } catch (IOException e) {

                            e.printStackTrace();
                        }

                    } else {

                        System.out.println("----- El archivo es nuevo, intentando enviar al Reportero...");
                        sendSensorData(listOfFiles[i].getName());

                    }

                } else {

                    System.out.println("--- El archivo es JSON pero no es un reporte de sensor");
                }

            } else if (listOfFiles[i].isDirectory()) {
                // System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        System.out.println("\n----------------------");
        System.out.println("\n Se ha procesado la carpeta de lecturas correctamente, buscando cambios en 10 segundos");
        try {
            TimeUnit.SECONDS.sleep(10);
            checkSensorDataFolder();
        } catch (Exception ex) {
            System.out.println(ex);
        }

        // Checks sensor data

    }

    public static boolean checkIfSensorDataExists(String filename) {

        JSONObject sensorDataHistoryJSON = readJSON("./lecturas/history.json");

        JSONArray sensorDataHistoryArrayJSON = (JSONArray) sensorDataHistoryJSON.get("historial");

        return sensorDataHistoryArrayJSON.toString().contains(filename);

    }

    public static boolean sendSensorData(String filename) {

        JSONObject sensorDataJSON = readJSON("./lecturas/" + filename);
        String encodedJSON = sensorDataJSON.toString();

        try {

            // Se crea un Socket, con la IP local del servidor, y el puerto
            Socket s = new Socket("LOCALHOST", puerto);

            // Se crea un Stream que enviara la informacion al servidor
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            dout.writeUTF(encodedJSON);

            dout.flush();

            // Se cierra el Stream
            dout.close();

            // Se cierra el puerto
            s.close();

            System.out.println("----- El archivo fue enviado al reportero correctamente.");

            addSentDataToHistory(filename);
            try {
                TimeUnit.SECONDS.sleep(1);
                checkSensorDataFolder();
            } catch (Exception ex) {
                System.out.println(ex);
            }

        } catch (Exception e) {

            System.out.println(e);
            System.out.println("No se pudo comunicar con el reportero, intentando nuevamente en 3 segundos...");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (Exception ex) {
                System.out.println(ex);
            }

            sendSensorData(filename);
            return false;

        }
        return true;

    }

    public static void addSentDataToHistory(String filename) {

        JSONObject sensorDataHistoryJSON = readJSON("./lecturas/history.json");
        JSONArray sensorDataHistoryArrayJSON = (JSONArray) sensorDataHistoryJSON.get("historial");

        sensorDataHistoryArrayJSON.add(filename);
        sensorDataHistoryJSON.put("historial", sensorDataHistoryArrayJSON);

        try (FileWriter file = new FileWriter("lecturas/history.json")) {

            file.write(sensorDataHistoryJSON.toJSONString());
            file.flush();

            System.out.println("------- El archivo se ha agregado al historial");

        } catch (IOException e) {

            e.printStackTrace();

        }

    }


    public static void parseSensorData(JSONObject sensorDataJSON) {

        String agente = (String) sensorDataJSON.get("agente");
        String fechahoraUTC = (String) sensorDataJSON.get("fechahoraUTC");
        System.out.println(agente);
        System.out.println(fechahoraUTC);

        JSONArray lecturasArray = (JSONArray) sensorDataJSON.get("lecturas");

        for (int i = 0; i < lecturasArray.size(); i++) {
            System.out.println(lecturasArray.get(i));
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