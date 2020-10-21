# uees-sd-2020-proyecto-p1
# Giuseppe Carmigniani

## Instalacion

Para ejectuar el programa, es necesario tener el Java JRE y JDK, para lo cual se puede usar los siguientes comandos

```
sudo apt install default-jre
sudo apt install default-jdk
```
## Compilacion

En la maquina del reportero, se debe compilar el archivo reportero.java usando el siguiente comando:

Hay que tomar en cuenta que es necesario usa la libreria json-simple-1.1.1.jar incluida, por lo que se debe pasar su ubicacion como argumento

```
javac -cp '% UBICACION DEL PROYECTO %/uees-sd-2020-proyecto-p1/json-simple-1.1.1.jar' reportero.java -Xlint:unchecked
```
Por ejemplo: 
```
javac -cp '.:/home/carmigniani-sandoval-nodo1/Desktop/uees-sd-2020-proyecto-p1/json-simple-1.1.1.jar' reportero.java -Xlint:unchecked
```

Para el verificador, se compila el archivo mensajeCliente.java usando este comando, igualmente se usa el argumento de la ubicacion de la libreria

```
javac -cp '% UBICACION DEL PROYECTO %/uees-sd-2020-proyecto-p1/json-simple-1.1.1.jar' verificador.java -Xlint:unchecked
```

De ser necesario, el verificador.java debe ser modificado ingresando el puerto y la ip del reportero que recibira los mensajes

## Ejecucion

Una vez compilados los archivos java, primero se inicia la maquina del reportero usando el comando
```
java -cp '% UBICACION DEL PROYECTO %/uees-sd-2020-proyecto-p1/json-simple-1.1.1.jar' reportero
```

Ahora se ejecuta el programa del lado del verificador, usando el comando
```
java -cp '% UBICACION DEL PROYECTO %/uees-sd-2020-proyecto-p1/json-simple-1.1.1.jar' verificador
```

Automaticamente el verificador revisara la carpeta de lecturas y enviara las lecturas no duplicadas al reportero para que las procese, los resultados seran guardados en la carpeta de registros.

## Estructura de carpetas

El proyecto tiene 4 carpetas

### Lecturas
En esta carpeta se ponen los documentos .json de los agentes

### Borrados
Los archivos .json previamente leidos que son duplicados iran a esta carpeta

### Otros
Aqui se envian archivos de otros formatos que no sean .json 

### Registros
Aqui se guardan los reportes generador por el reportador en formato JSON 
