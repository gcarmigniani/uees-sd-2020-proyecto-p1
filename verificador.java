
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
import java.nio.file.Files;
import java.nio.file.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class verificador {

    // este puerto se utilizara para la comunicacion
    public static int puerto = 9999;

    public static void main(String[] args) {

        // El Verificador arranca con una funcion que verifica la carpeta donde se
        // guardan todos los archivos de lectura de sensor subidas por los agentes
        checkSensorDataFolder();

    }

    // Esta funcion periodicamente busca en la carpeta asignada por nuevos archivos
    // de tipo JSON. En caso de encontrar un JSON con el formato de lectura de
    // sensor, primero busca si el archivo ha sido previamente leido en
    // history.json, si ya sido leido anteriormente, lo mueve a la carpeta borrados,
    // caso contrario lo procesa para enviarlo al Reportero. Ademas en caso de que
    // encuentre un archivo de no sea de extension .JSON, lo movera a la carpeta
    // "otros"
    public static void checkSensorDataFolder() {

        File folder = new File("./lecturas");
        File[] listOfFiles = folder.listFiles();

        System.out.println("\n Archivos en la carpeta lectura: \n");

        // Se crea un array de todos los archivos en la carpeta y se itera cada uno
        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {

                System.out.println("\n- " + listOfFiles[i].getName());

                // Se extrae la extension del nombre del archivo
                String extension = listOfFiles[i].getName().substring(listOfFiles[i].getName().length() - 5);

                // Si la extension no es de formato .JSON, se mueve al archivo a la carpeta
                // "otros"
                if (!extension.equalsIgnoreCase(".json")) {
                    System.out.println(
                            "--- El archivo " + listOfFiles[i].getName() + " no es JSON, moviendolo a carpeta 'otros'");

                    try {

                        Path temp = Files.move(Paths.get(listOfFiles[i].getAbsolutePath()),
                                Paths.get("./otros/" + listOfFiles[i].getName()));

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    // Si el archivo si es de formato .JSON, se busca que comienze con "ag" que
                    // significa que lo envio un agente, por ejemplo
                    // "ag002_2020-10-02T20-12-34.json"
                } else if (listOfFiles[i].getName().substring(0, 2).equalsIgnoreCase("ag")) {

                    System.out.println("--- El archivo es un reporte de sensor, Buscando en historial...");

                    // Aqui se llama esta funcion para confirmar si el archivo se ha leido
                    // previamente, en caso de haberlo leido antes, se lo movera a la carpeta
                    // "borrados", caso contrario continuara con el envio
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

                        // En caso de ser un nuevo archivo de lectura, se llamara a la funcion que lo
                        // envia al reportero
                        System.out.println("----- El archivo es nuevo, intentando enviar al Reportero...");
                        sendSensorData(listOfFiles[i].getName());

                    }

                } else {

                    // Si el archivo es .JSON pero no es del formato de lectura, se lo ignora
                    System.out.println("--- El archivo es JSON pero no es un reporte de sensor");
                }

            } else if (listOfFiles[i].isDirectory()) {
                // En caso de encontrar un directorio, no hace nada
            }
        }

        System.out.println("\n----------------------");
        System.out.println("\n Se ha procesado la carpeta de lecturas correctamente, buscando cambios en 10 segundos");

        // Una vez terminado de examinar la carpeta, la funcion espera una cantidad de
        // segundos antes de volver a llamarse asi misma recursivamente, lo que permite
        // que el programa este constantemente procesando la carpeta para encontrar
        // nuevos archivos de lectura subidos por los agentes
        try {
            TimeUnit.SECONDS.sleep(10);
            checkSensorDataFolder();
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    // Este funcion busca si el nombre del archivo de lectura enviado ya existe en
    // el historial, si lo encuentra devuelve verdadero, si no lo encuentra devuelve
    // falso
    public static boolean checkIfSensorDataExists(String filename) {

        JSONObject sensorDataHistoryJSON = readJSON("./lecturas/history.json");
        JSONArray sensorDataHistoryArrayJSON = (JSONArray) sensorDataHistoryJSON.get("historial");

        // Se crea un ArrayJSON de las lecturas previas, y este array se lo transforma
        // en un String, dentro de este String generado se utiliza la funcion .contains
        // para buscar si el nombre del archivo se encuentra en el historial de lecturas
        // y se devuelve el boolean del resultado
        return sensorDataHistoryArrayJSON.toString().contains(filename);

    }

    // Cuando se confirma que el archivo es de formato JSON y es una lectura de
    // sensor, se lo abre llamando a la funcion readJSON que devuelve un objeto JSON
    // del archivo, con este archivo se habre un puerto defino y se crea un
    // DataStream para enviar al Reportero que opera en otro sistema
    public static boolean sendSensorData(String filename) {

        JSONObject sensorDataJSON = readJSON("./lecturas/" + filename);

        // Se transforma el objeto JSON en un String para poder enviarlo en formato UTF
        // por el DataStream, lo que permite tambien la operabilidad con sistemas que no
        // necesarimente utilzien Java pero pueden hacer uso del JSON en formato String

        String encodedJSON = sensorDataJSON.toString();

        try {

            // Se crea un Socket, con la IP local del servidor, y el puerto
            Socket s = new Socket("LOCALHOST", puerto);

            // Se crea un Stream que enviara la informacion al servidor
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // Se escribe el String JSON en formato UTF
            dout.writeUTF(encodedJSON);

            // Se envia el mensaje
            dout.flush();

            // Se cierra el Stream
            dout.close();

            // Se cierra el puerto
            s.close();

            System.out.println("----- El archivo fue enviado al reportero correctamente.");

            // Una vez enviado el archivo, se agrega el nombre del archivo al historial para
            // no volver a enviar un archivo duplicado en el futuro
            addSentDataToHistory(filename);

            // Ye enviado el archivo, se llama de nuevoi a la funcion que revisa la carpeta
            // de lecturas, pero se espera 1 segundo antes de volver a llamarla para de esa
            // forma evitar el congestionamiento del DataStream o del Reportero, dandole
            // tiempo para procesar cada lectura enviada
            try {
                TimeUnit.SECONDS.sleep(1);
                checkSensorDataFolder();
            } catch (Exception ex) {
                System.out.println(ex);
            }

        } catch (Exception e) {

            // En caso de no poder enviar el mensaje con el JSON al reportero, se muestra el
            // mensaje e intenta repetir el envio del mensaje al reportero cada 3 segundos
            // hasta que responda
            System.out.println(e);
            System.out.println("No se pudo comunicar con el reportero, intentando nuevamente en 3 segundos...");
            
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            // Se llama a la funcion recursivamente si no se efectuo el envio del mensaje
            sendSensorData(filename);
            return false;

        }
        // En caso de enviarse el mensaje correctamente, termina la funcion y regresa
        // verdadero
        return true;

    }

    // Esta funcion toma el nombre del archivo enviado y lo agrega la lista de
    // historial par que no enviado duplicadamente
    public static void addSentDataToHistory(String filename) {

        // Se carga el JSON history.json que contiene los nombres de los archivos
        // enviados
        JSONObject sensorDataHistoryJSON = readJSON("./lecturas/history.json");
        JSONArray sensorDataHistoryArrayJSON = (JSONArray) sensorDataHistoryJSON.get("historial");

        // Se agrega el nombre del archivo de la lectura enviada al historial
        sensorDataHistoryArrayJSON.add(filename);
        sensorDataHistoryJSON.put("historial", sensorDataHistoryArrayJSON);

        // Se guarda el archivo JSON historial dentro de la carpeta lecturas
        try (FileWriter file = new FileWriter("lecturas/history.json")) {

            file.write(sensorDataHistoryJSON.toJSONString());
            file.flush();

            System.out.println("------- El archivo se ha agregado al historial");

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